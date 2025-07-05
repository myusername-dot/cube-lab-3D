package io.github.labyrinthgenerator.pages.game3d.entities.enemies;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.models.ModelInstanceBB;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Enemy extends Entity {
    protected ModelInstanceBB mdlInst;

    protected RectanglePlus rect;

    protected boolean isDead = false;

    protected int maxHp = 100;
    protected int currentHp = maxHp;

    public Enemy(final Vector3 position, final GameScreen screen) {
        super(position, screen);
    }

    public void addHp(final int amount) {
        currentHp += amount;
        limitHP();
        checkIfDead();
        destroyIfDead();
    }

    private void checkIfDead() {
        if (currentHp == 0) {
            isDead = true;
        }
    }

    @Override
    public synchronized void destroy() {
        if (!isDestroyed) {
            screen.game.getRectMan().removeRect(rect);
            super.destroy();
        }
    }

    private void destroyIfDead() {
        if (isDead) {
            log.info("Enemy is dead.");
            destroy();
        }
    }

    public RectanglePlus getRect() {
        return rect;
    }

    protected void limitHP() {
        if (currentHp > maxHp) {
            currentHp = maxHp;
        } else if (currentHp < 0) {
            currentHp = 0;
        }
    }

    @Override
    public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
        if (mdlInst != null) {
            mdlInst.setInFrustum(screen.frustumCull(screen.getCurrentCam(), mdlInst));
            if (mdlInst.isInFrustum()) {
                mdlBatch.render(mdlInst, env);
            }
        }
    }

    public void subHp(final int amount) {
        currentHp -= amount;
        limitHP();
        checkIfDead();
        destroyIfDead();
    }
}
