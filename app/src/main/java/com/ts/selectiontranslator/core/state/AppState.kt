package com.ts.selectiontranslator.core.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class TranslationSource {
    MANUAL,
    SELECTION,
}

data class TranslationEntry(
    val sourceText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceType: TranslationSource = TranslationSource.MANUAL,
)

data class DiagnosticEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
)

object AppState {
    val history = mutableStateListOf<TranslationEntry>()
    val favorites = mutableStateListOf<TranslationEntry>()

    var offlineMode by mutableStateOf(false)
        private set

    fun addHistory(entry: TranslationEntry) {
        history.removeAll {
            it.sourceText == entry.sourceText && it.sourceType == entry.sourceType
        }
        history.add(0, entry)
        while (history.size > 100) {
            history.removeAt(history.size - 1)
        }
    }

    fun toggleFavorite(entry: TranslationEntry) {
        val existing = favorites.indexOfFirst {
            it.sourceText == entry.sourceText && it.sourceType == entry.sourceType
        }
        if (existing >= 0) {
            favorites.removeAt(existing)
        } else {
            favorites.add(0, entry)
        }
    }

    fun isFavorite(entry: TranslationEntry): Boolean {
        return favorites.any {
            it.sourceText == entry.sourceText && it.sourceType == entry.sourceType
        }
    }

    fun setOffline(enabled: Boolean) {
        offlineMode = enabled
    }
}

object SelectionDiagnostics {
    val events = mutableStateListOf<DiagnosticEvent>()

    fun record(message: String) {
        events.add(0, DiagnosticEvent(message = message))
        while (events.size > 30) {
            events.removeAt(events.size - 1)
        }
    }

    fun clear() {
        events.clear()
    }
}
