package io.github.labyrinthgenerator.interfaces;

import com.badlogic.gdx.utils.viewport.Viewport;

public interface ApplicationFacade {
    Viewport getViewport();

    void resize(int width, int height);
}
