package io.github.labyrinthgenerator.pages.game3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.filters.OverlapFilterManager;
import io.github.labyrinthgenerator.pages.game3d.input.GameInputProcessor;
import io.github.labyrinthgenerator.pages.game3d.managers.*;
import io.github.labyrinthgenerator.pages.game3d.maps.LMapBuilder;
import io.github.labyrinthgenerator.pages.game3d.models.ModelMaker;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.screens.MainMenuScreen;
import io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider;

public class CubeLab3D extends Game implements Page {
	private SpriteBatch batch;
	private ModelBatch mdlBatch;
	private FrameBuffer fbo;
	private AssetsManager assMan;
    private ChunkManager chunkMan;
	private EntityManager entMan;
	private RectManager rectMan;
    private TickManager nonPosMan;
	private ModelMaker cellBuilder;
	private OverlapFilterManager overlapFilterMan;
	private LMapBuilder mapBuilder;
    private MyShaderProvider shaderProvider;
    private GameInputProcessor gameInput;

	public boolean gameIsPaused = false;

	private float timeSinceLaunch = 0;

	public float currentAmbientVolume = 0.1f;
	public float currentSfxVolume = 0.25f;
	public float currentMusicVolume = 0.05f;

	@Override
	public void create() {
        // if you change the window size, then when you go to a new page the screen shifts to the left or to the down
        float blackScreenWidth = Gdx.graphics.getBackBufferWidth();
        float blackScreenHeight = Gdx.graphics.getBackBufferHeight();
        Constants.WINDOW_WIDTH = (int) blackScreenWidth;
        Constants.WINDOW_HEIGHT = (int) blackScreenHeight;

		createNewMainFbo(Constants.FBO_WIDTH_DELUXE, Constants.FBO_HEIGHT_DELUXE);

		assMan = new AssetsManager();
		assMan.finishLoading();

		overlapFilterMan = new OverlapFilterManager();

		cellBuilder = new ModelMaker(this); // builds models...

		entMan = new EntityManager();
		rectMan = new RectManager(this);
        nonPosMan = new TickManager();

        shaderProvider = new MyShaderProvider(this, false);

		mapBuilder = new LMapBuilder(this);

		Gdx.input.setInputProcessor(gameInput = new GameInputProcessor());

		setScreen(new MainMenuScreen(this));

        batch = new SpriteBatch();
        mdlBatch = new ModelBatch(shaderProvider);
	}

    @Override
    public void input() {}

    @Override
    public void logic() {}

    @Override
    public void draw() {
        render();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public Page getNextPage() {
        return null;
    }

    public void createNewMainFbo(final int width, final int height) {
		fbo = new FrameBuffer(Format.RGB888, width, height, true);
		fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}

    public void setChunkMan(ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
        entMan.setChunkMan(chunkMan);
        rectMan.setChunkMan(chunkMan);
    }

	@Override
	public void dispose() {
		getScreen().dispose();

		batch.dispose();
		mdlBatch.dispose();
		fbo.dispose();

		assMan.dispose();
        shaderProvider.dispose();
	}

    @Override
    public Camera getCamera() {
        return null;
    }

	public AssetsManager getAssMan() {
		return assMan;
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public ModelMaker getCellBuilder() {
		return cellBuilder;
	}

    public ChunkManager getChunkMan() {
        return chunkMan;
    }

    public EntityManager getEntMan() {
		return entMan;
	}

	public FrameBuffer getFbo() {
		return fbo;
	}

	public GameInputProcessor getGameInput() {
		return gameInput;
	}

	public LMapBuilder getMapBuilder() {
		return mapBuilder;
	}

	public ModelBatch getMdlBatch() {
		return mdlBatch;
	}

	public OverlapFilterManager getOverlapFilterMan() {
		return overlapFilterMan;
	}

	public RectManager getRectMan() {
		return rectMan;
	}

    public TickManager getTickMan() {
        return nonPosMan;
    }

	public float getTimeSinceLaunch() {
		return timeSinceLaunch;
	}

    public MyShaderProvider getShaderProvider() {
        return shaderProvider;
    }

    @Override
    public GameScreen getScreen() {
        return (GameScreen) screen;
    }

	@Override
	public void render() {
		timeSinceLaunch += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		getScreen().render(Gdx.graphics.getDeltaTime());

		gameInput.resetScrolled();
	}
}
