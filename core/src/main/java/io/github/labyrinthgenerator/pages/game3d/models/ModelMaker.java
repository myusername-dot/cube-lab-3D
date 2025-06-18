package io.github.labyrinthgenerator.pages.game3d.models;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;

public class ModelMaker {

    final CubeLab3D game;

    private final ModelBuilder mdlBuilder;

    public Model mdlGrid = new Model();

    public Model mdlWallNorth = new Model();
    public Model mdlWallSouth = new Model();
    public Model mdlWallWest = new Model();
    public Model mdlWallEast = new Model();
    public Model mdlFloor = new Model();
    public Model mdlCeiling = new Model();

    public Model mdlDoor = new Model();

    public Model mdlEnemy = new Model();
    public Model mdlPoint = new Model();

    public ModelMaker(final CubeLab3D game) {
        this.game = game;

        mdlBuilder = new ModelBuilder();

        buildModels();
    }

    private void buildModels() {
        mdlGrid = mdlBuilder.createLineGrid(10, 10, 1, 1, new Material(), Usage.Position | Usage.Normal);

//		ENEMY
//			final TextureRegion texRegNorth = new TextureRegion((Texture) game.getAssMan().get(game.getAssMan().atlas01),
//					TEXTURE_SIZE * 2, TEXTURE_SIZE, -TEXTURE_SIZE, TEXTURE_SIZE); // flip x

        final TextureAttribute taEnemy = new TextureAttribute(TextureAttribute.Diffuse);
        taEnemy.textureDescription.minFilter = TextureFilter.Nearest;
        taEnemy.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matEnemy = new Material();
        matEnemy.set(taEnemy);

        mdlEnemy = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 0, -1, matEnemy, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

// POINT
        final TextureAttribute taPoint = new TextureAttribute(TextureAttribute.Diffuse);
        taPoint.textureDescription.minFilter = TextureFilter.Nearest;
        taPoint.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matPoint = new Material();
        matPoint.set(taPoint);

        mdlPoint = mdlBuilder.createRect(HALF_UNIT / 6f, HALF_UNIT / 6f, 0, -HALF_UNIT / 6f,
            HALF_UNIT / 6f, 0, -HALF_UNIT / 6f, -HALF_UNIT / 6f, 0, HALF_UNIT / 6f,
            -HALF_UNIT / 6f, 0, 0, 0, -1, matEnemy, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

//	NORTH WALL
        final Texture texRegNorth = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

        final TextureAttribute taNorth = new TextureAttribute(TextureAttribute.Diffuse, texRegNorth);
        taNorth.textureDescription.minFilter = TextureFilter.Nearest;
        taNorth.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matNorth = new Material();
        matNorth.set(taNorth);

        mdlWallNorth = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 0, -1, matNorth, Usage.Position | Usage.Normal | Usage.TextureCoordinates);

//	mdlInstWallNorth = new ModelInstance(mdlWallNorth);

//	SOUTH WALL
        final Texture texRegSouth = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            TEXTURE_SIZE * 3, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE); // flip x

        final TextureAttribute taSouth = new TextureAttribute(TextureAttribute.Diffuse, texRegSouth);
        taSouth.textureDescription.minFilter = TextureFilter.Nearest;
        taSouth.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matSouth = new Material();
        matSouth.set(taSouth);

        mdlWallSouth = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 0, -1, matSouth, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        mdlWallSouth.nodes.get(0).rotation.set(Vector3.Y, 180f);

//	mdlInstWallSouth = new ModelInstance(mdlWallSouth);

//	WEST WALL
        final Texture texRegWest = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

        final TextureAttribute taWest = new TextureAttribute(TextureAttribute.Diffuse, texRegWest);
        taWest.textureDescription.minFilter = TextureFilter.Nearest;
        taWest.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matWest = new Material();
        matWest.set(taWest);

        mdlWallWest = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 0, -1, matWest, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        mdlWallWest.nodes.get(0).rotation.set(Vector3.Y, -90f);

//	mdlInstWallWest = new ModelInstance(mdlWallWest);

//	EAST WALL
        final Texture texRegEast = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            TEXTURE_SIZE * 2, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE); // flip y

        final TextureAttribute taEast = new TextureAttribute(TextureAttribute.Diffuse, texRegEast);
        taEast.textureDescription.minFilter = TextureFilter.Nearest;
        taEast.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matEast = new Material();
        matEast.set(taEast);

        mdlWallEast = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 0, -1, matEast, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        mdlWallEast.nodes.get(0).rotation.set(Vector3.Y, 90f);

//	mdlInstWallEast = new ModelInstance(mdlWallEast);

//	FLOOR
        final Texture texRegFloor = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            TEXTURE_SIZE, TEXTURE_SIZE * 2, TEXTURE_SIZE, TEXTURE_SIZE); // flip x

        final TextureAttribute taFloor = new TextureAttribute(TextureAttribute.Diffuse, texRegFloor);
        taFloor.textureDescription.minFilter = TextureFilter.Nearest;
        taFloor.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matFloor = new Material();
        matFloor.set(taFloor);

        mdlFloor = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, 1, 0, matFloor, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        mdlFloor.nodes.get(0).rotation.set(Vector3.X, 90f);

//	mdlInstFloor = new ModelInstance(mdlFloor);

//		CEILING
        final Texture texRegCeiling = textureRegionToTexture((Texture) game.getAssMan().get(game.getAssMan().atlas01),
            TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE); // flip x

        final TextureAttribute taCeiling = new TextureAttribute(TextureAttribute.Diffuse, texRegCeiling);
        taCeiling.textureDescription.minFilter = TextureFilter.Nearest;
        taCeiling.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matCeiling = new Material();
        matCeiling.set(taCeiling);

        mdlCeiling = mdlBuilder.createRect(HALF_UNIT, HALF_UNIT, 0, -HALF_UNIT,
            HALF_UNIT, 0, -HALF_UNIT, -HALF_UNIT, 0, HALF_UNIT,
            -HALF_UNIT, 0, 0, -1, 0, matCeiling,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates);
        mdlCeiling.nodes.get(0).rotation.set(Vector3.X, -90f); // not totally correct. Should flip Y. Maybe not needed.

//		mdlInstCeiling = new ModelInstance(mdlCeiling);

//	DOOR
        final TextureRegion texRegDoorNorth = new TextureRegion(
            (Texture) game.getAssMan().get(game.getAssMan().atlas01), TEXTURE_SIZE * 5, TEXTURE_SIZE * 3, -TEXTURE_SIZE, TEXTURE_SIZE);
        final TextureRegion texRegDoorSouth = new TextureRegion(
            (Texture) game.getAssMan().get(game.getAssMan().atlas01), TEXTURE_SIZE * 4, TEXTURE_SIZE * 3, TEXTURE_SIZE, TEXTURE_SIZE);
        final TextureRegion texRegDoorMiddle = new TextureRegion(
            (Texture) game.getAssMan().get(game.getAssMan().atlas01), (int) (TEXTURE_SIZE * 6.25f), TEXTURE_SIZE * 3, -TEXTURE_SIZE / 4f, TEXTURE_SIZE);

        final TextureAttribute taDoorNorth = new TextureAttribute(TextureAttribute.Diffuse, texRegDoorNorth);
        taDoorNorth.textureDescription.minFilter = TextureFilter.Nearest;
        taDoorNorth.textureDescription.magFilter = TextureFilter.Nearest;

        final TextureAttribute taDoorSouth = new TextureAttribute(TextureAttribute.Diffuse, texRegDoorSouth);
        taDoorSouth.textureDescription.minFilter = TextureFilter.Nearest;
        taDoorSouth.textureDescription.magFilter = TextureFilter.Nearest;

        final TextureAttribute taDoorMiddle = new TextureAttribute(TextureAttribute.Diffuse, texRegDoorMiddle);
        taDoorMiddle.textureDescription.minFilter = TextureFilter.Nearest;
        taDoorMiddle.textureDescription.magFilter = TextureFilter.Nearest;

        final Material matDoorNorth = new Material();
        matDoorNorth.set(taDoorNorth);
        final Material matDoorSouth = new Material();
        matDoorSouth.set(taDoorSouth);
        final Material matDoorMiddle = new Material();
        matDoorMiddle.set(taDoorMiddle);

        mdlBuilder.begin();
        MeshPartBuilder meshBuilder;
        final Node node0 = mdlBuilder.node();
        meshBuilder = mdlBuilder.part("northSide", GL20.GL_TRIANGLES,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates, matDoorNorth);
        meshBuilder.rect(new Vector3(HALF_UNIT, HALF_UNIT, 0),
            new Vector3(-HALF_UNIT, HALF_UNIT, 0),
            new Vector3(-HALF_UNIT, -HALF_UNIT, 0),
            new Vector3(HALF_UNIT, -HALF_UNIT, 0), new Vector3(0, 0, -HALF_UNIT * 2));
        node0.translation.set(0, 0, PPU * 2);

        final Node node1 = mdlBuilder.node();
        meshBuilder = mdlBuilder.part("southSide", GL20.GL_TRIANGLES,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates, matDoorSouth);
        meshBuilder.rect(new Vector3(HALF_UNIT, HALF_UNIT, 0),
            new Vector3(-HALF_UNIT, HALF_UNIT, 0),
            new Vector3(-HALF_UNIT, -HALF_UNIT, 0),
            new Vector3(HALF_UNIT, -HALF_UNIT, 0), new Vector3(0, 0, HALF_UNIT * 2));

        node1.rotation.set(Vector3.Y, 180f);
        node1.translation.set(0, 0, PPU * -2);

        final Node node2 = mdlBuilder.node();
        meshBuilder = mdlBuilder.part("middleSide", GL20.GL_TRIANGLES,
            Usage.Position | Usage.Normal | Usage.TextureCoordinates, matDoorMiddle);
        meshBuilder.rect(new Vector3(PPU * 2, HALF_UNIT, 0),
            new Vector3(-PPU * 2, HALF_UNIT, 0),
            new Vector3(-PPU * 2, -HALF_UNIT, 0),
            new Vector3(PPU * 2, -HALF_UNIT, 0), new Vector3(0, 0, -HALF_UNIT * 2));

        node2.rotation.set(Vector3.Y, -90f);
        node2.translation.set(-HALF_UNIT, 0, 0);
        mdlDoor = mdlBuilder.end();
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
