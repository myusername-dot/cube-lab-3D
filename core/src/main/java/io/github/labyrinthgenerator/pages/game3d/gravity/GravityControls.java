package io.github.labyrinthgenerator.pages.game3d.gravity;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir.*;

public class GravityControls {

    // @formatter:off
    public static final Vector3f[] gravity = new Vector3f[]{
        new Vector3f( 0,  1,  0),  // down
        new Vector3f( 0, -1,  0),  // up
        new Vector3f( 1,  0,  0),  // forward
        new Vector3f(-1,  0,  0),  // back
        new Vector3f( 0,  0, -1),  // right
        new Vector3f( 0,  0,  1),  // left
    };

    public static final Vector3[] scl = new Vector3[]{
        new Vector3(  1,  1,  1),  // down
        new Vector3(  1, -1,  1),  // up
        new Vector3(  1, -1, -1),  // forward
        new Vector3( -1, -1,  1),  // back
        new Vector3(  1, -1, -1),  // right
        new Vector3( -1, -1,  1),  // left
    };

    public static final Vector3[] sclAddMask = new Vector3[]{
        new Vector3( -1, -1, -1),  // down
        new Vector3( -1,  2, -1),  // up
        new Vector3( -1,  0,  0),  // forward
        new Vector3(  0,  0, -1),  // back
        new Vector3( -1,  0,  0),  // right
        new Vector3(  0,  0, -1),  // left
    };
    // @formatter:on

    public static GravityDir currentGravity = DOWN;

    public static Vector3 adjustVecForGravity(Vector3 in) {
        return adjustVecForGravity(in, null);
    }

    public static Vector3 adjustVecForGravity(Vector3 in, Vector3i worldSize) {
        Vector3 out;
        switch (currentGravity) {
            case DOWN:
                out = new Vector3(in.x, in.y, in.z);
                out.scl(scl[DOWN.ord]);
                break;
            case UP:
                out = new Vector3(in.z, in.y, in.x);
                out.scl(scl[UP.ord]);
                if (worldSize != null)
                    out.add(new Vector3(0, worldSize.y, 0));
                break;
            case FORWARD:
                out = new Vector3(in.z, in.x, in.y);
                out.scl(scl[FORWARD.ord]);
                break;
            case BACK:
                out = new Vector3(in.z, in.x, in.y);
                out.scl(scl[BACK.ord]);
                if (worldSize != null)
                    out.add(new Vector3(worldSize.x, 0, worldSize.z));
                break;
            case LEFT:
                out = new Vector3(in.y, in.z, in.x);
                out.scl(scl[LEFT.ord]);
                break;
            case RIGHT:
                out = new Vector3(in.y, in.z, in.x);
                out.scl(scl[RIGHT.ord]);
                if (worldSize != null)
                    out.add(worldSize.x, 0, worldSize.z);
                break;
            default:
                out = in;
        }
        return out;
    }

    public static Vector3 swap(Vector3 in, boolean yScl, boolean invY) {
        float scl = yScl ? GravityControls.getYScl() : 1; // gravity y dir
        float inv = invY ? -1 : 1; // libgdx y < 0
        Vector3 out;
        switch (currentGravity) {
            case UP:
            case DOWN:
                out = new Vector3(in.x, in.y * inv * scl, in.z);
                break;
            case FORWARD:
            case BACK:
                out = new Vector3(in.y * inv, in.x * scl, in.z);
                //out = new Vector3(in.z, in.x * scl, in.y * inv);
                break;
            case LEFT:
            case RIGHT:
                out = new Vector3(in.x, in.z * scl, in.y * inv);
                //out = new Vector3(in.y * inv, in.z * scl, in.x);
                break;
            default:
                out = in;
        }
        return out;
    }

    public static Vector3 reSwap(Vector3 in, boolean yScl, boolean invY) {
        float scl = yScl ? GravityControls.getYScl() : 1;
        float inv = invY ? -1 : 1;
        Vector3 out;
        switch (currentGravity) {
            case UP:
            case DOWN:
                out = new Vector3(in.x, in.y * inv * scl, in.z);
                break;
            case FORWARD:
            case BACK:
                out = new Vector3(in.y * scl, in.x * inv, in.z);
                //out = new Vector3(in.y * scl, in.z * inv, in.x);
                break;
            case LEFT:
            case RIGHT:
                out = new Vector3(in.x, in.z * inv, in.y * scl);
                //out = new Vector3(in.z, in.x * inv, in.y * scl);
                break;
            default:
                out = in;
        }
        return out;
    }

    public static float getYScl() {
        return gravity[currentGravity.ord].sum();
    }

    public static void swapYDir() {
        GravityDir newDir = DOWN;
        switch (currentGravity) {
            case DOWN:
                newDir = UP;
                break;
            case UP:
                newDir = DOWN;
                break;
            case RIGHT:
                newDir = LEFT;
                break;
            case LEFT:
                newDir = RIGHT;
                break;
            case BACK:
                newDir = FORWARD;
                break;
            case FORWARD:
                newDir = BACK;
                break;
        }
        currentGravity = newDir;
    }

    public static void swapXDir() {
        GravityDir newDir = LEFT;
        switch (currentGravity) {
            case DOWN:
                newDir = BACK;
                break;
            case UP:
                newDir = FORWARD;
                break;
            case RIGHT:
                newDir = UP;
                break;
            case LEFT:
                newDir = DOWN;
                break;
            case BACK:
                newDir = LEFT;
                break;
            case FORWARD:
                newDir = RIGHT;
                break;
        }
        currentGravity =  newDir;
    }
}
