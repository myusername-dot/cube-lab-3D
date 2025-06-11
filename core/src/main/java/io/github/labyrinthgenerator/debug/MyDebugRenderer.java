package io.github.labyrinthgenerator.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class MyDebugRenderer {

    public final static Array<Shape2D> shapes = new Array<Shape2D>();

    protected ShapeRenderer renderer;

    public MyDebugRenderer() {
        // next we setup the immediate mode renderer
        renderer = new ShapeRenderer();
    }

    public void render(Matrix4 projMatrix) {
        renderer.setProjectionMatrix(projMatrix);
        renderShapes();
    }

    private void renderShapes() {
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(SHAPE_COLOR);

        for (Shape2D shape2D : shapes) {
            if (shape2D instanceof Polyline) {
                renderer.polyline(((Polyline) shape2D).getTransformedVertices());
                //drawPolyline((Polyline) shape2D);
            } else if (shape2D instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) shape2D;
                renderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                //drawRectangle((Rectangle) shape2D);
            }
        }

        renderer.end();
    }

    private void drawRectangle(Rectangle rectangle) {
        float x1 = rectangle.getX();
        float y1 = rectangle.getY();
        float x2 = x1 + rectangle.getWidth();
        float y3 = y1 + rectangle.getHeight();
        renderer.line(x1, y1, x2, y1);
        renderer.line(x2, y1, x2, y3);
        renderer.line(x2, y3, x1, y3);
        renderer.line(x1, y3, x1, y1);
    }

    private void drawPolyline(Polyline polyline) {
        float[] vertices = polyline.getTransformedVertices();
        float x1 = vertices[0];
        float y1 = vertices[1];
        for (int i = 2; i < vertices.length; i += 2) {
            float x2 = vertices[i];
            float y2 = vertices[i + 1];
            renderer.line(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
    }

    public final Color SHAPE_COLOR = Color.GREEN;
    public final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
}
