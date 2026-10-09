# 已接入卡牌与升级版

当前 123 张基础卡，每张都有源码对应的升级版，共 246 个可选定义。

表格的数值与标志由 scripts/extract-card-catalog.py 从构造器、upgrade() 和英文卡牌文本提取；效果由独立 Java 引擎执行。
不支持的卡牌不出现在构筑列表。完整源码差异清单见 [SOURCE_CARD_MATRIX.md](../../../../../SOURCE_CARD_MATRIX.md)。

## Ironclad (42)

Strike, Defend, Bash, Anger, Iron Wave, Twin Strike, Cleave, Pommel Strike, Shrug It Off, Battle Trance, Inflame, Metallicize, Seeing Red, Bloodletting, Impervious, Heavy Blade, Clothesline, Thunderclap, Uppercut, Pummel, Bludgeon, Carnage, Sword Boomerang, Hemokinesis, Dropkick, Flex, Limit Break, Shockwave, Intimidate, Disarm, Entrench, Flame Barrier, Rage, Rupture, Berserk, Barricade, Juggernaut, Brutality, Combust, Feel No Pain, Dark Embrace, Demon Form.

## Silent (28)

Strike, Defend, Neutralize, Dagger Spray, Poisoned Stab, Deadly Poison, Backflip, Dodge and Roll, Footwork, Noxious Fumes, Slice, Bane, Outmaneuver, Blur, Dash, Sucker Punch, Flying Knee, Quick Slash, Predator, Leg Sweep, Crippling Cloud, Piercing Wail, Catalyst, Caltrops, Wraith Form, After Image, Envenom, A Thousand Cuts.

## Defect (37)

Strike, Defend, Zap, Dualcast, Ball Lightning, Cold Snap, Beam Cell, Leap, Defragment, Capacitor, Glacier, Compile Driver, Chill, Darkness, Chaos, Charge Battery, Melter, Sweeping Beam, Rip and Tear, Doom and Gloom, Auto-Shields, Fission, Fusion, Coolheaded, Skim, Aggregate, Double Energy, Recursion, Consume, Loop, Electrodynamics, Static Discharge, Biased Cognition, Buffer, Heatsinks, Machine Learning, Storm.

## Colorless (16)

Finesse, Flash of Steel, Good Instincts, Dramatic Entrance, Bandage Up, Panacea, Trip, Blind, Swift Strike, Master of Strategy, Deep Breath, Dark Shackles, Impatience, Apotheosis, Panache, Sadistic Nature.

## 统一结算和源码差异

- 保留用户固定规则：同机双人交替操作，选牌扣费，结束回合逐张结算，基础 3 能量、5 抽牌、15 张卡组及 80/65/70 HP。
- 抽到的牌和本回合增加的能量不能重新加入已经提交的结算批次。所有未出的牌弃置；这也覆盖原版 Ethereal/Retain 的回合末去向。Ethereal 标志保留在定义中，但未出牌不会因此耗尽。
- 15 张卡组可选择已接入基础或升级版，同名卡是不同实例。能力牌移出循环，打出的 Exhaust 卡进入消耗堆。
- 先前回合格挡覆盖对手回合，直到自己下回合开始清除；Blur 与 Barricade 可以保留。
- 抽牌上限 10；抽牌堆空时才洗入弃牌堆；No Draw 阻止抽牌。查看抽牌堆按名称排序，不暴露下次抽牌顺序。
- 原工程 debug 分支的 Strike 150/Defend 50 未采用；正常值 6/5。
- 解锁/遗物/药水/战役、142 张剩余卡牌及交互选牌流程尚未全部接入；不把参考源码存在当作已实现。

## 原素材反馈

- Spine 3.4.02 原骨骼与原运行时：三英雄 Idle；正常攻击造成生命损失时 Hit，再回 Idle。
- 攻击前冲使用 AbstractCreature 的 fast attack 插值与 0.4s 时长；卡牌与命中按真实结算事件播放。
- FlashAtkImgEffect 对应斩击、钝击、火焰、毒和护盾纹理、数字飘字、增益、充能球、抽牌/弃牌飞行、消耗及音效。专用大型卡牌 VFX 仍未全部移植。
- Exordium 分层场景、原烟雾/灰尘纹理；原菜单/战斗音乐及环境音，音量可保存。
