package com.ts.selectiontranslator.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ts.selectiontranslator.R
import com.ts.selectiontranslator.core.state.AppState
import com.ts.selectiontranslator.core.state.TranslationEntry
import com.ts.selectiontranslator.core.state.TranslationSource
import com.ts.selectiontranslator.data.providers.LocalDictionaryProvider
import com.ts.selectiontranslator.data.providers.WebTranslationProvider
import com.ts.selectiontranslator.features.shortcuts.ShortcutPanel
import com.ts.selectiontranslator.features.translate.TranslationRepository
import com.ts.selectiontranslator.features.translate.TranslationRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    initialText: String? = null,
    autoTranslate: Boolean = false,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var page by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = page != 0) {
        page = 0
    }

    fun notify(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (page) {
            0 -> TranslatePage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                initialText = initialText,
                autoTranslate = autoTranslate,
                onOpenHistory = { page = 1 },
                onOpenFavorites = { page = 2 },
                onOpenPermissions = onOpenPermissions,
                onNotify = ::notify,
            )
            1 -> HistoryPage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onBack = { page = 0 },
                onNotify = ::notify,
            )
            else -> FavoritesPage(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onBack = { page = 0 },
                onNotify = ::notify,
            )
        }
    }
}

@Composable
private fun TranslatePage(
    modifier: Modifier,
    initialText: String?,
    autoTranslate: Boolean,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPermissions: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val translateFailed = stringResource(R.string.translate_failed)
    val favoriteAdded = stringResource(R.string.favorite_added)
    val favoriteRemoved = stringResource(R.string.favorite_removed)
    var input by rememberSaveable { mutableStateOf(initialText ?: "") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TranslationEntry?>(null) }
    val repository = remember(AppState.offlineMode) {
        if (AppState.offlineMode) {
            TranslationRepository(listOf(LocalDictionaryProvider()))
        } else {
            TranslationRepository(
                listOf(
                    LocalDictionaryProvider(),
                    WebTranslationProvider(),
                ),
            )
        }
    }

    val performTranslate: (String) -> Unit = { text ->
        if (text.isNotBlank() && !loading) {
            loading = true
            scope.launch {
                try {
                    val translated = repository.translate(
                        TranslationRequest(text = text.trim(), sourceLang = "en", targetLang = "zh"),
                    )
                    val entry = TranslationEntry(
                        sourceText = text.trim(),
                        translatedText = translated.text,
                        sourceType = TranslationSource.MANUAL,
                    )
                    result = entry
                    AppState.addHistory(entry)
                } catch (error: Exception) {
                    onNotify(translateFailed)
                } finally {
                    loading = false
                }
            }
        }
    }
    val translateAction = { performTranslate(input) }

    LaunchedEffect(initialText) {
        if (autoTranslate && !initialText.isNullOrBlank()) {
            input = initialText
            performTranslate(initialText)
        }
    }

    LazyColumn(
        modifier = modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    TextButton(onClick = onOpenPermissions) {
                        Text(text = stringResource(R.string.home_open_permissions))
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.home_input_label)) },
                placeholder = { Text(stringResource(R.string.home_input_placeholder)) },
                minLines = 3,
                maxLines = 6,
            )
        }

        item {
            Button(
                onClick = translateAction,
                enabled = input.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.home_translating))
                } else {
                    Text(text = stringResource(R.string.home_translate))
                }
            }
        }

        result?.let { entry ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.overlay_source_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(text = entry.sourceText, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = stringResource(R.string.home_result_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = entry.translatedText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_shortcuts_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            ShortcutPanel(
                onOpenHistory = onOpenHistory,
                onOpenFavorites = onOpenFavorites,
                onToggleOffline = {
                    AppState.setOffline(!AppState.offlineMode)
                },
            )
        }

        item {
            Text(
                text = stringResource(R.string.home_recent_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (AppState.history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_recent_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(AppState.history.take(3), key = { it.timestamp }) { entry ->
                HistoryRow(entry = entry, showFavorite = true, onToggleFavorite = {
                    AppState.toggleFavorite(entry)
                    onNotify(
                        if (AppState.isFavorite(entry)) {
                            favoriteAdded
                        } else {
                            favoriteRemoved
                        },
                    )
                })
            }
        }

        item {
            Text(
                text = stringResource(R.string.home_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryPage(
    modifier: Modifier,
    onBack: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val favoriteAdded = stringResource(R.string.favorite_added)
    val favoriteRemoved = stringResource(R.string.favorite_removed)
    LazyColumn(
        modifier = modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(title = stringResource(R.string.history_title), onBack = onBack)
        }
        if (AppState.history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(AppState.history, key = { it.timestamp }) { entry ->
                HistoryRow(entry = entry, showFavorite = true, onToggleFavorite = {
                    AppState.toggleFavorite(entry)
                    onNotify(
                        if (AppState.isFavorite(entry)) {
                            favoriteAdded
                        } else {
                            favoriteRemoved
                        },
                    )
                })
            }
        }
    }
}

@Composable
private fun FavoritesPage(
    modifier: Modifier,
    onBack: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val favoriteRemoved = stringResource(R.string.favorite_removed)
    LazyColumn(
        modifier = modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(title = stringResource(R.string.favorites_title), onBack = onBack)
        }
        if (AppState.favorites.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(AppState.favorites, key = { it.timestamp }) { entry ->
                HistoryRow(entry = entry, showFavorite = true, onToggleFavorite = {
                    AppState.toggleFavorite(entry)
                    onNotify(favoriteRemoved)
                })
            }
        }
    }
}

@Composable
private fun PageHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HistoryRow(
    entry: TranslationEntry,
    showFavorite: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (entry.sourceType == TranslationSource.SELECTION) {
                        stringResource(R.string.history_source_selection)
                    } else {
                        stringResource(R.string.history_source_manual)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.sourceType == TranslationSource.SELECTION) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                if (showFavorite) {
                    val isFavorite = AppState.isFavorite(entry)
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.favorites_title),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Text(text = entry.sourceText, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = entry.translatedText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
