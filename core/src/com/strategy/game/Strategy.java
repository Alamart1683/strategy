package com.strategy.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.strategy.game.map.Map;
import com.strategy.game.map.daemon.ForestChange;
import com.strategy.game.map.daemon.GrassChange;
import com.strategy.game.map.daemon.SeasonChangeDaemonTask;
import com.strategy.game.map.terrain.Season;
import com.strategy.game.map.daemon.TileChange;

import java.util.Timer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.Timer;

public class Strategy extends ApplicationAdapter {
	Map map;
	TileChange tileChange;
	ForestChange forestChange;
	GrassChange grassChange;
	SeasonChangeDaemonTask seasonChangeDaemonTask;
	Timer seasonChangeDemon;

	private IsometricTiledMapRenderer renderer; // Изометрический рендерер
	private OrthographicCamera camera;
	private AssetManager assetManager;
	private Texture tiles;
	private Texture texture;
	private BitmapFont font;
	private SpriteBatch batch;
	private SpriteBatch gui;
	private Season currentSeason;
	private String climate;

	// Переменные для отслеживания состояния клавиш
	private boolean upPressed, downPressed, leftPressed, rightPressed;
	private float cameraSpeed = 200; // Настройка скорости камеры

	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 900, 900);
		camera.update();

		font = new BitmapFont();
		batch = new SpriteBatch();
		gui = new SpriteBatch();

		currentSeason = Season.Summer;
		climate = "temperate";

		map = new Map(
				15,
				15,
				64,
				64,
				new Texture("assets/tiles/climate/temperate/plain_temperate_seasons64.png"),
				currentSeason
		);

		tileChange = new TileChange(map, 4);
		forestChange = new ForestChange(map, climate, currentSeason, batch);

		// grassChange = new GrassChange(map, climate, currentSeason, 3);

		seasonChangeDaemonTask = new SeasonChangeDaemonTask(tileChange, forestChange);
		seasonChangeDemon = new Timer();
		seasonChangeDemon.schedule(seasonChangeDaemonTask, 0, 300);

		renderer = new IsometricTiledMapRenderer(map.getMap(), 1f); // Изометрический рендерер и масштаб

		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean scrolled(float amountX, float amountY) {
				camera.zoom += amountY * 0.1f;
				camera.update();
				return true;
			}

			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
					case Input.Keys.UP:
						upPressed = true;
						break;
					case Input.Keys.DOWN:
						downPressed = true;
						break;
					case Input.Keys.LEFT:
						leftPressed = true;
						break;
					case Input.Keys.RIGHT:
						rightPressed = true;
						break;
				}
				return true;
			}

			@Override
			public boolean keyUp(int keycode) {
				switch (keycode) {
					case Input.Keys.UP:
						upPressed = false;
						break;
					case Input.Keys.DOWN:
						downPressed = false;
						break;
					case Input.Keys.LEFT:
						leftPressed = false;
						break;
					case Input.Keys.RIGHT:
						rightPressed = false;
						break;
				}
				return true;
			}
		});
	}

	@Override
	public void render() {
		ScreenUtils.clear(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		float deltaTime = Gdx.graphics.getDeltaTime();
		updateCamera(deltaTime);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.setProjectionMatrix(camera.combined);
		seasonChangeDaemonTask.renderPlants();
		gui.begin();
		font.draw(gui, "Year: " + seasonChangeDaemonTask.getYear(), 10, 40);
		font.draw(gui, "Season: " + seasonChangeDaemonTask.getCurrentSeason() + " iteration: " + seasonChangeDaemonTask.getCurrentIter(), 10, 20);
		gui.end();
	}

	private void updateCamera(float deltaTime) {
		// Адаптированный метод обновления камеры для изометрической геометрии
		float cameraMoveSpeed = cameraSpeed * deltaTime;

		if (upPressed) {
			camera.translate(-cameraMoveSpeed, cameraMoveSpeed);
		}
		if (downPressed) {
			camera.translate(cameraMoveSpeed, -cameraMoveSpeed);
		}
		if (leftPressed) {
			camera.translate(-cameraMoveSpeed, -cameraMoveSpeed);
		}
		if (rightPressed) {
			camera.translate(cameraMoveSpeed, cameraMoveSpeed);
		}
	}

	@Override
	public void dispose() {
		// Убедитесь, что освобождаете все ресурсы
		batch.dispose();
		gui.dispose();
		font.dispose();
		if (tiles != null) {
			tiles.dispose();
		}
		if (texture != null) {
			texture.dispose();
		}
		if (assetManager != null) {
			assetManager.dispose();
		}
	}
}
