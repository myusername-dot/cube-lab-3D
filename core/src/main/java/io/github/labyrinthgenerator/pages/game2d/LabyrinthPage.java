package io.github.labyrinthgenerator.pages.game2d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.additional.Vector2i;
import io.github.labyrinthgenerator.pages.Page;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LabyrinthPage implements Page {

    public static String txtFilename;

    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;

    private Tools2d toolsPage;

    private int frame;
    private int framerate = 30;

    private boolean escape;
    private boolean puffPuffins;
    private boolean newLab;
    private boolean isFinished;
    private boolean screenshot;
    private boolean txtFile;

    private Set<Vector2i> prevPoses;
    private Set<Vector2i> puffins;

    @Override
    public void create() {
        prefPoseTexture = new Texture("labyrinth2d/pref.png");
        prefPoseAcceptEscapeTexture = new Texture("labyrinth2d/pref_a.png");
        puffinTexture = new Texture("labyrinth2d/puff.png");

        createToolsPage();
        toolsPage.getLabyrinth().wormSecond(false, false, 0);

        prevPoses = new HashSet<>();
        puffins = new HashSet<>();

        puffPuffins = true;
    }

    void createToolsPage() {
        toolsPage = new Tools2d();
        toolsPage.create();
    }

    @Override
    public void input() {
    }

    @Override
    public void logic() {
        frame++;
        screenshot = false;
        txtFile = false;

        if (!isLogicFrame()) return;

        prevPoses.clear();
        puffins.clear();
        /*if (newLab) {
            toolsPage.dispose();
            createToolsPage();
            toolsPage.getLabyrinth().wormSecond(false, false, 0);
            newLab = false;
            puffPuffins = true;
        } else */
        if (puffPuffins) {
            escape = toolsPage.getLabyrinth().wormThird(
                1, 1,
                prevPoses, puffins,
                false, false
            );
            puffPuffins = !puffins.isEmpty();
        } else {
            // rest assured that the exit is open
            toolsPage.getLabyrinth().buildFourth(escape);
            escape = true;
            isFinished = true;
            screenshot = MyApplication.saveAsImage;
            txtFile = MyApplication.saveAsTxt;
        }

    }

    private boolean isLogicFrame() {
        return frame % (60 / framerate) == 0;
    }

    @Override
    public void draw() {
        toolsPage.prepareDraw();
        SpriteBatch spriteBatch = toolsPage.getSpriteBatch();

        float scale = toolsPage.getScale();
        int screenX = toolsPage.getScreenX();
        int screenY = toolsPage.getScreenY();

        for (Vector2i prevPose : prevPoses) {
            if (escape) {
                spriteBatch.draw(
                    prefPoseAcceptEscapeTexture,
                    screenX + prevPose.x * scale, screenY + prevPose.y * scale,
                    scale, scale
                );
            } else {
                spriteBatch.draw(
                    prefPoseTexture,
                    screenX + prevPose.x * scale, screenY + prevPose.y * scale,
                    scale, scale
                );
            }
        }
        for (Vector2i puffin : puffins) {
            spriteBatch.draw(
                puffinTexture,
                screenX + puffin.x * scale, screenY + puffin.y * scale,
                scale, scale
            );
        }
        toolsPage.drawLabyrinth();

        toolsPage.endDraw();

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
