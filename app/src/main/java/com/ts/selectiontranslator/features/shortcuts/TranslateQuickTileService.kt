package com.ts.selectiontranslator.features.shortcuts

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.ts.selectiontranslator.R
import com.ts.selectiontranslator.features.accessibility.SelectionAccessibilityService

class TranslateQuickTileService : TileService() {
    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (!isAccessibilityEnabled()) {
            startActivityAndCollapse(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        TranslationPrefs.setEnabled(this, !TranslationPrefs.isEnabled(this))
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        if (!isAccessibilityEnabled()) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.quick_tile_label_accessibility)
        } else {
            val enabled = TranslationPrefs.isEnabled(this)
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = getString(
                if (enabled) R.string.quick_tile_label_on else R.string.quick_tile_label_off,
            )
        }
        tile.updateTile()
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()
        val serviceId = ComponentName(
            this,
            SelectionAccessibilityService::class.java,
        ).flattenToString()
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0,
        ) == 1

        return accessibilityEnabled && enabledServices.split(':').any { it == serviceId }
    }
}
