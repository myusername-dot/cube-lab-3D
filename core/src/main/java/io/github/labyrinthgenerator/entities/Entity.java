package io.github.labyrinthgenerator.entities;

import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected Rectangle rectangle;


    public Entity(float x, float y, float width, float height, Rectangle rectangle) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rectangle = rectangle;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public float getRectangleX() {
        return rectangle.getX();
    }

    public float getRectangleY() {
        return rectangle.getY();
    }

    public boolean rectangleOverlaps(Rectangle doodleRectangle) {
        return rectangle.overlaps(doodleRectangle);
    }

    // ToDo clamp
    public void translateX(float value) {
        x += value;
        rectangle.setX(rectangle.getX() + value);
    }

    // ToDo clamp
    public void translateY(float value) {
        y += value;
        rectangle.setY(rectangle.getY() + value);
    }

    public void setX(float x) {
        float diff = x - this.x;
        translateX(diff);
    }

    public void setY(float y) {
        float diff = y - this.y;
        translateY(diff);
    }

    public void setXY(float x, float y) {
        setX(x);
        setY(y);
    }
}
