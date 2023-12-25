package theGame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import theGame.MainGame;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument

/**
 * 主机端入口
 * yehu1999
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("TileMapGame");
		config.setWindowedMode(1240, 720);
		new Lwjgl3Application(new MainGame(), config);
	}
}
