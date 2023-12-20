package com.strategy.game.map.forest;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.strategy.game.map.terrain.Season;
import lombok.Getter;

import java.util.Random;

@Getter
public class Grass extends Plant {
    private Season startGrowth;
    private Season endGrowth;
    private double lifeProbability;

    private int startSeasonBirthIter;

    public Grass(Season startGrowth,
                 Season endGrowth,
                 double lifeProbability,
                 int startSeasonBrithIter,
                 String plantName,
                 PlantType plantType,
                 int growthStep,
                 int growthStatus,
                 int growthThreshold,
                 int age,
                 int ageThreshold,
                 int fertility,
                 TextureRegion sprite,
                 TextureRegion[][] tiles) {
        super(plantName, plantType, growthStep, growthStatus, growthThreshold, age, ageThreshold, fertility, sprite, tiles);
        this.startGrowth = startGrowth;
        this.endGrowth = endGrowth;
        this.lifeProbability = lifeProbability;
        this.startSeasonBirthIter = startSeasonBrithIter;
    }

    public Grass(Grass grass) {
        super(
                grass.getPlantName(),
                grass.getPlantType(),
                grass.getGrowthStep(),
                grass.getGrowthStatus(),
                grass.getGrowthThreshold(),
                grass.getAge(),
                grass.getAgeThreshold(),
                grass.getFertility(),
                grass.getTile(),
                grass.getTiles()
        );
        this.startGrowth = grass.getStartGrowth();
        this.endGrowth = grass.getEndGrowth();
        this.lifeProbability = grass.getLifeProbability();
        this.startSeasonBirthIter = grass.getStartSeasonBirthIter();
    }

    @Override
    public int grow(String currentSeason) {
        setAge(getAge() + 1);
        int newGrowthStatus = 0;
        // Death trigger
        Random random = new Random(getAge());
        if (!isAlive()) {
            setTile(getTiles()[0][4]);
        }
        else if (isGrassAlive(Season.valueOf(currentSeason))) {
            setAlive(false);
            return newGrowthStatus;
        }
        // Growth trigger
        if (getAge() % getGrowthStep() == 0 && getGrowthStatus() < getGrowthThreshold()) {
            setLastGrowthStatus(getGrowthStatus());
            newGrowthStatus = getLastGrowthStatus() + 1;
            setGrowthStatus(newGrowthStatus);
        }
        // Change sprite trigger
        if (isAlive() && !currentSeason.equals(endGrowth.name())) {
            setTile(getTiles()[0][getAge() - 1]);
        }
        else if (isAlive()) {
            setTile(getTiles()[0][3]);
            if (random.nextInt((int)getLifeProbability() * 10) == 0) {
                setAlive(false);
            }
        }
        else { // if grass is dead
            setTile(getTiles()[0][4]);
        }

        return newGrowthStatus;
    }

    @Override
    public int determineSeason(String currentSeason) {
        switch (currentSeason) {
            case "Autumn" -> {
                return 3;
            }
            case "Winter" -> {
                return 4;
            }
            default -> {
                return 1;
            }
        }
    }

    public boolean isGrassAlive(Season currentSeason) {
        return currentSeason != Season.Winter;
    }

}
