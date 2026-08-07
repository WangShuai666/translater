package com.ts.selectiontranslator.features.overlay

class TranslationOverlayController {
    var currentSourceText: String = ""
        private set

    var currentTranslation: String = ""
        private set

    var isVisible: Boolean = false
        private set

    fun show(sourceText: String, translation: String) {
        currentSourceText = sourceText
        currentTranslation = translation
        isVisible = true
    }

    fun dismiss() {
        isVisible = false
    }
}
