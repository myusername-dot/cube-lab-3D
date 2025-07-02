package io.github.labyrinthgenerator.pages.game3d.models;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ModelInstanceBB extends ModelInstance {

	private boolean isInFrustum = false;

    private final Vector3 positionStatic; // for static objects

	public final Vector3 center = new Vector3(); // for sphere
	public final Vector3 dimensions = new Vector3(); // for sphere
	public float radius; // for sphere
	public final BoundingBox renderBox = new BoundingBox();

    public ModelInstanceBB(final Model model, final Vector3 positionStatic) {
        super(model);
        this.positionStatic = positionStatic;

        // Проверка на наличие вершин
        if (model.meshes.size == 0) {
            throw new GdxRuntimeException("Model has no meshes.");
        }

        updateTransforms();
    }

    private void updateTransforms() {
        // Обновите трансформацию
        calculateTransforms();

        // Инициализируйте границы
        calculateBoundingBox(renderBox);
        renderBox.mul(transform);

        // Получите центр и размеры
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

    @Override
    public Renderable getRenderable (final Renderable out, final Node node, final NodePart nodePart) {
        super.getRenderable(out, node, nodePart);

        out.userData = positionStatic == null ? null : positionStatic.cpy();
        return out;
    }
}
