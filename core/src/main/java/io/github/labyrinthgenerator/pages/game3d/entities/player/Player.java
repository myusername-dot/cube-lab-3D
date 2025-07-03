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
    float camY = HALF_UNIT;

    private final float playerMoveSpeed = 4f;
    private final float acceleration = 10f;
    private final float deceleration = 10f;

    private final float jumpStrength = 5.0f;
    private boolean isOnGround = true;

    private float velocityY = 0f;
    private final Vector2 velocity = new Vector2();
    private final Vector2 forwardVelocity = new Vector2();

    private boolean cheats = false;

    private final int maxHP = 100;
    private int currentHP = 100;
    public boolean isDead = false;
    public boolean gotHit = false;

    public int currentInventorySlot = 1;

    public boolean renderBloodOverlay = false;
    private final float bloodOverlayAlphaMax = 1f;
    private final float bloodOverlayAlphaMin = 0f;
    private final float bloodOverlayAlphaSpeed = 5f;
    public float bloodOverlayAlpha = bloodOverlayAlphaMin;

    public Player(Vector3 position, float rectWidth, float rectDepth, final GameScreen screen) {
        super(position.cpy().set(
                position.x + rectWidth / 2f,
                position.y,
                position.z + rectDepth / 2f),
            screen);
        playerCam = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        playerCam.position.set(new Vector3(0, HALF_UNIT, 0));
        playerCam.lookAt(new Vector3(0, camY, HALF_UNIT * 2));
        playerCam.near = 0.01f;
        playerCam.far = 10f;
        playerCam.update();

        rect = new RectanglePlus(
            position.x,
            position.y,
            position.z,
            rectWidth, HALF_UNIT, rectDepth,
            id, RectanglePlusFilter.PLAYER,
            screen.game.getRectMan()
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition());

        setCamPosition();
    }

    private void setCamPosition() {
        playerCam.position.set(getPositionX(), getPositionY() + camY, getPositionZ());
    }

    public void addHP(final int addHP) {
        this.currentHP += addHP;

        if (currentHP > maxHP) {
            currentHP = maxHP;
        }

//		log.info("Current HP: " + currentHP);
    }

    public int getCurrentHP() {
        return currentHP;
    }

    /*public final void collectItems() {
        final float rangeDistanceFromCam = 2f;

        float rectX = rect.getX() + rect.getWidth() / 2f;
        float rectZ = rect.getZ() + rect.getDepth() / 2f;
        for (final RectanglePlus rect : screen.game.getRectMan().getRectsByFilter(RectanglePlusFilter.ITEM)) {
            if (Intersector.intersectSegmentRectangle(playerCam.position.x, playerCam.position.z,
                playerCam.position.x + playerCam.direction.x * rangeDistanceFromCam,
                playerCam.position.z + playerCam.direction.z * rangeDistanceFromCam, rect.rectangle)) {

                float rectInRangeDistance = Vector2.dst2(playerCam.position.x, playerCam.position.z,
                    rectX, rectZ);

                if (rectInRangeDistance < rangeDistanceFromCam) {
                    if (screen.game.getEntMan()
                        .getEntityFromId(rect.getConnectedEntityId()) instanceof IUsable) {
                        IUsable currentUsableInterface = (IUsable) screen.game.getEntMan()
                            .getEntityFromId(rect.getConnectedEntityId());
                        currentUsableInterface.onUse();
                    }
                }
            }
        }
    }*/

    public float getExitDistance() {
        float playerX = rect.getX();
        float playerZ = rect.getZ();
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

        playerCam.rotate(Vector3.Y, Gdx.input.getDeltaX() * -cameraRotationSpeed * delta);

        if (verticalCameraMovement) {
            playerCam.rotate(new Vector3(playerCam.direction.z, 0f, -playerCam.direction.x),
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
            velocity.add(movementDir.cpy().scl(acceleration * delta)); // Увеличиваем скорость
        } else {
            // Если игрок не движется, замедляем скорость
            velocity.scl(1 - deceleration * delta);
        }

        // Ограничиваем скорость только в направлении камеры, чтобы игрока не заносило на поворотах
        Vector3 cameraForward = playerCam.direction.cpy().nor();
        float forwardVelocityScl = velocity.cpy().dot(cameraForward.x, cameraForward.z); // Получаем скорость в направлении камеры
        cameraForward.scl(forwardVelocityScl);
        forwardVelocity.set(cameraForward.x, cameraForward.z); // Устанавливаем скорость только в направлении

        // Ограничиваем скорость
        if (forwardVelocity.len() > playerMoveSpeed) {
            // получаем единичный скаляр по отношению к длине и умножаем на макс скорость
            forwardVelocity.nor().scl(playerMoveSpeed);
        }

        if (!horizontalMovement) velocity.set(forwardVelocity);
        else if (velocity.len() > playerMoveSpeed) {
            velocity.nor().scl(playerMoveSpeed);
        }

		/*if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			useUsableInterface(currentUsableInterface);
		}*/

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

        Vector3 newPosition = new Vector3(
            rect.getX() + velocity.x * delta,
            rect.getY() - velocityY * delta,
            rect.getZ() + velocity.y * delta
        );

        if (newPosition.y > 0f) {
            newPosition.y = 0f;
            isOnGround = true;
            velocityY = 0f;
        }

        rect.newPosition.set(newPosition.x, newPosition.y, newPosition.z);
    }

    public Vector2 getMovementDir() {
        return movementDir;
    }

    public Vector3 getDirection() {
        return playerCam.direction.cpy();
    }

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public Vector2 getForwardVelocity() {
        return forwardVelocity.cpy();
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

    public void subHP(final int subHP) {
        this.currentHP -= subHP;

        if (currentHP < 0) {
            currentHP = 0;
        }

        gotHit = true;
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void tick(final float delta) {
        if (gotHit) {
            renderBloodOverlay = true;
            bloodOverlayAlpha = bloodOverlayAlphaMax;
            gotHit = false;
        }

        if (renderBloodOverlay) {
            bloodOverlayAlpha -= delta * bloodOverlayAlphaSpeed;

            if (bloodOverlayAlpha <= bloodOverlayAlphaMin) {
                renderBloodOverlay = false;
            }
        }

        if (currentHP == 0) {
            isDead = true;
//			log.info("Player is dead.");
        }

        if (!cheats) {
            screen.checkOverlaps(rect, delta);
        } else {
            rect.setX(rect.newPosition.x);
            rect.setY(rect.newPosition.y);
            rect.setZ(rect.newPosition.z);
        }

        setPosition(rect.getX() + rect.getWidth() / 2f, rect.getY(), rect.getZ() + rect.getDepth() / 2f);
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
