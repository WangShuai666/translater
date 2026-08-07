package com.ts.selectiontranslator.features.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TranslationOverlayHost(
    sourceText: String,
    translation: String,
    onCopy: () -> Unit,
    onFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onClose: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = sourceText, style = MaterialTheme.typography.bodyMedium)
            Text(text = translation, style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onCopy, label = { Text("复制") })
                AssistChip(onClick = onFavorite, label = { Text("收藏") })
                AssistChip(onClick = onSpeak, label = { Text("朗读") })
                AssistChip(onClick = onClose, label = { Text("关闭") })
            }
        }
    }
}
