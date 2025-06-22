package io.github.labyrinthgenerator.labyrinth;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.Set;

public interface Lab {
    void create();
    boolean passage();
    int[][] get2D();
    int[][] get3D();
    boolean isFin();
    void convertTo3dGame();
    Set<Vector2i> getPrevPosses();
    Set<Vector2i> getPuffins();
}
