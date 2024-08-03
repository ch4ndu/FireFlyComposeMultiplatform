package app.viewmodel

import di.DispatcherProvider
import domain.atBeginningOfMonth
import domain.currentDate
import domain.minusMonths
import domain.model.DateRange
import domain.model.ExpenseData
import domain.repository.AccountRepository
import domain.repository.TransactionRepository
import domain.usecase.GetBalanceChartDataUseCase
import domain.usecase.GetCashFlowUseCase
import domain.usecase.GetOverallSpendingUseCase
import domain.withEndOfMonthAtEndOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getOverallSpendingUseCase: GetOverallSpendingUseCase,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val getCashFlowUseCase: GetCashFlowUseCase,
    private val dispatcherProvider: DispatcherProvider,
    getBalanceChartDataUseCase: GetBalanceChartDataUseCase
) : BaseViewModel() {

    private val dailyExpenseDataSelected = MutableStateFlow<ExpenseData?>(null)

    val dailySpendingFlow =
        getOverallSpendingUseCase.getOverallSpendingByDay(currentDate(), 30)
            .toStateFlow(initial = emptyList())

    val cashFlowDetails = getCashFlowUseCase.getCashFlowForDateRange(
        DateRange(
            currentDate().minusMonths(12).atBeginningOfMonth(),
            currentDate().withEndOfMonthAtEndOfDay()
        )
    ).toStateFlow(
        initial = emptyList()
    )

    fun updateSelectedExpenseData(expenseData: ExpenseData) {
        dailyExpenseDataSelected.value = expenseData
    }

    val netWorthChartDataFlow =
        accountRepository.getAssetAccountListFlow().flatMapLatest { accountList ->
            getBalanceChartDataUseCase.getCombinedChartData(
                DateRange(
                    currentDate().minusMonths(12).atBeginningOfMonth(),
                    currentDate().withEndOfMonthAtEndOfDay()
                ), accountList
            )
        }.flowOn(dispatcherProvider.io).toStateFlow(initial = null)

    val netWorthLineDataSetFlow =
        netWorthChartDataFlow.flatMapLatest { groupedExpenseData ->
            flowOf(groupedExpenseData?.expenseDataList)
        }.toStateFlow(initial = null)
}