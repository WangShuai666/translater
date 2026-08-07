package com.ts.selectiontranslator.core.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateTest {
    @Test
    fun `gate is not ready when accessibility is missing`() {
        val state = PermissionState(
            accessibilityEnabled = false,
            overlayEnabled = true,
        )

        assertFalse(state.isReadyForGlobalSelection)
    }

    @Test
    fun `gate is ready when both permissions are enabled`() {
        val state = PermissionState(
            accessibilityEnabled = true,
            overlayEnabled = true,
        )

        assertTrue(state.isReadyForGlobalSelection)
    }
}
