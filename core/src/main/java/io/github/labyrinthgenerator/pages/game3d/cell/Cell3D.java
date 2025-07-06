package io.github.labyrinthgenerator.pages.game3d.cell;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3f;

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

    private void adjustModelsForGravity(Vector3 gravityDirection) {
        Model tmp1, tmp2;
        if (gravityDirection.equals(new Vector3(0, 1, 0))) {
            // Гравитация направлена вниз
        } else if (gravityDirection.equals(new Vector3(0, -1, 0))) {
            // Гравитация направлена вверх
            tmp1 = mdlFloor;
            mdlFloor = mdlCeiling;
            mdlCeiling = tmp1;
        } else if (gravityDirection.equals(new Vector3(1, 0, 0))) {
            // Гравитация направлена вправо
            tmp1 = mdlCeiling;
            tmp2 = mdlWallEast;
            mdlWallWest = tmp1;
            mdlWallEast = mdlFloor;
            mdlFloor = tmp2;
            mdlCeiling = mdlWallEast;
            /*tmp1 = mdlWallNorth;
            mdlWallNorth = mdlWallSouth;
            mdlWallSouth = tmp1;*/
        } else if (gravityDirection.equals(new Vector3(-1, 0, 0))) {
            // Гравитация направлена влево
            tmp1 = mdlFloor;
            tmp2 = mdlWallEast;
            mdlWallWest = tmp1;
            mdlWallEast = mdlCeiling;
            mdlCeiling = tmp2;
            mdlFloor = mdlWallEast;
        } else if (gravityDirection.equals(new Vector3(0, 0, 1))) {
            // Гравитация направлена вперед
        } else if (gravityDirection.equals(new Vector3(0, 0, -1))) {
            // Гравитация направлена назад
        }
    }

    private ModelInstanceBB createModelInstanceBB(Model model, Texture texture, Vector3 positionImmutable) {
        ModelInstanceBB modelInstanceBB = new ModelInstanceBB(model, positionImmutable);
        if (texture != null) {
            final TextureAttribute ta = (TextureAttribute) modelInstanceBB.materials.get(0)
                .get(TextureAttribute.Diffuse);
            ta.set(new TextureRegion(texture));
        }
        return modelInstanceBB;
    }

    public void buildCell(Vector3f gravityDirection) {
        //Vector3f axis = gravityDirection.cpy().abs();
        //adjustModelsForGravity(gravityDirection.vec3());

        // @formatter:off
        if (hasWallNorth) mdlInstWallNorth = createWallInstance(mdlWallNorth, texRegNorth, gravityDirection, getPositionImmutable());
        if (hasWallSouth) mdlInstWallSouth = createWallInstance(mdlWallSouth, texRegSouth, gravityDirection, getPositionImmutable());
        if (hasWallWest) mdlInstWallWest = createWallInstance(mdlWallWest, texRegWest, gravityDirection, getPositionImmutable());
        if (hasWallEast) mdlInstWallEast = createWallInstance(mdlWallEast, texRegEast, gravityDirection, getPositionImmutable());
        if (hasFloor) mdlInstFloor = createFloorOrCeilingInstance(mdlFloor, texRegFloor, gravityDirection, getPositionImmutable());
        if (hasCeiling) mdlInstCeiling = createFloorOrCeilingInstance(mdlCeiling, texRegCeiling, gravityDirection, getPositionImmutable());
        // @formatter:on
    }

    private ModelInstanceBB createWallInstance(
        Model model, Texture texture,
        Vector3f gravityDirection,
        Vector3 position) {
        ModelInstanceBB instance = createModelInstanceBB(model, texture, position);
        transformMdlInstVertsAndNormal(instance, gravityDirection);
        instance.transform.setToTranslation(position);
        return instance;
    }

    private ModelInstanceBB createFloorOrCeilingInstance(
        Model model, Texture texture,
        Vector3f gravityDirection,
        Vector3 position) {
        ModelInstanceBB instance = createModelInstanceBB(model, texture, position);
        transformMdlInstVertsAndNormal(instance, gravityDirection);
        instance.transform.setToTranslation(position);
        return instance;
    }

    private void transformMdlInstVertsAndNormal(ModelInstanceBB instance, Vector3f gravityDirection) {
        Node node = instance.nodes.get(0);

        node.translation.set(Player.adjustVecForGravity(
            gravityDirection,
            node.translation,
            false
        ));
        Quaternion rotation = node.rotation;
        Vector3 rotationVec = new Vector3(rotation.x, rotation.y, rotation.z);
        rotationVec = Player.adjustVecForGravity(
            gravityDirection,
            rotationVec,
            false
        );
        rotation.set(rotationVec.x, rotationVec.y, rotationVec.z, rotation.w);

        MeshPart meshPart = node.parts.get(0).meshPart;
        meshPart.mesh = meshPart.mesh.copy(true);

        int cornerLength = 3 + 3 + 2; // position, normal, uv
        float[] vertices = new float[cornerLength * meshPart.mesh.getNumVertices()];
        meshPart.mesh.getVertices(vertices);
        // set 4 vertices // and normal
        for (int i = 0; i < meshPart.mesh.getNumVertices(); i++) {
            int corner = i * cornerLength;
            Vector3 localVertOrNormal = Player.adjustVecForGravity(
                gravityDirection,
                new Vector3(vertices[corner], vertices[corner + 1], vertices[corner + 2]),
                false
            );
            vertices[corner] = localVertOrNormal.x;
            vertices[corner + 1] = localVertOrNormal.y;
            vertices[corner + 2] = localVertOrNormal.z;
        }
        meshPart.mesh.setVertices(vertices);
        //instance.calculateTransforms();
        instance.updateTransforms();
    }

    private void setInFrustum(final ModelInstanceBB model, final ModelBatch mdlBatch, final Environment env, final Shader shader) {
        model.setInFrustum(screen.frustumCull(screen.getCurrentCam(), model));
        if (model.isInFrustum()) {
            mdlBatch.render(model, env, shader);
        }
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        Shader shader = screen.game.getShaderProvider().getShader();

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
