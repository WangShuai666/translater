package com.ts.selectiontranslator.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ts.selectiontranslator.MainActivity

class SelectionAccessibilityService : AccessibilityService() {
    private var lastSelectedText: String = ""
    private var lastTriggerAt: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) return

        val selected = event.source?.selectedText() ?: return
        val now = System.currentTimeMillis()
        if (selected.isBlank() || selected == lastSelectedText || now - lastTriggerAt < 1200L) return

        lastSelectedText = selected
        lastTriggerAt = now
        openTranslate(selected)
    }

    override fun onInterrupt() = Unit

    private fun AccessibilityNodeInfo.selectedText(): String? {
        val fullText = text?.toString() ?: return null
        val start = textSelectionStart
        val end = textSelectionEnd
        if (start < 0 || end <= start) return null
        val safeStart = start.coerceIn(0, fullText.length)
        val safeEnd = end.coerceIn(safeStart, fullText.length)
        return fullText.substring(safeStart, safeEnd)
    }

    private fun openTranslate(text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
    }
}
