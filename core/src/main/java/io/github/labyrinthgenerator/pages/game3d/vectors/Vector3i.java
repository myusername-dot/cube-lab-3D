package io.github.labyrinthgenerator.pages.game3d.vectors;

import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public class Vector3i {

    public int x;
    public int y;
    public int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(Vector3i v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3i(Vector3 v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
    }

    public Vector3i set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3i set(Vector3i v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vector3i cpy() {
        return new Vector3i(this);
    }

    public Vector3 vec3() {
        return new Vector3(x, y, z);
    }

    public Vector3i add(Vector3i v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vector3i add(int v) {
        x += v;
        y += v;
        z += v;
        return this;
    }

    public Vector3i scl(int scalar) {
        return this.set(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3i scl(float scalar) {
        return this.set((int) (this.x * scalar), (int) (this.y * scalar), (int) (this.z * scalar));
    }

    public int dot (Vector3i v) {
        return x * v.x + y * v.y + z * v.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3i vector3i = (Vector3i) o;
        return x == vector3i.x && y == vector3i.y && z == vector3i.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3i[" + "x=" + x + ", y=" + y + ", z=" + z + ']';
    }
}
