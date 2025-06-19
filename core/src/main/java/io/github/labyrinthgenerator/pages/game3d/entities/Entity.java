package io.github.labyrinthgenerator.pages.game3d.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.chunks.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.utils.EntityManager;

import java.util.Objects;

public class Entity {
	public final GameScreen screen;
    public final EntityManager entMan;
    private final ChunkManager chunkMan;

    private final Vector3 position;
    private Chunk chunk;
    protected final int id;
	protected boolean tick = true;
	protected boolean render2D = true;
	protected boolean render3D = true;

	protected Entity collidedEntity = null;

	public Entity(Vector3 position, final GameScreen screen) {
        this.position = position.cpy();
		this.screen = screen;

        entMan = screen.game.getEntMan();
		id = entMan.assignId();
        chunk = entMan.addEntityOnChunk(this.position.x, this.position.z, this);
        chunkMan = screen.game.getChunkMan();
	}

    public void setPosition(Vector3 newPosition) {
        position.set(newPosition);
        if (!chunk.contains(position.x, position.z)) {
            updateChunk();
        }
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        if (!chunk.contains(position.x, position.z)) {
            updateChunk();
        }
    }

    public void addToPosition(float x, float y, float z) {
        position.add(x, y, z);
        if (!chunk.contains(position.x, position.z)) {
            updateChunk();
        }
    }

    public void updateChunk() {
        Chunk newChunk = chunkMan.get(position.x, position.z);
        assert newChunk != chunk;
        if (newChunk == null) {
            throw new NullPointerException("Chunk at position " + position.x + ", " + position.z + " is null.");
        }
        entMan.updateEntityChunk(chunk, newChunk, this);
        screen.game.getRectMan().updateEntityChunkIfExistsRect(chunk, newChunk, this);
        chunk = newChunk;
    }

    public Vector3 getPositionImmutable() {
        return position.cpy();
    }

    public float getPositionX() {
        return position.x;
    }

    public float getPositionY() {
        return position.y;
    }

    public float getPositionZ() {
        return position.z;
    }

	public int getId() {
		return id;
	}

	public void onCollision(final RectanglePlus otherRect) {
		collidedEntity = entMan.getEntityFromId(otherRect.getConnectedEntityId());
	}

	public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
	}

	public boolean shouldRender3D() {
		return render3D;
	}

	public boolean shouldTick() {
		return tick;
	}

	public void tick(final float delta) {
	}

    public void destroy() {
        entMan.removeEntity(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
