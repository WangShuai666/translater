package com.ts.selectiontranslator.features.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ts.selectiontranslator.R

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
            Text(
                text = stringResource(R.string.overlay_source_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = sourceText, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.overlay_translation_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = translation, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onCopy, label = { Text(stringResource(R.string.overlay_copy)) })
                AssistChip(onClick = onFavorite, label = { Text(stringResource(R.string.overlay_favorite)) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onSpeak, label = { Text(stringResource(R.string.overlay_speak)) })
                AssistChip(onClick = onClose, label = { Text(stringResource(R.string.overlay_close)) })
            }
        }
    }
}
