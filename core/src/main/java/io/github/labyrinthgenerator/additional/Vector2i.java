package io.github.labyrinthgenerator.additional;

import java.util.Random;

public class Vector2i {
    private static final Random random = new Random();

    public int x;
    public int y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i v) {
        this.x = v.x;
        this.y = v.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2i vector2 = (Vector2i) o;
        return x == vector2.x && y == vector2.y;
    }

    @Override
    public int hashCode() {
        random.setSeed((long) x * y);
        return random.nextInt();
    }

    public Double getDistance(Vector2i vector2) {
        int dx = vector2.x - x;
        int dy = vector2.y - y;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}
