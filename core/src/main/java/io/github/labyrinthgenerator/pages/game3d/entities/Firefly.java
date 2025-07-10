package io.github.labyrinthgenerator.pages.game3d.entities;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.ai.FireflyAI;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.tickable.Wave;
import io.github.labyrinthgenerator.pages.light.PointLightPlus;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.TEXTURE_SIZE;

public class Firefly extends Enemy {
    private final FireflyAI ai;

    private final TextureRegion yellowTexReg;
    private final TextureRegion greenTexReg;

    public enum Color {
        YELLOW,
        GREEN
    }
    private Color color;
    private final Wave wave;
    private boolean isOnWave = false;

    public Firefly(final Vector3 position, final GameScreen screen, final Wave wave) {
        super(position, screen);
        this.wave = wave;

        mdlInst = new ModelInstanceBB(screen.game.getCellBuilder().mdlPoint, null);

        yellowTexReg = new TextureRegion((Texture) screen.game.getAssMan().get(screen.game.getAssMan().entities),
            0, 0,
            TEXTURE_SIZE, TEXTURE_SIZE);
        greenTexReg = new TextureRegion((Texture) screen.game.getAssMan().get(screen.game.getAssMan().entities),
            TEXTURE_SIZE, 0,
            TEXTURE_SIZE, TEXTURE_SIZE);

        color = Color.YELLOW;
        mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(yellowTexReg));
        mdlInst.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

        pointLight = new PointLightPlus();
        pointLight.set(com.badlogic.gdx.graphics.Color.YELLOW, position.x, position.y, position.z, 1);
        //screen.game.getShaderProvider().pointLights.put(this, pointLight);
        /*if (screen instanceof PlayScreen) {
            ((PlayScreen) screen).getEnv().add(pointLight);
        }*/

        final float rectWidth = mdlInst.radius;
        final float rectHeight = mdlInst.radius;
        final float rectDepth = mdlInst.radius;
        rect = new RectanglePlus(
            position.x - rectWidth / 2f,
            position.y - rectHeight / 2f,
            position.z - rectDepth / 2f,
            rectWidth, rectHeight, rectDepth, id, RectanglePlusFilter.ENTITY, screen.game.getRectMan()
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition());

        ai = new FireflyAI(this);
    }

    @Override
    public boolean shouldRender3D() {
        return render3D;
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        mdlInst.transform.setToLookAt(screen.getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f), Vector3.Y);
        mdlInst.transform.setTranslation(getPositionImmutable());

        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void tick(final float delta) {
        ai.tick(delta);

        screen.checkOverlaps(rect);

        setPosition(
            rect.getX() + rect.getWidth() / 2f,
            rect.getY() + rect.getHeight() / 2f,
            rect.getZ() + rect.getDepth() / 2f
        );

        pointLight.setPosition(getPositionImmutable());

        rect.oldPosition.set(rect.getPosition());

        if (isOnWave != wave.isOnWave(getPositionX(), getPositionZ())) {
            switchColor();
            switchTexture(color);
            isOnWave = wave.isOnWave(getPositionX(), getPositionZ());
        }
    }

    private void switchColor() {
        switch (color) {
            case GREEN:
                color = Color.YELLOW;
                break;
            case YELLOW:
                color = Color.GREEN;
                break;
        }
    }

    public void switchTexture(Color color) {
        this.color = color;
        switch (color) {
            case GREEN:
                mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(greenTexReg));
                pointLight.setColor(com.badlogic.gdx.graphics.Color.GREEN);
                break;
            case YELLOW:
                mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(yellowTexReg));
                pointLight.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
                break;
        }
    }
}
