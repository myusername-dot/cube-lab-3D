package io.github.labyrinthgenerator.pages.game2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Lab;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;
import io.github.labyrinthgenerator.labyrinth.Labyrinth2;
import io.github.labyrinthgenerator.pages.Page;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.Deflater;

@Slf4j
public class Tools2d implements Page {

    private ApplicationFacade application;
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private OrthographicCamera camera;
    private BitmapFont font;
    private Texture verticalWallTexture;
    private Texture horizontalWallTexture;
    private Texture entryTexture;
    private Texture escapeTexture;

    private Lab labyrinth;
    private int lW, lH;
    private int screenX, screenY;

    @Override
    public void create() {
        application = MyApplication.getApplicationInstance();
        setupViewportAndCamera();
        initializeTextures();
        createFonts("fonts/clacon2.ttf");
        initializeLabyrinthDimensions();
        labyrinth = new Labyrinth2(0, 0, lW, lH);
        labyrinth.create();
        calculateScreenCoordinates();
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

    protected void createFonts(String fontName) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontName));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        parameter.color = Color.DARK_GRAY;
        font = generator.generateFont(parameter);
    }

    private void initializeLabyrinthDimensions() {
        lW = (int) ((MyApplication.windowW - MyApplication.lDivider * 0.75) / MyApplication.lDivider);
        lH = (int) ((MyApplication.windowH - MyApplication.lDivider * 0.75) / MyApplication.lDivider);
        lW += lW % 2 == 0 ? 1 : 0;
        lH += lH % 2 == 0 ? 1 : 0;
    }

    private void calculateScreenCoordinates() {
        screenX = (int) (MyApplication.windowW - MyApplication.lDivider * lW);
        screenY = (int) (MyApplication.windowH - MyApplication.lDivider * lH);
    }

    @Override
    public void input() {
        // Input handling can be implemented here if needed
    }

    @Override
    public void logic() {
        // Logic implementation can be added here if needed
    }

    @Override
    public void draw() {
        prepareDraw();
        drawLabyrinth();
        endDraw();
    }

    public void drawLabyrinth() {
        int[][] labyrinthArray = labyrinth.get2D();
        for (int j = 0; j < lH; j++) {
            for (int i = 0; i < lW; i++) {
                Labyrinth.LEntity entity = Labyrinth.LEntity.values()[labyrinthArray[i][j]];
                switch (entity) {
                    case VERTICAL_WALL:
                        drawVerticalWall(i, j);
                        break;
                    case HORIZONTAL_WALL:
                    case LU_CORNER:
                    case RU_CORNER:
                    case LD_CORNER:
                    case RD_CORNER:
                        drawHorizontalWall(i, j, labyrinthArray);
                        break;
                    default:
                        break;
                }
            }
        }
        drawEntryAndEscape();
    }

    private void drawVerticalWall(int i, int j) {
        spriteBatch.draw(
            verticalWallTexture,
            screenX + i * MyApplication.lDivider, screenY + j * MyApplication.lDivider - MyApplication.lDivider,
            MyApplication.lDivider / 4f, MyApplication.lDivider * 2
        );
    }

    private void drawHorizontalWall(int i, int j, int[][] labyrinthArray) {
        if (shouldDrawHorizontalWall(i, j, labyrinthArray)) {
            spriteBatch.draw(
                horizontalWallTexture,
                screenX + i * MyApplication.lDivider, screenY + j * MyApplication.lDivider,
                MyApplication.lDivider, MyApplication.lDivider / 4f
            );
        }
    }

    private boolean shouldDrawHorizontalWall(int i, int j, int[][] labyrinthArray) {
        return i < lW - 1 &&
            (Labyrinth.LEntity.values()[labyrinthArray[i + 1][j]] != Labyrinth.LEntity.EMPTY ||
                (i > 0 && j > 1 && j < lH - 1 &&
                    Labyrinth.LEntity.values()[labyrinthArray[i - 1][j]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i + 1][j]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i][j - 1]] == Labyrinth.LEntity.EMPTY &&
                    Labyrinth.LEntity.values()[labyrinthArray[i][j + 1]] == Labyrinth.LEntity.EMPTY));
    }

    private void drawEntryAndEscape() {
        spriteBatch.draw(escapeTexture, screenX + (lW - 2) * MyApplication.lDivider, screenY + (lH - 2) * MyApplication.lDivider, MyApplication.lDivider, MyApplication.lDivider);
        spriteBatch.draw(entryTexture, screenX + 1 * MyApplication.lDivider, screenY + 1 * MyApplication.lDivider, MyApplication.lDivider, MyApplication.lDivider);
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
        int[][] labyrinthArray = labyrinth.get3D();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            for (int j = lH - 1; j >= 0; j--) {
                for (int i = 0; i < lW; i++) {
                    writer.write(Integer.toString(labyrinthArray[i][j]));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Error writing to file: " + e.getMessage());
        }
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Lab getLabyrinth() {
        return labyrinth;
    }

    public float getScale() {
        return MyApplication.lDivider;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public Page getNextPage() {
        return null;
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }
}
