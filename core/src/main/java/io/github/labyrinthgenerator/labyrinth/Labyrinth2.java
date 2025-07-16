package io.github.labyrinthgenerator.labyrinth;

import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector3i;

import java.util.*;

public class Labyrinth2 implements Lab {

    private enum Dirs {
        N(1), S(2), E(4), W(8), IN(16), IS(32);

        public final int value;

        Dirs(int value) {
            this.value = value;
        }
    }

    public static final int depth = 2;

    private final int width;
    private final int height;
    private final int[][][] grid;

    private final int heightFin;
    private final int widthFin;
    public final int[][][] gridFin;

    private boolean dirty;

    private final int startX, startY, startI;
    private boolean exit;
    private final Vector2i exitPos;

    private final Random random;

    private final Stack<Pair<Vector3i, Stack<Integer>>> puffinsStack;

    private final List<Set<Vector2i>> prevPosses;
    private final List<Set<Vector2i>> puffins;

    public static void main(String[] args) {
        Labyrinth2 labyrinth2 = new Labyrinth2(0, 0, 50, 10);
        labyrinth2.create();
        while (!labyrinth2.puffinsStack.empty()) labyrinth2.passageStack();
        labyrinth2.printMaze();
    }

    public Labyrinth2(int startX, int startY, int width, int height) {
        this.startX = startX;
        this.startY = startY;
        this.startI = 0;

        this.width = width / 2;
        this.height = height / 2;
        exitPos = new Vector2i(this.width - startX - 1, this.height - startY - 1);
        heightFin = this.height * 2 + 1;
        widthFin = this.width * 2 + 1;
        assert heightFin == height;
        assert widthFin == width;
        grid = new int[depth][this.height][this.width];
        gridFin = new int[depth][widthFin][heightFin];
        puffinsStack = new Stack<>();
        prevPosses = new ArrayList<>();
        puffins = new ArrayList<>();
        for (int i = 0; i < depth; i++) {
            prevPosses.add(new HashSet<>());
            puffins.add(new HashSet<>());
        }
        int randomSeed = (int) (Math.random() * 10_000_000);
        random = new Random(randomSeed);
    }

    @Override
    public void create() {
        pushPuffin(startX, startY, startI, startX, startY, startI);
    }

    @Override
    public boolean passage(boolean skip) {
        boolean escape = false;
        while (!isFin() && (skip || !dirty)) escape = passageStack();
        return escape;
    }

    public boolean passageStack() {
        int[] DX = {0, 0, 1, -1, 0, 0};
        int[] DY = {-1, 1, 0, 0, 0, 0};
        int[] DI = {0, 0, 0, 0, -1, 1};
        int[] OP = {
            Dirs.S.ordinal(), Dirs.N.ordinal(),
            Dirs.W.ordinal(), Dirs.E.ordinal(),
            Dirs.IS.ordinal(), Dirs.IN.ordinal()
        };

        Pair<Vector3i, Stack<Integer>> currentPos = this.puffinsStack.peek();

        int cx = currentPos.fst.x;
        int cy = currentPos.fst.y;
        int ci = currentPos.fst.z;

        Stack<Integer> dirsStack = currentPos.snd;

        if (!dirsStack.empty()) {
            int direction = dirsStack.pop();

            int nx = cx + DX[direction];
            int ny = cy + DY[direction];
            int ni = ci + DI[direction];

            if (ny >= 0 && ny < height && nx >= 0 && nx < width && ni >= 0 && ni < depth && grid[ni][ny][nx] == 0) {
                grid[ci][cy][cx] |= Dirs.values()[direction].value;
                grid[ni][ny][nx] |= Dirs.values()[OP[direction]].value;
                dirty = true;
                pushPuffin(cx, cy, ci, nx, ny, ni);
            }
        } else {
            this.puffinsStack.pop();
        }

        if (!exit) exit = exitPos.equals(new Vector2i(cx, cy));
        return exit;
    }

    private void pushPuffin(int cx, int cy, int ci, int nx, int ny, int ni) {
        Vector3i currentPos = new Vector3i(nx, ny, ni);
        List<Integer> directions = Arrays.asList(
            Dirs.N.ordinal(), Dirs.S.ordinal(),
            Dirs.E.ordinal(), Dirs.W.ordinal(),
            Dirs.IN.ordinal(), Dirs.IS.ordinal());
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
        prevPosses.get(ci).add(new Vector2i(cx + dx, cy + dy));
        prevPosses.get(ni).add(new Vector2i(nx, ny));
        puffins.get(ni).clear();
        puffins.get(ni).add(new Vector2i(nx, ny));
    }

    private void printMaze() {
        for (int i = 0; i < depth; i++) {
            System.out.println(new String(new char[width * 2 + 1]).replace("\0", "_"));
            for (int y = 0; y < height; y++) {
                System.out.print("|");
                for (int x = 0; x < width; x++) {
                    System.out.print((grid[i][y][x] & Dirs.S.value) != 0 ? " " : "_");
                    if ((grid[i][y][x] & Dirs.E.value) != 0) {
                        System.out.print(((grid[i][y][x] | grid[i][y][x + 1]) & Dirs.S.value) != 0 ? " " : "_");
                    } else {
                        System.out.print("|");
                    }
                }
                System.out.print("\n");
            }
            System.out.print("\n");
        }
    }

    private void convert(int i) {
        if (!dirty) return;
        // convert grid
        int[][] gridPlus = new int[height + 1][width];
        System.arraycopy(this.grid[i], 0, gridPlus, 1, height);
        Arrays.fill(gridPlus[0], Dirs.E.value);
        gridPlus[0][width - 1] = Dirs.N.value;

        for (int yf = 0; yf < heightFin; yf++) {
            int y = yf / 2;
            gridFin[i][0][yf] = 2;
            for (int x = 0; x < width; x++) {
                gridFin[i][x * 2 + 1][yf] = (gridPlus[y][x] & Dirs.S.value) != 0 ? 0 : 1 - yf % 2;
                if (yf % 2 == 0 && (gridPlus[y][x] & Dirs.E.value) != 0) {
                    gridFin[i][x * 2 + 2][yf] = ((gridPlus[y][x] & gridPlus[y][x + 1]) & Dirs.S.value) != 0 ? 0 : 1;
                } else if (yf % 2 == 0) {
                    gridFin[i][x * 2 + 2][yf] = /*yf != height * 2 ? 2 :*/ 1;
                } else if ((gridPlus[y + 1][x] & (Dirs.E.value)) == 0) {
                    gridFin[i][x * 2 + 2][yf] = 2;
                } else {
                    gridFin[i][x * 2 + 2][yf] = 0;
                }
            }
        }
        gridFin[i][0][0] = 1;
        gridFin[i][0][heightFin - 1] = 1;

        if (i == depth -1) dirty = false;
    }

    @Override
    public int[][] get2D(int i) {
        convert(i);
        return gridFin[i];
    }

    @Override
    public int[][] get3D(int i) {
        convert(i);
        return gridFin[i];
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
        for (int i = 0; i < depth; i++) {
            convert(i);
        }
    }

    @Override
    public Set<Vector2i> getPrevPosses(int i) {
        return prevPosses.get(i);
    }

    @Override
    public Set<Vector2i> getPuffins(int i) {
        return puffins.get(i);
    }
}
