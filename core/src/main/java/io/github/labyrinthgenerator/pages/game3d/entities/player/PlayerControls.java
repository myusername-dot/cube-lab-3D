package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;
import io.github.labyrinthgenerator.pages.game3d.managers.RectManager;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.currentGravity;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.gravity;

@Slf4j
public class PlayerControls {
    private final Vector2 horizontalMovementDir = new Vector2();
    private final RectManager rectMan;

    private final float cameraRotationSpeed = 25f;
    private float camY = 0;
    private boolean headbob = false;

    private boolean verticalCameraMovement = false;
    private boolean jumping = false;
    private boolean cheats = false;

    private final Vector3 currentHorizontalAxis = Vector3.Y.cpy();
    private final Vector3 currentVerticalAxis = new Vector3();

    private float currentHorizontalAngle = 0f;
    private float currentVerticalAngle = 0f;
    private static final float MAX_VERTICAL_ANGLE = 80f;

    private final float playerMoveSpeed = 4f;
    private final float acceleration = 10f;
    private final float deceleration = 10f;
    private final float jumpStrength = 8.0f;
    private boolean isOnGround = true;

    private float velocityY = 0f;
    private final Vector2 horizontalVelocity = new Vector2();
    private final Vector2 horizontalForwardVelocity = new Vector2();

    public int currentInventorySlot = 1;

    private final GameScreen screen;
    private final Player player;

    public PlayerControls(GameScreen screen, Player player) {
        this.screen = screen;
        this.player = player;
        this.rectMan = screen.game.getRectMan();
    }

    private void setCamPosition() {
        player.playerCam.position.set(player.getPositionImmutable().add(GravityControls.swap(new Vector3(0f, camY, 0f))));
    }

    private void rotateCam(float delta) {
        rotateCamHorizontal(delta);
        if (verticalCameraMovement) {
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
        player.playerCam.rotate(getCurrentVerticalAxis(), angle);
    }

    private Vector3 getCurrentVerticalAxis() {
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

    private void updateCameraRotation() {
        currentVerticalAngle = 0f;
        currentHorizontalAngle = 0f;
        // Goto local gravity coords
        currentHorizontalAxis.set(GravityControls.swap(Vector3.Y));
        player.playerCam.up.set(gravity[currentGravity.ord].vec3());
        player.playerCam.direction.set(GravityControls.swap(player.playerCam.direction));
        player.playerCam.update();
    }

    public void handleInput(final float delta) {
        resetMovementDir();
        handleDebugInput();
        handleGravityInput();
        handleInventoryInput();
        rotateCam(delta);
        handleHorizontalMovement(delta);
        handleVerticalMovement(delta);
        updateRectNewPosition(delta);
    }

    private void resetMovementDir() {
        horizontalMovementDir.setZero();
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

    private void reSwapCameraDirection() {
        player.playerCam.direction.set(GravityControls.reSwap(player.playerCam.direction));
    }

    private void handleGravityInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            reSwapCameraDirection();
            GravityControls.swapVerticalGravityDir();
            updateCameraRotation();
            isOnGround = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            reSwapCameraDirection();
            GravityControls.randomGravityDir();
            updateCameraRotation();
            isOnGround = false;
        }
    }

    private void handleInventoryInput() {
        if (screen.game.getGameInput().scrolledYDown) {
            currentInventorySlot = (currentInventorySlot % 6) + 1;
        } else if (screen.game.getGameInput().scrolledYUp) {
            currentInventorySlot = (currentInventorySlot - 2 + 6) % 6 + 1;
        }
        for (int i = 1; i <= 6; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + (i - 1))) {
                currentInventorySlot = i;
            }
        }
    }

    private void handleVerticalMovement(float delta) {
        if (!isOnGround) {
            velocityY -= 9.81f * delta;
            velocityY = MathUtils.clamp(velocityY, -9.81f, 9.81f);
        }
        if (jumping && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocityY = jumpStrength;
            isOnGround = false;
        }
        updateHeadbob();
    }

    private void handleHorizontalMovement(float delta) {
        // local gravity x z, inv -y
        Vector3 localCamDir = GravityControls.swap(player.playerCam.direction);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            horizontalMovementDir.add(localCamDir.x, localCamDir.z);
            headbob = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            horizontalMovementDir.sub(localCamDir.x, localCamDir.z);
            headbob = true;
        }
        applyHorizontalMovement(handleStrafeMovement(), delta);
    }

    private boolean handleStrafeMovement() {
        Vector3 rightDir = GravityControls.swap(player.playerCam.direction.cpy().crs(player.playerCam.up));
        boolean horizontalMovement = false;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontalMovementDir.sub(rightDir.x, rightDir.z);
            horizontalMovement = true;
            headbob = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontalMovementDir.add(rightDir.x, rightDir.z);
            horizontalMovement = true;
            headbob = true;
        }
        return horizontalMovement;
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
        Vector3 localCamDir = GravityControls.swap(player.playerCam.direction);

        // Ограничиваем скорость только в направлении камеры, чтобы игрока не заносило на поворотах
        Vector3 cameraForward = localCamDir.cpy().nor();//player.playerCam.direction.cpy().nor();
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

    private void updateHeadbob() {
        if (headbob) {
            camY = 0;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * playerMoveSpeed * 4f)
                * 0.01875f);
            camY += sinOffset;
            headbob = false;
        }
    }

    private void updateRectNewPosition(float delta) {
        Vector3 velocity = GravityControls.reSwap(
            new Vector3(horizontalVelocity.x, velocityY * GravityControls.getGravityScl() * -1, horizontalVelocity.y));
        Vector3 newPosition = player.rect.getPositionImmutable().add(velocity.cpy().scl(delta));
        preventOnWallPosition(newPosition);
        clampNewPosition(newPosition);
        player.rect.newPosition.set(newPosition);
    }

    private void clampNewPosition(Vector3 newPosition) {
        Vector3i worldSize = screen.game.getChunkMan().getWorldSize();
        float clampX = MathUtils.clamp(newPosition.x, -player.rect.getWidth() * 2f, worldSize.x + player.rect.getWidth());
        float clampY = MathUtils.clamp(newPosition.y, -player.rect.getHeight() * 2f, worldSize.y + player.rect.getHeight());
        float clampZ = MathUtils.clamp(newPosition.z, -player.rect.getDepth() * 2f, worldSize.z + player.rect.getDepth());
        if (newPosition.x != clampX || newPosition.y != clampY || newPosition.z != clampZ) {
            newPosition.set(clampX, clampY, clampZ);
            isOnGround = true;
            velocityY = 0f;
        }
    }

    private void preventOnWallPosition(Vector3 newPosition) {
        if (!isOnGround && checkStaticRectAtPosition(rectMan, new Vector3f(newPosition))) {
            Vector3f goToFloor = gravity[currentGravity.ord].cpy();
            findNewPositionShift(newPosition, goToFloor);
            goToFloor.scl(0.5f);
            goToFloor.add(goToFloor.vec3().sub(player.rect.getDims()));
            newPosition.add(goToFloor.vec3());
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

        throw new RuntimeException("New Player position not find, rect.newPosition: " + newPosition + ".");
    }

    private boolean checkStaticRectAtPosition(RectManager rectMan, Vector3f pos) {
        Vector3 subY = GravityControls.swap(new Vector3(0, 1, 0)).scl(GravityControls.getGravityScl());
        return rectMan.checkStaticPosition((int) (pos.x + subY.x), (int) (pos.y + subY.y), (int) (pos.z + subY.z));
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
        setCamPosition();

        player.rect.oldPosition.set(player.rect.getPositionImmutable());
    }

    public Vector2 getHorizontalMovementDir() {
        return horizontalMovementDir;
    }

    public Vector3 getDirection() {
        return player.playerCam.direction.cpy();
    }

    public Vector2 getHorizontalVelocity() {
        return horizontalVelocity.cpy();
    }

    public Vector2 getHorizontalForwardVelocity() {
        return horizontalForwardVelocity.cpy();
    }

    public float getVerticalVelocity() {
        return velocityY;
    }

    public boolean isOnGround() {
        return isOnGround;
    }

    public Vector3 getHorizontalAxis() {
        return currentHorizontalAxis;
    }

    public Vector3 getVerticalAxis() {
        return currentVerticalAxis;
    }

    public float getCurrentHorizontalAngle() {
        return currentHorizontalAngle;
    }

    /*private void useUsableInterface(final IUsable usableInterface) {
		if (currentUsableInterface != null) {
			usableInterface.onUse();
		}
	}*/
}
