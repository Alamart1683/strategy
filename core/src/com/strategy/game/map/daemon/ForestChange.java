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
            TextureRegion[][] tiles = TextureRegion.split(texture, map.getTileWidth(), map.getTileHeight());
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
        Gdx.app.postRunnable(() -> {
            spriteBatch.begin();
            if (tree == null) {
                currTreesInForest[x][y] = null;
                Sprite treeSprite = new Sprite(map.getTransparent());
                treeSprite.setPosition(x, y);
                treeSprite.draw(spriteBatch);;
            } else {
                Sprite treeSprite = new Sprite(tree.getTile());
                treeSprite.setPosition(x, y);
                currTreesInForest[x][y] = tree;
                treeSprite.draw(spriteBatch);
            }
            spriteBatch.end();
        });

    }

    public void initializeForest() {
        for (int i = 0; i < trees.size() * trees.size(); i++) {
            for (int j = 0; j < trees.size(); j++) {
                int x = random.nextInt(map.getWidth());
                int y = random.nextInt(map.getHeight());
                // More uniform tree growth
                if (x < map.getWidth() / 5)
                    x = map.getWidth() / 5;
                if (x > map.getWidth() - 5)
                    x = map.getWidth() - 5;
                if (y < map.getHeight() / 5)
                    y = map.getHeight() / 5;
                if (y > map.getHeight() - 5)
                    y = map.getHeight() - 5;
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

            boolean xMinus1y = false;
            if (x - 1 >= map.getWidth() || x - 1 < 0)
                xMinus1y = true;
            else if (currTreesInForest[x - 1][y].getTile().equals(map.getTransparent()))
                xMinus1y = true;

            boolean xPlus1y = false;
            if (x + 1 >= map.getWidth())
                xPlus1y = true;
            else if (currTreesInForest[x + 1][y].getTile().equals(map.getTransparent()))
                xPlus1y = true;

            boolean xyPlus1 = false;
            if (y + 1 >= map.getHeight())
                xyPlus1 = true;
            else if (currTreesInForest[x][y + 1].getTile().equals(map.getTransparent()))
                xyPlus1 = true;

            boolean xyMinus1 = false;
            if (y - 1 >= map.getHeight()|| y - 1 < 0)
                xyMinus1 = true;
            else if (currTreesInForest[x][y - 1].getTile().equals(map.getTransparent()))
                xyMinus1 = true;

            boolean xPlus1yPlus1 = false;
            if (x + 1 >= map.getWidth() || y + 1 >= map.getHeight())
                xPlus1yPlus1 = true;
            else if (currTreesInForest[x + 1][y + 1].getTile().equals(map.getTransparent()))
                xPlus1yPlus1 = true;

            boolean xMinus1yPlus1 = false;
            if (x - 1 >= map.getWidth() || x - 1 < 0 || y + 1 >= map.getHeight())
                xMinus1yPlus1 = true;
            else if (currTreesInForest[x - 1][y + 1].getTile().equals(map.getTransparent()))
                xMinus1yPlus1 = true;

            boolean xPlus1yMinus1 = false;
            if (x + 1 >= map.getWidth() || y - 1 >= map.getHeight() || y - 1 < 0)
                xPlus1yMinus1 = true;
            else if (currTreesInForest[x + 1][y - 1].getTile().equals(map.getTransparent()))
                xPlus1yMinus1 = true;

            boolean xMinus1yMinus1 = false;
            if (x - 1 >= map.getWidth() || x - 1 < 0 || y - 1 >= map.getHeight() || y - 1 < 0)
                xMinus1yMinus1 = true;
            else if (currTreesInForest[x - 1][y - 1].getTile().equals(map.getTransparent()))
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
}
