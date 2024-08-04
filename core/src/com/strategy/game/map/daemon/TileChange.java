package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.Map;
import com.strategy.game.map.terrain.Season;
import com.strategy.game.map.terrain.SuitableTerrain;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class TileChange {
    private Season currentSeason;
    private final Map map;
    private final TextureRegion[][] tiles;
    private final Random random = new Random();
    private int currentSeasonIter;
    private TextureRegion currTile;
    private TextureRegion nextTile;
    private final TiledMapTileLayer layer;
    private final ArrayList<TextureRegion> prevTiles = new ArrayList<>();

    public TileChange(Map map, int currentSeasonIter) {
        this.map = map;
        this.tiles = map.getTextureRegions();
        this.currentSeason = map.getStartSeason();
        this.currentSeasonIter = currentSeasonIter;
        this.currTile = map.determineDefaultSeasonTile();
        this.nextTile = map.determineDefaultSeasonTile();
        this.layer = (TiledMapTileLayer) map.getMap().getLayers().get(0);
    }

    private void setTile(TextureRegion tile, int x, int y) {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(new StaticTiledMapTile(tile));
        layer.setCell(x, y, cell);
    }

    public int temperateSeasonChanging(int currentSeasonIter) {
        if (currentSeasonIter == 9) {
            currentSeason = determineTemperateNextSeason();
            currentSeasonIter = 0;
        } else {
            updateCurrentTile();
            if (currentSeasonIter == 5) {
                randomizeTileChanges();
            } else {
                maintainPrevTilesList();
                applyPrevTileChanges();
            }
            currentSeasonIter++;
        }
        this.currentSeasonIter = currentSeasonIter;
        return currentSeasonIter;
    }

    private void updateCurrentTile() {
        currTile = nextTile;
        nextTile = determineTemperateNextTile();
    }

    private void randomizeTileChanges() {
        for (int i = 0; i < ((map.getWidth() * map.getHeight()) / 5); i++) {
            int x = (int) (random.nextGaussian() * map.getTileWidth());
            int y = (int) (random.nextGaussian() * map.getHeight());
            setTile(nextTile, x, y);
        }
        maintainPrevTilesList();
    }

    private void maintainPrevTilesList() {
        if (prevTiles.size() > 6) {
            prevTiles.remove(0);
        }
        prevTiles.add(currTile);
    }

    private void applyPrevTileChanges() {
        ArrayList<SuitableTerrain> addedTerrains = new ArrayList<>();
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                applySuitableTileChanges(addedTerrains, i, j);
                handleLayerCellChanges(addedTerrains, i, j);
            }
        }
    }

    private void applySuitableTileChanges(ArrayList<SuitableTerrain> addedTerrains, int i, int j) {
        for (int k = prevTiles.size() - 1; k > 1; k--) {
            if (isNotAddedTile(addedTerrains, i, j)) {
                List<SuitableTerrain> suitableTerrains = determineSuitableTiles(i, j, prevTiles.get(k - 1));
                if (!suitableTerrains.isEmpty()) {
                    SuitableTerrain suitableTerrain = selectRandomSuitableTerrain(suitableTerrains);
                    if (isNotAddedTile(addedTerrains, i, j)) {
                        setTile(prevTiles.get(k), suitableTerrain.getX(), suitableTerrain.getY());
                        addedTerrains.add(suitableTerrain);
                    }
                }
            }
        }
    }

    private void handleLayerCellChanges(ArrayList<SuitableTerrain> addedTerrains, int i, int j) {
        if (!layer.getCell(i, j).getTile().getTextureRegion().equals(currTile) && !prevTiles.contains(layer.getCell(i, j).getTile().getTextureRegion())) {
            setTile(prevTiles.get(0), i, j);
            addedTerrains.add(new SuitableTerrain(prevTiles.get(0), i, j));
        }
    }

    private SuitableTerrain selectRandomSuitableTerrain(List<SuitableTerrain> suitableTerrains) {
        if (suitableTerrains.size() > 1) {
            return suitableTerrains.get(random.nextInt(suitableTerrains.size() - 1));
        } else {
            return suitableTerrains.get(0);
        }
    }

    private boolean isNotAddedTile(ArrayList<SuitableTerrain> addedTerrains, int x, int y) {
        for (SuitableTerrain terrain : addedTerrains) {
            if (terrain.getX() == x && terrain.getY() == y)
                return false;
        }
        return true;
    }

    private TextureRegion determineTemperateNextTile() {
        int[][] seasonTiles = getSeasonTiles(currentSeason);
        if (currentSeasonIter >= 0 && currentSeasonIter < 5)
            return tiles[seasonTiles[0][0]][seasonTiles[0][1]];
        else if (currentSeasonIter == 5)
            return tiles[seasonTiles[1][0]][seasonTiles[1][1]];
        else if (currentSeasonIter == 6)
            return tiles[seasonTiles[2][0]][seasonTiles[2][1]];
        else if (currentSeasonIter == 7)
            return tiles[seasonTiles[3][0]][seasonTiles[3][1]];
        else if (currentSeasonIter == 8)
            return tiles[seasonTiles[4][0]][seasonTiles[4][1]];
        return null;
    }

    private int[][] getSeasonTiles(Season season) {
        return switch (season) {
            case Summer -> new int[][]{{0, 2}, {0, 3}, {0, 4}, {1, 0}, {1, 1}};
            case Autumn -> new int[][]{{1, 2}, {1, 3}, {1, 4}, {2, 0}, {2, 1}};
            case Winter -> new int[][]{{2, 2}, {2, 3}, {2, 4}, {3, 0}, {3, 1}};
            case Spring -> new int[][]{{3, 2}, {3, 3}, {3, 4}, {0, 0}, {0, 1}};
        };
    }

    public Season determineTemperateNextSeason() {
        return switch (currentSeason) {
            case Summer -> Season.Autumn;
            case Autumn -> Season.Winter;
            case Winter -> Season.Spring;
            default -> Season.Summer;
        };
    }

    private List<SuitableTerrain> determineSuitableTiles(int x, int y, TextureRegion prevTile) {
        List<SuitableTerrain> suitableTiles = new ArrayList<>();
        addSuitableTerrain(suitableTiles, x - 1, y, prevTile);
        addSuitableTerrain(suitableTiles, x + 1, y, prevTile);
        addSuitableTerrain(suitableTiles, x, y + 1, prevTile);
        addSuitableTerrain(suitableTiles, x, y - 1, prevTile);
        addSuitableTerrain(suitableTiles, x + 1, y + 1, prevTile);
        addSuitableTerrain(suitableTiles, x - 1, y + 1, prevTile);
        addSuitableTerrain(suitableTiles, x + 1, y - 1, prevTile);
        addSuitableTerrain(suitableTiles, x - 1, y - 1, prevTile);
        return suitableTiles;
    }

    private void addSuitableTerrain(List<SuitableTerrain> suitableTiles, int x, int y, TextureRegion prevTile) {
        if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight() && layer.getCell(x, y).getTile().getTextureRegion().equals(prevTile)) {
            suitableTiles.add(new SuitableTerrain(layer.getCell(x, y).getTile().getTextureRegion(), x, y));
        }
    }
}
