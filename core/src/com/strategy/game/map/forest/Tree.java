package com.strategy.game.map.forest;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

public class Tree extends Plant {
    private boolean isGrow;
    private final Random random = new Random();

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
    public int grow(String currentSeason, int currentSeasonIter) {
        if (currentSeasonIter == 9) {
            growAnnualCycle(currentSeason);
        } else if (currentSeasonIter > 1 && currentSeasonIter < 7 && isGrow) {
            if (shouldUpdateAge(currentSeasonIter)) {
                updateTreeAge(currentSeason);
                isGrow = false;
            }
        } else {
            handleSeasonalChange(currentSeason, currentSeasonIter);
        }
        return 0;
    }

    private void growAnnualCycle(String currentSeason) {
        setAge(getAge() + 1);
        Random random = new Random(getAge());
        if (getAge() > getAgeThreshold()) {
            setTile(getTiles()[getGrowthStatus() - 1][4]);
            if (random.nextInt(getAgeThreshold() * 3 / 10 - getAge()) == 0) {
                setAlive(false);
            }
        }
        if (getAge() % getGrowthStep() == 0 && getGrowthStatus() < getGrowthThreshold()) {
            isGrow = true;
            setGrowthStatus(getGrowthStatus() + 1);
        }
    }

    private boolean shouldUpdateAge(int currentSeasonIter) {
        int chance = 6 - currentSeasonIter;
        return random.nextInt(2) == 0 && currentSeasonIter >= 2 && currentSeasonIter <= 6 && chance >= 0;
    }

    private void handleSeasonalChange(String currentSeason, int currentSeasonIter) {
        if (currentSeasonIter <= 2) {
            if (random.nextInt(3 - currentSeasonIter) == 0) {
                updateTreeSeason(determineTemperatePrevSeason(currentSeason), currentSeason);
            }
        } else if (currentSeasonIter >= 6) {
            int chance = 12 - 3 * (currentSeasonIter - 6);
            if (random.nextInt(chance) == 0) {
                updateTreeSeason(currentSeason, determineTemperateNextSeason(currentSeason));
            }
        }
    }

    private void updateTreeAge(String currentSeason) {
        if (!getTile().equals(getTiles()[getGrowthStatus() - 1][4])) {
            if (getAge() <= getAgeThreshold()) {
                setTile(getTiles()[getGrowthStatus() - 1][determineSeason(currentSeason)]);
            }
        }
    }

    private void updateTreeSeason(String currentSeason, String newSeason) {
        if (!getTile().equals(getTiles()[getGrowthStatus() - 1][4])) {
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
