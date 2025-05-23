package app.ui

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import data.database.serializers.DateSerializer
import domain.model.ExpenseData
import domain.model.ExpenseIncomeData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeFormat
import toChartLabel

class MarkerLabelFormatter(
    private val spendingDataList: List<Any>,
    private val dateTimeFormatter: DateTimeFormat<LocalDateTime> = DateSerializer.chartMonthYearFormat,
    private val showSpendingLabel: Boolean,
    colorCode: Boolean = true
) : DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val index = targets[0].x.toInt()
        val data = spendingDataList[index]
        if (data is ExpenseIncomeData) {
            return data.toChartLabel(dateTimeFormatter)
        } else if (data is ExpenseData) {
            return data.toChartLabel(showSpendingLabel, dateTimeFormatter)
        }
        return "unknown"
    }
}