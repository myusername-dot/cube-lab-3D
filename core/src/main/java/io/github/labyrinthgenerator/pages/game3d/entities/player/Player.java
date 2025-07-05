package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;

@Slf4j
public class Player extends Entity {
    private final Vector2 movementDir = new Vector2();

    public final RectanglePlus rect;
    public final PerspectiveCamera playerCam;

    private final float cameraRotationSpeed = 25f;
    boolean headbob = false;
    boolean verticalCameraMovement = false;
    float camY = 0f;

    private final float playerMoveSpeed = 4f;
    private final float acceleration = 10f;
    private final float deceleration = 10f;

    private final float jumpStrength = 5.0f;
    private boolean isOnGround = true;

    private float velocityY = 0f;
    private final Vector2 horizontalVelocity = new Vector2();
    private final Vector2 horizontalForwardVelocity = new Vector2();

    private Vector3f gravityDirection = new Vector3f(0, 1, 0);

    private boolean cheats = false;

    private int currentHP = 100;
    public boolean isDead = false;
    public boolean gotHit = false;

    public int currentInventorySlot = 1;

    public Player(Vector3 position, float rectWidth, float rectHeight, float rectDepth, final GameScreen screen) {
        super(position, screen);
        playerCam = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        playerCam.position.set(new Vector3(0, camY, 0));
        playerCam.lookAt(new Vector3(0, camY, -HALF_UNIT * 2));
        playerCam.near = 0.01f;
        playerCam.far = 10f;
        playerCam.update();

        rect = new RectanglePlus(
            position.x - rectWidth / 2f,
            position.y + rectHeight / 2f, // FIXME world position bug h > 0
            position.z - rectDepth / 2f,
            rectWidth, rectHeight, rectDepth,
            id, RectanglePlusFilter.PLAYER,
            screen.game.getRectMan()
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition());

        setCamPosition();
    }

    private void setCamPosition() {
        playerCam.position.set(getPositionImmutable()
            .add(adjustVecForGravity(gravityDirection, new Vector3(0f, camY, 0f), true)));
    }

    public float getGravityScl() {
        return gravityDirection.x + gravityDirection.y + gravityDirection.z;
    }

    public void setGravityDirection(Vector3f newGravityDirection) {
        this.gravityDirection = newGravityDirection;//.nor(); // Нормализуем вектор
        updateCameraRotation();
    }

    private void updateCameraRotation() {
        // Поворачиваем камеру так, чтобы пол был под ногами
        playerCam.up.set(new Vector3(gravityDirection.x, gravityDirection.y, gravityDirection.z));
        //playerCam.direction.set(gravityDirection); // Направление камеры
        playerCam.update();
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

        if (screen.game.getGameInput().scrolledYDown) {
            currentInventorySlot++;
            currentInventorySlot = currentInventorySlot > 6 ? 1 : currentInventorySlot;
        } else if (screen.game.getGameInput().scrolledYUp) {
            currentInventorySlot--;
            currentInventorySlot = currentInventorySlot < 1 ? 6 : currentInventorySlot;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            setGravityDirection(gravityDirection.cpy().scl(-1));
            isOnGround = false;
        }

        playerCam.rotate(adjustVecForGravity(gravityDirection, Vector3.Y, false),
            Gdx.input.getDeltaX() * -cameraRotationSpeed * getGravityScl() * delta);

        if (verticalCameraMovement) {
            playerCam.rotate(/*adjustVecForGravity(*/new Vector3(playerCam.direction.z, 0f, -playerCam.direction.x)/*, false)*/,
                Gdx.input.getDeltaY() * -cameraRotationSpeed * delta);
        }

        if (!isOnGround) {
            velocityY -= 9.81f * delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
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

        if (!horizontalMovement) horizontalVelocity.set(horizontalForwardVelocity);
        else if (horizontalVelocity.len() > playerMoveSpeed) {
            horizontalVelocity.nor().scl(playerMoveSpeed);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            currentInventorySlot = 1;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            currentInventorySlot = 2;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            currentInventorySlot = 3;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            currentInventorySlot = 4;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            currentInventorySlot = 5;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            currentInventorySlot = 6;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            verticalCameraMovement = !verticalCameraMovement;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            cheats = !cheats;
        }

        if (headbob) {
            camY = HALF_UNIT;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * playerMoveSpeed * 4f)
                * 0.01875f);
            camY += sinOffset;

            headbob = false;
        }

        Vector3 velocity = adjustVecForGravity(gravityDirection, new Vector3(horizontalVelocity.x, velocityY, horizontalVelocity.y), true);

        Vector3 newPosition = new Vector3(
            rect.getX() + velocity.x * delta,
            rect.getY() - velocity.y * delta,
            rect.getZ() + velocity.z * delta
        );

        if (newPosition.y > rect.getHeight() / 2f) { // fixme
            newPosition.y = rect.getHeight() / 2f;
            isOnGround = true;
            velocityY = 0f;
        }

        rect.newPosition.set(newPosition.x, newPosition.y, newPosition.z);
    }

    public static Vector3 adjustVecForGravity(Vector3f gravityDirection, Vector3 in, boolean invertY) {
        int yScl = invertY ? 1 : -1;
        Vector3 out;
        if (gravityDirection.equals(new Vector3(0, 1, 0))) {
            // Гравитация направлена вниз
            out = new Vector3(in.x, in.y, in.z);
        } else if (gravityDirection.equals(new Vector3(0, -1, 0))) {
            // Гравитация направлена вверх
            out = new Vector3(in.x, -1 * yScl * in.y, in.z);
        } else if (gravityDirection.equals(new Vector3(1, 0, 0))) {
            // Гравитация направлена вправо
            out = new Vector3(-1 * in.y, -1 * in.x, in.z);
        } else if (gravityDirection.equals(new Vector3(-1, 0, 0))) {
            // Гравитация направлена влево
            out = new Vector3(in.z, -1 * in.x, -1 * in.y);
        } else if (gravityDirection.equals(new Vector3(0, 0, 1))) {
            // Гравитация направлена вперед
            out = new Vector3(in.x, -1 * in.z, -1 * in.y);
        } else if (gravityDirection.equals(new Vector3(0, 0, -1))) {
            // Гравитация направлена назад
            out = new Vector3(-1 * in.y, -1 * in.z, in.x);
        } else {
            out = new Vector3(0, 0, 0);
        }
        return out;
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
    public void tick(final float delta) {

        if (!cheats) {
            screen.checkOverlaps(rect, delta);
        } else {
            rect.setX(rect.newPosition.x);
            rect.setY(rect.newPosition.y);
            rect.setZ(rect.newPosition.z);
        }

        setPosition(rect.getX() + rect.getWidth() / 2f, rect.getY() - rect.getHeight() / 2f, rect.getZ() + rect.getDepth() / 2f);
        setCamPosition();

        rect.oldPosition.set(rect.getPosition());
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

	/*private void useUsableInterface(final IUsable usableInterface) {
		if (currentUsableInterface != null) {
			usableInterface.onUse();
		}
	}*/
}
