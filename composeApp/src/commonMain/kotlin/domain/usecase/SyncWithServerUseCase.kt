/*
 * Copyright (c) 2025 https://github.com/ch4ndu
 *
 *  This file is part of Rundown (https://github.com/ch4ndu/Rundown).
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package domain.usecase

import data.AppPref
import data.database.serializers.DateSerializer
import di.DispatcherProvider
import domain.model.DateRange
import domain.repository.AccountRepository
import domain.repository.BudgetRepository
import domain.repository.ChartsRepository
import domain.repository.TransactionRepository
import domain.withStartOfYear
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.lighthousegames.logging.logging
import kotlin.coroutines.cancellation.CancellationException

class SyncWithServerUseCase(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val appPref: AppPref,
    private val budgetRepository: BudgetRepository,
    private val chartsRepository: ChartsRepository,
    private val dispatcherProvider: DispatcherProvider,) {
    private val log = logging()

    suspend operator fun invoke(
    ): SyncStatus {
        val startedAt = """startedAt-${
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                .format(DateSerializer.displayFormat)
        }"""
        return withContext(dispatcherProvider.default) {
            log.d { "doWork:start" }
            try {
                appPref.setLastSyncedAt(startedAt)
                accountRepository.loadAccountDataFromNetwork("asset")
                val tz = TimeZone.currentSystemDefault()
                val now = Clock.System.now().toLocalDateTime(tz)
                Clock.System.now().minus(DateTimePeriod(years = 3), tz)
                val dateRange = DateRange(
                    Clock.System.now().minus(DateTimePeriod(years = 3), tz).toLocalDateTime(tz)
                        .withStartOfYear(),
                    now
                )
                val accountList = accountRepository.getAccountListFlow("asset").first()
                accountList.forEach {
                    withContext(dispatcherProvider.default) {
                        val temp = transactionRepository.loadAllAccountTransactionsFromNetwork(
                            it.id,
                            dateRange.startDate,
                            dateRange.endDate,
                        )
                    }
                }
                chartsRepository.loadRemoteChartData(dateRange.startDate, dateRange.endDate)
                budgetRepository.loadAllBudgets()
                log.d { "doWork: done" }
                appPref.setLastSyncedAt(
                    "$startedAt::EndedAt-${
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(DateSerializer.displayFormat)
                    }"
                )
                SyncStatus.SyncSuccess
            } catch (ex: Throwable) {
                var exReason = ex.toString()
                if (ex is CancellationException) {
                    exReason = "${ex.message}:$exReason"
                }
                log.e(ex) { "doWork failed" }
                appPref.setLastSyncedAt(
                    "$startedAt::EndedAt-${
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(DateSerializer.displayFormat)
                    }::Error-${exReason}"
                )
                SyncStatus.SyncFailed(ex)
            }
        }
    }

}

sealed class SyncStatus {
    data object SyncSuccess : SyncStatus()
    data class SyncFailed(val exception: Throwable?) : SyncStatus()
}