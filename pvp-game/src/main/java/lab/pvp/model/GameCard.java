package lab.pvp.model;

/** A per-deck card object. Attack, skill and power cards resolve polymorphically. */
public abstract class GameCard {
    protected CardDefinition definition;
    protected GameCard(CardDefinition definition) { this.definition = definition; }
    public final CardDefinition definition() { return definition; }
    void upgrade() {
        if (!definition.upgraded()) definition = CardFactory.get(definition.id() + "+");
    }
    abstract void resolve(BattleEngine engine, PlayerState owner, PlayerState opponent);
    boolean leavesDeckAfterPlay() { return definition.exhaust(); }
}
