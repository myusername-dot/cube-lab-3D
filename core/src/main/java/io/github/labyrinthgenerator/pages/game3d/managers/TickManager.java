package io.github.labyrinthgenerator.pages.game3d.managers;

import io.github.labyrinthgenerator.pages.game3d.tickable.Tickable;

import java.util.HashSet;
import java.util.Set;

/**
 * For objects that need to be updated every frame.
 */
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
        for (Tickable ent : entities) {
            if (ent.shouldTick()) {
                ent.tick(delta);
            }
        }
    }
}
