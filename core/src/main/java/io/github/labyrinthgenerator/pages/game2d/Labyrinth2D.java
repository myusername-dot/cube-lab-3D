package io.github.labyrinthgenerator.pages.game2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Lab;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game2d.utils.Tools2d;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static io.github.labyrinthgenerator.MyApplication.windowH;
import static io.github.labyrinthgenerator.MyApplication.windowW;

@Slf4j
public class Labyrinth2D implements Page {

    public static String txtFilename;

    public Viewport viewport;

    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;
    private Texture blurredBackground;

    private Tools2d toolsPage;

    private int frame;
    private static final int FRAMERATE = 60;

    private boolean escape;
    private boolean puffPuffins;
    private boolean isFinished;
    private boolean isGameInPause;
    private boolean screenshot;
    private boolean txtFile;

    private Lab lab;
    private Set<Vector2i> prevPoses;
    private Set<Vector2i> puffins;

    // Кнопки
    private BitmapFont buttonFont64;
    private GlyphLayout[] glyphLayouts;
    private final String[] options = {"PLAY", "REFRESH"};
    private int selectedOption = 1;

    @Override
    public void create() {
        ApplicationFacade application = MyApplication.getApplicationInstance();
        viewport = application.getViewport();
        loadTextures();
        createToolsPage();
        setupFonts();
        createGlyphLayouts(options);
        refresh();
    }

    public void refresh() {
        toolsPage.refresh();
        lab = toolsPage.getLabyrinth();
        prevPoses = lab.getPrevPosses();
        puffins = lab.getPuffins();
        puffPuffins = true;
        escape = false;
        isFinished = false;
        isGameInPause = false;
        screenshot = false;
        txtFile = false;
        if (blurredBackground != null) {
            blurredBackground.dispose();
            blurredBackground = null;
        }
    }

    private void loadTextures() {
        prefPoseTexture = new Texture("labyrinth2d/pref.png");
        prefPoseAcceptEscapeTexture = new Texture("labyrinth2d/pref_a.png");
        puffinTexture = new Texture("labyrinth2d/puff.png");
    }

    private void createToolsPage() {
        toolsPage = new Tools2d();
        toolsPage.create();
    }

    private void setupFonts() {
        buttonFont64 = new BitmapFont(Gdx.files.internal("fonts/font03_64.fnt"));
    }

    private void createGlyphLayouts(String[] options) {
        glyphLayouts = new GlyphLayout[options.length];
        for (int i = 0; i < options.length; i++) {
            glyphLayouts[i] = new GlyphLayout(buttonFont64, options[i]);
        }
    }

    @Override
    public void input() {
        if (isGameInPause) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedOption = 1;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedOption = 2;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                handleMenuSelection();
            }
        }
    }

    private void handleMenuSelection() {
        switch (selectedOption) {
            case 1: // Play
                isFinished = true; // Переход к следующей странице
                break;
            case 2: // Refresh
                refresh();
                break;
            default:
                break;
        }
    }

    @Override
    public void logic() {
        frame++;

        if (!isLogicFrame()) return;

        if (puffPuffins) {
            handlePuffPuffinsLogic();
        } else if (!isGameInPause) {
            finalizeLabyrinth();
        }
    }

    private void handlePuffPuffinsLogic() {
        escape = lab.passage();
        puffPuffins = !lab.isFin();
    }

    private void finalizeLabyrinth() {
        lab.convertTo3dGame();
        escape = true;
        isGameInPause = true; // Устанавливаем флаг паузы
        screenshot = true; // Установим флаг для сохранения скриншота
        txtFile = MyApplication.saveAsTxt;
    }

    private void createBlurredBackground() {
        try {
            // Создаем скриншот лабиринта
            Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            BufferedImage originalImage = Tools2d.pixmapToBufferedImage(pixmap);

            // Define the Gaussian blur kernel
            float[] matrix = {
                1/16f, 2/16f, 1/16f,
                2/16f, 4/16f, 2/16f,
                1/16f, 2/16f, 1/16f
            };
            Kernel kernel = new Kernel(3, 3, matrix);

            // Create a ConvolveOp with the kernel
            ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // Apply the blur
            BufferedImage blurredImage = op.filter(originalImage, null);

            int a = 0xff, r = 0, g = 0, b = 0xff;
            int mascARGB = (a << 24) | (r << 16) | (g << 8) | b; // leave only blue color
            Pixmap blurredImagePixmap = Tools2d.bufferedImageToPixmap(blurredImage, mascARGB);
            blurredBackground = new Texture(blurredImagePixmap);

            blurredImagePixmap.dispose();
            pixmap.dispose(); // Освобождаем память
        } catch (IOException e) {
            log.error("An error occurred during createBlurredBackground()", e);
        }
    }


    private boolean isLogicFrame() {
        return frame % (60 / FRAMERATE) == 0;
    }

    @Override
    public void draw() {
        toolsPage.prepareDraw();
        SpriteBatch spriteBatch = toolsPage.getSpriteBatch();

        // Рисуем размытие заднего фона, если есть
        if (isGameInPause) {
            if (blurredBackground != null) {
                spriteBatch.draw(blurredBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            } else {
                toolsPage.drawLabyrinth();
                handleSaving(); // финальный скриншот
                createBlurredBackground(); // Создаем размытие заднего фона
            }
            drawMenuOptions(spriteBatch);
        } else {
            float scale = toolsPage.getScale();
            int screenX = toolsPage.getScreenX();
            int screenY = toolsPage.getScreenY();
            drawPreviousPoses(spriteBatch, scale, screenX, screenY);
            drawPuffins(spriteBatch, scale, screenX, screenY);
            toolsPage.drawLabyrinth();
            handleSaving();
        }

        toolsPage.endDraw();
    }

    private void drawPreviousPoses(SpriteBatch spriteBatch, float scale, int screenX, int screenY) {
        for (Vector2i prevPose : prevPoses) {
            Texture textureToDraw = escape ? prefPoseAcceptEscapeTexture : prefPoseTexture;
            spriteBatch.draw(textureToDraw, screenX + prevPose.x * scale, screenY + prevPose.y * scale, scale, scale);
        }
    }

    private void drawPuffins(SpriteBatch spriteBatch, float scale, int screenX, int screenY) {
        for (Vector2i puffin : puffins) {
            spriteBatch.draw(puffinTexture, screenX + puffin.x * scale, screenY + puffin.y * scale, scale, scale);
        }
    }

    private void drawMenuOptions(SpriteBatch spriteBatch) {
        float baseY = 188;
        for (int i = 0; i < options.length; i++) {
            drawOption(spriteBatch, i, baseY - i * 32, (selectedOption - 1) == i);
        }
    }

    private void drawOption(SpriteBatch spriteBatch, int index, float y, boolean isSelected) {
        float x = viewport.getWorldWidth() / 2f - glyphLayouts[index].width / 2f;
        buttonFont64.draw(spriteBatch, options[index], x, y); // Используем текст из массива options
        if (isSelected) {
            buttonFont64.draw(spriteBatch, ">", x - 32, y); // Индикатор выбора
        }
    }

    private void handleSaving() {
        UUID uuid = null;
        if (screenshot) uuid = toolsPage.saveAsImage();
        if (txtFile) txtFilename = toolsPage.saveAsTxt(uuid);
        screenshot = false;
        txtFile = false;
    }

    @Override
    public Camera getCamera() {
        return toolsPage.getCamera();
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public Page getNextPage() {
        dispose();
        return new CubeLab3D();
    }

    @Override
    public void dispose() {
        toolsPage.dispose();
    }
}
