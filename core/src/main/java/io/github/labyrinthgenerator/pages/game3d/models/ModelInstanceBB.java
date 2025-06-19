package io.github.labyrinthgenerator.pages.game3d.models;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class ModelInstanceBB extends ModelInstance {

	private boolean isInFrustum = false;

	public final Vector3 center = new Vector3(); // for sphere
	public final Vector3 dimensions = new Vector3(); // for sphere
	public float radius; // for sphere
	public final BoundingBox renderBox = new BoundingBox();

	public ModelInstanceBB(final Model model) {
		super(model);

		calculateTransforms();
		calculateBoundingBox(renderBox);
		renderBox.mul(transform);

		renderBox.getCenter(center);
		renderBox.getDimensions(dimensions);
		radius = dimensions.len() / 2f;
	}

	public boolean isInFrustum() {
		return isInFrustum;
	}

	public void setInFrustum(final boolean isVisible) {
		this.isInFrustum = isVisible;
	}
}
