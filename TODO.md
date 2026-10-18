# 开发 TODO

当前开发与验收平台：Windows。macOS 已在 Apple Silicon（arm64）MacBook 上完成实机验证，可作为可选运行平台；Intel Mac 尚未验证，见下方「macOS 适配」说明。

## macOS 适配

- [x] 核对 Apple Silicon 的 Java（Temurin 17 arm64）、libGDX 1.13.1 与 LWJGL3 原生库兼容性。
- [x] 验证 macOS 启动参数（GUI 需 `-XstartOnFirstThread`）、依赖加载与窗口显示。
- [x] 验证 macOS 上的路径、存档（H2 `data/`）、键鼠操作和界面缩放。
- [x] 在 Mac 实机跑通 CUI/GUI 完整对局及存档读写，并提供正式启动说明。
- [ ] Intel（x86_64）Mac 的 Java、libGDX 与原生库兼容性尚未验证。

macOS 启动：双击项目根目录的 `开始游戏.command`（图形界面）或 `命令行游戏.command`（命令行）。首次需 JDK 17+，构建/测试用 `pvp-game` 下的 `./mvnw verify`。Apple Silicon 实机已验证 65 个 JUnit 测试全部通过，GUI 以 91 次界面点击完成 13 回合完整对局（见 `pvp-game/output/gui-smoke.json`）。

## 游戏内容扩展

- [ ] 按一代源码继续接入剩余基础卡牌。当前可玩范围为 122 张基础卡；卡牌升级已按用户要求移除，Apotheosis 同步关闭；其余 143 张见 pvp-game/SOURCE_CARD_MATRIX.md。

- [ ] 接入城市、彼方等其余场景与原动态层、火炬交互。
- [ ] 继续按源码接入专用大型卡牌 VFX（如完整 IronWave、RipAndTear 等），不只使用基础命中图。
- [ ] 继续接入其余遗物、药水、状态牌与交互选牌流程。
