package lab.pvp.model;

final class OrbState {
    final OrbType type;
    int darkDamage = 6;
    OrbState(OrbType type) { this.type = type; }
    String label(int focus) {
        return switch (type) {
            case LIGHTNING -> "Lightning " + Math.max(0, 3 + focus) + "/" + Math.max(0, 8 + focus);
            case FROST -> "Frost " + Math.max(0, 2 + focus) + "/" + Math.max(0, 5 + focus);
            case DARK -> "Dark +" + Math.max(0, 6 + focus) + "/" + darkDamage;
            case PLASMA -> "Plasma 1/2";
        };
    }
}
