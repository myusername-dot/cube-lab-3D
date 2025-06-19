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
import io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class Cell3D extends Entity {
	public final Vector3 position = new Vector3();

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
		super(screen);
		this.position.set(position.cpy().add(0, HALF_UNIT, 0));
	}

    private ModelInstanceBB createModelInstanceBB(Model model, Texture texture) {
        ModelInstanceBB modelInstanceBB = new ModelInstanceBB(model);
        if (texture != null) {
            final TextureAttribute ta = (TextureAttribute) modelInstanceBB.materials.get(0)
                .get(TextureAttribute.Diffuse);
            ta.set(new TextureRegion(texRegNorth));
        }
        return modelInstanceBB;
    }

	public void buildCell() {
		if (hasWallNorth) {
			mdlInstWallNorth = createModelInstanceBB(screen.game.getCellBuilder().mdlWallNorth, texRegNorth);
			mdlInstWallNorth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, -HALF_UNIT)));
		}
		if (hasWallSouth) {
			mdlInstWallSouth = createModelInstanceBB(screen.game.getCellBuilder().mdlWallSouth, texRegSouth);
			mdlInstWallSouth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, HALF_UNIT)));
		}
		if (hasWallWest) {
			mdlInstWallWest = createModelInstanceBB(screen.game.getCellBuilder().mdlWallWest, texRegWest);
			mdlInstWallWest.transform.setToTranslation(this.position.cpy().add(new Vector3(HALF_UNIT, 0, 0)));
		}
		if (hasWallEast) {
			mdlInstWallEast = createModelInstanceBB(screen.game.getCellBuilder().mdlWallEast, texRegEast);
			mdlInstWallEast.transform.setToTranslation(this.position.cpy().add(new Vector3(-HALF_UNIT, 0, 0)));
		}
		if (hasFloor) {
			mdlInstFloor = createModelInstanceBB(screen.game.getCellBuilder().mdlFloor, texRegFloor);
			mdlInstFloor.transform.setToTranslation(this.position.cpy().add(new Vector3(0, HALF_UNIT, 0)));
		}
		if (hasCeiling) {
			mdlInstCeiling = createModelInstanceBB(screen.game.getCellBuilder().mdlCeiling, texRegCeiling);
			mdlInstCeiling.transform.setToTranslation(this.position.cpy().add(new Vector3(0, -HALF_UNIT, 0)));
		}
	}

    private void setInFrustum(final ModelInstanceBB model, final ModelBatch mdlBatch, final Environment env, final Shader shader) {
        model.setInFrustum(screen.frustumCull(screen.getCurrentCam(), model));
        if (model.isInFrustum()) {
            mdlBatch.render(model, env, shader);
        }
    }

	@Override
	public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        Shader shader = ((MyShaderProvider) screen.game.getShaderProvider()).getShader();

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
