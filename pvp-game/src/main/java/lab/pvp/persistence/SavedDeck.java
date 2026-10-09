package lab.pvp.persistence;

import java.util.List;
import lab.pvp.model.Hero;

public record SavedDeck(long id, String name, Hero hero, List<String> cards) {
    public SavedDeck { cards = List.copyOf(cards); }
}
