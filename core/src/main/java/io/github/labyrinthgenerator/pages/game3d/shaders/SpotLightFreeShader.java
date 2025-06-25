package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Iterator;

public class SpotLightFreeShader implements Shader {

    public float intensity = 10f;
    public float cutoffAngle = 30f;
    public Color color = Color.ORANGE;

    private ShaderProgram program;

    String vertexShader =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float spotCutoff;\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 spotColor;\n" +
            "varying float v_distance;\n" + // Расстояние до камеры
            "varying float v_height;\n" + // Передаем высоту
            "void main(void)\n" +
            "{\n" +
            "    gl_Position = u_projTrans * u_worldTrans * a_position;\n" +
            "    v_height = gl_Position.y; // Передаем высоту в фрагментный шейдер\n" +
            "\n" +
            "    vec3 lightPosition  = (gl_Position).xyz;\n" +
            "    vec3 spotDirection  = normalize(lightPosition.xyz + vec3(0,0,1)).xyz;\n" +
            "\n" +
            "    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;\n" +
            "    vec3 lightDirection = normalize(vertex.xyz - lightPosition.xyz);\n" +
            "\n" +
            "    float angle = dot(spotDirection, -lightDirection);\n" +
            "    if (angle < 0) angle = 0;\n" +
            "\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    if (acos(angle) < radians(spotCutoff))\n" +
            "       spotColor = vec4(1,1,0.1,1); // lit (yellow)\n" +
            "    else\n" +
            "       spotColor = vec4(0.1,0.1,0.1,1); // unlit(black);\n" +
            "\n" +
            "    v_distance = length(gl_Position.xyz);\n" + // Вычисляем расстояние
            "}";

    String fragmentShader =
        "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying LOWP vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "varying vec4 spotColor;\n" +
            "uniform vec4 fogColor;\n" + // Цвет тумана
            "varying float v_distance;\n" +
            "uniform float fogDensity;\n" + // Плотность тумана
            "varying float v_height;\n" + // Передаем высоту
            "void main(void)\n" +
            "{\n" +
            "   vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "   float fogDistanceFactor = exp(-fogDensity * v_distance);\n" + // Вычисляем фактор тумана
            "   float heightFactor = max(0.0, v_height * 2);\n" + // Чем ниже фрагмент, тем больше плотность, 0 y в центре камеры, положительные значения ниже
            "   float fogFactor = clamp(fogDistanceFactor + heightFactor, 0.0, 1.0);\n" +
            "   gl_FragColor = mix(mix(c, spotColor, 0.5), fogColor, clamp(fogFactor, 0.0, 0.8));\n" + // Интерполяция между цветом текстуры и цветом тумана
            "}";


    private RenderContext context;
    private int u_projTrans;
    private int u_worldTrans;

    @Override
    public void init() {
        program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        program.bind();
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformf("spotCutoff", cutoffAngle);
        program.setUniformi("u_texture", 0);

        // Устанавливаем цвет тумана и его плотность
        program.setUniformf("fogColor", new Color(0.1f, 0.1f, 0.1f, 1.0f)); // Цвет тумана (например, серый)
        program.setUniformf("fogDensity", 1f); // Плотность тумана (можно настроить)

        context.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);

        // bind texture
        boolean bind = false;
        Iterator<Attribute> matIter = renderable.material.iterator();
        if (matIter.hasNext() && !bind) {
            Attribute attribute = matIter.next();
            if (attribute instanceof TextureAttribute) {
                TextureAttribute textureAttribute = (TextureAttribute) attribute;
                Texture texture = textureAttribute.textureDescription.texture;
                texture.bind(0);
                bind = true;
            }
        }

        renderable.meshPart.render(program);
    }

    @Override
    public void end() {
        context.end();
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }
}
