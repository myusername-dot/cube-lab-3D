package io.github.labyrinthgenerator.pages;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.labyrinthgenerator.Application;
import io.github.labyrinthgenerator.additional.Vector2;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DebugPage implements Page {

    private Texture prefPoseTexture;
    private Texture prefPoseAcceptEscapeTexture;
    private Texture puffinTexture;

    private MainPage mainPage;

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
        prefPoseTexture = new Texture("pp.png");
        prefPoseAcceptEscapeTexture = new Texture("ppa.png");
        puffinTexture = new Texture("puff.png");

        mainPage = new MainPage();
        mainPage.create();
        mainPage.getLabyrinth().wormSecond(false, false, 0);
        //mainPage.getLabyrinth().maxDistance = 5000;

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
                mainPage = new MainPage();
                mainPage.create();
                mainPage.getLabyrinth().wormSecond(false, false, 0);
                update = false;
            } else if (!fourth) { // third
                escape = mainPage.getLabyrinth().wormThird(1, 1, prevPoses, puffins, false, false);
                fourth = puffins.isEmpty();
            } else { // fourth
                mainPage.getLabyrinth().buildFourth(escape);
                fourth = false;
                escape = true;
                update = true;
                screenshot = Application.saveAsImage;
                txtFile = Application.saveAsTxt;
            }
        }
    }

    @Override
    public void draw(FitViewport viewport, SpriteBatch spriteBatch) {
        mainPage.prepareDraw(viewport, spriteBatch);
        float scale = mainPage.getScale();
        for (Vector2 prevPose : prevPoses) {
            if (escape)
                spriteBatch.draw(prefPoseAcceptEscapeTexture, prevPose.x * scale, prevPose.y * scale, scale, scale);
            else spriteBatch.draw(prefPoseTexture, prevPose.x * scale, prevPose.y * scale, scale, scale);
        }
        for (Vector2 puffin : puffins) {
            spriteBatch.draw(puffinTexture, puffin.x * scale, puffin.y * scale, scale, scale);
        }
        mainPage.drawLabyrinth(spriteBatch);
        mainPage.endDraw(spriteBatch);
        UUID uuid = null;
        if (screenshot) uuid = mainPage.saveAsImage();
        if (txtFile) mainPage.saveAsTxt(uuid);
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
