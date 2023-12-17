package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.Texture;
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

@Getter
public class ForestChange {
    private Map map;
    private TiledMapTileLayer forest;
    private List<Tree> trees;
    private Tree[][] currTreesInForest;
    private Random random = new Random();
    int currentIter;

    @SneakyThrows
    public ForestChange(Map map, String climate, Season startSeason) {
        this.map = map;
        this.forest = (TiledMapTileLayer) map.getMap().getLayers().get(1);
        this.trees = new ArrayList<>();
        this.currentIter = 0;
        this.currTreesInForest = initializeCurrTreesInForest();
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
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest")).toList();
        for (Path path: paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, map.getTileWidth(), map.getTileHeight());
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

    private void setTree(Tree tree, int x, int y) {
        if (tree == null) {
            currTreesInForest[x][y] = null;
            forest.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(map.getTransparent())));
        } else {
            forest.setCell(x, y, new TiledMapTileLayer.Cell().setTile(new StaticTiledMapTile(tree.getTile())));
            currTreesInForest[x][y] = tree;
        }
    }

    public void initializeForest() {
        for (int i = 0; i < trees.size() * trees.size(); i++) {
            for (int j = 0; j < trees.size(); j++) {
                int x = random.nextInt(forest.getWidth());
                int y = random.nextInt(forest.getHeight());
                // More uniform tree growth
                /*
                if (x < forest.getWidth() / 5)
                    x = forest.getWidth() / 5;
                if (x > forest.getWidth() - 5)
                    x = forest.getWidth() - 5;
                if (y < forest.getHeight() / 5)
                    y = forest.getHeight() / 5;
                if (y > forest.getHeight() - 5)
                    y = forest.getHeight() - 5;
                 */
                Tree tree = new Tree(trees.get(j));
                setTree(tree, x, y);
            }
        }
    }

    public void nextForestGrowsIter(Season currentSeason) {
        for (int i = 0; i < forest.getWidth(); i++) {
            for (int j = 0; j < forest.getHeight(); j++) {
                if (currTreesInForest[i][j] != null) {
                    if (!currTreesInForest[i][j].isAlive()) {
                        setTree(null, i, j);
                        currTreesInForest[i][j] = null;
                    }
                    else {
                        currTreesInForest[i][j].grow(currentSeason.name());
                        setTree(currTreesInForest[i][j], i, j);
                        if (currTreesInForest[i][j].getAge() % currTreesInForest[i][j].getFertility() == 0 &&
                                currTreesInForest[i][j].getGrowthStatus() == currTreesInForest[i][j].getGrowthThreshold() &&
                                currentSeason != Season.Winter && currentSeason != Season.Autumn
                        ) {
                            growNewTree(currTreesInForest[i][j], i, j);
                        }
                    }
                }
            }
        }
    }

    private void growNewTree(Tree tree, int i, int j) {
        for (int k = 0; k < tree.getFertility(); k++) {
            int x, y;
            if (k % 2 == 0) {
                x = random.nextInt(i + 1, i + 4);
                y = random.nextInt(j + 1, j + 4);
            } else if (k % 3 == 0) {
                x = random.nextInt(i + 1, i + 4);
                y = random.nextInt(j - 4, j - 1);
            } else if (k % 5 == 0) {
                x = random.nextInt(i - 4, i - 1);
                y = random.nextInt(j + 1, j + 4);
            } else {
                x = random.nextInt(i - 4, i - 1);
                y = random.nextInt(j - 4, j - 1);
            }
            if (checkNearTrees(x, y)) {
                Tree newTree = new Tree(determineTree(tree));
                currTreesInForest[x][y] = newTree;
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

    private Tree determineTree(Tree tree) {
        for(Tree currTree: trees) {
            if (currTree.getPlantName().equals(tree.getPlantName())) {
                return currTree;
            }
        }
        return trees.get(0);
    }

    private boolean checkNearTrees(int x, int y) {
        if (x < forest.getWidth() && x >= 0 && y < forest.getHeight() && y >= 0) {
            int nearWithoutBordersCase = random.nextInt(40);
            if (nearWithoutBordersCase == 0)
                return true;

            boolean xMinus1y = false;
            if (x - 1 >= forest.getWidth() || x - 1 < 0)
                xMinus1y = true;
            else if (forest.getCell(x - 1, y).getTile().getTextureRegion().equals(map.getTransparent()))
                xMinus1y = true;

            boolean xPlus1y = false;
            if (x + 1 >= forest.getWidth())
                xPlus1y = true;
            else if (forest.getCell(x + 1, y).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1y = true;

            boolean xyPlus1 = false;
            if (y + 1 >= forest.getHeight())
                xyPlus1 = true;
            else if (forest.getCell(x, y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xyPlus1 = true;

            boolean xyMinus1 = false;
            if (y - 1 >= forest.getHeight()|| y - 1 < 0)
                xyMinus1 = true;
            else if (forest.getCell(x, y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xyMinus1 = true;

            boolean xPlus1yPlus1 = false;
            if (x + 1 >= forest.getWidth() || y + 1 >= forest.getHeight())
                xPlus1yPlus1 = true;
            else if (forest.getCell(x + 1, y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1yPlus1 = true;

            boolean xMinus1yPlus1 = false;
            if (x - 1 >= forest.getWidth() || x - 1 < 0 || y + 1 >= forest.getHeight())
                xMinus1yPlus1 = true;
            else if (forest.getCell(x - 1,y + 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xMinus1yPlus1 = true;

            boolean xPlus1yMinus1 = false;
            if (x + 1 >= forest.getWidth() || y - 1 >= forest.getHeight() || y - 1 < 0)
                xPlus1yMinus1 = true;
            else if (forest.getCell(x + 1,y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
                xPlus1yMinus1 = true;

            boolean xMinus1yMinus1 = false;
            if (x - 1 >= forest.getWidth() || x - 1 < 0 || y - 1 >= forest.getHeight() || y - 1 < 0)
                xMinus1yMinus1 = true;
            else if (forest.getCell(x - 1,y - 1).getTile().getTextureRegion().equals(map.getTransparent()))
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
