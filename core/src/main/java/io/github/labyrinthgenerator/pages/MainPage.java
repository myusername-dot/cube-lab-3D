package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.Application;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;

import static io.github.labyrinthgenerator.Application.windowH;
import static io.github.labyrinthgenerator.Application.windowW;

public class MainPage implements Page {

    private ApplicationFacade application;
    private BitmapFont font;
    private Texture backgroundTexture;
    private Texture verticalWallTexture;
    private Texture horizontalWallTexture;

    private Texture escapeTexture;

    private Labyrinth labyrinth;
    private float scale;
    private int lW, lH;

    private int frame;

    @Override
    public void create() {
        application = Application.getApplicationInstanceFacade();
        //backgroundTexture = new Texture("backgrounds/notebook-paper-background.jpg");
        scale = 10;
        lW = (int) (windowW / scale) + 1;
        lH = (int) (windowH / scale) + 1;
        labyrinth = new Labyrinth(lW, lH);
        verticalWallTexture = new Texture("wall1.png");
        horizontalWallTexture = new Texture("wall2.png");
        escapeTexture = new Texture("e.png");

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
    public void draw(FitViewport viewport, SpriteBatch spriteBatch) {
        prepareDraw(viewport, spriteBatch);
        drawLabyrinth(spriteBatch);
        endDraw(spriteBatch);
    }

    public void drawLabyrinth(SpriteBatch spriteBatch) {
        int[][] labyrinth = this.labyrinth.getLabyrinth();
        for (int j = 0; j < lH; j++)
            for (int i = 0; i < lW; i++) {
                Labyrinth.LEntity lEntity = Labyrinth.LEntity.values()[(labyrinth[i][j])];
                switch (lEntity) {
                    case EMPTY:
                        continue;
                    case VERTICAL_WALL:
                        spriteBatch.draw(verticalWallTexture, i * scale, j * scale - 8, scale / 4f, scale + 10);
                        break;
                    case HORIZONTAL_WALL:
                        if (i < lW - 1 &&
                            (Labyrinth.LEntity.values()[(labyrinth[i + 1][j])] != Labyrinth.LEntity.EMPTY ||
                                i > 0 && j > 1 && j < lH - 1 &&
                                    Labyrinth.LEntity.values()[(labyrinth[i - 1][j])] == Labyrinth.LEntity.EMPTY &&
                                    Labyrinth.LEntity.values()[(labyrinth[i + 1][j])] == Labyrinth.LEntity.EMPTY &&
                                    (Labyrinth.LEntity.values()[(labyrinth[i][j - 1])] == Labyrinth.LEntity.EMPTY &&
                                    Labyrinth.LEntity.values()[(labyrinth[i][j + 1])] == Labyrinth.LEntity.EMPTY)
                            )
                        )
                            spriteBatch.draw(horizontalWallTexture, i * scale, j * scale, scale, scale / 4f);
                        break;
                }
            }
        spriteBatch.draw(escapeTexture, (lW - 2) * scale, (lH - 2) * scale, scale, scale);
    }

    public void prepareDraw(FitViewport viewport, SpriteBatch spriteBatch) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
    }


    public void endDraw(SpriteBatch spriteBatch) {
        spriteBatch.end();
        if (Application.debug) {
            application.getDebugger().render(application.getCamera().combined);
        }
    }

    public Labyrinth getLabyrinth() {
        return labyrinth;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public Page getNextPage() {
        return null;
    }
}
