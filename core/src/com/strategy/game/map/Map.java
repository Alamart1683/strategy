package com.strategy.game.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;

@Getter
public class Map {
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;
    private Texture tiles;
    private TextureRegion[][] textureRegions;
    private Season startSeason;
    private TiledMap map;
    private TextureRegion transparent;

    public Map(int width, int height, int tileWidth, int tileHeight, Texture tiles, Season startSeason) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = tiles;
        this.startSeason = startSeason;
        this.map = initializeMap();
    }

    private TiledMap initializeMap() {
        map = new TiledMap();
        MapLayers layers = map.getLayers();
        textureRegions = TextureRegion.split(tiles, tileWidth, tileHeight);
        TextureRegion startTile = determineDefaultSeasonTile();
        TiledMapTileLayer tileLayer = new TiledMapTileLayer(width, height, tileWidth, tileHeight);
        transparent = TextureRegion.split(new Texture("assets/tiles/transparent.png"), width, height)[0][0];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(startTile));
                tileLayer.setCell(i, j, cell);
            }
        }
        layers.add(tileLayer);
        return map;
    }

    public TextureRegion determineDefaultSeasonTile() {
        switch (startSeason) {
            case Summer:
                return textureRegions[0][2];
            case Autumn:
                return textureRegions[1][2];
            case Winter:
                return textureRegions[2][2];
            default:
                return textureRegions[3][2];
        }
    }
}
