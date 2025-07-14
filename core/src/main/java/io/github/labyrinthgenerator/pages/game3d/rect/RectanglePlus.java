package io.github.labyrinthgenerator.pages.game3d.rect;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
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

    public final boolean isStatic;

    public boolean overlaps = false;

    private Mesh rectMesh;
    private Matrix4 transform;

    public RectanglePlus(float x, float y, float z, float width, float height, float depth,
                         int connectedEntityId, RectanglePlusFilter filter, boolean isStatic,
                         RectManager rectMan) {
        this.id = maxId.getAndIncrement();
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;

        createMeshAndTransform();

        this.connectedEntityId = connectedEntityId;
        this.filter = filter;

        this.isStatic = isStatic;

        rectMan.addRect(this);
    }

    public boolean overlaps(RectanglePlus r) {
        return x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y && z < r.z + r.depth && z + depth > r.z;
    }

    public Vector3 getPositionImmutable() {
        return new Vector3(x, y, z);
    }

    public RectanglePlus set(Vector3 pos) {
        checkStatic();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;

        return this;
    }

    public Vector3 getDims() {
        return new Vector3(width, height, depth);
    }

    public void checkStatic() {
        if (isStatic) throw new UnsupportedOperationException("Rect " + this + " is static.");
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
        checkStatic();
        this.y = y;
    }

    public void setX(float x) {
        checkStatic();
        this.x = x;
    }

    public void setZ(float z) {
        checkStatic();
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

    @Override
    public String toString() {
        return "id: " + id + " [" + x + "," + y + "," + z + "; " + width + "," + height + "," + depth + ']';
    }

    private void createMeshAndTransform() {
        rectMesh = new Mesh(true, 24, 0,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"));
        float[] vertices = new float[]{
            0, 0, 0, width, 0, 0, // Front bottom
            width, 0, 0, width, 0, depth, // Right bottom
            width, 0, depth, 0, 0, depth, // Back bottom
            0, 0, depth, 0, 0, 0, // Left bottom
            0, height, 0, width, height, 0, // Front top
            width, height, 0, width, height, depth, // Right top
            width, height, depth, 0, height, depth, // Back top
            0, height, depth, 0, height, 0, // Left top
            0, 0, 0, 0, height, 0, // Left front,
            width, 0, 0, width, height, 0, // Right front
            0, 0, depth, 0, height, depth, // Left back
            width, 0, depth, width, height, depth // Right back
        };
        rectMesh.setVertices(vertices);

        transform = new Matrix4();
        transform.translate(x, y, z);
        transform.scale(1, 1, 1);
    }

    public Mesh getMesh() {
        return rectMesh;
    }

    public Matrix4 getTransformMatrix() {
        if (isStatic) return transform;
        transform.idt();
        transform.translate(x, y, z);
        transform.scale(1, 1, 1);
        return transform;
    }
}
