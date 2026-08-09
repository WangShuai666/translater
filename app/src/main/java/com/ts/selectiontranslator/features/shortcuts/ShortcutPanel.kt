package com.ts.selectiontranslator.features.shortcuts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ts.selectiontranslator.R
import com.ts.selectiontranslator.core.state.AppState

@Composable
fun ShortcutPanel(
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onToggleOffline: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShortcutTile(
            label = stringResource(R.string.shortcut_history),
            description = stringResource(R.string.shortcut_history_desc),
            icon = Icons.Outlined.History,
            onClick = onOpenHistory,
        )
        ShortcutTile(
            label = stringResource(R.string.shortcut_favorites),
            description = stringResource(R.string.shortcut_favorites_desc),
            icon = Icons.Outlined.BookmarkBorder,
            onClick = onOpenFavorites,
        )
        ShortcutTile(
            label = stringResource(R.string.shortcut_offline),
            description = stringResource(R.string.shortcut_offline_desc),
            icon = Icons.Outlined.WifiOff,
            onClick = onToggleOffline,
            isActive = AppState.offlineMode,
        )
    }
}
