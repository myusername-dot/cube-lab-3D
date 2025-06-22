package io.github.labyrinthgenerator.additional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGenerator3 {
    private enum Dirs {
        N(1), S(2), E(4), W(8);

        public final int value;

        Dirs(int value) {
            this.value = value;
        }
    }

    private static int width = 20;
    private static int height = 5;
    private static int[][] grid = new int[height][width];

    private static Random random = new Random(123);

    public static void main(String[] args) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0;
            }
        }
        passage(0, 0);
        printMaze();
    }

    private static void passage(int cx, int cy) {
        int[] DX = {0, 0, 1, -1};
        int[] DY = {-1, 1, 0, 0};
        int[] OP = {Dirs.S.ordinal(), Dirs.N.ordinal(), Dirs.W.ordinal(), Dirs.E.ordinal()};

        List<Integer> directions = Arrays.asList(Dirs.N.ordinal(), Dirs.S.ordinal(), Dirs.E.ordinal(), Dirs.W.ordinal());
        Collections.shuffle(directions, random);

        for (int direction : directions) {
            int nx = cx + DX[direction];
            int ny = cy + DY[direction];

            if (ny >= 0 && ny < height && nx >= 0 && nx < width && grid[ny][nx] == 0) {
                grid[cy][cx] |= Dirs.values()[direction].value;
                grid[ny][nx] |= Dirs.values()[OP[direction]].value;
                passage(nx, ny);
            }
        }
    }

    private static void printMaze() {
        System.out.println(new String(new char[width * 2 + 1]).replace("\0", "_"));
        for (int y = 0; y < height; y++) {
            System.out.print("|");
            for (int x = 0; x < width; x++) {
                System.out.print((grid[y][x] & Dirs.S.value) != 0 ? " " : "_");
                if ((grid[y][x] & Dirs.E.value) != 0) {
                    System.out.print(((grid[y][x] & grid[y][x + 1]) & Dirs.S.value) != 0 ? " " : "_");
                } else {
                    System.out.print("|");
                }
            }
            System.out.print("\n");
        }

        int[][] gridPlus = new int[height + 1][width];
        System.arraycopy(MazeGenerator3.grid, 0, gridPlus, 1, height);
        Arrays.fill(gridPlus[0], Dirs.E.value);
        gridPlus[0][width - 1] = Dirs.N.value;

        int heightFin = height * 2 + 1, widthFin = width * 2 + 1;
        int[][] gridFin = new int[heightFin][widthFin];
        for (int yf = 0; yf < heightFin; yf++) {
            int y = yf / 2;
            gridFin[yf][0] = 2;
            for (int x = 0; x < width; x++) {
                gridFin[yf][x * 2 + 1] = (gridPlus[y][x] & Dirs.S.value) != 0 ? 0 : 1 - yf % 2;
                if (yf % 2 == 0 && (gridPlus[y][x] & Dirs.E.value) != 0) {
                    gridFin[yf][x * 2 + 2] = ((gridPlus[y][x] & gridPlus[y][x + 1]) & Dirs.S.value) != 0 ? 0 : 1;
                } else if (yf % 2 == 0) {
                    gridFin[yf][x * 2 + 2] = yf != height * 2 ? 2 : 1;
                } else if ((gridPlus[y + 1][x] & (Dirs.E.value)) == 0) {
                    gridFin[yf][x * 2 + 2] = 2;
                } else {
                    gridFin[yf][x * 2 + 2] = 0;
                }
            }
        }
        gridFin[heightFin - 1][widthFin - 1] = 2;

        for (int y = 0; y < heightFin; y++) {
            for (int x = 0; x < widthFin; x++) {
                System.out.print(gridFin[y][x]);
            }
            System.out.println();
        }
    }
}
