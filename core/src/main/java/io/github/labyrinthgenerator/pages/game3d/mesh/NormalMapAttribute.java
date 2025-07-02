package io.github.labyrinthgenerator.pages.game3d.mesh;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

public class NormalMapAttribute extends TextureAttribute {
    public NormalMapAttribute(final long type, final Texture texture) {
        super(type, texture);
    }

    public static NormalMapAttribute createDiffuse(final Texture texture) {
        return new NormalMapAttribute(Diffuse, texture);
    }
}
