package io.github.labyrinthgenerator.pages.game3d.maps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.cell.Cell3D;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.models.ModelMaker;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.tickable.Wave;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;

@Slf4j
public class LMapBuilder {

    private final CubeLab3D game;

    public Vector2 mapLoadSpawnPosition = new Vector2();
    public Vector2 mapLoadExitPosition = new Vector2();

    public LMapBuilder(final CubeLab3D game) {
        this.game = game;
    }

    private List<String> readFile(String fileName) {
        if (fileName == null) {
            fileName = System.getProperty("user.home") + "/labyrinth-generations/text-files/" + "5afc3411-4f30-4c7a-ba26-948cfbd4edb1.txt";
        }
        List<String> lines = new ArrayList<>();
        File txtFile = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            do {
                line = reader.readLine();
                if (line != null) {
                    lines.add(line);
                    assert line.length() != 0;
                }
            }
            while (line != null);
        } catch (IOException e) {
            log.error("Error reading file: " + e.getMessage());
        }
        assert lines.size() != 0;
        assert lines.get(lines.size() - 1).length() != 0;

        return lines;
    }

    public void buildMap(String fileName) {
        List<String> lines = readFile(fileName);

        // CHUNKS
        int width = lines.get(lines.size() - 1).length(), height = lines.size();

        Vector2i chunksSize = new Vector2i(width / CHUNK_SIZE + 1, height / CHUNK_SIZE + 1);
        ChunkManager chunkMan = new ChunkManager(chunksSize);
        game.setChunkMan(chunkMan);

        for (int i = 0; i < chunksSize.x; i++) {
            for (int j = 0; j < chunksSize.y; j++) {
                chunkMan.add(i * CHUNK_SIZE, j * CHUNK_SIZE);
            }
        }

        // WALLS
        final Texture texWall = ModelMaker.textureRegionToTexture(
            game.getAssMan().get(game.getAssMan().atlas01), 2 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        final Texture texFloor = ModelMaker.textureRegionToTexture(
            game.getAssMan().get(game.getAssMan().atlas01), 6 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);

        List<Cell3D> cell3DList = new ArrayList<>();
        for (int j = 0; j < height; j++) {
            String line = lines.get(j);
            //System.out.println(line);
            int length = line.length();
            assert length == width;
            for (int i = 0; i < width; i++) {
                int id = Integer.parseInt(line.substring(i, i + 1));
                Labyrinth.LEntity entity = Labyrinth.LEntity.values()[id];

                Cell3D currentCell3D = new Cell3D(new Vector3(i, 0, j), (GameScreen) game.getScreen());

                currentCell3D.hasFloor = true;
                currentCell3D.hasCeiling = true;
                currentCell3D.texRegFloor = texFloor;
                currentCell3D.texRegCeiling = texFloor;
                currentCell3D.texRegEast = texWall;
                currentCell3D.texRegNorth = texWall;
                currentCell3D.texRegSouth = texWall;
                currentCell3D.texRegWest = texWall;
                if (entity == Labyrinth.LEntity.EMPTY) {
                    currentCell3D.hasCeiling = false;
                    currentCell3D.hasWallNorth = false;
                    currentCell3D.hasWallWest = false;
                    currentCell3D.hasWallSouth = false;
                    currentCell3D.hasWallEast = false;
                    currentCell3D.mobSpawn = true;
                } else {

                    final RectanglePlus rect = new RectanglePlus(
                        i, 0, j,
                        1, 1, 1,
                        currentCell3D.getId(), RectanglePlusFilter.WALL,
                        game.getRectMan());
                    // центровка в центр координат
                    rect.setX(rect.getX() - HALF_UNIT);
                    rect.setZ(rect.getZ() - HALF_UNIT);
                }
                cell3DList.add(currentCell3D);
            }
        }

        // Check for walls
        for (final Cell3D currentCell3D : cell3DList) {
            Vector3 currentPosition = currentCell3D.getPositionImmutable();

            for (final Cell3D otherCell3D : cell3DList) {
                Vector3 otherPosition = otherCell3D.getPositionImmutable();

                if (otherCell3D.hasWallWest
                    && otherPosition.x == currentPosition.x - 1
                    && otherPosition.z == currentPosition.z) {
                    currentCell3D.hasWallEast = false;
                }
                if (otherCell3D.hasWallEast
                    && otherPosition.x == currentPosition.x + 1
                    && otherPosition.z == currentPosition.z) {
                    currentCell3D.hasWallWest = false;
                }
                if (otherCell3D.hasWallSouth
                    && otherPosition.x == currentPosition.x
                    && otherPosition.z == currentPosition.z - 1) {
                    currentCell3D.hasWallNorth = false;
                }
                if (otherCell3D.hasWallNorth
                    && otherPosition.x == currentPosition.x
                    && otherPosition.z == currentPosition.z + 1) {
                    currentCell3D.hasWallSouth = false;
                }
            }
        }

        for (Cell3D cell3D : cell3DList) {
            cell3D.buildCell();
        }

        // NONPOS
        Wave wave = new Wave(game);

        // ENTITIES
        for (Cell3D cell3D : cell3DList) {
            if (!cell3D.mobSpawn) continue;
            int minFirefliesCount = 2, maxFirefliesCount = 5;
            int firefliesC = MathUtils.random(minFirefliesCount, maxFirefliesCount);
            for (int i = 0; i < firefliesC; i++) {
                Firefly firefly = new Firefly(
                    new Vector3(
                        cell3D.getPositionX() - HALF_UNIT,
                        MathUtils.random(0f, 0.4f),
                        cell3D.getPositionZ() - HALF_UNIT),
                    (GameScreen) game.getScreen(),
                    wave);
            }
        }

        // SPAWN
        // -0.5, 0.5; 0.5, 1
        // height - 2 - 0.5; height - 1 - 0.5
        mapLoadSpawnPosition.x = HALF_UNIT;
        mapLoadSpawnPosition.y = height - 2 - HALF_UNIT;

        mapLoadExitPosition.x = width - 2 - HALF_UNIT;
        mapLoadExitPosition.y = HALF_UNIT;
    }
}
