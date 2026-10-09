# cyf-qxw
PDC project card game

Java + libGDX 的同机双人卡牌对战项目。当前开发平台为 Windows。

## 下载与运行

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
