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

package data.database.model.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Errors(
        val name: List<String>?,
        val account_id: List<String>?,
        val current_amount: List<String>?,
        val targetDate: List<String>?,
        val currency_code: List<String>?,
        val amount_min: List<String>?,
        val repeat_freq: List<String>?,
        @SerialName("transactions.0.destination_name")
        val transactions_destination_name: List<String>?,
        @SerialName("transactions.0.destination_id")
        val transaction_destination_id: List<String>?,
        @SerialName("transactions.0.currency_code")
        val transactions_currency: List<String>?,
        @SerialName("transactions.0.source_name")
        val transactions_source_name: List<String>?,
        @SerialName("transactions.0.source_id")
        val transactions_source_id: List<String>?,
        @SerialName("transactions.0.budget_name")
        val transactions_budget_name: List<String>?,
        val bill_name: List<String>?,
        val piggy_bank_name: List<String>?,
        val account_number: List<String>?,
        val interest: List<String>?,
        @SerialName("liability_start_date")
        val liabilityStartDate: List<String>?,
        @SerialName("transactions.0.amount")
        val transaction_amount: List<String>?,
        val description: List<String>?,
        val date: List<String>?,
        val skip: List<String>?,
        val code: List<String>?,
        val symbol: List<String>?,
        @SerialName("decimal_places")
        val decimalPlaces: List<String>?,
        val tag: List<String>?,
        val latitude: List<String>?,
        val longitude: List<String>?,
        @SerialName("zoom_level")
        val zoomLevel: List<String>?,
        val iban: List<String>?,
        val bic: List<String>?,
        val opening_balance: List<String>?,
        val opening_balance_date: List<String>?,
        val interest_period: List<String>?,
        val liability_amount: List<String>?,
        val exception: List<String>?
)