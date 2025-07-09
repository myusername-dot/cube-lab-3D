package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.mesh.NormalMapAttribute;
import io.github.labyrinthgenerator.pages.light.PointLightPlus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider.MAX_LIGHT_RENDERING_DISTANCE;

@Slf4j
public class FogFreeShader extends SpotLightFreeShader {

    public float fogBaseDensity = 1.0f;
    public float fogDistance = 1.0f;
    private float timer;

    private final boolean lightingFlag;

    protected final MyShaderProvider myShaderProvider;

    private final String VERTEX_SHADER =
        "attribute vec4 a_position;\n" +
            "attribute vec3 a_normal;\n" +
            "attribute vec2 a_texCoord0;\n" + // texture
            //"attribute vec2 a_texCoord2;\n" + // normal
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float u_spotCutoff;\n" +
            "uniform float u_fogDensity;\n" +
            "varying vec4 position;\n" + // gl_Position
            "varying vec4 aPosition;\n" + // a_position
            "varying vec3 worldPosition;\n" + // (u_worldTrans * a_position).xyz
            "varying vec3 v_normal;\n" +
            "varying vec2 v_texCoords0;\n" +
            //"varying vec2 v_texCoords2;\n" +
            "varying vec4 spotColor;\n" +
            "varying float clip2Distance;\n" + // Расстояние до камеры
            "varying float fogClipDistanceFactor;\n" +
            "void main(void)\n" +
            "{\n" +
            "    gl_Position = u_projTrans * u_worldTrans * a_position;\n" +
            "    position = gl_Position;\n" +
            "    aPosition = a_position;\n" +
            "    v_normal = a_normal;\n" +
            "    worldPosition = (u_worldTrans * a_position).xyz;\n" +
            "\n" +
            "\n" + // Вычисляем расстояние до камеры
            "    clip2Distance = length(vec3(position.x, min(0.0, position.y * 4.0), position.z));\n" + // todo FIXME
            "\n" + // Вычисляем фактор плотности тумана
            "    fogClipDistanceFactor = (u_fogDensity * length(position) / 16);\n" +
            "\n" +
            "    vec3 lightPosition  = (position).xyz;\n" +
            "    vec3 spotDirection  = normalize(lightPosition + vec3(0,0,1));\n" +
            "    vec4 vertex = gl_ModelViewMatrix * gl_Vertex;\n" +
            "    vec3 lightDirection = normalize(vertex.xyz - lightPosition);\n" +
            "\n" +
            "    float angle = dot(spotDirection, -lightDirection);\n" +
            "    angle = max(angle, 0.0);\n" +
            "\n" +
            "    v_texCoords0 = a_texCoord0;\n" +
            //"    v_texCoords2 = a_texCoord2;\n" +
            "    spotColor = (acos(angle) < radians(u_spotCutoff)) ? vec4(1,1,0.1,1) : vec4(0.1,0.1,0.1,1);\n" +
            "}";


    private String FRAGMENT_SHADER =
        "#define NUM_LIGHTS " + MyShaderProvider.MAX_NUM_LIGHTS + "\n" +
            "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "\n" +
            "varying LOWP vec2 v_texCoords0;\n" +
            //"varying LOWP vec2 v_texCoords2;\n" +
            "uniform sampler2D u_texture;\n" +
            //"uniform sampler2D u_normalMap;\n" +
            "uniform samplerCube u_cubemap;\n" +
            "uniform bool u_isReflective;" +
            "uniform vec3 u_cameraPosition;\n" +
            "uniform vec4 u_fogColor;\n" +
            "uniform vec2 u_fogVelocity;\n" +
            "uniform float u_time;\n" +
            "\n" +
            "varying vec4 position;\n" + // Передаем gl_Position
            "varying vec4 aPosition;\n" + // position_a
            "varying vec3 worldPosition;\n" + // (u_worldTrans * a_position).xyz
            "varying vec3 v_normal;\n" +
            "varying vec4 spotColor;\n" +
            "varying float clip2Distance;\n" + // Расстояние до камеры
            "varying float fogClipDistanceFactor;\n" +
            "\n" +
            "#if defined(lightingFlag)\n" +
            "uniform vec3 u_pointLights[NUM_LIGHTS];\n" +
            "uniform float u_pointLightsDistance[NUM_LIGHTS];\n" +
            "uniform vec3 u_pointLightsScreen[NUM_LIGHTS];\n" +
            "uniform vec4 u_pointLightColors[NUM_LIGHTS];\n" +
            "uniform int u_pointLightsSize;\n" +
            "#endif\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec4 c = vec4(0);\n" +
            "    if (u_isReflective) {\n" +
            "\n" + // Преобразование в диапазон [-1, 1]
            "\n" + // normalize(texture(u_normalMap, v_texCoords2).xyz * 2.0 - 1.0)
            "       vec3 N = normalize(v_normal);\n" +
            "       vec3 V = normalize(u_cameraPosition - worldPosition);\n" +
            "       vec3 R = reflect(V, N);\n" + // + 0.1 * normalize(N)
            "       c = texture(u_cubemap, R);\n" + // c *= 1.0 - smoothstep(0.0, 1.0, length(R));
            "    } else {\n" +
            "       c = texture2D(u_texture, v_texCoords0);" +
            "    }\n" +
            "\n" +
            //"    float heightFactor = max(0.0, position.y);\n" +
            "    float heightFactor = max(0.0, (worldPosition.y - 0.5) * 1.6);\n" + // 0..1 -> 0..0.5 * 1.6
            "\n" +
            "    float longWave = clamp(\n" +
            //"        sin(aPosition.x + aPosition.z + u_time) * (max(-0.5, position.y) + 0.5) * 0.4, \n" + // 0.5..-0.5
            "        sin(aPosition.x + aPosition.z + u_time) * sqrt(heightFactor) * 0.5, \n" +
            "    0.0, 1.0);\n" +
            "\n" +
            "    float fogFactor = clamp((fogClipDistanceFactor * sqrt(heightFactor) + heightFactor + longWave), 0.0, 0.8);\n" +
            "\n" +
            "\n" + // Логика для смещения радиуса тумана
            "    float radius = length(u_fogVelocity / 2.0);\n" +
            "    float shiftedRadius = (radius > 0) ? (sign(u_fogVelocity.y) > 0 ? 1.0 : 0.3) : 0;\n" +
            "    if (radius > 0) {\n" +
            "        shiftedRadius -= sign(u_fogVelocity.y) * radius / (shiftedRadius / 0.15);\n" +
            "    }\n" +
            "\n" +
            "\n" + // Логика для добавления эффекта тумана
            "    if (clip2Distance / 1.5 < shiftedRadius) {\n" +
            "        fogFactor += clamp(\n" +
            "            sign(u_fogVelocity.y)\n" +
            "            * pow(\n" +
            "                shiftedRadius - clip2Distance / 1.5,\n" +
            "                (1.0 - sign(u_fogVelocity.y) * (radius / 2.0 - 0.5))\n" +
            "            )\n" +
            "            * heightFactor,\n" +
            "        -0.3, 0.3);\n" +
            "        fogFactor = clamp(fogFactor, 0.0, 1.0);\n" +
            "    }\n" +
            "\n" +
            "\n" + // Основной цвет
            "    if (!u_isReflective) " +
            "        gl_FragColor = mix(mix(mix(c, spotColor, 0.3), u_fogColor, fogFactor), spotColor, 0.1);\n" +
            "    else\n" +
            "        gl_FragColor = mix(c, u_fogColor, fogFactor);\n" +
            "\n" +
            "\n" + // Обработка точечных источников света
            "#if defined(lightingFlag)\n" +
            "    vec3 glowingColor = vec3(0.0);\n" +
            "    for (int i = 0; i < u_pointLightsSize; i++) {\n" +
            "        vec4 lightColor = u_pointLightColors[i];\n" +
            "        vec3 lightPos = u_pointLights[i];\n" +
            "        float intensity = 0.003;\n" +
            "        float distanceWall15 = pow(length(lightPos - worldPosition), 1.5);\n" +
            "        float attenuation = intensity / (distanceWall15 + 0.0001);\n" +
            "        vec3 lightPosScreen = u_pointLightsScreen[i];\n" +
            "        float distance = u_pointLightsDistance[i];\n" +
            "        if (distance > 0.5 && fogFactor > 0) {\n" +
            "            float screenDistance = length(lightPosScreen.xy - gl_FragCoord.xy);\n" +
            "            attenuation += intensity * 1000.0 / (screenDistance * distance + 0.0001)" +
            "               * fogFactor;\n" + // fog glowing
            "        }\n" +
            "        glowingColor += lightColor.rgb * attenuation;\n" +
            "    }\n" +
            "    gl_FragColor.rgb += glowingColor;\n" +
            "#endif\n" +
            "\n" +
            "\n" + // Коррекция цвета
            "    vec3 color = gl_FragColor.rgb;\n" +
            "    float saturation = 1.3;\n" + // Устанавливаем желаемую насыщенность
            "    vec3 gray = vec3(dot(color, vec3(0.299, 0.587, 0.114)));\n" + // Преобразуем в градацию серого
            "    color = mix(gray, color, saturation);\n" + // Интерполируем между серым и исходным цветом
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}\n";

    public FogFreeShader(MyShaderProvider myShaderProvider, boolean lightingFlag) {
        this.lightingFlag = lightingFlag;
        this.myShaderProvider = myShaderProvider;
        //cutoffAngle = 50f;
    }

    @Override
    public void init() {
        if (lightingFlag) {
            FRAGMENT_SHADER = "#define lightingFlag\n\n" + FRAGMENT_SHADER;
        }
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        Vector2 playerVelocity = getPlayerVelocity();
        program.bind();
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformi("u_texture", 0);
        program.setUniformi("u_cubemap", 1);
        //program.setUniformi("u_normalMap", 2);
        program.setUniformf("u_spotCutoff", cutoffAngle);
        setFogUniforms(playerVelocity);
        float[] cameraPosition = new float[]{camera.position.x, camera.position.y, camera.position.z};
        program.setUniform3fv("u_cameraPosition", cameraPosition, 0, 3);
        context.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    private void setDefaultLightUniforms() {
        program.setUniform3fv("u_pointLights[0]", new float[]{0, 0, 0}, 0, 3);
        program.setUniform3fv("u_pointLightsDistance[0]", new float[]{0, 0, 0}, 0, 3);
        program.setUniform3fv("u_pointLightsScreen[0]", new float[]{0, 0, 0}, 0, 3);
        program.setUniform4fv("u_pointLightColors[0]", new float[]{0, 0, 0, 0}, 0, 4);
    }

    private void setFogUniforms(Vector2 playerVelocity) {
        program.setUniformf("u_fogColor", new Color(0.5f, 0.5f, 0.5f, 0.5f));
        program.setUniformf("u_fogDensity", fogBaseDensity);
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

    private void setLightUniforms(List<PointLightPlus> pointLights) {
        for (int i = 0; i < pointLights.size(); i++) {
            PointLightPlus light = pointLights.get(i);
            program.setUniform3fv("u_pointLights[" + i + "]",
                new float[]{light.position.x, light.position.y, light.position.z}, 0, 3);
            program.setUniformf("u_pointLightsDistance[" + i + "]", light.camDistance);
            program.setUniform3fv("u_pointLightsScreen[" + i + "]",
                new float[]{light.screenPosition.x, light.screenPosition.y, light.screenPosition.z}, 0, 3);
            program.setUniform4fv("u_pointLightColors[" + i + "]",
                new float[]{light.color.r, light.color.g, light.color.b, light.color.a}, 0, 4);
        }
        program.setUniformi("u_pointLightsSize", pointLights.size());
    }

    @Override
    public void bindTexture(Renderable renderable) {

        for (Attribute attribute : renderable.material) {
            if (attribute instanceof NormalMapAttribute) {
                NormalMapAttribute normalAttribute = (NormalMapAttribute) attribute;
                Texture normalTexture = normalAttribute.textureDescription.texture;
                normalTexture.bind(0); // 2 FIXME
                continue;
            }

            if (attribute instanceof CubemapAttribute) {
                CubemapAttribute cubemapAttribute = (CubemapAttribute) attribute;
                Cubemap cubemap = cubemapAttribute.textureDescription.texture;
                cubemap.bind(1);
                program.setUniformi("u_isReflective", 1);
            }

            if (attribute instanceof TextureAttribute) {
                TextureAttribute textureAttribute = (TextureAttribute) attribute;
                Texture texture = textureAttribute.textureDescription.texture;
                texture.bind(0);
            }
        }
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformi("u_isReflective", 0); // !drop batch flag
        bindTexture(renderable);
        if (lightingFlag) setPointLightsUniforms(renderable);
        renderable.meshPart.render(program);
    }

    private Vector2 getPlayerVelocity() {
        Player player = myShaderProvider.getPlayer();
        if (player != null) {
            Vector2 playerVelocity = player.getHorizontalForwardVelocity();
            Vector3 playerDir = player.getDirection();
            return new Vector2(playerVelocity.x * sign(playerDir.x), playerVelocity.y * sign(playerDir.z));
        }
        return new Vector2(0, 0);
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

    public void increaseTimer(float delta) {
        timer += delta;
    }

    private int sign(float v) {
        return (v >= 0) ? 1 : -1;
    }
}
