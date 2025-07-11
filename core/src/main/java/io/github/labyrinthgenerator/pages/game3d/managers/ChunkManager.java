package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.CHUNK_SIZE;

public class ChunkManager {

    private final Vector3i worldSize;

    private final Vector3i chunksSize;
    private final Chunk[][][] chunks;

    public ChunkManager(Vector3i worldSize) {
        this.worldSize = worldSize.cpy();
        this.chunksSize = new Vector3i(
            worldSize.x / CHUNK_SIZE + 1,
            worldSize.y / CHUNK_SIZE + 1,
            worldSize.z / CHUNK_SIZE + 1
        );
        this.chunks = new Chunk[chunksSize.x][chunksSize.y][chunksSize.z];
    }

    public Chunk add(float x, float y, float z) {
        Vector3i position = getChunkPosition(x, y, z);
        if (chunks[position.x][position.y][position.z] == null) {
            Chunk chunk = createChunk(position);
            chunks[position.x][position.y][position.z] = chunk;
            return chunk;
        } else {
            throw new RuntimeException("Chunk at position: " + position + " already exists.");
        }
    }

    private Chunk createChunk(Vector3i position) {
        return new Chunk(position.x * CHUNK_SIZE, position.y * CHUNK_SIZE, position.z * CHUNK_SIZE);
    }

    public Chunk get(float x, float y, float z) {
        Vector3i position = getChunkPosition(x, y, z);
        Chunk chunk = chunks[position.x][position.y][position.z];
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + y + ", " + z + " is null.");
        }
        return chunk;
    }

    public List<Chunk> getNearestChunks(final Vector3 pos, int offsetChunks) {
        List<Chunk> nearestChunks = new ArrayList<>();
        Vector3i position = getChunkPosition(pos.x, pos.y, pos.z);
        int x1 = Math.max(0, position.x - offsetChunks);
        int y1 = Math.max(0, position.y - offsetChunks / 2);
        int z1 = Math.max(0, position.z - offsetChunks);
        int x2 = Math.min(chunksSize.x - 1, position.x + offsetChunks);
        int y2 = Math.min(chunksSize.y - 1, position.y + offsetChunks / 2);
        int z2 = Math.min(chunksSize.z - 1, position.z + offsetChunks);

        for (int i = x1; i <= x2; i++)
            for (int j = y1; j <= y2; j++)
                for (int k = z1; k <= z2; k++)
                    nearestChunks.add(chunks[i][j][k]);

        return nearestChunks;
    }

    public Vector3i getWorldSize() {
        return worldSize.cpy();
    }

    public Vector3i getChunksSize() {
        return chunksSize.cpy();
    }

    private Vector3i getChunkPosition(float x, float y, float z) {
        x = MathUtils.clamp(x, 0, worldSize.x);
        y = MathUtils.clamp(y, 0, worldSize.y);
        z = MathUtils.clamp(z, 0, worldSize.z);
        return new Vector3i(
            (int) (x / CHUNK_SIZE),
            (int) (y / CHUNK_SIZE),
            (int) (z / CHUNK_SIZE)
        );
    }

    public void clear() {
        for (int i = 0; i < chunksSize.x; i++) chunks[i] = null;
    }
}
