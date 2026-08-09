package com.ts.selectiontranslator.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import com.ts.selectiontranslator.core.state.SelectionDiagnostics
import com.ts.selectiontranslator.features.clipboard.ClipboardBridge
import com.ts.selectiontranslator.features.clipboard.ClipboardBridgeActivity
import com.ts.selectiontranslator.data.providers.LocalDictionaryProvider
import com.ts.selectiontranslator.data.providers.WebTranslationProvider
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
    private val repository = TranslationRepository(
        listOf(
            LocalDictionaryProvider(),
            WebTranslationProvider(),
        ),
    )
    private val mainHandler = Handler(Looper.getMainLooper())
    private val dismissResult = Runnable { removeResultOverlay() }
    private val windowManagerService: WindowManager by lazy {
        getSystemService(WindowManager::class.java)
    }

    private var resultOverlay: View? = null
    private var resultOverlayOwnerPackage: String? = null
    private var lastSelectionPackage: String? = null
    private var lastSelectedText: String = ""
    private var lastTriggerAt: Long = 0L
    private var clipboardRequestId: Long = 0L
    private var clipboardFallbackAt: Long = 0L
    private var lastSelectionEventAt: Long = 0L
    private var pendingSelectionCheck: Runnable? = null
    private var pendingSelectionSource: AccessibilityNodeInfo? = null
    private var requestId: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        SelectionDiagnostics.record("无障碍服务已连接")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        val now = System.currentTimeMillis()
        SelectionDiagnostics.record(
            "收到事件 ${AccessibilityEvent.eventTypeToString(eventType)}，来自 ${event.packageName}",
        )

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            -> dismissOverlayIfAppChanged(event)
        }

        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            lastSelectionEventAt = now
            event.packageName?.toString()?.let { lastSelectionPackage = it }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (!shouldHandleContentChanged(event, now)) return
            event.packageName?.toString()?.let { lastSelectionPackage = it }
            val changes = event.contentChangeTypes
            if (!isBroadContentChangeEvent(event) &&
                (changes == AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED ||
                changes and AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT == 0
                )
            ) {
                return
            }
        } else if (eventType != AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            return
        }

        val selected = event.source?.selectedText()
        if (selected.isNullOrBlank()) {
            SelectionDiagnostics.record("未读到选中文本，安排自动读取")
            scheduleSelectionFallback(event.source)
            return
        }

        cancelPendingSelectionCheck()
        SelectionDiagnostics.record("读到选中文本：${selected.take(24)}")
        handleSelection(selected, event.source)
    }

    private fun dismissOverlayIfAppChanged(event: AccessibilityEvent) {
        val ownerPackage = resultOverlayOwnerPackage ?: return
        val currentPackage = event.packageName?.toString() ?: activeWindowPackage() ?: return
        if (currentPackage != ownerPackage && currentPackage != this.packageName) {
            removeResultOverlay()
        }
    }

    private fun activeWindowPackage(): String? {
        return windows.firstOrNull { it.isActive }?.root?.packageName?.toString()
    }

    private fun shouldHandleContentChanged(event: AccessibilityEvent, now: Long): Boolean {
        if (now - lastSelectionEventAt <= 2500L) return true
        val source = event.source
        val className = source?.className?.toString().orEmpty()
        if (className.contains("WebView", ignoreCase = true)) return true

        val packageName = event.packageName?.toString().orEmpty()
        if (packageName == "com.github.android") {
            val selectionStart = source?.textSelectionStart ?: -1
            val selectionEnd = source?.textSelectionEnd ?: -1
            val hasSelection = selectionStart >= 0 && selectionEnd > selectionStart
            val className = source?.className?.toString().orEmpty()
            if (className.contains("EditText", ignoreCase = true)) {
                return hasSelection
            }
            return true
        }
        return false
    }

    private fun isBroadContentChangeEvent(event: AccessibilityEvent): Boolean {
        val packageName = event.packageName?.toString().orEmpty()
        val className = event.source?.className?.toString().orEmpty()
        return packageName == "com.github.android" || className.contains("WebView", ignoreCase = true)
    }

    private fun handleSelection(selected: String, source: AccessibilityNodeInfo?) {
        val now = System.currentTimeMillis()
        if (selected.isBlank() || selected == lastSelectedText || now - lastTriggerAt < 900L) return
        lastSelectedText = selected
        lastTriggerAt = now
        translateAndShow(selected, source)
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

    private fun findSelectionInWindow(): Pair<AccessibilityNodeInfo, String>? {
        val root = rootInActiveWindow ?: return null
        return findSelectionInNode(root)
    }

    private fun findSelectionInNode(node: AccessibilityNodeInfo): Pair<AccessibilityNodeInfo, String>? {
        node.selectedText()?.let { return node to it }
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            val found = findSelectionInNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun scheduleSelectionFallback(source: AccessibilityNodeInfo?) {
        pendingSelectionCheck?.let { mainHandler.removeCallbacks(it) }
        pendingSelectionSource = source ?: rootInActiveWindow
        val check = Runnable {
            pendingSelectionCheck = null
            SelectionDiagnostics.record("自动读取回退开始")
            val found = findSelectionInWindow()
            if (found != null) {
                handleSelection(found.second, found.first)
            } else {
                SelectionDiagnostics.record("窗口内未找到选区，尝试自动复制")
                copySelectionAndTranslate(pendingSelectionSource)
            }
            pendingSelectionSource = null
        }
        pendingSelectionCheck = check
        mainHandler.postDelayed(check, 450L)
    }

    private fun cancelPendingSelectionCheck() {
        pendingSelectionCheck?.let { mainHandler.removeCallbacks(it) }
        pendingSelectionCheck = null
        pendingSelectionSource = null
    }

    private fun copySelectionAndTranslate(source: AccessibilityNodeInfo?) {
        val now = System.currentTimeMillis()
        if (now - clipboardFallbackAt < 1600L) return

        val copyTarget = findCopyTarget(source)
        if (copyTarget == null && !clickCopyMenu()) {
            SelectionDiagnostics.record("没有找到可复制的节点")
            return
        }
        SelectionDiagnostics.record("自动复制成功，准备读取剪贴板")

        clipboardFallbackAt = now
        val request = ++clipboardRequestId
        ClipboardBridge.requestId = request
        ClipboardBridge.capturedText = null

        val intent = Intent(this, ClipboardBridgeActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS,
            )
        }
        runCatching { startActivity(intent) }.onFailure {
            SelectionDiagnostics.record("桥接页面启动失败：${it.message}")
        }

        mainHandler.postDelayed({
            val copied = ClipboardBridge.capturedText
            if (ClipboardBridge.requestId != request) return@postDelayed
            if (copied.isNullOrBlank() || copied == lastSelectedText) {
                SelectionDiagnostics.record("剪贴板读取为空或与上次相同")
                return@postDelayed
            }
            SelectionDiagnostics.record("剪贴板读取到：${copied.take(24)}")
            handleSelection(copied, source)
        }, 320L)
    }

    private fun findCopyTarget(source: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (source != null && source.isEnabled && canCopy(source)) {
            return source
        }

        val root = rootInActiveWindow ?: return null
        if (root != source && canCopy(root)) {
            return root
        }

        return findCopyTargetInNode(root)
    }

    private fun clickCopyMenu(): Boolean {
        for (window in windows) {
            val root = window.root ?: continue
            if (clickCopyMenuInNode(root)) return true
        }
        return false
    }

    private fun clickCopyMenuInNode(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString().orEmpty().trim()
        val description = node.contentDescription?.toString().orEmpty().trim()
        if (text == "复制" || description == "复制" ||
            text.equals("Copy", ignoreCase = true) || description.equals("Copy", ignoreCase = true)
        ) {
            if (runCatching { node.performAction(AccessibilityNodeInfo.ACTION_CLICK) }.getOrDefault(false)) {
                return true
            }
        }
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            if (clickCopyMenuInNode(child)) return true
        }
        return false
    }

    private fun findCopyTargetInNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isTextSelectable && node.isEnabled && canCopy(node)) {
            return node
        }
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            val found = findCopyTargetInNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun canCopy(node: AccessibilityNodeInfo): Boolean {
        return runCatching { node.performAction(AccessibilityNodeInfo.ACTION_COPY) }.getOrDefault(false)
    }

    private fun translateAndShow(text: String, source: AccessibilityNodeInfo?) {
        removeResultOverlay()
        val currentRequest = ++requestId
        scope.launch {
            val result = runCatching {
                repository.translate(TranslationRequest(text = text, sourceLang = "en", targetLang = "zh"))
            }.getOrNull()
            if (result == null || result.text.isBlank()) {
                SelectionDiagnostics.record("翻译失败，未显示浮层")
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (currentRequest != requestId) return@withContext
                SelectionDiagnostics.record("翻译完成：${result.text.take(24)}")
                showResultOverlay(text, result.text, source)
            }
        }
    }

    private fun showResultOverlay(sourceText: String, translatedText: String, source: AccessibilityNodeInfo?) {
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
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.TOP or Gravity.START
        val position = resultPosition(source, maxWidth)
        params.x = position.first
        params.y = position.second

        runCatching {
            windowManagerService.addView(card, params)
            resultOverlay = card
        }.onSuccess {
            SelectionDiagnostics.record("译文浮层已显示")
            resultOverlayOwnerPackage = lastSelectionPackage
        }.onFailure {
            SelectionDiagnostics.record("译文浮层显示失败：${it.message}")
        }
        mainHandler.postDelayed(dismissResult, 6000L)
    }

    private fun removeResultOverlay() {
        mainHandler.removeCallbacks(dismissResult)
        resultOverlay?.let {
            runCatching { windowManagerService.removeView(it) }
        }
        resultOverlay = null
        resultOverlayOwnerPackage = null
    }

    private fun resultPosition(source: AccessibilityNodeInfo?, width: Int): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val margin = dp(8)
        val estimatedHeight = dp(120)

        val rect = Rect()
        runCatching { source?.getBoundsInScreen(rect) }
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
