package io.github.labyrinthgenerator.pages.game3d.tickable;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;

public abstract class Tickable {

    protected final CubeLab3D game;

    protected boolean shouldTick = true;
    protected boolean shouldClear = true;

    public Tickable(CubeLab3D game) {
        this.game = game;
        game.getTickMan().addEntity(this);
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public boolean shouldClear() {
        return shouldClear;
    }

    public void tick(final float delta) {
    }
}
