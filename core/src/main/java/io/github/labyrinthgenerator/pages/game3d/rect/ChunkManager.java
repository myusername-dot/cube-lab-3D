package io.github.labyrinthgenerator.pages.game3d.rect;

import com.badlogic.gdx.math.Vector2;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChunkManager {

    private final HashMap<Vector2, Chunk> chunks = new HashMap<>();

    public Chunk add(float x, float z) {
        Vector2 position = getChunkVector2(x, z);
        if (!chunks.containsKey(position)) {
            Chunk chunk = new Chunk(x, z);
            chunks.put(position, chunk);
        }
        return chunks.get(position);
    }

    public Chunk get(float x, float z) {
        Vector2 position = getChunkVector2(x, z);
        Chunk chunk = chunks.get(position);
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
        }
        return chunk;
    }

    public List<Chunk> getNearestChunks(float playerX, float playerZ) {
        List<Chunk> nearestChunks = new ArrayList<>();
        for (Chunk chunk : chunks.values()) {
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

    private Vector2 getChunkVector2(float x, float z) {
        return new Vector2((int) (x / Constants.CHUNK_SIZE), (int) (z / Constants.CHUNK_SIZE));
    }
}
