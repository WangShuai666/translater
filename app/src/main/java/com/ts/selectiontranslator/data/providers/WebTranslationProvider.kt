package com.ts.selectiontranslator.data.providers

import com.ts.selectiontranslator.features.translate.TranslationRequest
import com.ts.selectiontranslator.features.translate.TranslationResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class WebTranslationProvider : TranslationProvider {
    override suspend fun translate(request: TranslationRequest): TranslationResult = withContext(Dispatchers.IO) {
        runCatching {
            TranslationResult(text = googleTranslate(request.text), providerName = "在线翻译")
        }.getOrElse {
            TranslationResult(text = myMemoryTranslate(request.text), providerName = "在线翻译")
        }
    }

    private fun googleTranslate(text: String): String {
        val endpoint = "https://translate.googleapis.com/translate_a/single" +
            "?client=gtx&sl=auto&tl=zh-CN&dt=t&q=" + URLEncoder.encode(text, "UTF-8")
        val raw = request(endpoint)
        val root = JSONArray(raw)
        val segments = root.getJSONArray(0)
        val translated = StringBuilder()
        for (index in 0 until segments.length()) {
            val segment = segments.getJSONArray(index)
            if (segment.length() > 0 && !segment.isNull(0)) {
                translated.append(segment.getString(0))
            }
        }
        return translated.toString().trim().ifBlank { throw IOException("Empty translation") }
    }

    private fun myMemoryTranslate(text: String): String {
        val source = if (containsChinese(text)) "zh-CN" else "en"
        val target = if (source == "zh-CN") "en" else "zh-CN"
        val endpoint = "https://api.mymemory.translated.net/get?q=" +
            URLEncoder.encode(text, "UTF-8") +
            "&langpair=$source|$target"
        val raw = request(endpoint)
        val root = JSONObject(raw)
        if (root.optInt("responseStatus") != 200) {
            throw IOException(root.optString("responseDetails"))
        }
        return root
            .optJSONObject("responseData")
            ?.optString("translatedText")
            .orEmpty()
            .trim()
            .ifBlank { throw IOException("Empty translation") }
    }

    private fun request(endpoint: String): String {
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 4000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "SelecT/0.1")
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun containsChinese(text: String): Boolean {
        return text.any { char ->
            char.code in 0x4E00..0x9FFF || char.code in 0x3400..0x4DBF
        }
    }
}
