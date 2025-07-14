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

    public static Vector3 adjustWorldVecForGravity(Vector3 in) {
        return adjustWorldVecForGravity(in, null);
    }

    public static Vector3 adjustWorldVecForGravity(Vector3 in, Vector3i worldSize) {
        switch (currentGravity) {
            case DOWN:
                in.scl(worldGravityScl[DOWN.ord]);
                break;
            case UP:
                in.set(in.z, in.y, in.x).scl(worldGravityScl[UP.ord]);
                if (worldSize != null) {
                    in.add(new Vector3(0, worldSize.y, 0));
                }
                break;
            case FORWARD:
                in.set(in.z, in.x, in.y).scl(worldGravityScl[FORWARD.ord]);
                break;
            case BACK:
                in.set(in.z, in.x, in.y).scl(worldGravityScl[BACK.ord]);
                if (worldSize != null) {
                    in.add(new Vector3(worldSize.x, 0, worldSize.z));
                }
                break;
            case LEFT:
                in.set(in.y, in.z, in.x).scl(worldGravityScl[LEFT.ord]);
                break;
            case RIGHT:
                in.set(in.y, in.z, in.x).scl(worldGravityScl[RIGHT.ord]);
                if (worldSize != null) {
                    in.add(worldSize.x, 0, worldSize.z);
                }
                break;
            default:
                break;
        }
        return in;
    }

    public static Vector3 swap(Vector3 v) {
        switch (currentGravity) {
            case UP:
            case DOWN:
            default:
                break;
            case FORWARD:
            case BACK:
                v.set(v.y, v.x, v.z);
                break;
            case LEFT:
            case RIGHT:
                v.set(v.x, v.z, v.y);
                break;
        }
        return v;
    }

    public static Vector3 reSwap(final Vector3 in) {
        switch (currentGravity) {
            case UP:
            case DOWN:
            default:
                break;
            case FORWARD:
            case BACK:
                in.set(in.y, in.x, in.z);
                break;
            case LEFT:
            case RIGHT:
                in.set(in.x, in.z, in.y);
                break;
        }
        return in;
    }

    public static float getGravityScl() {
        return gravity[currentGravity.ord].sum();
    }

    // @formatter:off
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
