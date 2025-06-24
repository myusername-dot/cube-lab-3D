package io.github.labyrinthgenerator.pages.game3d.nonpositional;

import com.badlogic.gdx.math.Vector2;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class Wave extends NonPosEntity {

    private final Vector2 worldSize;

    private final float size = 5f;
    private final float speed = 20f;
    private final float frequencySec = 0.5f;

    private float offsetX;
    //private final float offsetZ;
    private float timer = frequencySec;

    public Wave(GameScreen screen) {
        super(screen);
        worldSize = screen.game.getChunkMan().getWorldSize();
        offsetX = -HALF_UNIT - worldSize.x - size;
        //offsetZ = -HALF_UNIT + worldSize.y + size;
    }

    public void tick(final float delta) {
        timer += delta;
        if ((int) (timer / frequencySec) > 0) {
            offsetX += speed * delta;
            if (offsetX > -HALF_UNIT + worldSize.x + size) {
                offsetX = -HALF_UNIT - worldSize.x - size;
                timer = 0f;
            }
        }
    }

    public boolean isOnWave(float x, float z) {
        // Вычисляем расстояние от точки (x, z) до линии, проходящей через (x0, y0)
        // Уравнение линии: z - y0 = (x - x0)
        // Переписываем в виде: z - x = y0 - x0
        float distance = Math.abs((z - x) - offsetX);

        // Проверяем, находится ли расстояние в пределах ширины линии
        return distance <= size / 2;
    }
}
