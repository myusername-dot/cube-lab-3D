package io.github.labyrinthgenerator.pages.light;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

public class PointLightPlus extends PointLight {
    public final Vector3 screenPosition = new Vector3();
    public float camDistance;

    public void calculateScreenTransforms(Camera camera) {
        screenPosition.set(worldToScreen(position.cpy(), camera));
    }

    public Vector3 worldToScreen(final Vector3 worldPosition, final Camera camera) {
        Vector3 screenPos = camera.project(worldPosition);
        screenPos.x /= Gdx.graphics.getWidth();
        screenPos.y /= Gdx.graphics.getHeight();
        screenPos.x *= camera.viewportWidth;
        screenPos.y *= camera.viewportHeight;
        return screenPos;
    }
}
