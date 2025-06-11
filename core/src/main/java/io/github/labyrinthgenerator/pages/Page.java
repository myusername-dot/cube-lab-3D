package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public interface Page {

    void create();

    void input();

    void logic();

    void draw(FitViewport viewport, SpriteBatch spriteBatch);

    boolean isFinished();

    Page getNextPage();
}
