# cyf-qxw
PDC project card game

Java + libGDX 的同机双人卡牌对战项目。当前开发平台为 Windows。

## 下载与运行

[下载 Windows 免安装游戏包](https://github.com/cyf15/cyf-qxw/releases/latest/download/SpireDuel-Windows-x64.zip)（请选择发行附件，源码 ZIP 不是免安装游戏包）。

普通玩家使用免安装包 `SpireDuel-Windows-x64.zip`：完整解压到可写目录后，双击包内的 `启动游戏.cmd`。包内自带 Java、依赖和素材，游玩不需要安装 Java、Maven 或联网下载依赖。适用 Windows 10/11 64 位，显卡需支持游戏使用的 OpenGL。存档在包内 `data/`，更新时保留；启动错误日志在 `logs/startup.log`。请传整个 ZIP，不要只传 CMD 或 JAR。

开发者在已安装 Windows x64 JDK 17+ 的电脑上生成免安装包：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File pvp-game/scripts/package-windows.ps1
```

脚本先构建并运行测试，再生成带独立运行环境的 ZIP，位置为 `pvp-game/output/portable-时间/`。不会打包开发电脑的存档。下面是源码开发方式；GitHub 的源码 ZIP 不等于免安装发行包。

参考源码与原素材由固定版本的 `MySlayTheSpire` 子模块提供：

```powershell
git clone --recurse-submodules https://github.com/cyf15/cyf-qxw.git
cd cyf-qxw
```

如果已普通克隆，可补拉子模块：

```powershell
git submodule update --init --recursive
```

安装 Java 17 或更新版本后，双击 `开始游戏.cmd`。首次运行由 Maven Wrapper 构建，需联网下载依赖。命令行界面使用 `命令行游戏.cmd`。

构建并测试：

```powershell
cd pvp-game
.\mvnw.cmd verify
```

玩法、实现范围和源码对照见 [游戏说明](pvp-game/README.md)、[需求文档](需求文档.md) 和 [源码差异清单](pvp-game/SOURCE_CARD_MATRIX.md)。本地构建产物、存档、自动测试输出不纳入仓库。
