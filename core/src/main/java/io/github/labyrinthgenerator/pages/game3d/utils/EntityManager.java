package io.github.labyrinthgenerator.pages.game3d.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.chunks.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.thread.TickChunk;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityManager {
    private final AtomicInteger nextId = new AtomicInteger(0);

    private final Map<Chunk, Set<Entity>> entitiesByChunks = new HashMap<>();
    private final Map<Integer, Entity> entitiesById = new HashMap<>();

    private final Map<Chunk, Set<Entity>> entitiesByChunksClone = new HashMap<>();
    private final Map<Integer, Entity> entitiesByIdClone = new HashMap<>();

    // Read Committed
    // But the current transaction can only read changes in synchronized blocks
    private volatile boolean isTransaction = false;
    private long transactionId = -1;

    private ChunkManager chunkMan;

    private GameScreen screen;

    public Chunk addEntityOnChunkTransactional(float x, float z, final Entity ent) {
        Chunk chunk;
        synchronized (entitiesByChunksClone) {
            System.out.println("Method: addEntityOnChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdClone) {
                System.out.println("Method: addEntityOnChunk. Block synchronized (entitiesByIdClone).");
                //synchronized (ent) {

                Map<Integer, Entity> entitiesById = getTransactionEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

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
        synchronized (entitiesByChunksClone) {
            System.out.println("Method: updateEntityChunk. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdClone) {
                System.out.println("Method: updateEntityChunk. Block synchronized (entitiesByIdClone).");
                //synchronized (ent) {

                Player player = getScreen().getPlayer();
                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Try to move the Player to the other chunk.");
                }

                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

                entitiesByChunks.get(oldChunk).remove(ent);
                entitiesByChunks.computeIfAbsent(newChunk, k -> new HashSet<>());
                entitiesByChunks.get(newChunk).add(ent);

                if (player != null && player.getId() == ent.getId()) {
                    System.out.println("Player moved to the other chunk!");
                } else {
                    System.out.println("Entity id: " + ent.getId() + " moved to the other chunk!");
                }
                //}
            }
            System.out.println("Method: updateEntityChunk. Block end synchronized (entitiesByIdClone).");
        }
        System.out.println("Method: updateEntityChunk. Block end synchronized (entitiesByChunksClone).");
    }

    public int assignId() {
        return nextId.getAndIncrement();
    }

    public Entity getEntityFromId(final int id) {
        return entitiesById.get(id);
    }

    public GameScreen getScreen() {
        return screen;
    }

    public void removeEntityTransactional(Entity ent) {
        synchronized (entitiesByChunksClone) {
            System.out.println("Method: removeEntity. Block synchronized (entitiesByChunksClone).");
            synchronized (entitiesByIdClone) {
                System.out.println("Method: removeEntity. Block synchronized (entitiesByIdClone).");
                //synchronized (ent) {

                Map<Integer, Entity> entitiesById = getTransactionEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

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
        entitiesByChunks.clear();
        entitiesById.clear();
        entitiesByChunksClone.clear();
        entitiesByIdClone.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunks.get(chunk)) {
                if (ent.shouldRender3D()) {
                    ent.render3D(mdlBatch, env, delta);
                }
            }
        }
    }

    public void setScreen(final GameScreen screen) {
        this.screen = screen;
        this.chunkMan = screen.game.getChunkMan();
    }

    public synchronized void tickAllEntities(final float delta, float playerX, float playerZ) throws InterruptedException {
        try {
            System.out.println("Start tick all entities:");
            System.out.println("Entities count: " + entitiesById.size() + ",");
            System.out.println("Rectangles count: " + screen.game.getRectMan().rectsCount() + ".");

            List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);

            // TRANSACTION START
            startTransaction();
            screen.game.getRectMan().startTransaction();

            List<Future<Boolean>> futures = new ArrayList<>(nearestChunks.size());
            ExecutorService executorService = Executors.newFixedThreadPool(4);

            for (Chunk chunk : nearestChunks) {
                Set<Entity> entitiesByChunkClone = new HashSet<>(entitiesByChunks.get(chunk));
                TickChunk tickChunk = new TickChunk(entitiesByChunkClone, delta);
                futures.add(executorService.submit(tickChunk));
            }

            for(Future<Boolean> future : futures) {
                future.get();
            }
            executorService.shutdown();

            screen.game.getRectMan().commitTransaction();
            commitTransaction();
            // TRANSACTION END


            AtomicInteger entitiesSize = new AtomicInteger();
            entitiesByChunks.forEach((key, value) -> entitiesSize.addAndGet(value.size()));
            if (entitiesSize.get() != entitiesById.size()) {
                if (entitiesSize.get() > entitiesById.size())
                    throw new RuntimeException("entitiesSize.get() > entitiesById.size(): " + entitiesSize.get() + ", " + entitiesById.size());
                else
                    throw new RuntimeException("entitiesSize.get() < entitiesById.size(): " + entitiesSize.get() + ", " + entitiesById.size());
            }
            System.out.println("End tick all entities:");
            System.out.println("Entities count: " + entitiesSize.get() + ",");
            System.out.println("Rectangles count: " + screen.game.getRectMan().rectsCount() + ".");
        } catch (Exception e) {
            e.printStackTrace();
            screen.game.getRectMan().rollbackTransaction();
            rollbackTransaction();
            // TRANSACTION END

            System.err.println("End tick all entities, transaction rollback.");
        }
    }

    public synchronized void startTransaction() {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        entitiesByChunks.forEach((key, value) -> entitiesByChunksClone.put(key, new HashSet<>(value)));
        entitiesByIdClone.putAll(entitiesById);
        transactionId = System.nanoTime();
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        entitiesByChunks.clear();
        entitiesById.clear();
        entitiesByChunks.putAll(entitiesByChunksClone);
        entitiesById.putAll(entitiesByIdClone);
        isTransaction = false;
        entitiesByChunksClone.clear();
        entitiesByIdClone.clear();
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
        entitiesByChunksClone.clear();
        entitiesByIdClone.clear();
    }

    private Map<Chunk, Set<Entity>> getTransactionEntitiesByChunks() {
        if (isTransaction) return entitiesByChunksClone;
        else return entitiesByChunks;
    }

    private Map<Integer, Entity> getTransactionEntitiesById() {
        if (isTransaction) return entitiesByIdClone;
        else return entitiesById;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
