package com.ts.selectiontranslator.features.translate

import com.ts.selectiontranslator.data.cache.TranslationCache
import com.ts.selectiontranslator.data.providers.TranslationProvider

class TranslationRepository(
    private val providers: List<TranslationProvider>,
    private val cache: TranslationCache = TranslationCache(),
) {
    suspend fun translate(request: TranslationRequest): TranslationResult {
        cache.get(request)?.let { return it }

        var lastError: Throwable? = null
        for (provider in providers) {
            try {
                val result = provider.translate(request)
                cache.put(request, result)
                return result
            } catch (error: Exception) {
                lastError = error
            }
        }

        throw lastError ?: IllegalStateException("No translation providers available")
    }
}
