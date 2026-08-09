package com.ts.selectiontranslator.data.providers

import com.ts.selectiontranslator.features.translate.TranslationRequest
import com.ts.selectiontranslator.features.translate.TranslationResult

class LocalDictionaryProvider : TranslationProvider {
    private val dictionary = mapOf(
        "hello" to "你好",
        "hi" to "嗨",
        "good morning" to "早上好",
        "good afternoon" to "下午好",
        "good evening" to "晚上好",
        "goodbye" to "再见",
        "thank you" to "谢谢",
        "thanks" to "谢谢",
        "please" to "请",
        "sorry" to "对不起",
        "yes" to "是",
        "no" to "否",
        "love" to "爱",
        "time" to "时间",
        "world" to "世界",
        "language" to "语言",
        "translate" to "翻译",
        "translation" to "译文",
        "text" to "文本",
        "word" to "单词",
        "sentence" to "句子",
        "help" to "帮助",
        "home" to "首页",
        "history" to "历史",
        "favorite" to "收藏",
        "settings" to "设置",
        "offline" to "离线",
        "speed" to "速度",
        "accuracy" to "准确度",
        "fast" to "快速",
        "good" to "好",
        "bad" to "坏",
        "day" to "天",
        "night" to "夜晚",
        "morning" to "早晨",
        "evening" to "傍晚",
        "water" to "水",
        "food" to "食物",
        "friend" to "朋友",
        "family" to "家庭",
        "work" to "工作",
        "school" to "学校",
    )

    override suspend fun translate(request: TranslationRequest): TranslationResult {
        val trimmed = request.text.trim()
        val key = trimmed.lowercase()
        val translated = dictionary[key]
            ?: if (trimmed.isBlank()) {
                ""
            } else {
                throw NoSuchElementException("Not in local dictionary")
            }
        return TranslationResult(text = translated, providerName = "本地词典")
    }
}
