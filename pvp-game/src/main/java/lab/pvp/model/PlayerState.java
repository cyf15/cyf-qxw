package lab.pvp.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mutable engine-owned state, deliberately inaccessible to either View. */
final class PlayerState {
    final Hero hero;
    int hp, block, energy, orbSlots;
    final List<GameCard> draw = new ArrayList<>();
    final List<GameCard> hand = new ArrayList<>();
    final List<GameCard> discard = new ArrayList<>();
    final List<GameCard> exhaust = new ArrayList<>();
    final Map<String, Integer> powers = new LinkedHashMap<>();
    final List<OrbState> orbs = new ArrayList<>();
    PlayerState(Hero hero, List<String> deck) {
        this.hero = hero;
        hp = hero.maxHp();
        orbSlots = hero == Hero.DEFECT ? 3 : 0;
        deck.forEach(id -> draw.add(CardFactory.create(id)));
    }
    int power(String id) { return powers.getOrDefault(id, 0); }
    void setPower(String id, int value) { if (value == 0) powers.remove(id); else powers.put(id, value); }
    PlayerSnapshot snapshot() {
        return new PlayerSnapshot(hero, hp, hero.maxHp(), block, energy,
                hand.stream().map(GameCard::definition).toList(), draw.size(), discard.size(), powers,
                exhaust.size(), orbs.stream().map(o -> o.label(power("Focus"))).toList(), orbSlots);
    }
}
