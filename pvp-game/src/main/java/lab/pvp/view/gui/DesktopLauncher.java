package lab.pvp.view.gui;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.Gdx;

/** Desktop entry point. On macOS launch the JVM with -XstartOnFirstThread. */
public final class DesktopLauncher {
    private DesktopLauncher() { }

    public static void main(String[] args) {
        GameApplication game = new GameApplication();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Spire Duel | Local PvP");
        config.setWindowedMode(1280, 800);
        config.setWindowSizeLimits(960, 600, -1, -1);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.disableAudio(false);
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override public boolean closeRequested() {
                Gdx.app.postRunnable(game::requestExit);
                return false;
            }
        });
        new Lwjgl3Application(game, config);
    }
}
