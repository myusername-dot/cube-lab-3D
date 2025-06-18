package io.github.labyrinthgenerator.pages.game3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class MyShaderProvider implements ShaderProvider {

    public SpotLightShader spotLightShader;
    public SpotLightFreeShader spotLightFreeShader;
    public DefaultShaderProvider defaultShaderProvider;

    public MyShaderProvider(boolean spotLightEnabled) {
        spotLightShader = new SpotLightShader();
        spotLightShader.init();
        spotLightFreeShader = new SpotLightFreeShader();
        spotLightFreeShader.init();
        DefaultShader.Config config = new DefaultShader.Config();
        config.vertexShader = Gdx.files.internal("shaders/def.vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shaders/def.fragment.glsl").readString();
        if (spotLightEnabled) {
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
        defaultShaderProvider.dispose();
    }
}
