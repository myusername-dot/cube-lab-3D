package io.github.labyrinthgenerator.pages.game3d.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Array;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.HashMap;
import java.util.Map;

public class EntityManager {
	private int nextId = 0;

	public final Map<Integer, Entity> entities = new HashMap<>();

	private GameScreen screen;

	public void addEntity(final Entity ent) {
		entities.put(ent.getId(), ent);
	}

	public int assignId() {
		return nextId++;
	}

	public Entity getEntityFromId(final int id) {
		return entities.get(id);
	}

	public GameScreen getScreen() {
		return screen;
	}

	public void removeEntity(final int id) {
		entities.remove(id);
	}

	public void render2DAllEntities(final float delta) {
		for (final Entity ent : entities.values()) {
			if (ent.shouldRender2D()) {
				ent.render2D(delta);
			}
		}
	}

	public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta) {
		for (final Entity ent : entities.values()) {
//			ent.setRender3D(false);

            /*if (ent instanceof Enemy) {
                Enemy enemy = (Enemy) ent;
                if (!enemy.isPlayerInRange()) {
                    continue;
                }
            }*/
			if (ent.shouldRender3D()) {
				ent.render3D(mdlBatch, env, delta);
			}

		}
	}

	public void setScreen(final GameScreen screen) {
		this.screen = screen;
	}

	public void spawnEntity(final Entity ent) {
		entities.put(ent.getId(), ent);
	}

	public void tickAllEntities(final float delta) {
		for (final Entity ent : entities.values()) {
			if (ent.shouldTick()) {
				ent.tick(delta);
			}
		}
	}
}
