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
    private TextureRegion currTile;
    private TextureRegion nextTile;
    private TiledMapTileLayer layer;
    ArrayList<TextureRegion> prevTiles = new ArrayList<>();

    public SeasonChange(Map map) {
        this.map = map;
        this.tiles = map.getTextureRegions();
        this.currentSeason = map.getStartSeason();
        this.currentSeasonIter = 4;
        this.currTile = map.determineDefaultSeasonTile();
        this.nextTile = map.determineDefaultSeasonTile();
        this.layer = (TiledMapTileLayer) map.getMap().getLayers().get(0);
    }

    private void setTile(TextureRegion tile, int x, int y) {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(new StaticTiledMapTile(tile));
        layer.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(tile)));
    }

    public void temperateSeasonChanging() throws InterruptedException {
        if (currentSeasonIter == 9) {
            currentSeason = determineTemperateNextSeason();
            currentSeasonIter = 0;
        }
        while (currentSeasonIter < 9) {
            currTile = nextTile;
            nextTile = determineTemperateNextTile();
            if (currentSeasonIter == 5) {
                for (int i = 0; i < ((map.getWidth() * map.getHeight()) / 9); i++) {
                    int x = (int) random.nextGaussian() * map.getTileWidth();
                    int y = (int) random.nextGaussian() * map.getHeight();
                    setTile(nextTile, x, y);
                }
                prevTiles.add(currTile);
            } else {
                if (prevTiles.size() > 6) {
                    prevTiles.add(currTile);
                    prevTiles.remove(0);
                }
                else {
                    prevTiles.add(currTile);
                }
                ArrayList<SuitableTerrain> addedTerrains = new ArrayList<>();
                for (int i = 0; i < map.getWidth(); i++) {
                    for (int j = 0; j < map.getHeight(); j++) {
                        for (int k = prevTiles.size() - 1; k > 1; k--) {
                            if (isNotAddedTile(addedTerrains, i, j)) {
                                List<SuitableTerrain> suitableTerrains = determineSuitableTiles(i, j, prevTiles.get(k - 1));
                                SuitableTerrain suitableTerrain;
                                if (suitableTerrains.size() > 0) {
                                    if (suitableTerrains.size() > 1)
                                        suitableTerrain = suitableTerrains.get(random.nextInt(0, suitableTerrains.size() - 1));
                                    else
                                        suitableTerrain = suitableTerrains.get(0);
                                    if (isNotAddedTile(addedTerrains, i, j)) {
                                        setTile(prevTiles.get(k), suitableTerrain.getX(), suitableTerrain.getY());
                                        addedTerrains.add(suitableTerrain);
                                    }
                                }
                            }
                        }
                        if (layer.getCell(i, j).getTile().getTextureRegion().equals(currTile) && isNotAddedTile(addedTerrains, i, j)) {
                            setTile(nextTile, i, j);
                            addedTerrains.add(new SuitableTerrain(nextTile, i, j));
                        }
                    }
                }
            }
            currentSeasonIter++;
            Thread.sleep(1000);
        }
    }

    private boolean isNotAddedTile(ArrayList<SuitableTerrain> addedTerrains, int x, int y) {
        for (SuitableTerrain terrain: addedTerrains) {
            if (terrain.getX() == x && terrain.getY() == y)
                return false;
        }
        return true;
    }

    private TextureRegion determineTemperateNextTile() {
        if (currentSeason == Season.Summer) {
            if (currentSeasonIter >= 0 && currentSeasonIter < 5)
                return tiles[0][2];
            else if (currentSeasonIter == 5)
                return tiles[0][3];
            else if (currentSeasonIter == 6)
                return tiles[0][4];
            else if (currentSeasonIter == 7)
                return tiles[1][0];
            else if (currentSeasonIter == 8)
                return tiles[1][1];
        }
        else if (currentSeason == Season.Autumn) {
            if (currentSeasonIter >= 0 && currentSeasonIter < 5)
                return tiles[1][2];
            else if (currentSeasonIter == 5)
                return tiles[1][3];
            else if (currentSeasonIter == 6)
                return tiles[1][4];
            else if (currentSeasonIter == 7)
                return tiles[2][0];
            else if (currentSeasonIter == 8)
                return tiles[2][1];
        }
        else if (currentSeason == Season.Winter) {
            if (currentSeasonIter >= 0 && currentSeasonIter < 5)
                return tiles[2][2];
            else if (currentSeasonIter == 5)
                return tiles[2][3];
            else if (currentSeasonIter == 6)
                return tiles[2][4];
            else if (currentSeasonIter == 7)
                return tiles[3][0];
            else if (currentSeasonIter == 8)
                return tiles[3][1];
        } else if (currentSeason == Season.Spring) {
            if (currentSeasonIter >= 0 && currentSeasonIter < 5)
                return tiles[3][2];
            else if (currentSeasonIter == 5)
                return tiles[3][3];
            else if (currentSeasonIter == 6)
                return tiles[3][4];
            else if (currentSeasonIter == 7)
                return tiles[0][0];
            else if (currentSeasonIter == 8)
                return tiles[0][1];
        }
        return null;
    }

    private Season determineTemperateNextSeason() {
        return switch (currentSeason) {
            case Summer -> Season.Autumn;
            case Autumn -> Season.Winter;
            case Winter -> Season.Spring;
            default -> Season.Summer;
        };
    }

    private List<SuitableTerrain> determineSuitableTiles(int x, int y, TextureRegion prevTile) {
        List<SuitableTerrain> suitableTiles = new ArrayList<>();
        if (x - 1 < map.getWidth() && x - 1 >= 0)
            if (layer.getCell(x - 1, y).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1, y).getTile().getTextureRegion(), x - 1, y));
        if (x + 1 < map.getWidth())
            if (layer.getCell(x + 1, y).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y).getTile().getTextureRegion(), x + 1, y));
        if (y + 1 < map.getHeight())
            if (layer.getCell(x, y + 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x, y + 1).getTile().getTextureRegion(), x, y + 1));
        if (y - 1 < map.getWidth() && y - 1 >= 0)
            if (layer.getCell(x, y - 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x,y - 1).getTile().getTextureRegion(), x, y - 1));
        if (x + 1 < map.getWidth() && y + 1 < map.getHeight())
            if (layer.getCell(x + 1, y + 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y + 1).getTile().getTextureRegion(), x + 1, y + 1));
        if (x - 1 < map.getWidth() && x - 1 >= 0 && y + 1 < map.getHeight())
            if (layer.getCell(x - 1, y + 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1,y + 1).getTile().getTextureRegion(), x - 1, y + 1));
        if (x + 1 < map.getWidth() && y - 1 < map.getHeight() && y - 1 >= 0)
            if (layer.getCell(x + 1,y - 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x + 1, y - 1).getTile().getTextureRegion(), x + 1, y - 1));
        if (x - 1 < map.getWidth() && x - 1 >= 0 && y - 1 < map.getHeight() && y - 1 >= 0)
            if (layer.getCell(x - 1,y - 1).getTile().getTextureRegion().equals(prevTile))
                suitableTiles.add(new SuitableTerrain(layer.getCell(x - 1,y - 1).getTile().getTextureRegion(), x - 1, y - 1));
        return suitableTiles;
    }
}
