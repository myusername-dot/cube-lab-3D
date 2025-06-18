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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import io.github.labyrinthgenerator.pages.game2d.LabyrinthPage;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.constants.Constants;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.shaders.SkyBoxShaderProgram;

public class PlayScreen extends GameScreen {
    private final TextureRegion skyBg;
	/*private final TextureRegion guiBG, guiBGInventorySelected, guiRedCard, guiGreenCard, guiBlueCard, guiGoldenCard,
			texRegBloodOverlay, texRegBlackOverlay;*/

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

    //private final float bloodOverlayAlphaSwitch = 0.5f;

    public PlayScreen(final CubeLab3D game) {
        super(game);

        viewport = new StretchViewport(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        fogColor = new Color(66 / 256f, 33 / 256f, 54 / 256f, 1f);
        //env.set(new ColorAttribute(ColorAttribute.Fog, fogColor));

		/*texRegBloodOverlay = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().atlas01), 0, 0, 2, 2);
		texRegBlackOverlay = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().atlas01), 3, 0, 2, 2);*/

        skyBg = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().bgSky01));
        skyBg.flip(false, true);

		/*guiBG = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 0, 0, 160, 16);
		guiBGInventorySelected = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 240, 0, 16,
				16);
		guiRedCard = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 160, 0, 16, 16);
		guiGreenCard = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 160 + 16, 0, 16, 16);
		guiBlueCard = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 160 + 32, 0, 16, 16);
		guiGoldenCard = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().guiBG), 160 + 48, 0, 16, 16);*/

        guiFont01_64 = game.getAssMan().get(game.getAssMan().font03_64);
        guiFont01_32 = game.getAssMan().get(game.getAssMan().font03_32);
        guiFont02_16 = game.getAssMan().get(game.getAssMan().font02_16);
        glyphLayoutOptionContinue = new GlyphLayout(guiFont01_64, optionContinue);
        glyphLayoutOptionToMainMenu = new GlyphLayout(guiFont01_64, optionToMainMenu);
        glyphLayoutHeadsupDead = new GlyphLayout(guiFont01_64, headsupDead);

        sfxItem = game.getAssMan().get(game.getAssMan().sfxItem);
        //musicBackground = game.getAssMan().get(game.getAssMan().musicBackground01);

        game.getMapBuilder().buildMap(LabyrinthPage.txtFilename);

//		game.getEntMan().addEntity(new Grid(this));

        player = new Player(this);
        game.getEntMan().addEntity(player);
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

//		if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
//			Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
//		}

//		if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
//			game.getEntMan().entities.get(game.getEntMan().entities.size - 1).setDestroy(true);
//			game.getEntMan().entities.get(game.getEntMan().entities.size - 1).destroy();
//		}
    }

    private void limitGuiSelection() {
        if (guiMenuSelection < 0) {
            guiMenuSelection = 1;
        } else if (guiMenuSelection > 1) {
            guiMenuSelection = 0;
        }
    }

    public void playItemSound() {
        sfxItemId = sfxItem.play(game.getSfxVolume());
    }

    @Override
    public void render(final float delta) {
        super.render(delta);

        currentCam.update();

        game.getFbo().begin();
        Gdx.gl.glClearColor(fogColor.r, fogColor.g, fogColor.b, fogColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

//		game.getBatch().getProjectionMatrix().setToOrtho2D(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        //game.getBatch().begin();
        //game.getBatch().draw(skyBg, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        //game.getBatch().end();
        // sky
        envCubeMap.render(currentCam);

//		game.getEntMan().render2DAllEntities(delta);
        game.getMdlBatch().begin(currentCam);
        game.getEntMan().render3DAllEntities(game.getMdlBatch(), env, delta);
        game.getMdlBatch().end();
        game.getFbo().end();

//		render final fbo
        game.getBatch().begin();

        //renderBloodOverlay01();

        game.getBatch().draw(game.getFbo().getColorBufferTexture(), 0, 0, viewport.getWorldWidth(),
            viewport.getWorldHeight());

        guiFont01_32.draw(game.getBatch(), "fps: " + Gdx.graphics.getFramesPerSecond(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 16);

        if (showExitDistance && !showGuiMenu) {
            guiFont01_32.draw(game.getBatch(), "Exit distance: " + player.getExitDistance(), viewport.getWorldWidth() / 8f, viewport.getWorldHeight() - 32);
        }
//		gui
		/*game.getBatch().draw(player.guiCurrentGun, viewport.getWorldWidth() / 2f - 7.5f * 8f, (int) player.gunY,
				7.5f * 16f, 7.5f * 32f);*/

        //renderBloodOverlay02();

//		hud
		/*game.getBatch().setColor(1, 1, 1, 1); // Never cover HUD in blood.
		game.getBatch().draw(guiBG, 0, 0, viewport.getWorldWidth(), 7.5f * 8f);
		guiFont01_64.draw(game.getBatch(), Integer.toString(getPlayer().getCurrentHP()), 48, 42);
//		System.out.println(getPlayer().currentInventorySlot);

		switch (getPlayer().currentInventorySlot) {
		case 1:
			game.getBatch().draw(guiBGInventorySelected, 128, 0, 10 * 6.4f, 7.5f * 8);
			break;
		case 2:
			game.getBatch().draw(guiBGInventorySelected, 128 + 64, 0, 10 * 6.4f, 7.5f * 8);
			break;
		case 3:
			game.getBatch().draw(guiBGInventorySelected, 128 + 128, 0, 10 * 6.4f, 7.5f * 8);
			break;
		case 4:
			game.getBatch().draw(guiBGInventorySelected, 128 + 128 + 64, 0, 10 * 6.4f, 7.5f * 8);
			break;
		case 5:
			game.getBatch().draw(guiBGInventorySelected, 128 + 128 + 128, 0, 10 * 6.4f, 7.5f * 8);
			break;
		case 6:
			game.getBatch().draw(guiBGInventorySelected, 128 + 128 + 128 + 64, 0, 10 * 6.4f, 7.5f * 8);
			break;
		default:
			game.getBatch().draw(guiBGInventorySelected, 128, 0, 10 * 6.4f, 7.5f * 8);
			break;
		}

		if (getPlayer().hasRedKeycard) {
			game.getBatch().draw(guiRedCard, viewport.getWorldWidth() - 80, 32, 10 * 6.4f, 7.5f * 8);
		}
		if (getPlayer().hasGreenKeycard) {
			game.getBatch().draw(guiGreenCard, viewport.getWorldWidth() - 80 + 32, 32, 10 * 6.4f, 7.5f * 8);
		}
		if (getPlayer().hasBlueKeycard) {
			game.getBatch().draw(guiBlueCard, viewport.getWorldWidth() - 80, 8, 10 * 6.4f, 7.5f * 8);
		}
		if (getPlayer().hasGoldenKeycard) {
			game.getBatch().draw(guiGoldenCard, viewport.getWorldWidth() - 80 + 32, 8, 10 * 6.4f, 7.5f * 8);
		}*/

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

	/*private void renderBloodOverlay01() {
//		This pass makes it look cooler.
//		FIXME You can see floor through the gun if you look closely...
		if (getPlayer().bloodOverlayAlpha >= bloodOverlayAlphaSwitch) {
			game.getBatch().setColor(1, 0, 0, getPlayer().bloodOverlayAlpha);
			game.getBatch().draw(texRegBlackOverlay, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
		}
	}

	private void renderBloodOverlay02() {
//		This pass is more traditional.
		if (getPlayer().renderBloodOverlay) {
			if (getPlayer().bloodOverlayAlpha < bloodOverlayAlphaSwitch) {
				game.getBatch().setColor(1, 1, 1, getPlayer().bloodOverlayAlpha);
				game.getBatch().draw(texRegBloodOverlay, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
			}
		}
	}*/

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
