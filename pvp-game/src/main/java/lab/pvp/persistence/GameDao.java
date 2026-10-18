package lab.pvp.persistence;

import java.util.List;
import lab.pvp.model.Hero;

public interface GameDao extends AutoCloseable {
    SavedDeck saveDeck(String name, Hero hero, List<String> cards);
    List<SavedDeck> decks(Hero hero);
    void saveMatch(MatchRecord match, String log);
    List<MatchRecord> history();
    void saveProfile(String name, Hero preferredHero);
    List<String> profiles();
    @Override void close();
}
