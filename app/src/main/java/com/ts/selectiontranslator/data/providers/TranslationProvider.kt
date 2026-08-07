package com.ts.selectiontranslator.data.providers

import com.ts.selectiontranslator.features.translate.TranslationRequest
import com.ts.selectiontranslator.features.translate.TranslationResult

interface TranslationProvider {
    suspend fun translate(request: TranslationRequest): TranslationResult
}
