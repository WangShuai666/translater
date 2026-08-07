package com.ts.selectiontranslator.features.favorites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insert(entry: FavoriteEntity)

    @Query("SELECT * FROM translation_favorites ORDER BY createdAt DESC")
    suspend fun latest(): List<FavoriteEntity>
}
