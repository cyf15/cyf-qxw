package lab.pvp.view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import lab.pvp.model.CardDefinition;

/** Source card frame and illustration, with live rules text overlaid by Scene2D. */
final class CardWidget extends Table {
    CardWidget(GuiAssets assets, CardDefinition card, boolean compact, Runnable action) {
        setBackground(assets.cardFrame(card));
        pad(compact ? 10 : 13);
        float width = compact ? 152 : 180;
        Label name = new Label(card.name(), assets.skin, "body");
        name.setFontScale(compact ? .77f : .85f);
        name.setColor(GuiAssets.PAPER);
        name.setWrap(true);
        name.setAlignment(Align.center);
        Label cost = new Label(card.cost() < 0 ? "X" : Integer.toString(card.cost()), assets.skin, "heading");
        cost.setColor(GuiAssets.GOLD);
        Table top = new Table();
        top.add(cost).width(22);
        top.add(name).width(width - (compact ? 50 : 54)).height(compact ? 34 : 42);
        add(top).width(width - 20).row();
        Image art = new Image(assets.art(card));
        art.setScaling(Scaling.fit);
        add(art).width(width - 22).height(compact ? 72 : 88).padTop(3).row();
        Label kind = new Label(card.type().toString(), assets.skin, "small");
        kind.setFontScale(.76f);
        kind.setColor(GuiAssets.GOLD);
        add(kind).padTop(3).row();
        Label description = new Label(card.description(), assets.skin, "small");
        description.setColor(Color.valueOf("F4EBCE"));
        description.setFontScale(compact ? .84f : .97f);
        description.setAlignment(Align.center);
        description.setWrap(true);
        add(description).width(width - 25).height(compact ? 55 : 75).padTop(3).top();
        addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { action.run(); }
        });
        addListener(new InputListener() {
            @Override public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) setColor(1.0f, 1.0f, .88f, 1.0f);
            }
            @Override public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) setColor(Color.WHITE);
            }
        });
    }
}
