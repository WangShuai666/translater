package com.ts.selectiontranslator.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.selectiontranslator.data.providers.LocalDictionaryProvider
import com.ts.selectiontranslator.features.translate.TranslationRepository
import com.ts.selectiontranslator.features.translate.TranslationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectionAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repository = TranslationRepository(listOf(LocalDictionaryProvider()))
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dismissResult = Runnable { removeResultOverlay() }
    private val windowManagerService: WindowManager by lazy {
        getSystemService(WindowManager::class.java)
    }

    private var resultOverlay: View? = null
    private var lastSelectedText: String = ""
    private var lastTriggerAt: Long = 0L
    private var requestId: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            -> removeResultOverlay()
        }

        if (event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) return

        val selected = event.source?.selectedText() ?: return
        val now = System.currentTimeMillis()
        if (selected.isBlank() || selected == lastSelectedText || now - lastTriggerAt < 900L) return

        lastSelectedText = selected
        lastTriggerAt = now
        translateAndShow(selected, event.source)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeResultOverlay()
        scope.cancel()
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

    private fun translateAndShow(text: String, source: AccessibilityNodeInfo?) {
        removeResultOverlay()
        val currentRequest = ++requestId
        scope.launch {
            val result = runCatching {
                repository.translate(TranslationRequest(text = text, sourceLang = "en", targetLang = "zh"))
            }.getOrNull()
            if (result == null || result.text.isBlank()) return@launch

            withContext(Dispatchers.Main) {
                if (currentRequest != requestId) return@withContext
                showResultOverlay(text, result.text, source)
            }
        }
    }

    private fun showResultOverlay(sourceText: String, translatedText: String, source: AccessibilityNodeInfo?) {
        if (!Settings.canDrawOverlays(this)) return
        removeResultOverlay()

        val sourceView = TextView(this).apply {
            text = sourceText
            setTextColor(Color.rgb(71, 85, 105))
            textSize = 13f
            maxLines = 3
        }
        val translationView = TextView(this).apply {
            text = translatedText
            setTextColor(Color.rgb(15, 23, 42))
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 6
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.rgb(240, 253, 250))
                setStroke(dp(1), Color.rgb(15, 118, 110))
            }
            elevation = dp(6).toFloat()
            addView(sourceView)
            addView(translationView)
        }

        val metrics = resources.displayMetrics
        val maxWidth = (metrics.widthPixels * 0.72f).toInt()
        val params = WindowManager.LayoutParams(
            maxWidth,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or Gravity.START
        val position = resultPosition(source, maxWidth)
        params.x = position.first
        params.y = position.second

        windowManagerService.addView(card, params)
        resultOverlay = card
        mainHandler.postDelayed(dismissResult, 6000L)
    }

    private fun removeResultOverlay() {
        mainHandler.removeCallbacks(dismissResult)
        resultOverlay?.let {
            runCatching { windowManagerService.removeView(it) }
        }
        resultOverlay = null
    }

    private fun resultPosition(source: AccessibilityNodeInfo?, width: Int): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val margin = dp(8)
        val estimatedHeight = dp(120)

        val rect = Rect()
        source?.getBoundsInScreen(rect)
        if (rect.isEmpty) {
            return Pair(screenWidth - width - margin, (screenHeight / 3).coerceAtLeast(margin))
        }

        val x = rect.left.coerceIn(margin, (screenWidth - width - margin).coerceAtLeast(margin))
        val y = if (rect.bottom + estimatedHeight + margin < screenHeight) {
            rect.bottom + margin
        } else {
            (rect.top - estimatedHeight - margin).coerceAtLeast(margin)
        }
        return Pair(x, y)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
