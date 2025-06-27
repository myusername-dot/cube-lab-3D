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
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.labyrinthgenerator.pages.game2d.Labyrinth2D;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.shaders.FogFreeShader;
import io.github.labyrinthgenerator.pages.game3d.shaders.MyShaderProvider;
import io.github.labyrinthgenerator.pages.game3d.shaders.SkyBoxShaderProgram;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;

public class PlayScreen extends GameScreen {
    private final TextureRegion skyBg;

    private final Environment env;

    private final Color fogColor;

    private boolean showGuiMenu = false;
    private boolean showExitDistance = false;
    private int guiMenuSelection = 0;

    private final BitmapFont guiFont01_64;
    private final BitmapFont guiFont01_32;
    private final BitmapFont guiFont02_16;
    private final String headsupDead = "YOU DIED!";
    private final String optionContinue = "CONTINUE";
    private final String optionToMainMenu = "QUIT TO MAIN MENU";
    private final String selectedOptionMark = ">";
    private String playerHp;
    private final GlyphLayout glyphLayoutOptionContinue;
    private final GlyphLayout glyphLayoutHeadsupDead;
    private final GlyphLayout glyphLayoutOptionToMainMenu;

    private final Sound sfxItem;
    private long sfxItemId;
    //private final Sound musicBackground;
    //private final long musicBackgroundId;

    private final SkyBoxShaderProgram envCubeMap;

    public PlayScreen(final CubeLab3D game) {
        super(game);

        viewport = new StretchViewport(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        fogColor = new Color(66 / 256f, 33 / 256f, 54 / 256f, 1f);
        //env.set(new ColorAttribute(ColorAttribute.Fog, fogColor));

        skyBg = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().bgSky01));
        skyBg.flip(false, true);

        guiFont01_64 = game.getAssMan().get(game.getAssMan().font03_64);
        guiFont01_32 = game.getAssMan().get(game.getAssMan().font03_32);
        guiFont02_16 = game.getAssMan().get(game.getAssMan().font02_16);
        glyphLayoutOptionContinue = new GlyphLayout(guiFont01_64, optionContinue);
        glyphLayoutOptionToMainMenu = new GlyphLayout(guiFont01_64, optionToMainMenu);
        glyphLayoutHeadsupDead = new GlyphLayout(guiFont01_64, headsupDead);

        sfxItem = game.getAssMan().get(game.getAssMan().sfxItem);
        //musicBackground = game.getAssMan().get(game.getAssMan().musicBackground01);

        game.getMapBuilder().buildMap(Labyrinth2D.txtFilename);

        float playerRectWidth = (HALF_UNIT / 2f);
        float playerRectDepth = (HALF_UNIT / 2f);
        Vector3 playerSpawnPosition = new Vector3(
            game.getMapBuilder().mapLoadSpawnPosition.x + HALF_UNIT - playerRectWidth / 2f,
            0,
            game.getMapBuilder().mapLoadSpawnPosition.y + HALF_UNIT - playerRectDepth / 2f
        );
        player = new Player(playerSpawnPosition, playerRectWidth, playerRectDepth, this);
        setCurrentCam(player.playerCam);
        viewport.setCamera(currentCam);

        Gdx.input.setCursorCatched(true);

        //musicBackgroundId = musicBackground.loop(game.getMusicVolume());

        envCubeMap = new SkyBoxShaderProgram(new Pixmap(Gdx.files.internal(game.getAssMan().bgSky01)));
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleInput(final float delta) {
        super.handleInput(delta);

        if (player.isDead) {
            showGuiMenu = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { // For easy quit while debugging.
            showGuiMenu = !showGuiMenu;
            Gdx.input.setCursorCatched(!showGuiMenu);
            guiMenuSelection = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) { // For easy quit while debugging.
            showExitDistance = !showExitDistance;
        }

        if (showGuiMenu) {
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
                switch (guiMenuSelection) {
                    case 0:
//					continue
                        Gdx.input.setCursorCatched(true);
                        showGuiMenu = false;
                        break;
                    case 1:
//					to menu screen
                        //musicBackground.stop(musicBackgroundId);

                        removeAllEntities();
                        game.setScreen(new MainMenuScreen(game));
                        break;
                    default:
                        break;
                }
            }
        } else {
            game.gameIsPaused = false;
        }

        if (!game.gameIsPaused) {
            if (!player.isDead) {
                player.handleInput(delta);
            }
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
        Gdx.gl.glClearColor(fogColor.r, fogColor.g, fogColor.b, fogColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        envCubeMap.render(currentCam);

        game.getMdlBatch().begin(currentCam);
        game.getEntMan().render3DAllEntities(game.getMdlBatch(), env, delta, currentCam.position.x, currentCam.position.z);
        game.getMdlBatch().end();
        game.getFbo().end();

//		render final fbo
        game.getBatch().begin();

        game.getBatch().draw(game.getFbo().getColorBufferTexture(), 0, 0, viewport.getWorldWidth(),
            viewport.getWorldHeight());

        guiFont01_32.draw(game.getBatch(), "fps: " + Gdx.graphics.getFramesPerSecond(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 16);

        if (showExitDistance && !showGuiMenu) {
            guiFont01_32.draw(game.getBatch(), "velocity          : " + player.getVelocity(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 32);
            guiFont01_32.draw(game.getBatch(), "velocity forward: " + player.getVelocity(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 48);


            guiFont01_32.draw(game.getBatch(), "Exit distance: " + player.getExitDistance(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 64);
        }

//		gui menu
        if (showGuiMenu) {
            if (player.isDead) {
                final float headsupDeadX = viewport.getWorldWidth() / 2f - glyphLayoutHeadsupDead.width / 2f;
                guiFont01_64.draw(game.getBatch(), headsupDead, headsupDeadX, 188 + 64);
            }

            final float optionContinueX = viewport.getWorldWidth() / 2f - glyphLayoutOptionContinue.width / 2f;
            if (!player.isDead) {
                guiFont01_64.draw(game.getBatch(), optionContinue, optionContinueX, 188);
            }

            final float optionMainMenuX = viewport.getWorldWidth() / 2f - glyphLayoutOptionToMainMenu.width / 2f;
            guiFont01_64.draw(game.getBatch(), optionToMainMenu, optionMainMenuX, 188 - 32);

            if (player.isDead) {
                guiMenuSelection = 1;
            }

            switch (guiMenuSelection) {
                case 0:
                    guiFont01_64.draw(game.getBatch(), selectedOptionMark, optionContinueX - 32, 188);
                    break;
                case 1:
                    guiFont01_64.draw(game.getBatch(), selectedOptionMark, optionMainMenuX - 32, 188 - 32);
                    break;
                default:
                    guiFont01_64.draw(game.getBatch(), selectedOptionMark, optionContinueX - 32, 188);
                    break;
            }
        }

        game.getBatch().end();
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
