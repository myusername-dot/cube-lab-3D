package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Iterator;

public class SpotLightShader extends BaseShader {

    public float intensity = 10f;
    public float cutoffAngle = 10f;
    public Color color = Color.ORANGE;

    private boolean init = true;
    String vertexShader =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform vec3 u_normalTexture;\n" +
            "uniform float spotCutoff;\n" +
            "varying vec3 lightPosition;\n" +
            "varying vec3 fragPos;\n" +
            "varying vec2 v_texCoords;\n" +
            "varying mat4 u_worldTrans1;\n" +
            "varying mat4 u_projTrans1;\n" +
            "varying vec3 u_normalTexture1;\n" +
            "varying float spotCutoff1;\n" +
            "varying vec4 vertex;\n" +
            "void main(void)\n" +
            "{\n" +
            "    gl_Position = u_projTrans * u_worldTrans * a_position * gl_ModelViewMatrix;\n" +
            "    \n" +
            "    lightPosition  = gl_Position.xyz;\n" +
            "    fragPos = vec3(gl_ModelViewMatrix * a_position);\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    u_worldTrans1 = u_worldTrans;\n" +
            "    u_projTrans1 = u_projTrans;\n" +
            "    u_normalTexture1 = u_normalTexture;\n" +
            "    spotCutoff1 = spotCutoff;\n" +
            "    \n" +
            "    vertex = gl_ModelViewMatrix * gl_Vertex;\n" +
            "}";
    String fragmentShader =
        "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "varying LOWP vec3 lightPosition;\n" +
            "varying LOWP vec3 fragPos;\n" +
            "varying LOWP vec2 v_texCoords;\n" +
            "varying LOWP mat4 u_worldTrans1;\n" +
            "varying LOWP mat4 u_projTrans1;\n" +
            "varying LOWP vec3 u_normalTexture1;\n" +
            "varying LOWP float spotCutoff1;\n" +
            "varying LOWP vec4 vertex;\n" +
            "uniform sampler2D u_texture;\n" +
            "\n" +
            "float LinearizeDepth(float depth, float near, float far) \n" +
            "{\n" +
            "    float z = depth * 2.0 - 1.0; // back to NDC \n" +
            "    return (2.0 * near * far) / (far + near - z * (far - near));\n" +
            "}\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec3 spotDirection = normalize(lightPosition.xyz + vec3(0,0,1)).xyz;\n" +
            "    vec3 lightDirection = normalize(lightPosition.xyz - vertex.xyz);\n" +
            "\n" +
            "    float angle = dot(spotDirection, lightDirection);\n" +
            "    if (angle < 0) angle = 0;\n" +
            "\n" +
            "    vec4 v_color;\n" +
            "    if (acos(angle) < radians(spotCutoff1))\n" +
            "       v_color = vec4(1,1,0,1); // lit (yellow);\n" +
            "    else\n" +
            "        v_color = vec4(0.3,0.3,0.3,1); // unlit(black);\n" +
            /*"    {\n" +
            /*"    // smooth borders\n" +
            "       float diff = abs(acos(angle)) - abs(radians(spotCutoff1));\n" +
            "       diff = diff * 10;\n" +
            "       if (diff > 0.8) diff = 0.8;\n" +
            "       v_color = vec4((vec3(1,1,0) - diff), 1);\n" +
            "    }\n" +*/
            "\n" +
            "    vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "    // ambient\n" +
            "    float ambient = 0.5;\n" +
            "    gl_FragColor = c * (v_color * ambient);\n" +
            "    // diffuse\n" +
            "    vec3 norm = normalize(u_normalTexture1);\n" +
            "    vec3 lightDir = normalize(lightPosition - fragPos);\n" +
            "    float diff = max(dot(norm, lightDir), 0.0);\n" +
            "    vec3 diffuse = diff * v_color.xyz;\n" +
            "    vec3 result = (gl_FragColor.xyz + diffuse) * c.xyz;\n" +
            "    gl_FragColor = vec4(result, 1.0);\n" +
            "    // depth\n" +
            "    float near = 0.1;\n" +
            "    float far  = 1000.0;\n" +
            "    float depth = LinearizeDepth(gl_FragCoord.z, near, far) / far;\n" +
            "    gl_FragColor = gl_FragColor - depth;\n" +
            "}";

    private int u_projTrans;
    private int u_worldTrans;
    int u_tex;

    @Override
    public void init() {
        program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        u_tex = program.getUniformLocation("s_diffuse");
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformi("u_texture", 0);
        program.setUniformf("spotCutoff", cutoffAngle);
        context.begin();
        context.setDepthTest(GL20.GL_LESS);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        if (init) super.init(program, renderable);
        init = false;
        Iterator<Attribute> matIter = renderable.material.iterator();
        if (matIter.hasNext()) {
            Attribute attribute = matIter.next();
            if (attribute instanceof TextureAttribute) {
                TextureAttribute textureAttribute = (TextureAttribute) attribute;
                Texture textureRegion = textureAttribute.textureDescription.texture;
                textureRegion.bind(0);
            }
        }
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        super.render(renderable);
    }

    @Override
    public void end() {
        super.end();
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
