package lab.pvp.model;

/** Rendering consumes actual resolved actions; no damage or power value is guessed by the view. */
public record CombatEvent(String kind, int player, int source, String name, int amount,
                          int blocked, CardDefinition card, PlayerSnapshot state) { }
