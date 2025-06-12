package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.graphics.Camera;

public interface Page {

    Camera getCamera();

    void create();

    void input();

    void logic();

    void draw();

    boolean isFinished();

    Page getNextPage();

    void dispose();
}
