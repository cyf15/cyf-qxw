package lab.pvp.view.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.google.gson.GsonBuilder;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import lab.pvp.controller.GameController;
import lab.pvp.model.*;
import lab.pvp.persistence.MatchRecord;
import lab.pvp.persistence.SavedDeck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

/** Scene2D view over the same controller and immutable snapshots used by the console UI. */
public final class GameApplication extends ApplicationAdapter {
    private enum Screen { MENU, DECK, PASS, BATTLE, VICTORY }
    private static final float WIDTH = 1280, HEIGHT = 800;
    private Stage stage;
    private GuiAssets assets;
    private SourceAudio audio;
    private SourceSceneActor sourceScene;
    private SourceHeroActor smokeRobot;
    private final SourceHeroActor[] heroActors = new SourceHeroActor[2];
    private float resolutionDelay;
    private final java.util.ArrayDeque<CombatEvent> playback = new java.util.ArrayDeque<>();
    private CardDefinition resolvingCard;
    private int resolvingHit;
    private int visualDamageEvents, visualCardEvents;
    private int viewPlayer, playbackQueueIndex, remainingQueued;
    private boolean resolving;
    private Map<String, com.google.gson.JsonObject> sourceEffects;
    private final Label[] hpLabels = new Label[2], statLabels = new Label[2], powerLabels = new Label[2];
    private final Image[] hpFills = new Image[2];
    private GameController controller;
    private DisplaySettings settings;
    private Dialog settingsDialog;
    private Dialog exitDialog;
    private int settingsSmokeStep;
    private boolean settingsSmokeDone;
    private boolean smokePreviousVsync;
    private Screen screen;
    private boolean battleDirty;
    private boolean matchStarted;
    private final Hero[] heroes = { Hero.IRONCLAD, Hero.SILENT };
    private final String[] deckNames = { "Player 1 Ironclad", "Player 2 Silent" };
    private final List<List<String>> decks = List.of(new ArrayList<>(), new ArrayList<>());
    private int deckPlayer;
    private Label status;
    private String message = "";
    private TextField deckName;
    private String filter = "ALL";
    private ScrollPane catalogScroll;
    private float catalogScrollY;
    private final boolean smoke = Boolean.getBoolean("pvp.smoke");
    private float smokeTime;
    private int smokeStep;
    private int smokeActions;
    private int smokeFirstHp;
    private int smokeLastTurn;
    private int smokeInitialHistorySize;
    private long smokeInitialLastMatchId;
    private long smokeSavedDeckId;
    private final List<String> smokeEvidence = new ArrayList<>();
    private final Map<String, String> screenshots = new LinkedHashMap<>();

    @Override public void create() {
        assets = new GuiAssets();
        sourceEffects = new com.google.gson.Gson().fromJson(Gdx.files.internal("catalog/effects.json").readString(),
            new com.google.gson.reflect.TypeToken<Map<String, com.google.gson.JsonObject>>(){}.getType());
        String dataDirectory = System.getProperty("pvp.dataDir", smoke
            ? "output/gui-smoke-data-" + java.util.UUID.randomUUID() : "data");
        controller = new GameController(java.nio.file.Path.of(dataDirectory));
        settings = new DisplaySettings(java.nio.file.Path.of(dataDirectory));
        if (smoke) {
            List<MatchRecord> previous = controller.history();
            smokeInitialHistorySize = previous.size();
            smokeInitialLastMatchId = previous.stream().mapToLong(MatchRecord::id).max().orElse(0);
        }
        sourceScene = new SourceSceneActor(assets.scene, assets.vfx);
        audio = new SourceAudio();
        audio.volumes(settings.musicVolume, settings.soundVolume);
        stage = new Stage(new FitViewport(WIDTH, HEIGHT), new com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch());
        Gdx.input.setInputProcessor(stage);
        showMenu();
        applyDisplaySettings();
    }

    private void applyDisplaySettings() {
        boolean applied = settings.fullscreen
            ? Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode())
            : Gdx.graphics.setWindowedMode(settings.width, settings.height);
        if (!applied) throw new IllegalStateException("This display mode is not supported.");
        Gdx.graphics.setVSync(settings.vsync);
    }

    private boolean hasActiveMatch() {
        return (screen == Screen.BATTLE || screen == Screen.PASS) && matchStarted && controller.battle().snapshot().winner() < 0;
    }

    private void showSettings() {
        if (settingsDialog != null && settingsDialog.getStage() != null) return;
        Dialog dialog = dialog("Settings / Options");
        settingsDialog = dialog;
        dialog.setName("settings-dialog");
        final boolean[] fullscreen = {settings.fullscreen};
        final boolean[] vsync = {settings.vsync};
        final int[][] sizes = {{1024, 640}, {1280, 800}, {1600, 1000}, {1920, 1080}};
        final int[] size = {1};
        for (int i = 0; i < sizes.length; i++) if (sizes[i][0] == settings.width && sizes[i][1] == settings.height) size[0] = i;
        Table content = dialog.getContentTable();
        content.defaults().pad(7);
        content.add(label("Display mode", "body")).left();
        TextButton mode = button(fullscreen[0] ? "Fullscreen" : "Windowed", "setting-fullscreen", false, () -> {});
        mode.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { fullscreen[0] = !fullscreen[0]; mode.setText(fullscreen[0] ? "Fullscreen" : "Windowed"); }
        });
        content.add(mode).width(285).row();
        content.add(label("Window resolution", "body")).left();
        TextButton resolution = button(sizes[size[0]][0] + " x " + sizes[size[0]][1], "setting-resolution", false, () -> {});
        resolution.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { size[0] = (size[0] + 1) % sizes.length; resolution.setText(sizes[size[0]][0] + " x " + sizes[size[0]][1]); }
        });
        content.add(resolution).width(285).row();
        content.add(label("Vertical sync", "body")).left();
        TextButton sync = button(vsync[0] ? "On" : "Off", "setting-vsync", false, () -> {});
        sync.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { vsync[0] = !vsync[0]; sync.setText(vsync[0] ? "On" : "Off"); }
        });
        content.add(sync).width(285).row();
        final float[] volumes = {settings.musicVolume, settings.soundVolume};
        for (int channel = 0; channel < 2; channel++) {
            final int c = channel;
            content.add(label(c == 0 ? "Music volume" : "Sound effects", "body")).left();
            TextButton volume = button(Math.round(volumes[c] * 100) + "%", "setting-volume-" + c, false, () -> {});
            volume.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    volumes[c] = volumes[c] >= 1 ? 0 : Math.min(1, volumes[c] + .1f);
                    volume.setText(Math.round(volumes[c] * 100) + "%");
                }
            });
            content.add(volume).width(285).row();
        }
        Label feedback = muted("Apply saves these display options for the next launch.");
        feedback.setWrap(true);
        content.add(feedback).colspan(2).width(520).padTop(12).row();
        content.add(button("Apply settings", "settings-apply", true, () -> {
            boolean oldFullscreen = settings.fullscreen, oldVsync = settings.vsync;
            int oldWidth = settings.width, oldHeight = settings.height;
            try {
                settings.fullscreen = fullscreen[0]; settings.vsync = vsync[0];
                settings.width = sizes[size[0]][0]; settings.height = sizes[size[0]][1];
                applyDisplaySettings();
                settings.musicVolume = volumes[0]; settings.soundVolume = volumes[1];
                audio.volumes(volumes[0], volumes[1]); settings.save();
                feedback.setText("Settings saved.");
            } catch (RuntimeException ex) {
                settings.fullscreen = oldFullscreen; settings.vsync = oldVsync; settings.width = oldWidth; settings.height = oldHeight;
                applyDisplaySettings(); feedback.setText(ex.getMessage());
            }
        })).colspan(2).fillX().row();
        if (screen != Screen.MENU) {
            content.add(button("Return to main menu", "settings-menu", false, () -> requestLeave(false))).colspan(2).fillX().row();
        }
        content.add(button("Exit game", "settings-exit", false, this::requestExit)).colspan(2).fillX().row();
        dialog.getButtonTable().add(button("Back to game", "settings-back", false, dialog::hide)).width(240).padTop(10);
        dialog.show(stage);
    }

    void requestExit() { requestLeave(true); }

    private void requestLeave(boolean exitGame) {
        if (exitDialog != null && exitDialog.getStage() != null) return;
        if (!hasActiveMatch() && screen != Screen.DECK) {
            if (exitGame) Gdx.app.exit(); else { matchStarted = false; showMenu(); }
            return;
        }
        Dialog confirm = dialog(exitGame ? "Exit game?" : "Leave this duel?");
        exitDialog = confirm;
        confirm.setName("exit-confirmation");
        Label warning = label(hasActiveMatch()
            ? "This duel is still in progress. Leaving will abandon it.\nSaved decks and completed matches will be kept."
            : "Unsaved deck changes will be lost.\nDecks already saved to your library will be kept.", "body");
        warning.setWrap(true);
        confirm.getContentTable().add(warning).width(545).pad(15);
        confirm.getButtonTable().add(button("Cancel", "exit-cancel", false, confirm::hide)).width(175).pad(8);
        confirm.getButtonTable().add(button(exitGame ? "Exit game" : "Leave duel", "exit-confirm", true, () -> {
            if (exitGame) Gdx.app.exit();
            else { confirm.hide(); matchStarted = false; message = ""; showMenu(); }
        })).width(210).pad(8);
        confirm.show(stage);
    }

    private void clear(Screen next, boolean battleBackdrop) {
        screen = next;
        stage.clear();
        audio.scene(battleBackdrop);
        Image background = new Image(battleBackdrop
            ? new TextureRegionDrawable(assets.scene.findRegion("bg"))
            : new TextureRegionDrawable(assets.portrait(heroes[deckPlayer])));
        background.setScaling(Scaling.fill);
        background.setBounds(0, 0, WIDTH, HEIGHT);
        if (battleBackdrop) stage.addActor(sourceScene);
        else stage.addActor(background);
        Image shade = new Image(assets.panel(new Color(.025f, .04f, .045f, battleBackdrop ? .22f : .7f)));
        shade.setBounds(0, 0, WIDTH, HEIGHT);
        shade.setTouchable(Touchable.disabled);
        stage.addActor(shade);
    }

    private Table root() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(24);
        stage.addActor(root);
        return root;
    }

    private Label label(String text, String style) { return new Label(text, assets.skin, style); }

    private Label muted(String text) { return label(text, "small"); }

    private Label heading(String text) { return label(text, "heading"); }

    private TextButton button(String text, String name, boolean primary, Runnable action) {
        TextButton result = new TextButton(text, assets.skin, primary ? "primary" : "duel");
        result.setName(name);
        result.pad(10, 16, 10, 16);
        result.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { audio.play("SOTE_SFX_UIClick_1_v2.wav"); safe(action); }
        });
        return result;
    }

    private void safe(Runnable action) {
        try { action.run(); }
        catch (RuntimeException exception) {
            message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            if (status != null) status.setText(message);
            Gdx.app.error("SpireDuel", message, exception);
        }
    }

    private ScrollPane scroll(Actor content, boolean horizontal) {
        ScrollPane result = new ScrollPane(content, assets.skin, "duel");
        result.setScrollingDisabled(!horizontal, horizontal);
        result.setFadeScrollBars(false);
        result.setOverscroll(false, false);
        result.setSmoothScrolling(true);
        return result;
    }

    private Table footer(String hint) {
        Table row = new Table();
        status = muted(message.isBlank() ? hint : message);
        status.setWrap(true);
        row.add(status).growX().left();
        return row;
    }

    private void showMenu() {
        deckPlayer = 0;
        clear(Screen.MENU, false);
        Table root = root();
        Table intro = new Table();
        intro.defaults().left();
        Label eyebrow = muted("SLAY THE SPIRE  /  LOCAL DUEL");
        eyebrow.setColor(GuiAssets.GOLD);
        intro.add(eyebrow).padBottom(16).row();
        intro.add(label("SPIRE DUEL", "title")).row();
        intro.add(heading("Two players. One battleground.")).padTop(12).row();
        Label description = label("Choose your class. Forge a 15-card deck.\nTake turns at the same keyboard and claim victory.", "body");
        description.setColor(GuiAssets.MUTED);
        intro.add(description).padTop(22).padBottom(38).row();
        intro.add(button("Begin a local duel", "start", true, () -> {
            decks.forEach(List::clear);
            message = "";
            filter = "ALL";
            showDeck();
        })).width(310).height(56).row();
        intro.add(button("Match history", "history", false, this::showHistory)).width(310).height(48).padTop(12).row();
        intro.add(button("Settings / Options", "settings", false, this::showSettings)).width(310).height(48).padTop(12).row();
        intro.add(button("Exit game", "exit", false, this::requestExit)).width(310).height(48).padTop(12).row();
        intro.add(muted("3 CLASSES     15 CARDS     1 SURVIVOR")).padTop(35).row();
        intro.add(muted(controller.cards().allCards().size() + " playable source cards in the current catalog")).padTop(9).row();
        root.add(intro).expand().left().padLeft(48);
        Table right = new Table();
        right.setBackground(assets.panel(new Color(.06f, .1f, .11f, .9f)));
        right.pad(26);
        right.add(heading("THE CHALLENGERS")).left().padBottom(20).row();
        for (Hero hero : Hero.values()) {
            Table heroRow = new Table();
            Image portrait = new Image(assets.avatar(hero));
            portrait.setScaling(Scaling.fit);
            heroRow.add(portrait).size(66).padRight(18);
            Table words = new Table();
            Label name = label(hero.displayName(), "body");
            name.setColor(GuiAssets.heroColor(hero));
            words.add(name).left().row();
            words.add(muted(hero.maxHp() + " HP  /  " + classColor(hero))).left().padTop(5);
            heroRow.add(words).left().growX();
            right.add(heroRow).width(270).padBottom(24).row();
        }
        right.add(muted("Pass the keyboard after every turn.\nYour hand stays hidden until you are ready.")).width(270).left();
        root.add(right).width(338).padRight(32);
        root.row();
        root.add(muted("A local PvP adaptation using Slay the Spire 1 art and card rules.")).colspan(2).left().padLeft(48).padTop(12);
    }

    private String classColor(Hero hero) {
        return switch (hero) { case IRONCLAD -> "RED"; case SILENT -> "GREEN"; case DEFECT -> "BLUE"; };
    }

    private void showDeck() {
        clear(Screen.DECK, false);
        Hero hero = heroes[deckPlayer];
        List<String> selected = decks.get(deckPlayer);
        Table root = root();
        Table header = new Table();
        header.add(heading("BUILD YOUR DECK")).left();
        Label step = label("PLAYER " + (deckPlayer + 1) + " OF 2", "body");
        step.setColor(GuiAssets.GOLD);
        header.add(step).expandX().right().padRight(20);
        header.add(button("Options", "settings", false, this::showSettings)).padRight(8);
        header.add(button("Main menu", "menu", false, this::showMenu));
        root.add(header).growX().height(48).padBottom(12).row();
        Table classes = new Table();
        for (Hero option : Hero.values()) {
            TextButton choice = button(option.displayName() + "  /  " + option.maxHp() + " HP", "hero-" + option.name(), option == hero, () -> {
                if (heroes[deckPlayer] != option) {
                    heroes[deckPlayer] = option;
                    deckNames[deckPlayer] = "Player " + (deckPlayer + 1) + " " + option.displayName();
                    selected.clear();
                    catalogScrollY = 0;
                    message = "";
                    showDeck();
                }
            });
            classes.add(choice).growX().height(46).padRight(10);
        }
        root.add(classes).growX().padBottom(14).row();
        Table body = new Table();
        Table catalog = new Table();
        Table filters = new Table();
        for (String type : List.of("ALL", "ATTACK", "SKILL", "POWER", "COLORLESS")) {
            filters.add(button(type, "filter-" + type, type.equals(filter), () -> {
                filter = type;
                catalogScrollY = 0;
                showDeck();
            })).growX().padRight(5).height(35);
        }
        catalog.add(filters).growX().padBottom(10).row();
        Table grid = new Table();
        grid.top().left();
        int i = 0;
        for (CardDefinition card : controller.cards().pool(hero)) {
            if (!filter.equals("ALL") && !(filter.equals("COLORLESS") ? card.hero() == null : card.type().toString().equals(filter))) continue;
            CardWidget widget = new CardWidget(assets, card, true, () -> safe(() -> {
                if (selected.size() >= 15) { message = "Your deck is full. Remove a card to make room."; status.setText(message); return; }
                selected.add(card.id());
                catalogScrollY = catalogScroll.getScrollY();
                message = "Added " + card.name() + ".";
                showDeck();
            }));
            widget.setName("add-" + card.id());
            grid.add(widget).width(155).height(221).pad(4);
            if (++i % 4 == 0) grid.row();
        }
        catalogScroll = scroll(grid, false);
        catalog.add(catalogScroll).grow();
        body.add(catalog).width(676).growY().padRight(18);
        Table deck = new Table();
        deck.setBackground(assets.panel(new Color(.065f, .1f, .115f, .96f)));
        deck.pad(18);
        Table count = new Table();
        count.add(heading("YOUR DECK")).left();
        Label total = heading(selected.size() + " / 15");
        total.setName("deck-count");
        total.setColor(selected.size() == 15 ? GuiAssets.GOLD : GuiAssets.PAPER);
        count.add(total).expandX().right();
        deck.add(count).growX().padBottom(6).row();
        deck.add(muted("Click a selected card to remove one copy.")).left().padBottom(10).row();
        Table selectedRows = new Table();
        selectedRows.top();
        Map<String, Long> grouped = selected.stream().collect(Collectors.groupingBy(id -> id, LinkedHashMap::new, Collectors.counting()));
        for (Map.Entry<String, Long> entry : grouped.entrySet()) {
            CardDefinition card = controller.cards().get(entry.getKey());
            TextButton row = button(entry.getValue() + "x   " + card.name() + "     -", "remove-" + card.id(), false, () -> {
                selected.remove(card.id());
                message = "Removed one " + card.name() + ".";
                showDeck();
            });
            row.getLabel().setAlignment(Align.left);
            selectedRows.add(row).growX().height(37).padBottom(4).row();
        }
        if (selected.isEmpty()) {
            Label empty = muted("Choose cards from your class pool\nand the shared colorless pool.\n\nOr start with the suggested 15 cards\nand adjust the list to your liking.");
            empty.setAlignment(Align.center);
            selectedRows.add(empty).width(425).padTop(38);
        }
        deck.add(scroll(selectedRows, false)).grow().minHeight(135).row();
        Table quick = new Table();
        quick.add(button("Starter deck", "starter", false, () -> {
            selected.clear(); selected.addAll(presetDeck(hero));
            message = "Starter deck ready. You can still replace cards.";
            showDeck();
        })).growX().padRight(8);
        quick.add(button("Clear", "clear-deck", false, () -> { selected.clear(); message = ""; showDeck(); })).width(95);
        deck.add(quick).growX().padTop(10).row();
        deckName = new TextField(deckNames[deckPlayer], assets.skin, "duel");
        deckName.setMaxLength(60);
        deckName.setMessageText("Deck name");
        deckName.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { deckNames[deckPlayer] = deckName.getText(); }
        });
        deck.add(deckName).growX().height(38).padTop(10).row();
        Table storage = new Table();
        TextButton save = button("Save deck", "save-deck", false, () -> {
            controller.saveDeck(deckName.getText().trim(), hero, List.copyOf(selected));
            message = "Saved \"" + deckName.getText().trim() + "\" to your library.";
            status.setText(message);
        });
        save.setDisabled(selected.size() != 15);
        storage.add(save).growX().padRight(8);
        storage.add(button("Load saved", "load-deck", false, this::showLoadDecks)).growX();
        deck.add(storage).growX().padTop(8).row();
        TextButton next = button(deckPlayer == 0 ? "Lock deck & pass to Player 2" : "Enter the arena", "deck-next", true, this::confirmDeck);
        next.setDisabled(selected.size() != 15);
        deck.add(next).growX().height(48).padTop(12);
        body.add(deck).grow();
        root.add(body).grow().row();
        root.add(footer("Choose exactly 15 cards. Duplicate cards are allowed.")).growX().height(38).padTop(8);
        root.validate();
        catalogScroll.setScrollY(catalogScrollY);
        catalogScroll.updateVisualScroll();
    }

    private void confirmDeck() {
        if (decks.get(deckPlayer).size() != 15) throw new IllegalArgumentException("A deck must contain exactly 15 cards.");
        message = "";
        filter = "ALL";
        catalogScrollY = 0;
        if (deckPlayer == 0) {
            deckPlayer = 1;
            showDeck();
        } else {
            if (smoke) controller.startMatch(heroes[0], List.copyOf(decks.get(0)), heroes[1], List.copyOf(decks.get(1)), 20261010L);
            else controller.startMatch(heroes[0], List.copyOf(decks.get(0)), heroes[1], List.copyOf(decks.get(1)));
            matchStarted = true;
            controller.battle().addObserver(snapshot -> battleDirty = true);
            showPass();
        }
    }

    private void showLoadDecks() {
        List<SavedDeck> saved = controller.loadDecks(heroes[deckPlayer]);
        Dialog dialog = dialog("Saved decks - " + heroes[deckPlayer].displayName());
        Table items = new Table();
        if (saved.isEmpty()) items.add(label("No saved decks for this class yet.", "body")).pad(20);
        for (SavedDeck deck : saved) {
            items.add(button(deck.name() + "  /  " + deck.cards().size() + " cards", "load-" + deck.id(), false, () -> {
                decks.get(deckPlayer).clear();
                decks.get(deckPlayer).addAll(deck.cards());
                deckNames[deckPlayer] = deck.name();
                dialog.hide();
                message = "Loaded \"" + deck.name() + "\".";
                showDeck();
            })).width(510).height(45).padBottom(6).row();
        }
        dialog.getContentTable().add(scroll(items, false)).width(550).height(Math.min(360, Math.max(85, saved.size() * 55)));
        dialog.getButtonTable().add(button("Close", "close-load", false, dialog::hide)).width(150).padTop(14);
        dialog.show(stage);
    }

    private Dialog dialog(String title) {
        Dialog dialog = new Dialog(title, assets.skin, "duel");
        dialog.pad(28);
        dialog.getContentTable().padTop(18);
        dialog.setModal(true);
        dialog.setMovable(false);
        return dialog;
    }

    private void showHistory() {
        List<MatchRecord> history = controller.history();
        Dialog dialog = dialog("Match history");
        Table items = new Table();
        if (history.isEmpty()) items.add(label("Your completed duels will appear here.", "body")).pad(24);
        for (MatchRecord match : history) {
            String result = match.winner() == 2 ? "Draw" : "Player " + (match.winner() + 1) + " wins";
            Table row = new Table();
            row.setBackground(assets.panel("111D20"));
            row.pad(12);
            row.add(label(match.player1().displayName() + "  vs  " + match.player2().displayName(), "body")).left().growX();
            Label outcome = label(result, "body"); outcome.setColor(GuiAssets.GOLD);
            row.add(outcome).right().row();
            row.add(muted(match.playedAt() + "   /   " + match.turns() + " turns")).colspan(2).left().padTop(7);
            items.add(row).width(650).padBottom(8).row();
        }
        dialog.getContentTable().add(scroll(items, false)).width(690).height(Math.min(440, Math.max(100, history.size() * 86)));
        dialog.getButtonTable().add(button("Close", "close-history", false, dialog::hide)).width(150).padTop(14);
        dialog.show(stage);
    }

    private void showPass() {
        clear(Screen.PASS, true);
        BattleSnapshot snapshot = controller.battle().snapshot();
        PlayerSnapshot player = snapshot.players().get(snapshot.activePlayer());
        Table root = root();
        Table panel = new Table();
        panel.setBackground(assets.panel(new Color(.065f, .1f, .11f, .98f)));
        panel.pad(44, 65, 44, 65);
        Label step = muted("TURN " + snapshot.turnNumber() + "  /  PASS THE KEYBOARD");
        step.setColor(GuiAssets.GOLD);
        panel.add(step).padBottom(18).row();
        Image avatar = new Image(assets.avatar(player.hero())); avatar.setScaling(Scaling.fit);
        panel.add(avatar).size(125).padBottom(16).row();
        panel.add(heading("Player " + (snapshot.activePlayer() + 1) + ", your turn")).padBottom(12).row();
        panel.add(label(player.hero().displayName() + "  /  " + player.hp() + " HP", "body")).padBottom(22).row();
        panel.add(muted("Your hand is hidden until you are ready.")).padBottom(28).row();
        panel.add(button("I am ready - reveal my hand", "ready", true, () -> { message = ""; showBattle(); animateDeal(); })).width(350).height(55).row();
        panel.add(button("Options", "settings", false, this::showSettings)).width(350).padTop(12);
        root.add(panel);
        battleDirty = false;
    }

    private void showBattle() {
        BattleSnapshot snapshot = controller.battle().snapshot();
        if (snapshot.winner() >= 0) { showVictory(); return; }
        clear(Screen.BATTLE, true);
        viewPlayer = snapshot.activePlayer(); playbackQueueIndex = 0; remainingQueued = snapshot.queue().size();
        Table root = root();
        Table header = new Table();
        header.add(heading("THE ARENA")).left();
        Label turn = label("TURN " + snapshot.turnNumber() + "  /  PLAYER " + (snapshot.activePlayer() + 1), "body");
        turn.setColor(GuiAssets.GOLD);
        header.add(turn).expandX().right().padRight(24);
        header.add(button("Match log", "match-log", false, this::showLog));
        header.add(button("Options", "settings", false, this::showSettings)).padLeft(8);
        root.add(header).growX().height(43).padBottom(12).row();
        Table combatants = new Table();
        combatants.add(playerPanel(snapshot, 0)).growX().height(180).padRight(12);
        combatants.add(playerPanel(snapshot, 1)).growX().height(180);
        root.add(combatants).growX().padBottom(12).row();
        Table queue = new Table();
        queue.setBackground(assets.panel(new Color(.035f, .065f, .08f, .94f)));
        queue.pad(12, 16, 12, 16);
        Table queueText = new Table();
        queueText.add(muted("RESOLUTION QUEUE")).left().row();
        Label queuedCount = label(snapshot.queue().size() + " cards selected", "body"); queuedCount.setName("queue-count");
        queueText.add(queuedCount).left().padTop(8).row();
        queueText.add(muted("Resolves in selection order.")).left().padTop(4);
        queue.add(queueText).width(227).left();
        Table queuedCards = new Table();
        queuedCards.left();
        if (snapshot.queue().isEmpty()) queuedCards.add(muted("Select cards below, then end your turn to resolve their effects.")).left();
        int queueTileIndex = 0;
        for (CardDefinition card : snapshot.queue()) {
            Table tile = new Table(); tile.setName("queue-tile-" + queueTileIndex++);
            tile.setBackground(assets.panel("263431"));
            tile.pad(6);
            Image art = new Image(assets.art(card)); art.setScaling(Scaling.fit);
            tile.add(art).size(48, 42).padRight(6);
            Label name = muted(card.name()); name.setColor(GuiAssets.PAPER); name.setWrap(true);
            tile.add(name).width(87);
            queuedCards.add(tile).height(61).padRight(7);
        }
        queue.add(scroll(queuedCards, true)).growX().height(77);
        root.add(queue).growX().height(109).padBottom(10).row();
        PlayerSnapshot player = snapshot.players().get(snapshot.activePlayer());
        Table handHeader = new Table();
        handHeader.add(heading("YOUR HAND")).left();
        handHeader.add(muted(player.hand().size() + " cards")).expandX().right().padRight(10);
        for (String pile : new String[]{"Draw", "Discard", "Exhaust"}) {
            int count = pile.equals("Draw") ? player.drawCount() : pile.equals("Discard") ? player.discardCount() : player.exhaustCount();
            handHeader.add(button(pile + " " + count, "pile-" + pile, false, () -> showPile(pile))).height(34).padLeft(5);
        }
        root.add(handHeader).growX().height(37).row();
        Table hand = new Table();
        hand.left();
        for (int i = 0; i < player.hand().size(); i++) {
            final int cardIndex = i;
            CardDefinition card = player.hand().get(i);
            CardWidget widget = new CardWidget(assets, card, false, () -> safe(() -> {
                audio.play("SOTE_SFX_CardSelect_v2.ogg");
                controller.queueCard(cardIndex);
                message = card.name() + " queued. Effects resolve when the turn ends.";
                showBattle();
            }));
            widget.setName("hand-" + i);
            if (card.cost() > player.energy()) widget.setColor(.58f, .58f, .58f, 1f);
            hand.add(widget).width(183).height(264).padRight(12);
        }
        if (player.hand().isEmpty()) hand.add(label("No cards left in hand. End your turn when ready.", "body")).pad(30);
        root.add(scroll(hand, true)).growX().height(280).row();
        Table bottom = footer("Click cards to queue them. Unplayed cards are discarded at the end of your turn.");
        Label energy = heading(player.energy() + " ENERGY"); energy.setColor(GuiAssets.GOLD);
        bottom.add(energy).padLeft(15).padRight(25);
        bottom.add(button("End turn  >", "end-turn", true, () -> {
            int eventStart = controller.battle().combatEvents().size();
            controller.endTurn();
            List<CombatEvent> events = controller.battle().combatEvents();
            for (CombatEvent event : events.subList(eventStart, events.size())) {
                if (event.kind().equals("TURN")) break; // Incoming hand is revealed only after the privacy screen.
                playback.add(event);
            }
            stage.getRoot().setTouchable(Touchable.disabled);
            resolving = true; battleDirty = false; resolutionDelay = 0;
            message = "";
        })).width(185).height(53);
        root.add(bottom).growX().height(60).padTop(7);
        battleDirty = false;
    }

    private void floating(String text, int player, Color color) {
        Label value = heading(text); value.setColor(color);
        value.setPosition(player == 0 ? 130 : 730, 625);
        value.setTouchable(Touchable.disabled); stage.addActor(value);
        value.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 65, .85f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(.85f)),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
    }
    private void flash(String region, int player, boolean shield) {
        TextureRegion texture = assets.vfx.findRegion(region);
        if (texture != null) stage.addActor(new SourceFlashActor(texture, player == 0 ? 145 : 740, 615, shield));
    }
    private void playFeedback(CombatEvent event) {
        int player = event.player();
        PlayerSnapshot state = event.state();
        hpLabels[player].setText(state.hp() + " / " + state.maxHp() + " HP");
        statLabels[player].setText("BLOCK " + state.block() + "     ENERGY " + state.energy());
        ((Table) hpFills[player].getParent()).getCell(hpFills[player]).width(295f * state.hp() / state.maxHp());
        powerLabels[player].setText(state.powers().entrySet().stream().map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(" | "))
            + (state.orbSlots() > 0 ? "\nOrbs " + String.join(" / ", state.orbs()) : ""));
        if (player == viewPlayer) {
            for (String pile : new String[]{"Draw", "Discard", "Exhaust"}) {
                TextButton button = stage.getRoot().findActor("pile-" + pile);
                if (button != null) button.setText(pile + " " + (pile.equals("Draw") ? state.drawCount() : pile.equals("Discard") ? state.discardCount() : state.exhaustCount()));
            }
        }
        resolutionDelay = .16f;
        switch (event.kind()) {
            case "CARD" -> {
                resolvingCard = event.card(); resolvingHit = 0; visualCardEvents++;
                Actor tile = stage.getRoot().findActor("queue-tile-" + playbackQueueIndex++);
                if (tile != null) tile.getColor().a = .2f;
                Label count = stage.getRoot().findActor("queue-count");
                if (count != null) count.setText(Math.max(0, --remainingQueued) + " cards remaining");
                status.setText("Resolving " + resolvingCard.name());
                CardWidget card = new CardWidget(assets, resolvingCard, false, () -> {});
                card.setTouchable(Touchable.disabled); card.setBounds(565, 337, 140, 202); stage.addActor(card);
                card.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(0, 45, .42f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(.42f)),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
                if (resolvingCard.type() == CardType.ATTACK) heroActors[player].attack();
                var metadata = sourceEffects.get(resolvingCard.baseId());
                if (metadata != null) for (var sound : metadata.getAsJsonArray("sounds")) audio.key(sound.getAsString());
                resolutionDelay = .2f;
            }
            case "DAMAGE" -> {
                visualDamageEvents++;
                if (event.amount() > 0 && event.name().equals("Attack")) heroActors[player].hit();
                floating(event.amount() > 0 ? "-" + event.amount() : "Blocked", player, event.amount() > 0 ? Color.SALMON : GuiAssets.PAPER);
                if (event.blocked() > 0) floating("Block -" + event.blocked(), player, Color.SKY);
                String effect = "NONE";
                if (event.name().equals("Attack") && resolvingCard != null && resolvingCard.type() == CardType.ATTACK) {
                    var metadata = sourceEffects.get(resolvingCard.baseId());
                    var effects = metadata.getAsJsonArray("effects");
                    if (resolvingCard.baseId().equals("Pummel")) effect = resolvingHit++ < resolvingCard.magic() - 1 ? "BLUNT_LIGHT" : "BLUNT_HEAVY";
                    else if (!effects.isEmpty()) effect = effects.get(Math.min(resolvingHit++, effects.size() - 1)).getAsString();
                    if (metadata.getAsJsonArray("vfx").toString().contains("LightningEffect")) flash("combat/lightning", player, false);
                } else if (event.name().equals("Poison")) effect = "POISON";
                else if (event.name().equals("Lightning")) { flash("combat/lightning", player, false); audio.key("ORB_LIGHTNING_PASSIVE"); }
                else if (event.name().equals("Dark")) flash("combat/orbFlareOuter", player, false);
                String region = switch (effect) {
                    case "SLASH_DIAGONAL" -> "slash_light"; case "SLASH_HEAVY" -> "slash_heavy";
                    case "SLASH_HORIZONTAL" -> "slash_horizontal"; case "SLASH_VERTICAL" -> "slash_vertical";
                    case "BLUNT_LIGHT" -> "blunt_light"; case "BLUNT_HEAVY" -> "blunt_heavy";
                    case "FIRE" -> "fire"; case "POISON" -> "poison"; default -> null;
                };
                if (region != null) flash("attack/" + region, player, false);
                audio.key(switch (effect) {
                    case "SLASH_HEAVY" -> "ATTACK_HEAVY"; case "BLUNT_LIGHT" -> "BLUNT_FAST";
                    case "BLUNT_HEAVY" -> "BLUNT_HEAVY"; case "FIRE" -> "ATTACK_FIRE";
                    case "POISON" -> "ATTACK_POISON"; case "NONE" -> ""; default -> "ATTACK_FAST";
                });
                resolutionDelay = .24f;
            }
            case "BLOCK" -> { flash("attack/shield", player, true); floating("+" + event.amount() + " Block", player, Color.SKY); audio.key("BLOCK_GAIN_1"); }
            case "POWER" -> {
                flash(event.name().equals("Poison") ? "attack/poison" : "buffVFX1", player, false);
                floating(event.name() + " " + (event.amount() > 0 ? "+" : "") + event.amount(), player, GuiAssets.GOLD);
                audio.key(switch (event.name()) { case "Poison" -> "POWER_POISON"; case "Strength" -> "POWER_STRENGTH";
                    case "Dexterity" -> "POWER_DEXTERITY"; case "Focus" -> "POWER_FOCUS"; default -> "BUFF_1"; });
            }
            case "ORB", "EVOKE" -> {
                flash("combat/orbFlareInner", player, false); floating(event.kind() + " " + event.name(), player, Color.SKY);
                audio.key("ORB_" + event.name() + (event.kind().equals("ORB") ? "_CHANNEL" : "_EVOKE"));
            }
            case "DRAW" -> { floating("Draw " + event.amount(), player, GuiAssets.PAPER); audio.key("CARD_DRAW_8"); }
            case "DISCARD" -> {
                Image back = new Image(new TextureRegionDrawable(assets.cardUi.findRegion("512/card_back")));
                back.setBounds(event.name().equals("Played") ? 600 : 450, event.name().equals("Played") ? 385 : 490, 55, 78);
                back.setTouchable(Touchable.disabled); stage.addActor(back);
                back.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(1040, 380, .28f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(.28f)),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
                resolutionDelay = .05f;
            }
            case "EXHAUST" -> { floating("Exhaust", player, Color.GRAY); audio.key("CARD_EXHAUST"); }
            case "ENERGY" -> floating("+" + event.amount() + " Energy", player, GuiAssets.GOLD);
            case "HEAL" -> floating("+" + event.amount() + " HP", player, Color.GREEN);
            case "SHUFFLE" -> floating("Shuffle", player, GuiAssets.PAPER);
            default -> { }
        }
    }

    private List<String> presetDeck(Hero hero) {
        if (smoke && "multi-hit".equals(System.getProperty("pvp.smokeScenario")))
            return java.util.Collections.nCopies(15, hero == Hero.IRONCLAD ? "Pummel" : "Strike_G");
        return controller.cards().starterDeck(hero);
    }

    private void showPile(String pile) {
        Dialog window = dialog(pile + " pile");
        Table cards = new Table();
        var contents = controller.battle().inspectPile(pile);
        for (int i = 0; i < contents.size(); i++) {
            cards.add(new CardWidget(assets, contents.get(i), false, () -> {})).width(155).height(221).pad(6);
            if ((i + 1) % 4 == 0) cards.row();
        }
        if (contents.isEmpty()) cards.add(muted("This pile is empty."));
        window.getContentTable().add(scroll(cards, false)).width(730).height(450).row();
        window.getContentTable().add(muted(pile.equals("Draw") ? "Sorted by name. The next draw remains hidden." : "Cards reflect the current battle state.")).pad(10);
        window.getButtonTable().add(button("Close", "close-pile", false, window::hide)).width(180);
        window.show(stage);
    }

    private void animateDeal() {
        audio.play("STS_SFX_CardDeal8_v1.ogg");
        for (int i = 0; i < controller.battle().snapshot().players().get(controller.battle().snapshot().activePlayer()).hand().size(); i++) {
            Actor card = stage.getRoot().findActor("hand-" + i);
            if (card != null) {
                card.getColor().a = 0;
                card.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(i * .06f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(.18f)));
            }
        }
    }

    private Table playerPanel(BattleSnapshot snapshot, int index) {
        PlayerSnapshot player = snapshot.players().get(index);
        boolean active = snapshot.activePlayer() == index;
        Table panel = new Table();
        panel.setBackground(assets.panel(new Color(.075f, .115f, .13f, .95f)));
        if (heroActors[index] == null || heroActors[index].hero != player.hero()) {
            if (heroActors[index] != null) heroActors[index].dispose();
            heroActors[index] = new SourceHeroActor(player.hero(), index == 1);
        }
        panel.add(heroActors[index]).width(162).height(173).padRight(14);
        Table stats = new Table();
        Label name = heading("P" + (index + 1) + "  " + player.hero().displayName());
        name.setColor(GuiAssets.heroColor(player.hero()));
        stats.add(name).left().padBottom(5).row();
        Label hp = label(player.hp() + " / " + player.maxHp() + " HP", "body");
        hpLabels[index] = hp;
        hp.setColor(GuiAssets.PAPER);
        stats.add(hp).left().row();
        Table bar = new Table(); bar.setBackground(assets.panel("1C0F11"));
        Image fill = new Image(assets.panel(GuiAssets.heroColor(player.hero())));
        hpFills[index] = fill;
        float portion = Math.max(0, (float) player.hp() / player.maxHp());
        bar.add(fill).width(295 * portion).height(7).left(); bar.add().growX();
        stats.add(bar).width(295).height(7).left().padTop(7).padBottom(8).row();
        statLabels[index] = muted("BLOCK " + player.block() + "     ENERGY " + player.energy() + "     " + (active ? "YOUR TURN" : "WAITING"));
        stats.add(statLabels[index]).left().row();
        String powers = player.powers().isEmpty() ? "No active powers" : player.powers().entrySet().stream()
            .map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining("  |  "));
        if (player.orbSlots() > 0) powers += "\nOrbs " + player.orbs().size() + "/" + player.orbSlots() + ": "
            + (player.orbs().isEmpty() ? "empty" : String.join(" / ", player.orbs()));
        Label effects = muted(powers); powerLabels[index] = effects; effects.setWrap(true);
        stats.add(effects).width(310).height(42).left().padTop(5);
        panel.add(stats).growX().left();
        return panel;
    }

    private void showLog() {
        Dialog dialog = dialog("Battle log");
        Table rows = new Table();
        for (String line : controller.battle().snapshot().log()) {
            Label text = label(line, "body"); text.setWrap(true);
            rows.add(text).width(730).left().padBottom(9).row();
        }
        ScrollPane pane = scroll(rows, false);
        dialog.getContentTable().add(pane).width(765).height(430);
        dialog.getButtonTable().add(button("Close", "close-log", false, dialog::hide)).width(150).padTop(12);
        dialog.show(stage);
        pane.layout(); pane.setScrollPercentY(1);
    }

    private void showVictory() {
        clear(Screen.VICTORY, true);
        BattleSnapshot snapshot = controller.battle().snapshot();
        Table root = root();
        Table panel = new Table();
        panel.setBackground(assets.panel(new Color(.065f, .1f, .11f, .98f)));
        panel.pad(42, 65, 42, 65);
        Label over = muted("DUEL COMPLETE"); over.setColor(GuiAssets.GOLD);
        panel.add(over).padBottom(20).row();
        String winner = snapshot.winner() == 2 ? "DRAW" : "PLAYER " + (snapshot.winner() + 1) + " WINS";
        panel.add(label(winner, "title")).padBottom(20).row();
        panel.add(label("A battle decided in " + snapshot.turnNumber() + " turns.", "body")).padBottom(20).row();
        for (int i = 0; i < snapshot.players().size(); i++) {
            PlayerSnapshot player = snapshot.players().get(i);
            panel.add(muted("Player " + (i + 1) + "  /  " + player.hero().displayName() + "  /  " + player.hp() + " HP")).padBottom(10).row();
        }
        String persistence = controller.persistenceWarning().isBlank()
            ? "The result has been saved to match history."
            : "Save warning: " + controller.persistenceWarning();
        Label saved = muted(persistence); saved.setWrap(true); saved.setAlignment(Align.center);
        panel.add(saved).width(570).padTop(14).padBottom(22).row();
        panel.add(button("Play again", "play-again", true, () -> {
            deckPlayer = 0; decks.forEach(List::clear); message = ""; showDeck();
        })).width(320).height(52).row();
        panel.add(button("Main menu", "victory-menu", false, this::showMenu)).width(320).height(46).padTop(10).row();
        panel.add(button("Options / Exit", "settings", false, this::showSettings)).width(320).height(46).padTop(10);
        root.add(panel);
        battleDirty = false;
    }

    @Override public void render() {
        if (!resolving && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (exitDialog != null && exitDialog.getStage() != null) exitDialog.hide();
            else if (settingsDialog != null && settingsDialog.getStage() != null) settingsDialog.hide();
            else showSettings();
        }
        if (resolving) {
            resolutionDelay -= Gdx.graphics.getDeltaTime();
            if (resolutionDelay <= 0) {
                if (!playback.isEmpty()) playFeedback(playback.remove());
                else {
                    resolving = false;
                    stage.getRoot().setTouchable(Touchable.enabled);
                    if (controller.battle().snapshot().winner() >= 0) showVictory(); else showPass();
                }
            }
        } else if (battleDirty && screen == Screen.BATTLE) showBattle();
        Gdx.gl.glClearColor(.025f, .04f, .045f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        if (smoke && resolving && visualDamageEvents > 0 && !screenshots.containsKey("gui-hit-feedback.png")) screenshot("gui-hit-feedback.png");
        if (smoke && resolving && visualCardEvents > 0 && !screenshots.containsKey("gui-card-feedback.png")) screenshot("gui-card-feedback.png");
        if (smoke && !resolving) {
            smokeTime += Gdx.graphics.getDeltaTime();
            float delay = !settingsSmokeDone || smokeStep < 19 ? .6f : .04f;
            if (smokeTime > delay) {
                smokeTime = 0;
                try { if (!settingsSmokeDone) settingsSmokeTick(); else smokeTick(); }
                catch (Throwable error) { failSmoke(error); }
            }
        }
    }

    /** Drive the real Scene2D actors; no global keyboard or mouse automation is used. */
    private void click(String name) {
        Actor actor = stage.getRoot().findActor(name);
        if (actor == null) throw new IllegalStateException("Missing GUI actor: " + name + " on " + screen);
        if (actor instanceof Button button && button.isDisabled()) throw new IllegalStateException("Disabled GUI actor: " + name);
        Vector2 point = actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
        InputEvent down = new InputEvent(); down.setType(InputEvent.Type.touchDown); down.setStageX(point.x); down.setStageY(point.y); down.setPointer(0); down.setButton(Input.Buttons.LEFT);
        actor.fire(down);
        InputEvent up = new InputEvent(); up.setType(InputEvent.Type.touchUp); up.setStageX(point.x); up.setStageY(point.y); up.setPointer(0); up.setButton(Input.Buttons.LEFT);
        actor.fire(up);
        smokeActions++;
    }

    private void smokeTick() {
        switch (smokeStep++) {
            case 0 -> { screenshot("gui-menu.png"); click("start"); }
            case 1 -> click("starter");
            case 2 -> {
                screenshot("gui-deck.png");
                deckName.setText("GUI smoke " + java.util.UUID.randomUUID().toString().substring(0, 8));
                click("save-deck");
                SavedDeck saved = controller.loadDecks(heroes[0]).stream().filter(d -> d.name().equals(deckName.getText()))
                    .findFirst().orElseThrow(() -> new AssertionError("GUI save did not persist the named deck"));
                smokeSavedDeckId = saved.id();
                if (!saved.cards().equals(decks.get(0))) throw new AssertionError("GUI save changed deck contents");
                smokeEvidence.add("Player 1 saved a 15-card deck through the GUI.");
            }
            case 3 -> click("clear-deck");
            case 4 -> {
                if (!decks.get(0).isEmpty()) throw new AssertionError("Clear deck button failed");
                click("load-deck");
            }
            case 5 -> click("load-" + smokeSavedDeckId);
            case 6 -> {
                if (!decks.get(0).equals(presetDeck(heroes[0]))) throw new AssertionError("GUI load did not restore saved cards");
                smokeEvidence.add("The saved deck was cleared, then restored through the GUI load dialog with all 15 cards intact.");
                click("deck-next");
            }
            case 7 -> click("hero-SILENT");
            case 8 -> click("starter");
            case 9 -> click("deck-next");
            case 10 -> { if (screen != Screen.PASS || stage.getRoot().findActor("hand-0") != null) throw new AssertionError("Pass screen leaked a hand"); screenshot("gui-pass.png"); click("ready"); }
            case 11 -> {
                BattleSnapshot snapshot = controller.battle().snapshot();
                smokeFirstHp = snapshot.players().get(1).hp();
                int index = playableIndex(snapshot);
                if (index < 0) throw new AssertionError("Starter hand has no playable card");
                click("hand-" + index);
            }
            case 12 -> {
                BattleSnapshot snapshot = controller.battle().snapshot();
                if (snapshot.queue().isEmpty() || snapshot.players().get(1).hp() != smokeFirstHp) throw new AssertionError("Queue did not defer card effects");
                screenshot("gui-queued.png");
                smokeEvidence.add("Selecting a hand card queued its effect without immediately damaging the opponent.");
                click("end-turn");
            }
            case 13 -> {
                if (screen != Screen.PASS || controller.battle().snapshot().activePlayer() != 1) throw new AssertionError("Hotseat did not switch to Player 2");
                smokeEvidence.add("Ending the turn resolved the queue and showed Player 2's privacy screen.");
                click("ready");
            }
            case 14 -> {
                screenshot("gui-smoke.png");
                if (!audio.playing() || !audio.warning.isBlank()) throw new AssertionError("Source music did not play: " + audio.warning);
                for (SourceHeroActor actor : heroActors) if (actor == null || actor.animationSeconds <= 0) throw new AssertionError("Hero skeleton did not update");
                smokeEvidence.add("Original Ironclad and Silent skeletons rendered and updated; source combat music is playing through the audio backend.");
                smokeRobot = new SourceHeroActor(Hero.DEFECT, false);
                smokeRobot.setPosition(565, 390); stage.addActor(smokeRobot);
                smokeEvidence.add("Player 2's hand was revealed after clicking ready.");
            }
            case 15 -> {
                screenshot("gui-defect-model.png");
                if (smokeRobot.animationSeconds <= 0) throw new AssertionError("Defect skeleton did not animate");
                smokeRobot.remove(); smokeRobot.dispose(); smokeRobot = null;
                smokeEvidence.add("The original Defect skeleton also loaded, rendered and animated in the real OpenGL window.");
                click("pile-Draw"); click("close-pile");
                smokeEvidence.add("Draw-pile viewer opened and closed through actor clicks.");
                click("settings");
            }
            case 16 -> click("settings-exit");
            case 17 -> {
                if (stage.getRoot().findActor("exit-confirmation") == null) throw new AssertionError("Active duel exit did not ask for confirmation");
                screenshot("gui-exit-confirmation.png");
                click("exit-cancel");
            }
            case 18 -> {
                if (controller.battle().snapshot().turnNumber() != 2 || controller.battle().snapshot().winner() >= 0) throw new AssertionError("Cancelling exit changed the duel");
                click("settings-back");
                smokeEvidence.add("Exit during a duel asked for confirmation; cancelling preserved the match.");
            }
            default -> autoFinishSmoke();
        }
    }

    private void settingsSmokeTick() {
        switch (settingsSmokeStep++) {
            case 0 -> { smokePreviousVsync = settings.vsync; click("settings"); }
            case 1 -> { screenshot("gui-settings.png"); click("setting-vsync"); }
            case 2 -> click("settings-apply");
            case 3 -> {
                DisplaySettings reloaded = new DisplaySettings(settings.file.getParent());
                if (reloaded.vsync == smokePreviousVsync || reloaded.vsync != settings.vsync) throw new AssertionError("Display settings were not saved/reloaded");
                smokeEvidence.add("Options changed vertical sync and persisted it to disk.");
                click("setting-vsync"); click("settings-apply");
            }
            case 4 -> { click("settings-back"); settingsSmokeDone = true; }
        }
    }

    private int playableIndex(BattleSnapshot snapshot) {
        PlayerSnapshot player = snapshot.players().get(snapshot.activePlayer());
        for (int i = 0; i < player.hand().size(); i++) {
            CardDefinition card = player.hand().get(i);
            if (card.cost() >= 0 && card.cost() <= player.energy()) return i;
        }
        return -1;
    }

    private void autoFinishSmoke() {
        BattleSnapshot snapshot = controller.battle().snapshot();
        if (screen == Screen.VICTORY) {
            screenshot("gui-victory.png");
            List<MatchRecord> history = controller.history();
            if (history.size() != Math.min(100, smokeInitialHistorySize + 1)
                || history.stream().noneMatch(m -> m.id() > smokeInitialLastMatchId && m.winner() == snapshot.winner() && m.turns() == snapshot.turnNumber()))
                throw new AssertionError("Completed GUI match was not added as a new database record");
            if ("multi-hit".equals(System.getProperty("pvp.smokeScenario"))) {
                long hits = controller.battle().combatEvents().stream().filter(e -> e.kind().equals("DAMAGE") && e.player() == 1 && e.name().equals("Attack")).count();
                if (hits != 33 || visualDamageEvents != 42) throw new AssertionError("Pummel per-hit feedback count mismatch: " + hits + " / " + visualDamageEvents);
                smokeEvidence.add("Pummel source multi-hit scenario: 33 individual Pummel hits and 9 opponent hits were played; lethal damage stopped the remaining hits.");
            }
            if (visualCardEvents == 0 || visualDamageEvents == 0 || java.util.Arrays.stream(heroActors).mapToInt(h -> h.hitCount).sum() == 0 || java.util.Arrays.stream(heroActors).mapToInt(h -> h.attackCount).sum() == 0)
                throw new AssertionError("Card/attack/hit feedback was not exercised");
            smokeEvidence.add("Resolved cards, source attack movement, per-hit VFX/numbers and Hit-to-Idle animations were exercised during the match.");
            smokeEvidence.add("The GUI reached victory through actor clicks; exactly one new matching record was added to database history.");
            writeEvidence(true, null);
            click("settings");
            click("settings-exit");
            return;
        }
        if (snapshot.turnNumber() > 250 || smokeActions > 2600) throw new AssertionError("GUI smoke exceeded its deterministic action limit");
        if (screen == Screen.PASS) { click("ready"); return; }
        if (screen != Screen.BATTLE) throw new AssertionError("Unexpected smoke screen " + screen);
        if (snapshot.turnNumber() != smokeLastTurn) {
            smokeLastTurn = snapshot.turnNumber();
            if (smokeLastTurn % 20 == 0) Gdx.app.log("GUI smoke", "Turn " + smokeLastTurn);
        }
        int index = playableIndex(snapshot);
        if (index >= 0) click("hand-" + index);
        else click("end-turn");
    }

    private FileHandle output(String filename) {
        return Gdx.files.absolute(java.nio.file.Path.of(System.getProperty("pvp.outputDir", "output"), filename).toAbsolutePath().toString());
    }

    private void screenshot(String name) {
        FileHandle path = output(name); path.parent().mkdirs();
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(path, pixmap, Deflater.DEFAULT_COMPRESSION, true); }
        finally { pixmap.dispose(); }
        screenshots.put(name, path.file().getAbsolutePath());
    }

    private void failSmoke(Throwable failure) {
        Gdx.app.error("GUI smoke", "Verification failed", failure);
        writeEvidence(false, failure.toString());
        Gdx.app.exit();
    }

    private void writeEvidence(boolean passed, String failure) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passed", passed);
        result.put("mechanism", "Scene2D touchDown/touchUp events on actual UI actors; OpenGL framebuffer screenshots");
        result.put("actions", smokeActions);
        result.put("scenario", System.getProperty("pvp.smokeScenario", "starter"));
        result.put("resolvedCardFeedback", visualCardEvents); result.put("damageFeedback", visualDamageEvents);
        result.put("checks", smokeEvidence);
        result.put("screenshots", screenshots);
        if (failure != null) result.put("failure", failure);
        if (matchStarted) {
            BattleSnapshot snapshot = controller.battle().snapshot();
            result.put("turn", snapshot.turnNumber()); result.put("winner", snapshot.winner());
            result.put("player1Hp", snapshot.players().get(0).hp()); result.put("player2Hp", snapshot.players().get(1).hp());
        }
        output("gui-smoke.json").writeString(new GsonBuilder().setPrettyPrinting().create().toJson(result), false, "UTF-8");
    }

    @Override public void resize(int width, int height) { if (stage != null) stage.getViewport().update(width, height, true); }

    @Override public void dispose() {
        if (stage != null) { stage.getBatch().dispose(); stage.dispose(); }
        if (audio != null) audio.dispose();
        if (smokeRobot != null) smokeRobot.dispose();
        for (SourceHeroActor actor : heroActors) if (actor != null) actor.dispose();
        if (assets != null) assets.dispose();
        if (controller != null) controller.close();
    }
}
