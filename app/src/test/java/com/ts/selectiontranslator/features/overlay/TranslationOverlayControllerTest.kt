package com.ts.selectiontranslator.features.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationOverlayControllerTest {
    @Test
    fun `show stores source and translation separately`() {
        val controller = TranslationOverlayController()

        controller.show("original", "translated")

        assertEquals("original", controller.currentSourceText)
        assertEquals("translated", controller.currentTranslation)
        assertTrue(controller.isVisible)
    }

    @Test
    fun `dismiss hides overlay without clearing text`() {
        val controller = TranslationOverlayController()
        controller.show("original", "translated")

        controller.dismiss()

        assertFalse(controller.isVisible)
        assertEquals("original", controller.currentSourceText)
        assertEquals("translated", controller.currentTranslation)
    }
}
