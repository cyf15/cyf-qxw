# 源码复用与 PVP 适配核对

核对日期：2026-10-10。本文是源码审计结果与实现验收依据，不表示全部卡牌、遗物或效果已经实现。

## 来源与边界

- 参考工程：`../MySlayTheSpire`，来源 <https://github.com/Li-Binghui/MySlayTheSpire>。
- 本地提交已核对为 `c76684426329d59ec95a472792dacb703e59918c`。该工程是第三方反编译并修改过的工程，并非官方原始源码。
- 本次只读检查源码和资源，未运行原游戏、修改上游源码或验证上游可构建性。
- 下表源码路径相对于 `../MySlayTheSpire/src/main/java/com/megacrit/cardcrawl/`；资源路径相对于 `../MySlayTheSpire/src/main/resources/`。
- 用户明确的同机双人轮流 PVP、80/65/70 HP、15 张构筑、3 基础能量、5 基础抽牌、回合末统一结算和未出牌弃置优先。

## 可复用职责与核对入口

| 职责 | 真实源码入口 | 采用时的约束 |
| --- | --- | --- |
| 行动队列 | `actions/GameActionManager.java`，`getNextAction()`、`callEndOfTurnActions()` | 先处理 `actions`，再 `preTurnActions`，再下一张 `cardQueue`。同批卡牌按顺序完成各自效果；不可将伤害、增益分别汇总后处理。 |
| 出牌与费用 | `characters/AbstractPlayer.java`，`useCard()`（实际文件第 1472 行） | 结算时重新计算伤害，调用卡牌 `use()`，追加 `UseCardAction`，再消耗普通费用；不要重复扣费。 |
| 卡牌抽象与工厂 | `cards/AbstractCard.java`、各 `cards/<color>/*.java` 的构造器、`use()`、`upgrade()`、`makeCopy()` | 卡名、费用、基础值、升级值与效果顺序逐卡核对；不能仅凭描述猜效果。 |
| 战斗牌堆 | `cards/CardGroup.java`，`initializeDeck()`、移动/洗牌方法 | 战斗复制卡组，与永久构筑分离；多张同名牌是不同实例，不应按卡牌 ID 去重。 |
| 抽牌 | `actions/common/DrawCardAction.java`，`update()` | 手牌上限 10；抽牌堆耗尽才洗入弃牌堆；`No Draw` 会阻止抽牌。 |
| 卡牌去向 | `actions/utility/UseCardAction.java`，`update()` | 普通卡入弃牌；带耗尽卡入耗尽；能力卡生效后移出牌堆循环，不能当作普通弃牌。 |
| 状态钩子 | `core/AbstractCreature.java`；`powers/*.java` | 开始、出牌前后、回合末和整轮末是不同阶段。不要把每个状态都在换人时减 1。 |
| 能量 | `core/EnergyManager.java`，`recharge()`；`vfx/PlayerTurnEffect.java` | 默认下一回合覆盖为基础能量；能量保留仅来自明确实现的效果，不能自动带到下回合。 |
| 生命与格挡 | `core/AbstractCreature.java`，`decrementBlock()`；`characters/AbstractPlayer.java`，`damage()` | 普通/球伤害会受格挡阻挡；`HP_LOSS` 绕过格挡。归零即按用户规则终局。 |

原工程大量依赖全局 `AbstractDungeon.player`、怪物队列与全局 `EnergyPanel.totalCount`。本地 PVP 应保留这些职责与顺序，但将持有者、对手、牌堆、能量、球和状态绑定到各自玩家，不能复制全局单人状态后交替改指针。

## 统一结算造成的实际语义差异

1. 用户规定效果在回合末统一结算。选牌阶段可以验证并预留费用，但不得提前造成伤害、格挡、抽牌或上状态。结算应按选择顺序逐张执行，再进入结束回合触发，最后换人。
2. 普通源码的 `Offering`、`Adrenaline` 等回能/抽牌卡可立刻支撑后续出牌。若选择阶段严格按当前能量预扣，而结算开始后不能继续选牌，那么结算才获得的能量不能回头支付此前无法入队的牌；结算抽到的牌也来不及选用，并按“未出牌全部弃置”进入弃牌。这是用户批量规则带来的玩法变化，不能声称与原版连锁出牌完全相同，也不能擅自改成即时结算或把新抽牌保留到下一回合。
3. 弱化、易伤、力量、敏捷要在每张牌实际结算时参与计算。例如先 `Bash` 后 `Strike`，第二张打击应受到第一张施加的易伤影响。球伤害不是普通攻击伤害，不能误吃力量、虚弱或易伤倍率。
4. 结算中产生选牌、弃牌、耗尽牌、目标选择等交互时，需要对应的明确选择流程；未实现的卡不应列作可完整使用的卡。
5. 未出牌全部弃置是明确覆盖：`Retain`、`Equilibrium` 等原保留规则不能偷偷恢复；`isEthereal` 等相关冲突也必须记录，不能用原版默认覆盖用户规则。

## 格挡与状态时机

- `GameActionManager.getNextAction()` 的下一玩家回合分支先检查 `Barricade` / `Blur`，通常清除旧格挡。PVP 格挡应保留至持有者下一回合开始，覆盖对方攻击回合，不能自己回合结束就清零。
- `WeakPower`、`VulnerablePower`、`FrailPower` 在 `atEndOfRound()` 衰减，原调用点是 `monsters/MonsterGroup.java:409` 的 `applyEndOfTurnPowers()`；怪物刚施加时还有 `justApplied` 保护。PVP 必须按持有者经历的有效回合适配并测试，不能每换人衰减双方，导致持续时间减半或刚施加就移除。
- `BlurPower.atEndOfRound()` 将减层动作加入队列，但源码检查保甲时减层动作尚未执行。因此 PVP 最直接的等价处理是持有者下一回合开始先保留格挡，再衰减 Blur。`Blur(1)` 施放当回合末立即删除是错误的。
- `PoisonPower.atStartOfTurn()` 触发 `actions/unique/PoisonLoseHpAction`，伤害类型为 `HP_LOSS`，随后毒层数减少。PVP 归属于中毒玩家自己的回合开始，并在允许操作前检测死亡。
- 默认倍率：虚弱普通攻击 ×0.75，易伤普通攻击 ×1.5，脆弱卡牌格挡 ×0.75。整数取整及多段攻击要依实际 `AbstractCard` 计算过程核对。

## 卡池、职业、初始遗物与球

实际直接位于卡牌目录的 Java 文件数量：红 75、绿 75、蓝 76、无色 39；不含 `deprecated`、状态/诅咒、`tempCards` 或第四职业紫卡。无色 39 中包括 `Apparition`、`Bite`、`JAX`、`RitualDagger` 四张 SPECIAL 卡，因此不能把“文件数”宣称为普通商店卡池数，也不能把该数量宣称为当前支持数量。

- `TheSilent.getLoadout()` 的本地参考值是 70 HP，`Defect.getLoadout()` 是 75 HP；本项目按用户明确要求覆盖为 65 和 70。Defect 同方法给出 3 个球位。
- `Strike_Red.use()` 有 `Settings.isDebug` 下 150 伤害分支，`Defend_Red.use()` 有 50 格挡分支；普通分支对应 6 伤害、5 格挡，升级各 +3。不要复制调试作弊分支。
- `CardGroup.initializeDeck()` 把先天和瓶装卡放牌顶，超过基础 5 张时另排额外抽牌，仍受 10 张手牌上限影响。当前模型将 5 张作为基础抽牌，保留源码先天额外抽牌；这意味着大量先天时首回合手牌可以超过 5 张，应在实际界面与验收中明确。
- 起始遗物来自三职业 `getStartingRelics()`：`BurningBlood.onVictory()` 胜利后回复 6 HP；`SnakeRing.atBattleStart()` 多抽 2 张；`CrackedCore.atPreBattle()` 生成一个闪电球。当前模型已保留这三种起始遗物战斗效果，双方独立拥有；Silent 首回合抽 `max(5, 先天卡数量) + 2`，上限 10，此后基础抽 5。Defect 开局 3 球位和 1 个闪电球；Ironclad 获胜后回复 6 HP。
- 球原始值：`orbs/Lightning.java` 回合末伤害 3、激发 8；`Frost.java` 回合末格挡 2、激发 5；`Dark.java` 初始激发 6，每回合末增长 6；`Plasma.java` 回合开始能量 1、激发能量 2。
- `AbstractPlayer.channelOrb()` 在槽满时先激发最早的球、左移，再生成新球。集中加成适用于闪电、冰霜及黑暗增长，不应影响等离子能量。

当前可选卡牌范围已核对为 **123 张基础卡与对应升级版（共 246 定义）**：红 42、绿 28、蓝 37、无色 16，具体清单见 [SUPPORTED_CARDS.md](src/main/resources/catalog/SUPPORTED_CARDS.md)。以上源码目录数量是参考总量，不是已实现数量；其他遗物、药水、战役及要求额外选牌交互的卡牌尚不在当前可玩版本内。

## 可复用素材

| 用途 | 已在本地确认存在的资源 |
| --- | --- |
| 卡面图 | `cards/cards.atlas`、`cards/cards.png` 至 `cards/cards5.png`；卡牌构造器给出对应 region 名。 |
| 卡框和能量图 | `cardui/cardui.atlas`、`cardui/cardui*.png`。 |
| 背景 | `bottomScene/scene.atlas`、`bottomScene/scene*.png`，另有 `cityScene/`、`beyondScene/`。 |
| 静态角色选择图 | `images/ui/charSelect/` 中的职业 portrait 和按钮资源。 |
| 角色动画 | `images/characters/{ironclad,theSilent,defect}/idle/` 的 Spine atlas、JSON、PNG。PNG 为拼图，不是可直接绘制的完整立绘。 |
| 现有 UI 与字体 | `uiskin.json`、`uiskin.atlas`、`uiskin.png`；`default.fnt`、`default.png`；`font/`；`images/whiteSquare32.png`。 |
| 声音 | `audio/sound/`，包括角色语音子目录 `vo/`。 |

当前已接入原三英雄的 Spine 3.4.02 骨骼 atlas/json 和原运行时（40 个 Java 文件），显示活体人物；原骨骼仅有 Idle/Hit。攻击使用 AbstractPlayer.useCard() 调用的 fast attack 位移及原命中特效，不声称存在额外 Attack 骨骼片段。职业选择图继续用于菜单和交接。

反馈由 CombatEvent 记录实际逐次结算（含逐次 HP/格挡快照），GUI 按顺序播放卡牌、前冲、命中、受击、数字、状态、球、抽牌、弃牌和消耗；选择阶段不提前播放命中，结算播放期间不显示下一玩家手牌。原 FlashAtkImgEffect 的 0.6s 时长与纹理用于基础命中特效；Pummel 前几拳 BLUNT_LIGHT、末拳 BLUNT_HEAVY。

已接入原 Exordium 场景分层、烟雾/灰尘、菜单/战斗音乐和环境声，原 SoundMaster 键对应的音效路径导出到 sounds.json，已接入卡牌的效果类型/SFX/专用 VFX 名称导出到 effects.json。专用大型 VFX、其余场景及全部遗物等尚未全部移植。

所有未出牌弃置是用户明确覆盖，包含原 Ethereal 卡；其标志记录在定义中，但未出不会自动耗尽。此处与原单人规则的差异是有意适配。

## 需要真实验收的点

- CUI 和 GUI 均完成选职业、合法 15 张构筑、保存/加载、交替出牌和胜负；不能只测试模型后宣称 GUI 完整。
- 选择卡牌时 HP、格挡和状态不提前变化；重复选择同一实例、越权玩家操作、能量不足被拒绝；统一结算只扣一次费用。
- 连续卡牌的增益/弱化、毒先于回合操作、格挡跨对方回合、Blur 保甲、洗牌不丢/多牌、能力卡和耗尽去向、手牌上限分别覆盖。
- 原素材实际可加载，文字可读，窗口缩放后卡牌和按钮可操作，交接玩家不会误触前一回合的控件。
- 数据库完整保存重复卡和顺序、资料与对局记录；异常路径不静默失败；Windows 运行成功不能代替 macOS 真机验收。


## 本轮验证

61 个 JUnit 测试，新增独立源码预期覆盖升级值、抽牌、多段伤害、Artifact/负力量、临时力量、催化毒、格挡/反伤、能力触发、Buffer、Focus、Darkness/Fission/Recursion/Consume、Apotheosis 和反馈事件。另有所有 246 定义可执行检查；后者不是每个组合均正确的证明。GUI 实机点击验证三骨骼、原音频播放及完整 13 回合对局，截图与 JSON 在 output/final-feedback-smoke。没有运行原游戏作画面/声音同步对比，结果是本地源码对照和本地程序实测，不宣称与原版像素或全部时序完全一致。
