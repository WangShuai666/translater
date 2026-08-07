package com.ts.selectiontranslator.features.translate

data class TranslationRequest(
    val text: String,
    val sourceLang: String,
    val targetLang: String,
)
