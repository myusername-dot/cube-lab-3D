package io.github.labyrinthgenerator.pages.game3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GameScreen implements Screen {
	public final CubeLab3D game;

	protected Camera currentCam;

	protected Viewport viewport;

	private final Vector3 currentSpherePos = new Vector3();
	protected Player player;

	public GameScreen(final CubeLab3D game) {
		this.game = game;

		this.game.gameIsPaused = false;

		game.getEntMan().setScreen(this);
	}

	public void checkOverlaps(final RectanglePlus rect, final float delta) {
        List<RectanglePlus> nearestRects = game.getRectMan().getNearestRectsByFilters(rect);

		checkOverlapX(rect, nearestRects, delta);
        checkOverlapY(rect, nearestRects, delta);
		checkOverlapZ(rect, nearestRects, delta);
	}

	/**
	 * Check for overlap in angle X.
	 */
	private void checkOverlapX(final RectanglePlus rect, List<RectanglePlus> nearestRects, final float delta) {
		rect.setX(rect.newPosition.x);

        // остановка у стен
		if (game.getRectMan().checkCollisions(rect, nearestRects)) {
			rect.newPosition.x = rect.oldPosition.x;
//			System.err.println("OVERLAP X");
		}

		rect.setX(rect.newPosition.x);
	}

    /**
     * Check for overlap in angle X.
     */
    private void checkOverlapY(final RectanglePlus rect, List<RectanglePlus> nearestRects,  final float delta) {
        rect.setY(rect.newPosition.y);

        // остановка у стен
        if (game.getRectMan().checkCollisions(rect, nearestRects)) {
            rect.newPosition.y = rect.oldPosition.y;
//			System.err.println("OVERLAP Y");
        }

        rect.setY(rect.newPosition.y);
    }

	/**
	 * Check for overlap in angle Z.
	 */
	private void checkOverlapZ(final RectanglePlus rect, List<RectanglePlus> nearestRects,  final float delta) {
		rect.setZ(rect.newPosition.z);

        // остановка у стен
		if (game.getRectMan().checkCollisions(rect, nearestRects)) {
			rect.newPosition.z = rect.oldPosition.z;
//			System.err.println("OVERLAP Z");
		}

		rect.setZ(rect.newPosition.z);
	}

    public final void setEnemyInRangeAroundCam() {
        float closestDistanceBetweenRects = 50f;
        Set<RectanglePlus> enemiesRects = game.getRectMan().getRectsByFilter(RectanglePlusFilter.ENEMY);
        enemiesRects.addAll(game.getRectMan().getRectsByFilter(RectanglePlusFilter.ENTITY));
        for (final RectanglePlus enemyRect : enemiesRects) {

            ((Enemy) game.getEntMan().getEntityFromId(enemyRect.getConnectedEntityId()))
                .setIsPlayerInRange(false);

            float distanceBetweenRects = Vector2.dst2(
                currentCam.position.x, currentCam.position.z,
                enemyRect.getX() + enemyRect.getWidth() / 2f, enemyRect.getZ() + enemyRect.getDepth() / 2f);

            if (distanceBetweenRects < closestDistanceBetweenRects) {
                ((Enemy) game.getEntMan().getEntityFromId(enemyRect.getConnectedEntityId()))
                    .setIsPlayerInRange(true);
            }
        }
    }

	@Override
	public void dispose() {

	}

	public boolean frustumCull(final Camera cam, final ModelInstanceBB modelInst) {
//		modelInst.calculateTransforms(); // Use if animations or moving mesh parts/bones.
		modelInst.calculateBoundingBox(modelInst.renderBox);
		modelInst.renderBox.mul(modelInst.transform.cpy());

		modelInst.transform.getTranslation(currentSpherePos);
		currentSpherePos.add(modelInst.center);

		return cam.frustum.sphereInFrustum(currentSpherePos, modelInst.radius);
	}

	public Camera getCurrentCam() {
		return currentCam;
	}

	public Player getPlayer() {
		return player;
	}

	public void handleInput(final float delta) {
//		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { // For easy quit while debugging.
//			Gdx.app.exit();
//		}
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {

	}

	protected void removeAllEntities() {
		for (final Entity ent : new ArrayList<>(game.getEntMan().entities.values())) {
			ent.setDestroy(true);
			ent.destroy();
		}

//		for (final Entity ent : game.getEntMan().entities) {
//			System.out.println(ent);
//		}

		game.getEntMan().entities.clear(); // Removes cell3Ds and doors.
		game.getRectMan().clear(); // remove rect walls too.
//		System.err.println("Entities now: " + game.getEntMan().entities.size);
//		System.err.println("Rects now: " + game.getRectMan().rects.size);
	}

	@Override
	public void render(final float delta) {
		handleInput(delta);
		tick(delta);
	}

	@Override
	public void resize(final int width, final int height) {
		if (viewport != null) {
			viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}

	@Override
	public void resume() {

	}

	public void setCurrentCam(final Camera currentCam) {
		this.currentCam = currentCam;
	}

	@Override
	public void show() {

	}

	public void tick(final float delta) {
		if (!game.gameIsPaused) {
			game.getEntMan().tickAllEntities(delta);
		}
	}

}
