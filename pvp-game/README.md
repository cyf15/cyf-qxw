# Spire Duel：同机双人卡牌对战

Java 17+、libGDX、H2。两人各选一名角色、各构筑恰好 15 张卡，在同一台电脑交替操作。GUI 和 CUI 使用同一个规则引擎与控制器。

## 启动

Windows 可直接双击上一层的 `开始游戏.cmd`，或本目录的 `run-gui.cmd`。命令行版使用 `run-cui.cmd`。已经构建的程序位于 `target/spire-hotseat.jar`，需与 `target/lib/` 一起保留。

当前只支持并验证 Windows。macOS 适配已延后到上一层 [TODO.md](../TODO.md)；现有 `.sh` 脚本仅作未验证草稿保留。

重新构建与测试：

```powershell
.\mvnw.cmd verify
```

Maven Wrapper 首次使用需要网络。构建时从相邻 `../MySlayTheSpire/src/main/resources` 读取原素材，运行打包后的 JAR 不再需要该源码目录。

## 怎么玩

1. 选择 Begin a local duel。
2. 玩家 1 选择 Ironclad、Silent 或 Defect；点击卡牌添加，点击右侧条目移除。也可用 Starter deck 自动填满 15 张后调整。
3. 输入卡组名可 Save deck；Load saved 可加载该职业保存过的卡组。
4. 确认后玩家 2 完成相同步骤。开始对局时将键盘交给当前玩家，点击 Ready 显示手牌。
5. 点击手牌将牌放入待结算队列并支付费用；点击 End turn 后按选择顺序结算，再交给对方。HP 降到 0 判负，结果自动保存。

手牌超过屏幕宽度时横向滚动。Match log 查看战斗记录，主菜单 Match history 查看历史结果。

Settings / Options 可设置全屏/窗口、窗口分辨率、垂直同步、音乐与音效音量；点击 Apply settings 生效并保存到 `data/settings.properties`，下次启动会恢复。在构筑、交接和战斗页面可点击 Options，或按 Esc 打开/关闭设置。菜单中提供 Return to main menu 与 Exit game；正在进行的对局或未保存的构筑会先提示确认，取消可继续。窗口右上角关闭按钮也走相同的退出确认。

每回合基础 3 能量、抽 5 张牌。保留一代三职业初始遗物：Silent 首回合额外抽 2 张，Defect 初始一个闪电球，Ironclad 胜利后恢复 6 HP。职业 HP 按用户要求为 80、65、70。格挡在自己下一回合开始清除；Blur 可保留格挡。

## 当前范围

- 已有可运行的完整对局流程、三职业、卡组构建、CUI/GUI、存档和历史记录。
- 当前实装 123 张基础卡及对应升级版，共 246 个卡牌定义：红 42、绿 28、蓝 37、无色 16。菜单和构筑页只开放已实现的卡牌，不把未实现卡牌当作可用。
- 支持攻击、格挡、多段攻击、力量、敏捷、虚弱、易伤、毒、人工制品、能力牌、消耗、抽牌、能量及四种充能球等。完整清单见 [卡牌范围](src/main/resources/catalog/SUPPORTED_CARDS.md)。
- 原版共 265 个红/绿/蓝/无色卡牌源码文件；其余 142 张基础卡及更多遗物仍待扩展，逐文件差异见 [SOURCE_CARD_MATRIX.md](SOURCE_CARD_MATRIX.md)。没有实现原版完整单人爬塔流程。
- 按用户“回合末统一结算”规则，抽牌和本回合加能量也延后生效；结算期间不再次选牌，因此不具备原版即时抽牌/回能后继续出牌的连锁。未出的牌在回合结束弃置。这是该明确玩法规则产生的差异，不能称为完整复现原版出牌时序。
- GUI 使用原版 Spine 骨骼与 Idle/Hit 动画、源码攻击位移、原特效图集、Exordium 分层背景和原音乐/音效。抽牌/弃牌及逐次命中反馈读取真实引擎事件；播放结束才交接机器。当前使用英文，未生成新美术素材。
- 城市/彼方场景、火炬交互、所有专用大型卡牌特效及完整原 UI 仍待接入；已有资源不等于已完成移植。

## 存档与文件

默认保存在当前目录的 `data/`：H2 数据库包含卡组、玩家资料、比赛记录和日志；`data/logs/last-match.txt` 为最近一局文本日志。可通过 `-Dpvp.dataDir=路径` 指定独立目录。

CUI 构筑命令 `save 名称`、`load` 操作数据库，`export 路径` 导出 JSON；主菜单 Import deck JSON 导入。非法卡牌、错误职业或非 15 张卡的文件会被拒绝。

## 结构与来源

- `model/`：纯 Java 战斗模型，卡牌父类与攻击/技能/能力子类、CardFactory、状态快照和 Observer。
- `controller/`：CUI/GUI 共享的游戏操作、卡组验证和完成对局保存。
- `view/cui/` 与 `view/gui/`：可替换的文本/图形视图。
- `persistence/`：GameDao 接口与 H2/JDBC 实现。
- `scripts/extract-card-catalog.py`：从本地一代源码及英文文本提取卡名、费用、数值和图片路径。

参考工程：[Li-Binghui/MySlayTheSpire](https://github.com/Li-Binghui/MySlayTheSpire)，提交 `c76684426329d59ec95a472792dacb703e59918c`。复用原卡数据和图片、UI、字体资源；依据原卡行动和状态代码适配双玩家引擎，没有直接运行原单人全局状态。源码对应关系见 [SOURCE_NOTES.md](SOURCE_NOTES.md)。原工程保留在相邻目录。

## 验证

当前 61 个 JUnit 测试全部通过，覆盖牌堆、费用、队列延迟、伤害与状态、球、胜负、存档重开、损坏导入保护，以及长对局 CUI。测试报告在 `target/surefire-reports/`。Windows GUI 已通过 93 次界面点击完成 13 回合对局，并验证卡组保存/读取、新增比赛记录、三英雄骨骼、音频播放和逐次战斗反馈（44 次出牌、29 次伤害）。结果见 `output/final-feedback-smoke/gui-smoke.json`。

GUI 实机流程验证：

```powershell
java -Dpvp.smoke=true -Dpvp.dataDir=output/gui-test-data -jar target/spire-hotseat.jar
```

测试通过实际 Scene2D 按钮事件从菜单打到胜负，输出 `output/gui-smoke.json` 及各页面截图。测试存档与正式 `data/` 分开。CUI 流程证据在 `output/cui-smoke.txt`。

Project 1/2 提交材料独立维护在上一层 `提交材料清单.md`，后续再整理。
