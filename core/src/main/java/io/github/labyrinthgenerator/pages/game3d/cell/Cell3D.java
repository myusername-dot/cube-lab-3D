package io.github.labyrinthgenerator.pages.game3d.cell;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class Cell3D extends Entity {
    private ModelInstanceBB mdlInstWallNorth;
    private ModelInstanceBB mdlInstWallSouth;
    private ModelInstanceBB mdlInstWallWest;
    private ModelInstanceBB mdlInstWallEast;
    private ModelInstanceBB mdlInstFloor;
    private ModelInstanceBB mdlInstCeiling;

    public boolean hasWallNorth = true;
    public boolean hasWallSouth = true;
    public boolean hasWallWest = true;
    public boolean hasWallEast = true;
    public boolean hasFloor = false;
    public boolean hasCeiling = false;
    public boolean mobSpawn = false;

    public Texture texRegNorth, texRegSouth, texRegWest, texRegEast, texRegFloor, texRegCeiling;

    public Cell3D(final Vector3 position, final GameScreen screen) {
        super(position.add(0, HALF_UNIT, 0), screen);
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

    public void buildCell() {
        if (hasWallNorth) {
            mdlInstWallNorth = createWallInstance(screen.game.getCellBuilder().mdlWallNorth, texRegNorth,
                getPositionImmutable().add(0, 0, -HALF_UNIT));
        }
        if (hasWallSouth) {
            mdlInstWallSouth = createWallInstance(screen.game.getCellBuilder().mdlWallSouth, texRegSouth,
                getPositionImmutable().add(0, 0, HALF_UNIT));
        }
        if (hasWallWest) {
            mdlInstWallWest = createWallInstance(screen.game.getCellBuilder().mdlWallWest, texRegWest,
                getPositionImmutable().add(HALF_UNIT, 0, 0));
        }
        if (hasWallEast) {
            mdlInstWallEast = createWallInstance(screen.game.getCellBuilder().mdlWallEast, texRegEast,
                getPositionImmutable().add(-HALF_UNIT, 0, 0));
        }
        if (hasFloor) {
            mdlInstFloor = createFloorOrCeilingInstance(screen.game.getCellBuilder().mdlFloor, texRegFloor,
                getPositionImmutable().add(0, HALF_UNIT, 0));
        }
        if (hasCeiling) {
            mdlInstCeiling = createFloorOrCeilingInstance(screen.game.getCellBuilder().mdlCeiling, texRegCeiling,
                getPositionImmutable().add(0, -HALF_UNIT, 0));
        }
    }

    private ModelInstanceBB createWallInstance(Model model, Texture texture, Vector3 position) {
        ModelInstanceBB instance = createModelInstanceBB(model, texture, position);
        instance.transform.setToTranslation(position);
        return instance;
    }

    private ModelInstanceBB createFloorOrCeilingInstance(Model model, Texture texture, Vector3 position) {
        ModelInstanceBB instance = createModelInstanceBB(model, texture, position);
        instance.transform.setToTranslation(position);
        return instance;
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

        if (hasWallNorth) {
            setInFrustum(mdlInstWallNorth, mdlBatch, env, shader);
        }
        if (hasWallSouth) {
            setInFrustum(mdlInstWallSouth, mdlBatch, env, shader);
        }
        if (hasWallWest) {
            setInFrustum(mdlInstWallWest, mdlBatch, env, shader);
        }
        if (hasWallEast) {
            setInFrustum(mdlInstWallEast, mdlBatch, env, shader);
        }
        if (hasFloor) {
            setInFrustum(mdlInstFloor, mdlBatch, env, shader);
        }
        if (hasCeiling) {
            setInFrustum(mdlInstCeiling, mdlBatch, env, shader);
        }
    }
}
