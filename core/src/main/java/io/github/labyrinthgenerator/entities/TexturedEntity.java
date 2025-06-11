package io.github.labyrinthgenerator.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class TexturedEntity extends Entity {

    protected Texture texture;
    protected Sprite sprite;

    public TexturedEntity(float x, float y, float width, float height, Rectangle rectangle, Texture texture, Sprite sprite) {
        super(x, y, width, height, rectangle);
        this.texture = texture;
        this.sprite = sprite;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
        this.sprite.setTexture(texture);
    }

    public Texture getTexture() {
        return texture;
    }

    public Sprite getSprite() {
        return sprite;
    }

    // ToDo clamp
    @Override
    public void translateX(float value) {
        super.translateX(value);
        sprite.translateX(value);
    }

    // ToDo clamp
    @Override
    public void translateY(float value) {
        super.translateY(value);
        sprite.translateY(value);
    }

    @Override
    public void setX(float x) {
        float diff = x - this.x;
        translateX(diff);
    }

    @Override
    public void setY(float y) {
        float diff = y - this.y;
        translateY(diff);
    }

    @Override
    public void setXY(float x, float y) {
        setX(x);
        setY(y);
    }

}
