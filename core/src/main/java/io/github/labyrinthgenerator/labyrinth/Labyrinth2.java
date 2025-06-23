package io.github.labyrinthgenerator.labyrinth;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;

import java.util.*;

public class Labyrinth2 implements Lab {

    private enum Dirs {
        N(1), S(2), E(4), W(8);

        public final int value;

        Dirs(int value) {
            this.value = value;
        }
    }

    private final int width;
    private final int height;
    private final int[][] grid;

    private final int heightFin;
    private final int widthFin;
    public final int[][] gridFin;

    private boolean dirty;

    private final int startX, startY;

    private final Random random;

    private final Stack<Pair<Vector2i, Stack<Integer>>> puffinsStack;

    private final Set<Vector2i> prevPosses;
    private final Set<Vector2i> puffins;

    public static void main(String[] args) {
        Labyrinth2 labyrinth2 = new Labyrinth2(0, 0, 50, 10);
        labyrinth2.create();
        while (!labyrinth2.puffinsStack.empty()) labyrinth2.passageStack();
        labyrinth2.convert();
        labyrinth2.printMaze();
    }

    public Labyrinth2(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.width = width / 2;
        this.height = height / 2;
        heightFin = this.height * 2 + 1;
        widthFin = this.width * 2 + 1;
        assert heightFin == height;
        assert widthFin == width;
        grid = new int[this.height][this.width];
        gridFin = new int[widthFin][heightFin];
        puffinsStack = new Stack<>();
        prevPosses = new HashSet<>();
        puffins = new HashSet<>();
        int randomSeed = (int) (Math.random() * 10_000_000);
        random = new Random(randomSeed);
    }

    @Override
    public void create() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0;
            }
        }
        pushPuffin(startX, startY, startX, startY);
    }

    @Override
    public boolean passage() {
        boolean escape = false;
        while (!isFin() && !dirty) escape = passageStack();
        return escape;
    }

    public boolean passageStack() {
        int[] DX = {0, 0, 1, -1};
        int[] DY = {-1, 1, 0, 0};
        int[] OP = {Dirs.S.ordinal(), Dirs.N.ordinal(), Dirs.W.ordinal(), Dirs.E.ordinal()};

        Pair<Vector2i, Stack<Integer>> currentPos = this.puffinsStack.peek();

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
                dirty = true;
                pushPuffin(cx, cy, nx, ny);
            }
        } else {
            this.puffinsStack.pop();
        }

        return true;
    }

    private void pushPuffin(int cx, int cy, int nx, int ny) {
        Vector2i currentPos = new Vector2i(nx, ny);
        List<Integer> directions = Arrays.asList(Dirs.N.ordinal(), Dirs.S.ordinal(), Dirs.E.ordinal(), Dirs.W.ordinal());
        Collections.shuffle(directions, random);
        Stack<Integer> dirsStack = new Stack<>();
        dirsStack.addAll(directions);
        puffinsStack.push(new Pair<>(currentPos, dirsStack));

        int dx = nx - cx;
        int dy = ny - cy;
        cx = cx * 2 + 1;
        cy = cy * 2 + 1;
        nx = nx * 2 + 1;
        ny = ny * 2 + 1;
        prevPosses.add(new Vector2i(cx + dx, cy + dy));
        prevPosses.add(new Vector2i(nx, ny));
        puffins.clear();
        puffins.add(new Vector2i(nx, ny));
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

       /* convert();

        for (int y = 0; y < heightFin; y++) {
            for (int x = 0; x < widthFin; x++) {
                System.out.print(gridFin[x][y]);
            }
            System.out.println();
        }*/
    }

    private void convert() {
        if (!dirty) return;
        // convert grid
        int[][] gridPlus = new int[height + 1][width];
        System.arraycopy(this.grid, 0, gridPlus, 1, height);
        Arrays.fill(gridPlus[0], Dirs.E.value);
        gridPlus[0][width - 1] = Dirs.N.value;

        for (int yf = 0; yf < heightFin; yf++) {
            int y = yf / 2;
            gridFin[0][yf] = 2;
            for (int x = 0; x < width; x++) {
                gridFin[x * 2 + 1][yf] = (gridPlus[y][x] & Dirs.S.value) != 0 ? 0 : 1 - yf % 2;
                if (yf % 2 == 0 && (gridPlus[y][x] & Dirs.E.value) != 0) {
                    gridFin[x * 2 + 2][yf] = ((gridPlus[y][x] & gridPlus[y][x + 1]) & Dirs.S.value) != 0 ? 0 : 1;
                } else if (yf % 2 == 0) {
                    gridFin[x * 2 + 2][yf] = /*yf != height * 2 ? 2 :*/ 1;
                } else if ((gridPlus[y + 1][x] & (Dirs.E.value)) == 0) {
                    gridFin[x * 2 + 2][yf] = 2;
                } else {
                    gridFin[x * 2 + 2][yf] = 0;
                }
            }
        }
        gridFin[0][0] = 1;
        gridFin[0][heightFin - 1] = 1;

        dirty = false;
    }

    @Override
    public int[][] get2D() {
        convert();
        return gridFin;
    }

    @Override
    public int[][] get3D() {
        convert();
        return gridFin;
    }

    @Override
    public boolean isFin() {
        return puffinsStack.isEmpty();
    }

    @Override
    public void convertTo3dGame() {
        printMaze();
        puffinsStack.clear();
        prevPosses.clear();
        puffins.clear();
        convert();
    }

    @Override
    public Set<Vector2i> getPrevPosses() {
        return prevPosses;
    }

    @Override
    public Set<Vector2i> getPuffins() {
        return puffins;
    }
}
