package com.ts.selectiontranslator.features.clipboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

internal object ClipboardBridge {
    @Volatile
    var requestId: Long = 0L

    @Volatile
    var capturedText: String? = null
}

class ClipboardBridgeActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val readClipboard = Runnable { captureClipboard() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mainHandler.post(readClipboard)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.removeCallbacks(readClipboard)
        mainHandler.postDelayed(readClipboard, 80L)
    }

    private fun captureClipboard() {
        mainHandler.removeCallbacks(readClipboard)
        val copied = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
            ?.primaryClip
            ?.getItemAt(0)
            ?.coerceToText(this)
            ?.toString()
            ?.trim()

        if (!copied.isNullOrBlank()) {
            ClipboardBridge.capturedText = copied
        }
        finish()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(readClipboard)
        super.onDestroy()
    }
}
