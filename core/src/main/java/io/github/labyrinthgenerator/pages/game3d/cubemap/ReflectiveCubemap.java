package io.github.labyrinthgenerator.pages.game3d.cubemap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.labyrinthgenerator.pages.game3d.managers.EntityManager;

public class ReflectiveCubemap {

    private final PerspectiveCamera camFb;
    private final FrameBufferCubemap fb;
    private final Cubemap cubemap;
    private final ModelInstance reflectiveSphereMdlInst;

    private final EntityManager entMan;

    public ReflectiveCubemap(ModelBuilder modelBuilder, Vector3 position, int viewportWidth, int viewportHeight,
                             EntityManager entMan) {
        this.entMan = entMan;
        camFb = new PerspectiveCamera(90, viewportWidth, viewportHeight);
        camFb.position.set(position);
        camFb.lookAt(0, 0, 0);
        camFb.near = 0.01f;
        camFb.far = 10f;
        camFb.update();

        fb = new FrameBufferCubemap(Pixmap.Format.RGBA8888, viewportWidth, viewportHeight, true);
        cubemap = fb.getColorBufferTexture();

        Model sphereModel = modelBuilder.createSphere(1f, 1f, 1f, 32, 32,
            new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        reflectiveSphereMdlInst = new ModelInstance(sphereModel);

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

    public ModelInstance getReflectiveSphereModel() {
        return reflectiveSphereMdlInst;
    }

    public void dispose() {
        fb.dispose();
        cubemap.dispose();
    }
}
