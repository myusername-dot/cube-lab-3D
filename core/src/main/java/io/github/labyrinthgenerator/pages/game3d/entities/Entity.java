package io.github.labyrinthgenerator.pages.game3d.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.managers.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public abstract class Entity {
    protected final int id;

    public final GameScreen screen;
    public final EntityManager entMan;
    private final ChunkManager chunkMan;

    private final Vector3 position;
    private volatile Chunk chunk;

    protected boolean shouldTick = true;
    protected boolean render3D = true;

    protected volatile boolean isTick = false;
    protected volatile long transactionId = -1;
    protected volatile boolean isDestroyed = false;

    protected Entity collidedEntity = null;

    public Entity(Vector3 position, final GameScreen screen) {
        this.position = position.cpy();
        this.screen = screen;

        entMan = screen.game.getEntMan();
        id = entMan.assignId();
        chunk = entMan.addEntityOnChunkTransactional(this.position.x, this.position.z, this);
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

    public synchronized void updateChunk() {
        if (isDestroyed) return;
        Vector2 worldSize = chunkMan.getWorldSize();
        if (position.x < -0.5 || position.z < -0.5 ||
            position.x > worldSize.x || position.z > worldSize.y) {
            log.error("Entity id: " + id + " position " + position + " out of bounds.");
            return;
        }
        if (chunk != chunkMan.get(chunk.x, chunk.z)) {
            throw new RuntimeException("Entity id: " + id + ", method updateChunk(), " + chunk + " is invalid.");
        }
        Chunk newChunk = chunkMan.get(position.x, position.z);
        if (newChunk == null) {
            throw new NullPointerException("Chunk at position " + position + " is null.");
        }
        if (newChunk.equals(chunk)) {
            throw new UnsupportedOperationException("Ent id " + id + " at position " + position + " " + chunk + " newChunk == chunk.");
        }
        entMan.updateEntityChunkTransactional(chunk, newChunk, this);
        screen.game.getRectMan().updateEntityChunkIfExistsRectTransactional(chunk, newChunk, this);
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
        collidedEntity = entMan.getEntityById(otherRect.getConnectedEntityId());
    }

    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    }

    public boolean shouldRender3D() {
        return render3D;
    }

    public boolean shouldTick() {
        return shouldTick && !isDestroyed;
    }

    public void tick(final float delta) {
    }

    public synchronized void beforeTick() {
        long transactionId = entMan.getTransactionId();
        if (!entMan.isTransaction() || this.transactionId != transactionId) this.transactionId = transactionId;
        else
            throw new UnsupportedOperationException("Entity id: " + id + " has already been ticked in this transaction.");
        isTick = true;
    }

    public synchronized void afterTick() {
        isTick = false;
    }

    public synchronized void destroy() {
        if (!isDestroyed) {
            entMan.removeEntityTransactional(this);
            isDestroyed = true;
        }
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
