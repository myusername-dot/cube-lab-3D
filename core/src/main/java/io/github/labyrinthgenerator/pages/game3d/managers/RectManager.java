package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RectManager {

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
        Chunk chunk = chunkMan.get(
            rect.getX() + rect.getWidth() / 2f,
            rect.getY() - rect.getHeight() / 2f,
            rect.getZ() + rect.getDepth() / 2f
        );
        rects.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>())
            .put(rect, justObject);

        if (rect.getConnectedEntityId() >= 0) {
            rectsByConnectedEntityId.put(rect.getConnectedEntityId(), rect);
        }
        MyDebugRenderer.shapes.add(rect);
    }

    public void updateEntityChunkIfExistsRect(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        logRectStartChunkMovement(ent);

        RectanglePlus rect = rectsByConnectedEntityId.get(ent.getId());
        if (rect == null) {
            handleEntityNotFound(ent);
            return;
        }

        validateOldChunkContainsRect(oldChunk, newChunk, rect, ent);

        rects.get(oldChunk).get(rect.filter).remove(rect);
        rects.computeIfAbsent(newChunk, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>())
            .put(rect, justObject);

        logRectEndChunkMovement(ent);
    }

    public List<RectanglePlus> getNearestRectsByFilters(final Vector3 camPos, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return Collections.emptyList();

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(camPos);
        if (nearestChunks == null || nearestChunks.isEmpty()) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() at position " + camPos + ".");
        }

        List<RectanglePlus> nearestRects = new ArrayList<>();
        for (Chunk chunk : nearestChunks) {
            if (!rects.containsKey(chunk)) {
                //log.warn("Method getNearestRectsByFilters: !rects.containsKey(chunk).");
                continue;
            }
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

    private boolean overlapsPlusDistance(final RectanglePlus r1, final RectanglePlus r2) {
        float distance = 0.5f;
        return r1.getX() - distance < r2.getX() + r2.getWidth() + distance
            && r1.getX() + distance + r1.getWidth() > r2.getX() - distance
            && r1.getY() - distance < r2.getY() + r2.getHeight() + distance
            && r1.getY() + distance + r1.getHeight() > r2.getY() - distance
            && r1.getZ() - distance < r2.getZ() + r2.getDepth() + distance
            && r1.getZ() + distance + r1.getDepth() > r2.getZ() - distance;
    }

    public boolean checkCollisions(final RectanglePlus rect, final List<RectanglePlus> nearestRects) {
        return nearestRects.stream().anyMatch(otherRect -> checkCollision(rect, otherRect));
    }

    private boolean checkCollision(final RectanglePlus rect, final RectanglePlus otherRect) {
        if (!otherRect.equals(rect) && rect.overlaps(otherRect)) {
            handleCollision(rect, otherRect);
            return true;
        }
        return false;
    }

    private void handleCollision(final RectanglePlus rect, final RectanglePlus otherRect) {
        otherRect.overlaps = true;

        if (game.getEntMan().getEntityById(rect.getConnectedEntityId()) != null) {
            game.getEntMan().getEntityById(rect.getConnectedEntityId()).onCollision(otherRect);
        }

        if (game.getEntMan().getEntityById(otherRect.getConnectedEntityId()) != null) {
            game.getEntMan().getEntityById(otherRect.getConnectedEntityId()).onCollision(rect);
        }
    }

    public void removeRect(final RectanglePlus rect) {
        rects.values().forEach(c -> c.values().forEach(l -> l.remove(rect)));
        rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
        MyDebugRenderer.shapes.remove(rect);
    }

    public int rectsCountAndCheck() {
        AtomicInteger rectsCount = new AtomicInteger();
        rects.forEach((c, m) -> m.forEach((f, s) -> rectsCount.addAndGet(s.size())));
        checkRectCountConsistency(rectsCount);
        return rectsCount.get();
    }

    private void checkRectCountConsistency(AtomicInteger rectsCount) {
        if (rectsCount.get() != rectsByConnectedEntityId.size()) {
            throw new RuntimeException("Rect count mismatch: " + rectsCount.get() + " vs " + rectsByConnectedEntityId.size());
        }
    }

    public void clear() {
        rects.clear();
        rectsByConnectedEntityId.clear();
        MyDebugRenderer.shapes.clear();
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

    private void logRectStartChunkMovement(final Entity ent) {
        Player player = game.getScreen().getPlayer();
        log.debug(player != null && player.getId() == ent.getId() ?
            "Try to move the Player's rectangle to the other chunk." :
            "Entity id: " + ent.getId() + " rectangle is trying to move to the other chunk.");
    }

    private void handleEntityNotFound(Entity ent) {
        if (this.rectsByConnectedEntityId.get(ent.getId()) != null) {
            throw new RuntimeException("Entity id: " + ent.getId() + " rect == null && this.rectsByConnectedEntIdDoNotTouch.get(ent.getId()) != null");
        }
    }

    private void validateOldChunkContainsRect(Chunk oldChunk, Chunk newChunk, RectanglePlus rect, Entity ent) {
        if (!rects.get(oldChunk).containsKey(rect.filter)) {
            throw new RuntimeException("Entity id: " + ent.getId() + " !rects.get(oldChunk).containsKey(rect.filter)");
        }
        if (!rects.get(oldChunk).get(rect.filter).containsKey(rect)) {
            throwWhyChunkDoesNotContainRect(ent.getId(), oldChunk, newChunk, rect);
        }
    }

    private void throwWhyChunkDoesNotContainRect(int entId, Chunk oldChunk, Chunk newChunk, RectanglePlus rect) {
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

    private void logRectEndChunkMovement(final Entity ent) {
        Player player = game.getScreen().getPlayer();
        log.debug(player != null && player.getId() == ent.getId() ?
            "The Player's rectangle has been moved to the other chunk!" :
            "Entity id: " + ent.getId() + " rectangle has been moved to the other chunk!");
    }
}
