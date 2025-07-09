package io.github.labyrinthgenerator.pages.game3d.gravity;

public enum GravityDir {
    DOWN(0), UP(1), FORWARD(2), BACK(3), RIGHT(4), LEFT(5);

    public final int ord;

    GravityDir(int ord) {
        this.ord = ord;
    }
}
