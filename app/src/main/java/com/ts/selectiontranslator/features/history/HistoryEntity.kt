package com.ts.selectiontranslator.features.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val createdAt: Long,
)
