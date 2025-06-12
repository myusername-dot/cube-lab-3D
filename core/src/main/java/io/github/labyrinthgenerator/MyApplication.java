package io.github.labyrinthgenerator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.pages.Game3dPage;
import io.github.labyrinthgenerator.pages.LabyrinthPage;
import io.github.labyrinthgenerator.pages.Page;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class MyApplication extends ApplicationAdapter implements ApplicationFacade {

    public static final float lDivider = 10;
    public static final int   windowW = 640 + (int) (lDivider / 4f);
    public static final int   windowH = 320 + (int) (lDivider / 4f);

    public static final boolean debug = true;

    public static final boolean saveAsTxt = true;
    public static final boolean saveAsImage = true;

    private static MyApplication application;

    private FitViewport viewport;

    private MyDebugRenderer debugger;

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

        debugger = new MyDebugRenderer();

        page = new LabyrinthPage();
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
    public void dispose() {}

    public FitViewport getViewport() {
        return viewport;
    }

    public MyDebugRenderer getDebugger() {
        return debugger;
    }
}

