package lab.pvp.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lab.pvp.model.*;
import lab.pvp.persistence.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Shared application controller for the console and desktop views. */
public final class GameController implements AutoCloseable {
    private final CardFactory factory = new CardFactory();
    private final GameDao dao;
    private final Path dataDirectory;
    private final Gson json = new GsonBuilder().setPrettyPrinting().create();
    private BattleEngine battle;
    private boolean matchRecorded;
    private String persistenceWarning = "";

    public GameController() { this(Path.of(System.getProperty("pvp.dataDir", "data"))); }
    public GameController(Path dataDirectory) {
        this.dataDirectory = dataDirectory.toAbsolutePath().normalize();
        this.dao = new H2GameDao(this.dataDirectory.resolve("hotseat"));
    }
    public CardFactory cards() { return factory; }
    public BattleEngine battle() {
        if (battle == null) throw new IllegalStateException("Start a match first.");
        return battle;
    }
    public void startMatch(Hero first, List<String> firstDeck, Hero second, List<String> secondDeck) {
        startMatch(first, firstDeck, second, secondDeck, System.nanoTime());
    }
    public void startMatch(Hero first, List<String> firstDeck, Hero second, List<String> secondDeck, long seed) {
        validateDeck(first, firstDeck); validateDeck(second, secondDeck);
        BattleEngine next = new BattleEngine(first, firstDeck, second, secondDeck, seed);
        dao.saveProfile("Player 1", first); dao.saveProfile("Player 2", second);
        battle = next;
        matchRecorded = false;
        persistenceWarning = "";
        battle.addObserver(this::recordFinishedMatch);
    }
    public void queueCard(int index) { battle().queueCard(index); }
    public void endTurn() { battle().endTurn(); }
    public String persistenceWarning() { return persistenceWarning; }

    public SavedDeck saveDeck(String name, Hero hero, List<String> cardIds) {
        String clean = Objects.requireNonNull(name, "Deck name is required").trim();
        if (clean.isEmpty() || clean.length() > 120) throw new IllegalArgumentException("Use a deck name between 1 and 120 characters.");
        validateDeck(hero, cardIds);
        return dao.saveDeck(clean, hero, cardIds);
    }
    public List<SavedDeck> loadDecks(Hero hero) { return dao.decks(hero); }
    public List<MatchRecord> history() { return dao.history(); }
    public List<String> profiles() { return dao.profiles(); }

    public void validateDeck(Hero hero, List<String> cardIds) {
        Objects.requireNonNull(hero, "Choose a class.");
        if (cardIds == null || cardIds.size() != 15) throw new IllegalArgumentException("A match deck must contain exactly 15 cards.");
        for (String id : cardIds) {
            CardDefinition card = factory.get(id);
            if (card == null) throw new IllegalArgumentException("Unknown card: " + id);
            if (card.hero() != null && card.hero() != hero) throw new IllegalArgumentException(card.name() + " belongs to another class.");
        }
    }

    public void exportDeck(SavedDeck deck, Path file) {
        validateDeck(deck.hero(), deck.cards());
        try {
            Path target = file.toAbsolutePath();
            Files.createDirectories(target.getParent());
            Files.writeString(target, json.toJson(deck), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new IllegalStateException("Could not export deck: " + e.getMessage(), e); }
    }
    public SavedDeck importDeck(Path file) {
        try {
            DeckTransfer deck = json.fromJson(Files.readString(file, StandardCharsets.UTF_8), DeckTransfer.class);
            if (deck == null || deck.name() == null || deck.hero() == null || deck.cards() == null || deck.cards().contains(null)) throw new IllegalArgumentException("Invalid deck file.");
            return saveDeck(deck.name(), deck.hero(), deck.cards());
        } catch (IOException e) { throw new IllegalStateException("Could not read deck: " + e.getMessage(), e); }
        catch (com.google.gson.JsonParseException e) { throw new IllegalArgumentException("Invalid deck JSON.", e); }
    }
    private record DeckTransfer(String name, Hero hero, List<String> cards) {}

    private void recordFinishedMatch(BattleSnapshot state) {
        if (state.winner() < 0 || matchRecorded) return;
        String log = String.join(System.lineSeparator(), state.log());
        try {
            dao.saveMatch(new MatchRecord(0, Instant.now().toString(), state.players().get(0).hero(), state.players().get(1).hero(), state.winner(), state.turnNumber()), log);
            matchRecorded = true;
        } catch (RuntimeException e) {
            persistenceWarning = e.getMessage();
        }
        try {
            Files.createDirectories(dataDirectory.resolve("logs"));
            Files.writeString(dataDirectory.resolve("logs/last-match.txt"), log, StandardCharsets.UTF_8);
        } catch (IOException e) {
            persistenceWarning += " Match log could not be written: " + e.getMessage();
        }
    }
    @Override public void close() { dao.close(); }
}
