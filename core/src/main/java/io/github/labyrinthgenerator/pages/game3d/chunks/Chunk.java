package io.github.labyrinthgenerator.pages.game3d.chunks;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public final ConcurrentHashMap<RectanglePlusFilter, ConcurrentHashMap<RectanglePlus, Object>> rects;
    public final ConcurrentHashMap<Entity, Object> entities;

    private final float roundDist = 1f;
    private final HashMap<RectanglePlusFilter, HashMap<Vector3i, List<RectanglePlus>>> rectsRoundCenter;

    public Chunk(final ChunkManager chunkMan, float x, float y, float z) {
        this.chunkMan = chunkMan;
        this.worldSize = chunkMan.getWorldSize();
        this.x = x;
        this.y = y;
        this.z = z;
        this.center = new Vector3(x + width / 2f, y + height / 2f, z + depth / 2f);

        this.rects = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();
        this.rectsRoundCenter = new HashMap<>();
    }

    public boolean contains(float x, float y, float z) {
        x = MathUtils.clamp(x, 0, worldSize.x);
        y = MathUtils.clamp(y, 0, worldSize.y);
        z = MathUtils.clamp(z, 0, worldSize.z);

        return this.x <= x && this.x + this.width >= x
            && this.y <= y && this.y + this.height >= y
            && this.z <= z && this.z + this.depth >= z;
    }

    public void updateRectsRoundPositions() {
        rectsRoundCenter.clear();
        for (Map.Entry<RectanglePlusFilter, ConcurrentHashMap<RectanglePlus, Object>> rectsEntry : rects.entrySet()) {
            HashMap<Vector3i, List<RectanglePlus>> rectsRoundByFilter = new HashMap<>(rectsEntry.getValue().size());
            rectsRoundCenter.put(rectsEntry.getKey(), rectsRoundByFilter);
            for (RectanglePlus rect : rectsEntry.getValue().keySet()) {
                rect.nearestChunk = true;
                Vector3i positionRound = getRectRoundPosition(rect);
                rectsRoundByFilter.computeIfAbsent(positionRound, p -> new ArrayList<>()).add(rect);
            }
        }
    }

    public void getNearestRectsByFilter(RectanglePlus rect, RectanglePlusFilter filter, Collection<RectanglePlus> nearestRects) {
        HashMap<Vector3i, List<RectanglePlus>> roundRectsByFilter = rectsRoundCenter.get(filter);
        if (roundRectsByFilter == null) return;

        Vector3i roundPos = getRectRoundPosition(rect);
        Vector3i pos = new Vector3i(0, 0, 0);
        for (int i = roundPos.x - 1; i <= roundPos.x + 1; i++) {
            for (int j = roundPos.y - 1; j <= roundPos.y + 1; j++) {
                for (int k = roundPos.z - 1; k <= roundPos.z + 1; k++) {
                    pos.set(i, j, k);
                    List<RectanglePlus> rects = roundRectsByFilter.get(pos);
                    if (rects != null) {
                        nearestRects.addAll(rects);
                    }
                }
            }
        }
    }

    private Vector3i getRectRoundPosition(RectanglePlus rect) {
        Vector3 center = rect.getCenter();
        return new Vector3i((int) (center.x / roundDist), (int) (center.y / roundDist), (int) (center.z / roundDist));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return Float.compare(chunk.x, x) == 0
            && Float.compare(chunk.y, y) == 0
            && Float.compare(chunk.z, z) == 0;
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
