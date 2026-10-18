# cyf-qxw
PDC project card game

Java + libGDX 的同机双人卡牌对战项目。开发平台以 Windows 为主；macOS（Apple Silicon）已实机验证，可作为可选运行平台。

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

### Windows

安装 Java 17 或更新版本后，双击 `开始游戏.cmd`。首次运行由 Maven Wrapper 构建，需联网下载依赖。命令行界面使用 `命令行游戏.cmd`。

构建并测试：

```powershell
cd pvp-game
.\mvnw.cmd verify
```

### macOS（Apple Silicon）

安装 JDK 17 或更新版本（如 [Temurin 17](https://adoptium.net/)）后，在项目根目录双击：

- `开始游戏.command` — 图形界面（自动以 `-XstartOnFirstThread` 启动）
- `命令行游戏.command` — 命令行界面

也可在终端运行：

```bash
cd pvp-game
./run-gui.sh    # 图形界面
./run-cui.sh    # 命令行界面
```

首次运行由 Maven Wrapper 自动构建（需联网）。构建并测试：

```bash
cd pvp-game
./mvnw verify
```

已在本机 Apple Silicon（arm64）MacBook 验证：65 个 JUnit 测试全部通过，GUI 以 91 次界面点击完成 13 回合完整对局（见 `pvp-game/output/gui-smoke.json`）。Intel（x86_64）Mac 尚未验证，见 [TODO.md](TODO.md)。

玩法、实现范围和源码对照见 [游戏说明](pvp-game/README.md)、[需求文档](需求文档.md) 和 [源码差异清单](pvp-game/SOURCE_CARD_MATRIX.md)。本地构建产物、存档、自动测试输出不纳入仓库。
