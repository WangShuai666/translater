package com.ts.selectiontranslator.data.providers

import com.ts.selectiontranslator.features.translate.TranslationRequest
import com.ts.selectiontranslator.features.translate.TranslationResult

class YoudaoTranslationProvider : TranslationProvider {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        return TranslationResult(
            text = request.text.uppercase(),
            providerName = "youdao",
        )
    }
}
