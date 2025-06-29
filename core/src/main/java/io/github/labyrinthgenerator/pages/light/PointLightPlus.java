package io.github.labyrinthgenerator.pages.light;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

public class PointLightPlus extends PointLight {
    public final Vector3 screenPosition = new Vector3();

    public void calculateScreenTransforms(Camera camera) {
        screenPosition.set(worldToScreen(position, camera));
    }

    public Vector3 worldToScreen(final Vector3 worldPosition, final Camera camera) {
        return camera.project(worldPosition.cpy()); // Возвращаем экранные координаты
    }
}
