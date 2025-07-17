package io.github.labyrinthgenerator.pages.game3d.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
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

    public MyDebugRenderer() {
        createShader();
    }

    private void createShader() {
        shader = new ShaderProgram(
            Gdx.files.internal("shaders/debug.vertex.glsl"),
            Gdx.files.internal("shaders/debug.fragment.glsl"));
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        }
    }

    public void render(Matrix4 combined) {
        shader.bind();
        shader.setUniformMatrix("u_projTrans", combined);

        renderLines();

        shader.end();
    }

    private void renderLines() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (RectanglePlus rect : shapes) {
            Matrix4 worldTrans = rect.getTransformMatrix();
            shader.setUniformMatrix("u_worldTrans", worldTrans);
            Color color = rect.overlaps ? JOINT_COLOR : rect.nearest ? NEAREST_COLOR : rect.nearestChunk ? NEAREST_CHUNK_COLOR : SHAPE_COLOR;
            rect.overlaps = false;
            rect.nearest = false;
            rect.nearestChunk = false;
            shader.setUniformf("u_color", color);
            Mesh rectMesh = rect.getMesh();

            rectMesh.render(shader, GL20.GL_LINES);
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public final Color SHAPE_COLOR = Color.GREEN;
    public final Color JOINT_COLOR = new Color(0.5f, 0.8f, 0.8f, 1);
    public final Color NEAREST_COLOR = Color.RED;
    public final Color NEAREST_CHUNK_COLOR = new Color(0f, 0.5f, 0f, 1);
}
