package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.MyApplication;
import io.github.labyrinthgenerator.additional.Vector2;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LabyrinthPage implements Page {

    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;

    private Tools2dPage toolsPage;

    private int frame;

    private boolean escape;
    private boolean fourth;
    private boolean update;
    private boolean screenshot;
    private boolean txtFile;

    private Set<Vector2> prevPoses;
    private Set<Vector2> puffins;

    @Override
    public void create() {
        prefPoseTexture = new Texture("pref.png");
        prefPoseAcceptEscapeTexture = new Texture("pref_a.png");
        puffinTexture = new Texture("puff.png");

        toolsPage = new Tools2dPage();
        toolsPage.create();
        toolsPage.getLabyrinth().wormSecond(false, false, 0);

        prevPoses = new HashSet<>();
        puffins = new HashSet<>();
    }

    @Override
    public void input() {
    }

    @Override
    public void logic() {
        screenshot = false;
        txtFile = false;
        frame++;
        if (frame % 30 == 0) {
            prevPoses.clear();
            puffins.clear();
            if (update) { // second
                toolsPage = new Tools2dPage();
                toolsPage.create();
                toolsPage.getLabyrinth().wormSecond(false, false, 0);
                update = false;
            } else if (!fourth) { // third
                escape = toolsPage.getLabyrinth().wormThird(1, 1, prevPoses, puffins, false, false);
                fourth = puffins.isEmpty();
            } else { // fourth
                toolsPage.getLabyrinth().buildFourth(escape);
                fourth = false;
                escape = true;
                update = true;
                screenshot = MyApplication.saveAsImage;
                txtFile = MyApplication.saveAsTxt;
            }
        }
    }

    @Override
    public void draw() {
        toolsPage.prepareDraw();
        SpriteBatch spriteBatch = toolsPage.getSpriteBatch();
        float scale = toolsPage.getScale();
        for (Vector2 prevPose : prevPoses) {
            if (escape)
                spriteBatch.draw(prefPoseAcceptEscapeTexture, prevPose.x * scale, prevPose.y * scale, scale, scale);
            else spriteBatch.draw(prefPoseTexture, prevPose.x * scale, prevPose.y * scale, scale, scale);
        }
        for (Vector2 puffin : puffins) {
            spriteBatch.draw(puffinTexture, puffin.x * scale, puffin.y * scale, scale, scale);
        }
        toolsPage.drawLabyrinth();
        toolsPage.endDraw();
        UUID uuid = null;
        if (screenshot) uuid = toolsPage.saveAsImage();
        if (txtFile) toolsPage.saveAsTxt(uuid);
    }

    @Override
    public Camera getCamera() {
        return toolsPage.getCamera();
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
        toolsPage.dispose();
    }
}
