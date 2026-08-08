package com.ts.selectiontranslator.core.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TranslationEntry(
    val sourceText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
)

object AppState {
    val history = mutableStateListOf<TranslationEntry>()
    val favorites = mutableStateListOf<TranslationEntry>()

    var offlineMode by mutableStateOf(false)
        private set

    fun addHistory(entry: TranslationEntry) {
        history.removeAll { it.sourceText == entry.sourceText }
        history.add(0, entry)
        while (history.size > 100) {
            history.removeAt(history.size - 1)
        }
    }

    fun toggleFavorite(entry: TranslationEntry) {
        val existing = favorites.indexOfFirst { it.sourceText == entry.sourceText }
        if (existing >= 0) {
            favorites.removeAt(existing)
        } else {
            favorites.add(0, entry)
        }
    }

    fun setOffline(enabled: Boolean) {
        offlineMode = enabled
    }
}
