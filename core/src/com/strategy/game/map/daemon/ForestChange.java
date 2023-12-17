package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.strategy.game.map.Map;
import com.strategy.game.map.forest.PlantType;
import com.strategy.game.map.forest.Tree;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class ForestChange {
    private Map map;
    private TiledMapTileLayer forest;
    private List<Tree> trees;
    private Random random = new Random();
    int currentIter;

    @SneakyThrows
    public ForestChange(Map map, String climate, Season startSeason) {
        this.map = map;
        this.forest = (TiledMapTileLayer) map.getMap().getLayers().get(1);
        this.trees = new ArrayList<>();
        this.currentIter = 0;
        loadTrees(climate, startSeason);
        initializeForest();
    }

    private void loadTrees(String climate, Season startSeason) throws IOException {
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest")).toList();
        for (Path path: paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, map.getTileWidth() * 2, map.getTileHeight() * 2);
            Tree tree = new Tree(
                    path.getFileName().toString(),
                    PlantType.Tree,
                    4,1, 3, 1, 30, 13,
                    determineStartTreeTile(tiles, startSeason),
                    tiles
            );
            trees.add(tree);
        }
    }

    private TextureRegion determineStartTreeTile(TextureRegion[][] tiles, Season startSeason) {
        switch (startSeason) {
            case Spring -> {
                return tiles[0][0];
            }
            case Summer -> {
                return tiles[0][1];
            }
            case Autumn -> {
                return tiles[0][2];
            }
            case Winter -> {
                return tiles[0][3];
            }
            default -> {
                return tiles[0][4];
            }
        }
    }

    private void setTree(TextureRegion tree, int x, int y) {
        TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
        cell.setTile(new StaticTiledMapTile(tree));
        forest.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(tree)));
    }

    public void initializeForest() {
        for (int i = 0; i < trees.size() * forest.getWidth(); i++) {
            int x = random.nextInt(forest.getWidth());
            int y = random.nextInt(forest.getHeight());
            /*
            // More uniform tree growth
            if (x < forest.getWidth() / 5)
                x = forest.getWidth() / 5;
            if (x > forest.getWidth() - 5)
                x = forest.getWidth() - 5;
            if (y < forest.getHeight() / 5)
                y = forest.getHeight() / 5;
            if (y > forest.getHeight() - 5)
                y = forest.getHeight() - 5;
             */
            Tree tree = trees.get(random.nextInt(trees.size()));
            setTree(tree.getTile(), x, y);
        }
    }
}
