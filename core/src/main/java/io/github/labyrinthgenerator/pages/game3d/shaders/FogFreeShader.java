package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.PlayScreen;

public class FogFreeShader extends SpotLightFreeShader {

    public float fogBaseDensity = 1.0f; // Задайте базовую плотность тумана
    //float heightFactor = 0.6f;
    public float fogDistance = 1.0f; // Задайте дистанцию для изменения плотности
    private float timer;

    protected final CubeLab3D game;

    public FogFreeShader(CubeLab3D game) {
        this.game = game;
    }


    private final String vertexShader =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat4 u_projTrans;\n" +
            "uniform float spotCutoff;\n" +
            "uniform float fogDensity;\n" + // Плотность тумана
            "varying vec4 position;\n" + // Передаем высоту
            "varying vec2 v_texCoords;\n" +
            "varying vec4 spotColor;\n" +
            "varying float v_distance;\n" + // Расстояние до камеры
            "varying float fogDistanceFactor;\n" +
            "void main(void)\n" +
            "{\n" +
            "    gl_Position = u_projTrans * u_worldTrans * a_position;\n" +
            "    position = gl_Position;\n" +
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
            "    v_distance = length(vec3(gl_Position.x, min(0.0, gl_Position.y * 4.0), gl_Position.z));\n" + // Вычисляем расстояние // Чем ниже фрагмент, тем больше плотность, y = 0 в центре камеры, положительные значения ниже
            "    fogDistanceFactor = (fogDensity * v_distance / 20);\n" + // Вычисляем фактор тумана // float fogDistanceFactor = exp(-fogDensity * v_distance);
            "}";

    private final String fragmentShader =
        "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "varying LOWP vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform vec4 fogColor;\n" + // Цвет тумана
            "uniform float u_time;\n" +
            //"uniform vec3 u_fogVelocity;\n" + // Скорость тумана
            "varying vec4 position;\n" + // Передаем gl_Position
            "varying vec4 spotColor;\n" +
            "varying float v_distance;\n" +
            "varying float fogDistanceFactor;\n" +
            "void main(void)\n" +
            "{\n" +
            "   \n" +
            "   vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "   float heightFactor = max(0.0, position.y);\n" + // Чем ниже фрагмент, тем больше плотность, y = 0 в центре камеры, положительные значения ниже
            "   float wave = sin(position.x + position.z + u_time) * 0.3;\n" + // Создание эффекта волн
            "   float fogFactor = clamp((fogDistanceFactor + heightFactor + wave) * 1.0, 0.0, 0.5);\n" +
            "   if (v_distance < 1) fogFactor += (1 - v_distance);\n" +
            "   gl_FragColor = mix(mix(c, spotColor, 0.5), fogColor, fogFactor);\n" + // Интерполяция между цветом текстуры и цветом тумана
            "}";


    @Override
    public void init() {
        program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        u_projTrans = program.getUniformLocation("u_projTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
    }

    public void increaseTimer(float delta) {
        timer += delta;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;

        Vector3 playerVelocity = new Vector3(0, 0, 0);
        if (game.getScreen() instanceof PlayScreen) {
            Player player = ((PlayScreen) game.getScreen()).getPlayer();
            playerVelocity = player.getVelocity();
        }

        program.bind();
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformf("spotCutoff", cutoffAngle);
        program.setUniformi("u_texture", 0);

        // Устанавливаем цвет тумана и его плотность
        program.setUniformf("fogColor", new Color(0.9f, 0.9f, 0.9f, 0.9f)); // Цвет тумана (например, серый)
        program.setUniformf("fogDensity", fogBaseDensity); // Плотность тумана (можно настроить)

        float[] fogVelocity = new float[]{playerVelocity.x, playerVelocity.y, playerVelocity.z};
        //program.setUniform3fv("u_fogVelocity", fogVelocity, 0, 3);
        program.setUniformf("u_time", timer);

        context.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }
}

/*

        // Установите текстуру снега
        //program.setUniformi("u_snowTexture", 1);
        //snowTexture.bind(1); // Привязываем текстуру снега

String fragmentShader =
        "#ifdef GL_ES \n" +
            "#define LOWP lowp\n" +
            "precision mediump float;\n" +
            "#else\n" +
            "#define LOWP \n" +
            "#endif\n" +
            "varying LOWP vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            //"uniform sampler2D u_snowTexture;\n" + // Текстура снега
            "varying float v_distance;\n" +
            "uniform vec4 fogColor;\n" + // Цвет тумана
            "uniform float fogDensity;\n" +
            //"uniform float snowIntensity;\n" + // Интенсивность снега
            //"uniform float time;\n" +  // Добавляем uniform для времени
            "void main(void)\n" +
            "{\n" +
            "   vec4 c = texture2D(u_texture, v_texCoords);\n" +
            "   float fogFactor = exp(-fogDensity * v_distance);\n" +
            "   fogFactor = clamp(fogFactor, 0.0, 1.0);\n" +
            "   gl_FragColor = mix(c, fogColor, 1.0 - fogFactor);\n" +

            // Добавление снега
            "   vec2 snowCoords = vec2(mod(gl_FragCoord.x, 1.0), mod(gl_FragCoord.y - time * 0.1, 1.0));\n" + // Падающий снег
            "   vec4 snow = texture2D(u_snowTexture, snowCoords);\n" +
            "   if (snow.a > 0.0) {\n" + // Если есть снег
            "       gl_FragColor = mix(gl_FragColor, snow * snowIntensity, snow.a);\n" + // Смешиваем снег с цветом
            "   }\n" +
            "}";*/

/*
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.Texture;

public class SnowflakeModel {
    public static Model createSnowflakeModel(Texture snowTexture) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        // Задаем материал с текстурой снега
        Material material = new Material(TextureAttribute.createDiffuse(snowTexture));

        // Создаем квадрат (плоскость) для снежинки
        modelBuilder.part("snowflake",
                          com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo(),
                          com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.PrimitiveType.TriangleFan,
                          material)
                     .rect(-0.1f, 0, -0.1f, 0, 1, 0, 0, 0, 1, // левая вершина
                           0.1f, 0, -0.1f, 0, 1, 0, 1, 0, 1, // правая вершина
                           0.1f, 0, 0.1f, 0, 1, 0, 1, 1, 1, // верхняя вершина
                           -0.1f, 0, 0.1f, 0, 1, 0, 0, 1, 1); // нижняя вершина

        return modelBuilder.end();
    }
}

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

public class SnowflakeManager {
    private List<ModelInstance> snowflakes;
    private Model snowflakeModel;

    public SnowflakeManager(Model snowflakeModel) {
        this.snowflakeModel = snowflakeModel;
        this.snowflakes = new ArrayList<>();
        createSnowflakes(100); // Создаем 100 снежинок
    }

    private void createSnowflakes(int count) {
        for (int i = 0; i < count; i++) {
            // Создаем новую снежинку
            ModelInstance snowflakeInstance = new ModelInstance(snowflakeModel);
            // Размещаем снежинку в случайной позиции
            snowflakeInstance.transform.setTranslation(
                (float) Math.random() * 10 - 5, // X
                (float) Math.random() * 10 + 5, // Y (выше камеры)
                (float) Math.random() * 10 - 5  // Z
            );
            snowflakes.add(snowflakeInstance);
        }
    }

    public void update(float deltaTime) {
        for (ModelInstance snowflake : snowflakes) {
            // Падаем вниз
            Vector3 position = new Vector3();
            snowflake.transform.getTranslation(position);
            position.y -= deltaTime; // Скорость падения
            // Если снежинка вышла за пределы, перемещаем её обратно
            if (position.y < 0) {
                position.y = 10; // Возвращаем на высоту
            }
            snowflake.transform.setTranslation(position);
        }
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        for (ModelInstance snowflake : snowflakes) {
            modelBatch.render(snowflake, environment);
        }
    }
}

// В Вашем классе Game
SnowflakeManager snowflakeManager;

// В методе create()
snowflakeManager = new SnowflakeManager(SnowflakeModel.createSnowflakeModel(snowTexture));

// В методе render()
snowflakeManager.update(deltaTime);
snowflakeManager.render(modelBatch, environment);

* */
