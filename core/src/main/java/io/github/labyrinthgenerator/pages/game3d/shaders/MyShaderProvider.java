package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

public class MyShaderProvider implements ShaderProvider {

    public SpotLightShader spotLightShader;
    public SpotLightFreeShader spotLightFreeShader;
    public FogFreeShader fogFreeShader;
    public DefaultShaderProvider defaultShaderProvider;

    public MyShaderProvider(CubeLab3D game, boolean defaultSpotLightEnabled) {
        spotLightShader = new SpotLightShader();
        spotLightShader.init();
        spotLightFreeShader = new SpotLightFreeShader();
        spotLightFreeShader.init();
        fogFreeShader = new FogFreeShader(game);
        fogFreeShader.init();
        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("shaders/def.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shaders/def.fragment.glsl").readString();
        if (defaultSpotLightEnabled) {
            config.vertexShader = "#define spotFlag\n\n" +  config.vertexShader;
            config.fragmentShader = "#define spotFlag\n\n" + config.fragmentShader;
        }
        defaultShaderProvider = new DefaultShaderProvider(config);
    }

    @Override
    public Shader getShader(Renderable renderable) {
        /*if (spotLightShader.canRender(renderable))
            return spotLightShader;
        else*/
            return defaultShaderProvider.getShader(renderable);
    }

    public Shader getShader() {
        return spotLightFreeShader;
    }

    @Override
    public void dispose() {
        spotLightShader.dispose();
        spotLightFreeShader.dispose();
        fogFreeShader.dispose();
        defaultShaderProvider.dispose();
    }
}
