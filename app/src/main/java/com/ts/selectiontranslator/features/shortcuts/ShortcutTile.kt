package com.ts.selectiontranslator.features.shortcuts

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShortcutTile(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}
