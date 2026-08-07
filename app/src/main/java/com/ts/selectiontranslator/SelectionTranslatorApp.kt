package com.ts.selectiontranslator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ts.selectiontranslator.core.permissions.PermissionState
import com.ts.selectiontranslator.core.ui.AppTheme
import com.ts.selectiontranslator.features.accessibility.SelectionAccessibilityService
import com.ts.selectiontranslator.features.onboarding.PermissionGateScreen

@Composable
fun SelectionTranslatorApp() {
    AppTheme {
        val context = LocalContext.current
        var permissionState by remember(context) {
            mutableStateOf(context.readPermissionState())
        }

        DisposableEffect(context) {
            val lifecycleOwner = context as? LifecycleOwner
            if (lifecycleOwner == null) {
                onDispose { }
            } else {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        permissionState = context.readPermissionState()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
        }

        PermissionGateScreen(
            state = permissionState,
            onRequestAccessibility = { context.openAccessibilitySettings() },
            onRequestOverlay = { context.openOverlaySettings() },
        )
    }
}

private fun Context.readPermissionState(): PermissionState {
    return PermissionState(
        accessibilityEnabled = isAccessibilityServiceEnabled(),
        overlayEnabled = Settings.canDrawOverlays(this),
    )
}

private fun Context.isAccessibilityServiceEnabled(): Boolean {
    val enabledServices = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ).orEmpty()
    val serviceId = ComponentName(this, SelectionAccessibilityService::class.java).flattenToString()
    val accessibilityEnabled = Settings.Secure.getInt(
        contentResolver,
        Settings.Secure.ACCESSIBILITY_ENABLED,
        0,
    ) == 1

    return accessibilityEnabled && enabledServices.split(':').any { it == serviceId }
}

private fun Context.openAccessibilitySettings() {
    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

private fun Context.openOverlaySettings() {
    startActivity(
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        ),
    )
}
