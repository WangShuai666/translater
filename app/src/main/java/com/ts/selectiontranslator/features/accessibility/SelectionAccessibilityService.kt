package com.ts.selectiontranslator.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.ts.selectiontranslator.MainActivity

class SelectionAccessibilityService : AccessibilityService() {
    private var lastSelectedText: String = ""
    private var lastTriggerAt: Long = 0L
    private var quickTranslateBubble: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dismissBubble = Runnable { removeQuickTranslateBubble() }
    private val windowManagerService: WindowManager by lazy {
        getSystemService(WindowManager::class.java)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            -> removeQuickTranslateBubble()
        }

        if (event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) return

        val selected = event.source?.selectedText() ?: return
        val now = System.currentTimeMillis()
        if (selected.isBlank() || selected == lastSelectedText || now - lastTriggerAt < 1200L) return

        lastSelectedText = selected
        lastTriggerAt = now
        showQuickTranslateBubble(selected, event.source)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeQuickTranslateBubble()
        super.onDestroy()
    }

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

    private fun showQuickTranslateBubble(text: String, source: AccessibilityNodeInfo?) {
        if (!Settings.canDrawOverlays(this)) return
        removeQuickTranslateBubble()

        val view = TextView(this).apply {
            this.text = "译"
            setTextSize(16f)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF0F766E.toInt())
                setStroke(dp(2), Color.WHITE)
            }
            setOnClickListener {
                removeQuickTranslateBubble()
                openTranslate(text)
            }
        }

        val size = dp(52)
        val params = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or Gravity.START
        val position = bubblePosition(source, size)
        params.x = position.first
        params.y = position.second

        windowManagerService.addView(view, params)
        quickTranslateBubble = view
        mainHandler.postDelayed(dismissBubble, 8000L)
    }

    private fun removeQuickTranslateBubble() {
        mainHandler.removeCallbacks(dismissBubble)
        quickTranslateBubble?.let {
            runCatching { windowManagerService.removeView(it) }
        }
        quickTranslateBubble = null
    }

    private fun bubblePosition(source: AccessibilityNodeInfo?, size: Int): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val margin = dp(8)

        val rect = Rect()
        source?.getBoundsInScreen(rect)
        if (rect.isEmpty) {
            return Pair(screenWidth - size - margin, (screenHeight / 3).coerceAtLeast(margin))
        }

        val x = rect.left.coerceIn(margin, (screenWidth - size - margin).coerceAtLeast(margin))
        val y = if (rect.bottom + size + margin < screenHeight) {
            rect.bottom + margin
        } else {
            (rect.top - size - margin).coerceAtLeast(margin)
        }
        return Pair(x, y)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
