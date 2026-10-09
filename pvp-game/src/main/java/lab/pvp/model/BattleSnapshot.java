package lab.pvp.model;

import java.util.List;

public record BattleSnapshot(int turnNumber, int activePlayer, int winner,
        List<PlayerSnapshot> players, List<CardDefinition> queue, List<String> log) {
    public BattleSnapshot {
        players = List.copyOf(players);
        queue = List.copyOf(queue);
        log = List.copyOf(log);
    }
}
