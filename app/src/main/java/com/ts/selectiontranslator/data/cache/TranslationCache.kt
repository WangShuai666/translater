package com.ts.selectiontranslator.data.cache

import com.ts.selectiontranslator.features.translate.TranslationRequest
import com.ts.selectiontranslator.features.translate.TranslationResult

class TranslationCache {
    private val store = mutableMapOf<String, TranslationResult>()

    fun get(request: TranslationRequest): TranslationResult? = store[key(request)]

    fun put(request: TranslationRequest, result: TranslationResult) {
        store[key(request)] = result
    }

    private fun key(request: TranslationRequest): String {
        return "${request.sourceLang}:${request.targetLang}:${request.text}"
    }
}
