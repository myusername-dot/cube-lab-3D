package io.github.labyrinthgenerator.pages.game3d.debug;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.HashSet;
import java.util.Set;

public class MyDebugRenderer {

    public final static Set<RectanglePlus> shapes = new HashSet<>();

    public enum DebugMode {
        DISABLE,
        DEBUG_ONLY,
        DEBUG
    }

    public DebugMode debugMode = DebugMode.DISABLE;

    protected ShapeRenderer renderer;

    protected CubeLab3D game;

    public MyDebugRenderer(CubeLab3D game) {
        this.game = game;
        renderer = new ShapeRenderer();
    }

    public void render(Camera camera) {
        Player player = game.getScreen().getPlayer();
        Vector3 horizontalAxis = Vector3.Z.cpy().add(Vector3.X);
        float horizontalAngle = 0;
        if (player != null) {
            horizontalAxis.set(player.getHorizontalAxis());
            horizontalAngle = player.getCurrentHorizontalAngle();
        }

        if (player != null) camera.rotate(horizontalAxis, -horizontalAngle * 2);
        camera.up.scl(-1);
        camera.update();
        Matrix4 combined = camera.combined.cpy();
        if (player != null) camera.rotate(horizontalAxis, horizontalAngle * 2);
        camera.up.scl(-1);
        camera.update();
        // Создаем матрицу масштабирования для инверсии оси Y
        Matrix4 scaleMatrix = new Matrix4();
        scaleMatrix.setToScaling(1, -1, 1); // Инвертируем ось Y

        /*Vector3i worldSize = game.getChunkMan().getWorldSize();
        float[] vals = combined.getValues();
        combined.setTranslation(worldSize.x - vals[Matrix4.M03], vals[Matrix4.M13], vals[Matrix4.M23]);*/

        // Умножаем матрицу проекции на матрицу масштабирования
        combined.mul(scaleMatrix);

        // Устанавливаем перевернутую матрицу проекции
        renderer.setProjectionMatrix(combined);

        // Рендерим объекты
        renderShapes();
    }

    private void renderShapes() {
        renderer.begin(ShapeRenderer.ShapeType.Line);

        for (RectanglePlus rect : shapes) {
            renderer.setColor(rect.overlaps ? JOINT_COLOR : SHAPE_COLOR);
            render3DRectangle(rect);
        }

        renderer.end();
    }

    private void render3DRectangle(RectanglePlus rectanglePlus) {
        Vector3i worldSize = game.getChunkMan().getWorldSize();
        float x = rectanglePlus.getX();
        float y = rectanglePlus.getY();
        float z = rectanglePlus.getZ();
        float width = rectanglePlus.getWidth();
        float height = rectanglePlus.getHeight();
        float depth = rectanglePlus.getDepth();

        y = -y;
        height = -height;

        // Рендеринг 3D прямоугольника в виде линий (по периметру)
        renderer.line(x, y, z, x + width, y, z); // Front bottom
        renderer.line(x + width, y, z, x + width, y, z + depth); // Right bottom
        renderer.line(x + width, y, z + depth, x, y, z + depth); // Back bottom
        renderer.line(x, y, z + depth, x, y, z); // Left bottom

        renderer.line(x, y + height, z, x + width, y + height, z); // Front top
        renderer.line(x + width, y + height, z, x + width, y + height, z + depth); // Right top
        renderer.line(x + width, y + height, z + depth, x, y + height, z + depth); // Back top
        renderer.line(x, y + height, z + depth, x, y + height, z); // Left top

        renderer.line(x, y, z, x, y + height, z); // Left front
        renderer.line(x + width, y, z, x + width, y + height, z); // Right front
        renderer.line(x, y, z + depth, x, y + height, z + depth); // Left back
        renderer.line(x + width, y, z + depth, x + width, y + height, z + depth); // Right back
    }

    public final Color SHAPE_COLOR = Color.GREEN;
    public final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
}
