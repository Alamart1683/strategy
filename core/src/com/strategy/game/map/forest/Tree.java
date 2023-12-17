package com.strategy.game.map.forest;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

public class Tree extends Plant {

    public Tree(String plantName, PlantType plantType, int growthStep, int growthStatus, int growthThreshold, int age, int ageThreshold, int fertility, TextureRegion tile, TextureRegion[][] tiles) {
        super(plantName, plantType, growthStep, growthStatus, growthThreshold, age, ageThreshold, fertility, tile, tiles);
    }

    public Tree(Plant plant) {
        super(
                plant.getPlantName(),
                plant.getPlantType(),
                plant.getGrowthStep(),
                plant.getGrowthStatus(),
                plant.getGrowthThreshold(),
                plant.getAge(),
                plant.getAgeThreshold(),
                plant.getFertility(),
                plant.getTile(),
                plant.getTiles()
        );
    }

    @Override
    public int grow(String currentSeason) {
        setAge(getAge() + 1);
        int newGrowthStatus = 0;
        // Death trigger
        Random random = new Random(getAge());
        if (getAge() > getAgeThreshold()) {
            if (random.nextInt(getAgeThreshold() + getAgeThreshold() / 10 - getAge()) == 0) {
                setAlive(false);
                return newGrowthStatus;
            }
        }
        // Growth trigger
        if (getAge() % getGrowthStep() == 0 && getGrowthStatus() < getGrowthThreshold()) {
            setLastGrowthStatus(getGrowthStatus());
            newGrowthStatus = getLastGrowthStatus() + 1;
            setGrowthStatus(newGrowthStatus);
        }
        // Change sprite trigger
        setTile(getTiles()[getGrowthStatus() - 1][determineSeason(currentSeason)]);
        return newGrowthStatus;
    }

}
