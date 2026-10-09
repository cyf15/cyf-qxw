package lab.pvp.model;

/** Immutable source-card data, including upgrade variants. Null hero identifies a colorless card. */
public record CardDefinition(String id, String name, Hero hero, CardType type, int cost,
        String description, String artPath, int damage, int block, int magic,
        boolean exhaust, boolean innate, String sourcePath, boolean ethereal) {
    public CardDefinition(String id, String name, Hero hero, CardType type, int cost,
            String description, String artPath, int damage, int block, int magic,
            boolean exhaust, boolean innate, String sourcePath) {
        this(id, name, hero, type, cost, description, artPath, damage, block, magic, exhaust, innate, sourcePath, false);
    }
    public boolean upgraded() { return id.endsWith("+"); }
    public String baseId() { return upgraded() ? id.substring(0, id.length() - 1) : id; }
}
