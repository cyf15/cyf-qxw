package lab.pvp.model;

final class AttackCard extends GameCard {
    AttackCard(CardDefinition definition) { super(definition); }
    @Override void resolve(BattleEngine engine, PlayerState owner, PlayerState opponent) {
        String id = definition.baseId();
        if (id.equals("Iron Wave") || id.equals("Dash")) engine.cardBlock(owner, definition.block());
        if (id.equals("Hemokinesis")) engine.loseHp(owner, definition.magic());
        if (id.equals("Melter")) opponent.block = 0;
        int strengthMultiplier = id.equals("Heavy Blade") ? definition.magic() : 1;
        engine.attack(owner, opponent, definition.damage(), strengthMultiplier);
        if (id.equals("Twin Strike") || id.equals("Dagger Spray") ||
                (id.equals("Bane") && opponent.power("Poison") > 0))
            engine.attack(owner, opponent, definition.damage(), 1);
        if (id.equals("Pummel") || id.equals("Sword Boomerang") || id.equals("Rip and Tear"))
            for (int i = 1; i < definition.magic(); i++) engine.attack(owner, opponent, definition.damage(), 1);
        if (engine.isOver()) return;
        switch (id) {
            case "Bash", "Beam Cell" -> engine.debuff(opponent, "Vulnerable", definition.magic());
            case "Neutralize" -> engine.debuff(opponent, "Weakened", definition.magic());
            case "Poisoned Stab" -> engine.debuff(opponent, "Poison", definition.magic());
            case "Anger" -> owner.discard.add(CardFactory.create(definition.id()));
            case "Pommel Strike", "Sweeping Beam" -> engine.draw(owner, definition.magic());
            case "Flash of Steel", "Quick Slash" -> engine.draw(owner, 1);
            case "Clothesline", "Sucker Punch" -> engine.debuff(opponent, "Weakened", definition.magic());
            case "Uppercut" -> {
                engine.debuff(opponent, "Weakened", definition.magic());
                engine.debuff(opponent, "Vulnerable", definition.magic());
            }
            case "Thunderclap" -> engine.debuff(opponent, "Vulnerable", 1);
            case "Dropkick" -> { if (opponent.power("Vulnerable") > 0) { engine.gainEnergy(owner, 1); engine.draw(owner, 1); } }
            case "Flying Knee" -> engine.buff(owner, "Energized", 1);
            case "Predator" -> engine.buff(owner, "Next Turn Draw", 2);
            case "Doom and Gloom" -> engine.channel(owner, opponent, OrbType.DARK);
            case "Ball Lightning" -> engine.channel(owner, opponent, OrbType.LIGHTNING);
            case "Cold Snap" -> engine.channel(owner, opponent, OrbType.FROST);
            case "Compile Driver" -> engine.draw(owner, (int) owner.orbs.stream().map(o -> o.type).distinct().count());
            default -> { }
        }
    }
}
