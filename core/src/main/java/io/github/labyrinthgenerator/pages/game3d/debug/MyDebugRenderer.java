package io.github.labyrinthgenerator.pages.game3d.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;

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

    public void render(Matrix4 combined) {
        renderer.setProjectionMatrix(combined);
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
