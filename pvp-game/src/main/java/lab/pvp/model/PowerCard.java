package lab.pvp.model;

final class PowerCard extends GameCard {
    PowerCard(CardDefinition definition) { super(definition); }
    @Override void resolve(BattleEngine engine, PlayerState owner, PlayerState opponent) {
        switch (definition.baseId()) {
            case "Inflame" -> engine.buff(owner, "Strength", definition.magic());
            case "Metallicize" -> engine.buff(owner, "Metallicize", definition.magic());
            case "Footwork" -> engine.buff(owner, "Dexterity", definition.magic());
            case "Noxious Fumes" -> engine.buff(owner, "Noxious Fumes", definition.magic());
            case "Defragment" -> engine.buff(owner, "Focus", definition.magic());
            case "Capacitor" -> owner.orbSlots = Math.min(10, owner.orbSlots + definition.magic());
            case "Barricade" -> owner.setPower("Barricade", 1);
            case "Berserk" -> { engine.debuff(owner, "Vulnerable", definition.magic()); engine.buff(owner, "Berserk", 1); }
            case "Brutality", "Dark Embrace", "After Image", "Envenom", "Heatsinks", "Machine Learning", "Storm" -> engine.buff(owner, definition.baseId(), Math.max(1, definition.magic()));
            case "Combust" -> { engine.buff(owner, "Combust", definition.magic()); engine.buff(owner, "Combust HP Loss", 1); }
            case "Rupture", "Juggernaut", "Feel No Pain", "Demon Form", "Caltrops", "A Thousand Cuts", "Loop", "Static Discharge", "Buffer", "Panache", "Sadistic Nature" -> engine.buff(owner, definition.baseId(), definition.magic());
            case "Biased Cognition" -> { engine.buff(owner, "Focus", definition.magic()); engine.debuff(owner, "Bias", 1); }
            case "Wraith Form v2" -> { engine.buff(owner, "Intangible", definition.magic()); engine.debuff(owner, "Wraith Form", 1); }
            case "Electrodynamics" -> {
                owner.setPower("Electrodynamics", 1);
                for (int i = 0; i < definition.magic(); i++) engine.channel(owner, opponent, OrbType.LIGHTNING);
            }
            default -> throw new IllegalStateException("Unsupported power " + definition.baseId());
        }
    }
    @Override boolean leavesDeckAfterPlay() { return true; }
}
