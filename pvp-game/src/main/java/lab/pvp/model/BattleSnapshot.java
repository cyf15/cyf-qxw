package lab.pvp.model;

import java.util.List;

public record BattleSnapshot(int turnNumber, int activePlayer, int winner,
        List<PlayerSnapshot> players, List<String> log) {
    public BattleSnapshot {
        players = List.copyOf(players);
        log = List.copyOf(log);
    }
}
