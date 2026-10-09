"""Produce a per-source-file inventory. Presence in the source is never counted as implementation."""
from pathlib import Path
import re

project = Path(__file__).resolve().parents[1]
source = project.parent / "MySlayTheSpire"
java = source / "src/main/java/com/megacrit/cardcrawl/cards"
supported = {row.split("\t")[12] for row in (project / "src/main/resources/catalog/cards.tsv").read_text().splitlines()}
rows = []
for color in ("red", "green", "blue", "colorless"):
    for path in sorted((java / color).glob("*.java")):
        code = re.sub(r"/\*.*?\*/", "", path.read_text(encoding="utf-8"), flags=re.S)
        match = re.search(r'super\("([^"]+)"', code)
        identifier = match.group(1) if match else path.stem
        actions = sorted(set(re.findall(r"new (\w+(?:Action|Power|Orb))\(", code)))
        relative = path.relative_to(source).as_posix()
        state = "已接入基础与升级版" if relative in supported else "未接入"
        rows.append(f"| {color} | {identifier} | {state} | {', '.join(actions)} | `{relative}` |")
report = project / "SOURCE_CARD_MATRIX.md"
report.write_text("# 一代卡牌源码与本地实现清单\n\n"
    "由 `scripts/audit-source.py` 读取实际源码文件和本地可执行目录生成。卡牌源码存在不等于已经实现。\n\n"
    f"参考源码 {len(rows)} 个文件，本地支持 {len(supported)} 个基础卡文件及对应升级版，其余 {len(rows) - len(supported)} 个仍待接入。\n\n"
    "已接入项的具体顺序和交互由 SourceParityTest 验证；原版批量规则差异见 SOURCE_NOTES.md。\n\n"
    "| 职业 | 源码 ID | 状态 | 源码使用的行动/能力 | 源码文件 |\n| --- | --- | --- | --- | --- |\n" + "\n".join(rows) + "\n", encoding="utf-8")
print(f"Audited {len(rows)} source files; {len(supported)} supported, {len(rows) - len(supported)} remaining.")
