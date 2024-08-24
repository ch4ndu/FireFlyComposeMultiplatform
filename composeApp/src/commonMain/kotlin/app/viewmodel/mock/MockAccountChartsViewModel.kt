package app.viewmodel.mock

import androidx.lifecycle.SavedStateHandle
import app.viewmodel.AccountChartsViewModel
import di.DispatcherProvider
import domain.currentDate
import domain.minusMonths
import domain.model.DateRange
import domain.model.ExpenseData
import domain.model.ExpenseIncomeData
import domain.model.GroupedExpenseData
import domain.repository.AccountRepository
import domain.usecase.GetAccountSpendingUseCase
import domain.usecase.GetBalanceChartDataUseCase
import domain.usecase.GetCashFlowUseCase
import domain.usecase.GetOverallSpendingUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockAccountChartsViewModel(
    val dispatcherProvider: DispatcherProvider,
    getBalanceChartDataUseCase: GetBalanceChartDataUseCase,
    getAccountSpendingUseCase: GetAccountSpendingUseCase,
    getOverallSpendingUseCase: GetOverallSpendingUseCase,
    getCashFlowUseCase: GetCashFlowUseCase,
    accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : AccountChartsViewModel(
    dispatcherProvider,
    getBalanceChartDataUseCase,
    getAccountSpendingUseCase,
    getOverallSpendingUseCase,
    getCashFlowUseCase,
    accountRepository,
    savedStateHandle
) {
    override val chartDataFlow: Flow<GroupedExpenseData?>
        get() = flow {
            emit(
                GroupedExpenseData(
                    name = "CheckingAccount",
                    DateRange(startDate = currentDate().minusMonths(6), endDate = currentDate()),
                    expenseDataList = MockData.mockExpenseList2
                )
            )
        }

    override val spendingDataFlow: Flow<List<ExpenseData>>
        get() = flow {
            emit(MockData.mockExpenseList.shuffled())
        }

    override val cashFlowData: Flow<List<ExpenseIncomeData>>
        get() = flow {
            emit(MockData.mockExpenseIncomeList)
        }
}