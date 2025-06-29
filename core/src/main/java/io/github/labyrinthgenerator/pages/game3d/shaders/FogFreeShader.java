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

    public float fogBaseDensity = 1.0f; // Задайте базовую плотность тумана
    public float fogDistance = 1.0f; // Задайте дистанцию для изменения плотности
    private float timer;

    protected final MyShaderProvider myShaderProvider;

    public FogFreeShader(MyShaderProvider myShaderProvider) {
        this.myShaderProvider = myShaderProvider;
    }


    private final String vertexShader =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float spotCutoff;\n" +
            "uniform float fogDensity;\n" + // Плотность тумана
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
            "    clip2Distance = length(vec3(position.x, min(0.0, position.y * 4.0), position.z));\n" + // Вычисляем расстояние // Чем ниже фрагмент, тем больше плотность, y = 0 в центре камеры, положительные значения ниже
            "    fogClipDistanceFactor = (fogDensity * length(position) / 20);\n" + // Вычисляем фактор тумана // float fogClipDistanceFactor = exp(-fogDensity * v_distance);
            "}";

    private final String fragmentShader =
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
            "uniform vec3 u_pointLights[NUM_LIGHTS];\n" + // Массив для хранения позиций источников света
            "uniform vec3 u_pointLightsScreen[NUM_LIGHTS];\n" + // Массив для хранения позиций источников света на экране
            "uniform vec4 u_pointLightColors[NUM_LIGHTS];\n" + // Массив для хранения позиций источников света
            "uniform int pointLightsSize;\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "   vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "   float heightFactor = max(0.0, position.y);\n" + // Чем ниже фрагмент, тем больше плотность, y = 0 в центре камеры, положительные значения ниже
            "   float longWave = clamp(sin(position_a.x + position_a.z + u_time) * (max(-0.5, position.y) + 0.5) * 0.4, 0.0, 1.0);\n" + // Создание эффекта волн
            //"   float smallWave = sin((position_a.x * position_a.y * position_a.z) / 10 + u_time * 5) * heightFactor;\n" + // Создание эффекта волн
            "   \n" +
            "   float fogFactor = clamp((fogClipDistanceFactor + heightFactor + longWave), 0.0, 0.8);\n" +
            "   \n" +
            "   float radius = length(u_fogVelocity / 2.0);\n" + // 4.0 - players max move speed, max radius = 2
            //"   float radius = abs(u_fogVelocity.y / 2.0);\n" + // 4.0 - players max move speed, max radius = 2
            "   float shiftedRadius = 0;\n" +
            "   if (radius > 0) {\n" +
            "       if (sign(u_fogVelocity.y) > 0) shiftedRadius = 1.0;\n" +
            "       else shiftedRadius = 0.3;\n" +
            "       shiftedRadius -= sign(u_fogVelocity.y) * radius / (shiftedRadius / 0.15);\n" +
            "   }\n" +
            "   if (clip2Distance / 1.5 < shiftedRadius)\n" +
            "   {\n" +
            "       fogFactor += clamp(" +
            "           sign(u_fogVelocity.y)\n" +
            "               * pow(shiftedRadius - clip2Distance / 1.5, (1.0 - sign(u_fogVelocity.y) * (radius / 2.0 - 0.5)))\n" +
            "               * heightFactor,\n" +
            "           -0.3, 0.3);\n" +
            "       fogFactor = clamp(fogFactor, 0.0, 1.0);\n" +
            "   }\n" +
            "   \n" +
            "   gl_FragColor = mix(mix(mix(c, spotColor, 0.3), fogColor, fogFactor), spotColor, 0.1);\n" + // Интерполяция между цветом текстуры и цветом тумана
            "   \n" +
            "   \n" + // Point Lights
            "   if (pointLightsSize > 0)\n" +
            "   {\n" +
            "       vec3 glowingColor = vec3(0.0);\n" +
            "       for (int i = 0; i < pointLightsSize; i++) {\n" +
            "           vec3 lightPos = u_pointLights[i];\n" +
            "           vec4 lightColor = u_pointLightColors[i];\n" +
            "           float intensity = 0.003;\n" +
            "           \n" + // Расчет расстояния до источника света
            "           float distance = length(lightPos - v_position);\n" +
            "           \n" + // Расчет освещения (интенсивность уменьшается с увеличением расстояния)
            "           float attenuation = intensity / (distance * distance + 0.0001);\n" + // Добавляем небольшую константу для предотвращения деления на ноль
            "           \n" + // Добавляем освещение к финальному цвету
            "           glowingColor += lightColor.rgb * attenuation;\n" +
            "       }\n" +
            "       for (int i = 0; i < pointLightsSize; i++) {\n" +
            "           vec3 lightPos = u_pointLightsScreen[i];\n" +
            /*"           vec4 lightColor = u_pointLightColors[i];\n" +
            "           float intensity = 50;\n" +
            "           \n" + // Расчет расстояния до источника света
            "           float distance = length(lightPos.xy - gl_FragCoord.xy);\n" +
            "           \n" + // Расчет освещения (интенсивность уменьшается с увеличением расстояния)
            "           float attenuation = intensity / (distance * distance + 0.0001);\n" + // Добавляем небольшую константу для предотвращения деления на ноль
            "           \n" + // Добавляем освещение к финальному цвету
            "           glowingColor += lightColor.rgb * attenuation;\n" +*/
            "       }\n" +
            "       gl_FragColor.rgb += glowingColor;\n" +
            "   }\n" +
            "   \n" +
            "   vec3 color = gl_FragColor.rgb;\n" +
            "   float saturation = 1.3;\n" + //  - heightFactor Устанавливаем желаемую насыщенность (1.0 - без изменений, больше 1 - увеличение, меньше 1 - уменьшение)
            "   vec3 gray = vec3(dot(color, vec3(0.299, 0.587, 0.114)));\n" + // Преобразуем в градацию серого (универсальный метод)
            "   color = mix(gray, color, saturation);\n" + // Интерполируем между серым и исходным цветом
            "   gl_FragColor = vec4(color, 1.0);" + // Выводим цвет
            "}";


    @Override
    public void init() {
        program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
    }

    public void increaseTimer(float delta) {
        timer += delta;
    }

    private int sign(float v) {
        return (v >= 0) ? 1 : -1;
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
        Vector2 playerVelocity = new Vector2(0, 0);
        Player player = myShaderProvider.getPlayer();
        if (player != null) {
            Vector3 playerVelocity3 = player.getForwardVelocity();
            Vector3 playerDir = player.getDirection();
            playerVelocity.set(playerVelocity3.x * sign(playerDir.x), playerVelocity3.z * sign(playerDir.z));
        }
        return playerVelocity;
    }

    private void setFogUniforms(Vector2 playerVelocity) {
        // Устанавливаем цвет тумана и его плотность
        program.setUniformf("fogColor", new Color(0.5f, 0.5f, 0.5f, 0.9f)); // Цвет тумана (например, серый)
        program.setUniformf("fogDensity", fogBaseDensity); // Плотность тумана (можно настроить)

        float[] fogVelocity = new float[]{playerVelocity.x, playerVelocity.y};
        program.setUniform2fv("u_fogVelocity", fogVelocity, 0, 2);
        program.setUniformf("u_time", timer);
    }

    public void setPointLightsUniforms(Renderable renderable) {
        List<PointLightPlus> pointLightsByPlayerDistAndCamAngle = getPointLights(renderable);

        if (pointLightsByPlayerDistAndCamAngle.isEmpty()) {
            setDefaultLightUniforms();
        } else {
            setLightUniforms(pointLightsByPlayerDistAndCamAngle);
        }
    }

    private List<PointLightPlus> getPointLights(Renderable renderable) {
        List<PointLightPlus> pointLightsByPlayerDistAndCamAngle = new ArrayList<>();
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
                    pointLightsByPlayerDistAndCamAngle = myShaderProvider.getPointLightsByPlayerDistAndCamAngle(distance, angle);
                }
            }
        }
        return pointLightsByPlayerDistAndCamAngle;
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
}
