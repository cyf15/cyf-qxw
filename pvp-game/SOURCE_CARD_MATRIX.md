# 一代卡牌源码与本地实现清单

由 `scripts/audit-source.py` 读取实际源码文件和本地可执行目录生成。卡牌源码存在不等于已经实现。

参考源码 265 个文件，本地支持 123 个基础卡文件及对应升级版，其余 142 个仍待接入。

已接入项的具体顺序和交互由 SourceParityTest 验证；原版批量规则差异见 SOURCE_NOTES.md。

| 职业 | 源码 ID | 状态 | 源码使用的行动/能力 | 源码文件 |
| --- | --- | --- | --- | --- |
| red | Anger | 已接入基础与升级版 | DamageAction, MakeTempCardInDiscardAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Anger.java` |
| red | Armaments | 未接入 | ArmamentsAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Armaments.java` |
| red | Barricade | 已接入基础与升级版 | ApplyPowerAction, BarricadePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Barricade.java` |
| red | Bash | 已接入基础与升级版 | ApplyPowerAction, DamageAction, DamageAllEnemiesAction, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Bash.java` |
| red | Battle Trance | 已接入基础与升级版 | ApplyPowerAction, DrawCardAction, NoDrawPower | `src/main/java/com/megacrit/cardcrawl/cards/red/BattleTrance.java` |
| red | Berserk | 已接入基础与升级版 | ApplyPowerAction, BerserkPower, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Berserk.java` |
| red | Blood for Blood | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/BloodForBlood.java` |
| red | Bloodletting | 已接入基础与升级版 | GainEnergyAction, LoseHPAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Bloodletting.java` |
| red | Bludgeon | 已接入基础与升级版 | DamageAction, VFXAction, WaitAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Bludgeon.java` |
| red | Body Slam | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/BodySlam.java` |
| red | Brutality | 已接入基础与升级版 | ApplyPowerAction, BrutalityPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Brutality.java` |
| red | Burning Pact | 未接入 | DrawCardAction, ExhaustAction | `src/main/java/com/megacrit/cardcrawl/cards/red/BurningPact.java` |
| red | Carnage | 已接入基础与升级版 | DamageAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Carnage.java` |
| red | Clash | 未接入 | DamageAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Clash.java` |
| red | Cleave | 已接入基础与升级版 | DamageAllEnemiesAction, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Cleave.java` |
| red | Clothesline | 已接入基础与升级版 | ApplyPowerAction, DamageAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Clothesline.java` |
| red | Combust | 已接入基础与升级版 | ApplyPowerAction, CombustPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Combust.java` |
| red | Corruption | 未接入 | ApplyPowerAction, CorruptionPower, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Corruption.java` |
| red | Dark Embrace | 已接入基础与升级版 | ApplyPowerAction, DarkEmbracePower | `src/main/java/com/megacrit/cardcrawl/cards/red/DarkEmbrace.java` |
| red | Defend_R | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Defend_Red.java` |
| red | Demon Form | 已接入基础与升级版 | ApplyPowerAction, DemonFormPower | `src/main/java/com/megacrit/cardcrawl/cards/red/DemonForm.java` |
| red | Disarm | 已接入基础与升级版 | ApplyPowerAction, StrengthPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Disarm.java` |
| red | Double Tap | 未接入 | ApplyPowerAction, DoubleTapPower | `src/main/java/com/megacrit/cardcrawl/cards/red/DoubleTap.java` |
| red | Dropkick | 已接入基础与升级版 | DropkickAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Dropkick.java` |
| red | Dual Wield | 未接入 | DualWieldAction | `src/main/java/com/megacrit/cardcrawl/cards/red/DualWield.java` |
| red | Entrench | 已接入基础与升级版 | DoubleYourBlockAction, ExhaustAllEtherealAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Entrench.java` |
| red | Evolve | 未接入 | ApplyPowerAction, EvolvePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Evolve.java` |
| red | Exhume | 未接入 | ExhumeAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Exhume.java` |
| red | Feed | 未接入 | FeedAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Feed.java` |
| red | Feel No Pain | 已接入基础与升级版 | ApplyPowerAction, FeelNoPainPower | `src/main/java/com/megacrit/cardcrawl/cards/red/FeelNoPain.java` |
| red | Fiend Fire | 未接入 | FiendFireAction | `src/main/java/com/megacrit/cardcrawl/cards/red/FiendFire.java` |
| red | Fire Breathing | 未接入 | ApplyPowerAction, FireBreathingPower | `src/main/java/com/megacrit/cardcrawl/cards/red/FireBreathing.java` |
| red | Flame Barrier | 已接入基础与升级版 | ApplyPowerAction, FlameBarrierPower, GainBlockAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/FlameBarrier.java` |
| red | Flex | 已接入基础与升级版 | ApplyPowerAction, LoseStrengthPower, StrengthPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Flex.java` |
| red | Ghostly Armor | 未接入 | ExhaustAllEtherealAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/GhostlyArmor.java` |
| red | Havoc | 未接入 | PlayTopCardAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Havoc.java` |
| red | Headbutt | 未接入 | DamageAction, DiscardPileToTopOfDeckAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Headbutt.java` |
| red | Heavy Blade | 已接入基础与升级版 | DamageAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/HeavyBlade.java` |
| red | Hemokinesis | 已接入基础与升级版 | DamageAction, LoseHPAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Hemokinesis.java` |
| red | Immolate | 未接入 | DamageAllEnemiesAction, MakeTempCardInDiscardAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Immolate.java` |
| red | Impervious | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Impervious.java` |
| red | Infernal Blade | 未接入 | MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/red/InfernalBlade.java` |
| red | Inflame | 已接入基础与升级版 | ApplyPowerAction, StrengthPower, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Inflame.java` |
| red | Intimidate | 已接入基础与升级版 | ApplyPowerAction, SFXAction, VFXAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Intimidate.java` |
| red | Iron Wave | 已接入基础与升级版 | DamageAction, GainBlockAction, VFXAction, WaitAction | `src/main/java/com/megacrit/cardcrawl/cards/red/IronWave.java` |
| red | Juggernaut | 已接入基础与升级版 | ApplyPowerAction, JuggernautPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Juggernaut.java` |
| red | Limit Break | 已接入基础与升级版 | LimitBreakAction | `src/main/java/com/megacrit/cardcrawl/cards/red/LimitBreak.java` |
| red | Metallicize | 已接入基础与升级版 | ApplyPowerAction, MetallicizePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Metallicize.java` |
| red | Offering | 未接入 | DrawCardAction, GainEnergyAction, LoseHPAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Offering.java` |
| red | Perfected Strike | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/PerfectedStrike.java` |
| red | Pommel Strike | 已接入基础与升级版 | DamageAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/red/PommelStrike.java` |
| red | Power Through | 未接入 | GainBlockAction, MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/red/PowerThrough.java` |
| red | Pummel | 已接入基础与升级版 | DamageAction, PummelDamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Pummel.java` |
| red | Rage | 已接入基础与升级版 | ApplyPowerAction, RagePower, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Rage.java` |
| red | Rampage | 未接入 | DamageAction, ModifyDamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Rampage.java` |
| red | Reaper | 未接入 | VFXAction, VampireDamageAllEnemiesAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Reaper.java` |
| red | Reckless Charge | 未接入 | DamageAction, MakeTempCardInDrawPileAction | `src/main/java/com/megacrit/cardcrawl/cards/red/RecklessCharge.java` |
| red | Rupture | 已接入基础与升级版 | ApplyPowerAction, RupturePower | `src/main/java/com/megacrit/cardcrawl/cards/red/Rupture.java` |
| red | Searing Blow | 未接入 | DamageAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SearingBlow.java` |
| red | Second Wind | 未接入 | BlockPerNonAttackAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SecondWind.java` |
| red | Seeing Red | 已接入基础与升级版 | GainEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SeeingRed.java` |
| red | Sentinel | 未接入 | GainBlockAction, GainEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Sentinel.java` |
| red | Sever Soul | 未接入 | DamageAction, ExhaustAllNonAttackAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SeverSoul.java` |
| red | Shockwave | 已接入基础与升级版 | ApplyPowerAction, VulnerablePower, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Shockwave.java` |
| red | Shrug It Off | 已接入基础与升级版 | DrawCardAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/ShrugItOff.java` |
| red | Spot Weakness | 未接入 | SpotWeaknessAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SpotWeakness.java` |
| red | Strike_R | 已接入基础与升级版 | DamageAction, DamageAllEnemiesAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Strike_Red.java` |
| red | Sword Boomerang | 已接入基础与升级版 | AttackDamageRandomEnemyAction | `src/main/java/com/megacrit/cardcrawl/cards/red/SwordBoomerang.java` |
| red | Thunderclap | 未接入 | ApplyPowerAction, DamageAllEnemiesAction, SFXAction, VFXAction, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/red/ThunderClap.java` |
| red | True Grit | 未接入 | ExhaustAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/red/TrueGrit.java` |
| red | Twin Strike | 已接入基础与升级版 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/red/TwinStrike.java` |
| red | Uppercut | 已接入基础与升级版 | ApplyPowerAction, DamageAction, VulnerablePower, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/red/Uppercut.java` |
| red | Warcry | 未接入 | DrawCardAction, PutOnDeckAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Warcry.java` |
| red | Whirlwind | 未接入 | WhirlwindAction | `src/main/java/com/megacrit/cardcrawl/cards/red/Whirlwind.java` |
| red | Wild Strike | 未接入 | DamageAction, MakeTempCardInDrawPileAction | `src/main/java/com/megacrit/cardcrawl/cards/red/WildStrike.java` |
| green | Accuracy | 未接入 | AccuracyPower, ApplyPowerAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Accuracy.java` |
| green | Acrobatics | 未接入 | DiscardAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Acrobatics.java` |
| green | Adrenaline | 未接入 | DrawCardAction, GainEnergyAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Adrenaline.java` |
| green | After Image | 已接入基础与升级版 | AfterImagePower, ApplyPowerAction | `src/main/java/com/megacrit/cardcrawl/cards/green/AfterImage.java` |
| green | Venomology | 未接入 | ObtainPotionAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Alchemize.java` |
| green | All Out Attack | 未接入 | DamageAllEnemiesAction, DiscardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/AllOutAttack.java` |
| green | A Thousand Cuts | 已接入基础与升级版 | ApplyPowerAction, ThousandCutsPower | `src/main/java/com/megacrit/cardcrawl/cards/green/AThousandCuts.java` |
| green | Backflip | 已接入基础与升级版 | DrawCardAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Backflip.java` |
| green | Backstab | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Backstab.java` |
| green | Bane | 已接入基础与升级版 | BaneAction, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Bane.java` |
| green | Blade Dance | 未接入 | MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/green/BladeDance.java` |
| green | Blur | 已接入基础与升级版 | ApplyPowerAction, BlurPower, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Blur.java` |
| green | Bouncing Flask | 未接入 | BouncingFlaskAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/BouncingFlask.java` |
| green | Bullet Time | 未接入 | ApplyBulletTimeAction, ApplyPowerAction, NoDrawPower | `src/main/java/com/megacrit/cardcrawl/cards/green/BulletTime.java` |
| green | Burst | 未接入 | ApplyPowerAction, BurstPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Burst.java` |
| green | Calculated Gamble | 未接入 | CalculatedGambleAction | `src/main/java/com/megacrit/cardcrawl/cards/green/CalculatedGamble.java` |
| green | Caltrops | 已接入基础与升级版 | ApplyPowerAction, ThornsPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Caltrops.java` |
| green | Catalyst | 已接入基础与升级版 | DoublePoisonAction, TriplePoisonAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Catalyst.java` |
| green | Choke | 未接入 | ApplyPowerAction, ChokePower, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Choke.java` |
| green | Cloak And Dagger | 未接入 | GainBlockAction, MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/green/CloakAndDagger.java` |
| green | Concentrate | 未接入 | DiscardAction, GainEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Concentrate.java` |
| green | Corpse Explosion | 未接入 | ApplyPowerAction, CorpseExplosionPower, PoisonPower | `src/main/java/com/megacrit/cardcrawl/cards/green/CorpseExplosion.java` |
| green | Crippling Poison | 已接入基础与升级版 | ApplyPowerAction, PoisonPower, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/green/CripplingPoison.java` |
| green | Dagger Spray | 已接入基础与升级版 | DamageAllEnemiesAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/DaggerSpray.java` |
| green | Dagger Throw | 未接入 | DamageAction, DiscardAction, DrawCardAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/DaggerThrow.java` |
| green | Dash | 已接入基础与升级版 | DamageAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Dash.java` |
| green | Deadly Poison | 已接入基础与升级版 | ApplyPowerAction, PoisonPower | `src/main/java/com/megacrit/cardcrawl/cards/green/DeadlyPoison.java` |
| green | Defend_G | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Defend_Green.java` |
| green | Deflect | 未接入 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Deflect.java` |
| green | Die Die Die | 未接入 | DamageAllEnemiesAction, ShakeScreenAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/DieDieDie.java` |
| green | Distraction | 未接入 | MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Distraction.java` |
| green | Dodge and Roll | 已接入基础与升级版 | ApplyPowerAction, GainBlockAction, NextTurnBlockPower | `src/main/java/com/megacrit/cardcrawl/cards/green/DodgeAndRoll.java` |
| green | Doppelganger | 未接入 | DoppelgangerAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Doppelganger.java` |
| green | Endless Agony | 未接入 | DamageAction, MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/green/EndlessAgony.java` |
| green | Envenom | 已接入基础与升级版 | ApplyPowerAction, EnvenomPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Envenom.java` |
| green | Escape Plan | 未接入 | DrawCardAction, EscapePlanAction | `src/main/java/com/megacrit/cardcrawl/cards/green/EscapePlan.java` |
| green | Eviscerate | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Eviscerate.java` |
| green | Expertise | 未接入 | ExpertiseAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Expertise.java` |
| green | Finisher | 未接入 | DamagePerAttackPlayedAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Finisher.java` |
| green | Flechettes | 未接入 | FlechetteAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Flechettes.java` |
| green | Flying Knee | 已接入基础与升级版 | ApplyPowerAction, DamageAction, EnergizedPower | `src/main/java/com/megacrit/cardcrawl/cards/green/FlyingKnee.java` |
| green | Footwork | 已接入基础与升级版 | ApplyPowerAction, DexterityPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Footwork.java` |
| green | Glass Knife | 未接入 | DamageAction, ModifyDamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/GlassKnife.java` |
| green | Grand Finale | 未接入 | DamageAllEnemiesAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/GrandFinale.java` |
| green | Heel Hook | 未接入 | HeelHookAction | `src/main/java/com/megacrit/cardcrawl/cards/green/HeelHook.java` |
| green | Infinite Blades | 未接入 | ApplyPowerAction, InfiniteBladesPower | `src/main/java/com/megacrit/cardcrawl/cards/green/InfiniteBlades.java` |
| green | Leg Sweep | 已接入基础与升级版 | ApplyPowerAction, GainBlockAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/green/LegSweep.java` |
| green | Malaise | 未接入 | MalaiseAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Malaise.java` |
| green | Masterful Stab | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/MasterfulStab.java` |
| green | Neutralize | 已接入基础与升级版 | ApplyPowerAction, DamageAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Neutralize.java` |
| green | Night Terror | 未接入 | NightmareAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Nightmare.java` |
| green | Noxious Fumes | 已接入基础与升级版 | ApplyPowerAction, NoxiousFumesPower | `src/main/java/com/megacrit/cardcrawl/cards/green/NoxiousFumes.java` |
| green | Outmaneuver | 已接入基础与升级版 | ApplyPowerAction, EnergizedPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Outmaneuver.java` |
| green | Phantasmal Killer | 未接入 | ApplyPowerAction, PhantasmalPower | `src/main/java/com/megacrit/cardcrawl/cards/green/PhantasmalKiller.java` |
| green | PiercingWail | 已接入基础与升级版 | ApplyPowerAction, GainStrengthPower, SFXAction, StrengthPower, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/green/PiercingWail.java` |
| green | Poisoned Stab | 已接入基础与升级版 | ApplyPowerAction, DamageAction, PoisonPower | `src/main/java/com/megacrit/cardcrawl/cards/green/PoisonedStab.java` |
| green | Predator | 已接入基础与升级版 | ApplyPowerAction, DamageAction, DrawCardNextTurnPower | `src/main/java/com/megacrit/cardcrawl/cards/green/Predator.java` |
| green | Prepared | 未接入 | DiscardAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Prepared.java` |
| green | Quick Slash | 已接入基础与升级版 | DamageAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/QuickSlash.java` |
| green | Reflex | 未接入 | DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Reflex.java` |
| green | Riddle With Holes | 未接入 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/RiddleWithHoles.java` |
| green | Setup | 未接入 | SetupAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Setup.java` |
| green | Skewer | 未接入 | SkewerAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Skewer.java` |
| green | Slice | 已接入基础与升级版 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Slice.java` |
| green | Underhanded Strike | 未接入 | DamageAction, GainEnergyIfDiscardAction | `src/main/java/com/megacrit/cardcrawl/cards/green/SneakyStrike.java` |
| green | Storm of Steel | 未接入 | BladeFuryAction | `src/main/java/com/megacrit/cardcrawl/cards/green/StormOfSteel.java` |
| green | Strike_G | 已接入基础与升级版 | DamageAction, DamageAllEnemiesAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Strike_Green.java` |
| green | Sucker Punch | 已接入基础与升级版 | ApplyPowerAction, DamageAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/green/SuckerPunch.java` |
| green | Survivor | 未接入 | DiscardAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Survivor.java` |
| green | Tactician | 未接入 | GainEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Tactician.java` |
| green | Terror | 未接入 | ApplyPowerAction, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/green/Terror.java` |
| green | Tools of the Trade | 未接入 | ApplyPowerAction, ToolsOfTheTradePower | `src/main/java/com/megacrit/cardcrawl/cards/green/ToolsOfTheTrade.java` |
| green | Unload | 未接入 | DamageAction, UnloadAction | `src/main/java/com/megacrit/cardcrawl/cards/green/Unload.java` |
| green | Well Laid Plans | 未接入 | ApplyPowerAction, RetainCardPower | `src/main/java/com/megacrit/cardcrawl/cards/green/WellLaidPlans.java` |
| green | Wraith Form v2 | 已接入基础与升级版 | ApplyPowerAction, IntangiblePlayerPower, WraithFormPower | `src/main/java/com/megacrit/cardcrawl/cards/green/WraithForm.java` |
| blue | Aggregate | 已接入基础与升级版 | AggregateEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Aggregate.java` |
| blue | All For One | 未接入 | AllCostToHandAction, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/AllForOne.java` |
| blue | Amplify | 未接入 | AmplifyPower, ApplyPowerAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Amplify.java` |
| blue | Auto Shields | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/AutoShields.java` |
| blue | Ball Lightning | 已接入基础与升级版 | ChannelAction, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/BallLightning.java` |
| blue | Barrage | 未接入 | BarrageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Barrage.java` |
| blue | Beam Cell | 已接入基础与升级版 | ApplyPowerAction, DamageAction, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/blue/BeamCell.java` |
| blue | Biased Cognition | 已接入基础与升级版 | ApplyPowerAction, BiasPower, FocusPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/BiasedCognition.java` |
| blue | Blizzard | 未接入 | DamageAllEnemiesAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Blizzard.java` |
| blue | BootSequence | 未接入 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/BootSequence.java` |
| blue | Buffer | 已接入基础与升级版 | ApplyPowerAction, BufferPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Buffer.java` |
| blue | Capacitor | 已接入基础与升级版 | IncreaseMaxOrbAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Capacitor.java` |
| blue | Chaos | 已接入基础与升级版 | ChannelAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Chaos.java` |
| blue | Chill | 已接入基础与升级版 | ChannelAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Chill.java` |
| blue | Gash | 未接入 | DamageAction, GashAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Claw.java` |
| blue | Cold Snap | 已接入基础与升级版 | ChannelAction, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/ColdSnap.java` |
| blue | Compile Driver | 已接入基础与升级版 | CompileDriverAction, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/CompileDriver.java` |
| blue | Conserve Battery | 已接入基础与升级版 | ApplyPowerAction, EnergizedBluePower, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/ConserveBattery.java` |
| blue | Consume | 已接入基础与升级版 | ApplyPowerAction, DecreaseMaxOrbAction, FocusPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Consume.java` |
| blue | Coolheaded | 已接入基础与升级版 | ChannelAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Coolheaded.java` |
| blue | Core Surge | 未接入 | ApplyPowerAction, ArtifactPower, DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/CoreSurge.java` |
| blue | Creative AI | 未接入 | ApplyPowerAction, CreativeAIPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/CreativeAI.java` |
| blue | Darkness | 已接入基础与升级版 | ChannelAction, DarkImpulseAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Darkness.java` |
| blue | Defend_B | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Defend_Blue.java` |
| blue | Defragment | 已接入基础与升级版 | ApplyPowerAction, FocusPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Defragment.java` |
| blue | Doom and Gloom | 已接入基础与升级版 | ChannelAction, DamageAllEnemiesAction, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/DoomAndGloom.java` |
| blue | Double Energy | 已接入基础与升级版 | DoubleEnergyAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/DoubleEnergy.java` |
| blue | Dualcast | 已接入基础与升级版 | AnimateOrbAction, EvokeOrbAction, EvokeWithoutRemovingOrbAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Dualcast.java` |
| blue | Echo Form | 未接入 | ApplyPowerAction, EchoPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/EchoForm.java` |
| blue | Electrodynamics | 已接入基础与升级版 | ApplyPowerAction, ChannelAction, ElectroPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Electrodynamics.java` |
| blue | Undo | 未接入 | ApplyPowerAction, EquilibriumPower, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Equilibrium.java` |
| blue | Fission | 已接入基础与升级版 | FissionAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Fission.java` |
| blue | Force Field | 未接入 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/ForceField.java` |
| blue | FTL | 未接入 | FTLAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/FTL.java` |
| blue | Fusion | 已接入基础与升级版 | ChannelAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Fusion.java` |
| blue | Genetic Algorithm | 未接入 | GainBlockAction, IncreaseMiscAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/GeneticAlgorithm.java` |
| blue | Glacier | 已接入基础与升级版 | ChannelAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Glacier.java` |
| blue | Go for the Eyes | 未接入 | DamageAction, ForTheEyesAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/GoForTheEyes.java` |
| blue | Heatsinks | 已接入基础与升级版 | ApplyPowerAction, HeatsinkPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Heatsinks.java` |
| blue | Hello World | 未接入 | ApplyPowerAction, HelloPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/HelloWorld.java` |
| blue | Hologram | 未接入 | BetterDiscardPileToHandAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Hologram.java` |
| blue | Hyperbeam | 未接入 | ApplyPowerAction, DamageAllEnemiesAction, FocusPower, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Hyperbeam.java` |
| blue | Impulse | 未接入 | ImpulseAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Impulse.java` |
| blue | Leap | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Leap.java` |
| blue | Lockon | 未接入 | ApplyPowerAction, DamageAction, LockOnPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/LockOn.java` |
| blue | Loop | 已接入基础与升级版 | ApplyPowerAction, LoopPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Loop.java` |
| blue | Machine Learning | 已接入基础与升级版 | ApplyPowerAction, DrawPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/MachineLearning.java` |
| blue | Melter | 已接入基础与升级版 | DamageAction, RemoveAllBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Melter.java` |
| blue | Meteor Strike | 未接入 | ChannelAction, DamageAction, VFXAction, WaitAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/MeteorStrike.java` |
| blue | Multi-Cast | 未接入 | MulticastAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/MultiCast.java` |
| blue | Steam Power | 未接入 | DrawCardAction, MakeTempCardInDiscardAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Overclock.java` |
| blue | Rainbow | 未接入 | ChannelAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Rainbow.java` |
| blue | Reboot | 未接入 | DrawCardAction, ShuffleAction, ShuffleAllAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Reboot.java` |
| blue | Rebound | 未接入 | ApplyPowerAction, DamageAction, ReboundPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Rebound.java` |
| blue | Redo | 已接入基础与升级版 | RedoAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Recursion.java` |
| blue | Recycle | 未接入 | RecycleAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Recycle.java` |
| blue | Reinforced Body | 未接入 | ReinforcedBodyAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/ReinforcedBody.java` |
| blue | Reprogram | 未接入 | ApplyPowerAction, DexterityPower, FocusPower, StrengthPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Reprogram.java` |
| blue | Rip and Tear | 已接入基础与升级版 | NewRipAndTearAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/RipAndTear.java` |
| blue | Scrape | 未接入 | DamageAction, DrawCardAction, ScrapeFollowUpAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Scrape.java` |
| blue | Seek | 未接入 | BetterDrawPileToHandAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Seek.java` |
| blue | Self Repair | 未接入 | ApplyPowerAction, RepairPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/SelfRepair.java` |
| blue | Skim | 已接入基础与升级版 | DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Skim.java` |
| blue | Stack | 未接入 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Stack.java` |
| blue | Static Discharge | 已接入基础与升级版 | ApplyPowerAction, StaticDischargePower | `src/main/java/com/megacrit/cardcrawl/cards/blue/StaticDischarge.java` |
| blue | Steam | 未接入 | GainBlockAction, ModifyBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/SteamBarrier.java` |
| blue | Storm | 已接入基础与升级版 | ApplyPowerAction, StormPower | `src/main/java/com/megacrit/cardcrawl/cards/blue/Storm.java` |
| blue | Streamline | 未接入 | DamageAction, ReduceCostAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Streamline.java` |
| blue | Strike_B | 已接入基础与升级版 | DamageAction, DamageAllEnemiesAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Strike_Blue.java` |
| blue | Sunder | 未接入 | SunderAction, VFXAction, WaitAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Sunder.java` |
| blue | Sweeping Beam | 已接入基础与升级版 | DamageAllEnemiesAction, DrawCardAction, SFXAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/SweepingBeam.java` |
| blue | Tempest | 未接入 | TempestAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Tempest.java` |
| blue | Thunder Strike | 未接入 | NewThunderStrikeAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/ThunderStrike.java` |
| blue | Turbo | 未接入 | GainEnergyAction, MakeTempCardInDiscardAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Turbo.java` |
| blue | White Noise | 未接入 | MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/WhiteNoise.java` |
| blue | Zap | 已接入基础与升级版 | ChannelAction | `src/main/java/com/megacrit/cardcrawl/cards/blue/Zap.java` |
| colorless | Apotheosis | 已接入基础与升级版 | ApotheosisAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Apotheosis.java` |
| colorless | Ghostly | 未接入 | ApplyPowerAction, IntangiblePlayerPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Apparition.java` |
| colorless | Bandage Up | 已接入基础与升级版 | HealAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/BandageUp.java` |
| colorless | Bite | 未接入 | DamageAction, HealAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Bite.java` |
| colorless | Blind | 已接入基础与升级版 | ApplyPowerAction, WeakPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Blind.java` |
| colorless | Chrysalis | 未接入 | MakeTempCardInDrawPileAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Chrysalis.java` |
| colorless | Dark Shackles | 已接入基础与升级版 | ApplyPowerAction, GainStrengthPower, StrengthPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/DarkShackles.java` |
| colorless | Deep Breath | 已接入基础与升级版 | DrawCardAction, EmptyDeckShuffleAction, ShuffleAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/DeepBreath.java` |
| colorless | Discovery | 未接入 | DiscoveryAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Discovery.java` |
| colorless | Dramatic Entrance | 已接入基础与升级版 | DamageAllEnemiesAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/DramaticEntrance.java` |
| colorless | Enlightenment | 未接入 | EnlightenmentAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Enlightenment.java` |
| colorless | Finesse | 已接入基础与升级版 | DrawCardAction, GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Finesse.java` |
| colorless | Flash of Steel | 已接入基础与升级版 | DamageAction, DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/FlashOfSteel.java` |
| colorless | Forethought | 未接入 | ForethoughtAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Forethought.java` |
| colorless | Good Instincts | 已接入基础与升级版 | GainBlockAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/GoodInstincts.java` |
| colorless | HandOfGreed | 未接入 | GreedAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/HandOfGreed.java` |
| colorless | Impatience | 已接入基础与升级版 | ConditionalDrawAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Impatience.java` |
| colorless | Jack Of All Trades | 未接入 | MakeTempCardInHandAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/JackOfAllTrades.java` |
| colorless | J.A.X. | 未接入 | ApplyPowerAction, LoseHPAction, StrengthPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/JAX.java` |
| colorless | Madness | 未接入 | MadnessAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Madness.java` |
| colorless | Magnetism | 未接入 | ApplyPowerAction, MagnetismPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Magnetism.java` |
| colorless | Master of Strategy | 已接入基础与升级版 | DrawCardAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/MasterOfStrategy.java` |
| colorless | Mayhem | 未接入 | ApplyPowerAction, MayhemPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Mayhem.java` |
| colorless | Metamorphosis | 未接入 | MakeTempCardInDrawPileAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Metamorphosis.java` |
| colorless | Mind Blast | 未接入 | DamageAction, VFXAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/MindBlast.java` |
| colorless | Panacea | 已接入基础与升级版 | ApplyPowerAction, ArtifactPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Panacea.java` |
| colorless | Panache | 已接入基础与升级版 | ApplyPowerAction, PanachePower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Panache.java` |
| colorless | PanicButton | 未接入 | ApplyPowerAction, GainBlockAction, NoBlockPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/PanicButton.java` |
| colorless | Purity | 未接入 | ExhaustAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Purity.java` |
| colorless | RitualDagger | 未接入 | RitualDaggerAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/RitualDagger.java` |
| colorless | Sadistic Nature | 已接入基础与升级版 | ApplyPowerAction, SadisticPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/SadisticNature.java` |
| colorless | Secret Technique | 未接入 | SkillFromDeckToHandAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/SecretTechnique.java` |
| colorless | Secret Weapon | 未接入 | AttackFromDeckToHandAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/SecretWeapon.java` |
| colorless | Swift Strike | 已接入基础与升级版 | DamageAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/SwiftStrike.java` |
| colorless | The Bomb | 未接入 | ApplyPowerAction, TheBombPower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/TheBomb.java` |
| colorless | Thinking Ahead | 未接入 | DrawCardAction, PutOnDeckAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/ThinkingAhead.java` |
| colorless | Transmutation | 未接入 | TransmutationAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Transmutation.java` |
| colorless | Trip | 已接入基础与升级版 | ApplyPowerAction, VulnerablePower | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Trip.java` |
| colorless | Violence | 未接入 | DrawPileToHandAction | `src/main/java/com/megacrit/cardcrawl/cards/colorless/Violence.java` |
