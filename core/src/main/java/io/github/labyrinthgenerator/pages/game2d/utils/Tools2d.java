package io.github.labyrinthgenerator.pages.game2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.additional.image.ImageBlender;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Lab;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;
import io.github.labyrinthgenerator.labyrinth.Labyrinth2;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.Deflater;

@Slf4j
public class Tools2d {

    private ApplicationFacade application;
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private OrthographicCamera camera;
    private BitmapFont font;
    private Texture verticalWallTexture;
    private Texture horizontalWallTexture;
    private Texture entryTexture;
    private Texture escapeTexture;

    private Lab[] labyrinth;
    private int labyrinthWidthHeight;
    private float scaleX, scaleY;
    private int screenX, screenY;

    public void create() {
        application = MyApplication.getApplicationInstance();
        initializeTextures();
        createFonts("fonts/clacon2.ttf");
    }

    public void refresh() {
        disposeSpriteBatch();
        setupViewportAndCamera();
        initializeLabyrinthDimensions();
        createLabyrinths();
        calculateScreenCoordinates();
    }

    private void disposeSpriteBatch() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
    }

    private void setupViewportAndCamera() {
        viewport = application.getViewport();
        camera = new OrthographicCamera(viewport.getWorldWidth(), viewport.getWorldHeight());
        viewport.setCamera(camera);
        viewport.update(viewport.getScreenWidth(), viewport.getScreenHeight(), true);
        application.resize(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        spriteBatch = new SpriteBatch();
    }

    private void initializeTextures() {
        verticalWallTexture = new Texture("labyrinth2d/wall1.png");
        horizontalWallTexture = new Texture("labyrinth2d/wall2.png");
        entryTexture = new Texture("labyrinth2d/entry.png");
        escapeTexture = new Texture("labyrinth2d/escape.png");
    }

    private void createFonts(String fontName) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontName));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        parameter.color = Color.DARK_GRAY;
        font = generator.generateFont(parameter);
    }

    private void initializeLabyrinthDimensions() {
        labyrinthWidthHeight = (int) (((MyApplication.windowW - 1f) / 4f) / MyApplication.lDivider);
        labyrinthWidthHeight += labyrinthWidthHeight % 2 == 0 ? 1 : 0;
        scaleX = MyApplication.lDivider;
        float labScreenH = labyrinthWidthHeight * 3f;
        scaleY = MyApplication.windowH / labScreenH;
    }

    private void createLabyrinths() {
        labyrinth = new Lab[6 / 2];
        for (int edge = 0; edge < 6 / 2; edge++) {
            labyrinth[edge] = new Labyrinth2(0, 0, labyrinthWidthHeight, labyrinthWidthHeight);
            labyrinth[edge].create();
        }
    }

    private void calculateScreenCoordinates() {
        screenX = (int) (MyApplication.windowW - scaleX * labyrinthWidthHeight * 4);
        screenY = (int) (MyApplication.windowH - scaleY * labyrinthWidthHeight * 3);
    }

    public int getEdgeOffsetX(int edge) {
        return edge < 4 ? edge * labyrinthWidthHeight : labyrinthWidthHeight;
    }

    public int getEdgeOffsetY(int edge) {
        return edge < 4 ? labyrinthWidthHeight : edge == 4 ? 0 : labyrinthWidthHeight * 2;
    }

    public void drawLabyrinth() {
        for (int edge = 0; edge < 6; edge++) {
            drawEdgeLabyrinth(edge);
        }
    }

    private void drawEdgeLabyrinth(int edge) {
        int offsetX = getEdgeOffsetX(edge);
        int offsetY = getEdgeOffsetY(edge);
        int[][] labyrinthArray = labyrinth[edge / 2].get2D(edge % 2);

        for (int j = 0; j < labyrinthWidthHeight; j++) {
            for (int i = 0; i < labyrinthWidthHeight; i++) {
                Labyrinth.LEntity entity = Labyrinth.LEntity.values()[labyrinthArray[i][j]];
                switch (entity) {
                    case VERTICAL_WALL:
                        drawVerticalWall(i, j, offsetX, offsetY);
                        break;
                    case HORIZONTAL_WALL:
                    case LU_CORNER:
                    case RU_CORNER:
                    case LD_CORNER:
                    case RD_CORNER:
                        drawHorizontalWall(i, j, offsetX, offsetY, labyrinthArray);
                        break;
                    default:
                        break;
                }
            }
        }
        drawEntryAndEscape(offsetX, offsetY);
    }

    private void drawVerticalWall(int i, int j, int offsetX, int offsetY) {
        spriteBatch.draw(
            verticalWallTexture,
            screenX + (i + offsetX) * scaleX, screenY + (j + offsetY) * scaleY - scaleY,
            scaleX / 4f, scaleY * 2
        );
    }

    private void drawHorizontalWall(int i, int j, int offsetX, int offsetY, int[][] labyrinthArray) {
        if (shouldDrawHorizontalWall(i, j, labyrinthArray)) {
            spriteBatch.draw(
                horizontalWallTexture,
                screenX + (i + offsetX) * scaleX, screenY + (j + offsetY) * scaleY,
                scaleX, scaleY / 4f * 2
            );
        }
    }

    private boolean shouldDrawHorizontalWall(int i, int j, int[][] labyrinthArray) {
        return i < labyrinthWidthHeight - 1 &&
            (Labyrinth.LEntity.values()[labyrinthArray[i + 1][j]] != Labyrinth.LEntity.EMPTY ||
                (i > 0 && j > 1 && j < labyrinthWidthHeight - 1 &&
                    Labyrinth.LEntity.values()[labyrinthArray[i - 1][j]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i + 1][j]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i][j - 1]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i][j + 1]] == Labyrinth.LEntity.EMPTY));
    }

    private void drawEntryAndEscape(int offsetX, int offsetY) {
        spriteBatch.draw(escapeTexture, screenX + (offsetX + labyrinthWidthHeight - 2) * scaleX, screenY + (offsetY + labyrinthWidthHeight - 2) * scaleY, scaleX, scaleY);
        spriteBatch.draw(entryTexture, screenX + (offsetX + 1) * scaleX, screenY + (1 + offsetY) * scaleY, scaleX, scaleY);
    }

    public void prepareDraw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
    }

    public void endDraw() {
        spriteBatch.end();
    }

    public UUID saveAsImage() {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        UUID uuid = UUID.randomUUID();
        PixmapIO.writePNG(Gdx.files.external("./labyrinth-generations/screenshots/" + uuid + ".png"), pixmap, Deflater.DEFAULT_COMPRESSION, true);
        pixmap.dispose();
        return uuid;
    }

    public String saveAsTxt(UUID uuid) {
        if (uuid == null) uuid = UUID.randomUUID();
        File dir = new File(System.getProperty("user.home") + "/labyrinth-generations/text-files/");
        dir.mkdir();
        String filename = uuid + ".txt";
        File txtFile = new File(dir, filename);
        try {
            txtFile.createNewFile();
            writeLabyrinthToFile(txtFile);
        } catch (IOException e) {
            log.error("Error creating file: " + e.getMessage());
        }
        return txtFile.getAbsolutePath();
    }

    private void writeLabyrinthToFile(File txtFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            for (int edge = 0; edge < 6; edge++) {
                int[][] labyrinthArray = labyrinth[edge / 2].get3D(edge % 2);
                for (int j = labyrinthWidthHeight - 1; j >= 0; j--) {
                    for (int i = 0; i < labyrinthWidthHeight; i++) {
                        writer.write(Integer.toString(labyrinthArray[i][j]));
                    }
                    writer.newLine();
                }
                writer.write("/edge");
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Error writing to file: " + e.getMessage());
        }
    }

    public Texture createBlurredBackground() {
        Texture blurredBackground = null;
        try {
            Pixmap originalPixmap = Pixmap.createFromFrameBuffer(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
            BufferedImage originalImage = ImageBlender.pixmapToBufferedImage(originalPixmap);

            /*originalImage = ImageBlender.imageToBufferedImage(
                originalImage.getScaledInstance(
                    viewport.getScreenWidth(), viewport.getScreenHeight(), Image.SCALE_SMOOTH));*/
            BufferedImage blurredImage = ImageBlender.applyGaussianBlur(originalImage);

            int maskARGB = (0xff << 24) | (0 << 16) | (0 << 8) | 0xff; // Blue color
            Pixmap blurredPixmap = ImageBlender.bufferedImageToPixmap(blurredImage, maskARGB);
            blurredBackground = new Texture(blurredPixmap);
            originalPixmap.dispose();
            blurredPixmap.dispose();
        } catch (IOException e) {
            log.error("An error occurred during createBlurredBackground()", e);
        }
        return blurredBackground;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Lab[] getLabyrinth() {
        return labyrinth;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public int getViewportWidth() {
        return (int) viewport.getWorldWidth();
    }

    public int getViewportHeight() {
        return (int) viewport.getWorldHeight();
    }

    public void dispose() {
        spriteBatch.dispose();
    }
}
