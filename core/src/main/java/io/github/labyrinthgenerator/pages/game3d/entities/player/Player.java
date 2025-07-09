package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControl;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControl.gravity;

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

    private GravityDir gravityDir = GravityDir.DOWN;

    private int currentHP = 100;
    public boolean isDead = false;
    public boolean gotHit = false;

    public int currentInventorySlot = 1;

    public Player(Vector3 position, float rectWidth, float rectHeight, float rectDepth, final GameScreen screen) {
        super(position, screen);
        this.debugger = screen.game.getDebugger();

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
        playerCam.position.set(getPositionImmutable()
            .add(GravityControl.adjustVecForGravity(gravityDir, new Vector3(0f, camY, 0f))));

        debugCam.position.set(playerCam.position.x, -playerCam.position.y, playerCam.position.z);
    }

    private void rotateCamHorizontal(float delta) {
        float angle = Gdx.input.getDeltaX() * -cameraRotationSpeed
            * gravity[gravityDir.ord].sum() * delta;
        playerCam.rotate(Vector3.Y, angle);

        debugCam.rotate(Vector3.Y, angle);
    }

    private void rotateCamVertical(float delta) {
        float angle = Gdx.input.getDeltaY() * -cameraRotationSpeed * gravity[gravityDir.ord].sum() * delta;

        float newVerticalAngle = currentVerticalAngle + angle;

        if (newVerticalAngle > MAX_VERTICAL_ANGLE) {
            newVerticalAngle = MAX_VERTICAL_ANGLE;
        } else if (newVerticalAngle < -MAX_VERTICAL_ANGLE) {
            newVerticalAngle = -MAX_VERTICAL_ANGLE;
        }

        // Вычисляем угол, который необходимо применить
        angle = newVerticalAngle - currentVerticalAngle;
        currentVerticalAngle = newVerticalAngle;

        Vector3 axis = new Vector3(playerCam.direction.z, 0f, -playerCam.direction.x);
        playerCam.rotate(axis, angle);
        debugCam.rotate(axis, -angle);
    }

    public void setGravityDirection(GravityDir gravityDir) {
        this.gravityDir = gravityDir;//.nor(); // Нормализуем вектор
        updateCameraRotation();
    }

    private void updateCameraRotation() {
        // Поворачиваем камеру так, чтобы пол был под ногами
        playerCam.up.set(
            gravity[gravityDir.ord].x,
            gravity[gravityDir.ord].y,
            gravity[gravityDir.ord].z
        );
        //playerCam.direction.set(gravityDirection); // Направление камеры
        playerCam.update();

        debugCam.up.set(
            gravity[gravityDir.ord].x,
            gravity[gravityDir.ord].y,
            gravity[gravityDir.ord].z
        );
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            debugger.debugMode = MyDebugRenderer.DebugMode.values()[
                (debugger.debugMode.ordinal() + 1) % MyDebugRenderer.DebugMode.values().length];
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            jumping = !jumping;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            setGravityDirection(gravityDir == GravityDir.DOWN ? GravityDir.UP : GravityDir.DOWN);
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
            velocityY = Math.max(velocityY, -9.81f);
        }

        if (jumping && Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocityY = jumpStrength;
            isOnGround = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movementDir.add(playerCam.direction.x, playerCam.direction.z);
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movementDir.sub(playerCam.direction.x, playerCam.direction.z);
            headbob = true;
        }

        boolean horizontalMovement = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            Vector3 rightDir = playerCam.direction.cpy().crs(playerCam.up);
            movementDir.sub(rightDir.x, rightDir.z);
            horizontalMovement = true;
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            Vector3 rightDir = playerCam.direction.cpy().crs(playerCam.up);
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
        Vector3 cameraForward = playerCam.direction.cpy().nor();
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
            camY = HALF_UNIT;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * playerMoveSpeed * 4f)
                * 0.01875f);
            camY += sinOffset;

            headbob = false;
        }

        Vector3 velocity = GravityControl.adjustVecForGravity(
            gravityDir,
            new Vector3(horizontalVelocity.x, velocityY, horizontalVelocity.y)
        );

        Vector3 newPosition = new Vector3(
            rect.getX() + velocity.x * delta,
            rect.getY() - velocity.y * delta,
            rect.getZ() + velocity.z * delta
        );

        Vector3i worldSize = screen.game.getChunkMan().getWorldSize();
        float clampY = MathUtils.clamp(newPosition.y, worldSize.y + rect.getHeight() / 2f, rect.getHeight() / 2f);
        if (newPosition.y != clampY) {
            newPosition.y = clampY;
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
