package com.ts.selectiontranslator.features.shortcuts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.ui.unit.dp

@Composable
fun ShortcutPanel(
    onTranslateNow: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onToggleOffline: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShortcutTile(
            label = "实时翻译",
            description = "选中文字后立即显示译文。",
            icon = Icons.Outlined.Translate,
            onClick = onTranslateNow,
        )
        ShortcutTile(
            label = "历史记录",
            description = "查看最近翻译过的内容。",
            icon = Icons.Outlined.History,
            onClick = onOpenHistory,
        )
        ShortcutTile(
            label = "收藏内容",
            description = "保存常用词句，随时回看。",
            icon = Icons.Outlined.BookmarkBorder,
            onClick = onOpenFavorites,
        )
        ShortcutTile(
            label = "离线模式",
            description = "断网时使用本地能力继续工作。",
            icon = Icons.Outlined.WifiOff,
            onClick = onToggleOffline,
        )
    }
}
