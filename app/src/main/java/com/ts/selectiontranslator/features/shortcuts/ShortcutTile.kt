package com.ts.selectiontranslator.features.shortcuts

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ShortcutTile(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false,
    trailingLabel: String? = null,
) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val descriptionColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val animatedContainer by animateColorAsState(
        targetValue = containerColor,
        label = "shortcutContainer",
    )
    val animatedContent by animateColorAsState(
        targetValue = contentColor,
        label = "shortcutContent",
    )
    val animatedDescription by animateColorAsState(
        targetValue = descriptionColor,
        label = "shortcutDescription",
    )
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainer,
            contentColor = animatedContent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) animatedContent else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = label, style = MaterialTheme.typography.titleMedium, color = animatedContent)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = animatedDescription,
                    )
                }
                if (trailingLabel != null) {
                    Text(
                        text = trailingLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isActive) animatedContent else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
