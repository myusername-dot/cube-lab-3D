package io.github.labyrinthgenerator.labyrinth;

public class Info {
    public final Labyrinth.LEntity entity;
    public final Labyrinth.LEntity left;
    public final Labyrinth.LEntity right;
    public final Labyrinth.LEntity up;
    public final Labyrinth.LEntity down;
    public final boolean le;
    public final boolean re;
    public final boolean ue;
    public final boolean de;
    public final int notEmptyEntities;
    public final boolean isCorner;
    public final Labyrinth.LEntity cornerType;

    public Info(int[][] labyrinth, int i, int j, int width, int height) {
        entity = Labyrinth.LEntity.values()[labyrinth[i][j]];
        left = Labyrinth.LEntity.values()[labyrinth[i - 1][j]];
        right = Labyrinth.LEntity.values()[labyrinth[i + 1][j]];
        up = Labyrinth.LEntity.values()[labyrinth[i][j + 1]];
        down = Labyrinth.LEntity.values()[labyrinth[i][j - 1]];
        le = left == Labyrinth.LEntity.EMPTY;
        re = right == Labyrinth.LEntity.EMPTY;
        ue = up == Labyrinth.LEntity.EMPTY;
        de = down == Labyrinth.LEntity.EMPTY;
        notEmptyEntities = (le ? 0 : 1) + (re ? 0 : 1) + (ue ? 0 : 1) + (de ? 0 : 1);
        isCorner = isCorner(labyrinth, i, j, width, height);
        if (isCorner) {
            if (le) {
                if (de) cornerType = Labyrinth.LEntity.LU_CORNER;
                else cornerType = Labyrinth.LEntity.LD_CORNER;
            } else {
                if (de) cornerType = Labyrinth.LEntity.RU_CORNER;
                else cornerType = Labyrinth.LEntity.RD_CORNER;
            }
        } else {
            cornerType = null;
        }
    }

    private boolean isCorner(int[][] labyrinth, int x, int y, int width, int height) {
        return Labyrinth.LEntity.values()[(labyrinth[x][y])] == Labyrinth.LEntity.HORIZONTAL_WALL &&
            (x < width - 1 && x > 0 && y > 1 && y < height - 1 && (
                !(
                    Labyrinth.LEntity.values()[(labyrinth[x + 1][y])] == Labyrinth.LEntity.EMPTY
                        && Labyrinth.LEntity.values()[(labyrinth[x - 1][y])] == Labyrinth.LEntity.EMPTY
                        ||
                        Labyrinth.LEntity.values()[(labyrinth[x + 1][y])] == Labyrinth.LEntity.HORIZONTAL_WALL
                            && Labyrinth.LEntity.values()[(labyrinth[x - 1][y])] == Labyrinth.LEntity.HORIZONTAL_WALL
                )
                    && (Labyrinth.LEntity.values()[(labyrinth[x][y - 1])] != Labyrinth.LEntity.EMPTY
                    || Labyrinth.LEntity.values()[(labyrinth[x][y + 1])] != Labyrinth.LEntity.EMPTY)
            ));
    }
}
