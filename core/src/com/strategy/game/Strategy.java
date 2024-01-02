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
import com.strategy.game.map.daemon.SeasonChangeDaemonTask;
import com.strategy.game.map.terrain.Season;
import com.strategy.game.map.daemon.TileChange;

import java.util.Timer;

public class Strategy extends ApplicationAdapter {
	Map map;
	TileChange tileChange;
	ForestChange forestChange;
	GrassChange grassChange;
	SeasonChangeDaemonTask seasonChangeDaemonTask;
	Timer seasonChangeDemon;

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
	private int currentSeasonIter;
	private int grassStartBirthIter;

	private int yearCount = 0;
	private int year = 0;

	
	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, (w / h) * 3500, 3500);
		camera.update();

		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);

		font = new BitmapFont();
		batch = new SpriteBatch();

		currentSeason = Season.Summer;
		currentSeasonIter = 4;
		climate = "temperate";
		grassStartBirthIter = 3;

		map = new Map(
				50,
				50,
				128,
				128,
				new Texture("assets/tiles/climate/temperate/plain_temperate_seasons128.png"),
				currentSeason
		);

		tileChange = new TileChange(map, 4);

		forestChange = new ForestChange(map, climate, currentSeason);

		grassChange = new GrassChange(map, climate, currentSeason, 3);

		seasonChangeDaemonTask = new SeasonChangeDaemonTask(tileChange, forestChange, grassChange);
		seasonChangeDemon = new Timer();
		seasonChangeDemon.schedule(seasonChangeDaemonTask, 0, 50);

		renderer = new OrthogonalTiledMapRenderer(map.getMap());
	}

	@Override
	public void render () {
		ScreenUtils.clear(100f / 255f, 100f / 255f, 250f / 255f, 1f);
		camera.update();
		renderer.setView(camera);
		renderer.render();
		batch.begin();
		font.draw(batch, "Year: " + seasonChangeDaemonTask.getYear(), 10, 40);
		font.draw(batch, "Season: " + seasonChangeDaemonTask.getCurrentSeason() + " iteration: " + seasonChangeDaemonTask.getCurrentIter(), 10, 20);
		batch.end();
	}

	@Override
	public void dispose () {

	}


}
