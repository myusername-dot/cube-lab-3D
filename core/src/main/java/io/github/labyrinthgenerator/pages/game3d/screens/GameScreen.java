package io.github.labyrinthgenerator.pages.game3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.shaders.FogFreeShader;

import java.util.List;

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
        List<RectanglePlus> nearestRects = game.getRectMan().getNearestRectsByFilters(
            currentCam.position.x,
            currentCam.position.z,
            rect
        );

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
        }

        rect.setX(rect.newPosition.x);
    }

    /**
     * Check for overlap in angle X.
     */
    private void checkOverlapY(final RectanglePlus rect, List<RectanglePlus> nearestRects, final float delta) {
        rect.setY(rect.newPosition.y);

        // остановка у стен
        if (game.getRectMan().checkCollisions(rect, nearestRects)) {
            rect.newPosition.y = rect.oldPosition.y;
        }

        rect.setY(rect.newPosition.y);
    }

    /**
     * Check for overlap in angle Z.
     */
    private void checkOverlapZ(final RectanglePlus rect, List<RectanglePlus> nearestRects, final float delta) {
        rect.setZ(rect.newPosition.z);

        // остановка у стен
        if (game.getRectMan().checkCollisions(rect, nearestRects)) {
            rect.newPosition.z = rect.oldPosition.z;
        }

        rect.setZ(rect.newPosition.z);
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
        game.getRectMan().clear();
        game.getEntMan().clear();
        game.getChunkMan().clear();
        game.getTickMan().clear();
        game.getShaderProvider().clear();
    }

    @Override
    public void render(final float delta) {
        Shader shader = game.getShaderProvider().getShader();
        if (shader instanceof FogFreeShader) {
            ((FogFreeShader) shader).increaseTimer(delta);
        }
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
            game.getEntMan().tickAllEntities(delta, currentCam.position.x, currentCam.position.z);
            game.getTickMan().tickAllEntities(delta);
        }
    }
}
