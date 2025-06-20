package io.github.labyrinthgenerator.pages.game3d.chunks;

import com.badlogic.gdx.math.Vector2;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;

import java.util.Objects;

public class Chunk {
    public final float x;
    public final float z;
    public final int width = Constants.CHUNK_SIZE;
    public final int depth = Constants.CHUNK_SIZE;
    public final Vector2 center;

    public Chunk(float x, float z) {
        this.x = x;
        this.z = z;
        center = new Vector2(x + width / 2f, z + depth / 2f);
    }

    public boolean contains(float x, float z) {
        return this.x <= x && this.x + this.width >= x && this.z <= z && this.z + this.depth >= z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x && z == chunk.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "Chunk[" + "x=" + x + ", z=" + z + ']';
    }
}
