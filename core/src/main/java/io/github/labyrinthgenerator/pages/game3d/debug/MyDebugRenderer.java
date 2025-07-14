package io.github.labyrinthgenerator.pages.game3d.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
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

    private ShaderProgram shader;
    private Mesh lineMesh;
    private final CubeLab3D game;

    public MyDebugRenderer(CubeLab3D game) {
        this.game = game;
        createShader();
        createMesh();
    }

    private void createShader() {
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/line.vertex.glsl"),
            Gdx.files.internal("shaders/line.fragment.glsl"));
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        }
    }

    private void createMesh() {
        lineMesh = new Mesh(true, 1_000_000, 0,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_Color"));
    }

    public void render(Camera camera) {
        Matrix4 combined = camera.combined.cpy();

        // Устанавливаем матрицу проекции
        shader.bind();
        shader.setUniformMatrix("u_projectionView", combined);

        // Рендерим линии
        renderLines();

        shader.end();
    }

    private void renderLines() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (RectanglePlus rect : shapes) {
            // Получаем матрицу трансформации для текущего прямоугольника
            //Matrix4 worldTrans = rect.getTransformMatrix();

            //shader.setUniformMatrix("u_worldTrans", worldTrans);

            lineMesh.setVertices(getLineVertices(rect));

            lineMesh.render(shader, GL20.GL_LINES);
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private float[] getLineVertices(RectanglePlus rectanglePlus) {
        // Каждая прямоугольная форма добавляет 12 линий (по 2 вершины на линию)
        // 12 линий * 2 вершины * 7 значений (3 позиции и 4 цвета)
        float[] vertices = new float[12 * 2 * 7]; // 12 линий * 2 вершины * 7 значений
        int index = 0;

        Color color = rectanglePlus.overlaps ? JOINT_COLOR : SHAPE_COLOR;

        // Добавляем вершины для рендеринга 3D прямоугольника
        addRectangleVertices(rectanglePlus, color, vertices, index);

        return vertices;
    }

    private void addRectangleVertices(RectanglePlus rectanglePlus, Color color, float[] vertices, int index) {
        float x = rectanglePlus.getX();
        float y = rectanglePlus.getY();
        float z = rectanglePlus.getZ();
        float width = rectanglePlus.getWidth();
        float height = rectanglePlus.getHeight();
        float depth = rectanglePlus.getDepth();

        // Добавляем вершины для линий (по периметру)
        // Front bottom
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Right bottom
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();

        // Back bottom
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();

        // Left bottom
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Front top
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Right top
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();

        // Back top
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();

        // Left top
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Left front
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Right front
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z;
        vertices[index++] = color.toFloatBits();

        // Left back
        vertices[index++] = x;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();

        // Right back
        vertices[index++] = x + width;
        vertices[index++] = y;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
        vertices[index++] = x + width;
        vertices[index++] = y + height;
        vertices[index++] = z + depth;
        vertices[index++] = color.toFloatBits();
    }

    public final Color SHAPE_COLOR = Color.GREEN;
    public final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
}
