package com.strategy.game.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;

@Getter
public class SuitableTerrain {
    private final TextureRegion tile;
    private final int x;
    private final int y;

    public SuitableTerrain(TextureRegion tile, int x, int y) {
        this.tile = tile;
        this.x = x;
        this.y = y;
    }
}
