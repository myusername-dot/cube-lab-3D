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

    private final Map<Chunk, Set<Entity>> entitiesByChunksDoNotTouch = new HashMap<>();
    private Map<Integer, Entity> entitiesByIdDoNotTouch = new HashMap<>();

    private Map<Chunk, Set<Entity>> clonedEntitiesByChunksDoNotTouch = new HashMap<>(0);
    private Map<Integer, Entity> clonedEntitiesByIdDoNotTouch = new HashMap<>(0);

    // Read Committed
    private volatile boolean isTransaction = false;
    private volatile boolean saveMode = false;
    private volatile long transactionId = -1;

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
        synchronized (clonedEntitiesByChunksDoNotTouch) {
            System.out.println("Method: addEntityOnChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (clonedEntitiesByIdDoNotTouch) {
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
        synchronized (clonedEntitiesByChunksDoNotTouch) {
            System.out.println("Method: updateEntityChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (clonedEntitiesByIdDoNotTouch) {
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

    public Entity getEntityById(final int id) {
        Map<Integer, Entity> entitiesById = getEntitiesById();
        return entitiesById.get(id); // goto getNearestRectsByFilters if it has problems
        //return entitiesByIdDoNotTouch.get(id);
    }

    public GameScreen getScreen() {
        return screen;
    }

    public void removeEntityTransactional(Entity ent) {
        synchronized (clonedEntitiesByChunksDoNotTouch) {
            System.out.println("Method: removeEntity. Block synchronized (entitiesByChunksClone).");
            synchronized (clonedEntitiesByIdDoNotTouch) {
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
        entitiesByChunksDoNotTouch.clear();
        entitiesByIdDoNotTouch.clear();
        clonedEntitiesByChunksDoNotTouch.clear();
        clonedEntitiesByIdDoNotTouch.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunksDoNotTouch.get(chunk)) {
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
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 2);

            for (Chunk chunk : nearestChunks) {
                Set<Entity> entitiesByChunkClone = new HashSet<>(entitiesByChunksDoNotTouch.get(chunk));
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
        clonedEntitiesByChunksDoNotTouch = new HashMap<>(chunksInTransaction.size());
        clonedEntitiesByIdDoNotTouch = new HashMap<>(entitiesByIdDoNotTouch.size() / chunksInTransaction.size());
        for (Chunk chunk : chunksInTransaction) {
            // create new HashSet and put all entities from chunk
            Set<Entity> clonedSetEntitiesByChunk = new HashSet<>(entitiesByChunksDoNotTouch.get(chunk).size());
            clonedSetEntitiesByChunk.addAll(entitiesByChunksDoNotTouch.get(chunk));
            // put chunk and all entities of chunk to the new HashMap
            clonedEntitiesByChunksDoNotTouch.put(chunk, clonedSetEntitiesByChunk);
            // put all entities of chunk by id to the new HashMap
            clonedSetEntitiesByChunk.forEach(e -> clonedEntitiesByIdDoNotTouch.put(e.getId(), e));
        }
        transactionId = System.nanoTime();
        saveMode = true;
        isTransaction = true;
    }

    public synchronized void startTransactionUnsave() {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        clonedEntitiesByChunksDoNotTouch = entitiesByChunksDoNotTouch;
        clonedEntitiesByIdDoNotTouch = entitiesByIdDoNotTouch;
        transactionId = System.nanoTime();
        saveMode = false;
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        if (saveMode) {
            entitiesByChunksDoNotTouch.putAll(clonedEntitiesByChunksDoNotTouch);
            entitiesByIdDoNotTouch.putAll(clonedEntitiesByIdDoNotTouch);
            // todo remove removed entities
        }
        isTransaction = false;
        clonedEntitiesByChunksDoNotTouch = new HashMap<>(0);
        clonedEntitiesByIdDoNotTouch = new HashMap<>(0);
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
        clonedEntitiesByChunksDoNotTouch = new HashMap<>(0);
        clonedEntitiesByIdDoNotTouch = new HashMap<>(0);
    }

    private Map<Chunk, Set<Entity>> getEntitiesByChunks() {
        if (isTransaction) return clonedEntitiesByChunksDoNotTouch;
        else return entitiesByChunksDoNotTouch;
    }

    private Map<Integer, Entity> getEntitiesById() {
        if (isTransaction) return clonedEntitiesByIdDoNotTouch;
        else return entitiesByIdDoNotTouch;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public long getTransactionId() {
        return transactionId;
    }

    private void startTickLog() {
        System.out.println("Start tick all entities." +
            " Entities count: " + entitiesByIdDoNotTouch.size() +
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
        entitiesByChunksDoNotTouch.forEach((key, value) -> entitiesSize.addAndGet(value.size()));
        if (entitiesSize.get() != entitiesByIdDoNotTouch.size()) {
            if (entitiesSize.get() > entitiesByIdDoNotTouch.size())
                throw new RuntimeException("entitiesSize.get() > entitiesById.size(): " + entitiesSize.get() + ", " + entitiesByIdDoNotTouch.size());
            else
                throw new RuntimeException("entitiesSize.get() < entitiesById.size(): " + entitiesSize.get() + ", " + entitiesByIdDoNotTouch.size());
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
