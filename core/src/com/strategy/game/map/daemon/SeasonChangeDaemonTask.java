package com.strategy.game.map.daemon;

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
}
