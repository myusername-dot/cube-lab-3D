package io.github.labyrinthgenerator.pages.game3d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.filters.OverlapFilterManager;
import io.github.labyrinthgenerator.pages.game3d.input.GameInputProcessor;
import io.github.labyrinthgenerator.pages.game3d.managers.AssetsManager;
import io.github.labyrinthgenerator.pages.game3d.maps.LMapBuilder;
import io.github.labyrinthgenerator.pages.game3d.models.ModelMaker;
import io.github.labyrinthgenerator.pages.game3d.rect.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.rect.RectManager;
import io.github.labyrinthgenerator.pages.game3d.screens.MainMenuScreen;
import io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider;
import io.github.labyrinthgenerator.pages.game3d.utils.EntityManager;

public class CubeLab3D extends Game implements Page {
	private SpriteBatch batch;

	private ModelBatch mdlBatch;

	private FrameBuffer fbo;

	private AssetsManager assMan;

    private ChunkManager chunkMan;
	private EntityManager entMan;

	private RectManager rectMan;

	private ModelMaker cellBuilder;
	private OverlapFilterManager overlapFilterMan;
	private LMapBuilder mapBuilder;

	public boolean gameIsPaused = false;

	private float timeSinceLaunch = 0;

	private float currentAmbientVolume = 0.1f;

	private float currentSfxVolume = 0.25f;

	private float currentMusicVolume = 0.05f;
	private GameInputProcessor gameInput;

    private ShaderProvider shaderProvider;

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

        chunkMan = new ChunkManager();
		entMan = new EntityManager();
		rectMan = new RectManager(this);

		mapBuilder = new LMapBuilder(this);

		Gdx.input.setInputProcessor(gameInput = new GameInputProcessor());

//		setScreen(new PlayScreen(this));
		setScreen(new MainMenuScreen(this));

        batch = new SpriteBatch();
        shaderProvider = new MyShaderProvider(false);
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

	@Override
	public void dispose() {
		getScreen().dispose();

		batch.dispose();
		mdlBatch.dispose();
		fbo.dispose();

		assMan.dispose();
	}

    @Override
    public Camera getCamera() {
        return null;
    }

    public float getAmbientVolume() {
		return currentAmbientVolume;
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

	public float getMusicVolume() {
		return currentMusicVolume;
	}

	public OverlapFilterManager getOverlapFilterMan() {
		return overlapFilterMan;
	}

	public RectManager getRectMan() {
		return rectMan;
	}

	public float getSfxVolume() {
		return currentSfxVolume;
	}

	public float getTimeSinceLaunch() {
		return timeSinceLaunch;
	}

    public ShaderProvider getShaderProvider() {
        return shaderProvider;
    }

	@Override
	public void render() {
		timeSinceLaunch += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		getScreen().render(Gdx.graphics.getDeltaTime());

		gameInput.resetScrolled();
	}

	public void setAmbientVolume(final float currentAmbientVolume) {
		this.currentAmbientVolume = currentAmbientVolume;
	}

	public void setMusicVolume(final float newMusicVolume) {
		this.currentMusicVolume = newMusicVolume;
	}

	public void setSfxVolume(final float currentSfxVolume) {
		this.currentSfxVolume = currentSfxVolume;
	}
}
