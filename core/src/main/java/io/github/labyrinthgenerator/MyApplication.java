package io.github.labyrinthgenerator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game2d.Labyrinth2D;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class MyApplication extends ApplicationAdapter implements ApplicationFacade {

    public static final float lDivider = 10;
    public static final int windowW = 1080;
    public static final int windowH = (int) (windowW / 4f * 3f);

    public static final boolean saveAsTxt = true;
    public static final boolean saveAsImage = true;

    private static MyApplication application;

    private FitViewport viewport;

    private Page page;

    public static ApplicationFacade getApplicationInstance() {
        if (application == null) {
            application = new MyApplication();
        }
        return application;
    }

    private MyApplication() {
    }

    @Override
    public void create() {
        viewport = new FitViewport(windowW, windowH);
        viewport.setWorldSize(windowW, windowH);

        page = new Labyrinth2D();
        page.create();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        page.input();
        page.logic();
        page.draw();

        if (page.isFinished()) {
            page = page.getNextPage();
            page.create();
        }
    }

    @Override
    public void dispose() {
    }

    public Viewport getViewport() {
        return viewport;
    }
}

