# QBDY Installer Ultra

一款功能强大的 Android 应用管理工具，支持应用安装、应用管理及系统工具功能。

## 功能特点

### 📦 应用安装
- 支持从文件浏览器选择 APK 文件进行安装
- 快速安装 Android 应用

### ⚙️ 应用管理
- 查看和管理已安装的应用
- 应用列表管理功能

### 🔧 工具箱
- 设备管理器权限管理
- 请求所有文件访问权限（适用于 Android 11+）
- 文件浏览器功能

## 技术架构

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **最低 SDK**: Android 6.0 (API 23)
- **目标 SDK**: Android 13 (API 33)

## 核心组件

### MainActivity
应用主入口，包含以下核心功能：
- `InstallTab` - 应用安装标签页
- `AppManageTab` - 应用管理标签页  
- `ToolsTab` - 工具箱标签页
- `ActionCard` - 功能卡片组件
- `GlassCard` - 毛玻璃效果卡片组件
- `requestAllFilesAccess()` - 请求所有文件访问权限
- `showFileBrowser()` / `browseDirectory()` - 文件浏览功能

## 权限说明

应用需要以下权限：
- 设备管理器权限（用于应用管理功能）
- 文件访问权限（用于安装 APK 和文件浏览）

## 安装

1. 克隆项目：
```bash
git clone https://gitee.com/aevirtcn/qbdy-installer-ultra
```

2. 使用 Android Studio 打开项目

3. 构建 debug APK：
```bash
./gradlew assembleDebug
```

4. 在设备上安装生成的 APK

## 使用说明

1. **应用安装**：切换到安装标签页，点击功能卡片选择 APK 文件进行安装
2. **应用管理**：切换到管理标签页，查看和管理已安装的应用
3. **工具箱**：切换到工具标签页，访问设备管理器和文件浏览等功能

## 项目结构

```
app/src/main/java/com/dykt/installerultra/
└── MainActivity.kt          # 主界面与核心逻辑
```

## 许可证

本项目仅供学习和研究使用。