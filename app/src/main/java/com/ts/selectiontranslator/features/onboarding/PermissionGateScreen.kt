package com.ts.selectiontranslator.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ts.selectiontranslator.R
import com.ts.selectiontranslator.core.permissions.PermissionState

@Composable
fun PermissionGateScreen(
    state: PermissionState,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.permission_gate_primary_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.permission_gate_primary_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = stringResource(R.string.permission_gate_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (state.isReadyForGlobalSelection) {
                    stringResource(R.string.permission_gate_ready_body)
                } else {
                    stringResource(R.string.permission_gate_body)
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            PermissionRow(
                title = stringResource(R.string.permission_gate_accessibility_label),
                status = if (state.accessibilityEnabled) {
                    stringResource(R.string.permission_gate_enabled)
                } else {
                    stringResource(R.string.permission_gate_disabled)
                },
                actionLabel = stringResource(R.string.permission_gate_accessibility_action),
                icon = Icons.Outlined.AccessibilityNew,
                onAction = onRequestAccessibility,
            )

            PermissionRow(
                title = stringResource(R.string.permission_gate_overlay_label),
                status = if (state.overlayEnabled) {
                    stringResource(R.string.permission_gate_enabled)
                } else {
                    stringResource(R.string.permission_gate_disabled)
                },
                actionLabel = stringResource(R.string.permission_gate_overlay_action),
                icon = Icons.Outlined.SmartDisplay,
                onAction = onRequestOverlay,
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    status: String,
    actionLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}
