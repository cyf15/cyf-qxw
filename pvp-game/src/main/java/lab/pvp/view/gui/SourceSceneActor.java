package lab.pvp.view.gui;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Interpolation;
import java.util.*;

/** Preserve the trimmed atlas offsets used by TheBottomScene, rather than stretching its patches. */
final class SourceSceneActor extends Actor {
    private final TextureAtlas atlas;
    private final TextureAtlas vfx;
    private final List<Particle> fog = new ArrayList<>(), dust = new ArrayList<>();
    SourceSceneActor(TextureAtlas atlas, TextureAtlas vfx) { this.atlas = atlas; this.vfx = vfx; setSize(1280, 800); }
    private final class Particle {
        final TextureRegion image;
        float x, y, vx, vy, rotation, rotationSpeed, age, duration, scale, tint;
        final boolean smoke;
        Particle(boolean smoke) {
            this.smoke = smoke;
            image = new TextureRegion(vfx.findRegion("env/" + (smoke ? "smoke" + MathUtils.random(1, 3) : "dust" + MathUtils.random(1, 6))));
            if (smoke) image.flip(MathUtils.randomBoolean(), MathUtils.randomBoolean());
            duration = smoke ? MathUtils.random(10f, 12f) : 6;
            x = MathUtils.random(-130f, 1410f); y = smoke ? MathUtils.random(450f, 700f) : MathUtils.random(130f, 600f);
            vx = smoke ? MathUtils.random(-133f, 133f) : MathUtils.random(-8f, 8f);
            vy = smoke ? 0 : MathUtils.random(-8f, 20f);
            rotationSpeed = smoke ? MathUtils.random(-10f, 10f) : MathUtils.random(-120f, 120f);
            scale = smoke ? MathUtils.random(2.6f, 4f) : .67f;
            tint = smoke ? MathUtils.random(.1f, .15f) : MathUtils.random(.1f, .7f);
        }
        void update(float delta) { age += delta; x += vx * delta; y += vy * delta; rotation += rotationSpeed * delta; if (smoke) scale += delta / 3; }
        void draw(Batch batch) {
            float fade = Math.min(1, Math.min(age / (smoke ? 5 : 3), (duration - age) / (smoke ? 5 : 3)));
            batch.setColor(tint, tint, tint, Interpolation.fade.apply(Math.max(0, fade)) * (smoke ? .3f : 1 - tint));
            batch.draw(image, x, y, image.getRegionWidth() / 2f, image.getRegionHeight() / 2f,
                image.getRegionWidth(), image.getRegionHeight(), scale, scale, rotation);
        }
    }
    @Override public void act(float delta) {
        super.act(delta);
        for (List<Particle> list : List.of(fog, dust)) {
            for (Particle particle : list) particle.update(delta);
            list.removeIf(p -> p.age >= p.duration);
        }
        if (fog.size() < 50) fog.add(new Particle(true));
        if (dust.size() < 96) dust.add(new Particle(false));
    }
    @Override public void draw(Batch batch, float parentAlpha) {
        batch.setColor(1, 1, 1, parentAlpha);
        for (String name : new String[]{"bg", "mod/mg", "mod/midWall", "mod/mod2", "mod/mod1", "mod/ceiling", "mod/ceilingMod2", "mod/fg"}) {
            TextureAtlas.AtlasRegion region = atlas.findRegion(name);
            if (region != null) batch.draw(region, region.offsetX * getWidth() / 1920f,
                region.offsetY * getHeight() / 1080f, region.packedWidth * getWidth() / 1920f,
                region.packedHeight * getHeight() / 1080f);
            if (name.equals("bg")) { for (Particle p : fog) p.draw(batch); batch.setColor(1, 1, 1, parentAlpha); }
        }
        for (Particle p : dust) p.draw(batch);
        batch.setColor(1, 1, 1, 1);
    }
}
