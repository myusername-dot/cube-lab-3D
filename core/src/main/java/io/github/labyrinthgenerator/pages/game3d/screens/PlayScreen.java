package io.github.labyrinthgenerator.pages.game3d.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.labyrinthgenerator.pages.game2d.Labyrinth2D;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.shaders.SkyBoxShaderProgram;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class PlayScreen extends GameScreen {
    private final TextureRegion skyBg;
    private final Environment env;
    private Color fogColor;

    private boolean showGuiMenu = false;
    private boolean showExitDistance = false;
    private int guiMenuSelection = 0;

    private final BitmapFont guiFont64;
    private final BitmapFont guiFont32;
    private final String headsupDead = "YOU DIED!";
    private final String optionContinue = "CONTINUE";
    private final String optionToMainMenu = "QUIT TO MAIN MENU";
    private final String selectedOptionMark = ">";
    private GlyphLayout glyphLayoutOptionContinue;
    private GlyphLayout glyphLayoutHeadsupDead;
    private GlyphLayout glyphLayoutOptionToMainMenu;

    private final Sound sfxItem;
    private long sfxItemId;

    private final SkyBoxShaderProgram envCubeMap;

    public PlayScreen(final CubeLab3D game) {
        super(game);
        viewport = new StretchViewport(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        env = createEnvironment();
        skyBg = createSkyBackground(game);
        guiFont64 = game.getAssMan().get(game.getAssMan().font03_64);
        guiFont32 = game.getAssMan().get(game.getAssMan().font03_32);

        createGlyphLayouts(); // Инициализация GlyphLayout

        sfxItem = game.getAssMan().get(game.getAssMan().sfxItem);
        game.getMapBuilder().buildMap(Labyrinth2D.txtFilename);

        Vector3 playerSpawnPosition = getPlayerSpawnPosition(game);
        player = new Player(playerSpawnPosition, HALF_UNIT / 2f, HALF_UNIT / 2f, this);
        setCurrentCam(player.playerCam);
        viewport.setCamera(currentCam);

        Gdx.input.setCursorCatched(true);
        envCubeMap = new SkyBoxShaderProgram(new Pixmap(Gdx.files.internal(game.getAssMan().bgSky01)));
    }

    private Environment createEnvironment() {
        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        fogColor = new Color(66 / 256f, 33 / 256f, 54 / 256f, 1f);
        return environment;
    }

    private TextureRegion createSkyBackground(final CubeLab3D game) {
        TextureRegion textureRegion = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().bgSky01));
        textureRegion.flip(false, true);
        return textureRegion;
    }

    private Vector3 getPlayerSpawnPosition(final CubeLab3D game) {
        return new Vector3(
            game.getMapBuilder().mapLoadSpawnPosition.x + HALF_UNIT - (HALF_UNIT / 2f) / 2f,
            0,
            game.getMapBuilder().mapLoadSpawnPosition.y + HALF_UNIT - (HALF_UNIT / 2f) / 2f
        );
    }

    private void createGlyphLayouts() {
        glyphLayoutOptionContinue = new GlyphLayout(guiFont64, optionContinue);
        glyphLayoutOptionToMainMenu = new GlyphLayout(guiFont64, optionToMainMenu);
        glyphLayoutHeadsupDead = new GlyphLayout(guiFont64, headsupDead);
    }

    @Override
    public void handleInput(final float delta) {
        super.handleInput(delta);

        if (player.isDead) {
            showGuiMenu = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            toggleGuiMenu();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            showExitDistance = !showExitDistance;
        }

        if (showGuiMenu) {
            handleGuiMenuInput();
        } else {
            game.gameIsPaused = false;
            if (!player.isDead) {
                player.handleInput(delta);
            }
        }
    }

    private void toggleGuiMenu() {
        showGuiMenu = !showGuiMenu;
        Gdx.input.setCursorCatched(!showGuiMenu);
        guiMenuSelection = 0;
    }

    private void handleGuiMenuInput() {
        game.gameIsPaused = true;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            guiMenuSelection--;
            limitGuiSelection();
            playItemSound();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            guiMenuSelection++;
            limitGuiSelection();
            playItemSound();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            playItemSound();
            handleMenuSelection();
        }
    }

    private void handleMenuSelection() {
        switch (guiMenuSelection) {
            case 0: // Continue
                Gdx.input.setCursorCatched(true);
                showGuiMenu = false;
                break;
            case 1: // Quit to Main Menu
                removeAllEntities();
                game.setScreen(new MainMenuScreen(game));
                break;
            default:
                break;
        }
    }

    private void limitGuiSelection() {
        if (guiMenuSelection < 0) {
            guiMenuSelection = 1;
        } else if (guiMenuSelection > 1) {
            guiMenuSelection = 0;
        }
    }

    public void playItemSound() {
        sfxItemId = sfxItem.play(game.currentSfxVolume);
    }

    @Override
    public void render(final float delta) {
        super.render(delta);
        currentCam.update();

        game.getFbo().begin();
        clearScreen();
        envCubeMap.render(currentCam);
        renderEntities(delta);
        game.getFbo().end();

        renderFinalFbo();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(fogColor.r, fogColor.g, fogColor.b, fogColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    private void renderEntities(float delta) {
        game.getMdlBatch().begin(currentCam);
        game.getEntMan().render3DAllEntities(game.getMdlBatch(), env, delta, currentCam.position.x, currentCam.position.z);
        game.getMdlBatch().end();
    }

    private void renderFinalFbo() {
        game.getBatch().begin();
        game.getBatch().draw(game.getFbo().getColorBufferTexture(), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        guiFont32.draw(game.getBatch(), "fps: " + Gdx.graphics.getFramesPerSecond(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 16);
        renderDebugInfo();
        renderGuiMenu();
        game.getBatch().end();
    }

    private void renderDebugInfo() {
        if (showExitDistance && !showGuiMenu) {
            guiFont32.draw(game.getBatch(), "velocity          : " + player.getVelocity(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 32);
            guiFont32.draw(game.getBatch(), "velocity forward: " + player.getVelocity(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 48);
            guiFont32.draw(game.getBatch(), "Exit distance: " + player.getExitDistance(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 64);
        }
    }

    private void renderGuiMenu() {
        if (showGuiMenu) {
            if (player.isDead) {
                float headsupDeadX = viewport.getWorldWidth() / 2f - glyphLayoutHeadsupDead.width / 2f;
                guiFont64.draw(game.getBatch(), headsupDead, headsupDeadX, 188 + 64);
            }

            drawMenuOption(optionContinue, glyphLayoutOptionContinue, 188, !player.isDead);
            drawMenuOption(optionToMainMenu, glyphLayoutOptionToMainMenu, 188 - 32, true);

            if (player.isDead) {
                guiMenuSelection = 1;
            }

            drawSelectedOptionMarker();
        }
    }

    private void drawMenuOption(String optionText, GlyphLayout layout, float y, boolean visible) {
        if (visible) {
            float optionX = viewport.getWorldWidth() / 2f - layout.width / 2f;
            guiFont64.draw(game.getBatch(), optionText, optionX, y);
        }
    }

    private void drawSelectedOptionMarker() {
        float optionContinueX = viewport.getWorldWidth() / 2f - glyphLayoutOptionContinue.width / 2f;
        float optionMainMenuX = viewport.getWorldWidth() / 2f - glyphLayoutOptionToMainMenu.width / 2f;

        switch (guiMenuSelection) {
            case 0:
                guiFont64.draw(game.getBatch(), selectedOptionMark, optionContinueX - 32, 188);
                break;
            case 1:
                guiFont64.draw(game.getBatch(), selectedOptionMark, optionMainMenuX - 32, 188 - 32);
                break;
            default:
                guiFont64.draw(game.getBatch(), selectedOptionMark, optionContinueX - 32, 188);
                break;
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

    public Environment getEnv() {
        return env;
    }
}
