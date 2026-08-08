package com.ts.selectiontranslator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialText = intent?.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        setContent {
            SelectionTranslatorApp(initialText = initialText)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val initialText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        setContent {
            SelectionTranslatorApp(initialText = initialText)
        }
    }
}
