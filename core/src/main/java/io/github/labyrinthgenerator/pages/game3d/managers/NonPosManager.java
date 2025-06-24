package io.github.labyrinthgenerator.pages.game3d.managers;

import io.github.labyrinthgenerator.pages.game3d.nonpositional.NonPosEntity;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.HashSet;
import java.util.Set;

public class NonPosManager {

    private final Set<NonPosEntity> entities = new HashSet<>();

    public void addEntity(NonPosEntity ent) {
        entities.add(ent);
    }

    public void removeEntity(NonPosEntity ent) {
        entities.remove(ent);
    }

    public void clear() {
        entities.clear();
    }

    public void tickAllEntities(final float delta) {
        for (NonPosEntity ent : entities) {
            if (ent.shouldTick()) {
                ent.tick(delta);
            }
        }
    }
}
