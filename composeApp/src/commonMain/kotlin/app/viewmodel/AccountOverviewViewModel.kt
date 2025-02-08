@file:OptIn(ExperimentalPagingApi::class)

package app.viewmodel

import Constants
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import app.model.TotalsSummary
import app.ui.areDateRangesEquivalent
import data.database.dao.FireFlyTransactionDataDao
import data.database.model.transaction.FireFlyTransaction
import data.enums.ExpenseType
import di.DispatcherProvider
import domain.model.DateRange
import domain.repository.TransactionRepository
import domain.usecase.SyncWithServerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.lighthousegames.logging.logging

@OptIn(ExperimentalCoroutinesApi::class)
open class AccountOverviewViewModel(
    private val transactionRepository: TransactionRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val fireFlyTransactionDataDao: FireFlyTransactionDataDao,
    private val syncWithServerUseCase: SyncWithServerUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val loadState = MutableStateFlow(LoadState.Idle)

    private val accountIdFlow = MutableStateFlow("0")
    private val accountTypeFlow = MutableStateFlow("asset")

    fun setAccountId(value: String) {
        accountIdFlow.value = value
    }

//    private val accountIdFlow =
//        savedStateHandle.getStateFlow("accountId", "0")
//            .distinctUntilChanged(
//                areEquivalent = { old: String, new: String ->
//                    old.compareTo(new) == 0
//                })

    private val expenseTypeFlow =
        MutableStateFlow(listOf(ExpenseType.EXPENSE, ExpenseType.INCOME, ExpenseType.TRANSFER))

    fun setExpenseTypes(types: List<ExpenseType>) {
        expenseTypeFlow.value = types
    }

    private val dateRangeFlow =
        MutableStateFlow(
            DateRange(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        )

    val activeTag = MutableStateFlow("")
    val activeCategory = MutableStateFlow("")

    fun setDateRange(dateRange: DateRange) {
        dateRangeFlow.value = dateRange
    }

    open val topTransactionsFlow = accountIdFlow.flatMapLatest { accountId ->
        dateRangeFlow
            .distinctUntilChanged(areDateRangesEquivalent)
            .flatMapLatest { dateRange ->
                transactionRepository.getTopTransactionsForAccountFlow(
                    accountId = accountId.toLong(),
                    startDate = dateRange.startDate,
                    endDate = dateRange.endDate
                ).flowOn(dispatcherProvider.io)
            }
    }
    private val log = logging()

    open fun refreshRemoteData() {
        log.d { "refreshRemoteData" }
        loadState.value = LoadState.Loading
        viewModelScope.launch(dispatcherProvider.default) {
            syncWithServerUseCase.invoke()
            loadState.value = LoadState.Idle
        }
    }

    open val tagsForAccountWithDate =
        dateRangeFlow
            .distinctUntilChanged(areDateRangesEquivalent)
            .flatMapLatest { dateRange ->
                accountIdFlow.flatMapLatest { accountId ->
                    fireFlyTransactionDataDao.getTagsForAccountByDate(
                        startDate = dateRange.startDate,
                        endDate = dateRange.endDate,
                        accountId = accountId.toLong()
                    ).distinctUntilChanged()
                        .map { tagList ->
                            val splitTags = mutableListOf<String>()
                            tagList.forEach { tag ->
                                val split = tag.split(",")
                                split.forEach {
                                    if (it.isNotEmpty())
                                        splitTags.add(it)
                                }
                            }
                            val tagSet = splitTags.toSet()
                            tagSet.toList()
                        }.distinctUntilChanged()
                }
            }.flowOn(dispatcherProvider.io)

    open val tagSummaryAccount by lazy {
        combine(
            dateRangeFlow
                .distinctUntilChanged(areDateRangesEquivalent),
            accountIdFlow,
            tagsForAccountWithDate,
            expenseTypeFlow
        ) { dateRange, accountId, tags, expenseTypeList ->
            val tagTotalsMap = HashMap<String, TotalsSummary>()
            withContext(dispatcherProvider.io) {
                for (tag in tags) {
                    val expenseSum =
                        if (expenseTypeList.contains(ExpenseType.EXPENSE))
                            fireFlyTransactionDataDao.getTransactionSumByAccountAndTagAndType(
                                startDate = dateRange.startDate,
                                endDate = dateRange.endDate,
                                tag = tag,
                                types = Constants.NEW_EXPENSE_TRANSACTION_TYPES,
                                accountId = accountId.toLong()
                            ) else 0.0
                    val incomeSum =
                        if (expenseTypeList.contains(ExpenseType.INCOME))
                            fireFlyTransactionDataDao.getTransactionSumByAccountAndTagAndType(
                                startDate = dateRange.startDate,
                                endDate = dateRange.endDate,
                                tag = tag,
                                types = Constants.NEW_INCOME_TRANSACTION_TYPES,
                                accountId = accountId.toLong()
                            ) else 0.0
                    val transferSum =
                        if (expenseTypeList.contains(ExpenseType.TRANSFER))
                            fireFlyTransactionDataDao.getTransactionSumByAccountAndTagAndType(
                                startDate = dateRange.startDate,
                                endDate = dateRange.endDate,
                                tag = tag,
                                types = Constants.TRANSFER_TRANSACTION_TYPES,
                                accountId = accountId.toLong()
                            ) else 0.0
                    tagTotalsMap[tag] = TotalsSummary(
                        expenseSum = expenseSum,
                        incomeSum = incomeSum,
                        transferSum = transferSum
                    )
                }
            }
            tagTotalsMap
        }
    }

    open val transactionsForActiveTag by lazy {
        dateRangeFlow
            .distinctUntilChanged(areDateRangesEquivalent)
            .flatMapLatest { dateRange ->
                accountIdFlow.flatMapLatest { accountId ->
                    activeTag.flatMapLatest { activeTag ->
                        expenseTypeFlow.flatMapLatest { expenseTypeList ->
                            val typeList = mutableListOf<String>()
                            expenseTypeList.forEach {
                                when (it) {
                                    ExpenseType.EXPENSE ->
                                        typeList.addAll(Constants.NEW_EXPENSE_TRANSACTION_TYPES)

                                    ExpenseType.INCOME ->
                                        typeList.addAll(Constants.NEW_INCOME_TRANSACTION_TYPES)

                                    ExpenseType.TRANSFER ->
                                        typeList.addAll(Constants.TRANSFER_TRANSACTION_TYPES)
                                }
                            }
                            if (activeTag.isNotEmpty()) {
                                fireFlyTransactionDataDao.getTransactionsFromAccountByTagAndType(
                                    startDate = dateRange.startDate,
                                    endDate = dateRange.endDate,
                                    types = typeList,
                                    accountId = accountId.toLong(),
                                    tag = activeTag
                                ).flatMapLatest { transactionList ->
                                    flowOf(transactionList.sortedByDescending { it.amount })
                                }
                            } else {
                                flowOf(emptyList())
                            }
                        }
                    }
                }
            }
    }

    open val categoriesForAccountWithDate by lazy {
        dateRangeFlow
            .distinctUntilChanged(areDateRangesEquivalent)
            .flatMapLatest { dateRange ->
                accountIdFlow.flatMapLatest { accountId ->
                    fireFlyTransactionDataDao.getCategoriesForAccountByDate(
                        startDate = dateRange.startDate,
                        endDate = dateRange.endDate,
                        accountId = accountId.toLong()
                    ).flowOn(dispatcherProvider.io)
                        .distinctUntilChanged()
                        .map { categoryList ->
                            val categorySet = categoryList.toSet()
                            categorySet.toList()
                        }.distinctUntilChanged()
                }
            }
    }

    open val categorySummaryAccount by lazy {
        combine(
            dateRangeFlow.distinctUntilChanged(areDateRangesEquivalent),
            accountIdFlow,
            categoriesForAccountWithDate,
            expenseTypeFlow
        ) { dateRange, accountId, categories, expenseTypeList ->
            val categoriesTotalsMap = HashMap<String, TotalsSummary>()
            withContext(dispatcherProvider.io) {
                for (category in categories) {
                    val expenseSum = if (expenseTypeList.contains(ExpenseType.EXPENSE))
                        fireFlyTransactionDataDao.getTransactionSumByAccountAndCategoryAndType(
                            startDate = dateRange.startDate,
                            endDate = dateRange.endDate,
                            category = category,
                            types = Constants.NEW_EXPENSE_TRANSACTION_TYPES,
                            accountId = accountId.toLong()
                        ) else 0.0
                    val incomeSum = if (expenseTypeList.contains(ExpenseType.INCOME))
                        fireFlyTransactionDataDao.getTransactionSumByAccountAndCategoryAndType(
                            startDate = dateRange.startDate,
                            endDate = dateRange.endDate,
                            category = category,
                            types = Constants.NEW_INCOME_TRANSACTION_TYPES,
                            accountId = accountId.toLong()
                        ) else 0.0
                    val transferSum = if (expenseTypeList.contains(ExpenseType.TRANSFER))
                        fireFlyTransactionDataDao.getTransactionSumByAccountAndCategoryAndType(
                            startDate = dateRange.startDate,
                            endDate = dateRange.endDate,
                            category = category,
                            types = Constants.TRANSFER_TRANSACTION_TYPES,
                            accountId = accountId.toLong()
                        ) else 0.0
                    categoriesTotalsMap[category] = TotalsSummary(
                        expenseSum = expenseSum,
                        incomeSum = incomeSum,
                        transferSum = transferSum
                    )
                }
            }
            categoriesTotalsMap
        }
    }

    open val transactionsForActiveCategory by lazy {
        dateRangeFlow.flatMapLatest { dateRange ->
            accountIdFlow.flatMapLatest { accountId ->
                activeCategory.flatMapLatest { activeCategory ->
                    expenseTypeFlow.flatMapLatest { expenseTypeList ->
                        val typeList = mutableListOf<String>()
                        expenseTypeList.forEach {
                            when (it) {
                                ExpenseType.EXPENSE ->
                                    typeList.addAll(Constants.NEW_EXPENSE_TRANSACTION_TYPES)

                                ExpenseType.INCOME ->
                                    typeList.addAll(Constants.NEW_INCOME_TRANSACTION_TYPES)

                                ExpenseType.TRANSFER ->
                                    typeList.addAll(Constants.TRANSFER_TRANSACTION_TYPES)
                            }
                        }
                        if (activeCategory.isNotEmpty()) {
                            fireFlyTransactionDataDao.getTransactionsFromAccountByCategoryAndType(
                                startDate = dateRange.startDate,
                                endDate = dateRange.endDate,
                                accountId = accountId.toLong(),
                                category = activeCategory,
                                expenseTypeList = typeList
                            ).flatMapLatest { transactionList ->
                                flowOf(transactionList.sortedByDescending { it.amount })
                            }
                        } else {
                            flowOf(emptyList())
                        }
                    }.flowOn(dispatcherProvider.io)
                }
            }
        }
    }

    open fun getNewTransactionListForAccount(
        accountType: String,
        accountId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<FireFlyTransaction>> {
        return transactionRepository.getTransactionByAccountAndDate(
            accountType = accountType,
            accountId = accountId,
            startDate = startDate,
            endDate = endDate
        )
    }
}