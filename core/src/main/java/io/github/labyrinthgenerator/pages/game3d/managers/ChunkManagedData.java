package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManagedData {

    final ConcurrentHashMap<RectanglePlusFilter, ConcurrentHashMap<RectanglePlus, Object>> rects;
    final ConcurrentHashMap<Entity, Object> entities;

    final HashMap<RectanglePlusFilter, HashMap<Vector3i, List<RectanglePlus>>> rectsRoundCenter;

    //final float roundDist = 1f;

    public ChunkManagedData() {
        this.rects = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();
        this.rectsRoundCenter = new HashMap<>();
    }

    Vector3i getChunkRoundPosition(Vector3 centerPos) {
        return new Vector3i((int) (centerPos.x/* / roundDist*/), (int) (centerPos.y/* / roundDist*/), (int) (centerPos.z/* / roundDist*/));
    }
}
