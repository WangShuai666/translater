package com.ts.selectiontranslator.core.permissions

data class PermissionState(
    val accessibilityEnabled: Boolean,
    val overlayEnabled: Boolean,
) {
    val isReadyForGlobalSelection: Boolean
        get() = accessibilityEnabled
}
