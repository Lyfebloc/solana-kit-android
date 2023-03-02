package com.lyfebloc.solanakit.core

import com.lyfebloc.solanakit.SolanaKit
import com.lyfebloc.solanakit.noderpc.ApiSyncer
import com.lyfebloc.solanakit.noderpc.IApiSyncerListener
import com.lyfebloc.solanakit.noderpc.SyncerState
import com.lyfebloc.solanakit.transactions.ITransactionListener
import com.lyfebloc.solanakit.transactions.TransactionManager
import com.lyfebloc.solanakit.transactions.TransactionSyncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface ISyncListener {
    fun onUpdateBalanceSyncState(syncState: SolanaKit.SyncState)
    fun onUpdateTokenSyncState(syncState: SolanaKit.SyncState)
    fun onUpdateTransactionSyncState(syncState: SolanaKit.SyncState)
    fun onUpdateLastBlockHeight(lastBlockHeight: Long)
    fun onUpdateBalance(balance: Long)
}

class SyncManager(
    private val apiSyncer: ApiSyncer,
    private val balanceSyncer: BalanceManager,
    private val tokenAccountSyncer: TokenAccountManager,
    private val transactionSyncer: TransactionSyncer,
    private val transactionManager: TransactionManager
) : IApiSyncerListener, IBalanceListener, ITokenAccountListener, ITransactionListener {

    private var scope: CoroutineScope? = null

    var listener: ISyncListener? = null

    val balanceSyncState: SolanaKit.SyncState
        get() = balanceSyncer.syncState

    val tokenBalanceSyncState: SolanaKit.SyncState
        get() = tokenAccountSyncer.syncState

    val transactionsSyncState: SolanaKit.SyncState
        get() = transactionSyncer.syncState

    private var started = false
    private var forceSyncing = false

    init {
        balanceSyncer.listener = this
        apiSyncer.listener = this
        tokenAccountSyncer.listener = this
        transactionSyncer.listener = this
    }

    suspend fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        this.scope = scope

        apiSyncer.start(scope)
        balanceSyncer.start()
        tokenAccountSyncer.start()

        scope.launch {
            transactionManager.transactionsFlow
                .collect { balanceSyncer.sync() }
        }
    }

    suspend fun refresh(scope: CoroutineScope) {
        if (forceSyncing) return

        when (apiSyncer.state) {
            SyncerState.Ready -> {
                forceSyncing = true
                balanceSyncer.sync()
                tokenAccountSyncer.sync()
                transactionSyncer.sync()
            }
            is SyncerState.NotReady -> {
                start(scope)
            }
        }
    }

    fun stop() {
        started = false
        scope = null

        apiSyncer.stop()
        balanceSyncer.stop()
        tokenAccountSyncer.stop()
    }

    override fun didUpdateApiState(state: SyncerState) {
        when (state) {
            SyncerState.Ready -> {
                scope?.launch {
                    balanceSyncer.sync()
                    tokenAccountSyncer.sync()
                }
            }
            is SyncerState.NotReady -> {
                scope?.launch {
                    balanceSyncer.stop(state.error)
                    tokenAccountSyncer.stop(state.error)
                }
            }
        }
    }

    override fun onUpdateTokenSyncState(syncState: SolanaKit.SyncState) {
        scope?.launch {
            listener?.onUpdateTokenSyncState(syncState)
            checkForceSyncFinished()
        }
    }

    override fun onUpdateTransactionSyncState(syncState: SolanaKit.SyncState) {
        scope?.launch {
            listener?.onUpdateTransactionSyncState(syncState)
            checkForceSyncFinished()
        }
    }

    override fun onUpdateBalanceSyncState(value: SolanaKit.SyncState) {
        scope?.launch {
            listener?.onUpdateBalanceSyncState(value)
            checkForceSyncFinished()
        }
    }

    override fun didUpdateLastBlockHeight(lastBlockHeight: Long) {
        scope?.launch {
            listener?.onUpdateLastBlockHeight(lastBlockHeight)
            transactionSyncer.sync()
        }
    }

    override fun onUpdateBalance(balance: Long) {
        scope?.launch {
            listener?.onUpdateBalance(balance)
        }
    }

    private fun checkForceSyncFinished() {
        if (
            balanceSyncer.syncState is SolanaKit.SyncState.Synced &&
            tokenAccountSyncer.syncState is SolanaKit.SyncState.Synced &&
            transactionSyncer.syncState is SolanaKit.SyncState.Synced
        ) {
            forceSyncing = false
        }
    }

}
