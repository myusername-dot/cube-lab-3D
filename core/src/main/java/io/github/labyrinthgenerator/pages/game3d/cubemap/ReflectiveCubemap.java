package io.github.labyrinthgenerator.pages.game3d.cubemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;

public class ReflectiveCubemap {

    private final PerspectiveCamera camFb;
    private final FrameBufferCubemap fb;
    private final Cubemap cubemap;
    private final ModelInstanceBB reflectiveSphereMdlInst;

    private final CubeLab3D game;

    public ReflectiveCubemap(final Vector3 position, CubeLab3D game) {
        this.game = game;

        camFb = new PerspectiveCamera(90, 640, 480);
        camFb.position.set(position);
        camFb.lookAt(0, 0, 0);
        camFb.near = 0.01f;
        camFb.far = 5f;
        camFb.update();

        fb = new FrameBufferCubemap(Pixmap.Format.RGB888, 256, 256, true);
        fb.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        cubemap = fb.getColorBufferTexture();

        ModelBuilder modelBuilder = new ModelBuilder();

        Model sphereModel = modelBuilder.createSphere(
            0.8f, 0.8f, 0.8f, 32, 32,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        reflectiveSphereMdlInst = new ModelInstanceBB(sphereModel, null);

        reflectiveSphereMdlInst.transform.setToTranslation(position);
        reflectiveSphereMdlInst.calculateTransforms();
        reflectiveSphereMdlInst.materials.get(0).set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
    }

    public Vector3 getPosition() {
        return camFb.position;
    }

    public void setPosition(Vector3 position) {
        camFb.position.set(position);
        camFb.update();
        reflectiveSphereMdlInst.transform.setToTranslation(position);
    }

    public void updateCubemap(final Environment env, float delta) {
        if (!game.getScreen().frustumCull(game.getScreen().getCurrentCam(), reflectiveSphereMdlInst)) return;

        ModelBatch modelBatch = game.getMdlBatch();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        fb.begin();
        while (fb.nextSide()) {
            fb.getSide().getUp(camFb.up);
            fb.getSide().getDirection(camFb.direction);
            camFb.update();

            ScreenUtils.clear(1, 1, 1, 1, true);

            modelBatch.begin(camFb);

            game.getEntMan().render3DAllEntities(modelBatch, env, delta, camFb.position.x, camFb.position.z);

            modelBatch.end();
        }
        fb.end();
    }

    public void render(Environment env) {
        if (game.getScreen().frustumCull(game.getScreen().getCurrentCam(), reflectiveSphereMdlInst)) {
            game.getMdlBatch().render(reflectiveSphereMdlInst, env, game.getShaderProvider().getShader());
        }
    }

    public void dispose() {
        fb.dispose();
        cubemap.dispose();
    }
}
