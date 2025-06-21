package io.github.labyrinthgenerator.pages.game3d.rect;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.chunks.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RectManager {

    private final Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = new HashMap<>();
    private final Map<Integer, RectanglePlus> rectsByConnectedEntityId = new HashMap<>();

    private final Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsClone = new HashMap<>();
    private final Map<Integer, RectanglePlus> rectsByConnectedEntityIdClone = new HashMap<>();

    // Read Committed
    // But the current transaction can only read changes in synchronized blocks
    private volatile boolean isTransaction = false;

    private final CubeLab3D game;

    private ChunkManager chunkMan;

    public RectManager(final CubeLab3D game) {
        this.game = game;
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public void addRectTransactional(final RectanglePlus rect) {
        synchronized (rectsClone) {
            System.out.println("Method: addRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntityIdClone) {
                System.out.println("Method: addRect. Block synchronized (rectsByConnectedEntityIdClone).");
                //synchronized (rect) {

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                // FIXME rect x or rect x + width / 2?
                Chunk chunk = chunkMan.get(rect.getX(), rect.getZ());
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
        synchronized (rectsClone) {
            System.out.println("Method: updateEntityChunkIfExistsRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntityIdClone) {
                System.out.println("Method: updateEntityChunkIfExistsRect. Block synchronized (rectsByConnectedEntityIdClone).");

                Player player = ((GameScreen) game.getScreen()).getPlayer();
                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Try to move the Player's rectangle to the other chunk.");
                }

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                RectanglePlus rect = rectsByConnectedEntityId.get(ent.getId());
                if (rect == null) {
                    // must be removed
                    if (this.rectsByConnectedEntityId.get(ent.getId()) != null) {
                        throw new RuntimeException("Entity id: " + ent.getId() + " rect == null && this.rectsByConnectedEntityId.get(ent.getId()) != null");
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
                } else
                    System.out.println("Entity id: " + ent.getId() + " rectangle has been moved to the other chunk!");
                //}
            }
            System.out.println("Method: updateEntityChunkIfExistsRect. Block end synchronized (rectsByConnectedEntityIdClone).");
        }
        System.out.println("Method: updateEntityChunkIfExistsRect. Block end synchronized (rectsClone).");
    }

    public List<RectanglePlus> getNearestRectsByFilters(float playerX, float playerZ, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return new ArrayList<>();

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);
        if (nearestChunks == null || nearestChunks.isEmpty() /*|| nearestChunks.size() <= 2*/) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() || nearestChunks.size() <= 2 at position " + playerX + ", " + playerZ + ".");
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

    public void removeRectTransactional(final RectanglePlus rect) {
        synchronized (rectsClone) {
            System.out.println("Method: removeRect. Block synchronized (rectsClone).");
            synchronized (rectsByConnectedEntityIdClone) {
                System.out.println("Method: removeRect. Block synchronized (rectsByConnectedEntityIdClone).");
                //synchronized (rect) {

                Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rects = getTransactionRects();
                Map<Integer, RectanglePlus> rectsByConnectedEntityId = getTransactionRectsByConnectedEntityId();

                rects.values().forEach(c -> c.values().forEach(l -> l.remove(rect)));
                rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
                //}
            }
            System.out.println("Method: removeRect. Block end synchronized (rectsByConnectedEntityIdClone).");
        }
        System.out.println("Method: removeRect. Block end synchronized (rectsClone).");
    }

    public int rectsCount() {
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
        //rects.values().forEach(c -> c.values().forEach(Set::clear));
        //rects.values().forEach(Map::clear);
        rects.clear();
        rectsByConnectedEntityId.clear();
        rectsClone.clear();
        rectsByConnectedEntityIdClone.clear();
    }

    public synchronized void startTransaction() {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        rects.forEach((c, m) -> rectsClone.computeIfAbsent(c, k -> new HashMap<>(m.size())));
        rects.forEach((c, m) -> m.forEach((f, s) -> rectsClone.get(c).computeIfAbsent(f, k -> new HashSet<>(s))));
        rectsByConnectedEntityIdClone.putAll(rectsByConnectedEntityId);
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        rects.clear();
        rects.putAll(rectsClone);
        rectsByConnectedEntityId.clear();
        rectsByConnectedEntityId.putAll(rectsByConnectedEntityIdClone);
        isTransaction = false;
        rectsClone.clear();
        rectsByConnectedEntityIdClone.clear();
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
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

    private void throwWhyChunkDoesNotContainRect(
        int entId,
        Chunk oldChunk, Chunk newChunk,
        Map<Chunk, Map<RectanglePlusFilter, Set<RectanglePlus>>> rectsTransaction,
        RectanglePlus rect) {
        Optional<Chunk> transactionChunk = rectsTransaction.entrySet()
            .stream()
            .filter(e -> e.getValue().values().stream().anyMatch(s -> s.stream().anyMatch(r -> r.equals(rect))))
            .map(Map.Entry::getKey).findAny();
        Optional<Chunk> chunk = this.rects.entrySet()
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
