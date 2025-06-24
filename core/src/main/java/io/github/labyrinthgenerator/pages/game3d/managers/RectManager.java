package io.github.labyrinthgenerator.pages.game3d.managers;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RectManager {

    // ConcurrentHashMap, ConcurrentSkipListSet/ConcurrentHashMap<Object,null>
    private final ConcurrentHashMap<Chunk, ConcurrentHashMap<RectanglePlusFilter, ConcurrentHashMap<RectanglePlus, Object>>> rects = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, RectanglePlus> rectsByConnectedEntityId = new ConcurrentHashMap<>();
    private final Object justObject = new Object();

    private volatile boolean isTransaction = false;
    private volatile long transactionId = -1;

    private final CubeLab3D game;

    private ChunkManager chunkMan;

    public RectManager(final CubeLab3D game) {
        this.game = game;
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public void addRect(final RectanglePlus rect) {
        Chunk chunk = chunkMan.get(rect.getX() + rect.getWidth() / 2f, rect.getZ() + rect.getDepth() / 2f);

        rects.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>());
        rects.get(chunk).computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>());
        rects.get(chunk).get(rect.filter).put(rect, justObject);
        if (rect.getConnectedEntityId() >= 0) {
            rectsByConnectedEntityId.put(rect.getConnectedEntityId(), rect);
        }
    }

    public void updateEntityChunkIfExistsRect(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        Player player = ((GameScreen) game.getScreen()).getPlayer();
        if (player != null && player.getId() == ent.getId()) {
            log.debug("Try to move the Player's rectangle to the other chunk.");
        }

        RectanglePlus rect = rectsByConnectedEntityId.get(ent.getId());
        if (rect == null) {
            // must be removed
            if (this.rectsByConnectedEntityId.get(ent.getId()) != null) {
                throw new RuntimeException("Entity id: " + ent.getId() + " rect == null && this.rectsByConnectedEntIdDoNotTouch.get(ent.getId()) != null");
            }
            return;
        }
        if (!rects.get(oldChunk).containsKey(rect.filter)) {
            throw new RuntimeException("Entity id: " + ent.getId() + " !rects.get(oldChunk).containsKey(rect.filter)");
        }
        if (!rects.get(oldChunk).get(rect.filter).containsKey(rect)) {
            throwWhyChunkDoesNotContainRect(ent.getId(), oldChunk, newChunk, rect);
        }

        rects.get(oldChunk).get(rect.filter).remove(rect);
        rects.get(newChunk).computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>());
        rects.get(newChunk).get(rect.filter).put(rect, justObject);

        if (player != null && player.getId() == ent.getId()) {
            log.debug("The Player's rectangle has been moved to the other chunk!");
        } else {
            log.debug("Entity id: " + ent.getId() + " rectangle has been moved to the other chunk!");
        }
    }

    public List<RectanglePlus> getNearestRectsByFilters(float playerX, float playerZ, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return new ArrayList<>();

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);
        if (nearestChunks == null || nearestChunks.isEmpty()) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() at position " + playerX + ", " + playerZ + ".");
        }

        List<RectanglePlus> nearestRects = new ArrayList<>();
        for (Chunk chunk : nearestChunks) {
            for (RectanglePlusFilter filter : filters) {
                Map<RectanglePlus, Object> otherRects = rects.get(chunk).get(filter);
                if (otherRects == null) continue;
                for (final RectanglePlus otherRect : otherRects.keySet()) {
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
                if (game.getEntMan().getEntityById(rect.getConnectedEntityId()) != null) {
//							log.debug("id1: " + rect.getConnectedEntityId());
                    game.getEntMan().getEntityById(rect.getConnectedEntityId()).onCollision(otherRect);
                }

                if (game.getEntMan().getEntityById(otherRect.getConnectedEntityId()) != null) {
//							log.debug("id2: " + otherRect.getConnectedEntityId());
                    game.getEntMan().getEntityById(otherRect.getConnectedEntityId()).onCollision(rect);
                }
                return true;
            }
        }
        return false;
    }

    public void removeRect(final RectanglePlus rect) {
        rects.values().forEach(c -> c.values().forEach(l -> l.remove(rect)));
        rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
    }

    public int rectsCountAndCheck() {
        AtomicInteger rectsCount = new AtomicInteger();
        rects.forEach((c, m) -> m.forEach((f, s) -> rectsCount.addAndGet(s.size())));
        if (rectsCount.get() != rectsByConnectedEntityId.size()) {
            if (rectsCount.get() > rectsByConnectedEntityId.size())
                throw new RuntimeException("rectsCount.get() > rectsByConnectedEntityId.size(): " + rectsCount.get() + ", " + rectsByConnectedEntityId.size());
            else
                throw new RuntimeException("rectsCount.get() < rectsByConnectedEntityId.size(): " + rectsCount.get() + ", " + rectsByConnectedEntityId.size());
        }
        return rectsCount.get();
    }

    public void clear() {
        rects.clear();
        rectsByConnectedEntityId.clear();
    }

    public synchronized void joinTransaction(long transactionId) {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        this.transactionId = transactionId;
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
    }

    private void throwWhyChunkDoesNotContainRect(
        int entId,
        Chunk oldChunk, Chunk newChunk,
        RectanglePlus rect) {
        Optional<Chunk> chunk = this.rects.entrySet()
            .stream()
            .filter(e -> e.getValue().values().stream().anyMatch(s -> s.keySet().stream().anyMatch(r -> r.equals(rect))))
            .map(Map.Entry::getKey).findAny();

        throw new RuntimeException(
            "Entity id: " + entId + " !rects.get(oldChunk).get(rect.filter).contains(rect). " +
                "Old is: " + oldChunk + ", new is: " + newChunk +
                ", is: " + chunk.orElse(null)
        );
    }
}
