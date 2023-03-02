package com.lyfebloc.solanakit.database.main

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lyfebloc.solanakit.database.main.dao.BalanceDao
import com.lyfebloc.solanakit.database.main.dao.LastBlockHeightDao
import com.lyfebloc.solanakit.database.transaction.RoomTypeConverters
import com.lyfebloc.solanakit.models.BalanceEntity
import com.lyfebloc.solanakit.models.LastBlockHeightEntity

@Database(
    entities = [
        BalanceEntity::class,
        LastBlockHeightEntity::class
    ],
    version = 4, exportSchema = false
)
@TypeConverters(RoomTypeConverters::class)
abstract class MainDatabase : RoomDatabase() {

    abstract fun balanceDao(): BalanceDao
    abstract fun lastBlockHeightDao(): LastBlockHeightDao

    companion object {

        fun getInstance(context: Context, databaseName: String): MainDatabase {
            return Room.databaseBuilder(context, MainDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }

    }

}
