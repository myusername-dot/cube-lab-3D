package io.github.labyrinthgenerator.pages.game3d.vectors;

import com.badlogic.gdx.math.Vector3;

import java.util.Objects;

public class Vector3f {

    public float x;
    public float y;
    public float z;

    public Vector3f() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3f(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3f set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3f cpy () {
        return new Vector3f(this);
    }

    public Vector3f scl (float scalar) {
        return set(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3f sub (Vector3 v) {
        return set(x * v.x, y * v.y, z * v.z);
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

    public Vector3f add (Vector3 v) {
        return set(x + v.x, y + v.y, z + v.z);
    }

    public Vector3f roundInt () {
        return set((int) x, (int) y, (int) z);
    }

    public Vector3 vec3 () {
        return new Vector3(x, y, z);
    }

    public float sum () {
        return x + y + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Vector3f) {
            Vector3f vector3f = (Vector3f) o;
            return x == vector3f.x && y == vector3f.y && z == vector3f.z;
        } else if (o instanceof Vector3) {
            Vector3 vector3 = (Vector3) o;
            return x == vector3.x && y == vector3.y && z == vector3.z;
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

