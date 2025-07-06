package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.CHUNKS_RANGE_AROUND_CAM_CHUNK;
import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.CHUNK_SIZE;

public class ChunkManager {

    private final Vector3i size;
    private final Chunk[][][] chunks;

    public ChunkManager(Vector3i size) {
        this.size = size;
        this.chunks = new Chunk[size.x][size.y][size.z];
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
        return new Chunk(position.x * CHUNK_SIZE, -position.y * CHUNK_SIZE, position.z * CHUNK_SIZE); // -position.y!!!
    }

    public Chunk get(float x, float y, float z) {
        Vector3i position = getChunkPosition(x, y, z);
        if (position.x >= size.x || position.y >= size.y || position.z >= size.z
            || position.x < 0 || position.y < 0 || position.z < 0) {
            throw new RuntimeException("position >= size || < 0: " + position + ", " + size + ", " + new Vector3f(x, y, z) + ".");
        }
        Chunk chunk = chunks[position.x][position.y][position.z];
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + y + ", " + z + " is null.");
        }
        return chunk;
    }

    public List<Chunk> getNearestChunks(final Vector3 pos) {
        return getNearestChunksInBox(pos, 0);
    }

    public List<Chunk> getNearestChunksInBox(final Vector3 pos, int offsetChunks) {
        List<Chunk> nearestChunks = new ArrayList<>();
        Vector3i position = getChunkPosition(pos.x, pos.y, pos.z);
        int x1 = Math.max(0, position.x - CHUNKS_RANGE_AROUND_CAM_CHUNK - offsetChunks);
        //int y1 = Math.max(0, position.y - CHUNKS_RANGE_AROUND_CAM_CHUNK - offsetChunks);
        int z1 = Math.max(0, position.z - CHUNKS_RANGE_AROUND_CAM_CHUNK - offsetChunks);
        int x2 = Math.min(size.x - 1, position.x + CHUNKS_RANGE_AROUND_CAM_CHUNK + offsetChunks);
        //int y2 = Math.min(size.y - 1, position.y + CHUNKS_RANGE_AROUND_CAM_CHUNK + offsetChunks);
        int z2 = Math.min(size.z - 1, position.z + CHUNKS_RANGE_AROUND_CAM_CHUNK + offsetChunks);

        for (int i = x1; i <= x2; i++)
            //for (int j = y1; j <= y2; j++)
            for (int k = z1; k <= z2; k++)
                nearestChunks.add(chunks[i][0][k]);

        return nearestChunks;
    }

    public Vector3 getWorldSize() {
        return new Vector3(size.x * CHUNK_SIZE, -size.y * CHUNK_SIZE, size.z * CHUNK_SIZE); // -size.y !!!
    }

    private Vector3i getChunkPosition(float x, float y, float z) {
        return new Vector3i(
            (int) (x / CHUNK_SIZE),
            (int) (-y / CHUNK_SIZE), // -y!!!
            (int) (z / CHUNK_SIZE)
        );
    }

    public void clear() {
        for (int i = 0; i < size.x; i++) chunks[i] = null;
    }
}
