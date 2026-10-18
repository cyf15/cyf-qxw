package lab.pvp.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import lab.pvp.controller.GameController;
import lab.pvp.model.Hero;
import lab.pvp.model.CardFactory;
import lab.pvp.view.cui.ConsoleLauncher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class GamePersistenceSafetyTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void malformedImportsNeverReplaceAnExistingDeck() throws Exception {
        Path data = temporary.getRoot().toPath();
        Path file = data.resolve("invalid.json");
        try (GameController controller = new GameController(data)) {
            SavedDeck original = controller.saveDeck("Keep me", Hero.IRONCLAD,
                    controller.cards().starterDeck(Hero.IRONCLAD));
            List<String> invalidDocuments = List.of(
                    "{",
                    "null",
                    "[]",
                    "{}",
                    "{\"name\":\"Keep me\",\"hero\":\"IRONCLAD\"}",
                    "{\"name\":\"Keep me\",\"hero\":\"IRONCLAD\",\"cards\":null}",
                    "{\"name\":\"Keep me\",\"hero\":\"NOT_A_HERO\",\"cards\":[]}",
                    "{\"name\":\"Keep me\",\"hero\":\"IRONCLAD\",\"cards\":[null]}",
                    "{\"name\":\"Keep me\",\"hero\":\"IRONCLAD\",\"cards\":[\"Strike_R\"]}",
                    deckJson("\"Does not exist\""),
                    deckJson("\"Strike_G\""),
                    deckJson("null"));
            for (String document : invalidDocuments) {
                Files.writeString(file, document);
                assertThrows("Invalid document must be rejected: " + document,
                        RuntimeException.class, () -> controller.importDeck(file));
                List<SavedDeck> stored = controller.loadDecks(Hero.IRONCLAD);
                assertEquals("An invalid import must not create another deck", 1, stored.size());
                assertEquals("An invalid import must not overwrite the existing deck", original, stored.get(0));
            }
        }
        try (GameController reopened = new GameController(data)) {
            assertEquals(reopened.cards().starterDeck(Hero.IRONCLAD),
                    reopened.loadDecks(Hero.IRONCLAD).get(0).cards());
        }
    }

    @Test public void sameNamedDecksForDifferentHeroesRemainIndependent() {
        Path data = temporary.getRoot().toPath();
        String name = "玩家's \"deck\"; DROP TABLE decks; --";
        try (GameController controller = new GameController(data)) {
            controller.saveDeck(name, Hero.IRONCLAD, controller.cards().starterDeck(Hero.IRONCLAD));
            controller.saveDeck(name, Hero.SILENT, controller.cards().starterDeck(Hero.SILENT));
            controller.saveDeck(name, Hero.IRONCLAD, Collections.nCopies(15, "Strike_R"));
        }
        try (GameController reopened = new GameController(data)) {
            assertEquals(1, reopened.loadDecks(Hero.IRONCLAD).size());
            assertEquals(1, reopened.loadDecks(Hero.SILENT).size());
            assertEquals(name, reopened.loadDecks(Hero.IRONCLAD).get(0).name());
            assertEquals(Collections.nCopies(15, "Strike_R"), reopened.loadDecks(Hero.IRONCLAD).get(0).cards());
            assertEquals(reopened.cards().starterDeck(Hero.SILENT), reopened.loadDecks(Hero.SILENT).get(0).cards());
            assertNotEquals(reopened.loadDecks(Hero.IRONCLAD).get(0).id(), reopened.loadDecks(Hero.SILENT).get(0).id());
        }
    }

    @Test public void historyAndCompleteUnicodeBattleLogSurviveReopening() throws Exception {
        Path database = temporary.getRoot().toPath().resolve("audit-game");
        String log = "P1 使用 Bash\nP2's HP = 0\n" + "事件记录，不应截断。\n".repeat(400);
        try (H2GameDao dao = new H2GameDao(database)) {
            dao.saveMatch(new MatchRecord(0, "2026-10-10T00:00:00Z", Hero.IRONCLAD, Hero.SILENT, 0, 7), log);
            dao.saveMatch(new MatchRecord(0, "2026-10-10T00:01:00Z", Hero.DEFECT, Hero.IRONCLAD, 2, 13), "Draw");
        }
        try (H2GameDao dao = new H2GameDao(database)) {
            List<MatchRecord> history = dao.history();
            assertEquals(2, history.size());
            assertEquals(Hero.DEFECT, history.get(0).player1());
            assertEquals(2, history.get(0).winner());
            assertEquals(13, history.get(0).turns());
            assertEquals(Hero.SILENT, history.get(1).player2());
            assertEquals(0, history.get(1).winner());
            assertEquals(7, history.get(1).turns());
        }
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:file:" + database.toAbsolutePath().toString().replace('\\', '/'), "sa", "");
                Statement statement = connection.createStatement();
                ResultSet records = statement.executeQuery("SELECT battle_log FROM matches ORDER BY id")) {
            assertTrue(records.next());
            assertEquals(log, records.getString(1));
        }
    }

    @Test public void failedExportDoesNotChangeSavedDeck() throws Exception {
        Path data = temporary.getRoot().toPath();
        try (GameController controller = new GameController(data)) {
            SavedDeck saved = controller.saveDeck("Safe", Hero.DEFECT, controller.cards().starterDeck(Hero.DEFECT));
            Path directory = Files.createDirectory(data.resolve("not-a-file"));
            assertThrows(IllegalStateException.class, () -> controller.exportDeck(saved, directory));
            assertEquals(List.of(saved), controller.loadDecks(Hero.DEFECT));
        }
    }

    @Test public void consoleStillShowsRecentEventsAfterTheBattleLogRollsOver() throws Exception {
        int defendIndex = 0;
        var cards = CardFactory.pool(Hero.IRONCLAD);
        for (int i = 0; i < cards.size(); i++) if (cards.get(i).id().equals("Defend_R")) defendIndex = i + 1;
        assertTrue(defendIndex > 0);
        String oneDeck = "1\n" + ("add " + defendIndex + "\n").repeat(15) + "ready\n";
        String commands = "1\n" + oneDeck + oneDeck + "end\n\n".repeat(170) + "quit\n0\n";
        InputStream previousInput = System.in;
        PrintStream previousOutput = System.out;
        String previousDirectory = System.getProperty("pvp.dataDir");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(capture);
            System.setProperty("pvp.dataDir", temporary.getRoot().toPath().resolve("console").toString());
            ConsoleLauncher.main(new String[0]);
        } finally {
            System.setIn(previousInput);
            System.setOut(previousOutput);
            if (previousDirectory == null) System.clearProperty("pvp.dataDir");
            else System.setProperty("pvp.dataDir", previousDirectory);
        }
        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue("A later turn must still print model events after more than 300 events", transcript.contains("  Turn 171: P1 "));
        assertTrue("Abandoning the match must return to the main menu", transcript.lastIndexOf("1 New match") > transcript.indexOf("  Turn 171: P1 "));
        assertFalse(transcript.contains("Cannot start:"));
    }

    private static String deckJson(String cardValue) {
        return "{\"name\":\"Keep me\",\"hero\":\"IRONCLAD\",\"cards\":["
                + String.join(",", Collections.nCopies(15, cardValue)) + "]}";
    }
}
