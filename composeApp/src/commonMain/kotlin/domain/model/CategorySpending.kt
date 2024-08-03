package domain.model

data class CategorySpending(
    val categoryName: String,
    val expenseIncomeData: List<ExpenseIncomeData>,
    val expenseSum: Float,
    val incomeSum: Float
) {

//    fun getDisplayOverView(): AnnotatedString {
//        return buildAnnotatedString {
//            if (expenseSum > 0) {
//                withStyle(SpanStyle(GrillRed)) {
//                    append("Expenses: ${expenseSum.getDisplayWithCurrency("$")}")
//                }
//            }
//            if (incomeSum > 0) {
//                withStyle(SpanStyle(DeltaGekko)) {
//                    append("\r\nIncome: ${incomeSum.getDisplayWithCurrency("$")}")
//                }
//            }
//        }
//    }
}
