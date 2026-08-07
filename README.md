# Selection Translator

一款 Android 全局选中文字翻译工具。

## 功能
- 任意应用内选中文字后触发翻译
- 原文不遮挡，译文独立浮层展示
- 默认英译中
- 联网优先，离线兜底
- 国内翻译 API 适配
- 历史、收藏、朗读、快捷入口

## 权限
- 无障碍服务
- 悬浮窗权限
- 网络权限

## 技术栈
- Kotlin
- Jetpack Compose
- Room
- coroutines
- AccessibilityService

## 构建
```bash
./gradlew assembleDebug
```

## 测试
```bash
./gradlew testDebugUnitTest
```

## 目录
- `app/` 应用主工程
- `docs/` 设计与计划文档
- `gradle/` Gradle 配置

## 说明
项目当前为可运行原型，后续会继续补强真实翻译接口、OCR 和离线包能力。
