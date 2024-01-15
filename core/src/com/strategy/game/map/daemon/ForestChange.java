package com.strategy.game.map.daemon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import java.util.TimerTask;

@Getter
public class ForestChange {
    private Map map;
    private List<Tree> trees;
    private Tree[][] currTreesInForest;
    private Random random = new Random();
    private SpriteBatch spriteBatch;
    int currentIter;

    @SneakyThrows
    public ForestChange(Map map, String climate, Season startSeason, SpriteBatch spriteBatch) {
        this.map = map;
        this.trees = new ArrayList<>();
        this.currentIter = 0;
        this.currTreesInForest = initializeCurrTreesInForest();
        this.spriteBatch = spriteBatch;
        loadTrees(climate, startSeason);
        initializeForest();
    }

    private Tree[][] initializeCurrTreesInForest() {
        currTreesInForest = new Tree[map.getWidth()][map.getHeight()];
        for (int i = 0; i < currTreesInForest.length; i++) {
            for (int j = 0; j < currTreesInForest[0].length; j++) {
                currTreesInForest[i][j] = null;
            }
        }
        return currTreesInForest;
    }

    private void loadTrees(String climate, Season startSeason) throws IOException {
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest/trees")).toList();
        for (Path path: paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, 128, 128);
            Tree tree = new Tree(
                    path.getFileName().toString(),
                    PlantType.Tree,
                    4,1, 3, 1, 30, 6,
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

    private void setTree(Tree tree, int x, int y) {
        if (tree == null) {
            currTreesInForest[x][y] = null;
        } else {
            currTreesInForest[x][y] = tree;
        }
    }

    public void initializeForest() {
        for (int i = 0; i < trees.size() * trees.size(); i++) {
            for (int j = 0; j < trees.size(); j++) {
                int x = random.nextInt(map.getWidth());
                int y = random.nextInt(map.getHeight());
                Tree tree = new Tree(trees.get(j));
                setTree(tree, x, y);
            }
        }
    }

    public void nextForestGrowsIter(Season currentSeason, int currentSeasonIter) {
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                if (currTreesInForest[i][j] != null) {
                    if (!currTreesInForest[i][j].isAlive()) {
                        setTree(null, i, j);
                        currTreesInForest[i][j] = null;
                    }
                    else {
                        currTreesInForest[i][j].grow(currentSeason.name(), currentSeasonIter);
                        setTree(currTreesInForest[i][j], i, j);
                        if (currTreesInForest[i][j].getAge() % currTreesInForest[i][j].getFertility() == 0 &&
                                currTreesInForest[i][j].getGrowthStatus() == currTreesInForest[i][j].getGrowthThreshold() &&
                                currentSeason != Season.Winter && currentSeason != Season.Autumn
                        ) {
                            growNewTree(currTreesInForest[i][j], i, j, currentSeason);
                        }
                    }
                }
            }
        }
    }

    private void growNewTree(Tree tree, int i, int j, Season currentSeason) {
        for (int k = 0; k < tree.getFertility(); k++) {
            if (random.nextInt(tree.getFertility()) == 0) {
                int x, y;
                if (k % 2 == 0) {
                    x = random.nextInt(i + 1, i + 3);
                    y = random.nextInt(j + 1, j + 3);
                } else if (k % 3 == 0) {
                    x = random.nextInt(i + 1, i + 3);
                    y = random.nextInt(j - 3, j - 1);
                } else if (k % 5 == 0) {
                    x = random.nextInt(i - 3, i - 1);
                    y = random.nextInt(j + 1, j + 3);
                } else {
                    x = random.nextInt(i - 3, i - 1);
                    y = random.nextInt(j - 3, j - 1);
                }
                if (checkNearTrees(x, y) && currTreesInForest[x][y] == null) {
                    Tree newTree = new Tree(determineTree(tree));
                    currTreesInForest[x][y] = newTree;
                    newTree.setTile(determineStartTreeTile(newTree.getTiles(), currentSeason));
                    setTree(newTree, x, y);
                    // With age, the ability to produce shoots decreases
                    if (currTreesInForest[x][y].getFertility() > 1) {
                        currTreesInForest[x][y].setFertility(currTreesInForest[x][y].getFertility() - currTreesInForest[x][y].getGrowthStep());
                        if (currTreesInForest[x][y].getFertility() < 1)
                            currTreesInForest[x][y].setFertility(1);
                    }
                    return;
                }
            }
        }
    }

    private Tree determineTree(Tree tree) {
        for(Tree currTree: trees) {
            if (currTree.getPlantName().equals(tree.getPlantName())) {
                return currTree;
            }
        }
        return trees.get(0);
    }

    private boolean checkNearTrees(int x, int y) {
        if (x < map.getWidth() && x >= 0 && y < map.getHeight() && y >= 0) {
            int nearWithoutBordersCase = random.nextInt(40);
            if (nearWithoutBordersCase == 0)
                return true;

            boolean xMinus1y = false, xyPlus1 = false, xPlus1y = false, xyMinus1 = false, xPlus1yPlus1 = false, xMinus1yPlus1 = false, xPlus1yMinus1 = false, xMinus1yMinus1 = false;

            if (x - 1 < 0)
                xMinus1y = true;
            else if (currTreesInForest[x - 1][y] == null)
                xMinus1y = true;

            if (x + 1 >= map.getWidth())
                xPlus1y = true;
            else if (currTreesInForest[x + 1][y] == null)
                xPlus1y = true;

            if (y + 1 >= map.getHeight())
                xyPlus1 = true;
            else if (currTreesInForest[x][y + 1] == null)
                xyPlus1 = true;

            if (y - 1 < 0)
                xyMinus1 = true;
            else if (currTreesInForest[x][y - 1] == null)
                xyMinus1 = true;

            if (x + 1 >= map.getWidth() || y + 1 >= map.getHeight())
                xPlus1yPlus1 = true;
            else if (currTreesInForest[x + 1][y + 1] == null)
                xPlus1yPlus1 = true;

            if (x - 1 < 0 || y + 1 >= map.getHeight())
                xMinus1yPlus1 = true;
            else if (currTreesInForest[x - 1][y + 1] == null)
                xMinus1yPlus1 = true;

            if (x + 1 >= map.getWidth() || y - 1 < 0)
                xPlus1yMinus1 = true;
            else if (currTreesInForest[x + 1][y - 1] == null)
                xPlus1yMinus1 = true;

            if (x - 1 < 0 || y - 1 < 0)
                xMinus1yMinus1 = true;
            else if (currTreesInForest[x - 1][y - 1] == null)
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

    // rendering trees
    public void renderForest() {
        for (int i = currTreesInForest.length - 1; i >= 0; i--) {
            for (int j = currTreesInForest[0].length - 1; j >= 0; j--) {
                spriteBatch.begin();
                if (currTreesInForest[i][j] != null) {
                    Sprite sprite = new Sprite(currTreesInForest[i][j].getTile());
                    sprite.setPosition(i * map.getTileWidth() - map.getTileWidth() / 2, j * map.getTileHeight() + map.getTileHeight() / 2);
                    //sprite.setScale(1);
                    sprite.draw(spriteBatch);
                }
                spriteBatch.end();
            }
        }
    }

    public class TreesOnTile {
        private int x;
        private int y;
    }
}
