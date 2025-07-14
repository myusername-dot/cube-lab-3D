package io.github.labyrinthgenerator.pages.game2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.interfaces.ApplicationFacade;
import io.github.labyrinthgenerator.labyrinth.Lab;
import io.github.labyrinthgenerator.interfaces.Page;
import io.github.labyrinthgenerator.pages.game2d.utils.Tools2d;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class Labyrinth2D implements Page {

    public static String txtFilename;

    private Viewport viewport;
    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;
    private Texture blurredBackground;

    private Tools2d tools;

    private int frame;
    private static final int FRAMERATE = 60;

    private boolean[] escape = new boolean[6];
    private boolean[] puffPuffins = new boolean[6];
    private boolean isFinished;
    private boolean isGameInPause;
    private boolean screenshot;
    private boolean txtFile;

    private Lab[] lab;
    private List<Set<Vector2i>> prevPoses;
    private List<Set<Vector2i>> puffins;

    private BitmapFont buttonFont64;
    private GlyphLayout[] glyphLayouts;
    private final String[] options = {"PLAY", "REFRESH"};
    private int selectedOption = 0;

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
        tools.refresh();
        lab = tools.getLabyrinth();
        initializeGameState();
        disposeBlurredBackground();
    }

    private void initializeGameState() {
        prevPoses = new ArrayList<>(6);
        puffins = new ArrayList<>(6);
        for (int edge = 0; edge < 6; edge++) {
            prevPoses.add(lab[edge].getPrevPosses());
            puffins.add(lab[edge].getPuffins());
            puffPuffins[edge] = true;
            escape[edge] = false;
        }
        isFinished = false;
        isGameInPause = false;
        screenshot = false;
        txtFile = false;
    }

    private void disposeBlurredBackground() {
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
        tools = new Tools2d();
        tools.create();
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
            handleMenuNavigation();
        }
    }

    private void handleMenuNavigation() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            changeSelectedOption(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            changeSelectedOption(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            handleMenuSelection();
        }
    }

    private void changeSelectedOption(int delta) {
        selectedOption = (selectedOption + delta + options.length) % options.length;
    }

    private void handleMenuSelection() {
        switch (selectedOption) {
            case 0: // Play
                isFinished = true; // Переход к следующей странице
                break;
            case 1: // Refresh
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

        boolean finalize = true;
        for (int edge = 0; edge < 6; edge++) {
            if (puffPuffins[edge]) {
                handlePuffPuffinsLogic(edge);
                finalize = false;
            }
        }
        if (finalize && !isGameInPause) {
            finalizeLabyrinth();
        }
    }

    private void handlePuffPuffinsLogic(int edge) {
        escape[edge] = lab[edge].passage(false);
        puffPuffins[edge] = !lab[edge].isFin();
    }

    private void finalizeLabyrinth() {
        for (int edge = 0; edge < 6; edge++) {
            lab[edge].convertTo3dGame();
            escape[edge] = true;
        }
        isGameInPause = true; // Устанавливаем флаг паузы
        screenshot = true; // Установим флаг для сохранения скриншота
        txtFile = MyApplication.saveAsTxt;
    }

    private boolean isLogicFrame() {
        return frame % (60 / FRAMERATE) == 0;
    }

    @Override
    public void draw() {
        tools.prepareDraw();
        SpriteBatch spriteBatch = tools.getSpriteBatch();

        if (isGameInPause) {
            handlePauseDraw(spriteBatch);
        } else {
            handleGameDraw(spriteBatch);
        }

        tools.endDraw();
    }

    private void handlePauseDraw(SpriteBatch spriteBatch) {
        if (blurredBackground != null) {
            spriteBatch.draw(blurredBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        } else {
            tools.drawLabyrinth();
            handleSaving();
            blurredBackground = tools.createBlurredBackground();
        }
        drawMenuOptions(spriteBatch);
    }

    private void handleGameDraw(SpriteBatch spriteBatch) {
        float scaleX = tools.getScaleX(), scaleY = tools.getScaleY();
        int screenX = tools.getScreenX();
        int screenY = tools.getScreenY();
        drawPreviousPoses(spriteBatch, scaleX, scaleY, screenX, screenY);
        drawPuffins(spriteBatch, scaleX, scaleY, screenX, screenY);
        tools.drawLabyrinth();
        handleSaving();
    }

    private void drawPreviousPoses(SpriteBatch spriteBatch, float scaleX, float scaleY, int screenX, int screenY) {
        for (int edge = 0; edge < 6; edge++) {
            drawPoses(spriteBatch, scaleX, scaleY, screenX, screenY, edge, prevPoses.get(edge), escape[edge] ? prefPoseAcceptEscapeTexture : prefPoseTexture);
        }
    }

    private void drawPuffins(SpriteBatch spriteBatch, float scaleX, float scaleY, int screenX, int screenY) {
        for (int edge = 0; edge < 6; edge++) {
            drawPoses(spriteBatch, scaleX, scaleY, screenX, screenY, edge, puffins.get(edge), puffinTexture);
        }
    }

    private void drawPoses(SpriteBatch spriteBatch, float scaleX, float scaleY, int screenX, int screenY, int edge, Set<Vector2i> poses, Texture textureToDraw) {
        int offsetX = tools.getEdgeOffsetX(edge);
        int offsetY = tools.getEdgeOffsetY(edge);
        for (Vector2i pose : poses) {
            spriteBatch.draw(textureToDraw, screenX + (offsetX + pose.x) * scaleX, screenY + (offsetY + pose.y) * scaleY, scaleX, scaleY);
        }
    }

    private void drawMenuOptions(SpriteBatch spriteBatch) {
        float baseY = 188;
        for (int i = 0; i < options.length; i++) {
            drawOption(spriteBatch, i, baseY - i * 32, selectedOption == i);
        }
    }

    private void drawOption(SpriteBatch spriteBatch, int index, float y, boolean isSelected) {
        float x = viewport.getWorldWidth() / 2f - glyphLayouts[index].width / 2f;
        buttonFont64.draw(spriteBatch, options[index], x, y);
        if (isSelected) {
            buttonFont64.draw(spriteBatch, ">", x - 32, y);
        }
    }

    private void handleSaving() {
        UUID uuid = null;
        if (screenshot) uuid = tools.saveAsImage();
        if (txtFile) txtFilename = tools.saveAsTxt(uuid);
        screenshot = false;
        txtFile = false;
    }

    @Override
    public Camera getCamera() {
        return tools.getCamera();
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
        tools.dispose();
    }
}
