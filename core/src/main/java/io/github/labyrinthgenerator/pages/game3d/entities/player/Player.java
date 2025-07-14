package io.github.labyrinthgenerator.pages.game3d.entities.player;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Firefly;
import io.github.labyrinthgenerator.pages.game3d.entities.player.controls.PlayerControls;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import lombok.extern.slf4j.Slf4j;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.WINDOW_HEIGHT;
import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.WINDOW_WIDTH;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.currentGravity;

@Slf4j
public class Player extends Entity {
    public final PlayerControls controls;

    public final RectanglePlus rect;
    public final PerspectiveCamera playerCam;

    public Player(Vector3 position, float rectWidth, float rectHeight, float rectDepth, final GameScreen screen) {
        super(position, screen);
        currentGravity = GravityDir.DOWN;
        playerCam = createCamera(screen, position);
        rect = createRectangle(position, rectWidth, rectHeight, rectDepth, screen);
        controls = new PlayerControls(screen, this);
    }

    private PerspectiveCamera createCamera(GameScreen screen, Vector3 position) {
        Vector3 lookAt = new Vector3(0, 0, -1);
        PerspectiveCamera camera = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        GameScreen.setupCamera(camera, position, lookAt);
        screen.setCurrentCam(camera);
        return camera;
    }

    private RectanglePlus createRectangle(Vector3 position, float rectWidth, float rectHeight, float rectDepth, GameScreen screen) {
        RectanglePlus rectangle = new RectanglePlus(
            position.x - rectWidth / 2f,
            position.y - rectHeight / 2f,
            position.z - rectDepth / 2f,
            rectWidth, rectHeight, rectDepth,
            id, RectanglePlusFilter.PLAYER,
            false,
            screen.game.getRectMan()
        );
        rectangle.oldPosition.set(rectangle.getPositionImmutable());
        rectangle.newPosition.set(rectangle.getPositionImmutable());
        return rectangle;
    }

    public void handleInput(final float delta) {
        controls.handleInput(delta);
    }

    @Override
    public void beforeTick() {
        log.debug("Start tick player thread id: " + Thread.currentThread().getId() + ".");
        super.beforeTick();
    }

    @Override
    public void tick(final float delta) {
        controls.tick();
    }

    @Override
    public void afterTick() {
        super.afterTick();
        log.debug("End tick player thread id: " + Thread.currentThread().getId() + ".");
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

    public float getExitDistance() {
        float playerX = getPositionX();
        float playerZ = getPositionZ();
        float exitX = screen.getExitPosition().x;
        float exitZ = screen.getExitPosition().z;
        float dx = exitX - playerX;
        float dz = exitZ - playerZ;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }
}
