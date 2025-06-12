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

    public Info(int[][] labyrinth, int i, int j) {
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
    }
}
