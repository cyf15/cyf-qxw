package lab.pvp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Shared CUI/GUI engine. Source actions resolve sequentially at the user's end-turn
 * boundary; selecting a card reserves its energy immediately. No rendering dependency.
 */
public final class BattleEngine {
    private final PlayerState[] players;
    private final Random random;
    private final List<GameCard> queue = new ArrayList<>();
    private final List<String> log = new ArrayList<>();
    private final List<CombatEvent> combatEvents = new ArrayList<>();
    private final List<GameObserver> observers = new CopyOnWriteArrayList<>();
    private final boolean[] firstTurn = {true, true};
    private int activePlayer;
    private int turnNumber = 1;
    private int winner = -1;

    public BattleEngine(Hero hero1, List<String> deck1, Hero hero2, List<String> deck2, long seed) {
        CardFactory.validateDeck(hero1, deck1);
        CardFactory.validateDeck(hero2, deck2);
        random = new Random(seed);
        players = new PlayerState[] {new PlayerState(hero1, deck1), new PlayerState(hero2, deck2)};
        for (PlayerState player : players) {
            Collections.shuffle(player.draw, random);
            // Innate cards are placed above the otherwise shuffled draw pile.
            player.draw.sort((a, b) -> Boolean.compare(b.definition().innate(), a.definition().innate()));
            String relic = switch (player.hero) {
                case IRONCLAD -> "Burning Blood";
                case SILENT -> "Ring of the Snake";
                case DEFECT -> "Cracked Core";
            };
            player.setPower("Relic: " + relic, 1);
            if (player.hero == Hero.DEFECT) player.orbs.add(new OrbState(OrbType.LIGHTNING));
        }
        event("Local hotseat: P1 " + hero1.displayName() + " versus P2 " + hero2.displayName() + ".");
        event("Select cards to spend energy; effects resolve in selection order on End turn.");
        event("Starting relics active: Burning Blood, Ring of the Snake, or Cracked Core for each hero.");
        startTurn();
    }

    public synchronized BattleSnapshot snapshot() {
        return new BattleSnapshot(turnNumber, activePlayer, winner,
                List.of(players[0].snapshot(), players[1].snapshot()),
                queue.stream().map(GameCard::definition).toList(), log);
    }
    /** Original draw-pile viewer sorts cards, so inspecting it never reveals future draw order. */
    public synchronized List<CardDefinition> inspectPile(String pile) {
        PlayerState owner = players[activePlayer];
        List<GameCard> cards = switch (pile) {
            case "Draw" -> owner.draw; case "Discard" -> owner.discard; case "Exhaust" -> owner.exhaust;
            default -> throw new IllegalArgumentException("Unknown pile: " + pile);
        };
        var result = cards.stream().map(GameCard::definition);
        if (pile.equals("Draw")) result = result.sorted(java.util.Comparator.comparing(CardDefinition::name));
        return result.toList();
    }
    public synchronized List<CombatEvent> combatEvents() { return List.copyOf(combatEvents); }
    private void visual(String kind, PlayerState target, PlayerState source, String name, int amount, int blocked, CardDefinition card) {
        combatEvents.add(new CombatEvent(kind, target == players[0] ? 0 : 1, source == null ? -1 : source == players[0] ? 0 : 1, name, amount, blocked, card, target.snapshot()));
    }
    public void addObserver(GameObserver observer) { observers.add(Objects.requireNonNull(observer, "observer")); }
    public void removeObserver(GameObserver observer) { observers.remove(observer); }

    public synchronized void queueCard(int handIndex) {
        requireActive();
        PlayerState player = players[activePlayer];
        if (handIndex < 0 || handIndex >= player.hand.size()) throw new IllegalArgumentException("Choose a card in your hand.");
        GameCard card = player.hand.get(handIndex);
        if (card.definition().cost() > player.energy) throw new IllegalArgumentException("Not enough energy for " + card.definition().name() + ".");
        player.energy -= card.definition().cost();
        player.hand.remove(handIndex);
        queue.add(card);
        event("P" + (activePlayer + 1) + " queued " + card.definition().name() + " (" + card.definition().cost() + " energy).");
        notifyObservers();
    }

    public synchronized void endTurn() {
        requireActive();
        PlayerState owner = players[activePlayer];
        PlayerState opponent = players[1 - activePlayer];
        while (!queue.isEmpty() && !isOver()) {
            GameCard card = queue.remove(0);
            event("Resolve " + card.definition().name() + ".");
            visual("CARD", owner, owner, card.definition().name(), 0, 0, card.definition());
            int afterImage = owner.power("After Image"), cuts = owner.power("A Thousand Cuts"),
                rage = owner.power("Rage"), heatsinks = owner.power("Heatsinks"), storm = owner.power("Storm"), panache = owner.power("Panache");
            card.resolve(this, owner, opponent);
            rawBlock(owner, afterImage);
            damage(opponent, cuts, false);
            if (card.definition().type() == CardType.ATTACK) rawBlock(owner, rage);
            if (card.definition().type() == CardType.POWER) {
                draw(owner, heatsinks);
                for (int i = 0; i < storm; i++) channel(owner, opponent, OrbType.LIGHTNING);
            }
            if (panache > 0) {
                owner.setPower("Panache Counter", owner.power("Panache Counter") + 1);
                if (owner.power("Panache Counter") == 5) { damage(opponent, panache, false); owner.setPower("Panache Counter", 0); }
            }
            if (card.definition().exhaust()) exhaust(owner, card);
            else if (!card.leavesDeckAfterPlay()) discard(owner, card, true);
        }
        if (!isOver()) {
            // Metallicize's pre-card hook, then source orb end-turn hooks.
            rawBlock(owner, owner.power("Metallicize"));
            if (owner.power("Combust") > 0) {
                loseHp(owner, owner.power("Combust HP Loss")); damage(opponent, owner.power("Combust"), false);
            }
            for (OrbState orb : List.copyOf(owner.orbs)) passiveOrb(owner, opponent, orb, false);
            if (owner.power("Lose Strength") > 0) { debuff(owner, "Strength", -owner.power("Lose Strength")); owner.setPower("Lose Strength", 0); }
            if (owner.power("Gain Strength") > 0) { buff(owner, "Strength", owner.power("Gain Strength")); owner.setPower("Gain Strength", 0); }
            if (owner.power("Wraith Form") > 0) debuff(owner, "Dexterity", -owner.power("Wraith Form"));

        }
        // Unresolved cards have no effects after lethal damage. Finish pile cleanup only.
        owner.discard.addAll(queue);
        queue.clear();
        for (GameCard card : List.copyOf(owner.hand)) {
            owner.hand.remove(card);
            // User override: every unplayed card is discarded, including source Ethereal cards.
            discard(owner, card, false);
        }
        owner.discard.addAll(owner.hand); owner.hand.clear();
        owner.setPower("Rage", 0); owner.setPower("Panache Counter", 0);
        owner.setPower("No Draw", 0);
        if (!isOver()) {
            decrement(owner, "Weakened");
            decrement(owner, "Vulnerable");
            decrement(owner, "Frail");
            activePlayer = 1 - activePlayer;
            turnNumber++;
            startTurn();
        }
        notifyObservers();
    }

    /** Current player concedes; both views may expose this without editing model state. */
    public synchronized void concede() {
        requireActive();
        event("P" + (activePlayer + 1) + " conceded.");
        finish(1 - activePlayer);
        notifyObservers();
    }

    private void startTurn() {
        PlayerState player = players[activePlayer];
        PlayerState opponent = players[1 - activePlayer];
        if (player.power("Blur") == 0 && player.power("Barricade") == 0) player.block = 0;
        else decrement(player, "Blur"); // Preserve block before the Blur duration expires.
        player.energy = 3 + player.power("Energized") + player.power("Berserk");
        player.setPower("Flame Barrier", 0);
        decrement(player, "Intangible");
        player.setPower("Energized", 0);
        visual("TURN", player, null, "Turn", 0, 0, null);
        event("Turn " + turnNumber + ": P" + (activePlayer + 1) + " " + player.hero.displayName() + ".");
        // Powers keep insertion order, matching their source atStartOfTurn callbacks.
        for (String power : List.copyOf(player.powers.keySet())) {
            if (isOver()) return;
            switch (power) {
                case "Poison" -> {
                    damage(player, player.power("Poison"), true, null, false, "Poison");
                    decrement(player, "Poison");
                }
                case "Demon Form" -> buff(player, "Strength", player.power(power));
                case "Bias" -> debuff(player, "Focus", -player.power(power));
                case "Loop" -> {
                    if (!player.orbs.isEmpty()) for (int i = 0; i < player.power(power); i++) passiveOrb(player, opponent, player.orbs.get(0), true);
                }
                case "Noxious Fumes" -> debuff(opponent, "Poison", player.power(power));
                case "Next Turn Block" -> {
                    rawBlock(player, player.power(power));
                    player.setPower(power, 0);
                }
                default -> { }
            }
        }
        if (isOver()) return;
        for (OrbState orb : player.orbs) if (orb.type == OrbType.PLASMA) gainEnergy(player, 1);
        int count = 5 + player.power("Machine Learning") + player.power("Next Turn Draw");
        player.setPower("Next Turn Draw", 0);
        if (firstTurn[activePlayer]) {
            int innateCount = (int) player.draw.stream().filter(c -> c.definition().innate()).count();
            count = Math.max(count, innateCount);
            if (player.hero == Hero.SILENT) count += 2;
            firstTurn[activePlayer] = false;
        }
        draw(player, count);
        if (player.power("Brutality") > 0) { loseHp(player, player.power("Brutality")); draw(player, player.power("Brutality")); }
    }

    void draw(PlayerState player, int amount) {
        if (player.power("No Draw") > 0 || isOver()) return;
        int drawn = 0;
        while (drawn < amount && player.hand.size() < 10) {
            if (player.draw.isEmpty()) {
                if (player.discard.isEmpty()) break;
                shuffleDiscardIntoDraw(player);
            }
            player.hand.add(player.draw.remove(0));
            drawn++;
        }
        if (drawn > 0) { event(player.hero.displayName() + " drew " + drawn + "."); visual("DRAW", player, null, "Draw", drawn, 0, null); }
    }
    void shuffleDiscardIntoDraw(PlayerState player) {
        if (player.discard.isEmpty()) return;
        player.draw.addAll(player.discard);
        player.discard.clear();
        Collections.shuffle(player.draw, random);
        event(player.hero.displayName() + " shuffled the discard pile into the draw pile.");
        visual("SHUFFLE", player, null, "Shuffle", player.draw.size(), 0, null);
    }
    void attack(PlayerState source, PlayerState target, int base, int strengthMultiplier) {
        if (isOver()) return;
        double result = Math.max(0, base + source.power("Strength") * strengthMultiplier);
        if (source.power("Weakened") > 0) result *= 0.75;
        if (target.power("Vulnerable") > 0) result *= 1.5;
        damage(target, (int) Math.floor(result), false, source, true);
    }
    int modifiedBlock(PlayerState player, int base) {
        double value = Math.max(0, base + player.power("Dexterity"));
        if (player.power("Frail") > 0) value *= 0.75;
        return (int) Math.floor(value);
    }
    void cardBlock(PlayerState player, int amount) { rawBlock(player, modifiedBlock(player, amount)); }
    void rawBlock(PlayerState player, int amount) {
        if (amount <= 0 || isOver()) return;
        player.block = Math.min(999, player.block + amount);
        event(player.hero.displayName() + " gains " + amount + " Block.");
        visual("BLOCK", player, null, "Block", amount, 0, null);
        if (player.power("Juggernaut") > 0) damage(players[player == players[0] ? 1 : 0], player.power("Juggernaut"), false);
    }
    void gainEnergy(PlayerState player, int amount) {
        if (isOver()) return;
        player.energy = Math.min(999, player.energy + amount);
        event(player.hero.displayName() + " gains " + amount + " Energy during resolution.");
        visual("ENERGY", player, null, "Energy", amount, 0, null);
    }
    void heal(PlayerState player, int amount) {
        int actual = Math.min(amount, player.hero.maxHp() - player.hp);
        player.hp += actual;
        if (actual > 0) { event(player.hero.displayName() + " heals " + actual + " HP."); visual("HEAL", player, null, "Heal", actual, 0, null); }
    }
    void loseHp(PlayerState player, int amount) { damage(player, amount, true); }
    private void damage(PlayerState target, int amount, boolean ignoresBlock) { damage(target, amount, ignoresBlock, null, false); }
    private void damage(PlayerState target, int amount, boolean ignoresBlock, PlayerState source, boolean attack) {
        damage(target, amount, ignoresBlock, source, attack, ignoresBlock ? "HP Loss" : attack ? "Attack" : "Effect");
    }
    private void damage(PlayerState target, int amount, boolean ignoresBlock, PlayerState source, boolean attack, String cause) {
        if (isOver() || amount <= 0) return;
        if (target.power("Intangible") > 0) amount = Math.min(amount, 1);
        int absorbed = ignoresBlock ? 0 : Math.min(target.block, amount);
        target.block -= absorbed;
        int lost = Math.min(target.hp, amount - absorbed);
        if (lost > 0 && target.power("Buffer") > 0) { decrement(target, "Buffer"); lost = 0; }
        target.hp -= lost;
        event(target.hero.displayName() + " takes " + lost + " damage" + (absorbed > 0 ? " (" + absorbed + " blocked)" : "") + ".");
        visual("DAMAGE", target, source, cause, lost, absorbed, null);
        if (players[0].hp == 0 || players[1].hp == 0)
            finish(players[0].hp == 0 ? (players[1].hp == 0 ? 2 : 1) : 0);
        if (isOver()) return;
        if (ignoresBlock && lost > 0 && target.power("Rupture") > 0) buff(target, "Strength", target.power("Rupture"));
        if (attack && source != null) {
            if (lost > 0) {
                debuff(target, "Poison", source.power("Envenom"));
                for (int i = 0; i < target.power("Static Discharge"); i++) channel(target, source, OrbType.LIGHTNING);
            }
            damage(source, target.power("Caltrops") + target.power("Flame Barrier"), false);
        }
    }
    void buff(PlayerState player, String name, int amount) {
        if (isOver() || amount == 0) return;
        int total = player.power(name) + amount;
        if (name.equals("Strength") || name.equals("Dexterity") || name.equals("Focus"))
            total = Math.max(-999, Math.min(999, total));
        // Poison's source constructor caps its initial amount; normal stacks use AbstractPower.
        if (name.equals("Poison") && player.power(name) == 0) total = Math.min(9999, total);
        player.setPower(name, total);
        event(player.hero.displayName() + " gains " + amount + " " + name + ".");
        visual("POWER", player, null, name, amount, 0, null);
    }
    void debuff(PlayerState player, String name, int amount) {
        if (isOver() || amount == 0) return;
        if (player.power("Artifact") > 0) {
            decrement(player, "Artifact");
            event(player.hero.displayName() + " Artifact blocked " + name + ".");
            return;
        }
        buff(player, name, amount);
        PlayerState opponent = players[player == players[0] ? 1 : 0];
        if (player != players[activePlayer]) damage(player, opponent.power("Sadistic Nature"), false);
    }
    private void decrement(PlayerState player, String power) {
        if (player.power(power) > 0) player.setPower(power, player.power(power) - 1);
    }
    void channel(PlayerState owner, PlayerState opponent, OrbType type) {
        if (isOver() || owner.orbSlots == 0) return;
        if (owner.orbs.size() >= owner.orbSlots) evokeFirst(owner, opponent, 1);
        if (!isOver()) {
            owner.orbs.add(new OrbState(type));
            event(owner.hero.displayName() + " channels " + type + ".");
            visual("ORB", owner, null, type.name(), 1, 0, null);
        }
    }
    void evokeFirst(PlayerState owner, PlayerState opponent, int times) {
        if (owner.orbs.isEmpty() || isOver()) return;
        OrbState orb = owner.orbs.get(0);
        for (int i = 0; i < times && !isOver(); i++) {
            event("Evoke " + orb.type + ".");
            visual("EVOKE", owner, null, orb.type.name(), 1, 0, null);
            switch (orb.type) {
                case LIGHTNING -> damage(opponent, Math.max(0, 8 + owner.power("Focus")), false, owner, false, "Lightning");
                case FROST -> rawBlock(owner, Math.max(0, 5 + owner.power("Focus")));
                case DARK -> damage(opponent, orb.darkDamage, false, owner, false, "Dark");
                case PLASMA -> gainEnergy(owner, 2);
            }
        }
        owner.orbs.remove(0);
    }
    void passiveOrb(PlayerState owner, PlayerState opponent, OrbState orb, boolean start) {
        if (isOver()) return;
        switch (orb.type) {
            case LIGHTNING -> damage(opponent, Math.max(0, 3 + owner.power("Focus")), false, owner, false, "Lightning");
            case FROST -> rawBlock(owner, Math.max(0, 2 + owner.power("Focus")));
            case DARK -> orb.darkDamage += Math.max(0, 6 + owner.power("Focus"));
            case PLASMA -> { if (start) gainEnergy(owner, 1); }
        }
    }
    private void discard(PlayerState owner, GameCard card, boolean played) {
        owner.discard.add(card);
        visual("DISCARD", owner, null, played ? "Played" : "Unplayed", 1, 0, card.definition());
    }
    void exhaust(PlayerState owner, GameCard card) {
        owner.exhaust.add(card);
        event("Exhaust " + card.definition().name() + ".");
        visual("EXHAUST", owner, null, card.definition().name(), 1, 0, card.definition());
        rawBlock(owner, owner.power("Feel No Pain")); draw(owner, owner.power("Dark Embrace"));
    }
    void upgradeDeck(PlayerState owner) {
        for (List<GameCard> pile : List.of(owner.hand, owner.draw, owner.discard, queue)) for (GameCard card : pile) card.upgrade();
        event(owner.hero.displayName() + " upgrades all cards in hand, draw, discard and queue.");
    }
    OrbType randomOrb() { return OrbType.values()[random.nextInt(OrbType.values().length)]; }
    boolean isOver() { return winner != -1; }
    private void finish(int result) {
        if (isOver()) return;
        winner = result;
        if (result < 2 && players[result].hero == Hero.IRONCLAD) heal(players[result], 6);
        event(result == 2 ? "Draw: both players lost all HP." : "P" + (result + 1) + " wins.");
    }
    private void requireActive() { if (isOver()) throw new IllegalStateException("The match has ended."); }
    private void event(String message) {
        log.add(message);
    }
    private void notifyObservers() {
        BattleSnapshot state = snapshot();
        for (GameObserver observer : observers) {
            // A failed presentation callback must not undo a completed game command.
            try { observer.onChanged(state); } catch (RuntimeException ignored) { }
        }
    }
}
