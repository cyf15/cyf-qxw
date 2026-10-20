package lab.pvp.controller;

import lab.pvp.model.*;
import lab.pvp.persistence.SavedDeck;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class GameControllerTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private Path data() { return temporary.getRoot().toPath(); }

    @Test public void savedDeckSurvivesClosingAndReopeningDatabase() {
        try (GameController c = new GameController(data())) { c.saveDeck("Test deck", Hero.IRONCLAD, c.cards().starterDeck(Hero.IRONCLAD)); }
        try (GameController c = new GameController(data())) {
            assertEquals(1, c.loadDecks(Hero.IRONCLAD).size());
            assertEquals(c.cards().starterDeck(Hero.IRONCLAD), c.loadDecks(Hero.IRONCLAD).get(0).cards());
            assertTrue(c.loadDecks(Hero.SILENT).isEmpty());
        }
    }
    @Test public void updatingSavedDeckDoesNotDuplicateIt() {
        try (GameController c = new GameController(data())) {
            c.saveDeck("Repeated name", Hero.IRONCLAD, c.cards().starterDeck(Hero.IRONCLAD));
            c.saveDeck("Repeated name", Hero.IRONCLAD, Collections.nCopies(15, "Strike_R"));
            assertEquals(1, c.loadDecks(Hero.IRONCLAD).size());
            assertEquals(Collections.nCopies(15, "Strike_R"), c.loadDecks(Hero.IRONCLAD).get(0).cards());
        }
    }
    @Test public void invalidDeckCannotReachDatabase() {
        try (GameController c = new GameController(data())) {
            assertThrows(IllegalArgumentException.class, () -> c.saveDeck("empty", Hero.SILENT, List.of()));
            assertThrows(IllegalArgumentException.class, () -> c.saveDeck("wrong class", Hero.SILENT, Collections.nCopies(15, "Strike_R")));
            assertThrows(IllegalArgumentException.class, () -> c.saveDeck("unknown", Hero.SILENT, Collections.nCopies(15, "not a card")));
            assertTrue(c.loadDecks(Hero.SILENT).isEmpty());
        }
    }
    @Test public void deckJsonRoundTripsAndIsValidated() throws Exception {
        Path file = data().resolve("deck.json");
        try (GameController c = new GameController(data())) {
            SavedDeck original = c.saveDeck("My deck", Hero.DEFECT, c.cards().starterDeck(Hero.DEFECT));
            c.exportDeck(original, file);
            SavedDeck loaded = c.importDeck(file);
            assertEquals(original.hero(), loaded.hero()); assertEquals(original.cards(), loaded.cards());
            Files.writeString(file, "{\"name\":\"bad\",\"hero\":\"DEFECT\",\"cards\":[]}");
            assertThrows(IllegalArgumentException.class, () -> c.importDeck(file));
        }
    }
    @Test public void completeMatchSavesResultExactlyOnceAndWritesFileLog() {
        try (GameController c = new GameController(data())) {
            List<String> deck = Collections.nCopies(15, "Strike_R");
            c.startMatch(Hero.IRONCLAD, deck, Hero.IRONCLAD, deck, 42L);
            for (int n = 0; n < 50 && c.battle().snapshot().winner() < 0; n++) {
                while (c.battle().snapshot().winner() < 0 && c.battle().snapshot().players().get(c.battle().snapshot().activePlayer()).energy() > 0) c.playCard(0);
                if (c.battle().snapshot().winner() < 0) c.endTurn();
            }
            assertTrue(c.battle().snapshot().winner() >= 0);
            assertEquals(1, c.history().size());
            assertEquals(c.battle().snapshot().winner(), c.history().get(0).winner());
            assertTrue(Files.isRegularFile(data().resolve("logs/last-match.txt")));
            assertThrows(IllegalStateException.class, c::endTurn);
            assertEquals(1, c.history().size());
            assertEquals(List.of("Player 1", "Player 2"), c.profiles());
        }
        try (GameController c = new GameController(data())) { assertEquals(1, c.history().size()); }
    }
    @Test public void failedNewMatchDoesNotReplaceCurrentMatch() {
        try (GameController c = new GameController(data())) {
            c.startMatch(Hero.IRONCLAD, c.cards().starterDeck(Hero.IRONCLAD), Hero.SILENT, c.cards().starterDeck(Hero.SILENT), 1L);
            BattleEngine previous = c.battle();
            assertThrows(IllegalArgumentException.class, () -> c.startMatch(Hero.IRONCLAD, List.of(), Hero.SILENT, List.of()));
            assertSame(previous, c.battle());
        }
    }
    @Test public void removedUpgradesCannotBeImportedOrSaved() throws Exception {
        try (GameController c = new GameController(data())) {
            SavedDeck base = c.saveDeck("Base", Hero.IRONCLAD, c.cards().starterDeck(Hero.IRONCLAD));
            List<String> upgraded = Collections.nCopies(15, "Strike_R+");
            assertThrows(IllegalArgumentException.class, () -> c.saveDeck("Old upgrades", Hero.IRONCLAD, upgraded));
            Path file = data().resolve("legacy-upgrades.json");
            Files.writeString(file, "{\"name\":\"Legacy\",\"hero\":\"IRONCLAD\",\"cards\":["
                + String.join(",", Collections.nCopies(15, "\"Strike_R+\"")) + "]}");
            assertThrows(IllegalArgumentException.class, () -> c.importDeck(file));
            assertEquals(List.of(base), c.loadDecks(Hero.IRONCLAD));
        }
    }
    @Test public void legacyDatabaseDeckIsPreservedButCannotStartAMatch() {
        try (var dao = new lab.pvp.persistence.H2GameDao(data().resolve("hotseat"))) {
            dao.saveDeck("Legacy", Hero.IRONCLAD, Collections.nCopies(15, "Strike_R+"));
        }
        try (GameController c = new GameController(data())) {
            SavedDeck legacy = c.loadDecks(Hero.IRONCLAD).get(0);
            c.startMatch(Hero.IRONCLAD, c.cards().starterDeck(Hero.IRONCLAD), Hero.SILENT, c.cards().starterDeck(Hero.SILENT), 1);
            BattleEngine previous = c.battle();
            assertThrows(IllegalArgumentException.class, () -> c.startMatch(Hero.IRONCLAD, legacy.cards(), Hero.SILENT, c.cards().starterDeck(Hero.SILENT), 1));
            assertSame(previous, c.battle());
            assertEquals(List.of(legacy), c.loadDecks(Hero.IRONCLAD));
        }
    }
}
