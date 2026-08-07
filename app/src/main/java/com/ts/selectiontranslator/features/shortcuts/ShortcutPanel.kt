package com.ts.selectiontranslator.features.shortcuts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ShortcutPanel(
    onTranslateNow: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onToggleOffline: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ShortcutTile(label = "实时翻译", onClick = onTranslateNow)
        ShortcutTile(label = "历史", onClick = onOpenHistory)
        ShortcutTile(label = "收藏", onClick = onOpenFavorites)
        ShortcutTile(label = "离线模式", onClick = onToggleOffline)
    }
}
