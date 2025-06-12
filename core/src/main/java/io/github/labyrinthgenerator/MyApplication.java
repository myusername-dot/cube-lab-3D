package io.github.labyrinthgenerator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.debug.MyDebugRenderer;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.pages.CreateLabyrinthPage;
import io.github.labyrinthgenerator.pages.Page;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class MyApplication extends ApplicationAdapter implements ApplicationFacade {

    public static final int windowW = 642;
    public static final int windowH = 322;

    public static final boolean debug = true;

    public static final boolean saveAsTxt = true;
    public static final boolean saveAsImage = true;

    private static MyApplication application;

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private OrthographicCamera camera;

    private MyDebugRenderer debugger;

    private Page page;

    public static ApplicationFacade getApplicationInstanceFacade() {
        if (application == null) {
            application = new MyApplication();
        }
        return application;
    }

    private MyApplication() {
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(windowW, windowH);
        camera = new OrthographicCamera(viewport.getWorldWidth(), viewport.getWorldHeight());
        viewport.setCamera(camera);

        debugger = new MyDebugRenderer();

        page = new CreateLabyrinthPage();
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
        page.draw(viewport, spriteBatch);

        if (page.isFinished()) {
            page = page.getNextPage();
            page.create();
        }
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public FitViewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public MyDebugRenderer getDebugger() {
        return debugger;
    }
}

