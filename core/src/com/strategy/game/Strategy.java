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
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.strategy.game.map.Map;
import com.strategy.game.map.daemon.ForestChange;
import com.strategy.game.map.daemon.GrassChange;
import com.strategy.game.map.forest.Plant;
import com.strategy.game.map.terrain.Season;
import com.strategy.game.map.daemon.SeasonChange;

public class Strategy extends ApplicationAdapter {
	Map map;
	SeasonChange seasonChange;
	ForestChange forestChange;

	GrassChange grassChange;

	private TiledMapRenderer renderer;
	private OrthographicCamera camera;
	private CameraInputController cameraController;
	private AssetManager assetManager;
	private Texture tiles;
	private Texture texture;
	private BitmapFont font;
	private SpriteBatch batch;
	private Season currentSeason;
	private String climate;

	private int yearCount = 0;
	private int year = 0;

	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 1500, 1500);
		camera.update();

		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		currentSeason = Season.Summer;
		climate = "temperate";

		map = new Map(
				25,
				25,
				128,
				128,
				new Texture("assets/tiles/climate/temperate/plain_temperate_seasons128.png"),
				Season.Summer
		);

		seasonChange = new SeasonChange(map);

		forestChange = new ForestChange(map, climate, currentSeason);

		grassChange = new GrassChange(map, climate, currentSeason);


		new Thread(() -> {
			while (true) {
				try {
					seasonChange.temperateSeasonChanging();
					if (seasonChange.getCurrentSeasonIter() == 9) {
						forestChange.nextForestGrowsIter(seasonChange.determineTemperateNextSeason());
						yearCount++;
					}
					grassChange.nextGrassGrowsIter(currentSeason);
					if (yearCount == 4) {
						year++;
						yearCount = 0;
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();

		/*
		new Thread(() -> {
			while (true) {
				forestChange.nextForestGrowsIter(currentSeason);
			}
		}).start();
		 */

		renderer = new OrthogonalTiledMapRenderer(map.getMap());
	}

	@Override
	public void render () {
		ScreenUtils.clear(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.begin();
		font.draw(batch, "Year: " + year, 10, 40);
		font.draw(batch, "Season: " + seasonChange.getCurrentSeason() + " iteration: " + seasonChange.getCurrentSeasonIter(), 10, 20);
		batch.end();
	}

	@Override
	public void dispose () {

	}


}
