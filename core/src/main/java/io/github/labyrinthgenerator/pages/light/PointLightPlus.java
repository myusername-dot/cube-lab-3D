package io.github.labyrinthgenerator.pages.light;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

public class PointLightPlus extends PointLight {
    public final Vector3 screenPosition = new Vector3();
    public float camDistance;

    public void calculateScreenTransforms(Camera camera) {
        screenPosition.set(worldToScreen(position, camera));
    }

    public Vector3 worldToScreen(final Vector3 worldPosition, final Camera camera) {
        Vector3 worldPositionBug = worldPosition.cpy();
        //worldPositionBug.y += 0.5f;
        return camera.project(worldPositionBug); // Возвращаем экранные координаты
    }
}
