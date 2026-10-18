package lab.pvp.model;

/** Immutable source-card data, base cards only. Null hero identifies a colorless card. */
public record CardDefinition(String id, String name, Hero hero, CardType type, int cost,
        String description, String artPath, int damage, int block, int magic,
        boolean exhaust, boolean innate, String sourcePath, boolean ethereal) {
    public CardDefinition(String id, String name, Hero hero, CardType type, int cost,
            String description, String artPath, int damage, int block, int magic,
            boolean exhaust, boolean innate, String sourcePath) {
        this(id, name, hero, type, cost, description, artPath, damage, block, magic, exhaust, innate, sourcePath, false);
    }
}
