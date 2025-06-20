package io.github.labyrinthgenerator.pages.game3d.rect;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.chunks.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;

import java.util.*;

public class RectManager {

    private Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = new HashMap<>();
    private Map<Integer, RectanglePlus> rectsByConnectedEntityId = new HashMap<>();

    private final Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsClone = new HashMap<>();
    private final Map<Integer, RectanglePlus> rectsByConnectedEntityIdClone = new HashMap<>();

    // Read committed
    private boolean isTransaction = false;

    private final ChunkManager chunkMan;

    private final CubeLab3D game;

    public RectManager(final CubeLab3D game) {
        this.game = game;
        this.chunkMan = game.getChunkMan();
    }

    public void addRect(final RectanglePlus rect) {
        synchronized (rectsClone) {
            synchronized (rectsByConnectedEntityIdClone) {
                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                Chunk chunk = chunkMan.get(rect.getX(), rect.getZ());
                rects.computeIfAbsent(chunk, k -> new HashMap<>());
                rects.get(chunk).computeIfAbsent(rect.filter, k -> new HashSet<>());
                rects.get(chunk).get(rect.filter).add(rect);
                if (rect.getConnectedEntityId() >= 0) {
                    rectsByConnectedEntityId.put(rect.getConnectedEntityId(), rect);
                }
            }
        }
    }

    public void updateEntityChunkIfExistsRect(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        synchronized (rectsClone) {
            synchronized (rectsByConnectedEntityIdClone) {
                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                RectanglePlus rect = rectsByConnectedEntityId.get(ent.getId());
                if (rect == null) return;
                if (!rects.get(oldChunk).containsKey(rect.filter)) {
                    System.err.println("ent id: " + ent.getId() + " !rects.get(oldChunk).containsKey(rect.filter)");
                } else {
                    rects.get(oldChunk).get(rect.filter).remove(rect);
                }
                rects.get(newChunk).computeIfAbsent(rect.filter, k -> new HashSet<>());
                rects.get(newChunk).get(rect.filter).add(rect);
            }
        }
    }

    public List<RectanglePlus> getNearestRectsByFilters(float playerX, float playerZ, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return new ArrayList<>();

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);
        if (nearestChunks == null || nearestChunks.isEmpty() || nearestChunks.size() < 2) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() || nearestChunks.size() < 2 at position " + playerX + ", " + playerZ + ".");
        }

        List<RectanglePlus> nearestRects = new ArrayList<>();
        for (Chunk chunk : nearestChunks) {
            for (RectanglePlusFilter filter : filters) {
                Set<RectanglePlus> otherRects = rects.get(chunk).get(filter);
                if (otherRects == null) continue;
                for (final RectanglePlus otherRect : otherRects) {
                    if (overlapsPlusDistance(rect, otherRect)) {
                        nearestRects.add(otherRect);
                    }
                }
            }
        }
        return nearestRects;
    }

    private boolean overlapsPlusDistance(RectanglePlus r1, RectanglePlus r2) {
        float distance = 0.1f;
        return
            r1.getX() < r2.getX() + r2.getWidth() + distance && r1.getX() + r1.getWidth() + distance > r2.getX()
                && r1.getY() < r2.getY() + r2.getHeight() + distance && r1.getY() + r1.getHeight() + distance > r2.getY()
                && r1.getZ() < r2.getZ() + r2.getDepth() + distance && r1.getZ() + r1.getDepth() + distance > r2.getZ();
        /*return x < r.x + r.width && x + width > r.x
                && y < r.y + r.height && y + height > r.y
                && z < r.z + r.depth && z + depth > r.z;*/
    }

    public boolean checkCollisions(final RectanglePlus rect, final List<RectanglePlus> nearestRects) {
        for (final RectanglePlus otherRect : nearestRects) {
            if (checkCollision(rect, otherRect)) return true;
        }
        return false;
    }

    private boolean checkCollision(final RectanglePlus rect, final RectanglePlus otherRect) {
        if (otherRect != rect) { // if not itself...
            if (rect.overlaps(otherRect)) {
                if (game.getEntMan().getEntityFromId(rect.getConnectedEntityId()) != null) {
//							System.out.println("id1: " + rect.getConnectedEntityId());
                    game.getEntMan().getEntityFromId(rect.getConnectedEntityId()).onCollision(otherRect);
                }

                if (game.getEntMan().getEntityFromId(otherRect.getConnectedEntityId()) != null) {
//							System.out.println("id2: " + otherRect.getConnectedEntityId());
                    game.getEntMan().getEntityFromId(otherRect.getConnectedEntityId()).onCollision(rect);
                }
                return true;
            }
        }
        return false;
    }

    public void removeRect(final RectanglePlus rect) {
        synchronized (rectsClone) {
            synchronized (rectsByConnectedEntityIdClone) {
                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                rects.values().forEach(c -> c.values().forEach(l -> l.remove(rect)));
                rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
            }
        }
    }

    public void clear() {
        rects.values().forEach(c -> c.values().forEach(Set::clear));
        rects.values().forEach(Map::clear);
        rects.clear();
        rectsByConnectedEntityId.clear();
    }

    public synchronized void startTransaction() {
        if (isTransaction) {
            throw new RuntimeException("Transaction already started.");
        }
        rects.forEach((c, m) -> rectsClone.computeIfAbsent(c, k -> new HashMap<>(m.size())));
        rects.forEach((c, m) -> m.forEach((f, s) -> rectsClone.get(c).computeIfAbsent(f, k -> new HashSet<>(s))));
        rectsByConnectedEntityIdClone.putAll(rectsByConnectedEntityId);
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction already committed.");
        }
        rects = rectsClone;
        rectsByConnectedEntityId = rectsByConnectedEntityIdClone;
        isTransaction = false;
        rectsClone.clear();
        rectsByConnectedEntityIdClone.clear();
    }

    private Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> getTransactionRects() {
        if (isTransaction) return rectsClone;
        else return this.rects;
    }

    private Map<Integer, RectanglePlus> getTransactionRectsByConnectedEntityId() {
        if (isTransaction) return rectsByConnectedEntityIdClone;
        else return this.rectsByConnectedEntityId;
    }
}
