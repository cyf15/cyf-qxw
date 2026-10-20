package lab.pvp.view.cui;

import lab.pvp.controller.GameController;
import lab.pvp.model.*;
import lab.pvp.persistence.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Text view of the same application controller used by libGDX. */
public final class ConsoleLauncher {
    private final Scanner input = new Scanner(System.in);
    private final GameController controller;
    private ConsoleLauncher(GameController controller) { this.controller = controller; }
    public static void main(String[] args) {
        try (GameController controller = new GameController()) {
            new ConsoleLauncher(controller).run();
        } catch (RuntimeException e) { System.err.println("Cannot start: " + e.getMessage()); }
    }
    private void run() {
        System.out.println("SPIRE HOTSEAT | local 1v1 | three classes | any deck size");
        while (true) {
            System.out.println("\n1 New match   2 Match history   3 Import deck JSON   4 Starter demo   5 Random match   0 Quit");
            String command = line();
            if (command == null || command.equals("0")) return;
            try {
                switch (command) {
                    case "1" -> setup();
                    case "2" -> controller.history().forEach(System.out::println);
                    case "3" -> { System.out.println("Path:"); String p = line(); if (p != null) System.out.println(controller.importDeck(Path.of(p))); }
                    case "4" -> quickDemo();
                    case "5" -> randomMatch();
                    default -> System.out.println("Choose 0, 1, 2, 3, 4 or 5.");
                }
            } catch (RuntimeException e) { System.out.println("Could not complete action: " + e.getMessage()); }
        }
    }
    private void setup() {
        Hero[] heroes = new Hero[2];
        List<List<String>> decks = new ArrayList<>();
        for (int player = 0; player < 2; player++) {
            System.out.println("\nPLAYER " + (player + 1) + " | 1 Ironclad (80 HP)  2 Silent (65 HP)  3 Defect (70 HP)  0 Back");
            String raw = line();
            if (raw == null || raw.equals("0")) return;
            int chosen = parse(raw, 1, 3) - 1;
            heroes[player] = Hero.values()[chosen];
            List<String> deck = build(heroes[player]);
            if (deck == null) return;
            decks.add(deck);
        }
        controller.startMatch(heroes[0], decks.get(0), heroes[1], decks.get(1));
        battle();
    }
    private List<String> build(Hero hero) {
        List<String> chosen = new ArrayList<>();
        List<CardDefinition> pool = controller.cards().pool(hero);
        System.out.println("Class pool + colorless cards. Enter 'pool' to list cards, or 'random' for a ready deck.");
        while (true) {
            System.out.println("\nDeck " + chosen.size() + " cards: " + chosen.stream().map(id -> controller.cards().get(id).name()).toList());
            System.out.println("Commands: pool | add NUMBER | remove NUMBER | starter | random | clear | save NAME | load | export PATH | ready | back");
            String raw = line();
            if (raw == null || raw.equalsIgnoreCase("back")) return null;
            try {
                String[] parts = raw.split("\\s+", 2);
                switch (parts[0].toLowerCase(Locale.ROOT)) {
                    case "pool" -> { for (int i = 0; i < pool.size(); i++) printCard(i + 1, pool.get(i)); }
                    case "add" -> chosen.add(pool.get(parse(arg(parts), 1, pool.size()) - 1).id());
                    case "remove" -> chosen.remove(parse(arg(parts), 1, chosen.size()) - 1);
                    case "starter" -> { chosen.clear(); chosen.addAll(controller.cards().starterDeck(hero)); }
                    case "random" -> { chosen.clear(); chosen.addAll(randomDeck(hero)); System.out.println("Random deck selected. Review or edit it, then enter 'ready'."); }
                    case "clear" -> chosen.clear();
                    case "ready" -> { controller.validateDeck(hero, chosen); return List.copyOf(chosen); }
                    case "save" -> { controller.saveDeck(arg(parts), hero, chosen); System.out.println("Deck saved."); }
                    case "load" -> {
                        List<SavedDeck> stored = controller.loadDecks(hero);
                        if (stored.isEmpty()) { System.out.println("No saved decks for this class."); break; }
                        for (int i = 0; i < stored.size(); i++) System.out.println((i + 1) + ". " + stored.get(i).name());
                        System.out.println("Deck number:"); String number = line(); if (number == null) return null;
                        SavedDeck deck = stored.get(parse(number, 1, stored.size()) - 1);
                        controller.validateDeck(hero, deck.cards()); chosen.clear(); chosen.addAll(deck.cards());
                    }
                    case "export" -> { controller.exportDeck(new SavedDeck(0, hero.displayName() + " deck", hero, chosen), Path.of(arg(parts))); System.out.println("Deck exported."); }
                    default -> System.out.println("Unknown deck command.");
                }
            } catch (RuntimeException e) { System.out.println(e.getMessage()); }
        }
    }
    private void battle() {
        System.out.println("Play cards face-down. Damage resolves after both players finish their turns.");
        while (true) {
            BattleSnapshot state = controller.battle().snapshot();
            System.out.println("Recent actions:");
            for (int i = Math.max(0, state.log().size() - 12); i < state.log().size(); i++) System.out.println("  " + state.log().get(i));
            for (int i = 0; i < 2; i++) {
                PlayerSnapshot p = state.players().get(i);
                System.out.printf("P%d %s | HP %d/%d | block %d | energy %d | powers %s%n", i + 1, p.hero().displayName(), p.hp(), p.maxHp(), p.block(), p.energy(), p.powers());
                if (p.hero() == Hero.DEFECT) System.out.println("  Orb slots " + p.orbSlots() + " | " + p.orbs());
            }
            if (state.winner() >= 0) {
                System.out.println(state.winner() == 2 ? "DRAW" : "PLAYER " + (state.winner() + 1) + " WINS!");
                if (!controller.persistenceWarning().isBlank()) System.out.println(controller.persistenceWarning());
                return;
            }
            PlayerSnapshot active = state.players().get(state.activePlayer());
            System.out.println("Turn " + state.turnNumber() + " | PLAYER " + (state.activePlayer() + 1) + " | draw " + active.drawCount() + " / discard " + active.discardCount());
            for (int i = 0; i < active.hand().size(); i++) printCard(i + 1, active.hand().get(i));
            System.out.println("Card number to play | end | quit (abandon match)");
            String raw = line();
            if (raw == null || raw.equalsIgnoreCase("quit")) return;
            try {
                if (raw.equalsIgnoreCase("end")) {
                    controller.endTurn();
                    if (controller.battle().snapshot().winner() < 0) { System.out.print("\n".repeat(30)); System.out.println("Pass to PLAYER " + (controller.battle().snapshot().activePlayer() + 1) + ". Press Enter when ready."); if (line() == null) return; }
                } else controller.playCard(parse(raw, 1, active.hand().size()) - 1);
            } catch (RuntimeException e) { System.out.println(e.getMessage()); }
        }
    }
    private String line() { return input.hasNextLine() ? input.nextLine().trim() : null; }
    private void quickDemo() {
        System.out.println("STARTER DEMO | normal rules, starter decks, fixed seed 42; both players play manually.");
        controller.startMatch(Hero.IRONCLAD, CardFactory.starterDeck(Hero.IRONCLAD),
                Hero.SILENT, CardFactory.starterDeck(Hero.SILENT), 42);
        battle();
    }
    private void randomMatch() {
        Hero[] heroes = Hero.values();
        Hero first = heroes[ThreadLocalRandom.current().nextInt(heroes.length)];
        Hero second = heroes[ThreadLocalRandom.current().nextInt(heroes.length)];
        List<String> firstDeck = randomDeck(first);
        List<String> secondDeck = randomDeck(second);
        System.out.println("RANDOM MATCH | Player 1: " + first.displayName() + " | Player 2: " + second.displayName());
        System.out.println("Player 1 random deck:");
        printCards(firstDeck.stream().map(CardFactory::get).toList());
        System.out.println("Player 2 random deck:");
        printCards(secondDeck.stream().map(CardFactory::get).toList());
        controller.startMatch(first, firstDeck, second, secondDeck);
        battle();
    }
    private List<String> randomDeck(Hero hero) {
        List<CardDefinition> pool = new ArrayList<>(controller.cards().pool(hero));
        Collections.shuffle(pool);
        return pool.subList(0, 15).stream().map(CardDefinition::id).toList();
    }
    private static void printCards(List<CardDefinition> cards) {
        if (cards.isEmpty()) System.out.println("  (empty)");
        for (int i = 0; i < cards.size(); i++) printCard(i + 1, cards.get(i));
    }
    private static void printCard(int number, CardDefinition card) {
        String tags = (card.innate() ? " | Innate" : "") + (card.exhaust() ? " | Exhaust" : "");
        System.out.printf("%2d. %s | %d energy | %s%s%n    %s%n", number, card.name(), card.cost(), card.type(), tags, card.description());
    }
    private static String arg(String[] parts) { if (parts.length < 2 || parts[1].isBlank()) throw new IllegalArgumentException("This command needs a value."); return parts[1]; }
    private static int parse(String value, int min, int max) {
        try { int i = Integer.parseInt(value); if (i >= min && i <= max) return i; } catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException("Enter a number from " + min + " to " + max + ".");
    }
}
