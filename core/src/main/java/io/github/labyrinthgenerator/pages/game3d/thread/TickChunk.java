package io.github.labyrinthgenerator.pages.game3d.thread;

import io.github.labyrinthgenerator.pages.game3d.entities.Entity;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class TickChunk implements Callable<Boolean> {

    private final Set<Entity> entitiesByChunkClone;
    private final float delta;
    //private final CountDownLatch cdl;

    public TickChunk(Set<Entity> entitiesByChunkClone, float delta) {
        this.entitiesByChunkClone = entitiesByChunkClone;
        this.delta = delta;
    }

    @Override
    public Boolean call() {
        System.out.println("TickChunk thread id: " + Thread.currentThread().getId() + " begin.");
        for (Entity ent : entitiesByChunkClone) {
            if (ent.shouldTick()) {
                ent.beforeTick();
                ent.tick(delta);
                ent.afterTick();
            }
        }
        System.out.println("TickChunk thread id: " + Thread.currentThread().getId() + " end.");
        return true;
    }


}
