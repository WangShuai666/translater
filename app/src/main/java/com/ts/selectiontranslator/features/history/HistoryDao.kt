package com.ts.selectiontranslator.features.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entry: HistoryEntity)

    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC")
    suspend fun latest(): List<HistoryEntity>
}
