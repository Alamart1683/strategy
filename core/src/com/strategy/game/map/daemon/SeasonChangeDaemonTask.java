package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;

@Getter
public class SeasonChangeDaemonTask extends TimerTask {
    private TileChange tileChange;
    private ForestChange forestChange;

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

    // rendering plants
    public void renderPlants() {
        forestChange.getSpriteBatch().begin();

        int mapWidth = forestChange.getCurrTreesInForest().length;
        int mapHeight = forestChange.getCurrTreesInForest()[0].length;

        // Create a list to hold sprites with their positions
        List<SpritePosition> spritePositions = new ArrayList<>();

        // Collect all sprites with their positions
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                if (forestChange.getCurrTreesInForest()[i][j] != null) {
                    Sprite sprite = new Sprite(forestChange.getCurrTreesInForest()[i][j].getTile());
                    float x = i * forestChange.getMap().getTileWidth() - forestChange.getMap().getTileWidth() / 2;
                    float y = j * forestChange.getMap().getTileHeight();
                    if (forestChange.getCurrTreesInForest()[i][j].getDepth() == 2) {
                        y += forestChange.getMap().getTileHeight() / 2;
                    }
                    spritePositions.add(new SpritePosition(sprite, x, y, forestChange.getCurrTreesInForest()[i][j].getDepth()));
                }
            }
        }

        // Sort the sprites by y-coordinate and depth
        spritePositions.sort((sp1, sp2) -> {
            // First sort by y-coordinate (ascending)
            if (sp1.y != sp2.y) {
                return Float.compare(sp2.y, sp1.y); // Inverted to sort higher y first
            } else {
                // Then sort by depth (ascending)
                return Integer.compare(sp1.depth, sp2.depth);
            }
        });

        // Draw the sorted sprites
        for (SpritePosition sp : spritePositions) {
            sp.sprite.setPosition(sp.x, sp.y);
            sp.sprite.draw(forestChange.getSpriteBatch());
        }

        forestChange.getSpriteBatch().end();
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