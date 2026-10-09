package lab.pvp.model;

final class SkillCard extends GameCard {
    SkillCard(CardDefinition definition) { super(definition); }
    @Override void resolve(BattleEngine engine, PlayerState owner, PlayerState opponent) {
        if (definition.block() > 0 && (!definition.baseId().equals("Auto Shields") || owner.block == 0)) engine.cardBlock(owner, definition.block());
        switch (definition.baseId()) {
            case "Shrug It Off", "Finesse" -> engine.draw(owner, 1);
            case "Backflip" -> engine.draw(owner, 2);
            case "Battle Trance" -> {
                engine.draw(owner, definition.magic());
                engine.debuff(owner, "No Draw", 1);
            }
            case "Seeing Red" -> engine.gainEnergy(owner, 2);
            case "Bloodletting" -> {
                engine.loseHp(owner, 3);
                if (!engine.isOver()) engine.gainEnergy(owner, definition.magic());
            }
            case "Deadly Poison" -> engine.debuff(opponent, "Poison", definition.magic());
            case "Dodge and Roll" -> engine.buff(owner, "Next Turn Block", engine.modifiedBlock(owner, definition.block()));
            case "Outmaneuver" -> engine.buff(owner, "Energized", definition.upgraded() ? 3 : 2);
            case "Conserve Battery" -> engine.buff(owner, "Energized", 1);
            case "Blur" -> engine.buff(owner, "Blur", 1);
            case "Zap" -> engine.channel(owner, opponent, OrbType.LIGHTNING);
            case "Dualcast" -> engine.evokeFirst(owner, opponent, 2);
            case "Glacier" -> {
                engine.channel(owner, opponent, OrbType.FROST);
                engine.channel(owner, opponent, OrbType.FROST);
            }
            case "Chill" -> engine.channel(owner, opponent, OrbType.FROST);
            case "Darkness" -> {
                engine.channel(owner, opponent, OrbType.DARK);
                if (definition.upgraded()) for (OrbState orb : owner.orbs)
                    if (orb.type == OrbType.DARK) engine.passiveOrb(owner, opponent, orb, false);
            }
            case "Chaos" -> { for (int i = 0; i < definition.magic(); i++) engine.channel(owner, opponent, engine.randomOrb()); }
            case "Bandage Up" -> engine.heal(owner, definition.magic());
            case "Panacea" -> engine.buff(owner, "Artifact", definition.magic());
            case "Trip" -> engine.debuff(opponent, "Vulnerable", definition.magic());
            case "Blind" -> engine.debuff(opponent, "Weakened", definition.magic());
            case "Master of Strategy" -> engine.draw(owner, definition.magic());
            case "Deep Breath" -> {
                engine.shuffleDiscardIntoDraw(owner);
                engine.draw(owner, definition.magic());
            }
            case "Flex" -> { engine.buff(owner, "Strength", definition.magic()); engine.debuff(owner, "Lose Strength", definition.magic()); }
            case "Limit Break" -> { if (owner.power("Strength") < 0) engine.debuff(owner, "Strength", owner.power("Strength")); else engine.buff(owner, "Strength", owner.power("Strength")); }
            case "Shockwave" -> { engine.debuff(opponent, "Weakened", definition.magic()); engine.debuff(opponent, "Vulnerable", definition.magic()); }
            case "Intimidate" -> engine.debuff(opponent, "Weakened", definition.magic());
            case "Disarm" -> engine.debuff(opponent, "Strength", -definition.magic());
            case "Entrench" -> engine.rawBlock(owner, owner.block);
            case "Flame Barrier" -> engine.buff(owner, "Flame Barrier", definition.magic());
            case "Rage" -> engine.buff(owner, "Rage", definition.magic());
            case "Leg Sweep" -> engine.debuff(opponent, "Weakened", definition.magic());
            case "Crippling Poison" -> { engine.debuff(opponent, "Poison", definition.magic()); engine.debuff(opponent, "Weakened", 2); }
            case "Catalyst" -> engine.debuff(opponent, "Poison", opponent.power("Poison") * (definition.upgraded() ? 2 : 1));
            case "PiercingWail", "Dark Shackles" -> {
                boolean artifact = opponent.power("Artifact") > 0;
                engine.debuff(opponent, "Strength", -definition.magic());
                if (!artifact) engine.buff(opponent, "Gain Strength", definition.magic());
            }
            case "Coolheaded" -> { engine.channel(owner, opponent, OrbType.FROST); engine.draw(owner, definition.magic()); }
            case "Skim" -> engine.draw(owner, definition.magic());
            case "Fusion" -> { for (int i = 0; i < definition.magic(); i++) engine.channel(owner, opponent, OrbType.PLASMA); }
            case "Aggregate" -> engine.gainEnergy(owner, owner.draw.size() / definition.magic());
            case "Double Energy" -> engine.gainEnergy(owner, owner.energy);
            case "Redo" -> {
                if (!owner.orbs.isEmpty()) {
                    OrbState orb = owner.orbs.get(0);
                    engine.evokeFirst(owner, opponent, 1);
                    if (!engine.isOver()) owner.orbs.add(orb);
                }
            }
            case "Consume" -> {
                engine.buff(owner, "Focus", definition.magic());
                if (owner.orbSlots > 0) {
                    owner.orbSlots--;
                    if (owner.orbs.size() > owner.orbSlots) owner.orbs.remove(owner.orbs.size() - 1);
                }
            }
            case "Fission" -> {
                int count = owner.orbs.size();
                if (definition.upgraded()) while (!owner.orbs.isEmpty() && !engine.isOver()) engine.evokeFirst(owner, opponent, 1);
                else owner.orbs.clear();
                engine.gainEnergy(owner, count); engine.draw(owner, count);
            }
            case "Impatience" -> { if (owner.hand.stream().noneMatch(c -> c.definition().type() == CardType.ATTACK)) engine.draw(owner, definition.magic()); }
            case "Apotheosis" -> engine.upgradeDeck(owner);
            default -> { }
        }
    }
}
