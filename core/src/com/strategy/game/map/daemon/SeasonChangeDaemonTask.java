package com.strategy.game.map.daemon;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;

import java.util.TimerTask;

@Getter
public class SeasonChangeDaemonTask extends TimerTask {
    private TileChange tileChange;
    private ForestChange forestChange;
    // private GrassChange grassChange;

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
        //grassChange.nextGrassGrowsIter(currentSeason, currentSeasonIter);
        if (yearCount == 4) {
            year++;
            yearCount = 0;
        }
    }

    // rendering plants
    public void renderPlants() {
        forestChange.getSpriteBatch().begin();
        for (int i = 0; i  < forestChange.getCurrTreesInForest().length; i++) {
            for (int j = forestChange.getCurrTreesInForest()[0].length - 1; j > 0; j--) {
                if (forestChange.getCurrTreesInForest()[i][j] != null) {
                    Sprite sprite = new Sprite(forestChange.getCurrTreesInForest()[i][j].getTile());
                    if (forestChange.getCurrTreesInForest()[i][j].getDepth() == 1) {
                        sprite.setPosition(i * forestChange.getMap().getTileWidth() - forestChange.getMap().getTileWidth() / 2, j * forestChange.getMap().getTileHeight());
                    }
                    else if (forestChange.getCurrTreesInForest()[i][j].getDepth() == 2) {
                        sprite.setPosition(i * forestChange.getMap().getTileWidth() - forestChange.getMap().getTileWidth() / 2, j * forestChange.getMap().getTileHeight() + forestChange.getMap().getTileHeight() / 2);
                    }
                    sprite.draw(forestChange.getSpriteBatch());
                }
            }
        }
        forestChange.getSpriteBatch().end();
    }
}
