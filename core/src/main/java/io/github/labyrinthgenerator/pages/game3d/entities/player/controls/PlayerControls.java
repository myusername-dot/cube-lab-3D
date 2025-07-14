package io.github.labyrinthgenerator.pages.game3d.entities.player.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerControls {
    private final GameScreen screen;
    private final Player player;

    private final PlayersCamera cameraControls;
    private final PlayersInventory inventoryControls;
    private final PlayersMovement movementControls;

    boolean isOnGround = true;

    boolean headBob = false;
    boolean verticalCameraMovement = false;
    boolean jumping = false;
    boolean cheats = false;

    public PlayerControls(GameScreen screen, Player player) {
        this.screen = screen;
        this.player = player;
        this.cameraControls = new PlayersCamera(player, this);
        this.inventoryControls = new PlayersInventory(player);
        this.movementControls = new PlayersMovement(player, this);
    }

    public void handleInput(final float delta) {
        handleDebugInput();
        handleGravityInput();
        inventoryControls.handleInventoryInput();
        cameraControls.rotateCam(delta);
        movementControls.handleMovement(delta);
        updateHeadBob();
        movementControls.updateRectNewPosition(delta);
    }

    private void handleDebugInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            toggleDebugMode();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            jumping = !jumping;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            verticalCameraMovement = !verticalCameraMovement;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            cheats = !cheats;
        }
    }

    private void toggleDebugMode() {
        MyDebugRenderer debugger = screen.game.getDebugger();
        debugger.debugMode = MyDebugRenderer.DebugMode.values()[(debugger.debugMode.ordinal() + 1) % MyDebugRenderer.DebugMode.values().length];
    }

    private void handleGravityInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            cameraControls.reSwapCameraDirection();
            GravityControls.swapVerticalGravityDir();
            cameraControls.updateCameraRotation();
            isOnGround = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            cameraControls.reSwapCameraDirection();
            GravityControls.randomGravityDir();
            cameraControls.updateCameraRotation();
            isOnGround = false;
        }
    }

    private void updateHeadBob() {
        if (headBob) {
            cameraControls.camY = 0;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * movementControls.playerMoveSpeed * 4f)
                * 0.01875f);
            cameraControls.camY += sinOffset;
            headBob = false;
        }
    }

    public void tick() {
        if (!cheats) {
            screen.checkOverlaps(player.rect);
        } else {
            player.rect.set(player.rect.newPosition);
        }

        player.setPosition(
            player.rect.getX() + player.rect.getWidth() / 2f,
            player.rect.getY() + player.rect.getHeight() / 2f,
            player.rect.getZ() + player.rect.getDepth() / 2f
        );
        cameraControls.setCamPosition();

        player.rect.oldPosition.set(player.rect.getPositionImmutable());
    }

    public Vector2 getHorizontalMovementDir() {
        return movementControls.horizontalMovementDir.cpy();
    }

    public Vector3 getDirection() {
        return player.playerCam.direction.cpy();
    }

    public Vector2 getHorizontalVelocity() {
        return movementControls.horizontalVelocity.cpy();
    }

    public Vector2 getHorizontalForwardVelocity() {
        return movementControls.horizontalForwardVelocity.cpy();
    }

    public float getVerticalVelocity() {
        return movementControls.verticalVelocity;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public Vector3 getHorizontalAxis() {
        return cameraControls.currentHorizontalAxis.cpy();
    }

    public Vector3 getVerticalAxis() {
        return cameraControls.currentVerticalAxis.cpy();
    }

    public float getCurrentHorizontalAngle() {
        return cameraControls.currentHorizontalAngle;
    }
}
