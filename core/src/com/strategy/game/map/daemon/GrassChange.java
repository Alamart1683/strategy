package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.Map;
import com.strategy.game.map.forest.Grass;
import com.strategy.game.map.forest.PlantType;
import com.strategy.game.map.forest.Tree;
import com.strategy.game.map.terrain.Season;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GrassChange {
    private Map map;
    private TiledMapTileLayer grassLayer;
    private List<Grass> grassList;
    private Grass[][] currGrassInForest;
    private Random random = new Random();
    int currentIter;

    @SneakyThrows
    public GrassChange(Map map, String climate, Season startSeason) {
        this.map = map;
        this.grassLayer = (TiledMapTileLayer) map.getMap().getLayers().get(2);
        this.grassList = new ArrayList<>();
        this.currentIter = 0;
        this.currGrassInForest = initializeCurrGrassInForest();
        loadGrass(climate, startSeason);
        initializeGrass(startSeason);
    }

    private Grass[][] initializeCurrGrassInForest() {
        currGrassInForest = new Grass[map.getWidth()][map.getHeight()];
        for (int i = 0; i < currGrassInForest.length; i++) {
            for (int j = 0; j < currGrassInForest[0].length; j++) {
                currGrassInForest[i][j] = null;
            }
        }
        return currGrassInForest;
    }

    private void loadGrass(String climate, Season startSeason) throws IOException {
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest/grass")).toList();
        for (Path path: paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, map.getTileWidth(), map.getTileHeight());
            Grass grass = new Grass(
                    Season.Spring,
                    Season.Autumn,
                    0.2,
                    4,
                    path.getFileName().toString(),
                    PlantType.Tree,
                    1,1, 3, 1, 3, 3,
                    tiles[paths.indexOf(path)][2], // 2 is default summer tile,
                    tiles
            );
            grassList.add(grass);
        }
    }

    public void initializeGrass(Season startSeason) {
        for (Grass grass: grassList) {
            for (int i = 0; i < currGrassInForest.length; i++) {
                for (int j = 0; j < currGrassInForest[0].length; j++) {
                    int probability = random.nextInt((int)(((grass.getLifeProbability() * currentIter) / (double) grassList.size()) * 10.));
                    Grass newGrass = new Grass(grass);
                    if (probability == 0) {
                        if (startSeason != Season.Winter) {
                            if (startSeason != Season.Summer) {
                                newGrass.setTile(determineStartGrassTile(grass.getTiles(), startSeason, grassList.indexOf(grass)));
                            }
                            setGrass(newGrass, i, j);
                        }
                    }
                }
            }
        }
    }

    public void nextGrassGrowsIter(Season currentSeason) {
        for (int i = 0; i < grassLayer.getWidth(); i++) {
            for (int j = 0; j < grassLayer.getHeight(); j++) {

            }
        }
    }

    private Grass determineGrass(Grass grass) {
        for(Grass currGrass: grassList) {
            if (currGrass.getPlantName().equals(grass.getPlantName())) {
                return currGrass;
            }
        }
        return grassList.get(0);
    }

    private void setGrass(Grass grass, int x, int y) {
        if (grass == null) {
            currGrassInForest[x][y] = null;
            grassLayer.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(map.getTransparent())));
        } else {
            grassLayer.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(grass.getTile())));
            currGrassInForest[x][y] = grass;
        }
    }

    private TextureRegion determineStartGrassTile(TextureRegion[][] tiles, Season startSeason, int grassIndex) {
        switch (startSeason) {
            case Spring -> {
                return tiles[grassIndex][0];
            }
            case Summer -> {
                return tiles[grassIndex][2];
            }
            case Autumn -> {
                return tiles[grassIndex][3];
            }
            default -> {
                return tiles[grassIndex][4];
            }
        }
    }
}
