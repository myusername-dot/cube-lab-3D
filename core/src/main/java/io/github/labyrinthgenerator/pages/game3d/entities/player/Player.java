package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.entities.IUsable;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.Set;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;


public class Player extends Entity {
    private final Vector3 movementDir = new Vector3();

    final Vector2 movementDirVec2 = new Vector2(movementDir.x, movementDir.z);

    private final float cameraRotationSpeed = 25f;

    private final float playerMoveSpeed = 4f;
    private final int maxHP = 100;
    private int currentHP = 100;

    public boolean gotHit = false;

    public boolean renderBloodOverlay = false;
    private final float bloodOverlayAlphaMax = 1f;
    private final float bloodOverlayAlphaMin = 0f;
    private final float bloodOverlayAlphaSpeed = 5f;
    public float bloodOverlayAlpha = bloodOverlayAlphaMin;

    public final PerspectiveCamera playerCam;

    public RectanglePlus rect;

    public boolean isDead = false;

    float camY = HALF_UNIT;

    public int currentInventorySlot = 1;

    boolean headbob = false;
    boolean verticalCameraMovement = false;

    float updateEnemiesRangeTimeSec = 0f;
    float updateEnemiesRangeTimer = updateEnemiesRangeTimeSec;

    public Player(final GameScreen screen) {
        super(screen);

        playerCam = new PerspectiveCamera(70, 640, 480);
        playerCam.position.set(new Vector3(0, HALF_UNIT, 0));
        playerCam.lookAt(new Vector3(0, camY, HALF_UNIT * 2));
        playerCam.near = 0.01f;
        playerCam.far = 10f;
        playerCam.update();

        float rectWidth = (HALF_UNIT / 2f);
        float rectDepth = (HALF_UNIT / 2f);
        rect = new RectanglePlus(
            screen.game.getMapBuilder().mapLoadSpawnPosition.x + HALF_UNIT - rectWidth / 2f,
            0f,
            screen.game.getMapBuilder().mapLoadSpawnPosition.y + HALF_UNIT - rectDepth / 2f,
            rectWidth, HALF_UNIT, rectDepth,
            id, RectanglePlusFilter.PLAYER
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition()); // Needed for spawning at correct position.
        screen.game.getRectMan().addRect(rect); // never forget!

        setCamPosition();
    }

    private void setCamPosition() {
        playerCam.position.set(rect.getX() + rect.getWidth() / 2f, camY, rect.getZ() + rect.getDepth() / 2f);
    }

    public void addHP(final int addHP) {
        this.currentHP += addHP;

        if (currentHP > maxHP) {
            currentHP = maxHP;
        }

//		System.out.println("Current HP: " + currentHP);
    }

    @Override
    public void destroy() {
        if (destroy) {
            screen.game.getRectMan().removeRect(rect);
        }

        super.destroy(); // should be last.
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public final void setEnemyInRangeAroundCam(float delta) {
        updateEnemiesRangeTimer += delta;
        if (updateEnemiesRangeTimer >= updateEnemiesRangeTimeSec) {
            screen.setEnemyInRangeAroundCam();
            updateEnemiesRangeTimer = 0f;
        }
    }

    public final void collectItems() {
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
    }

    public float getExitDistance() {
        float playerX = rect.getX();
        float playerZ = rect.getZ();
        float exitX = screen.game.getMapBuilder().mapLoadExitPosition.x;
        float exitZ = screen.game.getMapBuilder().mapLoadExitPosition.y;
        float dx = exitX - playerX;
        float dz = exitZ - playerZ;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    public void handleInput(final float delta) {
        movementDir.setZero();

		/*if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			shoot();
		}*/

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

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            movementDir.add(playerCam.direction.cpy());
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            movementDir.sub(playerCam.direction.cpy());
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            movementDir.sub(playerCam.direction.cpy().crs(playerCam.up));
            headbob = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            movementDir.add(playerCam.direction.cpy().crs(playerCam.up));
            headbob = true;
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

        if (headbob) {
            camY = HALF_UNIT;
            final float sinOffset = (float) (Math.sin(screen.game.getTimeSinceLaunch() * playerMoveSpeed * 4f)
                * 0.01875f);
            camY += sinOffset;

			/*gunY = gunYStart;
			gunY += sinOffset * 200f;*/

            headbob = false;
        }

        Vector2 newPositionXZ = rect.rectangle.getPosition(
            new Vector2()).cpy().add(movementDirVec2.nor().cpy().scl(playerMoveSpeed * delta)
        );
        movementDirVec2.set(movementDir.x, movementDir.z);
        rect.newPosition.set(newPositionXZ.x, rect.getY(), newPositionXZ.y);
    }

    @Override
    public void onCollision(final RectanglePlus otherRect) {
        super.onCollision(otherRect);

        if (collidedEntity instanceof Firefly) {
            ((Firefly) collidedEntity).switchTexture();
        }

		/*if (otherRect.filter == RectanglePlusFilter.ITEM) {
			((PlayScreen) screen).playItemSound();
		}*/
    }

	/*private void shoot() {
		if (!shootTimerSet) {
			shootTimerEnd = System.currentTimeMillis() + shootTimerCD;

			if (!shootAnimationTimerSet) {
				shootAnimationTimerEnd = System.currentTimeMillis() + shootAnimationTimerCD;
				guiCurrentGun = guiGunShoot;
				shootAnimationTimerSet = true;
			}

			getEnemyRectInRangeFromCam();
//			System.out.println("shot");

			shootTimerSet = true;
		}
	}*/

    public void subHP(final int subHP) {
        this.currentHP -= subHP;

        if (currentHP < 0) {
            currentHP = 0;
        }

        gotHit = true;

//		System.out.println("Current HP: " + currentHP);
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void tick(final float delta) {
//		if (gotHit) {
//			if (!gotHitAnimationTimerSet) {
//				gotHitAnimationTimerEnd = System.currentTimeMillis() + gotHitAnimationTimerCD;
//				bloodOverlayAlpha = 0;
//				renderBloodOverlay = true;
//				gotHitAnimationTimerSet = true;
//			}
//
//			gotHit = false;
//		}

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
//			System.out.println("Player is dead.");
        }

		/*if (shootTimerSet) {
			if (System.currentTimeMillis() >= shootTimerEnd) {
				shootAnimationTimerSet = false;
				shootTimerSet = false;
			}
		}

		if (shootAnimationTimerSet) {
			if (System.currentTimeMillis() >= shootAnimationTimerEnd) {
				shootAnimationTimerSet = false;
			}
		}

		if (!shootAnimationTimerSet) {
			guiCurrentGun = guiGun;
		}*/

        screen.checkOverlaps(rect, delta);

        setCamPosition();

        setEnemyInRangeAroundCam(delta);
        collectItems();

        rect.oldPosition.set(rect.getPosition());
    }

	/*private void useUsableInterface(final IUsable usableInterface) {
		if (currentUsableInterface != null) {
			usableInterface.onUse();
		}
	}*/
}
