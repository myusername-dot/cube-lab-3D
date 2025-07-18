package io.github.labyrinthgenerator.pages.game3d.chunks;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManagedData;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.Objects;

public class Chunk {

    public final float x;
    public final float y;
    public final float z;
    public final int width = Constants.CHUNK_SIZE;
    public final int height = Constants.CHUNK_SIZE;
    public final int depth = Constants.CHUNK_SIZE;
    public final Vector3 center;

    private final ChunkManager chunkMan;
    private final Vector3i worldSize;

    public final ChunkManagedData data;

    public Chunk(final ChunkManager chunkMan, float x, float y, float z) {
        this.chunkMan = chunkMan;
        this.worldSize = chunkMan.getWorldSize();
        this.x = x;
        this.y = y;
        this.z = z;
        this.center = new Vector3(x + width / 2f, y + height / 2f, z + depth / 2f);
        data = new ChunkManagedData();
    }

    public boolean contains(float x, float y, float z) {
        x = MathUtils.clamp(x, 0, worldSize.x);
        y = MathUtils.clamp(y, 0, worldSize.y);
        z = MathUtils.clamp(z, 0, worldSize.z);
        // @formatter:off
        return this.x <= x && this.x + this.width  >= x
            && this.y <= y && this.y + this.height >= y
            && this.z <= z && this.z + this.depth  >= z;
        // @formatter:on
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        // @formatter:off
        return Float.compare(chunk.x, x) == 0
            && Float.compare(chunk.y, y) == 0
            && Float.compare(chunk.z, z) == 0;
        // @formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Chunk[x=" + x + ", y=" + y + ", z=" + z + ']';
    }
}
