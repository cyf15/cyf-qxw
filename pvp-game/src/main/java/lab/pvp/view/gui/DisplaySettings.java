package lab.pvp.view.gui;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/** Local display preferences; they never change combat rules. */
final class DisplaySettings {
    final Path file;
    boolean fullscreen;
    float musicVolume = .35f, soundVolume = .65f;
    boolean vsync = true;
    int width = 1280;
    int height = 800;

    DisplaySettings(Path dataDirectory) {
        file = dataDirectory.resolve("settings.properties");
        Properties values = new Properties();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                values.load(reader);
                musicVolume = volume(values.getProperty("musicVolume", ".35"));
                soundVolume = volume(values.getProperty("soundVolume", ".65"));
                fullscreen = Boolean.parseBoolean(values.getProperty("fullscreen", "false"));
                vsync = Boolean.parseBoolean(values.getProperty("vsync", "true"));
                int w = Integer.parseInt(values.getProperty("width", "1280"));
                int h = Integer.parseInt(values.getProperty("height", "800"));
                if (w >= 960 && w <= 7680 && h >= 600 && h <= 4320) { width = w; height = h; }
            } catch (IOException | IllegalArgumentException ignored) { /* Safe display defaults for malformed preferences. */ }
        }
    }

    private static float volume(String value) { float result = Float.parseFloat(value); return Float.isFinite(result) ? Math.max(0, Math.min(1, result)) : 0; }

    void save() {
        Properties values = new Properties();
        values.setProperty("musicVolume", Float.toString(musicVolume));
        values.setProperty("soundVolume", Float.toString(soundVolume));
        values.setProperty("fullscreen", Boolean.toString(fullscreen));
        values.setProperty("vsync", Boolean.toString(vsync));
        values.setProperty("width", Integer.toString(width));
        values.setProperty("height", Integer.toString(height));
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path temporary = file.resolveSibling("settings.properties.tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) { values.store(writer, "Spire Duel display settings"); }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new IllegalStateException("Could not save settings: " + e.getMessage(), e); }
    }
}
