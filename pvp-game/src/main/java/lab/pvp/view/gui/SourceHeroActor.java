package lab.pvp.view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.esotericsoftware.spine.*;
import lab.pvp.model.Hero;

/** Original 3.4.02 skeleton and runtime, including deformable mesh attachments. */
final class SourceHeroActor extends Actor implements Disposable {
    final Hero hero;
    private final TextureAtlas atlas;
    private final Skeleton skeleton;
    private final AnimationState state;
    private final SkeletonMeshRenderer renderer = new SkeletonMeshRenderer();
    private final boolean mirrored;
    private float attackTimer, attackOffset;
    int attackCount, hitCount;
    float animationSeconds;

    SourceHeroActor(Hero hero, boolean mirrored) {
        this.hero = hero;
        this.mirrored = mirrored;
        String folder = "images/characters/" + switch (hero) {
            case IRONCLAD -> "ironclad"; case SILENT -> "theSilent"; case DEFECT -> "defect";
        } + "/idle/skeleton";
        atlas = new TextureAtlas(Gdx.files.internal(folder + ".atlas"));
        SkeletonJson reader = new SkeletonJson(atlas);
        reader.setScale(.55f);
        SkeletonData data = reader.readSkeletonData(Gdx.files.internal(folder + ".json"));
        skeleton = new Skeleton(data);
        AnimationStateData transitions = new AnimationStateData(data);
        transitions.setDefaultMix(.1f);
        state = new AnimationState(transitions);
        state.setAnimation(0, "Idle", true);
        setSize(162, 173);
    }

    void attack() { attackTimer = .4f; attackCount++; }
    void hit() {
        hitCount++;
        state.setAnimation(0, "Hit", false).setTimeScale(.6f);
        state.addAnimation(0, "Idle", true, 0);
    }

    @Override public void act(float delta) {
        super.act(delta);
        animationSeconds += delta;
        if (attackTimer > 0) {
            attackTimer = Math.max(0, attackTimer - delta);
            attackOffset = com.badlogic.gdx.math.Interpolation.fade.apply(0, 60, attackTimer * 2) * (mirrored ? -1 : 1);
        } else attackOffset = 0;
        state.update(delta);
        state.apply(skeleton);
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        skeleton.setFlipX(mirrored);
        skeleton.setPosition(getX() + (hero == Hero.IRONCLAD ? getWidth() * .8f : getWidth() / 2) + attackOffset, getY() + 12);
        skeleton.getColor().set(1, 1, 1, parentAlpha);
        skeleton.updateWorldTransform();
        renderer.draw((PolygonSpriteBatch) batch, skeleton);
    }

    @Override public void dispose() { atlas.dispose(); }
}
