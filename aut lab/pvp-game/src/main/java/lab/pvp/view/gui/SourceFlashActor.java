package lab.pvp.view.gui;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Interpolation;

/** Reuses FlashAtkImgEffect's textures, 0.6s lifetime and shield descent. */
final class SourceFlashActor extends Actor {
    private final TextureRegion texture;
    private final boolean shield;
    private float age;
    SourceFlashActor(TextureRegion texture, float x, float y, boolean shield) {
        this.texture = texture; this.shield = shield;
        setBounds(x - texture.getRegionWidth() / 3f, y - texture.getRegionHeight() / 3f,
            texture.getRegionWidth() * .67f, texture.getRegionHeight() * .67f);
    }
    @Override public void act(float delta) { super.act(delta); age += delta; if (age >= .6f) remove(); }
    @Override public void draw(Batch batch, float parentAlpha) {
        float remaining = .6f - age;
        float alpha = shield ? remaining < .2f ? remaining * 5 : Interpolation.fade.apply(1, 0, remaining * .75f / .6f) : remaining / .6f;
        batch.setColor(1, 1, 1, Math.max(0, alpha) * parentAlpha);
        batch.draw(texture, getX(), getY() + (shield ? Interpolation.exp10In.apply(0, 53, remaining / .6f) : 0), getWidth(), getHeight());
        batch.setColor(1, 1, 1, 1);
    }
}
