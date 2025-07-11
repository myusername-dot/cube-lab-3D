package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.labyrinth.Pair;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.PlayScreen;
import io.github.labyrinthgenerator.pages.game3d.tickable.Tickable;
import io.github.labyrinthgenerator.pages.light.PointLightPlus;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class MyShaderProvider extends Tickable implements ShaderProvider {

    public static final int MAX_NUM_LIGHTS = 20;
    public static final float MAX_LIGHT_RENDERING_DISTANCE = 10f;

    public SpotLightShader spotLightShader;
    public SpotLightFreeShader spotLightFreeShader;
    public FogFreeShader fogFreeShader;
    public DefaultShaderProvider defaultShaderProvider;

    private final TreeMap<Float, Pair<PointLightPlus, Float>> pointLightsByPlayerDistAndCamAngle = new TreeMap<>();

    public MyShaderProvider(CubeLab3D game, boolean defaultSpotLightEnabled) {
        super(game);
        shouldClear = false;

        spotLightShader = new SpotLightShader();
        spotLightShader.init();
        spotLightFreeShader = new SpotLightFreeShader();
        spotLightFreeShader.init();
        fogFreeShader = new FogFreeShader(this);
        fogFreeShader.init();
        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("shaders/def.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shaders/def.fragment.glsl").readString();
        if (defaultSpotLightEnabled) {
            config.vertexShader = "#define spotFlag\n\n" + config.vertexShader;
            config.fragmentShader = "#define spotFlag\n\n" + config.fragmentShader;
        }
        defaultShaderProvider = new DefaultShaderProvider(config);
    }

    public Player getPlayer() {
        if (game.getScreen() instanceof PlayScreen) {
            return ((PlayScreen) game.getScreen()).getPlayer();
        }
        return null;
    }

    public float getViewAngle(final PerspectiveCamera camera, final Vector3 point) {
        // Получаем направление от камеры до точки
        Vector3 directionToPoint = point.cpy().sub(camera.position).nor();

        // Получаем направление камеры
        Vector3 cameraDirection = camera.direction.cpy().nor();

        // Вычисляем угол между направлением камеры и направлением к точке
        return cameraDirection.dot(directionToPoint);
    }

    public void setNearestPointLights(final Player player) {
        pointLightsByPlayerDistAndCamAngle.clear();
        player.playerCam.update();

        Vector3 playerPos3 = player.getPositionImmutable();
        List<Entity> nearestEntities = game.getEntMan().getNearestEntities(playerPos3.x, playerPos3.z);
        List<PointLightPlus> pointLights = nearestEntities.stream()
            .map(Entity::getPointLight)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        for (PointLightPlus pointLight : pointLights) {
            Vector2 pointLightPos2 = new Vector2(pointLight.position.x, pointLight.position.z);
            float distance = pointLightPos2.dst(playerPos3.x, playerPos3.z);
            if (distance < MAX_LIGHT_RENDERING_DISTANCE) {
                // Угол между направлением камеры и точкой
                float angle = getViewAngle(player.playerCam, pointLight.position);
                // Угол обзора камеры в радианах
                float fov = player.playerCam.fieldOfView * 0.5f; // делим на 2, чтобы получить половину угла
                // Преобразуем угол в радианы
                float cosFov = (float) Math.cos(Math.toRadians(fov));

                // Проверяем, находится ли точка в пределах угла обзора
                if (angle > cosFov) {
                    pointLight.calculateScreenTransforms(player.playerCam);
                    pointLightsByPlayerDistAndCamAngle.put(distance, new Pair<>(pointLight, angle));
                }
            }
        }
    }

    public List<PointLightPlus> getPointLightsByPlayerDistAndCamAngle(final float distance, final float angle) {

        return pointLightsByPlayerDistAndCamAngle.subMap(distance - HALF_UNIT * 3, distance + HALF_UNIT * 3).values()
            .stream()
            .filter(p -> Math.abs(angle - p.snd) < 0.2)
            .sorted((e1, e2) -> Float.compare(Math.abs(angle - e1.snd), Math.abs(angle - e2.snd)))
            .limit(MAX_NUM_LIGHTS)
            .map(p -> p.fst)
            .collect(Collectors.toList());
    }

    @Override
    public void tick(final float delta) {
        Player player = getPlayer();
        if (player != null) {
            setNearestPointLights(player);
        }
    }

    public void clear() {
        pointLightsByPlayerDistAndCamAngle.clear();
    }

    @Override
    public Shader getShader(Renderable renderable) {
        /*if (spotLightShader.canRender(renderable))
            return spotLightShader;
        else*/
        return defaultShaderProvider.getShader(renderable);
    }

    public Shader getShader() {
        return fogFreeShader;
    }

    @Override
    public void dispose() {
        spotLightShader.dispose();
        spotLightFreeShader.dispose();
        fogFreeShader.dispose();
        defaultShaderProvider.dispose();
    }
}
