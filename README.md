# SelecT

一款安卓全局选中翻译工具。

## 功能
- 在任意应用里选中文字后直接翻译
- 原文保留，译文独立浮层显示，不遮挡原文
- 默认英译中，优先接入国内翻译 API
- 支持历史记录、收藏、离线模式和快捷入口

## 权限
- 无障碍服务
- 悬浮窗权限
- 网络权限

## 构建
```bash
gradlew.bat assembleDebug
```

## 测试
```bash
gradlew.bat testDebugUnitTest
```

## 安装
生成后安装 `app/build/outputs/apk/debug/app-debug.apk` 到手机即可。

## 目录
- `app/` 应用代码
- `docs/` 设计与说明
- `gradle/` 构建配置
