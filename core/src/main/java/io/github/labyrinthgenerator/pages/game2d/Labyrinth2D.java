package io.github.labyrinthgenerator.pages.game2d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.labyrinth.Lab;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game2d.utils.Tools2d;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.*;

public class Labyrinth2D implements Page {

    public static String txtFilename;

    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;

    private Tools2d toolsPage;

    private int frame;
    private static final int FRAMERATE = 60;

    private boolean escape;
    private boolean puffPuffins = true;
    private boolean isFinished;
    private boolean screenshot;
    private boolean txtFile;

    private Lab lab;
    private Set<Vector2i> prevPoses;
    private Set<Vector2i> puffins;

    @Override
    public void create() {
        loadTextures();
        createToolsPage();
        lab = toolsPage.getLabyrinth();
        prevPoses = lab.getPrevPosses();
        puffins = lab.getPuffins();
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

    @Override
    public void input() {
        // Input handling can be implemented here if needed
    }

    @Override
    public void logic() {
        frame++;
        resetFlags();

        if (!isLogicFrame()) return;

        if (puffPuffins) {
            handlePuffPuffinsLogic();
        } else {
            finalizeLabyrinth();
        }
    }

    private void resetFlags() {
        screenshot = false;
        txtFile = false;
    }

    private void handlePuffPuffinsLogic() {
        escape = lab.passage();
        puffPuffins = !lab.isFin();
    }

    private void finalizeLabyrinth() {
        lab.convertTo3dGame();
        escape = true;
        isFinished = true;
        screenshot = MyApplication.saveAsImage;
        txtFile = MyApplication.saveAsTxt;
    }

    private boolean isLogicFrame() {
        return frame % (60 / FRAMERATE) == 0;
    }

    @Override
    public void draw() {
        toolsPage.prepareDraw();
        SpriteBatch spriteBatch = toolsPage.getSpriteBatch();
        float scale = toolsPage.getScale();
        int screenX = toolsPage.getScreenX();
        int screenY = toolsPage.getScreenY();

        drawPreviousPoses(spriteBatch, scale, screenX, screenY);
        drawPuffins(spriteBatch, scale, screenX, screenY);
        toolsPage.drawLabyrinth();
        toolsPage.endDraw();

        handleSaving();
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

    private void handleSaving() {
        UUID uuid = null;
        if (screenshot) uuid = toolsPage.saveAsImage();
        if (txtFile) txtFilename = toolsPage.saveAsTxt(uuid);
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
