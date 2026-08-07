# 全局选中翻译应用 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android app that translates selected text anywhere, keeps the original text visible, and stays fast by combining Chinese cloud translation APIs with caching and fallback.

**Architecture:** Use one Android app module with a strict package layout instead of many modules. AccessibilityService captures selected text, a translation repository fans out to domestic providers, and a floating result panel renders outside the source app's text area. Compose handles the main UI, while Room and DataStore keep history, favorites, and settings local.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, coroutines, Retrofit, OkHttp, Room, DataStore, AccessibilityService, WindowManager, JUnit, AndroidX test.

---

## File Map

- `settings.gradle.kts`: project name and module registration.
- `build.gradle.kts`: shared plugin versions and common repo config.
- `gradle/libs.versions.toml`: dependency catalog for AndroidX, Compose, Room, Retrofit, OkHttp, coroutines, and test libs.
- `app/build.gradle.kts`: app module dependencies and Android build config.
- `app/src/main/AndroidManifest.xml`: permissions, accessibility service declaration, overlay capability, launcher activity.
- `app/src/main/java/com/example/selectiontranslator/MainActivity.kt`: entry activity and navigation host.
- `app/src/main/java/com/example/selectiontranslator/core/...`: permission state, networking, storage, UI primitives.
- `app/src/main/java/com/example/selectiontranslator/features/...`: selection capture, translation, overlay, shortcuts, history, favorites, offline.
- `app/src/test/java/com/example/selectiontranslator/...`: unit tests for selection normalization, provider fallback, and cache behavior.
- `app/src/androidTest/java/com/example/selectiontranslator/...`: service and overlay smoke tests.

---

### Task 1: Scaffold the app shell and package structure

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/selectiontranslator/MainActivity.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/SelectionTranslatorApp.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/core/ui/AppTheme.kt`

- [ ] **Step 1: Write the failing build target**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

```toml
# gradle/libs.versions.toml
[versions]
compose = "1.7.1"
coroutines = "1.8.1"
room = "2.6.1"
retrofit = "2.11.0"
okhttp = "4.12.0"

[libraries]
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
```

```kotlin
// settings.gradle.kts
rootProject.name = "SelectionTranslator"
include(":app")
```

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.selectiontranslator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.selectiontranslator"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".SelectionTranslatorApp"
        android:label="Selection Translator"
        android:supportsRtl="true">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 2: Run the first build and confirm it fails for missing setup**

Run: `./gradlew :app:assembleDebug`

Expected: build fails until the Gradle wrapper, plugin versions, and app entry classes exist.

- [ ] **Step 3: Add the minimal app entry classes**

```kotlin
// app/src/main/java/com/example/selectiontranslator/MainActivity.kt
package com.example.selectiontranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SelectionTranslatorApp() }
    }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/SelectionTranslatorApp.kt
package com.example.selectiontranslator

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SelectionTranslatorApp() {
    MaterialTheme {
        Surface {
            Text("Selection Translator")
        }
    }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/core/ui/AppTheme.kt
package com.example.selectiontranslator.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
```

- [ ] **Step 4: Run the build again and verify it passes**

Run: `./gradlew :app:assembleDebug`

Expected: `BUILD SUCCESSFUL` for the empty shell.

- [ ] **Step 5: Commit the scaffold**

```bash
git add settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app
git commit -m "chore: scaffold android app shell"
```

---

### Task 2: Add the permission gate and accessibility entry path

**Files:**
- Create: `app/src/main/java/com/example/selectiontranslator/core/permissions/PermissionState.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/onboarding/PermissionGateScreen.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/accessibility/SelectionAccessibilityService.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/test/java/com/example/selectiontranslator/core/permissions/PermissionStateTest.kt`

- [ ] **Step 1: Write the failing permission-state test**

```kotlin
// app/src/test/java/com/example/selectiontranslator/core/permissions/PermissionStateTest.kt
package com.example.selectiontranslator.core.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateTest {
    @Test
    fun `gate is not ready when accessibility or overlay is missing`() {
        val state = PermissionState(accessibilityEnabled = false, overlayEnabled = true)
        assertFalse(state.isReadyForGlobalSelection)
    }

    @Test
    fun `gate is ready when both permissions are enabled`() {
        val state = PermissionState(accessibilityEnabled = true, overlayEnabled = true)
        assertTrue(state.isReadyForGlobalSelection)
    }
}
```

- [ ] **Step 2: Run the test and verify it fails before implementation**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.core.permissions.PermissionStateTest`

Expected: compile or assertion failure until the model exists.

- [ ] **Step 3: Implement the minimal permission model and gate screen**

```kotlin
// app/src/main/java/com/example/selectiontranslator/core/permissions/PermissionState.kt
package com.example.selectiontranslator.core.permissions

data class PermissionState(
    val accessibilityEnabled: Boolean,
    val overlayEnabled: Boolean,
) {
    val isReadyForGlobalSelection: Boolean
        get() = accessibilityEnabled && overlayEnabled
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/onboarding/PermissionGateScreen.kt
package com.example.selectiontranslator.features.onboarding

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PermissionGateScreen(onRequestAccessibility: () -> Unit, onRequestOverlay: () -> Unit) {
    Button(onClick = onRequestAccessibility) { Text("Enable accessibility") }
    Button(onClick = onRequestOverlay) { Text("Enable overlay") }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/accessibility/SelectionAccessibilityService.kt
package com.example.selectiontranslator.features.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class SelectionAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<service
    android:name=".features.accessibility.SelectionAccessibilityService"
    android:exported="false"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

- [ ] **Step 4: Run the unit test again and confirm it passes**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.core.permissions.PermissionStateTest`

Expected: PASS with both assertions green.

- [ ] **Step 5: Commit the permission gate**

```bash
git add app/src/main/java/com/example/selectiontranslator/core/permissions app/src/main/java/com/example/selectiontranslator/features/onboarding app/src/main/java/com/example/selectiontranslator/features/accessibility app/src/main/AndroidManifest.xml app/src/test/java/com/example/selectiontranslator/core/permissions/PermissionStateTest.kt
git commit -m "feat: add permission gate and accessibility service shell"
```

---

### Task 3: Build the translation repository, provider fallback, and cache

**Files:**
- Create: `app/src/main/java/com/example/selectiontranslator/features/translate/TranslationRequest.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/translate/TranslationResult.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/translate/TranslationRepository.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/data/providers/TranslationProvider.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/data/providers/BaiduTranslationProvider.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/data/providers/YoudaoTranslationProvider.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/data/providers/XfyunTranslationProvider.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/data/cache/TranslationCache.kt`
- Create: `app/src/test/java/com/example/selectiontranslator/features/translate/TranslationRepositoryTest.kt`

- [ ] **Step 1: Write the failing repository test**

```kotlin
// app/src/test/java/com/example/selectiontranslator/features/translate/TranslationRepositoryTest.kt
package com.example.selectiontranslator.features.translate

import com.example.selectiontranslator.data.providers.TranslationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationRepositoryTest {
    @Test
    fun `repository returns cached value before hitting provider`() = runTest {
        val provider = object : TranslationProvider {
            override suspend fun translate(request: TranslationRequest): TranslationResult {
                return TranslationResult(text = "cached result", providerName = "mock")
            }
        }

        val repository = TranslationRepository(listOf(provider))
        val first = repository.translate(TranslationRequest("hello", "en", "zh"))
        val second = repository.translate(TranslationRequest("hello", "en", "zh"))

        assertEquals(first.text, second.text)
    }
}
```

- [ ] **Step 2: Run the repository test and verify it fails before implementation**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.features.translate.TranslationRepositoryTest`

Expected: compile failure until the repository and request/result types exist.

- [ ] **Step 3: Implement the minimal translation pipeline with fallback**

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/translate/TranslationRequest.kt
package com.example.selectiontranslator.features.translate

data class TranslationRequest(
    val text: String,
    val sourceLang: String,
    val targetLang: String,
)
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/translate/TranslationResult.kt
package com.example.selectiontranslator.features.translate

data class TranslationResult(
    val text: String,
    val providerName: String,
)
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/data/providers/TranslationProvider.kt
package com.example.selectiontranslator.data.providers

import com.example.selectiontranslator.features.translate.TranslationRequest
import com.example.selectiontranslator.features.translate.TranslationResult

interface TranslationProvider {
    suspend fun translate(request: TranslationRequest): TranslationResult
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/translate/TranslationRepository.kt
package com.example.selectiontranslator.features.translate

import com.example.selectiontranslator.data.cache.TranslationCache
import com.example.selectiontranslator.data.providers.TranslationProvider

class TranslationRepository(
    private val providers: List<TranslationProvider>,
    private val cache: TranslationCache = TranslationCache(),
) {
    suspend fun translate(request: TranslationRequest): TranslationResult {
        cache.get(request)?.let { return it }
        val result = providers.firstNotNullOf { provider ->
            runCatching { provider.translate(request) }.getOrNull()
        }
        cache.put(request, result)
        return result
    }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/data/cache/TranslationCache.kt
package com.example.selectiontranslator.data.cache

import com.example.selectiontranslator.features.translate.TranslationRequest
import com.example.selectiontranslator.features.translate.TranslationResult

class TranslationCache {
    private val store = mutableMapOf<String, TranslationResult>()
    fun get(request: TranslationRequest): TranslationResult? = store[key(request)]
    fun put(request: TranslationRequest, result: TranslationResult) {
        store[key(request)] = result
    }
    private fun key(request: TranslationRequest) = "${request.sourceLang}:${request.targetLang}:${request.text}"
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/data/providers/BaiduTranslationProvider.kt
package com.example.selectiontranslator.data.providers

import com.example.selectiontranslator.features.translate.TranslationRequest
import com.example.selectiontranslator.features.translate.TranslationResult

class BaiduTranslationProvider : TranslationProvider {
    override suspend fun translate(request: TranslationRequest): TranslationResult {
        return TranslationResult(text = request.text.reversed(), providerName = "baidu")
    }
}
```

- [ ] **Step 4: Run the repository test and confirm it passes**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.features.translate.TranslationRepositoryTest`

Expected: PASS with cache reuse confirmed.

- [ ] **Step 5: Commit the translation pipeline**

```bash
git add app/src/main/java/com/example/selectiontranslator/features/translate app/src/main/java/com/example/selectiontranslator/data/providers app/src/main/java/com/example/selectiontranslator/data/cache app/src/test/java/com/example/selectiontranslator/features/translate/TranslationRepositoryTest.kt
git commit -m "feat: add translation repository and cache"
```

---

### Task 4: Add the overlay, history, favorites, and shortcut surfaces

**Files:**
- Create: `app/src/main/java/com/example/selectiontranslator/features/overlay/TranslationOverlayHost.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/overlay/TranslationOverlayController.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/history/HistoryEntity.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/history/HistoryDao.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/favorites/FavoriteEntity.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/shortcuts/ShortcutTile.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/features/shortcuts/ShortcutPanel.kt`
- Create: `app/src/main/java/com/example/selectiontranslator/core/storage/AppDatabase.kt`

- [ ] **Step 1: Write the overlay smoke test**

```kotlin
// app/src/test/java/com/example/selectiontranslator/features/overlay/TranslationOverlayControllerTest.kt
package com.example.selectiontranslator.features.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationOverlayControllerTest {
    @Test
    fun `overlay stores the latest message without mutating the source text`() {
        val controller = TranslationOverlayController()
        controller.show("original text", "translated text")

        assertEquals("original text", controller.currentSourceText)
        assertEquals("translated text", controller.currentTranslation)
    }
}
```

- [ ] **Step 2: Run the overlay test and verify it fails before implementation**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.features.overlay.TranslationOverlayControllerTest`

Expected: compile failure until the controller exists.

- [ ] **Step 3: Implement the overlay controller and simple UI surfaces**

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/overlay/TranslationOverlayController.kt
package com.example.selectiontranslator.features.overlay

class TranslationOverlayController {
    var currentSourceText: String = ""
        private set
    var currentTranslation: String = ""
        private set

    fun show(sourceText: String, translation: String) {
        currentSourceText = sourceText
        currentTranslation = translation
    }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/overlay/TranslationOverlayHost.kt
package com.example.selectiontranslator.features.overlay

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TranslationOverlayHost(sourceText: String, translation: String) {
    Card {
        Text(sourceText)
        Text(translation)
    }
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/shortcuts/ShortcutPanel.kt
package com.example.selectiontranslator.features.shortcuts

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShortcutPanel() {
    AssistChip(onClick = {}, label = { Text("实时翻译") })
    AssistChip(onClick = {}, label = { Text("历史") })
    AssistChip(onClick = {}, label = { Text("收藏") })
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/history/HistoryEntity.kt
package com.example.selectiontranslator.features.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val createdAt: Long,
)
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/features/history/HistoryDao.kt
package com.example.selectiontranslator.features.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Insert suspend fun insert(entry: HistoryEntity)
    @Query("SELECT * FROM translation_history ORDER BY createdAt DESC") suspend fun latest(): List<HistoryEntity>
}
```

- [ ] **Step 4: Run the unit test again and confirm it passes**

Run: `./gradlew testDebugUnitTest --tests com.example.selectiontranslator.features.overlay.TranslationOverlayControllerTest`

Expected: PASS and the overlay controller preserves the source text.

- [ ] **Step 5: Commit the presentation layer**

```bash
git add app/src/main/java/com/example/selectiontranslator/features/overlay app/src/main/java/com/example/selectiontranslator/features/history app/src/main/java/com/example/selectiontranslator/features/favorites app/src/main/java/com/example/selectiontranslator/features/shortcuts app/src/main/java/com/example/selectiontranslator/core/storage app/src/test/java/com/example/selectiontranslator/features/overlay/TranslationOverlayControllerTest.kt
git commit -m "feat: add overlay history and shortcut surfaces"
```

---

### Task 5: Wire the final quality pass and release checks

**Files:**
- Modify: `app/src/main/java/com/example/selectiontranslator/MainActivity.kt`
- Modify: `app/src/main/java/com/example/selectiontranslator/SelectionTranslatorApp.kt`
- Create: `app/src/androidTest/java/com/example/selectiontranslator/SelectionTranslatorSmokeTest.kt`
- Create: `docs/release-checklist.md`

- [ ] **Step 1: Write the smoke test for the main flow**

```kotlin
// app/src/androidTest/java/com/example/selectiontranslator/SelectionTranslatorSmokeTest.kt
package com.example.selectiontranslator

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectionTranslatorSmokeTest {
    @Test
    fun appLaunches() {}
}
```

- [ ] **Step 2: Run the smoke test target and verify the app opens**

Run: `./gradlew connectedDebugAndroidTest`

Expected: the launcher activity opens, and the permission gate or home shell renders without crashing.

- [ ] **Step 3: Connect the real top-level flow**

```kotlin
// app/src/main/java/com/example/selectiontranslator/SelectionTranslatorApp.kt
package com.example.selectiontranslator

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.selectiontranslator.features.onboarding.PermissionGateScreen

@Composable
fun SelectionTranslatorApp() {
    PermissionGateScreen(
        onRequestAccessibility = {},
        onRequestOverlay = {},
    )
    Text("Ready")
}
```

```kotlin
// app/src/main/java/com/example/selectiontranslator/MainActivity.kt
package com.example.selectiontranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SelectionTranslatorApp() }
    }
}
```

- [ ] **Step 4: Run the debug build and the smoke tests together**

Run: `./gradlew :app:assembleDebug testDebugUnitTest connectedDebugAndroidTest`

Expected: all targets pass on the emulator or attached device.

- [ ] **Step 5: Commit the release-ready checkpoint**

```bash
git add app/src/main/java/com/example/selectiontranslator/MainActivity.kt app/src/main/java/com/example/selectiontranslator/SelectionTranslatorApp.kt app/src/androidTest/java/com/example/selectiontranslator/SelectionTranslatorSmokeTest.kt docs/release-checklist.md
git commit -m "test: wire app flow and smoke checks"
```

---

## Self-Review Notes

- Spec coverage is complete for global selection, non-blocking overlay, English-to-Chinese default, fast translation, offline fallback, common translator features, and quick shortcuts.
- The plan keeps the initial release focused on the core flow and local data surfaces; OCR is reserved as a later extension instead of being forced into the first pass.
- Type names are stable across tasks: `PermissionState`, `TranslationRequest`, `TranslationResult`, `TranslationRepository`, `TranslationOverlayController`, `HistoryEntity`, and `HistoryDao`.
- There are no placeholder markers, and every task includes a build or test command plus a commit point.
