"""Reproduce the supported base/upgrade card catalog from the checked-out STS1 reference.

Run from any directory: python scripts/extract-card-catalog.py
Only the explicitly implemented cards are exported. No art is generated.
"""
import json
import re
from pathlib import Path

PROJECT = Path(__file__).resolve().parents[1]
SOURCE = PROJECT.parent / "MySlayTheSpire"
JAVA = SOURCE / "src/main/java/com/megacrit/cardcrawl/cards"
RESOURCES = SOURCE / "src/main/resources"
SELECTED = {
    "red": "Strike_Red Defend_Red Bash Anger IronWave TwinStrike Cleave PommelStrike ShrugItOff BattleTrance Inflame Metallicize SeeingRed Bloodletting Impervious HeavyBlade".split(),
    "green": "Strike_Green Defend_Green Neutralize DaggerSpray PoisonedStab DeadlyPoison Backflip DodgeAndRoll Footwork NoxiousFumes Slice Bane Outmaneuver Blur".split(),
    "blue": "Strike_Blue Defend_Blue Zap Dualcast BallLightning ColdSnap BeamCell Leap Defragment Capacitor Glacier CompileDriver Chill Darkness Chaos ConserveBattery".split(),
    "colorless": "Finesse FlashOfSteel GoodInstincts DramaticEntrance BandageUp Panacea Trip Blind SwiftStrike MasterOfStrategy DeepBreath".split(),
}
SELECTED["red"] += "Clothesline Thunderclap Uppercut Pummel Bludgeon Carnage SwordBoomerang Hemokinesis Dropkick Flex LimitBreak Shockwave Intimidate Disarm Entrench FlameBarrier Rage Rupture Berserk Barricade Juggernaut Brutality Combust FeelNoPain DarkEmbrace DemonForm".split()
SELECTED["green"] += "Dash SuckerPunch FlyingKnee QuickSlash Predator LegSweep CripplingPoison PiercingWail Catalyst Caltrops WraithForm AfterImage Envenom AThousandCuts".split()
SELECTED["blue"] += "Melter SweepingBeam RipAndTear DoomAndGloom AutoShields Fission Fusion Coolheaded Skim Aggregate DoubleEnergy Recursion Consume Loop Electrodynamics StaticDischarge BiasedCognition Buffer Heatsinks MachineLearning Storm".split()
SELECTED["colorless"] += "DarkShackles Impatience Apotheosis Panache SadisticNature".split()
HEROES = {"red": "IRONCLAD", "green": "SILENT", "blue": "DEFECT", "colorless": ""}
strings = json.loads((RESOURCES / "localization/eng/cards.json").read_text(encoding="utf-8-sig"))
rows = []
for color, classes in SELECTED.items():
    for cls in classes:
        path = JAVA / color / (cls + ".java")
        code = re.sub(r"/\*.*?\*/", "", path.read_text(encoding="utf-8"), flags=re.S)
        match = re.search(r'super\("([^"]+)",\s*cardStrings.NAME,\s*"([^"]+)",\s*(-?\d+),\s*cardStrings.DESCRIPTION,\s*CardType\.(\w+)', code)
        if not match:
            raise ValueError(f"Unrecognized constructor: {path}")
        card_id, art, cost, card_type = match.groups()
        constructor = code[:code.index("public void use(")]
        def stat(name):
            found = re.search(r"this\." + name + r"\s*=\s*(\d+)", constructor)
            return int(found.group(1)) if found else 0
        damage, block, magic = [stat(n) for n in ("baseDamage", "baseBlock", "baseMagicNumber")]
        upgrade = code.split("public void upgrade()", 1)[1]
        upgrade = upgrade.split("public AbstractCard makeCopy", 1)[0]
        art_path = "images/1024Portraits/" + art + ".png"
        if not (RESOURCES / art_path).is_file():
            raise FileNotFoundError(art_path)
        for upgraded in (False, True):
            values = [damage, block, magic]
            price = int(cost)
            exhaust = "this.exhaust = true" in constructor
            innate = "this.isInnate = true" in constructor
            ethereal = "this.isEthereal = true" in constructor
            if upgraded:
                for index, method in enumerate(("upgradeDamage", "upgradeBlock", "upgradeMagicNumber")):
                    change = re.search(method + r"\((-?\d+)\)", upgrade)
                    if change: values[index] += int(change.group(1))
                change = re.search(r"upgradeBaseCost\((\d+)\)", upgrade)
                if change: price = int(change.group(1))
                if "this.exhaust = false" in upgrade: exhaust = False
                if "this.isInnate = true" in upgrade: innate = True
                if "this.isEthereal = false" in upgrade: ethereal = False
            text = strings[card_id].get("UPGRADE_DESCRIPTION") if upgraded and "cardStrings.UPGRADE_DESCRIPTION" in upgrade else None
            text = text or strings[card_id]["DESCRIPTION"]
            for token, number in zip(("!D!", "!B!", "!M!"), values): text = text.replace(token, str(number))
            text = re.sub(r"#[rgbyp]", "", text).replace(" NL ", " ")
            for icon in ("[R]", "[G]", "[B]", "[E]"): text = text.replace(icon, "Energy")
            rows.append([card_id + ("+" if upgraded else ""), strings[card_id]["NAME"] + ("+" if upgraded else ""), HEROES[color], card_type, str(price), text,
                         art_path, *map(str, values), str(exhaust).lower(), str(innate).lower(),
                         path.relative_to(SOURCE).as_posix(), str(ethereal).lower()])
out = PROJECT / "src/main/resources/catalog"
out.mkdir(parents=True, exist_ok=True)
(out / "cards.tsv").write_text("\n".join("\t".join(row).replace("\r", "") for row in rows) + "\n", encoding="utf-8")
print(f"Extracted {len(rows)} implemented card definitions; all portrait files exist.")

# Keep presentation metadata tied to the same source files as the executable catalog.
sounds = dict(re.findall(r'this\.map\.put\("([^"\n]+)", load\("([^"\n]+)"\)\)', (SOURCE / "src/main/java/com/megacrit/cardcrawl/audio/SoundMaster.java").read_text()))
(out / "sounds.json").write_text(json.dumps(sounds, indent=2) + "\n", encoding="utf-8")
effects = {}
for row in rows:
    if row[0].endswith("+"): continue
    code = re.sub(r"/\*.*?\*/", "", (SOURCE / row[12]).read_text(encoding="utf-8"), flags=re.S)
    use = code.split("public void use(", 1)[1].split("public void upgrade", 1)[0]
    effects[row[0]] = {"effects": re.findall(r"AttackEffect\.(\w+)", use),
                       "sounds": re.findall(r'new SFXAction\("([^"\n]+)"', use),
                       "vfx": re.findall(r"new (\w+Effect)\(", use)}
(out / "effects.json").write_text(json.dumps(effects, indent=2) + "\n", encoding="utf-8")
