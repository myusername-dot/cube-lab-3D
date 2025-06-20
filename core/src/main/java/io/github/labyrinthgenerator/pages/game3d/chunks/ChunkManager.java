package io.github.labyrinthgenerator.pages.game3d.chunks;

import com.badlogic.gdx.math.Vector2;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.*;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.CHUNK_SIZE;
import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class ChunkManager {

    private final Map<Vector2i, Chunk> chunksByPosition = new HashMap<>();

    public Chunk add(float x, float z) {
        Vector2i position = getChunkVector2i(x, z);
        if (!chunksByPosition.containsKey(position)) {
            Chunk chunk = new Chunk(position.x * CHUNK_SIZE - HALF_UNIT, position.y * CHUNK_SIZE - HALF_UNIT);
            chunksByPosition.put(position, chunk);
        } else {
            throw new RuntimeException("Chunk at position: " + position + " already exists.");
        }
        return chunksByPosition.get(position);
    }

    public Chunk get(float x, float z) {
        Vector2i position = getChunkVector2i(x, z);
        Chunk chunk = chunksByPosition.get(position);
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
        }
        return chunk;
    }

    public List<Chunk> getNearestChunks(float playerX, float playerZ) {
        List<Chunk> nearestChunks = new ArrayList<>();
        for (Chunk chunk : chunksByPosition.values()) {
            float distance = Vector2.dst(
                playerX, playerZ,
                chunk.center.x, chunk.center.y
            );
            if (distance < Constants.CHUNKS_RANGE_AROUND_CAM) {
                nearestChunks.add(chunk);
            }
        }
        return nearestChunks;
    }

    private Vector2i getChunkVector2i(float x, float z) {
        return new Vector2i((int) ((x + HALF_UNIT) / CHUNK_SIZE), (int) ((z + HALF_UNIT) / CHUNK_SIZE));
    }

    public void clear() {
        chunksByPosition.clear();
    }
}
