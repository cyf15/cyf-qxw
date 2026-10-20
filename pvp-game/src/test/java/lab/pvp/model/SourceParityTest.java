package lab.pvp.model;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** Independent expectations read from source card/action/power classes, not from the TSV. */
public class SourceParityTest {
    private BattleEngine battle(Hero hero, String... opening) {
        List<String> deck = new ArrayList<>(Collections.nCopies(15, hero == Hero.DEFECT ? "Defend_B" : hero == Hero.SILENT ? "Defend_G" : "Defend_R"));
        for (int i = 0; i < opening.length; i++) deck.set(i, opening[i]);
        for (long seed = 0; seed < 10000; seed++) {
            BattleEngine battle = new BattleEngine(hero, deck, Hero.IRONCLAD, Collections.nCopies(15, "Strike_R"), seed);
            if (battle.snapshot().players().get(0).hand().stream().map(CardDefinition::id).toList().containsAll(List.of(opening))) return battle;
        }
        throw new AssertionError("Opening hand not found");
    }
    private PlayerSnapshot p(BattleEngine b, int i) { return b.snapshot().players().get(i); }
    private void play(BattleEngine b, String id) {
        var hand = p(b, b.snapshot().activePlayer()).hand();
        for (int i = 0; i < hand.size(); i++) if (hand.get(i).id().equals(id)) { b.playCard(i); return; }
        fail("Missing " + id);
    }
    private int power(BattleEngine b, int i, String key) { return p(b, i).powers().getOrDefault(key, 0); }

    @Test public void catalogOnlyContainsBaseCardsAndRejectsRemovedUpgradeEffects() {
        assertEquals(122, CardFactory.allCards().size());
        assertTrue(CardFactory.allCards().stream().noneMatch(c -> c.id().endsWith("+") || c.name().endsWith("+")));
        for (CardDefinition card : CardFactory.allCards())
            assertThrows(IllegalArgumentException.class, () -> CardFactory.create(card.id() + "+"));
        assertThrows(IllegalArgumentException.class, () -> CardFactory.get("Apotheosis"));
        assertEquals(6, CardFactory.get("Strike_R").damage());
        assertEquals(5, CardFactory.get("Defend_G").block());
        assertEquals(1, CardFactory.get("Zap").cost());
        assertTrue(CardFactory.get("Limit Break").exhaust());
        assertFalse(CardFactory.get("Chill").innate());
    }
    @Test public void basePommelStrikeDrawsOneAndAngerCopiesItsBaseCard() {
        BattleEngine b = battle(Hero.IRONCLAD, "Pommel Strike", "Anger");
        play(b, "Pommel Strike"); play(b, "Anger"); b.endTurn();
        assertTrue(b.snapshot().log().contains("Ironclad drew 1."));
        assertEquals(7, p(b, 0).discardCount());
        b.endTurn(); // settle
        assertEquals(9 + 6, 80 - p(b, 1).hp());
    }
    @Test public void pummelUsesFourStrengthScaledHitsAndExhausts() {
        BattleEngine b = battle(Hero.IRONCLAD, "Inflame", "Pummel");
        play(b, "Inflame"); play(b, "Pummel"); b.endTurn();
        assertEquals(1, p(b, 0).exhaustCount());
        b.endTurn(); // settle
        assertEquals(64, p(b, 1).hp());
    }
    @Test public void uppercutAppliesBothDebuffs() {
        BattleEngine b = battle(Hero.IRONCLAD, "Uppercut"); play(b, "Uppercut"); b.endTurn();
        assertEquals(1, power(b, 1, "Weakened")); assertEquals(1, power(b, 1, "Vulnerable"));
        b.endTurn(); // settle
        assertEquals(67, p(b, 1).hp());
    }
    @Test public void flexExpiresAfterQueuedAttacks() {
        BattleEngine b = battle(Hero.IRONCLAD, "Flex", "Strike_R"); play(b, "Flex"); play(b, "Strike_R"); b.endTurn();
        assertEquals(0, power(b, 0, "Strength"));
        b.endTurn(); // settle
        assertEquals(72, p(b, 1).hp());
    }
    @Test public void artifactBlocksStrengthReduction() {
        BattleEngine b = new BattleEngine(Hero.IRONCLAD, Collections.nCopies(15, "Panacea"), Hero.IRONCLAD, Collections.nCopies(15, "Disarm"), 1);
        b.playCard(0); b.endTurn(); b.playCard(0); b.endTurn();
        assertEquals(0, power(b, 0, "Artifact")); assertEquals(0, power(b, 0, "Strength"));
    }
    @Test public void piercingWailRestoresStrengthAfterAffectedPlayersTurn() {
        BattleEngine b = battle(Hero.SILENT, "PiercingWail"); play(b, "PiercingWail"); b.endTurn();
        assertEquals(-6, power(b, 1, "Strength")); b.playCard(0); b.endTurn();
        assertEquals(65, p(b, 0).hp()); assertEquals(0, power(b, 1, "Strength"));
    }
    @Test public void baseCatalystDoublesPoisonBeforeItsTick() {
        BattleEngine b = battle(Hero.SILENT, "Deadly Poison", "Catalyst"); play(b, "Deadly Poison"); play(b, "Catalyst"); b.endTurn();
        assertEquals(70, p(b, 1).hp()); assertEquals(9, power(b, 1, "Poison"));
    }
    @Test public void unplayedCarnageFollowsUserDiscardOverride() {
        BattleEngine b = battle(Hero.IRONCLAD, "Carnage"); b.endTurn();
        assertEquals(0, p(b, 0).exhaustCount()); assertEquals(5, p(b, 0).discardCount());
    }
    @Test public void barricadePreservesBlockAtNextOwnerTurn() {
        BattleEngine b = battle(Hero.IRONCLAD, "Barricade"); play(b, "Barricade"); b.endTurn(); b.endTurn();
        play(b, "Defend_R"); b.endTurn(); b.endTurn();
        assertEquals(5, p(b, 0).block());
    }
    @Test public void flameBarrierRetaliatesAndExpiresAtNextOwnerTurn() {
        BattleEngine b = battle(Hero.IRONCLAD, "Flame Barrier"); play(b, "Flame Barrier"); b.endTurn();
        b.playCard(0); b.playCard(0); b.endTurn();
        assertEquals(80, p(b, 0).hp()); assertEquals(72, p(b, 1).hp()); assertEquals(0, power(b, 0, "Flame Barrier"));
    }
    @Test public void juggernautTriggersOnRawBlockGain() {
        BattleEngine b = battle(Hero.IRONCLAD, "Juggernaut", "Defend_R"); play(b, "Juggernaut"); play(b, "Defend_R"); b.endTurn();
        b.endTurn(); // settle
        assertEquals(75, p(b, 1).hp());
    }
    @Test public void ruptureAndCombustUseHpLossAndUnmodifiedDamage() {
        BattleEngine b = battle(Hero.IRONCLAD, "Rupture", "Combust"); play(b, "Rupture"); play(b, "Combust"); b.endTurn();
        assertEquals(79, p(b, 0).hp()); assertEquals(1, power(b, 0, "Strength"));
        b.endTurn(); // settle
        assertEquals(75, p(b, 1).hp());
    }
    @Test public void feelNoPainTriggersOnExhaustAndDarkEmbraceDraws() {
        BattleEngine b = battle(Hero.IRONCLAD, "Feel No Pain", "Dark Embrace", "Intimidate");
        play(b, "Feel No Pain"); play(b, "Dark Embrace"); play(b, "Intimidate"); b.endTurn();
        assertEquals(3, p(b, 0).block()); assertEquals(1, p(b, 0).exhaustCount()); assertEquals(0, p(b, 0).hand().size());
        assertTrue(b.snapshot().log().contains("Ironclad drew 1."));
    }
    @Test public void biasedCognitionDecaysFocusAtNextTurn() {
        BattleEngine b = battle(Hero.DEFECT, "Biased Cognition"); play(b, "Biased Cognition"); b.endTurn(); b.endTurn();
        assertEquals(3, power(b, 0, "Focus"));
    }
    @Test public void bufferPreventsHpLossButConsumesOnlyAfterBlock() {
        BattleEngine b = battle(Hero.DEFECT, "Buffer"); play(b, "Buffer"); b.endTurn(); b.playCard(0); b.endTurn();
        assertEquals(70, p(b, 0).hp()); assertEquals(0, power(b, 0, "Buffer"));
    }
    @Test public void baseDarknessOnlyGrowsAtEndTurn() {
        BattleEngine b = battle(Hero.DEFECT, "Darkness"); play(b, "Darkness"); b.endTurn();
        assertTrue(p(b, 0).orbs().contains("Dark +6/12"));
    }
    @Test public void baseFissionRemovesOrbsWithoutEvoking() {
        BattleEngine b = battle(Hero.DEFECT, "Fission"); play(b, "Fission"); b.endTurn();
        assertEquals(80, p(b, 1).hp()); assertEquals(0, p(b, 0).orbs().size()); assertEquals(4, p(b, 0).energy());
    }
    @Test public void recursionRechannelsEvolvedOrbAndConsumeRemovesRightmostSlot() {
        BattleEngine b = battle(Hero.DEFECT, "Redo", "Consume"); play(b, "Redo"); play(b, "Consume"); b.endTurn();
        assertEquals(2, p(b, 0).orbSlots()); assertEquals(List.of("Lightning 5/10"), p(b, 0).orbs());
        b.endTurn(); // settle
        assertEquals(67, p(b, 1).hp());
    }
    @Test public void baseOutmaneuverGrantsTwoNextTurnEnergy() {
        BattleEngine b = battle(Hero.SILENT, "Outmaneuver"); play(b, "Outmaneuver"); b.endTurn(); b.endTurn(); assertEquals(5, p(b, 0).energy());
    }
    @Test public void eachCatalogCardActuallyResolvesWithoutMissingPowerCase() {
        for (CardDefinition card : CardFactory.allCards()) {
            Hero hero = card.hero() == null ? Hero.IRONCLAD : card.hero();
            BattleEngine b = new BattleEngine(hero, Collections.nCopies(15, card.id()), Hero.IRONCLAD, Collections.nCopies(15, "Defend_R"), 0);
            if (card.cost() <= 3) { b.playCard(0); b.endTurn(); }
        }
    }
    @Test public void feedbackEventsHidePlayedCardsAndResolveHitsAtSettlement() {
        BattleEngine b = battle(Hero.IRONCLAD, "Pummel");
        int before = b.combatEvents().size(); play(b, "Pummel");
        assertTrue(b.combatEvents().size() > before);
        assertEquals(0, b.snapshot().activePlayer());
        var played = b.combatEvents().subList(before, b.combatEvents().size());
        assertEquals("HIDDEN", played.get(0).kind());
        assertEquals(0, played.stream().filter(e -> e.kind().equals("DAMAGE")).count());
        b.endTurn(); b.endTurn(); // settle
        var hits = b.combatEvents().stream().filter(e -> e.kind().equals("DAMAGE") && e.name().equals("Attack")).toList();
        assertEquals(4, hits.size());
        assertEquals(List.of(78, 76, 74, 72), hits.stream().map(e -> e.state().hp()).toList());
        assertTrue(hits.stream().allMatch(e -> e.player() == 1 && e.source() == 0 && e.amount() == 2));
    }
    @Test public void drawViewerIsSortedAndDoesNotMutateTheNextDraw() {
        BattleEngine b = battle(Hero.IRONCLAD, "Bash");
        BattleSnapshot before = b.snapshot(); var draw = b.inspectPile("Draw");
        assertEquals(before.players().get(0).drawCount(), draw.size());
        assertEquals(draw.stream().map(CardDefinition::name).sorted().toList(), draw.stream().map(CardDefinition::name).toList());
        assertEquals(before, b.snapshot());
        assertThrows(UnsupportedOperationException.class, () -> draw.clear());
    }
    @Test public void artifactBlocksFlexsTemporaryStrengthLoss() {
        BattleEngine b = battle(Hero.IRONCLAD, "Panacea", "Flex");
        play(b, "Panacea"); play(b, "Flex"); b.endTurn();
        assertEquals(2, power(b, 0, "Strength")); assertEquals(0, power(b, 0, "Artifact"));
    }
}
