package io.github.labyrinthgenerator.pages.game3d.tickable;

import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

public class Wave extends Tickable {

    private final float size = 5f;
    private final float speed = 20f;
    private final int count = 2;

    private final float startX;
    private final float endX;
    private final float[] waveX;

    public Wave(CubeLab3D game) {
        super(game);
        Vector3i worldSize = game.getChunkMan().getWorldSize();
        startX = -size - Math.max(worldSize.x, worldSize.z);
        endX = size + worldSize.x;
        waveX = new float[count];
        float step = Math.abs(endX - startX) / count;
        for (int i = 0; i < count; i++) {
            waveX[i] = startX + step * i;
        }
    }

    public void tick(final float delta) {
        for (int i = 0; i < count; i++) {
            waveX[i] += speed * delta;
            if (waveX[i] > endX) {
                waveX[i] = startX + waveX[i] - endX;
            }
        }
    }

    public boolean isOnWave(float x, float z) {
        // Вычисляем расстояние от точки (x, z) до линии, проходящей через (x0, y0)
        // Уравнение линии: z - y0 = (x - x0)
        // Переписываем в виде: z - x = y0 - x0
        for (float waveX : this.waveX) {
            float distance = Math.abs((z - x) - waveX);

            // Проверяем, находится ли расстояние в пределах ширины линии
            if (distance <= size / 2) return true;
        }
        return false;
    }
}
