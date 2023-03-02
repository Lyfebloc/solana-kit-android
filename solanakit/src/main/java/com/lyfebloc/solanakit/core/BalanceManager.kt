package com.lyfebloc.solanakit.core

import com.solana.api.Api
import com.solana.api.getBalance
import com.solana.core.PublicKey
import com.lyfebloc.solanakit.SolanaKit
import com.lyfebloc.solanakit.database.main.MainStorage

interface IBalanceListener {
    fun onUpdateBalanceSyncState(value: SolanaKit.SyncState)
    fun onUpdateBalance(balance: Long)
}

class BalanceManager(
    private val publicKey: PublicKey,
    private val rpcClient: Api,
    private val storage: MainStorage
) {

    var syncState: SolanaKit.SyncState = SolanaKit.SyncState.NotSynced(SolanaKit.SyncError.NotStarted())
        private set(value) {
            if (value != field) {
                field = value
                listener?.onUpdateBalanceSyncState(value)
            }
        }

    var listener: IBalanceListener? = null

    var balance: Long? = storage.getBalance()
        private set

    fun start() {
        syncState = SolanaKit.SyncState.Syncing()
    }

    fun stop(error: Throwable? = null) {
        syncState = SolanaKit.SyncState.NotSynced(error ?: SolanaKit.SyncError.NotStarted())
    }

    fun sync() {
        syncState = SolanaKit.SyncState.Syncing()

        rpcClient.getBalance(publicKey) { result ->
            result.onSuccess { balance ->
                handleBalance(balance)
            }

            result.onFailure {
                syncState = SolanaKit.SyncState.NotSynced(it)
            }
        }
    }

    private fun handleBalance(balance: Long) {
        if (this.balance != balance) {
            this.balance = balance
            storage.saveBalance(balance)
            listener?.onUpdateBalance(balance)
        }

        syncState = SolanaKit.SyncState.Synced()
    }

}
