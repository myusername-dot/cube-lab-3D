package io.github.labyrinthgenerator.interfaces;

import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.debug.MyDebugRenderer;

public interface ApplicationFacade {
    FitViewport getViewport();

    MyDebugRenderer getDebugger();
}
