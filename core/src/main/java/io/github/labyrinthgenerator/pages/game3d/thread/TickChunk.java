package io.github.labyrinthgenerator.pages.game3d.thread;

import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AlreadyConnectedException;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
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
        //log.debug("TickChunk thread id: " + Thread.currentThread().getId() + " begin.");
        for (Entity ent : entitiesByChunkClone) {
            try {
                if (ent.shouldTick()) {
                    ent.beforeTick();
                    ent.tick(delta);
                    ent.afterTick();
                }
            } catch (AlreadyConnectedException e) {
                log.warn("Entity id: " + ent.getId() + " has already been ticked in this transaction.");
            }
        }
        //log.debug("TickChunk thread id: " + Thread.currentThread().getId() + " end.");
        return true;
    }


}
