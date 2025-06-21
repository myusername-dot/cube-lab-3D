package io.github.labyrinthgenerator.additional;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.Random;

public class Vector2iSeedHash extends Vector2i {
    private static final Random random = new Random();

    public Vector2iSeedHash(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2iSeedHash vector2 = (Vector2iSeedHash) o;
        return x == vector2.x && y == vector2.y;
    }

    // only for stream limit
    @Override
    public int hashCode() {
        random.setSeed((long) x * y);
        return random.nextInt();
    }

    public Double getDistance(Vector2iSeedHash vector2) {
        int dx = vector2.x - x;
        int dy = vector2.y - y;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}
