package io.github.labyrinthgenerator.pages.game3d.rect;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.managers.RectManager;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RectanglePlus {
    private static final AtomicInteger maxId = new AtomicInteger(0);
    public final int id;
    private static final long serialVersionUID = 6589196508238637331L;

    private float x, y, z;
    private final float width, height, depth;

    public final Vector3 oldPosition = new Vector3();
    public final Vector3 newPosition = new Vector3();

    private final int connectedEntityId;
    public final RectanglePlusFilter filter;

    public boolean overlaps = false;

    public RectanglePlus(float x, float y, float z, float width, float height, float depth,
                         int connectedEntityId, RectanglePlusFilter filter, RectManager rectMan) {
        this.id = maxId.getAndIncrement();
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.connectedEntityId = connectedEntityId;
        this.filter = filter;

        rectMan.addRect(this);
    }

    public boolean overlaps(RectanglePlus r) {
        return x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y && z < r.z + r.depth && z + depth > r.z;
    }

    public Vector3 getPosition() {
        return new Vector3(x, y, z);
    }

    public RectanglePlus set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    public RectanglePlus add(Vector3 v) {
        x += v.x;
        y += v.y;
        z += v.z;

        return this;
    }

    public int getConnectedEntityId() {
        return connectedEntityId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RectanglePlus that = (RectanglePlus) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
