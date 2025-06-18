package io.github.labyrinthgenerator.pages.game3d.cell;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
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

	public boolean neighbourCellNorth = false;
	public boolean neighbourCellSouth = false;
	public boolean neighbourCellWest = false;
	public boolean neighbourCellEast = false;

	public Texture texRegNorth, texRegSouth, texRegWest, texRegEast, texRegFloor, texRegCeiling;

	public Cell3D(final Vector3 position, final GameScreen screen) {
		super(screen);
		this.position.set(position.cpy().add(0, HALF_UNIT, 0));
	}

	public void buildCell() {
		if (hasWallNorth) {
			mdlInstWallNorth = new ModelInstanceBB(screen.game.getCellBuilder().mdlWallNorth);

			if (texRegNorth != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstWallNorth.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegNorth));
			}

			mdlInstWallNorth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, -HALF_UNIT)));
		}

		if (hasWallSouth) {
			mdlInstWallSouth = new ModelInstanceBB(screen.game.getCellBuilder().mdlWallSouth);

			if (texRegSouth != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstWallSouth.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegSouth));
			}

			mdlInstWallSouth.transform.setToTranslation(this.position.cpy().add(new Vector3(0, 0, HALF_UNIT)));
		}

		if (hasWallWest) {
			mdlInstWallWest = new ModelInstanceBB(screen.game.getCellBuilder().mdlWallWest);

			if (texRegWest != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstWallWest.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegWest));
			}

			mdlInstWallWest.transform.setToTranslation(this.position.cpy().add(new Vector3(HALF_UNIT, 0, 0)));
		}

		if (hasWallEast) {
			mdlInstWallEast = new ModelInstanceBB(screen.game.getCellBuilder().mdlWallEast);

			if (texRegEast != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstWallEast.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegEast));
			}

			mdlInstWallEast.transform.setToTranslation(this.position.cpy().add(new Vector3(-HALF_UNIT, 0, 0)));
		}

		if (hasFloor) {
			mdlInstFloor = new ModelInstanceBB(screen.game.getCellBuilder().mdlFloor);

			if (texRegFloor != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstFloor.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegFloor));
			}

			mdlInstFloor.transform.setToTranslation(this.position.cpy().add(new Vector3(0, HALF_UNIT, 0)));
		}

		if (hasCeiling) {
			mdlInstCeiling = new ModelInstanceBB(screen.game.getCellBuilder().mdlCeiling);

			if (texRegCeiling != null) {
				final TextureAttribute ta = (TextureAttribute) mdlInstCeiling.materials.get(0)
						.get(TextureAttribute.Diffuse);
				ta.set(new TextureRegion(texRegCeiling));
			}

			mdlInstCeiling.transform.setToTranslation(this.position.cpy().add(new Vector3(0, -HALF_UNIT, 0)));
		}
	}

	@Override
	public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
		if (hasWallNorth) {
			mdlInstWallNorth.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstWallNorth));
			if (mdlInstWallNorth.isInFrustum()) {
				mdlBatch.render(mdlInstWallNorth, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}

		if (hasWallSouth) {
			mdlInstWallSouth.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstWallSouth));
			if (mdlInstWallSouth.isInFrustum()) {
				mdlBatch.render(mdlInstWallSouth, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}

		if (hasWallWest) {
			mdlInstWallWest.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstWallWest));
			if (mdlInstWallWest.isInFrustum()) {
				mdlBatch.render(mdlInstWallWest, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}

		if (hasWallEast) {
			mdlInstWallEast.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstWallEast));
			if (mdlInstWallEast.isInFrustum()) {
				mdlBatch.render(mdlInstWallEast, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}

		if (hasFloor) {
			mdlInstFloor.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstFloor));
			if (mdlInstFloor.isInFrustum()) {
				mdlBatch.render(mdlInstFloor, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}

		if (hasCeiling) {
			mdlInstCeiling.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInstCeiling));
			if (mdlInstCeiling.isInFrustum()) {
				mdlBatch.render(mdlInstCeiling, env, ((MyShaderProvider) screen.game.getShaderProvider()).getShader());
			}
		}
	}

}
