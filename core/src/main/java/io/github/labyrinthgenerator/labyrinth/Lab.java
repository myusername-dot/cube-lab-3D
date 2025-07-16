package io.github.labyrinthgenerator.labyrinth;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.Set;

public interface Lab {
    void create();

    boolean passage(boolean skip);

    int[][] get2D(int i);

    int[][] get3D(int i);

    boolean isFin();

    void convertTo3dGame();

    Set<Vector2i> getPrevPosses(int i);

    Set<Vector2i> getPuffins(int i);
}
