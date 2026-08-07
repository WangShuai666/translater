package com.ts.selectiontranslator.core.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ts.selectiontranslator.features.favorites.FavoriteDao
import com.ts.selectiontranslator.features.favorites.FavoriteEntity
import com.ts.selectiontranslator.features.history.HistoryDao
import com.ts.selectiontranslator.features.history.HistoryEntity

@Database(
    entities = [HistoryEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
}
