package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RectManager {

    private final ConcurrentHashMap<Integer, RectanglePlus> rectsByConnectedEntityId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Vector3i, RectanglePlus> staticRectsByPosition = new ConcurrentHashMap<>();
    private final Object justObject = new Object();

    private volatile boolean isTick = false;
    private volatile long tickId = -1;

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
                rect.getY() + rect.getHeight() / 2f,
                rect.getZ() + rect.getDepth() / 2f
        );
        chunk.data.rects.computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>()).put(rect, justObject);

        if (rect.getConnectedEntityId() >= 0) {
            rectsByConnectedEntityId.put(rect.getConnectedEntityId(), rect);
        }
        if (rect.isStatic) {
            staticRectsByPosition.put(new Vector3i(rect.getPositionImmutable()), rect);
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

        oldChunk.data.rects.get(rect.filter).remove(rect);
        newChunk.data.rects.computeIfAbsent(rect.filter, k -> new ConcurrentHashMap<>()).put(rect, justObject);

        logRectEndChunkMovement(ent);
    }

    public void processingRectOverlapsByFilters(final Vector3 camPos, final RectanglePlus rect) {
        List<RectanglePlusFilter> filters = game.getOverlapFilterMan().getFiltersOverlap(rect.filter);
        if (filters.isEmpty()) return;

        List<Chunk> nearestChunks = chunkMan.getNearestChunks(camPos, Constants.CHUNKS_UPDATE_RANGE_AROUND_CAM);
        if (nearestChunks == null || nearestChunks.isEmpty()) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() at position " + camPos + ".");
        }
        Set<RectanglePlus> otherRects = new HashSet<>();
        for (Chunk chunk : nearestChunks) {
            for (RectanglePlusFilter filter : filters) {
                getNearestRectsByChunkByFilter(chunk, rect.getCenter(), filter, otherRects);
            }
        }
        if (rect.filter.equals(RectanglePlusFilter.PLAYER)) {
            otherRects.forEach(r -> r.nearest = true);
        }
        for (final RectanglePlus otherRect : otherRects) {
            if (rect != otherRect && rect.overlaps(otherRect)) {
                Vector3 diff = rect.overlapsDiff(otherRect);
                rect.add(diff);
                rect.overlaps = true;
                otherRect.overlaps = true;
                handleCollision(rect, otherRect);
            }
        }
    }

    private void handleCollision(final RectanglePlus rect, final RectanglePlus otherRect) {
        if (game.getEntMan().getEntityById(rect.getConnectedEntityId()) != null) {
            game.getEntMan().getEntityById(rect.getConnectedEntityId()).onCollision(otherRect);
        }

        if (game.getEntMan().getEntityById(otherRect.getConnectedEntityId()) != null) {
            game.getEntMan().getEntityById(otherRect.getConnectedEntityId()).onCollision(rect);
        }
    }

    public void updateChunkNearestRectsPositions(Chunk chunk) {
        chunk.data.rectsRoundCenter.clear();
        for (Map.Entry<RectanglePlusFilter, ConcurrentHashMap<RectanglePlus, Object>> rectsEntry : chunk.data.rects.entrySet()) {
            HashMap<Vector3i, List<RectanglePlus>> rectsRoundByFilter = new HashMap<>(rectsEntry.getValue().size());
            chunk.data.rectsRoundCenter.put(rectsEntry.getKey(), rectsRoundByFilter);
            for (RectanglePlus rect : rectsEntry.getValue().keySet()) {
                rect.nearestChunk = true;
                Set<Vector3i> roundPoses = splitToRoundPositions(rect);
                for (Vector3i positionRound : roundPoses) {
                    rectsRoundByFilter.computeIfAbsent(positionRound, p -> new ArrayList<>()).add(rect);
                }
            }
        }
    }

    private Set<Vector3i> splitToRoundPositions(RectanglePlus rect) {
        Set<Vector3i> roundPoses = new HashSet<>();
        Vector3 pos = rect.getPositionImmutable();
        for (int i = (int) pos.x; i <= MathUtils.ceil(pos.x + rect.getWidth()); i++) {
            for (int j = (int) pos.y; j <= MathUtils.ceil(pos.y + rect.getHeight()); j++) {
                for (int k = (int) pos.z; k <= MathUtils.ceil(pos.z + rect.getDepth()); k++) {
                    roundPoses.add(new Vector3i(i, j, k));
                }
            }
        }
        return roundPoses;
    }

    public void getNearestRectsByChunkByFilter(Chunk chunk, Vector3 centerPos, RectanglePlusFilter filter, Collection<RectanglePlus> dst) {
        HashMap<Vector3i, List<RectanglePlus>> roundRectsByFilter = chunk.data.rectsRoundCenter.get(filter);
        if (roundRectsByFilter == null) return;

        Vector3i roundPos = chunk.data.getChunkRoundPosition(centerPos);
        Vector3i shiftedRoundPos = new Vector3i(0, 0, 0);
        for (int i = roundPos.x - 1; i <= roundPos.x + 1; i++) {
            for (int j = roundPos.y - 1; j <= roundPos.y + 1; j++) {
                for (int k = roundPos.z - 1; k <= roundPos.z + 1; k++) {
                    shiftedRoundPos.set(i, j, k);
                    List<RectanglePlus> rects = roundRectsByFilter.get(shiftedRoundPos);
                    if (rects != null) {
                        dst.addAll(rects);
                    }
                }
            }
        }
    }

    public boolean checkStaticPosition(int x, int y, int z) {
        return staticRectsByPosition.containsKey(new Vector3i(x, y, z));
    }

    public void removeRect(final RectanglePlus rect) {
        Entity entity = game.getEntMan().getEntityById(rect.getConnectedEntityId());
        Chunk chunk;
        if (entity != null) chunk = entity.getChunk();
        else chunk = chunkMan.get(rect.getX(), rect.getY(), rect.getZ());
        chunk.data.rects.get(rect.filter).remove(rect);
        rectsByConnectedEntityId.remove(rect.getConnectedEntityId());
        staticRectsByPosition.remove(new Vector3i(rect.getPositionImmutable()));
        MyDebugRenderer.shapes.remove(rect);
    }

    public int rectsCount() {
        return rectsByConnectedEntityId.size();
    }

    public int rectsCountAndCheck() {
        AtomicInteger rectsCount = new AtomicInteger();
        List<Chunk> chunks = chunkMan.getChunks();
        chunks.forEach(c -> c.data.rects.forEach((f, s) -> rectsCount.addAndGet(s.size())));
        checkRectCountConsistency(rectsCount);
        return rectsCount.get();
    }

    private void checkRectCountConsistency(AtomicInteger rectsCount) {
        if (rectsCount.get() != rectsByConnectedEntityId.size()) {
            throw new RuntimeException("Rect count mismatch: " + rectsCount.get() + " vs " + rectsByConnectedEntityId.size());
        }
    }

    public void clear() {
        rectsByConnectedEntityId.clear();
        staticRectsByPosition.clear();
        MyDebugRenderer.shapes.clear();
    }

    public synchronized void joinTick(long tickId) {
        if (isTick) {
            throw new RuntimeException("Tick has already started.");
        }
        this.tickId = tickId;
        isTick = true;
    }

    public synchronized void endTick() {
        if (!isTick) {
            throw new RuntimeException("Tick has already ended.");
        }
        isTick = false;
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
        if (!oldChunk.data.rects.containsKey(rect.filter)) {
            throw new RuntimeException("Entity id: " + ent.getId() + " !rects.get(oldChunk).containsKey(rect.filter)");
        }
        if (!oldChunk.data.rects.get(rect.filter).containsKey(rect)) {
            throwWhyChunkDoesNotContainRect(ent.getId(), oldChunk, newChunk, rect);
        }
    }

    private void throwWhyChunkDoesNotContainRect(int entId, Chunk oldChunk, Chunk newChunk, RectanglePlus rect) {
        Optional<Chunk> chunk = chunkMan.getChunks()
                .stream()
                .filter(c -> c.data.rects.values().stream().anyMatch(s -> s.keySet().stream().anyMatch(r -> r.equals(rect))))
                .findAny();

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
