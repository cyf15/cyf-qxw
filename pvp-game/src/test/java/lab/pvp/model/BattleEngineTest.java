package lab.pvp.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class BattleEngineTest {
    private static List<String> deck(String id) { return Collections.nCopies(15, id); }
    private static List<String> mixed(String special, String rest) {
        List<String> result = new ArrayList<>(deck(rest)); result.set(0, special); return result;
    }
    private static BattleEngine game(Hero hero, List<String> cards) {
        return new BattleEngine(hero, cards, Hero.IRONCLAD, deck("Defend_R"), 42);
    }
    private static PlayerSnapshot p(BattleEngine engine, int index) { return engine.snapshot().players().get(index); }
    private static void queue(BattleEngine engine, String id) {
        List<CardDefinition> hand = p(engine, engine.snapshot().activePlayer()).hand();
        for (int i = 0; i < hand.size(); i++) if (hand.get(i).id().equals(id)) { engine.queueCard(i); return; }
        fail("Expected card in hand: " + id);
    }
    private static BattleEngine firstHand(Hero hero, List<String> deck, String... ids) {
        for (long seed = 0; seed < 2000; seed++) {
            BattleEngine engine = new BattleEngine(hero, deck, Hero.IRONCLAD, deck("Defend_R"), seed);
            List<String> actual = p(engine, 0).hand().stream().map(CardDefinition::id).toList();
            if (actual.containsAll(List.of(ids))) return engine;
        }
        throw new AssertionError("Cannot find requested opening hand");
    }

    @Test public void rejectsWrongDeckSizeUnknownCardsAndOtherClassCards() {
        assertThrows(IllegalArgumentException.class, () -> game(Hero.IRONCLAD, List.of("Strike_R")));
        assertThrows(IllegalArgumentException.class, () -> game(Hero.IRONCLAD, deck("Strike_G")));
        assertThrows(IllegalArgumentException.class, () -> game(Hero.IRONCLAD, deck("Unimplemented")));
    }
    @Test public void catalogContainsOnlySupportedSourceCardsAndExactStarterDecks() {
        assertEquals(246, CardFactory.allCards().size());
        for (Hero hero : Hero.values()) {
            assertEquals(15, CardFactory.starterDeck(hero).size());
            CardFactory.validateDeck(hero, CardFactory.starterDeck(hero));
            assertTrue(CardFactory.pool(hero).stream().allMatch(c -> c.hero() == hero || c.hero() == null));
            assertTrue(CardFactory.pool(hero).stream().filter(c -> c.hero() == hero).count() >= 12);
        }
        assertEquals(6, CardFactory.get("Strike_R").damage());
        assertEquals(5, CardFactory.get("Defend_R").block());
        assertEquals("Charge Battery", CardFactory.get("Conserve Battery").name());
    }
    @Test public void initialStateUsesUserHpAndSourceStartingRelics() {
        BattleEngine iron = game(Hero.IRONCLAD, deck("Strike_R"));
        assertEquals(80, p(iron, 0).hp());
        assertEquals(3, p(iron, 0).energy());
        assertEquals(5, p(iron, 0).hand().size());
        assertEquals(0, p(iron, 1).hand().size());
        BattleEngine silent = game(Hero.SILENT, deck("Strike_G"));
        assertEquals(65, p(silent, 0).hp());
        assertEquals(7, p(silent, 0).hand().size());
        BattleEngine defect = game(Hero.DEFECT, deck("Strike_B"));
        assertEquals(70, p(defect, 0).hp());
        assertEquals(List.of("Lightning 3/8"), p(defect, 0).orbs());
    }
    @Test public void queueReservesEnergyButDefersDamageUntilEndTurn() {
        BattleEngine engine = game(Hero.IRONCLAD, deck("Strike_R"));
        engine.queueCard(0);
        assertEquals(2, p(engine, 0).energy());
        assertEquals(80, p(engine, 1).hp());
        assertEquals(1, engine.snapshot().queue().size());
        engine.endTurn();
        assertEquals(74, p(engine, 1).hp());
        assertEquals(1, engine.snapshot().activePlayer());
        assertEquals(0, p(engine, 0).hand().size());
        assertEquals(5, p(engine, 0).discardCount());
        assertEquals(5, p(engine, 1).hand().size());
    }
    @Test public void invalidPlayDoesNotMutateAnySnapshotFieldOrNotify() {
        BattleEngine engine = game(Hero.IRONCLAD, deck("Bash"));
        engine.queueCard(0);
        AtomicInteger notices = new AtomicInteger(); engine.addObserver(s -> notices.incrementAndGet());
        BattleSnapshot before = engine.snapshot();
        assertThrows(IllegalArgumentException.class, () -> engine.queueCard(0));
        assertEquals(before, engine.snapshot());
        assertThrows(IllegalArgumentException.class, () -> engine.queueCard(-1));
        assertEquals(before, engine.snapshot());
        assertEquals(0, notices.get());
    }
    @Test public void blockProtectsAgainstOpponentThenExpiresOnOwnersNextTurn() {
        BattleEngine engine = new BattleEngine(Hero.IRONCLAD, deck("Defend_R"), Hero.IRONCLAD, deck("Strike_R"), 0);
        engine.queueCard(0); assertEquals(0, p(engine, 0).block());
        engine.endTurn(); assertEquals(5, p(engine, 0).block());
        engine.queueCard(0); engine.endTurn();
        assertEquals(79, p(engine, 0).hp());
        assertEquals(0, p(engine, 0).block());
    }
    @Test public void queueOrderAppliesStrengthBeforeOnlySubsequentAttacks() {
        BattleEngine first = firstHand(Hero.IRONCLAD, mixed("Inflame", "Strike_R"), "Inflame", "Strike_R");
        queue(first, "Inflame"); queue(first, "Strike_R"); first.endTurn();
        assertEquals(72, p(first, 1).hp());
        assertEquals(Integer.valueOf(2), p(first, 0).powers().get("Strength"));
        assertEquals(0, p(first, 0).exhaustCount()); // Power leaves play, not exhaust.
        assertEquals(4, p(first, 0).discardCount());
        BattleEngine last = firstHand(Hero.IRONCLAD, mixed("Inflame", "Strike_R"), "Inflame", "Strike_R");
        queue(last, "Strike_R"); queue(last, "Inflame"); last.endTurn();
        assertEquals(74, p(last, 1).hp());
    }
    @Test public void vulnerableAndHeavyBladeUseSourceDamageMath() {
        BattleEngine bash = firstHand(Hero.IRONCLAD, mixed("Bash", "Strike_R"), "Bash", "Strike_R");
        queue(bash, "Bash"); queue(bash, "Strike_R"); bash.endTurn();
        assertEquals(63, p(bash, 1).hp());
        assertEquals(Integer.valueOf(2), p(bash, 1).powers().get("Vulnerable"));
        BattleEngine blade = firstHand(Hero.IRONCLAD, mixed("Inflame", "Heavy Blade"), "Inflame", "Heavy Blade");
        queue(blade, "Inflame"); queue(blade, "Heavy Blade"); blade.endTurn();
        assertEquals(60, p(blade, 1).hp());
    }
    @Test public void weakLastsThroughAffectedPlayersTurnAndRoundsDown() {
        BattleEngine engine = new BattleEngine(Hero.SILENT, deck("Neutralize"), Hero.IRONCLAD, deck("Strike_R"), 0);
        engine.queueCard(0); engine.endTurn();
        assertEquals(Integer.valueOf(1), p(engine, 1).powers().get("Weakened"));
        engine.queueCard(0); engine.endTurn();
        assertEquals(61, p(engine, 0).hp());
        assertFalse(p(engine, 1).powers().containsKey("Weakened"));
    }
    @Test public void poisonBypassesBlockAndDecaysAtVictimsStart() {
        BattleEngine engine = new BattleEngine(Hero.SILENT, deck("Blur"), Hero.SILENT, deck("Deadly Poison"), 0);
        engine.queueCard(0); engine.endTurn();
        engine.queueCard(0); engine.endTurn();
        assertEquals(60, p(engine, 0).hp());
        assertEquals(5, p(engine, 0).block());
        assertEquals(Integer.valueOf(4), p(engine, 0).powers().get("Poison"));
    }
    @Test public void artifactConsumesAChargeInsteadOfApplyingDebuff() {
        BattleEngine engine = new BattleEngine(Hero.IRONCLAD, deck("Panacea"), Hero.SILENT, deck("Deadly Poison"), 0);
        engine.queueCard(0); engine.endTurn();
        engine.queueCard(0); engine.endTurn();
        assertEquals(80, p(engine, 0).hp());
        assertFalse(p(engine, 0).powers().containsKey("Poison"));
        assertFalse(p(engine, 0).powers().containsKey("Artifact"));
        assertEquals(1, p(engine, 0).exhaustCount());
    }
    @Test public void blurPreservesBlockOnceBeforeItsDurationExpires() {
        BattleEngine engine = game(Hero.SILENT, deck("Blur"));
        engine.queueCard(0); engine.endTurn();
        assertEquals(5, p(engine, 0).block());
        engine.endTurn();
        assertEquals(5, p(engine, 0).block());
        assertFalse(p(engine, 0).powers().containsKey("Blur"));
        engine.endTurn(); engine.endTurn();
        assertEquals(0, p(engine, 0).block());
    }
    @Test public void nextTurnEnergyAndBlockAreAppliedToTheirOwner() {
        BattleEngine engine = game(Hero.SILENT, deck("Outmaneuver"));
        engine.queueCard(0); engine.endTurn();
        assertEquals(3, p(engine, 1).energy());
        engine.endTurn(); assertEquals(5, p(engine, 0).energy());
        BattleEngine roll = game(Hero.SILENT, deck("Dodge and Roll"));
        roll.queueCard(0); roll.endTurn(); roll.endTurn();
        assertEquals(4, p(roll, 0).block());
    }
    @Test public void drawAndEnergyEffectsRespectDeferredResolution() {
        BattleEngine engine = game(Hero.IRONCLAD, deck("Pommel Strike"));
        engine.queueCard(0);
        assertEquals(10, p(engine, 0).drawCount());
        engine.endTurn();
        assertEquals(9, p(engine, 0).drawCount());
        assertEquals(6, p(engine, 0).discardCount());
        assertEquals(0, p(engine, 0).hand().size());
        BattleEngine energy = game(Hero.IRONCLAD, deck("Seeing Red"));
        energy.queueCard(0); assertEquals(2, p(energy, 0).energy());
        energy.endTurn(); assertEquals(4, p(energy, 0).energy());
        assertEquals(1, p(energy, 0).exhaustCount());
        energy.endTurn(); assertEquals(3, p(energy, 0).energy());
    }
    @Test public void noDrawPowerStopsLaterDrawActionsInTheSameBatch() {
        BattleEngine engine = firstHand(Hero.IRONCLAD, mixed("Battle Trance", "Pommel Strike"), "Battle Trance", "Pommel Strike");
        queue(engine, "Battle Trance"); queue(engine, "Pommel Strike"); engine.endTurn();
        assertEquals(7, p(engine, 0).drawCount());
        assertFalse(p(engine, 0).powers().containsKey("No Draw"));
    }
    @Test public void exhaustedCardsNeverShuffleBackButAngerAddsCopies() {
        BattleEngine engine = game(Hero.IRONCLAD, deck("Impervious"));
        engine.queueCard(0); engine.endTurn();
        assertEquals(30, p(engine, 0).block());
        assertEquals(1, p(engine, 0).exhaustCount());
        for (int i = 0; i < 12; i++) engine.endTurn();
        PlayerSnapshot state = p(engine, 0);
        assertEquals(14, state.hand().size() + state.drawCount() + state.discardCount());
        BattleEngine anger = game(Hero.IRONCLAD, deck("Anger"));
        anger.queueCard(0); anger.endTurn();
        assertEquals(6, p(anger, 0).discardCount());
    }
    @Test public void orbsChannelPassivelyTriggerEvokeAndRespectFocus() {
        BattleEngine dual = game(Hero.DEFECT, deck("Dualcast"));
        dual.queueCard(0); dual.endTurn();
        assertEquals(64, p(dual, 1).hp());
        assertTrue(p(dual, 0).orbs().isEmpty());
        BattleEngine frost = game(Hero.DEFECT, deck("Glacier"));
        frost.queueCard(0); frost.endTurn();
        assertEquals(11, p(frost, 0).block());
        assertEquals(3, p(frost, 0).orbs().size());
        assertEquals(77, p(frost, 1).hp());
        BattleEngine focus = game(Hero.DEFECT, deck("Defragment"));
        focus.queueCard(0); focus.endTurn();
        assertEquals(76, p(focus, 1).hp());
        assertEquals(List.of("Lightning 4/9"), p(focus, 0).orbs());
    }
    @Test public void overflowEvokesOldestOrbBeforeChannelingNewOne() {
        BattleEngine engine = game(Hero.DEFECT, deck("Zap"));
        engine.queueCard(0); engine.queueCard(0); engine.queueCard(0); engine.endTurn();
        assertEquals(3, p(engine, 0).orbs().size());
        assertEquals(63, p(engine, 1).hp()); // 8 evoke + 3*3 passive.
    }
    @Test public void darkOrbStoresDamageAndInnateCardsRespectTenCardHandLimit() {
        BattleEngine engine = game(Hero.DEFECT, deck("Darkness"));
        engine.queueCard(0); engine.endTurn();
        assertTrue(p(engine, 0).orbs().contains("Dark +6/12"));
        BattleEngine innate = game(Hero.IRONCLAD, deck("Dramatic Entrance"));
        assertEquals(10, p(innate, 0).hand().size());
        assertEquals(5, p(innate, 0).drawCount());
    }
    @Test public void seededShuffleIsReproducibleAndPilesSurviveRepeatedTurns() {
        BattleEngine a = game(Hero.IRONCLAD, CardFactory.starterDeck(Hero.IRONCLAD));
        BattleEngine b = game(Hero.IRONCLAD, CardFactory.starterDeck(Hero.IRONCLAD));
        assertEquals(a.snapshot(), b.snapshot());
        for (int i = 0; i < 12; i++) { a.endTurn(); b.endTurn(); }
        assertEquals(a.snapshot(), b.snapshot());
        for (PlayerSnapshot state : a.snapshot().players())
            assertEquals(15, state.hand().size() + state.drawCount() + state.discardCount());
    }
    @Test public void differentSeedsActuallyShuffleTheDeck() {
        BattleEngine a = new BattleEngine(Hero.IRONCLAD, CardFactory.starterDeck(Hero.IRONCLAD), Hero.IRONCLAD, deck("Defend_R"), 0);
        BattleEngine b = new BattleEngine(Hero.IRONCLAD, CardFactory.starterDeck(Hero.IRONCLAD), Hero.IRONCLAD, deck("Defend_R"), 1);
        assertNotEquals(p(a, 0).hand(), p(b, 0).hand());
    }
    @Test public void plasmaIgnoresFocusAndGrantsEnergyAtOwnersStart() {
        for (long seed = 0; seed < 2000; seed++) {
            BattleEngine engine = new BattleEngine(Hero.DEFECT, mixed("Defragment", "Chaos"), Hero.IRONCLAD, deck("Defend_R"), seed);
            if (p(engine, 0).hand().stream().noneMatch(c -> c.id().equals("Defragment"))) continue;
            queue(engine, "Defragment"); queue(engine, "Chaos"); engine.endTurn();
            if (!p(engine, 0).orbs().contains("Plasma 1/2")) continue;
            assertEquals(Integer.valueOf(1), p(engine, 0).powers().get("Focus"));
            engine.endTurn();
            assertEquals(4, p(engine, 0).energy());
            return;
        }
        fail("No deterministic Plasma seed found");
    }
    @Test public void noxiousFumesTicksOnOwnersStartAndPoisonCanWinBeforeDraw() {
        BattleEngine fumes = game(Hero.SILENT, deck("Noxious Fumes"));
        fumes.queueCard(0); fumes.endTurn();
        assertFalse(p(fumes, 1).powers().containsKey("Poison"));
        fumes.endTurn();
        assertEquals(Integer.valueOf(2), p(fumes, 1).powers().get("Poison"));
        BattleEngine poison = new BattleEngine(Hero.SILENT, deck("Deadly Poison"), Hero.IRONCLAD, deck("Defend_R"), 0);
        for (int i = 0; i < 20 && poison.snapshot().winner() == -1; i++) {
            if (poison.snapshot().activePlayer() == 0)
                for (int j = 0; j < 3; j++) poison.queueCard(0);
            poison.endTurn();
        }
        assertEquals(0, poison.snapshot().winner());
        assertEquals(0, p(poison, 1).hp());
        assertTrue(p(poison, 1).hand().isEmpty());
    }
    @Test public void lethalDamageClampsHpStopsQueueAndPreventsFuturePlay() {
        BattleEngine engine = new BattleEngine(Hero.IRONCLAD, deck("Anger"), Hero.SILENT, deck("Defend_G"), 3);
        int turns = 0;
        while (engine.snapshot().winner() == -1 && turns++ < 20) {
            if (engine.snapshot().activePlayer() == 0)
                while (!p(engine, 0).hand().isEmpty()) engine.queueCard(0);
            engine.endTurn();
        }
        assertEquals(0, engine.snapshot().winner());
        assertEquals(0, p(engine, 1).hp());
        BattleSnapshot done = engine.snapshot();
        assertThrows(IllegalStateException.class, () -> engine.queueCard(0));
        assertThrows(IllegalStateException.class, engine::endTurn);
        assertEquals(done, engine.snapshot());
    }
    @Test public void observersReceiveImmutableCompletedStates() {
        BattleEngine engine = game(Hero.IRONCLAD, deck("Strike_R"));
        AtomicInteger notices = new AtomicInteger();
        engine.addObserver(s -> { notices.incrementAndGet(); assertNotNull(s.players()); });
        BattleSnapshot before = engine.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> before.players().clear());
        assertThrows(UnsupportedOperationException.class, () -> before.players().get(0).hand().clear());
        assertThrows(UnsupportedOperationException.class, () -> before.players().get(0).powers().clear());
        engine.queueCard(0); engine.endTurn();
        assertEquals(2, notices.get());
        assertEquals(5, before.players().get(0).hand().size());
        assertEquals(80, before.players().get(1).hp());
    }
}
