package com.strategy.game.map.forest;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

public class Tree extends Plant {
    private int currentSeasonIter;
    private Random random = new Random();

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
        currentSeasonIter = 0;
    }

    @Override
    public int grow(String currentSeason, int currentSeasonIter) {
        if (currentSeasonIter == 9) {
            setAge(getAge() + 1);
            int newGrowthStatus = 0;
            // Death trigger
            Random random = new Random(getAge());
            if (getAge() > getAgeThreshold()) {
                if (random.nextInt(getAgeThreshold() + getAgeThreshold() / 10 - getAge()) == 0) {
                    setAlive(false);
                }
            }
            // Growth trigger
            if (getAge() % getGrowthStep() == 0 && getGrowthStatus() < getGrowthThreshold()) {
                setLastGrowthStatus(getGrowthStatus());
                newGrowthStatus = getLastGrowthStatus() + 1;
                setGrowthStatus(newGrowthStatus);
            }
            // Change growth sprite trigger
            if (getAge() <= getAgeThreshold())
                setTile(getTiles()[getGrowthStatus() - 1][determineSeason(currentSeason)]);
            else // if tree is dead
                setTile(getTiles()[getGrowthStatus() - 1][4]);
            return newGrowthStatus;
        } else {
            switch (currentSeasonIter) {
                case 6 -> {
                    if (random.nextInt(8) == 0) {
                        updateTreeSeason(currentSeason, determineTemperateNextSeason(currentSeason));
                    }
                }
                case 7 -> {
                    if (random.nextInt(5) == 0) {
                        updateTreeSeason(currentSeason, determineTemperateNextSeason(currentSeason));
                    }
                }
                case 8 -> {
                    if (random.nextInt(2) == 0) {
                        updateTreeSeason(currentSeason, determineTemperateNextSeason(currentSeason));
                    }
                }
            }
        }
        return 0;
    }

    private void updateTreeSeason(String currentSeason, String newSeason) {
        if (currentSeason.equals("Summer")) {
            setTile(getTiles()[getGrowthStatus() - 1][determineSeason(newSeason)]);
        }
        if (currentSeason.equals("Autumn")) {
            setTile(getTiles()[getGrowthStatus() - 1][determineSeason(newSeason)]);
        }
        if (currentSeason.equals("Winter")) {
            setTile(getTiles()[getGrowthStatus() - 1][determineSeason(newSeason)]);
        }
        if (currentSeason.equals("Spring")) {
            setTile(getTiles()[getGrowthStatus() - 1][determineSeason(newSeason)]);
        }
    }

    public String determineTemperateNextSeason(String currentSeason) {
        return switch (currentSeason) {
            case "Summer" -> "Autumn";
            case "Autumn" -> "Winter";
            case "Winter" -> "Spring";
            default -> "Summer";
        };
    }

    public String determineTemperatePrevSeason(String currentSeason) {
        return switch (currentSeason) {
            case "Summer" -> "Spring";
            case "Autumn" -> "Summer";
            case "Winter" -> "Autumn";
            default -> "Winter";
        };
    }
}
