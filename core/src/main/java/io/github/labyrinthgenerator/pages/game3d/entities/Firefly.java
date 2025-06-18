package io.github.labyrinthgenerator.pages.game3d.entities;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.Enemy;
import io.github.labyrinthgenerator.pages.game3d.entities.enemies.ai.FireflyAI;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.TEXTURE_SIZE;

public class Firefly extends Enemy {
    private final FireflyAI ai;

    private final TextureRegion yellowTexReg;
    private final TextureRegion greenTexReg;
    //private PointLight pointLight;

    public Firefly(final Vector3 position, final GameScreen screen) {
        super(position, screen);

        mdlInst = new ModelInstanceBB(screen.game.getCellBuilder().mdlPoint);

        yellowTexReg = new TextureRegion((Texture) screen.game.getAssMan().get(screen.game.getAssMan().entities),
            0, 0,
            TEXTURE_SIZE, TEXTURE_SIZE);
        greenTexReg = new TextureRegion((Texture) screen.game.getAssMan().get(screen.game.getAssMan().entities),
            TEXTURE_SIZE, 0,
            TEXTURE_SIZE, TEXTURE_SIZE);

        mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(yellowTexReg));
        mdlInst.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

        /*pointLight = new PointLight();
        pointLight.set(Color.YELLOW, position.x, position.y, position.z, 50);
        if (screen instanceof PlayScreen) {
            ((PlayScreen) screen).getEnv().add(pointLight);
        }*/

        final float rectWidth = mdlInst.radius;
        final float rectHeight = mdlInst.radius;
        final float rectDepth = mdlInst.radius;
        rect = new RectanglePlus(
            this.position.x - rectWidth / 2f, this.position.y - rectHeight / 2f, this.position.z - rectDepth / 2f,
            rectWidth, rectHeight, rectDepth, id, RectanglePlusFilter.ENTITY
        );
        rect.oldPosition.set(rect.getPosition());
        rect.newPosition.set(rect.getPosition());

        screen.game.getRectMan().addRect(rect);

        ai = new FireflyAI(this);
    }

    @Override
    public boolean shouldRender3D() {
        return render3D && isPlayerInRange;
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        mdlInst.transform.setToLookAt(screen.getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f), Vector3.Y);
        mdlInst.transform.setTranslation(position.cpy().add(0, 0.5f, 0));

        super.render3D(mdlBatch, env, delta);
    }

    @Override
    public void tick(final float delta) {
        if (isPlayerInRange) {
            ai.tick(delta);

            screen.checkOverlaps(rect, delta);

            position.set(
                rect.getX() + rect.getWidth() / 2f,
                rect.getY() + rect.getHeight() / 2f,
                rect.getZ() + rect.getDepth() / 2f
            );

            //pointLight.setPosition(position);

            rect.oldPosition.set(rect.getPosition());
        }
    }

    public void switchTexture() {
        mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(greenTexReg));
        mdlInst.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));
    }

    @Override
    public void destroy() {
        if (destroy) {
            if (rect != null) {
                screen.game.getRectMan().removeRect(rect);
            }
        }

        super.destroy(); // should be last.
    }
}
