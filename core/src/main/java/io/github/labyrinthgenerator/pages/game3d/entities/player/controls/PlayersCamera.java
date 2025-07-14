package io.github.labyrinthgenerator.pages.game3d.entities.player.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.currentGravity;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.gravity;

public class PlayersCamera {
    private final Player player;
    private final PlayerControls controls;

    float camY = 0;

    private final float cameraRotationSpeed = 25f;

    final Vector3 currentHorizontalAxis = Vector3.Y.cpy();
    final Vector3 currentVerticalAxis = new Vector3();

    private static final float MAX_VERTICAL_ANGLE = 80f;

    float currentHorizontalAngle = 0f;
    float currentVerticalAngle = 0f;

    PlayersCamera(Player player, PlayerControls controls) {
        this.player = player;
        this.controls = controls;
    }

    void setCamPosition() {
        player.playerCam.position.set(player.getPositionImmutable().add(GravityControls.swap(new Vector3(0f, camY, 0f))));
    }

    void reSwapCameraDirection() {
        player.playerCam.direction.set(GravityControls.reSwap(player.playerCam.direction));
    }

    void rotateCam(float delta) {
        rotateCamHorizontal(delta);
        if (controls.verticalCameraMovement) {
            rotateCamVertical(delta);
        }
    }

    private void rotateCamHorizontal(float delta) {
        // Gravity dir -1 or 1
        float gScl = GravityControls.getGravityScl();
        float angle = Gdx.input.getDeltaX() * -cameraRotationSpeed * gScl * delta;
        currentHorizontalAngle += angle;
        // Rotate local dir angle
        player.playerCam.rotate(currentHorizontalAxis, angle);
    }

    private void rotateCamVertical(float delta) {
        float gScl = GravityControls.getGravityScl() * (currentGravity == GravityDir.UP || currentGravity == GravityDir.DOWN ? 1 : -1);
        float angle = Gdx.input.getDeltaY() * -cameraRotationSpeed * gScl * delta;
        currentVerticalAngle = MathUtils.clamp(currentVerticalAngle + angle, -MAX_VERTICAL_ANGLE, MAX_VERTICAL_ANGLE);
        player.playerCam.rotate(setCurrentVerticalAxis(), angle);
    }

    private Vector3 setCurrentVerticalAxis() {
        Vector3 axis = player.playerCam.direction.cpy();
        // Scl -x, z coords
        Vector3 axScl = new Vector3(-1, 0, 1);
        // Goto local gravity coords
        axScl = GravityControls.swap(axScl);
        // Swap to z -x. Local y after scaling is 0
        currentVerticalAxis.set(swapNot0(axis.scl(axScl)));

        return currentVerticalAxis;
    }

    private Vector3 swapNot0(Vector3 in) {
        float x = in.x, y = in.y, z = in.z;
        if (x == 0) in.set(x, z, y);
        if (y == 0) in.set(z, y, x);
        if (z == 0) in.set(y, x, z);
        return in;
    }

    void updateCameraRotation() {
        currentVerticalAngle = 0f;
        currentHorizontalAngle = 0f;
        // Goto local gravity coords
        currentHorizontalAxis.set(GravityControls.swap(Vector3.Y));
        player.playerCam.up.set(gravity[currentGravity.ord].vec3());
        player.playerCam.direction.set(GravityControls.swap(player.playerCam.direction));
        player.playerCam.update();
    }
}
