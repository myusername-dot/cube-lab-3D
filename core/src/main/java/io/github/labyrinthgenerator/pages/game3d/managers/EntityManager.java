package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.thread.TickChunk;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityManager {
    private final AtomicInteger nextId = new AtomicInteger(0);

    private final Map<Chunk, Set<Entity>> entitiesByChunksDoNotTouchIt = new HashMap<>();
    private Map<Integer, Entity> entitiesByIdDoNotTouchIt = new HashMap<>();

    private Map<Chunk, Set<Entity>> entitiesByChunksCloneDoNotTouch = new HashMap<>(0);
    private Map<Integer, Entity> entitiesByIdCloneDoNotTouch = new HashMap<>(0);

    // Read Committed
    private volatile boolean isTransaction = false;
    private long transactionId = -1;

    private GameScreen screen;
    private ChunkManager chunkMan;

    public void setScreen(final GameScreen screen) {
        this.screen = screen;
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public Chunk addEntityOnChunkTransactional(float x, float z, final Entity ent) {
        Chunk chunk;
        synchronized (entitiesByChunksCloneDoNotTouch) {
            System.out.println("Method: addEntityOnChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdCloneDoNotTouch) {
                System.out.println("Method: addEntityOnChunk. Block synchronized (entitiesByIdClone).");
                //synchronized (ent) {

                Map<Integer, Entity> entitiesById = getEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getEntitiesByChunks();

                entitiesById.put(ent.getId(), ent);
                chunk = chunkMan.get(x, z);
                if (chunk == null) {
                    throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
                }

                entitiesByChunks.computeIfAbsent(chunk, k -> new HashSet<>());
                entitiesByChunks.get(chunk).add(ent);
                //}
            }
            System.out.println("Method: addEntityOnChunk. Block end synchronized (entitiesByIdClone).");
        }
        System.out.println("Method: addEntityOnChunk. Block end synchronized (entitiesByChunksClone).");
        return chunk;
    }

    public void updateEntityChunkTransactional(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        synchronized (entitiesByChunksCloneDoNotTouch) {
            System.out.println("Method: updateEntityChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdCloneDoNotTouch) {
                System.out.println("Method: updateEntityChunk. Block synchronized (entitiesByIdClone).");

                Player player = getScreen().getPlayer();
                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Try to move the Player to the other chunk.");
                }

                Map<Chunk, Set<Entity>> entitiesByChunks = getEntitiesByChunks();

                entitiesByChunks.get(oldChunk).remove(ent);
                entitiesByChunks.computeIfAbsent(newChunk, k -> new HashSet<>());
                entitiesByChunks.get(newChunk).add(ent);

                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Player moved to the other chunk!");
                } else {
                    System.out.println("Entity id: " + ent.getId() + " moved to the other chunk!");
                }
            }
            System.out.println("Method: updateEntityChunk. Block end synchronized (entitiesByIdClone).");
        }
        System.out.println("Method: updateEntityChunk. Block end synchronized (entitiesByChunksClone).");
    }

    public int assignId() {
        return nextId.getAndIncrement();
    }

    public Entity getEntityFromId(final int id) {
        Map<Integer, Entity> entitiesById = getEntitiesById();
        return entitiesById.get(id);
    }

    public GameScreen getScreen() {
        return screen;
    }

    public void removeEntityTransactional(Entity ent) {
        synchronized (entitiesByChunksCloneDoNotTouch) {
            System.out.println("Method: removeEntity. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdCloneDoNotTouch) {
                System.out.println("Method: removeEntity. Block synchronized (entitiesByIdClone).");
                //synchronized (ent) {

                Map<Integer, Entity> entitiesById = getEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getEntitiesByChunks();

                entitiesById.remove(ent.getId());
                entitiesByChunks.values().forEach(c -> c.remove(ent));
                //}
            }
            System.out.println("Method: removeEntity. Block end synchronized (entitiesByIdClone).");
        }
        System.out.println("Method: removeEntity. Block end synchronized (entitiesByChunksClone).");
    }

    public void clear() {
        //entitiesByChunks.values().forEach(Set::clear);
        entitiesByChunksDoNotTouchIt.clear();
        entitiesByIdDoNotTouchIt.clear();
        entitiesByChunksCloneDoNotTouch.clear();
        entitiesByIdCloneDoNotTouch.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunksDoNotTouchIt.get(chunk)) {
                if (ent.shouldRender3D()) {
                    ent.render3D(mdlBatch, env, delta);
                }
            }
        }
    }

    public synchronized void tickAllEntities(final float delta, float playerX, float playerZ) throws InterruptedException {
        long tickTime = System.currentTimeMillis();
        try {
            startTickLog();


            long startTransactionTime = System.nanoTime();

            List<Chunk> chunksInTransaction = chunkMan.getNearestChunksInBox(playerX, playerZ, 1);
            // TRANSACTION START
            startTransaction(chunksInTransaction);
            screen.game.getRectMan().joinTransaction(chunksInTransaction, transactionId);

            startTransactionLog(startTransactionTime);


            // THREADS LOGIC START
            List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);

            List<Future<Boolean>> futures = new ArrayList<>(nearestChunks.size());
            ExecutorService executorService = Executors.newFixedThreadPool(4);

            for (Chunk chunk : nearestChunks) {
                Set<Entity> entitiesByChunkClone = new HashSet<>(entitiesByChunksDoNotTouchIt.get(chunk));
                TickChunk tickChunk = new TickChunk(entitiesByChunkClone, delta);
                futures.add(executorService.submit(tickChunk));
            }

            for (Future<Boolean> future : futures) {
                future.get();
            }
            executorService.shutdown();
            // THREADS LOGIC END


            long endTransactionTime = System.nanoTime();

            screen.game.getRectMan().commitTransaction();
            commitTransaction();
            // TRANSACTION END

            endTransactionLog(endTransactionTime);


            endTickLogAndChecks(tickTime);

        } catch (Exception e) {
            e.printStackTrace();
            screen.game.getRectMan().rollbackTransaction();
            rollbackTransaction();
            // TRANSACTION END

            rollbackTickLog(tickTime);
        }
    }

    public synchronized void startTransaction(List<Chunk> chunksInTransaction) {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        entitiesByChunksCloneDoNotTouch = new HashMap<>(chunksInTransaction.size());
        for (Chunk chunk : chunksInTransaction) {
            entitiesByChunksCloneDoNotTouch.put(chunk, new HashSet<>(entitiesByChunksDoNotTouchIt.get(chunk)));
        }
        // it's faster than new HashMap<>(entitiesById)
        entitiesByIdCloneDoNotTouch = new HashMap<>(entitiesByIdDoNotTouchIt.size());
        entitiesByIdCloneDoNotTouch.putAll(entitiesByIdDoNotTouchIt);
        transactionId = System.nanoTime();
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        entitiesByChunksDoNotTouchIt.putAll(entitiesByChunksCloneDoNotTouch);
        entitiesByIdDoNotTouchIt = entitiesByIdCloneDoNotTouch;
        isTransaction = false;
        entitiesByChunksCloneDoNotTouch = new HashMap<>(0);
        entitiesByIdCloneDoNotTouch = new HashMap<>(0);
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
        entitiesByChunksCloneDoNotTouch = new HashMap<>(0);
        entitiesByIdCloneDoNotTouch = new HashMap<>(0);
    }

    private Map<Chunk, Set<Entity>> getEntitiesByChunks() {
        if (isTransaction) return entitiesByChunksCloneDoNotTouch;
        else return entitiesByChunksDoNotTouchIt;
    }

    private Map<Integer, Entity> getEntitiesById() {
        if (isTransaction) return entitiesByIdCloneDoNotTouch;
        else return entitiesByIdDoNotTouchIt;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public long getTransactionId() {
        return transactionId;
    }

    private void startTickLog() {
        System.out.println("Start tick all entities." +
            " Entities count: " + entitiesByIdDoNotTouchIt.size() +
            ", rectangles count: " + screen.game.getRectMan().rectsCountAndCheck() + ".");
    }

    private void startTransactionLog(long startTransactionTime) {
        startTransactionTime = System.nanoTime() - startTransactionTime;
        double seconds = (double) startTransactionTime / 1_000_000_000.0;
        System.out.println("Transaction started in " + seconds + " seconds ");
    }

    private void endTransactionLog(long endTransactionTime) {
        endTransactionTime = System.nanoTime() - endTransactionTime;
        double seconds = (double) endTransactionTime / 1_000_000_000.0;
        System.out.println("Transaction ended in " + seconds + " seconds ");
    }

    private void endTickLogAndChecks(long tickTime) {
        AtomicInteger entitiesSize = new AtomicInteger();
        entitiesByChunksDoNotTouchIt.forEach((key, value) -> entitiesSize.addAndGet(value.size()));
        if (entitiesSize.get() != entitiesByIdDoNotTouchIt.size()) {
            if (entitiesSize.get() > entitiesByIdDoNotTouchIt.size())
                throw new RuntimeException("entitiesSize.get() > entitiesById.size(): " + entitiesSize.get() + ", " + entitiesByIdDoNotTouchIt.size());
            else
                throw new RuntimeException("entitiesSize.get() < entitiesById.size(): " + entitiesSize.get() + ", " + entitiesByIdDoNotTouchIt.size());
        }
        tickTime = System.currentTimeMillis() - tickTime;
        System.out.println("End tick all entities." +
            " Entities count: " + entitiesSize.get() +
            ", rectangles count: " + screen.game.getRectMan().rectsCountAndCheck() +
            ". Time spent seconds: " + tickTime / 1000d + ".");
    }

    private void rollbackTickLog(long tickTime) {
        System.err.println(
            "End tick all entities, transaction rollback." +
                " Time spent seconds: " + tickTime / 1000d + "."
        );
    }
}
