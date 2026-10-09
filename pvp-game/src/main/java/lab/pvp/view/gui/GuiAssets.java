package lab.pvp.view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import lab.pvp.model.CardDefinition;
import lab.pvp.model.Hero;

import java.util.HashMap;
import java.util.Map;

/** All art and fonts are loaded from the existing MySlayTheSpire resource tree. */
final class GuiAssets implements Disposable {
    static final Color INK = Color.valueOf("101619");
    static final Color PANEL = Color.valueOf("182124");
    static final Color PAPER = Color.valueOf("EDE7D7");
    static final Color MUTED = Color.valueOf("B0B7AC");
    static final Color GOLD = Color.valueOf("D7B975");
    final Skin skin;
    final TextureAtlas cards;
    final TextureAtlas cardUi;
    final TextureAtlas scene;
    final TextureAtlas vfx;
    final BitmapFont body, small, title, heading;
    private final Map<String, Texture> textures = new HashMap<>();

    GuiAssets() {
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        cards = new TextureAtlas(Gdx.files.internal("cards/cards.atlas"));
        cardUi = new TextureAtlas(Gdx.files.internal("cardui/cardui.atlas"));
        vfx = new TextureAtlas(Gdx.files.internal("vfx/vfx.atlas"));
        scene = new TextureAtlas(Gdx.files.internal("bottomScene/scene.atlas"));
        body = font("font/NotoSans-Regular.ttf", 18);
        small = font("font/NotoSans-Regular.ttf", 14);
        title = font("font/Kreon-Bold.ttf", 60);
        heading = font("font/Kreon-Bold.ttf", 28);
        skin.add("body", body);
        skin.add("small", small);
        skin.add("title", title);
        skin.add("heading", heading);
        skin.add("body", new Label.LabelStyle(body, PAPER));
        skin.add("small", new Label.LabelStyle(small, MUTED));
        skin.add("heading", new Label.LabelStyle(heading, PAPER));
        skin.add("title", new Label.LabelStyle(title, PAPER));
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle();
        button.font = body;
        button.fontColor = PAPER;
        button.up = panel("293630");
        button.over = panel("41523F");
        button.down = panel("566342");
        button.disabled = panel("22292B");
        button.disabledFontColor = Color.valueOf("626C69");
        skin.add("duel", button);
        TextButton.TextButtonStyle primary = new TextButton.TextButtonStyle(button);
        primary.up = panel("B89956");
        primary.over = panel("D5B876");
        primary.down = panel("8B703C");
        primary.fontColor = INK;
        skin.add("primary", primary);
        TextField.TextFieldStyle field = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        field.font = body;
        field.fontColor = PAPER;
        field.background = panel("11191C");
        skin.add("duel", field);
        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        scroll.vScroll = panel("192326");
        scroll.vScrollKnob = panel("6C7568");
        scroll.hScroll = panel("192326");
        scroll.hScrollKnob = panel("6C7568");
        scroll.vScroll.setMinWidth(8);
        scroll.vScrollKnob.setMinWidth(8);
        scroll.hScroll.setMinHeight(8);
        scroll.hScrollKnob.setMinHeight(8);
        skin.add("duel", scroll);
        Window.WindowStyle window = new Window.WindowStyle(heading, PAPER, panel("1B272A"));
        window.stageBackground = panel(new Color(0, 0, 0, .8f));
        skin.add("duel", window);
    }

    private BitmapFont font(String path, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont result = generator.generateFont(parameter);
        generator.dispose();
        return result;
    }

    Texture texture(String path) {
        return textures.computeIfAbsent(path, key -> {
            Texture result = new Texture(Gdx.files.internal(key));
            result.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return result;
        });
    }

    Drawable panel(String hex) { return panel(Color.valueOf(hex)); }

    Drawable panel(Color tint) {
        // Tint an existing source UI texture; never generate a replacement bitmap.
        TextureRegionDrawable source = new TextureRegionDrawable(new TextureRegion(texture("images/whiteSquare32.png")));
        return source.tint(tint);
    }

    TextureRegion portrait(Hero hero) {
        return new TextureRegion(texture("images/ui/charSelect/" + switch (hero) {
            case IRONCLAD -> "ironclad";
            case SILENT -> "silent";
            case DEFECT -> "defect";
        } + "Portrait.jpg"));
    }

    TextureRegion avatar(Hero hero) {
        return new TextureRegion(texture("images/ui/charSelect/" + switch (hero) {
            case IRONCLAD -> "ironclad";
            case SILENT -> "silent";
            case DEFECT -> "defect";
        } + "Button.png"));
    }

    TextureRegion art(CardDefinition card) {
        String path = card.artPath().replace('\\', '/');
        if (path.startsWith("images/1024Portraits/") && Gdx.files.internal(path).exists())
            return new TextureRegion(texture(path));
        path = path.replace("images/512/", "").replace("images/1024Portraits/", "").replace(".png", "");
        TextureAtlas.AtlasRegion region = cards.findRegion(path);
        return region != null ? region : cardUi.findRegion("512/card_back");
    }

    Drawable cardFrame(CardDefinition card) {
        String kind = card.type().toString().toLowerCase();
        String color = card.hero() == null ? "gray" : switch (card.hero()) {
            case IRONCLAD -> "red";
            case SILENT -> "green";
            case DEFECT -> "blue";
        };
        TextureAtlas.AtlasRegion region = cardUi.findRegion("512/bg_" + kind + "_" + color);
        if (region == null) region = cardUi.findRegion("512/card_bg");
        return new TextureRegionDrawable(region);
    }

    static Color heroColor(Hero hero) {
        return Color.valueOf(switch (hero) {
            case IRONCLAD -> "D67458";
            case SILENT -> "8EB66C";
            case DEFECT -> "77B6DA";
        });
    }

    @Override public void dispose() {
        skin.dispose();
        cards.dispose();
        cardUi.dispose();
        scene.dispose(); vfx.dispose();
        textures.values().forEach(Texture::dispose);
    }
}
