package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.Deflater;

import static com.badlogic.gdx.Gdx.gl20;
import static io.github.labyrinthgenerator.MyApplication.*;

public class Tools2dPage implements Page {

    private ApplicationFacade application;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private OrthographicCamera camera;

    private BitmapFont font;
    private Texture backgroundTexture;
    private Texture verticalWallTexture;
    private Texture horizontalWallTexture;

    private Texture entryTexture;
    private Texture escapeTexture;

    private Labyrinth labyrinth;
    private int lW, lH;
    private int screenX, screenY;

    private int frame;

    @Override
    public void create() {
        application = MyApplication.getApplicationInstance();
        viewport = application.getViewport();
        camera = new OrthographicCamera(viewport.getScreenWidth(), viewport.getScreenHeight());
        viewport.setCamera(camera);
        viewport.update(viewport.getScreenWidth(), viewport.getScreenHeight(), true);
        spriteBatch = new SpriteBatch();
        //backgroundTexture = new Texture("backgrounds/notebook-paper-background.jpg");

        lW = (int) ((windowW - lDivider * 0.75) / lDivider);
        lH = (int) ((windowH - lDivider * 0.75) / lDivider);
        lW += lW % 2 == 0 ? 1 : 0;
        lH += lH % 2 == 0 ? 1 : 0;
        labyrinth = new Labyrinth(lW, lH);
        screenX = (int) (windowW - lDivider * lW);
        screenY = (int) (windowH - lDivider * lH);

        float blackScreenWidth = Gdx.graphics.getBackBufferWidth();
        float blackScreenHeight = Gdx.graphics.getBackBufferHeight();
        if (blackScreenWidth != windowW || blackScreenHeight != windowH) {
            float blackScreenWorldScale;
            if (windowH > windowW) blackScreenWorldScale = blackScreenHeight / windowH;
            else blackScreenWorldScale = blackScreenWidth / windowW;
            float worldWindowW = windowW * blackScreenWorldScale;
            float worldWindowH = windowH * blackScreenWorldScale;

            float bordersWidth = blackScreenWidth - worldWindowW;
            float bordersHeight = blackScreenHeight - worldWindowH;

            viewport.setScreenX((int) bordersWidth / 2);
            viewport.setScreenY((int) bordersHeight / 2);
        }

        verticalWallTexture = new Texture("wall1.png");
        horizontalWallTexture = new Texture("wall2.png");
        entryTexture = new Texture("entry.png");
        escapeTexture = new Texture("escape.png");

        createFonts("fonts/clacon2.ttf");
    }

    protected void createFonts(String fontName) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontName));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        parameter.color = Color.DARK_GRAY;
        font = generator.generateFont(parameter);
    }

    @Override
    public void input() {
    }

    @Override
    public void logic() {
        frame++;
        if (frame % 180 == 0) {
            if (frame == 180)
                labyrinth.wormSecond(false, false, 0);
            else
                labyrinth.wormSecond(true, true, 4);
        }
    }

    @Override
    public void draw() {
        prepareDraw();
        drawLabyrinth();
        endDraw();
    }

    public void drawLabyrinth() {
        int[][] labyrinth = this.labyrinth.getLabyrinth();
        for (int j = 0; j < lH; j++)
            for (int i = 0; i < lW; i++) {
                Labyrinth.LEntity lEntity = Labyrinth.LEntity.values()[(labyrinth[i][j])];
                switch (lEntity) {
                    case EMPTY:
                        continue;
                    case VERTICAL_WALL:
                        spriteBatch.draw(
                            verticalWallTexture,
                            screenX + i * lDivider, screenY + j * lDivider - lDivider,
                            lDivider / 4f, lDivider * 2
                        );
                        break;
                    case HORIZONTAL_WALL:
                    case LU_CORNER:
                    case RU_CORNER:
                    case LD_CORNER:
                    case RD_CORNER:
                        if (i < lW - 1 &&
                            (Labyrinth.LEntity.values()[(labyrinth[i + 1][j])] != Labyrinth.LEntity.EMPTY ||
                                i > 0 && j > 1 && j < lH - 1 &&
                                    Labyrinth.LEntity.values()[(labyrinth[i - 1][j])] == Labyrinth.LEntity.EMPTY &&
                                    Labyrinth.LEntity.values()[(labyrinth[i + 1][j])] == Labyrinth.LEntity.EMPTY &&
                                    (Labyrinth.LEntity.values()[(labyrinth[i][j - 1])] == Labyrinth.LEntity.EMPTY &&
                                        Labyrinth.LEntity.values()[(labyrinth[i][j + 1])] == Labyrinth.LEntity.EMPTY)
                            )
                        ) {
                            spriteBatch.draw(
                                horizontalWallTexture,
                                screenX + i * lDivider, screenY + j * lDivider,
                                lDivider, lDivider / 4f
                            );
                        }
                        break;
                }
            }
        spriteBatch.draw(escapeTexture, screenX + (lW - 2) * lDivider, screenY + (lH - 2) * lDivider, lDivider, lDivider);
        spriteBatch.draw(entryTexture, screenX + 1 * lDivider, screenY + 1 * lDivider, lDivider, lDivider);
    }

    public void prepareDraw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
    }


    public void endDraw() {
        spriteBatch.end();
        if (MyApplication.debug) {
            application.getDebugger().render(camera.combined);
        }
    }

    public UUID saveAsImage() {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        UUID uuid = UUID.randomUUID();
        PixmapIO.writePNG(Gdx.files.external("./labyrinth-generations/screenshots/" + uuid + ".png"), pixmap, Deflater.DEFAULT_COMPRESSION, true);
        pixmap.dispose();
        return uuid;
    }

    public void saveAsTxt(UUID uuid) {
        if (uuid == null) uuid = UUID.randomUUID();
        File dir = new File(System.getProperty("user.home") + "/labyrinth-generations/text-files/");
        dir.mkdir();
        File txtFile = new File(dir, uuid + ".txt");
        try {
            txtFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[][] labyrinth = getLabyrinth().getConvertedLabyrinth();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            for (int j = lH - 1; j >= 0; j--) {
                for (int i = 0; i < lW; i++) {
                    writer.write(Integer.toString(labyrinth[i][j]));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Labyrinth getLabyrinth() {
        return labyrinth;
    }

    public float getScale() {
        return lDivider;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public FitViewport getViewport() {
        return viewport;
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
