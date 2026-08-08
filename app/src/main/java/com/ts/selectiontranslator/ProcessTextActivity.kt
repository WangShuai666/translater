package com.ts.selectiontranslator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class ProcessTextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedText = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()

        val target = Intent(this, MainActivity::class.java).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, selectedText)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(target)
        finish()
    }
}
