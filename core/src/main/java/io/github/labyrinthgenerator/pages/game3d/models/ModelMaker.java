package io.github.labyrinthgenerator.pages.game3d.models;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.HALF_UNIT;
import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.TEXTURE_SIZE;

public class ModelMaker {

    final CubeLab3D game;
    private final ModelBuilder mdlBuilder;

    public Model mdlGrid;
    public Model mdlWallNorth;
    public Model mdlWallSouth;
    public Model mdlWallWest;
    public Model mdlWallEast;
    public Model mdlFloor;
    public Model mdlCeiling;
    public Model mdlEnemy;
    public Model mdlPoint;

    public ModelMaker(final CubeLab3D game) {
        this.game = game;
        this.mdlBuilder = new ModelBuilder();
        buildModels();
    }

    private void buildModels() {
        mdlGrid = mdlBuilder.createLineGrid(10, 10, 1, 1,
            new Material(), Usage.Position | Usage.Normal);

        mdlEnemy = createModelWithTexture(
            1, 1, 0,
            0, 1, 0,
            0, 0, 0,
            1, 0, 0,
            0, 0, -1,
            getTextureMaterial(TextureAttribute.Diffuse));

        mdlPoint = createModelWithTexture(
            HALF_UNIT / 6f, HALF_UNIT / 6f, 0,
            -HALF_UNIT / 6f, HALF_UNIT / 6f, 0,
            -HALF_UNIT / 6f, -HALF_UNIT / 6f, 0,
            HALF_UNIT / 6f, -HALF_UNIT / 6f, 0,
            0, 0, -1,
            getTextureMaterial(TextureAttribute.Diffuse));

        mdlWallNorth = createWallModel(0, TEXTURE_SIZE, "NORTH");
        mdlWallSouth = createWallModel(TEXTURE_SIZE * 2, TEXTURE_SIZE, "SOUTH");
        mdlWallWest = createWallModel(TEXTURE_SIZE * 3, TEXTURE_SIZE, "WEST");
        mdlWallEast = createWallModel(TEXTURE_SIZE, TEXTURE_SIZE, "EAST");

        mdlFloor = createFloorOrCeilingModel(TEXTURE_SIZE, TEXTURE_SIZE * 2, -1);
        mdlCeiling = createFloorOrCeilingModel(TEXTURE_SIZE, 0, 1);
    }

    private Model createWallModel(int textureX, int textureY, String direction) {
        Texture texture = textureRegionToTexture(game.getAssMan().get(game.getAssMan().atlas01),
            textureX, textureY, TEXTURE_SIZE, TEXTURE_SIZE);
        Material material = getTextureMaterial(TextureAttribute.Diffuse, texture);

        Model wallModel = mdlBuilder.createRect(
            1, 0, 0,
            0, 0, 0,
            0, 1, 0,
            1, 1, 0,
            1, 0, 1,
            material,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates // goto Cell3D cornerLength
        );

        switch (direction) {
            case "NORTH":
                wallModel.nodes.get(0).rotation.set(Vector3.Y, -180f);
                wallModel.nodes.get(0).translation.add(1, 0, 0);
                break;
            case "SOUTH":
                wallModel.nodes.get(0).rotation.set(Vector3.Y, 0f);
                wallModel.nodes.get(0).translation.add(0, 0, 1);
                break;
            case "WEST":
                wallModel.nodes.get(0).rotation.set(Vector3.Y, -90f);
                wallModel.nodes.get(0).translation.add(1, 0, 1);
                break;
            case "EAST":
                wallModel.nodes.get(0).rotation.set(Vector3.Y, 90f);
                break;
        }
        return wallModel;
    }

    private Model createFloorOrCeilingModel(int textureX, int textureY, int direction) {
        Texture texture = textureRegionToTexture(game.getAssMan().get(game.getAssMan().atlas01),
            textureX, textureY, TEXTURE_SIZE, TEXTURE_SIZE);
        Material material = getTextureMaterial(TextureAttribute.Diffuse, texture);
        Model model = mdlBuilder.createRect(
            1, 0, 0,
            0, 0, 0,
            0, 1, 0,
            1, 1, 0,
            0, -direction, 0,
            material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);  // goto Cell3D cornerLength

        model.nodes.get(0).rotation.set(Vector3.X, direction == 1 ? 90f : -90f);
        if (direction == -1) model.nodes.get(0).translation.add(0, 1, 0);
        // todo ceiling not check

        return model;
    }

    private Model createModelWithTexture(float x1, float y1, float z1, float x2, float y2, float z2, float x3,
                                         float y3, float z3, float x4, float y4, float z4, float nx, float ny,
                                         float nz, Material material) {
        return mdlBuilder.createRect(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, nx, ny, nz,
            material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);  // goto Cell3D cornerLength
    }

    private Material getTextureMaterial(long attributeType) {
        return getTextureMaterial(attributeType, null);
    }

    private Material getTextureMaterial(long attributeType, Texture texture) {
        TextureAttribute textureAttribute = new TextureAttribute(attributeType, texture);
        textureAttribute.textureDescription.minFilter = TextureFilter.Nearest;
        textureAttribute.textureDescription.magFilter = TextureFilter.Nearest;

        Material material = new Material();
        material.set(textureAttribute);
        return material;
    }

    public static Texture textureRegionToTexture(Texture textureRegion, int x, int y, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        TextureData textureData = textureRegion.getTextureData();
        if (!textureData.isPrepared()) textureData.prepare();
        Pixmap texturePixmap = textureData.consumePixmap();

        pixmap.drawPixmap(texturePixmap, 0, 0, x, y, width, height);
        Texture newTexture = new Texture(pixmap);

        texturePixmap.dispose();
        pixmap.dispose();

        return newTexture;
    }
}
