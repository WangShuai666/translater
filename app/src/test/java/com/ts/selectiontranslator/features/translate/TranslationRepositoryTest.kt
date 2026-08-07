package com.ts.selectiontranslator.features.translate

import com.ts.selectiontranslator.data.providers.TranslationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TranslationRepositoryTest {
    @Test
    fun `repository reuses cached translation on second request`() = runTest {
        var calls = 0
        val provider = object : TranslationProvider {
            override suspend fun translate(request: TranslationRequest): TranslationResult {
                calls += 1
                return TranslationResult(text = "cached result", providerName = "mock")
            }
        }

        val repository = TranslationRepository(listOf(provider))
        val request = TranslationRequest("hello", "en", "zh")

        val first = repository.translate(request)
        val second = repository.translate(request)

        assertEquals(1, calls)
        assertSame(first, second)
    }

    @Test
    fun `repository falls back to the next provider after failure`() = runTest {
        var primaryCalls = 0
        var secondaryCalls = 0
        val failingProvider = object : TranslationProvider {
            override suspend fun translate(request: TranslationRequest): TranslationResult {
                primaryCalls += 1
                error("primary down")
            }
        }
        val workingProvider = object : TranslationProvider {
            override suspend fun translate(request: TranslationRequest): TranslationResult {
                secondaryCalls += 1
                return TranslationResult(text = "fallback result", providerName = "secondary")
            }
        }

        val repository = TranslationRepository(listOf(failingProvider, workingProvider))

        val result = repository.translate(TranslationRequest("hello", "en", "zh"))

        assertEquals(1, primaryCalls)
        assertEquals(1, secondaryCalls)
        assertEquals("fallback result", result.text)
        assertEquals("secondary", result.providerName)
    }
}
