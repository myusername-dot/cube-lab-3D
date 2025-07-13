package io.github.labyrinthgenerator.pages.game3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.labyrinthgenerator.pages.game2d.Labyrinth2D;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityDir;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;

public class MainMenuScreen extends GameScreen {

    private final TextureRegion skyBg;
    private final TextureRegion guiTitle;
    private final BitmapFont guiFont64;
    private final BitmapFont guiFont32;
    private final GlyphLayout[] glyphLayouts;
    private final String[] options;
    private final GlyphLayout[] controlsGlyphLayouts;
    private final String[] controlsOptions;
    private int selectedOption = 0;
    private int fboOption = 0;
    private boolean displayControls = false;

    //private final Sound sfxAmbient;
    //private final long sfxAmbientId;
    private final Sound sfxChoice;

    public MainMenuScreen(final CubeLab3D game) {
        super(game);

        viewport = new StretchViewport(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        env = createEnvironment();
        skyBg = createSkyBackground(game);
        guiTitle = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiTitle));
        guiFont64 = game.getAssMan().get(game.getAssMan().font03_64);
        guiFont32 = game.getAssMan().get(game.getAssMan().font03_32);

        options = new String[]{
            "START GAME",
            "QUIT GAME",
            "CHANGE RENDER QUALITY",
            "DISPLAY CONTROLS"
        };
        glyphLayouts = createGlyphLayouts(options);

        controlsOptions = new String[]{
            "MOVE: ARROW KEYS",
            "ROTATE: MOUSE",
            "USE: E  INVENTORY: 1->6",
            "SPACE/ENTER/ESCAPE GO BACK"
        };
        controlsGlyphLayouts = createGlyphLayouts(controlsOptions);

        //sfxAmbient = game.getAssMan().get(game.getAssMan().sfxAmbientDark);
        sfxChoice = game.getAssMan().get(game.getAssMan().sfxItem);
        //sfxAmbientId = playAmbientSound(game);

        game.setScreen(this);
        game.getMapBuilder().buildMap(Labyrinth2D.txtFilename);

        GravityControls.currentGravity = GravityDir.DOWN;

        setupCamera();

        Gdx.input.setCursorCatched(false);
    }

    private TextureRegion createSkyBackground(final CubeLab3D game) {
        TextureRegion skyBg = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().bgSky01));
        skyBg.flip(false, true);
        return skyBg;
    }

    private GlyphLayout[] createGlyphLayouts(String[] options) {
        GlyphLayout[] layouts = new GlyphLayout[options.length];
        for (int i = 0; i < options.length; i++) {
            layouts[i] = new GlyphLayout(guiFont64, options[i]);
        }
        return layouts;
    }

    /*private long playAmbientSound(final CubeLab3D game) {
        long ambientId = sfxAmbient.play(game.currentAmbientVolume);
        sfxAmbient.setLooping(ambientId, true);
        return ambientId;
    }*/

    private void setupCamera() {
        Vector3 pos = getPlayerSpawnPosition().add(0, HALF_UNIT, 0);
        Vector3 lookAt = new Vector3(0, 0, -1);
        currentCam = new PerspectiveCamera(70, WINDOW_WIDTH, WINDOW_HEIGHT);
        setupCamera(currentCam, pos, lookAt);
        viewport.setCamera(currentCam);
    }

    @Override
    public void handleInput(final float delta) {
        super.handleInput(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            handleMenuSelection();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            handleEscapeKey();
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) && !displayControls) {
            changeSelectedOption(-1);
        }

        if ((Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) && !displayControls) {
            changeSelectedOption(1);
        }
    }

    private void handleMenuSelection() {
        sfxChoice.play(game.currentSfxVolume);
        if (!displayControls) {
            switch (selectedOption) {
                case 0:
                    //sfxAmbient.stop(sfxAmbientId);
                    removeAllEntities();
                    game.setScreen(new PlayScreen(game));
                    break;
                case 1:
                    Gdx.app.exit();
                    break;
                case 2:
                    changeRenderQuality();
                    break;
                case 3:
                    displayControls = true;
                    break;
                default:
                    break;
            }
        } else {
            displayControls = false;
        }
    }

    private void changeRenderQuality() {
        fboOption = (fboOption + 1) % 3;
        switch (fboOption) {
            case 0:
                game.createNewMainFbo(Constants.FBO_WIDTH_DELUXE, Constants.FBO_HEIGHT_DELUXE);
                break;
            case 1:
                game.createNewMainFbo(Constants.FBO_WIDTH_DECENT, Constants.FBO_HEIGHT_DECENT);
                break;
            case 2:
                game.createNewMainFbo(Constants.FBO_WIDTH_ORIGINAL, Constants.FBO_HEIGHT_ORIGINAL);
                break;
        }
    }

    private void handleEscapeKey() {
        if (!displayControls) {
            Gdx.app.exit();
        } else {
            displayControls = false;
        }
    }

    private void changeSelectedOption(int delta) {
        selectedOption = (selectedOption + delta + options.length) % options.length; // Изменяем на количество опций
        sfxChoice.play(game.currentSfxVolume);
    }

    @Override
    public void render(final float delta) {
        super.render(delta);

        currentCam.rotate(Vector3.Y, 6f * delta);
        currentCam.update();
        updateBackView();

        game.getFbo().begin();
        clearScreen();
        drawSkyBackground();
        game.getMdlBatch().begin(currentCam);
        game.getEntMan().render3DAllEntities(game.getMdlBatch(), env, delta, currentCam.position.cpy(), true);
        game.getMdlBatch().end();
        game.getFbo().end();

        drawFinalFbo();
        drawGui();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(fogColor.r, fogColor.g, fogColor.b, fogColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void drawSkyBackground() {
        game.getBatch().begin();
        game.getBatch().draw(skyBg, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.getBatch().end();
    }

    private void drawFinalFbo() {
        game.getBatch().begin();
        game.getBatch().draw(game.getFbo().getColorBufferTexture(), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.getBatch().draw(guiTitle, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.getBatch().end();
    }

    private void drawGui() {
        game.getBatch().begin();
        if (!displayControls) {
            drawMainMenuOptions();
        } else {
            drawControlInfo();
        }
        game.getBatch().end();
    }

    private void drawMainMenuOptions() {
        float baseY = 188;
        for (int i = 0; i < options.length; i++) {
            drawOption(glyphLayouts[i], i, options, baseY - i * 32, selectedOption == i);
        }
    }

    private void drawControlInfo() {
        float baseY = 188;
        for (int i = 0; i < controlsGlyphLayouts.length; i++) {
            drawOption(controlsGlyphLayouts[i], i, controlsOptions, baseY - i * 32, false);
        }
    }

    private void drawOption(GlyphLayout layout, int index, String[] options, float y, boolean isSelected) {
        float x = viewport.getWorldWidth() / 2f - layout.width / 2f;
        guiFont64.draw(game.getBatch(), options[index], x, y); // Используем текст из массива options
        if (isSelected) {
            guiFont64.draw(game.getBatch(), ">", x - 32, y);
        }
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
    }

    @Override
    public void tick(final float delta) {
        super.tick(delta);
    }
}
