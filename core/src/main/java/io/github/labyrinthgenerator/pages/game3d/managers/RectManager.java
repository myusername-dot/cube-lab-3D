package io.github.labyrinthgenerator.pages.game3d.managers;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RectManager {

    private final Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsDoNotTouch = new HashMap<>();
    private Map<Integer, RectanglePlus> rectsByConnectedEntIdDoNotTouch = new HashMap<>();

    private Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsCloneDoNotTouch = new HashMap<>(0);
    private Map<Integer, RectanglePlus> rectsByConnectedEntIdCloneDoNotTouch = new HashMap<>(0);

    // Read Committed
    private volatile boolean isTransaction = false;
    private long transactionId = -1;

    private final CubeLab3D game;

    private ChunkManager chunkMan;

    public RectManager(final CubeLab3D game) {
        this.game = game;
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public void addRectTransactional(final RectanglePlus rect) {
        synchronized (rectsCloneDoNotTouch) {
            System.out.println("Method: addRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntIdCloneDoNotTouch) {
                System.out.println("Method: addRect. Block synchronized (rectsByConnectedEntityIdClone).");
                //synchronized (rect) {

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getRectsByConnectedEntId();

                Chunk chunk = chunkMan.get(rect.getX() + rect.getWidth() / 2f, rect.getZ() + rect.getDepth() / 2f);
                rects.computeIfAbsent(chunk, k -> new HashMap<>());
                rects.get(chunk).computeIfAbsent(rect.filter, k -> new HashSet<>());
                rects.get(chunk).get(rect.filter).add(rect);
                if (rect.getConnectedEntityId() >= 0) {
                    rectsByConnectedEntityId.put(rect.getConnectedEntityId(), rect);
                }
                //}
                System.out.println("Method: addRect. Block end synchronized (rectsByConnectedEntityIdClone).");
            }
            System.out.println("Method: addRect. Block end synchronized (rectsClone).");
        }
    }

    public void updateEntityChunkIfExistsRectTransactional(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        synchronized (rectsCloneDoNotTouch) {
            System.out.println("Method: updateEntityChunkIfExistsRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntIdCloneDoNotTouch) {
                System.out.println("Method: updateEntityChunkIfExistsRect. Block synchronized (rectsByConnectedEntityIdClone).");

                Player player = ((GameScreen) game.getScreen()).getPlayer();
                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Try to move the Player's rectangle to the other chunk.");
                }

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getRectsByConnectedEntId();

                RectanglePlus rect = rectsByConnectedEntityId.get(ent.getId());
                if (rect == null) {
                    // must be removed
                    if (this.rectsByConnectedEntIdDoNotTouch.get(ent.getId()) != null) {
                        throw new RuntimeException("Entity id: " + ent.getId() + " rect == null && this.rectsByConnectedEntIdDoNotTouch.get(ent.getId()) != null");
                    }
                    return;
                }
                //synchronized (rect) {
                if (!rects.get(oldChunk).containsKey(rect.filter)) {
                    throw new RuntimeException("Entity id: " + ent.getId() + " !rects.get(oldChunk).containsKey(rect.filter)");
                }
                if (!rects.get(oldChunk).get(rect.filter).contains(rect)) {
                    throwWhyChunkDoesNotContainRect(ent.getId(), oldChunk, newChunk, rects, rect);
                }

                rects.get(oldChunk).get(rect.filter).remove(rect);
                rects.get(newChunk).computeIfAbsent(rect.filter, k -> new HashSet<>());
                rects.get(newChunk).get(rect.filter).add(rect);

                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("The Player's rectangle has been moved to the other chunk!");
                } else {
                    System.out.println("Entity id: " + ent.getId() + " rectangle has been moved to the other chunk!");
                }
            }
            System.out.println("Method: updateEntityChunkIfExistsRect. Block end synchronized (rectsByConnectedEntityIdClone).");
        }
        System.out.println("Method: updateEntityChunkIfExistsRect. Block end synchronized (rectsClone).");
    }

    public List<RectanglePlus> getNearestRectsByFilters(float playerX, float playerZ, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return new ArrayList<>();

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);
        if (nearestChunks == null || nearestChunks.isEmpty()) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() at position " + playerX + ", " + playerZ + ".");
        }

        Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getRects();
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

    public void removeRectTransactional(final RectanglePlus rect) {
        synchronized (rectsCloneDoNotTouch) {
            System.out.println("Method: removeRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntIdCloneDoNotTouch) {
                System.out.println("Method: removeRect. Block synchronized (rectsByConnectedEntityIdClone).");
                //synchronized (rect) {

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getRectsByConnectedEntId();

                rects.values().forEach(c -> c.values().forEach(l -> l.remove(rect)));
                rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
                //}
            }
            System.out.println("Method: removeRect. Block end synchronized (rectsByConnectedEntityIdClone).");
        }
        System.out.println("Method: removeRect. Block end synchronized (rectsClone).");
    }

    public int rectsCountAndCheck() {
        AtomicInteger rectsCount = new AtomicInteger();
        rectsDoNotTouch.forEach((c, m) -> m.forEach((f, s) -> rectsCount.addAndGet(s.size())));
        if (rectsCount.get() != rectsByConnectedEntIdDoNotTouch.size()) {
            if (rectsCount.get() > rectsByConnectedEntIdDoNotTouch.size())
                throw new RuntimeException("rectsCount.get() > rectsByConnectedEntityId.size(): " + rectsCount.get() + ", " + rectsByConnectedEntIdDoNotTouch.size());
            else
                throw new RuntimeException("rectsCount.get() < rectsByConnectedEntityId.size(): " + rectsCount.get() + ", " + rectsByConnectedEntIdDoNotTouch.size());
        }
        return rectsCount.get();
    }

    public void clear() {
        //rects.values().forEach(c -> c.values().forEach(Set::clear));
        //rects.values().forEach(Map::clear);
        rectsDoNotTouch.clear();
        rectsByConnectedEntIdDoNotTouch.clear();
        rectsCloneDoNotTouch.clear();
        rectsByConnectedEntIdCloneDoNotTouch.clear();
    }

    public synchronized void joinTransaction(List<Chunk> chunksInTransaction, long transactionId) {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        rectsCloneDoNotTouch = new HashMap<>(chunksInTransaction.size());
        for (Chunk chunk : chunksInTransaction) {
            // put chunks and filters
            rectsCloneDoNotTouch.put(chunk, new HashMap<>(rectsDoNotTouch.get(chunk)));
            Map<RectanglePlusFilter, Set<RectanglePlus>> filters = rectsCloneDoNotTouch.get(chunk);
            // put rects
            filters.replaceAll((f, v) -> new HashSet<>(filters.get(f)));
        }
        // it's faster than new HashMap<>(rectsByConnectedEntityId)
        rectsByConnectedEntIdCloneDoNotTouch = new HashMap<>(rectsByConnectedEntIdDoNotTouch.size());
        rectsByConnectedEntIdCloneDoNotTouch.putAll(rectsByConnectedEntIdDoNotTouch);
        this.transactionId = transactionId;
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        rectsDoNotTouch.putAll(rectsCloneDoNotTouch);
        rectsByConnectedEntIdDoNotTouch = rectsByConnectedEntIdCloneDoNotTouch;
        isTransaction = false;
        rectsCloneDoNotTouch = new HashMap<>(0);
        rectsByConnectedEntIdCloneDoNotTouch = new HashMap<>(0);
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
        rectsCloneDoNotTouch = new HashMap<>(0);
        rectsByConnectedEntIdCloneDoNotTouch = new HashMap<>(0);
    }

    private Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> getRects() {
        if (isTransaction) return rectsCloneDoNotTouch;
        else return this.rectsDoNotTouch;
    }

    private Map<Integer, RectanglePlus> getRectsByConnectedEntId() {
        if (isTransaction) return rectsByConnectedEntIdCloneDoNotTouch;
        else return this.rectsByConnectedEntIdDoNotTouch;
    }

    private void throwWhyChunkDoesNotContainRect(
        int entId,
        Chunk oldChunk, Chunk newChunk,
        Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsTransaction,
        RectanglePlus rect) {
        Optional<Chunk> transactionChunk = rectsTransaction.entrySet()
            .stream()
            .filter(e -> e.getValue().values().stream().anyMatch(s -> s.stream().anyMatch(r -> r.equals(rect))))
            .map(Map.Entry::getKey).findAny();
        Optional<Chunk> chunk = this.rectsDoNotTouch.entrySet()
            .stream()
            .filter(e -> e.getValue().values().stream().anyMatch(s -> s.stream().anyMatch(r -> r.equals(rect))))
            .map(Map.Entry::getKey).findAny();

        throw new RuntimeException(
            "Entity id: " + entId + " !rects.get(oldChunk).get(rect.filter).contains(rect). " +
                "Old is: " + oldChunk + ", new is: " + newChunk +
                ", transaction is: " + transactionChunk.orElse(null) +
                ", is: " + chunk.orElse(null)
        );
    }
}
