package lab.pvp.view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.Disposable;
import java.util.*;

/** Paths follow SoundMaster / MainMusic. All recordings come from the source project. */
final class SourceAudio implements Disposable {
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, String> paths = new com.google.gson.Gson().fromJson(Gdx.files.internal("catalog/sounds.json").readString(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
    private Music music, ambience;
    private String track;
    float musicVolume = .35f, soundVolume = .65f;
    String warning = "";
    void scene(boolean battle) {
        String next = "audio/music/" + (battle ? "STS_Level1_NewMix_v1.ogg" : "STS_MenuTheme_NewMix_v1.ogg");
        if (next.equals(track)) return;
        try {
            if (music != null) music.dispose();
            if (ambience != null) { ambience.dispose(); ambience = null; }
            music = Gdx.audio.newMusic(Gdx.files.internal(next));
            track = next; music.setLooping(true); music.setVolume(musicVolume); music.play();
            if (battle) {
                ambience = Gdx.audio.newMusic(Gdx.files.internal("audio/sound/SOTE_Level1_Ambience_v6.ogg"));
                ambience.setLooping(true); ambience.setVolume(soundVolume * .25f); ambience.play();
            }
        } catch (RuntimeException e) { warning = "Audio unavailable: " + e.getMessage(); Gdx.app.error("Audio", warning); }
    }
    void volumes(float musicVolume, float soundVolume) {
        this.musicVolume = musicVolume; this.soundVolume = soundVolume;
        if (music != null) music.setVolume(musicVolume);
        if (ambience != null) ambience.setVolume(soundVolume * .25f);
    }
    void key(String key) { String file = paths.get(key); if (file != null) play(file); }
    void play(String file) {
        try { sounds.computeIfAbsent(file, key -> Gdx.audio.newSound(Gdx.files.internal("audio/sound/" + key))).play(soundVolume); }
        catch (RuntimeException e) { warning = "Audio unavailable: " + e.getMessage(); Gdx.app.error("Audio", warning); }
    }
    boolean playing() { return music != null && music.isPlaying(); }
    @Override public void dispose() {
        sounds.values().forEach(Sound::dispose);
        if (music != null) music.dispose(); if (ambience != null) ambience.dispose();
    }
}
