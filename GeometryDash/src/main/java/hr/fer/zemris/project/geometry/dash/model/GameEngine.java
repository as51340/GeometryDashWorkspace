package hr.fer.zemris.project.geometry.dash.model;

import java.util.ArrayList;

import hr.fer.zemris.project.geometry.dash.model.math.Vector2D;
import hr.fer.zemris.project.geometry.dash.model.drawables.player.Player;
import hr.fer.zemris.project.geometry.dash.model.settings.GameConstants;
import hr.fer.zemris.project.geometry.dash.model.settings.Settings;
import hr.fer.zemris.project.geometry.dash.model.settings.music.SoundSystem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 * Engine that will be used in implementation of game <strong>Geometry dash</strong>
 * It defines only properties that are connected directly with running game, it doesn't have any specific
 * connection with geometry dash, so it means on this "platform" can also be other games played
 * @author Andi �krgat
 *
 */
public class GameEngine implements SoundSystem{
	
	/**
	 * Frames per second, default value is 60
	 */
	private int fps;
	
	/**
	 * Game's title
	 */
	private String title;
	
	/**
	 * Maybe here, maybe somewhere else
	 */
	private int width;
	
	/**
	 * Maybe here, maybe somewhere else
	 */
	private int height;
	
	/**
	 * Reference to game loop 
	 */
	private Timeline gameLoop;
	
	/**
	 * Game world
	 */
	private GameWorld gameWorld;
	
	/**
	 * {@linkplain Settings}
	 */
	private Settings settings;
	
	/**
	 * Basic constructor that sets game's title
	 * Creates game loop and event handler
	 * @param title Game's title
	 */
	public GameEngine(int fps, String title, int width, int height) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.fps = fps;;
		settings = new Settings();
		gameWorld = new GameWorld(); //for now list of obstacles is empty, not focus on that currently
		createGameLoop();
	}

	/**
	 * @return the gameWorld
	 */
	public GameWorld getGameWorld() {
		return gameWorld;
	}
	
	/**
	 * @return the fps
	 */
	public int getFps() {
		return fps;
	}

	/**
	 * @param fps the fps to set
	 */
	public void setFps(int fps) {
		this.fps = fps;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the gameLoop
	 */
	public Timeline getGameLoop() {
		return gameLoop;
	}
	
	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * Starts game loop
	 */
	public void start() {
		gameLoop.play();
	}
	
	/**
	 * @return {@linkplain KeyFrame} - sets fps and directly update time + event handler
	 */
	private KeyFrame createKeyFrame() {
		Duration frameTime = Duration.millis(1000.0/getFps()); //for 60 FPS and that is usually standard
		Vector2D camDir = new Vector2D(1,0);
        camDir.scale(GameConstants.timeBetweenUpdates * 50f);
		//time between update will be approx. 16.67ms, for 10ms we have to provide 100 fps as value
        KeyFrame keyFrame = new KeyFrame(frameTime, new EventHandler<ActionEvent>() {
   
			@Override
			public void handle(ActionEvent event) {
				gameWorld.getRenderer().render();
			}
        	
        });
        return keyFrame;
	}
	
	/**
	 * Initializes {@linkplain Timeline} object for looping through game
	 */
	private void createGameLoop() {
		gameLoop = new Timeline();
		gameLoop.setCycleCount(Animation.INDEFINITE);
		gameLoop.getKeyFrames().add(createKeyFrame());
	}
	
	/**
	 * Initializes stage from game-engine data
	 * @param stage {@linkplain Stage}
	 */
	public void createStageFromData(Stage stage) {
		stage.setTitle(this.title);
		stage.setWidth(this.width);
		stage.setHeight(this.height);
	}

	@Override
	public void playKillSound() {
		//TODO
	}
	
}
