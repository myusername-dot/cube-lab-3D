package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.light.PointLightPlus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;
import static io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider.MAX_LIGHT_RENDERING_DISTANCE;

@Slf4j
public class FogFreeShader extends SpotLightFreeShader {

    public float fogBaseDensity = 1.0f;
    public float fogDistance = 1.0f;
    private float timer;

    protected final MyShaderProvider myShaderProvider;

    private static final String VERTEX_SHADER =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float spotCutoff;\n" +
            "uniform float fogDensity;\n" +
            "varying vec4 position;\n" +
            "varying vec4 position_a;\n" +
            "varying vec3 v_position;\n" +
            "varying vec2 v_texCoords;\n" +
            "varying vec4 spotColor;\n" +
            "varying float clip2Distance;\n" + // Расстояние до камеры
            "varying float fogClipDistanceFactor;\n" +
            "void main(void)\n" +
            "{\n" +
            "    gl_Position = u_projTrans * u_worldTrans * a_position;\n" +
            "    position = gl_Position;\n" +
            "    position_a = a_position;\n" +
            "    v_position = (u_worldTrans * a_position).xyz;\n" +
            "\n" +
            "    vec3 lightPosition  = (gl_Position).xyz;\n" +
            "    vec3 spotDirection  = normalize(lightPosition.xyz + vec3(0,0,1));\n" +
            "    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;\n" +
            "    vec3 lightDirection = normalize(vertex.xyz - lightPosition.xyz);\n" +
            "\n" +
            "    float angle = dot(spotDirection, -lightDirection);\n" +
            "    angle = max(angle, 0.0);\n" +
            "\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    spotColor = (acos(angle) < radians(spotCutoff)) ? vec4(1,1,0.1,1) : vec4(0.1,0.1,0.1,1);\n" +
            "\n" +
            "    clip2Distance = length(vec3(position.x, min(0.0, position.y * 4.0), position.z));\n" +
            "    fogClipDistanceFactor = (fogDensity * length(position) / 10);\n" +
            "}";

    private static final String FRAGMENT_SHADER =
        "#define NUM_LIGHTS " + MyShaderProvider.MAX_NUM_LIGHTS + "\n" +
            "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "varying LOWP vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform vec4 fogColor;\n" +
            "uniform float u_time;\n" +
            "uniform vec2 u_fogVelocity;\n" +
            "varying vec4 position;\n" + // Передаем gl_Position
            "varying vec4 position_a;\n" +
            "varying vec3 v_position;\n" +
            "varying vec4 spotColor;\n" +
            "varying float clip2Distance;\n" +
            "varying float fogClipDistanceFactor;\n" +
            "uniform vec3 u_normalTexture;\n" +
            "uniform vec3 u_pointLights[NUM_LIGHTS];\n" +
            "uniform vec3 u_pointLightsScreen[NUM_LIGHTS];\n" +
            "uniform vec4 u_pointLightColors[NUM_LIGHTS];\n" +
            "uniform int pointLightsSize;\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "   vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "   float heightFactor = max(0.0, position.y);\n" +
            "   float longWave = clamp(\n" +
            "       sin(position_a.x + position_a.z + u_time) * (max(-0.5, position.y) + 0.5) * 0.4, " +
            "   0.0, 1.0);\n" +
            "\n" +
            "   float fogFactor = clamp((fogClipDistanceFactor * sqrt(heightFactor) + heightFactor + longWave), 0.0, 0.8);\n" +
            "\n" +
            "   float radius = length(u_fogVelocity / 2.0);\n" +
            "   float shiftedRadius = (radius > 0) ? (sign(u_fogVelocity.y) > 0 ? 1.0 : 0.3) : 0;\n" +
            "   if (radius > 0) {\n" +
            "       shiftedRadius -= sign(u_fogVelocity.y) * radius / (shiftedRadius / 0.15);\n" +
            "   }\n" +
            "   if (clip2Distance / 1.5 < shiftedRadius) {\n" +
            "       fogFactor += clamp(\n" +
            "           sign(u_fogVelocity.y)\n" +
            "           * pow(\n" +
            "               shiftedRadius - clip2Distance / 1.5,\n" +
            "               (1.0 - sign(u_fogVelocity.y) * (radius / 2.0 - 0.5))\n" +
            "           )\n" +
            "           * heightFactor,\n" +
            "       -0.3, 0.3);\n" +
            "       fogFactor = clamp(fogFactor, 0.0, 1.0);\n" +
            "   }\n" +
            "\n" +
            "   gl_FragColor = mix(mix(mix(c, spotColor, 0.3), fogColor, fogFactor), spotColor, 0.1);\n" +
            "\n" +
            "   if (pointLightsSize > 0) {\n" +
            "       vec3 glowingColor = vec3(0.0);\n" +
            "       for (int i = 0; i < pointLightsSize; i++) {\n" +
            "           vec3 lightPos = u_pointLights[i];\n" +
            "           vec4 lightColor = u_pointLightColors[i];\n" +
            "           float intensity = 0.003;\n" +
            "           float distance = length(lightPos - v_position);\n" +
            "           float attenuation = intensity / (distance * distance + 0.0001);\n" +
            "           glowingColor += lightColor.rgb * attenuation;\n" +
            "           vec3 lightScreenPos = u_pointLightsScreen[i];\n" +
            "       }\n" +
            "       gl_FragColor.rgb += glowingColor;\n" +
            "   }\n" +
            "\n" +
            "   vec3 color = gl_FragColor.rgb;\n" +
            "   float saturation = 1.3;\n" + //  - heightFactor Устанавливаем желаемую насыщенность (1.0 - без изменений, больше 1 - увеличение, меньше 1 - уменьшение)
            "   vec3 gray = vec3(dot(color, vec3(0.299, 0.587, 0.114)));\n" + // Преобразуем в градацию серого (универсальный метод)
            "   color = mix(gray, color, saturation);\n" + // Интерполируем между серым и исходным цветом
            "   gl_FragColor = vec4(color, 1.0);\n" +
            "}";

    public FogFreeShader(MyShaderProvider myShaderProvider) {
        this.myShaderProvider = myShaderProvider;
    }

    @Override
    public void init() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
    }

    public void increaseTimer(float delta) {
        timer += delta;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        Vector2 playerVelocity = getPlayerVelocity();
        program.bind();
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformf("spotCutoff", cutoffAngle);
        program.setUniformi("u_texture", 0);
        setFogUniforms(playerVelocity);
        context.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    private Vector2 getPlayerVelocity() {
        Player player = myShaderProvider.getPlayer();
        if (player != null) {
            Vector3 playerVelocity3 = player.getForwardVelocity();
            Vector3 playerDir = player.getDirection();
            return new Vector2(playerVelocity3.x * sign(playerDir.x), playerVelocity3.z * sign(playerDir.z));
        }
        return new Vector2(0, 0);
    }

    private void setFogUniforms(Vector2 playerVelocity) {
        program.setUniformf("fogColor", new Color(0.5f, 0.5f, 0.5f, 0.9f));
        program.setUniformf("fogDensity", fogBaseDensity);
        program.setUniform2fv("u_fogVelocity", new float[]{playerVelocity.x, playerVelocity.y}, 0, 2);
        program.setUniformf("u_time", timer);
    }

    public void setPointLightsUniforms(Renderable renderable) {
        List<PointLightPlus> pointLights = getPointLights(renderable);
        if (pointLights.isEmpty()) {
            setDefaultLightUniforms();
        } else {
            setLightUniforms(pointLights);
        }
    }

    private List<PointLightPlus> getPointLights(Renderable renderable) {
        List<PointLightPlus> pointLights = new ArrayList<>();
        Player player = myShaderProvider.getPlayer();
        if (player != null) {
            Object userData = renderable.userData;
            if (userData instanceof Vector3) {
                Vector3 rendPos3 = (Vector3) userData;
                Vector2 rendPos2 = new Vector2(rendPos3.x, rendPos3.z);
                Vector3 playerPos3 = player.getPositionImmutable();
                float distance = rendPos2.dst(playerPos3.x, playerPos3.z);
                if (distance < MAX_LIGHT_RENDERING_DISTANCE) {
                    float angle = myShaderProvider.getViewAngle(player.playerCam, rendPos3);
                    pointLights = myShaderProvider.getPointLightsByPlayerDistAndCamAngle(distance, angle);
                }
            }
        }
        return pointLights;
    }

    private void setDefaultLightUniforms() {
        program.setUniform3fv("u_pointLights[0]", new float[]{0, 0, 0}, 0, 3);
        program.setUniform3fv("u_pointLightsScreen[0]", new float[]{0, 0, 0}, 0, 3);
        program.setUniform4fv("u_pointLightColors[0]", new float[]{0, 0, 0, 0}, 0, 4);
    }

    private void setLightUniforms(List<PointLightPlus> pointLights) {
        for (int i = 0; i < pointLights.size(); i++) {
            PointLightPlus light = pointLights.get(i);
            program.setUniform3fv("u_pointLights[" + i + "]",
                new float[]{light.position.x, light.position.y + HALF_UNIT, light.position.z}, 0, 3);
            program.setUniform3fv("u_pointLightsScreen[" + i + "]",
                new float[]{light.screenPosition.x, light.screenPosition.y, light.screenPosition.z}, 0, 3);
            program.setUniform4fv("u_pointLightColors[" + i + "]",
                new float[]{light.color.r, light.color.g, light.color.b, light.color.a}, 0, 4);
        }
        program.setUniformi("pointLightsSize", pointLights.size());
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        bindTexture(renderable);
        setPointLightsUniforms(renderable);
        renderable.meshPart.render(program);
    }

    private int sign(float v) {
        return (v >= 0) ? 1 : -1;
    }
}
