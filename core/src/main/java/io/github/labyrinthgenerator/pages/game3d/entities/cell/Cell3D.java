package io.github.labyrinthgenerator.pages.game3d.entities.cell;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

public class Cell3D extends Entity {
    private Model mdlWallNorth;
    private Model mdlWallSouth;
    private Model mdlWallWest;
    private Model mdlWallEast;
    private Model mdlFloor;
    private Model mdlCeiling;

    private ModelInstanceBB mdlInstWallNorth;
    private ModelInstanceBB mdlInstWallSouth;
    private ModelInstanceBB mdlInstWallWest;
    private ModelInstanceBB mdlInstWallEast;
    private ModelInstanceBB mdlInstFloor;
    private ModelInstanceBB mdlInstCeiling;

    // @formatter:off
    public boolean hasWallNorth = false;
    public boolean hasWallSouth = false;
    public boolean hasWallWest  = false;
    public boolean hasWallEast  = false;
    public boolean hasFloor     = false;
    public boolean hasCeiling   = false;

    public boolean hasWalls     = false;
    public boolean mobSpawn     = false;

    public Texture texRegNorth, texRegSouth, texRegWest, texRegEast, texRegFloor, texRegCeiling;

    public Cell3D(final Vector3 position, final GameScreen screen) {
        super(position, screen);

        mdlWallNorth = screen.game.getCellBuilder().mdlWallNorth;
        mdlWallSouth = screen.game.getCellBuilder().mdlWallSouth;
        mdlWallWest  = screen.game.getCellBuilder().mdlWallWest;
        mdlWallEast  = screen.game.getCellBuilder().mdlWallEast;
        mdlFloor     = screen.game.getCellBuilder().mdlFloor;
        mdlCeiling   = screen.game.getCellBuilder().mdlCeiling;
    }
    // @formatter:on

    private ModelInstanceBB createModelInstanceBB(Model model, Texture texture, Vector3 positionImmutable) {
        ModelInstanceBB modelInstanceBB = new ModelInstanceBB(model, positionImmutable);
        if (texture != null) {
            final TextureAttribute ta = (TextureAttribute) modelInstanceBB.materials.get(0)
                .get(TextureAttribute.Diffuse);
            ta.set(new TextureRegion(texture));
        }
        return modelInstanceBB;
    }

    public void buildCell() {
        // @formatter:off
        if (hasWallNorth) mdlInstWallNorth = createModelInstance(mdlWallNorth, texRegNorth,   getPositionImmutable());
        if (hasWallSouth) mdlInstWallSouth = createModelInstance(mdlWallSouth, texRegSouth,   getPositionImmutable());
        if (hasWallWest ) mdlInstWallWest  = createModelInstance(mdlWallWest,  texRegWest,    getPositionImmutable());
        if (hasWallEast ) mdlInstWallEast  = createModelInstance(mdlWallEast,  texRegEast,    getPositionImmutable());
        if (hasFloor    ) mdlInstFloor     = createModelInstance(mdlFloor,     texRegFloor,   getPositionImmutable());
        if (hasCeiling  ) mdlInstCeiling   = createModelInstance(mdlCeiling,   texRegCeiling, getPositionImmutable());
        // @formatter:on
    }

    private ModelInstanceBB createModelInstance(Model model, Texture texture, Vector3 position) {

        ModelInstanceBB instance = createModelInstanceBB(model, texture, position);
        transformMdlInstVertsAndNormal(instance);
        instance.transform.setToTranslation(position);
        return instance;
    }

    private void transformMdlInstVertsAndNormal(ModelInstanceBB instance) {
        Node node = instance.nodes.get(0);

        node.translation.set(GravityControls.adjustWorldVecForGravity(node.translation));
        Quaternion rotation = node.rotation;
        Vector3 rotationVec = new Vector3(rotation.x, rotation.y, rotation.z);
        rotationVec = GravityControls.adjustWorldVecForGravity(rotationVec);
        rotation.set(rotationVec.x, rotationVec.y, rotationVec.z, rotation.w);

        MeshPart meshPart = node.parts.get(0).meshPart;
        meshPart.mesh = meshPart.mesh.copy(true);

        int cornerLength = 3 + 3 + 2; // position, normal, uv
        float[] vertices = new float[cornerLength * meshPart.mesh.getNumVertices()];
        meshPart.mesh.getVertices(vertices);
        // set 4 vertices // and normal
        for (int i = 0; i < meshPart.mesh.getNumVertices(); i++) {
            int corner = i * cornerLength;
            Vector3 localVertOrNormal = GravityControls.adjustWorldVecForGravity(
                new Vector3(vertices[corner], vertices[corner + 1], vertices[corner + 2])
            );
            // @formatter:off
            vertices[corner    ] = localVertOrNormal.x;
            vertices[corner + 1] = localVertOrNormal.y;
            vertices[corner + 2] = localVertOrNormal.z;
            // @formatter:on
        }
        meshPart.mesh.setVertices(vertices);
        //instance.calculateTransforms();
        instance.updateTransforms();
    }

    private void setInFrustum(final ModelInstanceBB model, final ModelBatch mdlBatch, final Environment env, final Shader shader) {
        //model.setInFrustum(screen.frustumCull(screen.getCurrentCam(), model));
        model.setInFrustum(screen.frustumCull(screen.getCurrentCam(), getPositionImmutable(), model.radius * 3));
        isInFrustum = isInFrustum || model.isInFrustum();
        if (model.isInFrustum()) {
            mdlBatch.render(model, env, shader);
        }
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        Shader shader = screen.game.getShaderProvider().getShader();
        isInFrustum = false;

        // @formatter:off
        if (hasWallNorth) setInFrustum(mdlInstWallNorth, mdlBatch, env, shader);
        if (hasWallSouth) setInFrustum(mdlInstWallSouth, mdlBatch, env, shader);
        if (hasWallWest ) setInFrustum(mdlInstWallWest,  mdlBatch, env, shader);
        if (hasWallEast ) setInFrustum(mdlInstWallEast,  mdlBatch, env, shader);
        if (hasFloor    ) setInFrustum(mdlInstFloor,     mdlBatch, env, shader);
        if (hasCeiling  ) setInFrustum(mdlInstCeiling,   mdlBatch, env, shader);
        // @formatter:on
    }
}
