package lab.pvp.model;

public enum Hero {
    IRONCLAD("Ironclad", 80), SILENT("Silent", 65), DEFECT("Defect", 70);
    private final String displayName;
    private final int maxHp;
    Hero(String displayName, int maxHp) { this.displayName = displayName; this.maxHp = maxHp; }
    public String displayName() { return displayName; }
    public int maxHp() { return maxHp; }
}
