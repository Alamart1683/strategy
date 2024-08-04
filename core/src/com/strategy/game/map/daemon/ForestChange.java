package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
        int width = map.getWidth();
        int height = map.getHeight();
        Tree[][] forest = new Tree[width][height];
        return forest;
    }

    private void loadTrees(String climate, Season startSeason) throws IOException {
        List<Path> paths = Files.list(Path.of("assets/tiles/climate/" + climate + "/forest/trees")).toList();
        for (Path path : paths) {
            Texture texture = new Texture(path.toString());
            TextureRegion[][] tiles = TextureRegion.split(texture, 128, 128);
            Tree tree = new Tree(
                    path.getFileName().toString(),
                    PlantType.Tree,
                    4, 1, 3, 1, 30, 6,
                    determineStartTreeTile(tiles, startSeason),
                    tiles
            );
            trees.add(tree);
        }
    }

    private TextureRegion determineStartTreeTile(TextureRegion[][] tiles, Season startSeason) {
        return switch (startSeason) {
            case Spring -> tiles[0][0];
            case Summer -> tiles[0][1];
            case Autumn -> tiles[0][2];
            case Winter -> tiles[0][3];
            default -> tiles[0][4];
        };
    }

    private void setTree(Tree tree, int x, int y) {
        currTreesInForest[x][y] = tree;
    }

    public void initializeForest() {
        int width = map.getWidth();
        int height = map.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (random.nextBoolean()) {
                    Tree tree = new Tree(trees.get(random.nextInt(trees.size())));
                    tree.setDepth(determineTreeDepth(i, j));
                    setTree(tree, i, j);
                }
            }
        }
    }

    public void nextForestGrowsIter(Season currentSeason, int currentSeasonIter) {
        int width = map.getWidth();
        int height = map.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Tree tree = currTreesInForest[i][j];
                if (tree != null) {
                    if (!tree.isAlive()) {
                        setTree(null, i, j);
                    } else {
                        tree.grow(currentSeason.name(), currentSeasonIter);
                        if (tree.getAge() % tree.getFertility() == 0 &&
                                tree.getGrowthStatus() == tree.getGrowthThreshold() &&
                                currentSeason != Season.Winter && currentSeason != Season.Autumn) {
                            growNewTree(tree, i, j, currentSeason);
                        }
                    }
                }
            }
        }
    }

    private void growNewTree(Tree tree, int i, int j, Season currentSeason) {
        for (int k = 0; k < tree.getFertility(); k++) {
            if (random.nextInt(tree.getFertility()) == 0) {
                int x = i + random.nextInt(-1, 2);
                int y = j + random.nextInt(-1, 2);
                if (isValidPosition(x, y) && currTreesInForest[x][y] == null) {
                    Tree newTree = new Tree(determineTree(tree));
                    newTree.setTile(determineStartTreeTile(newTree.getTiles(), currentSeason));
                    newTree.setDepth(determineTreeDepth(x, y));
                    setTree(newTree, x, y);
                    updateTreeFertility(newTree);
                    return;
                }
            }
        }
    }

    private Tree determineTree(Tree tree) {
        return trees.stream()
                .filter(t -> t.getPlantName().equals(tree.getPlantName()))
                .findFirst()
                .orElse(trees.get(0));
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight();
    }

    private void updateTreeFertility(Tree tree) {
        if (tree.getFertility() > 1) {
            tree.setFertility(Math.max(tree.getFertility() - tree.getGrowthStep(), 1));
        }
    }

    private int determineTreeDepth(int x, int y) {
        if (x > 0 && currTreesInForest[x - 1][y] != null) {
            return currTreesInForest[x - 1][y].getDepth() == 1 ? 2 : 1;
        } else if (x < currTreesInForest.length - 1 && currTreesInForest[x + 1][y] != null) {
            return currTreesInForest[x + 1][y].getDepth() == 1 ? 2 : 1;
        }
        return 1;
    }
}
