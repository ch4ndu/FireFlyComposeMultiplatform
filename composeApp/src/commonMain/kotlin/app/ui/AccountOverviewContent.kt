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

package app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.model.MergeMode
import app.theme.FireflyAppTheme
import data.database.model.transaction.FireFlyTransaction
import data.database.serializers.DateSerializer
import domain.model.DateRange
import domain.model.ExpenseData
import domain.model.ExpenseIncomeData
import domain.model.GroupedExpenseData
import getDisplayWithCurrency
import kotlinx.datetime.format


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountOverview(
    accountId: String,
    topExpenseList: List<FireFlyTransaction>,
    chartDataList: List<ExpenseData>?,
    chartData: GroupedExpenseData?,
    spendingByMonth: List<ExpenseData>,
    cashFlowData: List<ExpenseIncomeData>,
    tabVisible: (visible: Boolean) -> Unit,
    tabRowHeight: Dp,
    dateRange: DateRange,
    onAccountCashFlowDetailsClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val isScrollingUp = listState.isScrollingUp()
    tabVisible.invoke(isScrollingUp)
    val dimensions = FireflyAppTheme.dimensions
    val isLargeScreen = isLargeScreen()
    val aspectRatio = remember(isLargeScreen.value) {
        getAspectRatio(isLargeScreen.value)
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(dimensions.listSpacing),
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(dimensions.contentMargin)
    ) {

        item(key = "tabRowPadding") {
            Spacer(modifier = Modifier.height(tabRowHeight))
        }

        item(key = "topPadding") {
            Spacer(modifier = Modifier.height(dimensions.listSpacing))
        }

        if (chartData != null) {
            item(
                key = "lineChart",
                contentType = "lineChart"
            ) {
                ThemedBox {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(top = dimensions.verticalPadding)
                                .align(Alignment.CenterHorizontally),
                            text = "Balance Trend",
                            style = FireflyAppTheme.typography.headlineMedium
                        )
                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio)
                                .padding(horizontal = dimensions.horizontalPadding),
                            chartDataList = chartDataList
                        )
                        val firstEntry = chartData.expenseDataList.getOrNull(0)
                        val firstAmount = firstEntry?.expenseAmount ?: 0f
                        val startDateDisplay =
                            dateRange.startDate.format(DateSerializer.chartMonthYearFormat)
                        val lastEntry =
                            chartData.expenseDataList.getOrNull(chartData.expenseDataList.count() - 1)
                        val lastAmount = lastEntry?.expenseAmount ?: 0f
                        val endDateDisplay =
                            dateRange.endDate.format(DateSerializer.chartDayMonthYearFormat)
                        Row(
                            modifier = Modifier.padding(
                                vertical = 5.dp,
                                horizontal = dimensions.contentMargin
                            )
                        ) {
                            Text(
                                text = "Balance on ${startDateDisplay}: ",
                                style = FireflyAppTheme.typography.titleMedium
                            )
                            Text(
                                text = lastAmount.getDisplayWithCurrency("$"),
                                style = FireflyAppTheme.typography.titleLarge.copy(
                                    color = lastAmount.getTextColorForAmount()
                                )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .padding(bottom = 5.dp)
                                .padding(horizontal = dimensions.contentMargin)
                        ) {
                            Text(
                                text = "Balance on ${endDateDisplay}: ",
                                style = FireflyAppTheme.typography.titleMedium
                            )
                            Text(
                                text = firstAmount.getDisplayWithCurrency("$"),
                                style = FireflyAppTheme.typography.titleLarge.copy(
                                    color = firstAmount.getTextColorForAmount()
                                )
                            )
                        }
                    }
                }
            }
        }

        if (cashFlowData.isNotEmpty()) {
            item(
                key = "cashFlowOverview",
                contentType = "cashFlowOverview"
            ) {
                ThemedBox {
                    DetailsIconButton(boxScope = this, onClick = onAccountCashFlowDetailsClick)
                    Column {
                        Text(
                            text = "CashFlow Overview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 6.dp),
                            textAlign = TextAlign.Center
                        )
                        BarChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio),
                            dataList = cashFlowData,
                            mergeMode = MergeMode.Stacked,
                            dateTimeFormatter = DateSerializer.chartMonthYearFormat
                        ) {
                        }
                    }
                }
            }
        }
        if (topExpenseList.isNotEmpty()) {
            item(
                key = "transactionSpacer",
                contentType = "transactionSpacerContent"
            ) {
                Text(
                    text = "Top transactions for the period",
                    modifier = Modifier
                        .padding(horizontal = dimensions.contentMargin),
                    style = FireflyAppTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(dimensions.listSpacing))
            }

            itemsIndexed(
                topExpenseList,
                contentType = { _, _ ->
                    "transactionItem"
                },
                key = { _, transaction ->
                    transaction.transaction_journal_id
                }
            ) { _, transaction ->
                TransactionItem(accountId = accountId, item = transaction)
            }

            item(key = "spacer") {
                Spacer(modifier = Modifier.height(dimensions.listSpacing))
            }

        }
        item(key = "bottomSpacer") { Spacer(modifier = Modifier.height(BottomSheetDefaults.SheetPeekHeight)) }
    }
}