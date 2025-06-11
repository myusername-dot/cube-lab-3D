package io.github.labyrinthgenerator.interfaces;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.debug.MyDebugRenderer;

public interface ApplicationFacade {
    SpriteBatch getSpriteBatch();

    FitViewport getViewport();

    OrthographicCamera getCamera();

    MyDebugRenderer getDebugger();
}
