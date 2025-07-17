package io.github.labyrinthgenerator.pages.game3d.managers;

import io.github.labyrinthgenerator.pages.game3d.tickable.Tickable;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * For objects that need to be updated every frame.
 */
@Slf4j
public class TickManager {

    private final Set<Tickable> entities = new HashSet<>();

    public void addEntity(Tickable ent) {
        entities.add(ent);
    }

    public void removeEntity(Tickable ent) {
        entities.remove(ent);
    }

    public void clear() {
        entities.removeIf(Tickable::shouldClear);
    }

    public void tickAllEntities(final float delta) {
        long tickTime = System.nanoTime();
        for (Tickable ent : entities) {
            if (ent.shouldTick()) {
                ent.tick(delta);
            }
        }
        logTickDuration(tickTime);
    }

    private void logTickDuration(long tickTime) {
        tickTime = System.nanoTime() - tickTime;
        log.info("TickManager time sec: " + tickTime / 1_000_000_000.0d + ".");
    }
}
