package io.github.labyrinthgenerator.pages.game3d.gravity;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir.*;

public class GravityControls {

    // @formatter:off
    public static final Vector3f[] gravity = new Vector3f[]{
        new Vector3f( 0, -1,  0),  // down
        new Vector3f( 0,  1,  0),  // up
        new Vector3f( 1,  0,  0),  // forward
        new Vector3f(-1,  0,  0),  // back
        new Vector3f( 0,  0, -1),  // right
        new Vector3f( 0,  0,  1),  // left
    };

    public static final Vector3[] worldScl = new Vector3[]{
        new Vector3(  1, -1,  1),  // down
        new Vector3(  1,  1,  1),  // up
        new Vector3(  1,  1, -1),  // forward
        new Vector3( -1,  1,  1),  // back
        new Vector3(  1,  1, -1),  // right
        new Vector3( -1,  1,  1),  // left
    };

    public static final Vector3[] worldSclAddMask = new Vector3[]{
        new Vector3( -1,  0, -1),  // down
        new Vector3( -1, -1, -1),  // up
        new Vector3( -1, -1,  0),  // forward
        new Vector3(  0, -1, -1),  // back
        new Vector3( -1, -1,  0),  // right
        new Vector3(  0, -1, -1),  // left
    };
    // @formatter:on

    public static GravityDir currentGravity = DOWN;

    public static Vector3 adjustWorldVecForGravity(Vector3 in) {
        return adjustWorldVecForGravity(in, null);
    }

    public static Vector3 adjustWorldVecForGravity(final Vector3 in, Vector3i worldSize) {
        Vector3 out = new Vector3(in); // Создаем выходной вектор на основе входного
        switch (currentGravity) {
            case DOWN:
                out.scl(worldScl[DOWN.ord]);
                break;
            case UP:
                out.set(in.z, in.y, in.x).scl(worldScl[UP.ord]);
                if (worldSize != null) {
                    out.add(new Vector3(0, worldSize.y, 0));
                }
                break;
            case FORWARD:
                out.set(in.z, in.x, in.y).scl(worldScl[FORWARD.ord]);
                break;
            case BACK:
                out.set(in.z, in.x, in.y).scl(worldScl[BACK.ord]);
                if (worldSize != null) {
                    out.add(new Vector3(worldSize.x, 0, worldSize.z));
                }
                break;
            case LEFT:
                out.set(in.y, in.z, in.x).scl(worldScl[LEFT.ord]);
                break;
            case RIGHT:
                out.set(in.y, in.z, in.x).scl(worldScl[RIGHT.ord]);
                if (worldSize != null) {
                    out.add(worldSize.x, 0, worldSize.z);
                }
                break;
            default:
                break;
        }
        return out;
    }

    public static Vector3 swap(final Vector3 in, boolean gScl) {
        // Local gravity dir
        float scl = gScl ? GravityControls.getGravityScl() : 1;

        Vector3 out = new Vector3(in); // Создаем выходной вектор
        switch (currentGravity) {
            case UP:
            case DOWN:
                out.y *= scl;
                break;
            case FORWARD:
            case BACK:
                out.set(in.y, in.x * scl, in.z);
                break;
            case LEFT:
            case RIGHT:
                out.set(in.x, in.z * scl, in.y);
                break;
            default:
                break;
        }
        return out;
    }

    public static Vector3 reSwap(final Vector3 in, boolean gScl) {
        // Local gravity dir
        float scl = gScl ? GravityControls.getGravityScl() : 1;

        Vector3 out = new Vector3(in); // Создаем выходной вектор
        switch (currentGravity) {
            case UP:
            case DOWN:
                out.y *= scl;
                break;
            case FORWARD:
            case BACK:
                out.set(in.y * scl, in.x, in.z);
                break;
            case LEFT:
            case RIGHT:
                out.set(in.x, in.z, in.y * scl);
                break;
            default:
                break;
        }
        return out;
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
