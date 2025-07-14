package io.github.labyrinthgenerator.pages.game3d.vectors;

import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public class Vector3f extends Vector3 {

    public Vector3f() {
        super();
    }

    public Vector3f(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector3f(Vector3 v) {
        super(v);
    }

    @Override
    public Vector3f set(float x, float y, float z) {
        super.set(x, y, z);
        return this;
    }

    @Override
    public Vector3f cpy () {
        return new Vector3f(this);
    }

    @Override
    public Vector3f scl (float scalar) {
        super.scl(scalar);
        return this;
    }

    @Override
    public Vector3f sub (Vector3 v) {
        super.sub(v);
        return this;
    }

    public Vector3f abs () {
        return set(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public Vector3f back () {
        return new Vector3f(z, y, x);
    }

    public Vector3f shiftR () {
        return set(z, x, y);
    }

    public Vector3f shiftL () {
        return set(y, z, x);
    }

    public Vector3f invZero (float val) {
        return new Vector3f(x == 0 ? val : 0, y == 0 ? val : 0, z == 0 ? val : 0);
    }

    @Override
    public Vector3f add (Vector3 v) {
        super.add(v);
        return this;
    }

    public Vector3f roundInt () {
        return set((int) x, (int) y, (int) z);
    }

    public float sum () {
        return x + y + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Vector3f) {
            Vector3f v = (Vector3f) o;
            return x == v.x && y == v.y && z == v.z;
        } else if (o instanceof Vector3) {
            Vector3 v = (Vector3) o;
            return x == v.x && y == v.y && z == v.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vector3f[" + "x=" + x + ", y=" + y + ", z=" + z + ']';
    }
}

