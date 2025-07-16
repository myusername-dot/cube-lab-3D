package io.github.labyrinthgenerator.pages.game3d.entities.player.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.managers.RectManager;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayersMovement {
    private final Player player;
    private final PlayerControls controls;
    private final ChunkManager chunkMan;
    private final RectManager rectMan;

    final float playerMoveSpeed = 4f;
    private final float acceleration = 10f;
    private final float deceleration = 10f;
    private final float jumpStrength = 8.0f;

    float verticalVelocity = 0f;

    final Vector2 horizontalMovementDir = new Vector2();
    final Vector2 horizontalVelocity = new Vector2();
    final Vector2 horizontalForwardVelocity = new Vector2();

    PlayersMovement(Player player, PlayerControls controls) {
        this.player = player;
        this.controls = controls;
        this.chunkMan = player.screen.game.getChunkMan();
        this.rectMan = player.screen.game.getRectMan();
    }

    void resetHorizontalMovementDir() {
        horizontalMovementDir.setZero();
    }

    void handleMovement(float delta) {
        handleHorizontalMovement(delta);
        handleVerticalMovement(delta);
        updateRectNewPosition(delta);
    }

    private void handleHorizontalMovement(float delta) {
        if (!controls.isOnGround && !controls.jumping && !controls.cheats) {
            horizontalVelocity.set(0, 0);
            return;
        }
        resetHorizontalMovementDir();
        // local gravity x z, inv -y
        Vector3 localCamDir = GravityControls.swap(player.playerCam.direction.cpy());
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            horizontalMovementDir.add(localCamDir.x, localCamDir.z);
            controls.headBob = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            horizontalMovementDir.sub(localCamDir.x, localCamDir.z);
            controls.headBob = true;
        }
        applyHorizontalMovement(handleStrafeMovement(), delta);
    }

    private boolean handleStrafeMovement() {
        Vector3 rightDir = GravityControls.swap(player.playerCam.direction.cpy().crs(player.playerCam.up));
        boolean horizontalMovement = false;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalMovementDir.sub(rightDir.x, rightDir.z);
            horizontalMovement = true;
            controls.headBob = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalMovementDir.add(rightDir.x, rightDir.z);
            horizontalMovement = true;
            controls.headBob = true;
        }
        return horizontalMovement;
    }

    private void handleVerticalMovement(float delta) {
        if (!controls.isOnGround) {
            verticalVelocity -= 9.81f * delta;
            verticalVelocity = MathUtils.clamp(verticalVelocity, -9.81f, 9.81f);
        }
        if (controls.jumping && controls.isOnGround && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            verticalVelocity = jumpStrength;
            controls.isOnGround = false;
        }
    }

    private void applyHorizontalMovement(boolean horizontalMovement, float delta) {
        // Нормализуем направление движения и применяем ускорение
        if (horizontalMovementDir.len() > 0) {
            horizontalMovementDir.nor();
            horizontalVelocity.add(horizontalMovementDir.cpy().scl(acceleration * delta));
        } else {
            horizontalVelocity.scl(1 - deceleration * delta);
        }
        limitHorizontalVelocity(horizontalMovement);
    }

    private void limitHorizontalVelocity(boolean horizontalMovement) {
        Vector3 localCamDir = GravityControls.swap(player.playerCam.direction.cpy());

        // Ограничиваем скорость только в направлении камеры, чтобы игрока не заносило на поворотах
        Vector3 cameraForward = localCamDir.cpy().nor();
        float forwardVelocityScl = horizontalVelocity.cpy().dot(cameraForward.x, cameraForward.z); // Получаем скорость в направлении камеры
        cameraForward.scl(forwardVelocityScl);
        horizontalForwardVelocity.set(cameraForward.x, cameraForward.z); // Устанавливаем скорость только в направлении

        // Ограничиваем скорость
        if (horizontalForwardVelocity.len() > playerMoveSpeed) {
            // получаем единичный скаляр по отношению к длине и умножаем на макс скорость
            horizontalForwardVelocity.nor().scl(playerMoveSpeed);
        }

        if (!horizontalMovement) {
            horizontalVelocity.set(horizontalForwardVelocity);
        } else if (horizontalVelocity.len() > playerMoveSpeed) {
            horizontalVelocity.nor().scl(playerMoveSpeed);
        }
    }

    private void updateRectNewPosition(float delta) {
        Vector3 velocity = GravityControls.reSwap(
            new Vector3(horizontalVelocity.x, verticalVelocity * GravityControls.getGravityScl() * -1, horizontalVelocity.y));
        Vector3 newPosition = player.rect.getPositionImmutable().add(velocity.cpy().scl(delta));
        preventOnWallPosition(newPosition);
        clampNewPosition(newPosition);
        player.rect.newPosition.set(newPosition);
    }

    private void preventOnWallPosition(Vector3 newPosition) {
        if (!controls.isOnGround && verticalVelocity < 0
            && checkStaticRectAtPosition(rectMan, new Vector3f(newPosition))) {
            Vector3f goToFloor = GravityControls.gravity[GravityControls.currentGravity.ord].cpy();
            findNewPositionShift(newPosition, goToFloor);
            goToFloor.scl(0.5f);
            goToFloor.add(goToFloor.cpy().sub(player.rect.getDims()));
            newPosition.add(goToFloor);
        }
    }

    private void findNewPositionShift(Vector3 newPosition, Vector3f currentGravity) {
        currentGravity.shiftR();
        Vector3f shiftedPosition = currentGravity.cpy().add(newPosition);
        if (!checkStaticRectAtPosition(rectMan, shiftedPosition)) return;
        currentGravity.shiftR();
        shiftedPosition = currentGravity.cpy().add(newPosition);
        if (!checkStaticRectAtPosition(rectMan, shiftedPosition)) return;
        currentGravity.scl(-1);
        shiftedPosition = currentGravity.cpy().add(newPosition);
        if (!checkStaticRectAtPosition(rectMan, shiftedPosition)) return;
        currentGravity.shiftL();
        shiftedPosition = currentGravity.cpy().add(newPosition);
        if (!checkStaticRectAtPosition(rectMan, shiftedPosition)) return;

        log.warn("New Player position not find, rect.newPosition: " + newPosition + ".");
    }

    private boolean checkStaticRectAtPosition(RectManager rectMan, Vector3f pos) {
        Vector3 subY = GravityControls.swap(new Vector3(0, 1, 0)).scl(GravityControls.getGravityScl());
        return rectMan.checkStaticPosition((int) (pos.x + subY.x), (int) (pos.y + subY.y), (int) (pos.z + subY.z));
    }


    private void clampNewPosition(Vector3 newPosition) {
        Vector3i worldSize = chunkMan.getWorldSize();
        float clampX = MathUtils.clamp(newPosition.x, -player.rect.getWidth() * 2f, worldSize.x + player.rect.getWidth());
        float clampY = MathUtils.clamp(newPosition.y, -player.rect.getHeight() * 2f, worldSize.y + player.rect.getHeight());
        float clampZ = MathUtils.clamp(newPosition.z, -player.rect.getDepth() * 2f, worldSize.z + player.rect.getDepth());
        if (newPosition.x != clampX || newPosition.y != clampY || newPosition.z != clampZ) {
            newPosition.set(clampX, clampY, clampZ);
            controls.isOnGround = true;
            verticalVelocity = 0f;
        }
    }
}
