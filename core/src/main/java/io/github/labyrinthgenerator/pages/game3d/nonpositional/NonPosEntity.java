package io.github.labyrinthgenerator.pages.game3d.nonpositional;

import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

public abstract class NonPosEntity {

    public final GameScreen screen;

    protected boolean shouldTick = true;
    protected boolean render3D = true;

    public NonPosEntity(GameScreen screen) {
        this.screen = screen;
        screen.game.getNonPosMan().addEntity(this);
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public void tick(final float delta) {}
}
