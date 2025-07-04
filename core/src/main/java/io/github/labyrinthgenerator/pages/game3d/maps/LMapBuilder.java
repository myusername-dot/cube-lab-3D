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
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
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

    private List<List<String>> readFile(String fileName) {
        if (fileName == null) {
            fileName = System.getProperty("user.home") + "/labyrinth-generations/text-files/" + "5afc3411-4f30-4c7a-ba26-948cfbd4edb1.txt";
        }
        List<List<String>> edges = new ArrayList<>();
        File txtFile = new File(fileName);
        int edgesCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            do {
                edges.add(new ArrayList<>());
                do {
                    line = reader.readLine();
                    if (line != null && !line.equals("/edge")) {
                        edges.get(edgesCount).add(line);
                        assert line.length() != 0;
                    }
                }
                while (line != null && !line.equals("/edge"));
                edgesCount++;
            }
            while (line != null);
        } catch (IOException e) {
            log.error("Error reading file: " + e.getMessage());
        }
        assert edges.size() != 0;
        assert edgesCount == 6;
        assert edges.size() == edgesCount;
        edges.forEach(e -> {
            assert e.get(e.size() - 1).length() != 0;
        });

        return edges;
    }

    public void buildMap(String fileName) {
        List<List<String>> edges = readFile(fileName);

        int width = edges.get(0).get(edges.get(0).size() - 1).length(), height = 1, depth = edges.get(0).size();

        // CHUNKS
        Vector3i chunksSize = new Vector3i((width / CHUNK_SIZE + 1) * 4, height, (depth / CHUNK_SIZE + 1) * 3);
        ChunkManager chunkMan = new ChunkManager(chunksSize);
        game.setChunkMan(chunkMan);

        for (int i = 0; i < chunksSize.x; i++)
            for (int j = 0; j < chunksSize.y; j++)
                for (int k = 0; k < chunksSize.z; k++)
                chunkMan.add(i * CHUNK_SIZE, -j * CHUNK_SIZE, k * CHUNK_SIZE); // -j!!!

        for (int edge = 0; edge < 6; edge++) {
            List<String> lines = edges.get(edge);

            assert width == lines.get(lines.size() - 1).length();
            assert depth == lines.size();

            int offsetX = edge < 4 ? edge * width : width;
            int offsetZ = edge < 4 ? depth : edge == 4 ? 0 : depth * 2;

            // WALLS
            final Texture texWall = ModelMaker.textureRegionToTexture(
                game.getAssMan().get(game.getAssMan().atlas01), 2 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);
            final Texture texFloor = ModelMaker.textureRegionToTexture(
                game.getAssMan().get(game.getAssMan().atlas01), 6 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);

            List<Cell3D> cell3DList = new ArrayList<>();
            for (int k = offsetZ; k < offsetZ + depth; k++) {
                String line = lines.get(k - offsetZ);
                //System.out.println(line);
                int length = line.length();
                assert length == width;
                for (int i = offsetX; i < offsetX + width; i++) {
                    int id = Integer.parseInt(line.substring(i - offsetX, i - offsetX + 1));
                    Labyrinth.LEntity entity = Labyrinth.LEntity.values()[id];

                    Cell3D currentCell3D = new Cell3D(new Vector3(i, 0, k), game.getScreen());

                    if (entity == Labyrinth.LEntity.EMPTY) {
                        currentCell3D.hasFloor = true;
                        currentCell3D.texRegFloor = texFloor;
                        currentCell3D.mobSpawn = true;
                    } else {
                        currentCell3D.hasWallNorth = true;
                        currentCell3D.hasWallWest = true;
                        currentCell3D.hasWallSouth = true;
                        currentCell3D.hasWallEast = true;
                        currentCell3D.texRegEast = texWall;
                        currentCell3D.texRegNorth = texWall;
                        currentCell3D.texRegSouth = texWall;
                        currentCell3D.texRegWest = texWall;

                        final RectanglePlus rect = new RectanglePlus(
                            i, 0, k,
                            1, 1, 1,
                            currentCell3D.getId(), RectanglePlusFilter.WALL,
                            game.getRectMan());
                        // центровка в центр координат
                        rect.setX(rect.getX() - HALF_UNIT);
                        rect.setZ(rect.getZ() - HALF_UNIT);

                        cell3DList.add(currentCell3D);

                        // FLOOR LAYER 2
                        currentCell3D = new Cell3D(new Vector3(i, -1, k), game.getScreen());
                        currentCell3D.hasFloor = true;
                        currentCell3D.texRegFloor = texFloor;
                        // FLOOR LAYER 2
                    }
                    cell3DList.add(currentCell3D);
                }
            }

            // Check for walls
            for (final Cell3D currentCell3D : cell3DList) {
                Vector3 currentPosition = currentCell3D.getPositionImmutable();

                for (final Cell3D otherCell3D : cell3DList) {
                    Vector3 otherPosition = otherCell3D.getPositionImmutable();

                    if (otherCell3D.hasWallNorth
                        && otherPosition.x == currentPosition.x
                        && otherPosition.y == currentPosition.y
                        && otherPosition.z == currentPosition.z + 1) {
                        currentCell3D.hasWallSouth = false;
                    }
                    if (otherCell3D.hasWallSouth
                        && otherPosition.x == currentPosition.x
                        && otherPosition.y == currentPosition.y
                        && otherPosition.z == currentPosition.z - 1) {
                        currentCell3D.hasWallNorth = false;
                    }
                    if (otherCell3D.hasWallEast
                        && otherPosition.x == currentPosition.x + 1
                        && otherPosition.y == currentPosition.y
                        && otherPosition.z == currentPosition.z) {
                        currentCell3D.hasWallWest = false;
                    }
                    if (otherCell3D.hasWallWest
                        && otherPosition.x == currentPosition.x - 1
                        && otherPosition.y == currentPosition.y
                        && otherPosition.z == currentPosition.z) {
                        currentCell3D.hasWallEast = false;
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
                        game.getScreen(),
                        wave);
                }
            }
        }

        // SPAWN
        // -0.5, 0.5; 0.5, 1
        // depth - 2 - 0.5; depth - 1 - 0.5
        mapLoadSpawnPosition.x = HALF_UNIT;
        mapLoadSpawnPosition.y = depth * 2 - 2 - HALF_UNIT;

        mapLoadExitPosition.x = width - 2 - HALF_UNIT;
        mapLoadExitPosition.y = depth + HALF_UNIT;
    }
}
