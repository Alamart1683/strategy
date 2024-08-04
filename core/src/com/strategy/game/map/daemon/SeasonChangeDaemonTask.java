package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.strategy.game.map.forest.Tree;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

@Getter
public class SeasonChangeDaemonTask extends TimerTask {
    private final TileChange tileChange;
    private final ForestChange forestChange;

    private Season currentSeason;
    private int currentIter;
    private int yearCount;
    private int year;

    public SeasonChangeDaemonTask(TileChange tileChange, ForestChange forestChange) {
        this.tileChange = tileChange;
        this.forestChange = forestChange;
        this.currentSeason = tileChange.getCurrentSeason();
        this.currentIter = tileChange.getCurrentSeasonIter();
        this.yearCount = 0;
        this.year = 0;
    }

    @Override
    public void run() {
        currentIter = tileChange.temperateSeasonChanging(currentIter);
        if (currentIter == 9) {
            currentSeason = tileChange.determineTemperateNextSeason();
            yearCount++;
        }
        forestChange.nextForestGrowsIter(currentSeason, currentIter);
        if (yearCount == 4) {
            year++;
            yearCount = 0;
        }
    }

    // Rendering plants in isometric view
    public void renderPlants() {
        SpriteBatch spriteBatch = forestChange.getSpriteBatch();
        spriteBatch.begin();

        Tree[][] currTreesInForest = forestChange.getCurrTreesInForest();
        int mapWidth = currTreesInForest.length;
        int mapHeight = currTreesInForest[0].length;
        float tileWidth = forestChange.getMap().getTileWidth();
        float tileHeight = forestChange.getMap().getTileHeight();

        List<SpritePosition> spritePositions = new ArrayList<>();

        // Collect sprites with their positions
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                Tree tree = currTreesInForest[i][j];
                if (tree != null) {
                    Sprite sprite = new Sprite(tree.getTile());

                    // Convert from 2D grid coordinates to isometric coordinates
                    float x = (i - j) * tileWidth / 2;
                    float y = (i + j) * tileHeight / 2;

                    // Adjust y position for depth
                    y -= tree.getDepth() * tileHeight / 2;

                    spritePositions.add(new SpritePosition(sprite, x, y, tree.getDepth()));
                }
            }
        }

        // Sort sprites by y-coordinate and depth for correct rendering order
        spritePositions.sort((sp1, sp2) -> {
            // First sort by y-coordinate (ascending)
            if (sp1.y != sp2.y) {
                return Float.compare(sp2.y, sp1.y); // Inverted to sort higher y first
            } else {
                // Then sort by depth (ascending)
                return Integer.compare(sp1.depth, sp2.depth);
            }
        });

        // Draw sprites
        for (SpritePosition sp : spritePositions) {
            sp.sprite.setPosition(sp.x, sp.y);
            sp.sprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private static class SpritePosition {
        Sprite sprite;
        float x, y;
        int depth;

        SpritePosition(Sprite sprite, float x, float y, int depth) {
            this.sprite = sprite;
            this.x = x;
            this.y = y;
            this.depth = depth;
        }
    }
}