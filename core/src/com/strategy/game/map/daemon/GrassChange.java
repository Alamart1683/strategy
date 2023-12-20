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
                    determineStartGrassTile(tiles, startSeason),
                    tiles
            );
            grassList.add(grass);
        }
    }

    public void nextGrassGrowsIter(Season currentSeason) {
        for (int i = 0; i < grassLayer.getWidth(); i++) {
            for (int j = 0; j < grassLayer.getHeight(); j++) {
                if (currGrassInForest[i][j] != null) {
                    if (!currGrassInForest[i][j].isAlive()) {
                        setGrass(null, i, j);
                        currGrassInForest[i][j] = null;
                    } else if (currGrassInForest[i][j].getStartGrowth().equals(currentSeason)) {
                        if (random.nextInt((int) currGrassInForest[i][j].getLifeProbability() * 10) == 0) {
                            int x = random.nextInt(grassLayer.getWidth());
                            int y = random.nextInt(grassLayer.getHeight());
                            // More uniform tree growth
                            if (x < grassLayer.getWidth() / 5)
                                x = grassLayer.getWidth() / 5;
                            if (x > grassLayer.getWidth() - 5)
                                x = grassLayer.getWidth() - 5;
                            if (y < grassLayer.getHeight() / 5)
                                y = grassLayer.getHeight() / 5;
                            if (y > grassLayer.getHeight() - 5)
                                y = grassLayer.getHeight() - 5;
                            if (currGrassInForest[x][y] == null) {
                                setGrass(new Grass(determineGrass(currGrassInForest[i][j])), x, y);
                            }
                        }
                        currGrassInForest[i][j].grow(currentSeason.name());
                        setGrass(currGrassInForest[i][j], i, j);
                    }
                    else if (!currentSeason.equals(Season.Winter) && currentIter == 0) {
                        if (random.nextInt((int) (currGrassInForest[i][j].getLifeProbability() * 10)) == 0) {
                            int x = random.nextInt(grassLayer.getWidth());
                            int y = random.nextInt(grassLayer.getHeight());
                            // More uniform tree growth
                            if (x < grassLayer.getWidth() / 5)
                                x = grassLayer.getWidth() / 5;
                            if (x > grassLayer.getWidth() - 5)
                                x = grassLayer.getWidth() - 5;
                            if (y < grassLayer.getHeight() / 5)
                                y = grassLayer.getHeight() / 5;
                            if (y > grassLayer.getHeight() - 5)
                                y = grassLayer.getHeight() - 5;
                            if (currGrassInForest[x][y] == null) {
                                setGrass(new Grass(determineGrass(currGrassInForest[i][j])), i, j);
                            }
                        } else {
                            currGrassInForest[i][j].grow(currentSeason.name());
                            setGrass(currGrassInForest[i][j], i, j);
                        }
                    } else if (!currentSeason.equals(Season.Winter)) {
                        currGrassInForest[i][j].grow(currentSeason.name());
                        setGrass(currGrassInForest[i][j], i, j);
                    }
                } else {
                    if (grassList.get(0).getStartGrowth().equals(currentSeason)) {
                        if (random.nextInt((int) (grassList.get(0).getLifeProbability() * 10)) == 0) {
                            int x = random.nextInt(grassLayer.getWidth());
                            int y = random.nextInt(grassLayer.getHeight());
                            // More uniform tree growth
                            if (x < grassLayer.getWidth() / 5)
                                x = grassLayer.getWidth() / 5;
                            if (x > grassLayer.getWidth() - 5)
                                x = grassLayer.getWidth() - 5;
                            if (y < grassLayer.getHeight() / 5)
                                y = grassLayer.getHeight() / 5;
                            if (y > grassLayer.getHeight() - 5)
                                y = grassLayer.getHeight() - 5;
                            if (currGrassInForest[x][y] == null) {
                                setGrass(new Grass(grassList.get(0)), x, y);
                            }
                        }
                        currGrassInForest[i][j].grow(currentSeason.name());
                        setGrass(currGrassInForest[i][j], i, j);
                    }
                    else if (!currentSeason.equals(Season.Winter) && currentIter == 0) {
                        if (random.nextInt((int) (grassList.get(0).getLifeProbability() * 10)) == 0) {
                            int x = random.nextInt(grassLayer.getWidth());
                            int y = random.nextInt(grassLayer.getHeight());
                            // More uniform tree growth
                            if (x < grassLayer.getWidth() / 5)
                                x = grassLayer.getWidth() / 5;
                            if (x > grassLayer.getWidth() - 5)
                                x = grassLayer.getWidth() - 5;
                            if (y < grassLayer.getHeight() / 5)
                                y = grassLayer.getHeight() / 5;
                            if (y > grassLayer.getHeight() - 5)
                                y = grassLayer.getHeight() - 5;
                            if (currGrassInForest[x][y] == null) {
                                setGrass(new Grass(grassList.get(0)), i, j);
                            }
                        }
                    }
                }
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

    private TextureRegion determineStartGrassTile(TextureRegion[][] tiles, Season startSeason) {
        switch (startSeason) {
            case Spring -> {
                return tiles[0][0];
            }
            case Summer -> {
                return tiles[0][2];
            }
            case Autumn -> {
                return tiles[0][3];
            }
            default -> {
                return tiles[0][4];
            }
        }
    }
}
