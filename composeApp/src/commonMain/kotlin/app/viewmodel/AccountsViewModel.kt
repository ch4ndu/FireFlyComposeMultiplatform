package app.viewmodel

import Platform
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import data.AppPref
import di.DispatcherProvider
import domain.repository.AccountRepository
import domain.usecase.SyncWithServerUseCase
import io.ktor.util.PlatformUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
open class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val syncWithServerUseCase: SyncWithServerUseCase,
    appPref: AppPref
) : ViewModel() {

    private val accountType = MutableStateFlow("asset")

    open val accountList = accountType.flatMapLatest { accountType ->
        accountRepository.getAccountList(accountType)
    }

//    val accountList = accountType.mapLatest { accountType ->
//        Pager(
//            PagingConfig(
//                pageSize = Constants.PAGE_SIZE,
//                enablePlaceholders = false
//            ),
////            remoteMediator = accountRepository.loadNetworkData(accountType)
//            remoteMediator = null
//        ) {
//            accountRepository.getAccountList(accountType)
//        }
//    }

    open val lastSyncedAt = appPref.lastSyncedAt()
        .flowOn(dispatcherProvider.io)
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )

    private val log = logging()

    open fun refreshData() {
        log.d { "refreshRemoteData" }
        viewModelScope.launch(dispatcherProvider.default) {
            syncWithServerUseCase.invoke()
        }
    }
}