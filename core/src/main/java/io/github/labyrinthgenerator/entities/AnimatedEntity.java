package io.github.labyrinthgenerator.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class AnimatedEntity extends Entity {

    private final Animation<TextureRegion> animation;

    public AnimatedEntity(float x, float y, float width, float height, Rectangle rectangle, Animation<TextureRegion> animation) {
        super(x, y, width, height, rectangle);
        this.animation = animation;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public Sprite getSprite(float delta) {
        Sprite sprite = new Sprite(animation.getKeyFrame(delta));
        sprite.setSize(width, height);
        sprite.setPosition(x, y);
        return sprite;
    }
}
