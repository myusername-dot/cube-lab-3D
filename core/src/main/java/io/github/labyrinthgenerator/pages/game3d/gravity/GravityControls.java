package io.github.labyrinthgenerator.pages.game3d.gravity;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir.*;

public class GravityControls {

    public static final GravityDir[] gravityDirections = new GravityDir[]{
        DOWN, FORWARD, UP, BACK, RIGHT, LEFT
    };

    // @formatter:off
    public static final Vector3f[] gravity = new Vector3f[]{
        new Vector3f( 0, -1,  0),  // down
        new Vector3f( 0,  1,  0),  // up
        new Vector3f( 1,  0,  0),  // forward
        new Vector3f(-1,  0,  0),  // back
        new Vector3f( 0,  0, -1),  // right
        new Vector3f( 0,  0,  1),  // left
    };

    public static final Vector3[] worldGravityScl = new Vector3[]{
        new Vector3(  1,  1,  1),  // down
        new Vector3(  1, -1,  1),  // up
        new Vector3(  1,  1,  1),  // forward
        new Vector3( -1,  1, -1),  // back
        new Vector3( -1,  1, -1),  // right
        new Vector3(  1,  1,  1),  // left
    };

    public static final Vector3[] worldPositionRectangleAdd = new Vector3[]{
        new Vector3(  0, -1,  0),  // down
        new Vector3(  0,  0,  0),  // up
        new Vector3(  0,  0, -1),  // forward
        new Vector3( -1,  0,  0),  // back
        new Vector3(  0,  0, -1),  // right
        new Vector3( -1,  0,  0),  // left
    };
    // @formatter:on

    public static GravityDir currentGravity = DOWN;

    public static Vector3 adjustWorldVecForGravity(Vector3 dst) {
        return adjustWorldVecForGravity(dst, null);
    }

    public static Vector3 adjustWorldVecForGravity(Vector3 dst, Vector3i worldSize) {
        switch (currentGravity) {
            case DOWN:
                dst.scl(worldGravityScl[DOWN.ord]);
                break;
            case UP:
                dst.set(dst.z, dst.y, dst.x).scl(worldGravityScl[UP.ord]);
                if (worldSize != null) {
                    dst.add(new Vector3(0, worldSize.y, 0));
                }
                break;
            case FORWARD:
                dst.set(dst.z, dst.x, dst.y).scl(worldGravityScl[FORWARD.ord]);
                break;
            case BACK:
                dst.set(dst.z, dst.x, dst.y).scl(worldGravityScl[BACK.ord]);
                if (worldSize != null) {
                    dst.add(new Vector3(worldSize.x, 0, worldSize.z));
                }
                break;
            case LEFT:
                dst.set(dst.y, dst.z, dst.x).scl(worldGravityScl[LEFT.ord]);
                break;
            case RIGHT:
                dst.set(dst.y, dst.z, dst.x).scl(worldGravityScl[RIGHT.ord]);
                if (worldSize != null) {
                    dst.add(worldSize.x, 0, worldSize.z);
                }
                break;
            default:
                break;
        }
        return dst;
    }

    public static float getGravityScl() {
        return gravity[currentGravity.ord].sum();
    }

    // @formatter:off
    public static Vector3 swap(Vector3 dst) {
        switch (currentGravity) {
            case UP: case DOWN: default:
                break;
            case FORWARD: case BACK:
                dst.set(dst.y, dst.x, dst.z);
                break;
            case LEFT: case RIGHT:
                dst.set(dst.x, dst.z, dst.y);
        }
        return dst;
    }

    public static Vector3 reSwap(final Vector3 dst) {
        switch (currentGravity) {
            case UP: case DOWN: default:
                break;
            case FORWARD: case BACK:
                dst.set(dst.y, dst.x, dst.z);
                break;
            case LEFT: case RIGHT:
                dst.set(dst.x, dst.z, dst.y);
        }
        return dst;
    }

    public static void swapVerticalGravityDir() {
        switch (currentGravity) {
            case DOWN: currentGravity = UP; break;
            case UP: currentGravity = DOWN; break;
            case RIGHT: currentGravity = LEFT; break;
            case LEFT: currentGravity = RIGHT; break;
            case BACK: currentGravity = FORWARD; break;
            case FORWARD: currentGravity = BACK; break;
        }
    }

    public static void randomGravityDir() {
        switch (currentGravity) {
            case DOWN: currentGravity = BACK; break;
            case UP: currentGravity = FORWARD; break;
            case RIGHT: currentGravity = UP; break;
            case LEFT: currentGravity = DOWN; break;
            case BACK: currentGravity = LEFT; break;
            case FORWARD: currentGravity = RIGHT; break;
        }
    }
    // @formatter:on
}
