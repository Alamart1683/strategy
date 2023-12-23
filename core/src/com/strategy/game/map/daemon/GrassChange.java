package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.Map;
import com.strategy.game.map.forest.Grass;
import com.strategy.game.map.forest.PlantType;
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

    @SneakyThrows
    public GrassChange(Map map, String climate, Season startSeason, int startSeasonBirthIter) {
        this.map = map;
        this.grassLayer = (TiledMapTileLayer) map.getMap().getLayers().get(2);
        this.grassList = new ArrayList<>();
        this.currGrassInForest = initializeCurrGrassInForest();
        loadGrass(climate, startSeasonBirthIter);
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

    private void loadGrass(String climate, int startSeasonBirthIter) throws IOException {
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest/grass")).toList();
        for (Path path: paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, map.getTileWidth(), map.getTileHeight());
            Grass grass = new Grass(
                    Season.Spring,
                    Season.Autumn,
                    0.1,
                    startSeasonBirthIter,
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
        for (int i = 0; i < currGrassInForest.length; i++) {
            for (int j = 0; j < currGrassInForest[0].length; j++) {
                Grass grass = grassList.get(random.nextInt(grassList.size()));
                int probability = random.nextInt((int) ((grass.getLifeProbability() * 10) / grassList.size() * 5.));
                Grass newGrass = new Grass(grass);
                if (probability == 0) {
                    if (startSeason != Season.Winter) {
                        if (startSeason != Season.Summer) {
                            newGrass.setTile(determineStartGrassTile(grass.getTiles(), startSeason, grassList.indexOf(grass)));
                        }
                        else {
                            newGrass.setAge(3);
                        }
                    }
                    if (startSeason == Season.Autumn) {
                        newGrass.setAge(3);
                    }
                    setGrass(newGrass, i, j);
                }
            }
        }
    }

    public void nextGrassGrowsIter(Season currentSeason, int currentSeasonIter) {
        if (currentSeason == Season.Spring && currentSeasonIter == 0) {
            growSpringGrass(20);
        }
        for (int i = 0; i < grassLayer.getWidth(); i++) {
            for (int j = 0; j < grassLayer.getHeight(); j++) {
                Grass currentGrass = currGrassInForest[i][j];
                Grass grass = grassList.get(random.nextInt(grassList.size()));
                if (currentGrass != null) {
                    if (currentSeason == Season.Spring && currentSeasonIter >= grass.getStartSeasonBirthIter()) {
                        int probability = random.nextInt((int) (grass.getLifeProbability() * currentSeasonIter * currentSeasonIter + 1));
                        if (probability == 0) {
                            growNewGrass(grass, i, j);
                        }
                    }
                    currentGrass.grow(currentSeason.name(), currentSeasonIter);
                    if (currentGrass.isAlive())
                        setGrass(currentGrass, i, j);
                    else
                        setGrass(null, i, j);
                }
            }
        }
    }

    public void growNewGrass(Grass grass, int i, int j) {
        for (int k = 0; k < grass.getFertility() * grass.getFertility(); k++) {
            int x, y;
            if (k % 2 == 0) {
                x = random.nextInt(i + 1, i + 2);
                y = random.nextInt(j + 1, j + 2);
            } else if (k % 3 == 0) {
                x = random.nextInt(i + 1, i + 2);
                y = random.nextInt(j - 2, j - 1);
            } else if (k % 5 == 0) {
                x = random.nextInt(i - 2, i - 1);
                y = random.nextInt(j + 1, j + 2);
            } else {
                x = random.nextInt(i - 2, i - 1);
                y = random.nextInt(j - 2, j - 1);
            }
            if (checkNearGrass(x, y) && currGrassInForest[x][y] == null) {
                Grass newGrass = new Grass(grass);
                newGrass.setTile(newGrass.getTiles()[0][0]);
                currGrassInForest[x][y] = newGrass;
                setGrass(newGrass, x, y);
                return;
            }
        }
    }

    private void growSpringGrass(int birthChange) {
        for (int i = 0; i < currGrassInForest.length; i++) {
            for (int j = 0; j < currGrassInForest[0].length; j++) {
                Grass grass = grassList.get(random.nextInt(grassList.size()));
                int probability = random.nextInt((int) ((grass.getLifeProbability() * birthChange) / grassList.size() * 5.));
                Grass newGrass = new Grass(grass);
                if (probability == 0) {
                    newGrass.setTile(newGrass.getTiles()[0][0]);
                    setGrass(newGrass, i, j);
                }
            }
        }
    }

    private boolean checkNearGrass(int x, int y) {
        if (x < grassLayer.getWidth() && x >= 0 && y < grassLayer.getHeight() && y >= 0) {
            int nearWithoutBordersCase = random.nextInt(40);
            if (nearWithoutBordersCase == 0)
                return true;

            boolean xMinus1y = false;
            if (x - 1 >= grassLayer.getWidth() || x - 1 < 0)
                xMinus1y = true;
            else if (grassLayer.getCell(x - 1, y).getTile().getTextureRegion().equals(map.getTransparent()))
                xMinus1y = true;

            boolean xPlus1y = false;
            if (x + 1 >= grassLayer.getWidth())
                xPlus1y = true;
            else if (grassLayer.getCell(x + 1, y).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1y = true;

            boolean xyPlus1 = false;
            if (y + 1 >= grassLayer.getHeight())
                xyPlus1 = true;
            else if (grassLayer.getCell(x, y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xyPlus1 = true;

            boolean xyMinus1 = false;
            if (y - 1 >= grassLayer.getHeight()|| y - 1 < 0)
                xyMinus1 = true;
            else if (grassLayer.getCell(x, y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xyMinus1 = true;

            boolean xPlus1yPlus1 = false;
            if (x + 1 >= grassLayer.getWidth() || y + 1 >= grassLayer.getHeight())
                xPlus1yPlus1 = true;
            else if (grassLayer.getCell(x + 1, y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1yPlus1 = true;

            boolean xMinus1yPlus1 = false;
            if (x - 1 >= grassLayer.getWidth() || x - 1 < 0 || y + 1 >= grassLayer.getHeight())
                xMinus1yPlus1 = true;
            else if (grassLayer.getCell(x - 1,y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xMinus1yPlus1 = true;

            boolean xPlus1yMinus1 = false;
            if (x + 1 >= grassLayer.getWidth() || y - 1 >= grassLayer.getHeight() || y - 1 < 0)
                xPlus1yMinus1 = true;
            else if (grassLayer.getCell(x + 1,y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1yMinus1 = true;

            boolean xMinus1yMinus1 = false;
            if (x - 1 >= grassLayer.getWidth() || x - 1 < 0 || y - 1 >= grassLayer.getHeight() || y - 1 < 0)
                xMinus1yMinus1 = true;
            else if (grassLayer.getCell(x - 1,y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xMinus1yMinus1 = true;
            int nearCase = random.nextInt(2);
            if (nearCase == 0) {
                return xyMinus1 && xyPlus1 && xMinus1y && xPlus1y;
            } else {
                return xPlus1yPlus1 && xMinus1yPlus1 && xPlus1yMinus1 && xMinus1yMinus1 && xyMinus1 && xyPlus1 && xMinus1y && xPlus1y;
            }
        }
        return false;
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
