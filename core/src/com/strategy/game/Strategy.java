package com.strategy.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.strategy.game.map.Map;
import com.strategy.game.map.Season;
import com.strategy.game.map.daemon.SeasonChange;

import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Strategy extends ApplicationAdapter {
	Map map;
	SeasonChange seasonChange;
	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private CameraInputController cameraController;
	private AssetManager assetManager;
	private Texture tiles;
	private Texture texture;
	private BitmapFont font;
	private SpriteBatch batch;
	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 1000, 1000);
		camera.update();

		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		map = new Map(
				100,
				100,
				64,
				64,
				new Texture("assets/tiles/climate/temperate/plain_temperate_seasons.png"),
				Season.Summer
		);

		seasonChange = new SeasonChange(map);

		new Thread(() -> {
			while (true) {
				try {
					seasonChange.temperateSeasonChanging();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();

		renderer = new OrthogonalTiledMapRenderer(map.getMap());
	}

	@Override
	public void render () {
		ScreenUtils.clear(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.begin();
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		font.draw(batch, "Season: " + seasonChange.getCurrentSeason() + " iteration: " + seasonChange.getCurrentSeasonIter(), 10, 40);
		batch.end();
	}

	@Override
	public void dispose () {

	}


}
