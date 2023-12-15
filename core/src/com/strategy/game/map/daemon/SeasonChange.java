package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.Map;
import com.strategy.game.map.Season;
import com.strategy.game.map.SuitableTerrain;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class SeasonChange {
    private Season currentSeason;
    private Map map;
    private TextureRegion[][] tiles;
    private Random random = new Random();
    private int currentSeasonIter;
    private TextureRegion prevPrevPrevTile;
    private TextureRegion prevPrevTile;
    private TextureRegion prevTile;
    private TextureRegion currTile;
    private TextureRegion nextTile;
    private TiledMapTileLayer layer;
    private TiledMapTileLayer newLayer;

    public SeasonChange(Map map) {
        this.map = map;
        this.tiles = map.getTextureRegions();
        this.currentSeason = map.getStartSeason();
        this.currentSeasonIter = 4;
        this.prevPrevPrevTile = map.determineDefaultSeasonTile();
        this.prevPrevTile = map.determineDefaultSeasonTile();
        this.prevTile = map.determineDefaultSeasonTile();
        this.currTile = map.determineDefaultSeasonTile();
        this.nextTile = map.determineDefaultSeasonTile();
        this.layer = copyLayer((TiledMapTileLayer) map.getMap().getLayers().get(0));
    }

    private TiledMapTileLayer copyLayer(TiledMapTileLayer oldLayer) {
        TiledMapTileLayer layer = new TiledMapTileLayer(map.getWidth(), map.getHeight(), map.getTileWidth(), map.getTileHeight());
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                layer.setCell(i, j, new TiledMapTileLayer.Cell().setTile(oldLayer.getCell(i, j).getTile()));
            }
        }
        return layer;
    }

    private void setTile(TextureRegion tile, int x, int y) {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(new StaticTiledMapTile(tile));
        layer.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(tile)));
    }

    public void temperateSeasonChanging() throws InterruptedException {
        if (currentSeasonIter == 6) {
            currentSeason = determineTemperateNextSeason();
            currentSeasonIter = 0;
        }
        while (currentSeasonIter < 6) {
            prevPrevPrevTile = prevPrevTile;
            prevPrevTile = prevTile;
            prevTile = currTile;
            currTile = nextTile;
            nextTile = determineTemperateNextTile();
            if (currentSeasonIter == 0 && currentSeason != Season.Summer || currentSeasonIter == 4 && currentSeason == Season.Spring) {
                for (int j = 0; j < ((map.getWidth() * map.getHeight()) / 3 + 1); j++) {
                    int x = random.nextInt(map.getWidth());
                    int y = random.nextInt(map.getHeight());
                    if (currentSeason == Season.Spring && currentSeasonIter == 4)
                        setTile(nextTile, x, y);
                    else
                        setTile(currTile, x, y);
                }
            }
            else if (currentSeasonIter >= 4 && currentSeason != Season.Spring ||
                    currentSeasonIter == 3 && currentSeason == Season.Spring ||
                    currentSeasonIter == 0 && currentSeason == Season.Summer) {
                for (int j = 0; j < map.getWidth(); j++) {
                    for (int k = 0; k < map.getHeight(); k++) {
                        setTile(currTile, j, k);
                    }
                }
            } else {
                for (int j = 0; j < map.getWidth(); j++) {
                    for (int k = 0; k < map.getHeight(); k++) {
                        if (layer.getCell(j, k).getTile().getTextureRegion().equals(prevTile)) {
                            if (currentSeasonIter == 5 && currentSeason == Season.Spring) {
                                int rand = random.nextInt(2);
                                if (rand == 0) {
                                    setTile(currTile, j, k);
                                }
                            } else {
                                setTile(currTile, j, k);
                            }

                        }
                    }
                }
                for (int j = 0; j < map.getWidth(); j++) {
                    for (int k = 0; k < map.getTileHeight(); k++) {
                        if (layer.getCell(j, k).getTile().getTextureRegion().equals(currTile)) {
                            List<SuitableTerrain> terrains = determineSuitableTiles(j, k);
                            SuitableTerrain suitableTerrain;
                            if (terrains.size() >= 1) {
                                if (terrains.size() > 1)
                                    suitableTerrain = terrains.get(random.nextInt(0, terrains.size() - 1));
                                else
                                    suitableTerrain = terrains.get(0);
                                setTile(prevTile, suitableTerrain.getX(), suitableTerrain.getY());
                            }
                        } else if (layer.getCell(j, k).getTile().getTextureRegion().equals(prevPrevPrevTile))
                            setTile(prevPrevTile, j, k);
                    }
                }
            }
            currentSeasonIter++;
            Thread.sleep(4000);
        }
    }

    private TextureRegion determineTemperateNextTile() {
        if (currentSeason == Season.Summer)
            if (currentSeasonIter < 5)
                return tiles[0][1];
            else
                return tiles[0][2];
        else if (currentSeason == Season.Autumn)
            if (currentSeasonIter == 0)
                return tiles[1][0];
            else if (currentSeasonIter == 1)
                return tiles[1][1];
            else if (currentSeasonIter == 5)
                return tiles[1][2];
            else
                return tiles[1][1];
        else if (currentSeason == Season.Winter)
            if (currentSeasonIter == 0)
                return tiles[2][0];
            else if (currentSeasonIter == 1)
                return tiles[2][1];
            else if (currentSeasonIter == 5)
                return tiles[2][2];
            else
                return tiles[2][1];
        else if (currentSeason == Season.Spring)
            if (currentSeasonIter >= 0 && currentSeasonIter < 4)
                return tiles[0][0];
            else
                return tiles[0][1];
        return null;
    }

    private Season determineTemperateNextSeason() {
        switch (currentSeason) {
            case Summer:
                return Season.Autumn;
            case Autumn:
                return Season.Winter;
            case Winter:
                return Season.Spring;
            default:
                return Season.Summer;
        }
    }

    private List<SuitableTerrain> determineSuitableTiles(int x, int y) {
        List<SuitableTerrain> suitableTiles = new ArrayList<>();
        if (x - 1 < map.getWidth() && x - 1 >= 0)
            if (layer.getCell(x - 1, y).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1, y).getTile().getTextureRegion(), x - 1, y));
        if (x + 1 < map.getWidth())
            if (layer.getCell(x + 1, y).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y).getTile().getTextureRegion(), x + 1, y));
        if (y + 1 < map.getHeight())
            if (layer.getCell(x, y + 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x, y + 1).getTile().getTextureRegion(), x, y + 1));
        if (y - 1 < map.getWidth() && y - 1 >= 0)
            if (layer.getCell(x, y - 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x,y - 1).getTile().getTextureRegion(), x, y - 1));
        if (x + 1 < map.getWidth() && y + 1 < map.getHeight())
            if (layer.getCell(x + 1, y + 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y + 1).getTile().getTextureRegion(), x + 1, y + 1));
        if (x - 1 < map.getWidth() && x - 1 >= 0 && y + 1 < map.getHeight())
            if (layer.getCell(x - 1, y + 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1,y + 1).getTile().getTextureRegion(), x - 1, y + 1));
        if (x + 1 < map.getWidth() && y - 1 < map.getHeight() && y - 1 >= 0)
            if (layer.getCell(x + 1,y - 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y - 1).getTile().getTextureRegion(), x + 1, y - 1));
        if (x - 1 < map.getWidth() && x - 1 >= 0 && y - 1 < map.getHeight() && y - 1 >= 0)
            if (layer.getCell(x - 1,y - 1).getTile().getTextureRegion().equals(prevPrevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1,y - 1).getTile().getTextureRegion(), x - 1, y - 1));
        return suitableTiles;
    }
}
