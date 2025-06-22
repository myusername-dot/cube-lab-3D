package io.github.labyrinthgenerator.labyrinth;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.*;

public class Labyrinth2 {

    private enum Dirs {
        N(1), S(2), E(4), W(8);

        public final int value;

        Dirs(int value) {
            this.value = value;
        }
    }

    private int width;
    private int height;
    private int[][] grid;

    private int heightFin;
    private int widthFin;
    public int[][] gridFin;

    public int randomSeed = 123;
    private Random random;

    public Stack<Pair<Vector2i, Stack<Integer>>> puffins;
    public List<Vector2i> prefPosses;

    public static void main(String[] args) {
        Labyrinth2 labyrinth2 = new Labyrinth2(100, 100);
        labyrinth2.create();
        while (!labyrinth2.puffins.empty())
            labyrinth2.passage();
        labyrinth2.printMaze();
    }

    public Labyrinth2(int width, int height) {
        this.width = width;
        this.height = height;
        heightFin = height * 2 + 1;
        widthFin = width * 2 + 1;
    }

    public void create() {
        grid = new int[height][width];
        gridFin = new int[heightFin][widthFin];
        random = new Random(randomSeed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0;
            }
        }
        prefPosses = new ArrayList<>();
        puffins = new Stack<>();
        pushPuffin(0, 0);
    }

    private void passage() {
        int[] DX = {0, 0, 1, -1};
        int[] DY = {-1, 1, 0, 0};
        int[] OP = {Dirs.S.ordinal(), Dirs.N.ordinal(), Dirs.W.ordinal(), Dirs.E.ordinal()};

        Pair<Vector2i, Stack<Integer>> currentPos = puffins.peek();

        int cx = currentPos.fst.x;
        int cy = currentPos.fst.y;

        Stack<Integer> dirsStack = currentPos.snd;

        if (!dirsStack.empty()) {
            int direction = dirsStack.pop();

            int nx = cx + DX[direction];
            int ny = cy + DY[direction];

            if (ny >= 0 && ny < height && nx >= 0 && nx < width && grid[ny][nx] == 0) {
                grid[cy][cx] |= Dirs.values()[direction].value;
                grid[ny][nx] |= Dirs.values()[OP[direction]].value;
                pushPuffin(nx, ny);
            }
        } else {
            puffins.pop();
        }
    }

    private void pushPuffin(int cx, int cy) {
        Vector2i currentPos = new Vector2i(cx, cy);
        prefPosses.add(currentPos);
        List<Integer> directions = Arrays.asList(Dirs.N.ordinal(), Dirs.S.ordinal(), Dirs.E.ordinal(), Dirs.W.ordinal());
        Collections.shuffle(directions, random);
        Stack<Integer> dirsStack = new Stack<>();
        dirsStack.addAll(directions);
        puffins.push(new Pair<>(currentPos, dirsStack));
    }

    private void printMaze() {
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

        // convert grid
        int[][] gridPlus = new int[height + 1][width];
        System.arraycopy(this.grid, 0, gridPlus, 1, height);
        Arrays.fill(gridPlus[0], Dirs.E.value);
        gridPlus[0][width - 1] = Dirs.N.value;

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
