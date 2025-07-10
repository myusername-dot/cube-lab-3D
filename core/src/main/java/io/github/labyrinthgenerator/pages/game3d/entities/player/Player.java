package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.currentGravity;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.gravity;

@Slf4j
public class Player extends Entity {
    private final Vector2 movementDir = new Vector2();

    public final RectanglePlus rect;

    public final PerspectiveCamera playerCam;
    private final PerspectiveCamera debugCam;

    private final MyDebugRenderer debugger;

    private final float cameraRotationSpeed = 25f;
    private float camY = HALF_UNIT; // camera y bug, should be -HALF_UNIT
    private boolean headbob = false;

    private boolean verticalCameraMovement = false;
    private boolean jumping = false;
    private boolean cheats = false;

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

    private int currentHP = 100;
    public boolean isDead = false;
    public boolean gotHit = false;

    private int test = 0;

    public int currentInventorySlot = 1;

    public Player(Vector3 position, float rectWidth, float rectHeight, float rectDepth, final GameScreen screen) {
        super(position, screen);
        this.debugger = screen.game.getDebugger();

        currentGravity = GravityDir.DOWN;

        Vector3 lookAt = new Vector3(0, 0, -1);
        playerCam = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        GameScreen.setupCamera(playerCam, getPositionImmutable(), lookAt);
        screen.setCurrentCam(playerCam);

        debugCam = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        GameScreen.setupCamera(debugCam, getPositionImmutable().scl(1, -1, 1), lookAt.scl(-1));
        screen.setDebugCam(debugCam);

        rect = new RectanglePlus(
            position.x - rectWidth / 2f,
            position.y + rectHeight / 2f,
            position.z - rectDepth / 2f,
            rectWidth, rectHeight, rectDepth,
            id, RectanglePlusFilter.PLAYER,
            screen.game.getRectMan()
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition());
    }

    private void setCamPosition() {
        playerCam.position.set(getPositionImmutable());
        //.add(GravityControls.adjustVecForGravity(new Vector3(0f, camY, 0f))));

        debugCam.position.set(playerCam.position.x, -playerCam.position.y, playerCam.position.z);
    }

    private void rotateCamHorizontal(float delta) {
        // Gravity dir -1 or 1
        float scl = GravityControls.getYScl(false);
        float angle = Gdx.input.getDeltaX() * -cameraRotationSpeed * scl * delta;
        Vector3 axis = Vector3.Y;
        // Goto local gravity coords
        axis = GravityControls.swap(axis, false, false);
        // Rotate local dir angle
        playerCam.rotate(axis, angle);
        debugCam.rotate(axis, angle);
    }

    private void rotateCamVertical(float delta) {
        // Gravity dir -1 or 1
        float scl = GravityControls.getYScl(true);
        float angle = Gdx.input.getDeltaY() * -cameraRotationSpeed * scl * delta;

        float newVerticalAngle = currentVerticalAngle + angle;

        if (newVerticalAngle > MAX_VERTICAL_ANGLE) {
            newVerticalAngle = MAX_VERTICAL_ANGLE;
        } else if (newVerticalAngle < -MAX_VERTICAL_ANGLE) {
            newVerticalAngle = -MAX_VERTICAL_ANGLE;
        }

        // Вычисляем угол, который необходимо применить
        angle = newVerticalAngle - currentVerticalAngle;
        currentVerticalAngle = newVerticalAngle;

        Vector3 axis = playerCam.direction.cpy();
        // Vertical movement vec scl. Rotate x, z coords
        Vector3 axScl = new Vector3(-1, 0, 1);
        // Goto local gravity coords
        axScl = GravityControls.swap(axScl, false, false);
        // Swap left and right. Local y after scaling is 0
        axis = swapNot0(axis.scl(axScl));
        // Rotate local dir angle
        playerCam.rotate(axis, angle);
        debugCam.rotate(axis, -angle);
    }

    private Vector3 swapNot0(Vector3 in) {
        float x = in.x, y = in.y, z = in.z;
        if (x == 0) in.set(x, z, y);
        if (y == 0) in.set(z, y, x);
        if (z == 0) in.set(y, x, z);
        return in;
    }

    private void reSwapCameraDirection() {
        playerCam.direction.set(GravityControls.reSwap(playerCam.direction, true, true));
    }

    private void updateCameraRotation() {
        currentVerticalAngle = 0f;
        // Поворачиваем камеру так, чтобы пол был под ногами
        playerCam.up.set(gravity[currentGravity.ord].vec3());
        playerCam.direction.set(GravityControls.swap(playerCam.direction, true, true));
        playerCam.update();

        debugCam.up.set(gravity[currentGravity.ord].vec3());
        debugCam.direction.set(GravityControls.swap(debugCam.direction, true, true));
        debugCam.update();
    }

    public float getExitDistance() {
        float playerX = getPositionX();
        float playerZ = getPositionZ();
        float exitX = screen.getExitPosition().x;
        float exitZ = screen.getExitPosition().z;
        float dx = exitX - playerX;
        float dz = exitZ - playerZ;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    public void handleInput(final float delta) {
        movementDir.setZero();

        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            playerCam.up.set(gravity[test++ % gravity.length].vec3());
            playerCam.update();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            debugger.debugMode = MyDebugRenderer.DebugMode.values()[
                (debugger.debugMode.ordinal() + 1) % MyDebugRenderer.DebugMode.values().length];
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            jumping = !jumping;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            reSwapCameraDirection();
            GravityControls.swapYDir();
            updateCameraRotation();
            isOnGround = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            reSwapCameraDirection();
            GravityControls.swapXDir();
            updateCameraRotation();
            isOnGround = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            verticalCameraMovement = !verticalCameraMovement;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            cheats = !cheats;
        }

        if (screen.game.getGameInput().scrolledYDown) {
            currentInventorySlot++;
            currentInventorySlot = currentInventorySlot > 6 ? 1 : currentInventorySlot;
        } else if (screen.game.getGameInput().scrolledYUp) {
            currentInventorySlot--;
            currentInventorySlot = currentInventorySlot < 1 ? 6 : currentInventorySlot;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) currentInventorySlot = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) currentInventorySlot = 2;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) currentInventorySlot = 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) currentInventorySlot = 4;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) currentInventorySlot = 5;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) currentInventorySlot = 6;

        rotateCamHorizontal(delta);

        if (verticalCameraMovement) {
            rotateCamVertical(delta);
        }

        if (!isOnGround) {
            velocityY -= 9.81f * delta;
            velocityY = MathUtils.clamp(velocityY, -9.81f, 9.81f);
        }

        if (jumping && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocityY = jumpStrength;
            isOnGround = false;
        }

        // local gravity x z, inv -y
        Vector3 localCamDir = GravityControls.swap(playerCam.direction, false, true);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movementDir.add(localCamDir.x, localCamDir.z);
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movementDir.sub(localCamDir.x, localCamDir.z);
            headbob = true;
        }

        boolean horizontalMovement = false;
        Vector3 rightDir = GravityControls.swap(playerCam.direction.cpy().crs(playerCam.up), false, true);

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movementDir.sub(rightDir.x, rightDir.z);
            horizontalMovement = true;
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movementDir.add(rightDir.x, rightDir.z);
            horizontalMovement = true;
            headbob = true;
        }

        // Нормализуем направление движения и применяем ускорение
        if (movementDir.len() > 0) {
            movementDir.nor(); // Нормализуем
            horizontalVelocity.add(movementDir.cpy().scl(acceleration * delta)); // Увеличиваем скорость
        } else {
            // Если игрок не движется, замедляем скорость
            horizontalVelocity.scl(1 - deceleration * delta);
        }

        // Ограничиваем скорость только в направлении камеры, чтобы игрока не заносило на поворотах
        Vector3 cameraForward = localCamDir.cpy().nor();//playerCam.direction.cpy().nor();
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

        if (headbob) {
            //camY = HALF_UNIT;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * playerMoveSpeed * 4f)
                * 0.01875f);
            //camY += sinOffset;

            headbob = false;
        }

        Vector3 velocity = GravityControls.reSwap(
            new Vector3(horizontalVelocity.x, velocityY * GravityControls.getYScl(true), horizontalVelocity.y),
            false, true);

        Vector3 newPosition = new Vector3(
            rect.getX() + velocity.x * delta,
            rect.getY() + velocity.y * delta,
            rect.getZ() + velocity.z * delta
        );

        Vector3i worldSize = screen.game.getChunkMan().getWorldSize();
        float clampX = MathUtils.clamp(newPosition.x, -rect.getWidth() / 2f, worldSize.x - rect.getWidth() / 2f);
        float clampY = MathUtils.clamp(newPosition.y, worldSize.y + rect.getHeight() / 2f, rect.getHeight() / 2f);
        float clampZ = MathUtils.clamp(newPosition.z, -rect.getDepth() / 2f, worldSize.z - rect.getDepth() / 2f);
        if (newPosition.x != clampX || newPosition.y != clampY || newPosition.z != clampZ) {
            newPosition.set(clampX, clampY, clampZ);
            isOnGround = true;
            velocityY = 0f;
        }

        rect.newPosition.set(newPosition.x, newPosition.y, newPosition.z);
    }

    @Override
    public void tick(final float delta) {

        if (!cheats) {
            screen.checkOverlaps(rect);
        } else {
            rect.setX(rect.newPosition.x);
            rect.setY(rect.newPosition.y);
            rect.setZ(rect.newPosition.z);
        }

        setPosition(
            rect.getX() + rect.getWidth() / 2f,
            rect.getY() - rect.getHeight() / 2f,
            rect.getZ() + rect.getDepth() / 2f
        );
        setCamPosition();

        rect.oldPosition.set(rect.getPosition());
    }

    @Override
    public void onCollision(final RectanglePlus otherRect) {
        super.onCollision(otherRect);

        if (collidedEntity instanceof Firefly) {
            ((Firefly) collidedEntity).switchTexture(Firefly.Color.GREEN);
        }

		/*if (otherRect.filter == RectanglePlusFilter.ITEM) {
			((PlayScreen) screen).playItemSound();
		}*/
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void beforeTick() {
        log.debug("Start tick player thread id: " + Thread.currentThread().getId() + ".");
        super.beforeTick();
    }

    @Override
    public void afterTick() {
        super.afterTick();
        log.debug("End tick player thread id: " + Thread.currentThread().getId() + ".");
    }

    public Vector2 getMovementDir() {
        return movementDir;
    }

    public Vector3 getDirection() {
        return playerCam.direction.cpy();
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

	/*private void useUsableInterface(final IUsable usableInterface) {
		if (currentUsableInterface != null) {
			usableInterface.onUse();
		}
	}*/
}
