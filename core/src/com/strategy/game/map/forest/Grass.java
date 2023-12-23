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
    private Random random = new Random();


    public Grass(Season startGrowth,
                 Season endGrowth,
                 double lifeProbability,
                 int startSeasonBirthIter,
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
        this.startSeasonBirthIter = startSeasonBirthIter;
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
        if (!currentSeason.equals(Season.Autumn.toString())) {
            setGrowthStatus(getLastGrowthStatus() + 1);
        }
        int oldAge = getAge();
        if (getAge() < getAgeThreshold()) {
            if (random.nextInt(2) == 0) {
                setAge(getAge() + 1);
            }
        }
        // tile changing
        if (oldAge < getAge()) {
            setTile(getTiles()[0][getAge() - 1]);
        }

        if (getTile().equals(getTiles()[0][2]) && currentSeason.equals(Season.Autumn.toString())) {
            if (random.nextInt(getGrowthStatus()) == 0) {
                setGrowthStatus(getGrowthStatus() - 1);
                setTile(getTiles()[0][3]);
            }
        }
        else if (currentSeason.equals(Season.Autumn.toString()) && getTile().equals(getTiles()[0][3])) {
            if (random.nextInt(getGrowthStatus() * getAge() + (((getAge() * getAge()) / 2))) == 0) {
                setGrowthStatus(getGrowthStatus() - 1);
                setAlive(false);
            }
        }
        if (currentSeason.equals(Season.Winter.toString())) {
            if (isAlive()) {
                    setTile(getTiles()[0][3]);
            }
            if (random.nextInt(3) < 2) {
                setAlive(false);
            }
        }
        return getAge();
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
