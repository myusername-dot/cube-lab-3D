package io.github.labyrinthgenerator.pages.game3d.cubemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.labyrinthgenerator.pages.game3d.managers.EntityManager;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

public class ReflectiveCubemap {

    private final PerspectiveCamera camFb;
    private final FrameBufferCubemap fb;
    private final Cubemap cubemap;
    private final ModelInstanceBB reflectiveSphereMdlInst;

    private final EntityManager entMan;

    public ReflectiveCubemap(Vector3 position, int viewportWidth, int viewportHeight,
                             EntityManager entMan) {
        this.entMan = entMan;

        camFb = new PerspectiveCamera(90, 640, 480);
        camFb.position.set(position);
        camFb.lookAt(0, 0, 0);
        camFb.near = 0.01f;
        camFb.far = 10f;
        camFb.update();

        fb = new FrameBufferCubemap(Pixmap.Format.RGB888, 512, 512, true);
        fb.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        cubemap = fb.getColorBufferTexture();

        ModelBuilder modelBuilder = new ModelBuilder();

        Model sphereModel = modelBuilder.createSphere(1f, 1f, 1f, 32, 32,
            new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        reflectiveSphereMdlInst = new ModelInstanceBB(sphereModel, null);

        reflectiveSphereMdlInst.transform.setToTranslation(position);
        reflectiveSphereMdlInst.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
    }

    public void setPosition(Vector3 position) {
        camFb.position.set(position);
        camFb.update();
        reflectiveSphereMdlInst.transform.setToTranslation(position);
    }

    public void updateCubemap(final ModelBatch modelBatch, final Environment env, float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        fb.begin();
        while (fb.nextSide()) {
            fb.getSide().getUp(camFb.up);
            fb.getSide().getDirection(camFb.direction);
            camFb.update();

            ScreenUtils.clear(1, 1, 1, 1, true);

            modelBatch.begin(camFb);

            entMan.render3DAllEntities(modelBatch, env, delta, camFb.position.x, camFb.position.z);

            modelBatch.end();
        }
        fb.end();
    }

    public void render(ModelBatch mdlBatch, Environment env, GameScreen screen) {
        if (screen.frustumCull(screen.getCurrentCam(), reflectiveSphereMdlInst)) {
            mdlBatch.render(reflectiveSphereMdlInst, env);
        }
    }

    public void dispose() {
        fb.dispose();
        cubemap.dispose();
    }
}
