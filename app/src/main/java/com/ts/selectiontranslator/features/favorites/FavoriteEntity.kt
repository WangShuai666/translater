package com.ts.selectiontranslator.features.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val createdAt: Long,
)
