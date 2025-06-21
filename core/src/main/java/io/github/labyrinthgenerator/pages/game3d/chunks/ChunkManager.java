package io.github.labyrinthgenerator.pages.game3d.chunks;

import com.badlogic.gdx.math.Vector2;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;

public class ChunkManager {

    private final Vector2i size;

    private final Chunk[][] chunks;

    public ChunkManager(Vector2i size) {
        this.size = size;
        chunks = new Chunk[size.x][size.y];
    }

    public Chunk add(float x, float z) {
        Vector2i position = getChunkPosition(x, z);
        if (chunks[position.x][position.y] == null) {
            Chunk chunk = new Chunk(position.x * CHUNK_SIZE - HALF_UNIT, position.y * CHUNK_SIZE - HALF_UNIT);
            chunks[position.x][position.y] = chunk;
        } else {
            throw new RuntimeException("Chunk at position: " + position + " already exists.");
        }
        return chunks[position.x][position.y];
    }

    public Chunk get(float x, float z) {
        Vector2i position = getChunkPosition(x, z);
        Chunk chunk = chunks[position.x][position.y];
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
        }
        return chunk;
    }

    public List<Chunk> getNearestChunks(float playerX, float playerZ) {
        return getNearestChunksInBox(playerX, playerZ, 0);
    }

    public List<Chunk> getNearestChunksInBox(float playerX, float playerZ, int offsetChunks) {
        List<Chunk> nearestChunks = new ArrayList<>();
        Vector2i position = getChunkPosition(playerX, playerZ);
        int x1Position = position.x - CHUNKS_RANGE_AROUND_CAM_CHUNK - offsetChunks;
        int y1Position = position.y - CHUNKS_RANGE_AROUND_CAM_CHUNK - offsetChunks;
        int x2Position = position.x + CHUNKS_RANGE_AROUND_CAM_CHUNK + offsetChunks;
        int y2Position = position.y + CHUNKS_RANGE_AROUND_CAM_CHUNK + offsetChunks;
        if (x1Position < 0) x1Position = 0;
        if (y1Position < 0) y1Position = 0;
        if (x2Position > size.x - 1) x2Position = size.x - 1;
        if (y2Position > size.y - 1) y2Position = size.y - 1;
        for (int i = x1Position; i <= x2Position; i++) {
            for (int j = y1Position; j <= y2Position; j++) {
                nearestChunks.add(chunks[i][j]);
            }
        }
        return nearestChunks;
    }

    public Vector2 getWorldSize() {
        return new Vector2(size.x * CHUNK_SIZE - HALF_UNIT, size.y * CHUNK_SIZE - HALF_UNIT);
    }

    private Vector2i getChunkPosition(float x, float z) {
        return new Vector2i((int) ((x + HALF_UNIT) / CHUNK_SIZE), (int) ((z + HALF_UNIT) / CHUNK_SIZE));
    }

    public void clear() {
        for (int i = 0; i < size.x; i++) {
            for (int j = 0; j < size.y; j++) {
                chunks[i][j] = null;
            }
        }
    }
}
