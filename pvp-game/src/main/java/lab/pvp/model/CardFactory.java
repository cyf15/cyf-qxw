package lab.pvp.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Creates only implemented cards; imported source definitions are not executable code. */
public final class CardFactory {
    private static final Map<String, CardDefinition> DEFINITIONS = loadDefinitions();
    public CardFactory() { }

    public static List<CardDefinition> allCards() { return List.copyOf(DEFINITIONS.values()); }
    public static List<CardDefinition> pool(Hero hero) {
        Objects.requireNonNull(hero, "hero");
        return DEFINITIONS.values().stream().filter(c -> c.hero() == null || c.hero() == hero).toList();
    }
    public static CardDefinition get(String id) {
        CardDefinition card = DEFINITIONS.get(id);
        if (card == null) throw new IllegalArgumentException("Unknown or unsupported card: " + id);
        return card;
    }
    public static List<String> starterDeck(Hero hero) {
        Objects.requireNonNull(hero, "hero");
        String suffix = switch (hero) { case IRONCLAD -> "R"; case SILENT -> "G"; case DEFECT -> "B"; };
        List<String> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) cards.add("Strike_" + suffix);
        for (int i = 0; i < 4; i++) cards.add("Defend_" + suffix);
        cards.addAll(switch (hero) {
            case IRONCLAD -> List.of("Bash", "Iron Wave", "Twin Strike", "Shrug It Off", "Inflame", "Anger");
            case SILENT -> List.of("Neutralize", "Deadly Poison", "Backflip", "Footwork", "Dagger Spray", "Noxious Fumes");
            case DEFECT -> List.of("Zap", "Dualcast", "Cold Snap", "Ball Lightning", "Defragment", "Leap");
        });
        return List.copyOf(cards);
    }
    public static void validateDeck(Hero hero, List<String> ids) {
        if (hero == null) throw new IllegalArgumentException("Choose a hero.");
        if (ids == null || ids.size() != 15) throw new IllegalArgumentException("A match deck must contain exactly 15 cards.");
        for (String id : ids) {
            CardDefinition card = get(id);
            if (card.hero() != null && card.hero() != hero)
                throw new IllegalArgumentException(card.name() + " belongs to " + card.hero().displayName() + ".");
        }
    }
    public static GameCard create(String id) {
        CardDefinition card = get(id);
        return switch (card.type()) {
            case ATTACK -> new AttackCard(card);
            case SKILL -> new SkillCard(card);
            case POWER -> new PowerCard(card);
        };
    }
    private static Map<String, CardDefinition> loadDefinitions() {
        InputStream stream = CardFactory.class.getResourceAsStream("/catalog/cards.tsv");
        if (stream == null) throw new IllegalStateException("Missing bundled catalog/cards.tsv");
        Map<String, CardDefinition> cards = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\t", -1);
                CardDefinition card = new CardDefinition(p[0], p[1], p[2].isEmpty() ? null : Hero.valueOf(p[2]),
                        CardType.valueOf(p[3]), Integer.parseInt(p[4]), p[5], p[6],
                        Integer.parseInt(p[7]), Integer.parseInt(p[8]), Integer.parseInt(p[9]),
                        Boolean.parseBoolean(p[10]), Boolean.parseBoolean(p[11]), p[12], p.length > 13 && Boolean.parseBoolean(p[13]));
                if (cards.put(card.id(), card) != null) throw new IllegalStateException("Duplicate card " + card.id());
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException ex) {
            throw new IllegalStateException("Cannot load source card catalog", ex);
        }
        return Collections.unmodifiableMap(cards);
    }
}
