package com.lyfebloc.solanakit.database.main

import com.lyfebloc.solanakit.models.BalanceEntity
import com.lyfebloc.solanakit.models.LastBlockHeightEntity

class MainStorage(
    private val database: MainDatabase
) {

    fun getLastBlockHeight(): Long? {
        return database.lastBlockHeightDao().getLastBlockHeight()?.height
    }

    fun saveLastBlockHeight(lastBlockHeight: Long) {
        database.lastBlockHeightDao().insert(LastBlockHeightEntity(lastBlockHeight))
    }

    fun saveBalance(balance: Long) {
        database.balanceDao().insert(BalanceEntity(balance))
    }

    fun getBalance(): Long? {
        return database.balanceDao().getBalance()?.lamports
    }

}
