package io.github.labyrinthgenerator.pages.game3d.gravity;

import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir.*;

public class GravityControl {

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

    public static final Vector3[] sclMask = new Vector3[]{
        new Vector3( -1, -1, -1),  // down
        new Vector3( -1,  0, -1),  // up
        new Vector3( -1,  0,  0),  // forward
        new Vector3(  0,  0, -1),  // back
        new Vector3( -1,  0,  0),  // right
        new Vector3(  0,  0, -1),  // left
    };
    // @formatter:on

    public static Vector3 adjustVecForGravity(GravityDir gravityDir, Vector3 in) {
        return adjustVecForGravity(gravityDir, in, null);
    }

    public static Vector3 adjustVecForGravity(GravityDir gravityDir, Vector3 in, Vector3i worldSize) {
        Vector3 out;
        if (gravityDir == DOWN) {
            out = new Vector3(in.x, in.y, in.z);
            out.scl(scl[DOWN.ord]);
        } else if (gravityDir == UP) {
            out = new Vector3(in.z, in.y, in.x);
            out.scl(scl[UP.ord]);
            if (worldSize != null)
                out.add(new Vector3(0, worldSize.y, 0));
        } else if (gravityDir == FORWARD) {
            out = new Vector3(in.z, in.x, in.y);
            out.scl(scl[FORWARD.ord]);
        } else if (gravityDir == BACK) {
            out = new Vector3(in.z, in.x, in.y);
            out.scl(scl[BACK.ord]);
            if (worldSize != null)
                out.add(new Vector3(worldSize.x, 0, worldSize.z));
        } else if (gravityDir == LEFT) {
            out = new Vector3(in.y, in.z, in.x);
            out.scl(scl[LEFT.ord]);
        } else if (gravityDir == RIGHT) {
            out = new Vector3(in.y, in.z, in.x);
            out.scl(scl[RIGHT.ord]);
            if (worldSize != null)
                out.add(worldSize.x, 0, worldSize.z);
        } else {
            out = in;
        }
        return out;
    }
}
