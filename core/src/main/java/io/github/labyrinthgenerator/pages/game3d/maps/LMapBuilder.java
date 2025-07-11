package io.github.labyrinthgenerator.pages.game3d.maps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.labyrinth.Labyrinth;
import io.github.labyrinthgenerator.pages.game3d.CubeLab3D;
import io.github.labyrinthgenerator.pages.game3d.cell.Cell3D;
import io.github.labyrinthgenerator.pages.game3d.entities.Firefly;
import io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls;
import io.github.labyrinthgenerator.pages.game3d.managers.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.models.ModelMaker;
import io.github.labyrinthgenerator.pages.game3d.rect.RectanglePlus;
import io.github.labyrinthgenerator.pages.game3d.rect.filters.RectanglePlusFilter;
import io.github.labyrinthgenerator.pages.game3d.tickable.Wave;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.labyrinthgenerator.pages.game3d.constants.Constants.*;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.currentGravity;
import static io.github.labyrinthgenerator.pages.game3d.gravity.GravityControls.gravityDirections;

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
        edges.remove(edges.get(edges.size() - 1));
        assert edgesCount == 6;
        assert edges.size() == edgesCount; // ????
        edges.forEach(e -> {
            assert e.get(e.size() - 1).length() != 0;
        });

        return edges;
    }

    public void buildMap(String fileName) {
        List<List<String>> edges = readFile(fileName);

        int width = edges.get(0).get(edges.get(0).size() - 1).length(), height = width, depth = edges.get(0).size();

        // CHUNKS
        ChunkManager chunkMan = new ChunkManager(new Vector3i(width, height, depth));
        Vector3i chunksSize = chunkMan.getChunksSize();
        game.setChunkMan(chunkMan);

        for (int i = 0; i < chunksSize.x; i++)
            for (int j = 0; j < chunksSize.y; j++)
                for (int k = 0; k < chunksSize.z; k++)
                    chunkMan.add(i * CHUNK_SIZE, j * CHUNK_SIZE, k * CHUNK_SIZE);

        // WALLS TEX
        final Texture texWall = ModelMaker.textureRegionToTexture(
            game.getAssMan().get(game.getAssMan().atlas01), 2 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);
        final Texture texFloor = ModelMaker.textureRegionToTexture(
            game.getAssMan().get(game.getAssMan().atlas01), 6 * TEXTURE_SIZE, 0, TEXTURE_SIZE, TEXTURE_SIZE);

        // EDGES
        for (int edge = 0; edge < 6; edge++) {
            List<String> lines = edges.get(edge);

            assert width == lines.get(lines.size() - 1).length();
            assert depth == lines.size();

            currentGravity = gravityDirections[edge];

            Vector3 scl = new Vector3();

            // WALLS
            List<Cell3D> cell3DList = new ArrayList<>();
            for (int k = 0; k < depth; k++) {
                String line = lines.get(k);
                int length = line.length();
                assert length == width;
                for (int i = 0; i < width; i++) {
                    int id = Integer.parseInt(line.substring(i, i + 1));
                    Labyrinth.LEntity entity = Labyrinth.LEntity.values()[id];

                    // Применяем сдвиги к координатам
                    Vector3 cellPosition = GravityControls.adjustWorldVecForGravity(
                        new Vector3(i, 0, k),
                        game.getChunkMan().getWorldSize()
                    );
                    Cell3D currentCell3D = new Cell3D(cellPosition, game.getScreen());

                    if (entity == Labyrinth.LEntity.EMPTY) {
                        currentCell3D.hasFloor = true;
                        currentCell3D.texRegFloor = texFloor;
                        currentCell3D.mobSpawn = true;
                    } else {
                        currentCell3D.hasWalls = true;
                        currentCell3D.hasWallNorth = true;
                        currentCell3D.hasWallWest = true;
                        currentCell3D.hasWallSouth = true;
                        currentCell3D.hasWallEast = true;
                        currentCell3D.texRegEast = texWall;
                        currentCell3D.texRegNorth = texWall;
                        currentCell3D.texRegSouth = texWall;
                        currentCell3D.texRegWest = texWall;

                        scl.set(cellPosition);
                        scl.add(GravityControls.worldPositionRectangleAdd[currentGravity.ord]);
                        new RectanglePlus(
                            scl.x, scl.y, scl.z,
                            1, 1, 1,
                            currentCell3D.getId(), RectanglePlusFilter.WALL,
                            game.getRectMan());

                        cell3DList.add(currentCell3D);

                        // FLOOR LAYER 2
                        currentCell3D = new Cell3D(
                            GravityControls.adjustWorldVecForGravity(
                                new Vector3(i, 1, k),
                                game.getChunkMan().getWorldSize()
                            ),
                            game.getScreen()
                        );
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
                    if (currentCell3D.hasWalls && otherCell3D.hasWalls) {

                        Vector3 otherPosition = otherCell3D.getPositionImmutable();

                        scl = GravityControls.adjustWorldVecForGravity(new Vector3(0, 0, 1));
                        if (otherPosition.equals(currentPosition.cpy().add(scl))) {
                            currentCell3D.hasWallSouth = false;
                        }
                        scl = GravityControls.adjustWorldVecForGravity(new Vector3(0, 0, -1));
                        if (otherPosition.equals(currentPosition.cpy().add(scl))) {
                            currentCell3D.hasWallNorth = false;
                        }
                        scl = GravityControls.adjustWorldVecForGravity(new Vector3(1, 0, 0));
                        if (otherPosition.equals(currentPosition.cpy().add(scl))) {
                            currentCell3D.hasWallWest = false;
                        }
                        scl = GravityControls.adjustWorldVecForGravity(new Vector3(-1, 0, 0));
                        if (otherPosition.equals(currentPosition.cpy().add(scl))) {
                            currentCell3D.hasWallEast = false;
                        }
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
                    scl.set(HALF_UNIT, -MathUtils.random(0.3f, 0.7f), HALF_UNIT); // models y = -1
                    scl = GravityControls.adjustWorldVecForGravity(scl);
                    new Firefly(
                        cell3D.getPositionImmutable().add(scl),
                        game.getScreen(),
                        wave
                    );
                }
            }
        }

        // SPAWN
        // -0.5, 0.5; 0.5, 1
        // depth - 2 - 0.5; depth - 1 - 0.5
        mapLoadSpawnPosition.x = 1;
        mapLoadSpawnPosition.y = depth - 2;

        mapLoadExitPosition.x = width - 2;
        mapLoadExitPosition.y = 1;
    }
}
