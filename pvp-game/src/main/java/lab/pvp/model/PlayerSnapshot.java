package lab.pvp.model;

import java.util.List;
import java.util.Map;

public record PlayerSnapshot(Hero hero, int hp, int maxHp, int block, int energy,
        List<CardDefinition> hand, int drawCount, int discardCount, Map<String, Integer> powers,
        int exhaustCount, List<String> orbs, int orbSlots) {
    public PlayerSnapshot {
        hand = List.copyOf(hand);
        powers = Map.copyOf(powers);
        orbs = List.copyOf(orbs);
    }
}
