package io.github.labyrinthgenerator.pages.game3d.thread;

import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class TickChunk implements Runnable {

    private final Set<Entity> entitiesByChunkClone;
    private final float delta;
    private final CountDownLatch cdl;

    public TickChunk(Set<Entity> entitiesByChunkClone, float delta, CountDownLatch cdl) {
        this.entitiesByChunkClone = entitiesByChunkClone;
        this.delta = delta;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        for (Entity ent : entitiesByChunkClone) {
            if (ent.shouldTick()) {
                ent.tick(delta);
            }
        }
        cdl.countDown();
    }
}
