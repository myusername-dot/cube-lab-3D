package io.github.labyrinthgenerator.additional;

import java.util.ArrayList;
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

        List<Integer> directions = new ArrayList<>();
        directions.add(Dirs.N.ordinal());
        directions.add(Dirs.S.ordinal());
        directions.add(Dirs.E.ordinal());
        directions.add(Dirs.W.ordinal());
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
                    System.out.print(((grid[y][x] | grid[y][x + 1]) & Dirs.S.value) != 0 ? " " : "_");
                } else {
                    System.out.print("|");
                }
            }
            System.out.print("\n");
        }
        System.out.println(new String(new char[width * 2 + 1]).replace("\0", "1"));
        for (int y = 0; y < height; y++) {
            for (int r = 0; r <= 1; r++) {
                System.out.print(2);
                for (int x = 0; x < width; x++) {
                    System.out.print((grid[y][x] & Dirs.S.value) != 0 ? 0 : 1 - r);
                    if ((grid[y][x] & Dirs.E.value) != 0) {
                        System.out.print(((grid[y][x] | grid[y][x + 1]) & Dirs.S.value) != 0 ?
                            (r == 1 && y < height - 1 && (grid[y + 1][x] & Dirs.E.value) == 0) ? 2 :
                                0 : 1 - r);
                    } else if (r == 0) {
                        System.out.print(2);
                    } else if (y > 0 && (grid[y - 1][x] & Dirs.E.value) == 0) {
                        System.out.print(2);
                    } else if (y < height - 1 && (grid[y + 1][x] & Dirs.E.value) == 0) { // check
                        System.out.print(2);
                    } else {
                        System.out.print(0);
                    }
                }
                System.out.print("\n");
            }
        }
    }
}
