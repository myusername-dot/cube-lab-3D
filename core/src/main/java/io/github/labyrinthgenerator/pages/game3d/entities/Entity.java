package io.github.labyrinthgenerator.pages.game3d.entities;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.managers.EntityManager;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.light.PointLightPlus;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AlreadyConnectedException;
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

    protected PointLightPlus pointLight = null;

    public Entity(Vector3 position, final GameScreen screen) {
        this.position = position.cpy();
        this.screen = screen;

        entMan = screen.game.getEntMan();
        id = entMan.assignId();
        chunk = entMan.addEntityOnChunk(this.position, this);
        chunkMan = screen.game.getChunkMan();
    }

    public void setPosition(Vector3 newPosition) {
        position.set(newPosition);
        if (!chunk.contains(position.x, position.y, position.z)) {
            updateChunk();
        }
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        if (!chunk.contains(position.x, position.y, position.z)) {
            updateChunk();
        }
    }

    public void addToPosition(float x, float y, float z) {
        position.add(x, y, z);
        if (!chunk.contains(position.x, position.y, position.z)) {
            updateChunk();
        }
    }

    public synchronized void updateChunk() {
        if (isDestroyed) return;
        Vector3 worldSize = chunkMan.getWorldSize();
        if (position.x < 0 || position.y > 0 || position.z < 0 ||
            position.x > worldSize.x || position.y < worldSize.y || position.z > worldSize.z) {
            //log.warn("Entity id: " + id + " position " + position + " out of bounds.");
            return;
        }
        if (chunk != chunkMan.get(chunk.x, chunk.y, chunk.z)) {
            throw new RuntimeException("Entity id: " + id + ", method updateChunk(), " + chunk + " is invalid.");
        }
        Chunk newChunk = chunkMan.get(position.x, position.y, position.z);
        if (newChunk == null) {
            throw new NullPointerException("Chunk at position " + position + " is null.");
        }
        if (newChunk.equals(chunk)) {
            throw new UnsupportedOperationException("Ent id " + id + " at position " + position + " " + chunk + " newChunk == chunk.");
        }
        entMan.updateEntityChunk(chunk, newChunk, this);
        screen.game.getRectMan().updateEntityChunkIfExistsRect(chunk, newChunk, this);
        chunk = newChunk;
    }

    public Vector3 getPositionImmutable() {
        return position.cpy();
    }

    protected Vector3 doNotTouchPosition() {
        return position;
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

    public Chunk getChunk() {
        return chunk;
    }

    public int getId() {
        return id;
    }

    public PointLightPlus getPointLight() {
        return pointLight;
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
        else throw new AlreadyConnectedException(); // todo create exception
        isTick = true;
    }

    public synchronized void afterTick() {
        isTick = false;
    }

    public synchronized void destroy() {
        if (!isDestroyed) {
            entMan.removeEntity(this);
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
