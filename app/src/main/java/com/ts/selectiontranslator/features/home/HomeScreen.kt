package com.ts.selectiontranslator.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.selectiontranslator.features.shortcuts.ShortcutPanel

@Composable
fun HomeScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Selection Translator",
                style = MaterialTheme.typography.headlineSmall,
            )
            ShortcutPanel(
                onTranslateNow = {},
                onOpenHistory = {},
                onOpenFavorites = {},
                onToggleOffline = {},
            )
        }
    }
}
