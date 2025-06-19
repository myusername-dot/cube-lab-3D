package io.github.labyrinthgenerator.labyrinth;

import com.badlogic.gdx.math.MathUtils;
import io.github.labyrinthgenerator.additional.Vector2i;

import java.util.*;
import java.util.stream.Collectors;

public class Labyrinth {

    public static int maxDistance;

    public enum LEntity {
        EMPTY,
        HORIZONTAL_WALL,
        VERTICAL_WALL,
        LU_CORNER,
        RU_CORNER,
        RD_CORNER,
        LD_CORNER
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private final int width;
    private final int height;
    private int[][] labyrinth;
    private int[][] convertedLabyrinth;

    private final Vector2i escape;

    private final Set<Vector2i> prevPoses;

    public Labyrinth(int width, int height) {
        this.width = width;
        this.height = height;
        escape = new Vector2i(width - 2, height - 2);
        prevPoses = new HashSet<>();
        maxDistance = Integer.MAX_VALUE;
        create();
    }

    public void create() {
        labyrinth = new int[width][height];
        for (int j = height - 2; j >= 0; j -= 2)
            for (int i = 0; i < width - 1; i += 2) {
                //1,1
                //2,0
                labyrinth[i][j + 1] = LEntity.HORIZONTAL_WALL.ordinal();
                labyrinth[i + 1][j + 1] = LEntity.HORIZONTAL_WALL.ordinal();
                labyrinth[i][j] = LEntity.VERTICAL_WALL.ordinal();
                labyrinth[i + 1][j] = LEntity.EMPTY.ordinal();
            }
        // walls
        for (int i = 0; i < width; i++)
            labyrinth[i][0] = LEntity.HORIZONTAL_WALL.ordinal();
        for (int j = height - 2; j >= 1; j -= 2) {
            labyrinth[width - 1][j] = LEntity.VERTICAL_WALL.ordinal();
            labyrinth[width - 1][j + 1] = LEntity.HORIZONTAL_WALL.ordinal();
        }
        labyrinth[width - 1][height - 1] = LEntity.HORIZONTAL_WALL.ordinal();


        for (int j = height - 1; j >= 0; j--) {
            for (int i = 0; i < width; i++) {
                System.out.print(labyrinth[i][j]);
            }
            System.out.println();
        }
    }

    public void wormFirst() {
        for (int j = height - 2; j >= 1; j -= 2) {
            for (int i = 1; i <= width - 2; i += 2) {
                List<Direction> directions = getDirections(i, j, true, false);
                if (directions.isEmpty()) continue;
                int window = MathUtils.random(0, directions.size() - 1);
                Direction direction = directions.get(window);
                dig(direction, i, j);
            }
        }
    }

    public void wormSecond(boolean sortedByEscapeDistance, boolean sortedByDistance, int limit) {
        Map<Vector2i, Integer> puffins = new HashMap<>();
        for (int j = height - 2; j >= 1; j -= 2) {
            for (int i = 1; i <= width - 2; i += 2) {
                if (LEntity.values()[labyrinth[i][j]] != LEntity.EMPTY) {
                    continue;
                }
                Set<Vector2i> prevPosesTmp = new HashSet<>();
                Map<Vector2i, Integer> puffinsTmp = new HashMap<>();
                boolean escape = setPuffins(puffinsTmp, prevPosesTmp, 0, i, j, new Vector2i(i, j), false);
                if (escape) {
                    System.out.println("x:" + i + "y:" + j + " escape == true");
                    continue;
                }

                if (!puffinsTmp.isEmpty()) {
                    puffins.putAll(puffinsTmp);
                }
            }
        }

        List<Vector2i> puffinsList = puffins.entrySet().stream()
            .sorted(
                (e1, e2) ->
                    sortedByEscapeDistance ?
                        e1.getKey().getDistance(escape).compareTo(e2.getKey().getDistance(escape)) :
                        sortedByDistance ? e1.getValue().compareTo(e2.getValue()) :
                            0
            )
            .limit(limit > 0 ? limit : Integer.MAX_VALUE)
            //.limit((int) (puffins.size() / 1.5))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        for (Vector2i puffin : puffinsList) {
            puff(puffin, false);
        }
        puffins.clear();
    }

    public boolean wormThird(
            int startX, int startY,
            Set<Vector2i> prevPoses, Set<Vector2i> puffins,
            boolean sortedByEscapeDistance,
            boolean exitWhenFindEscape
    ) {
        if (LEntity.values()[labyrinth[startX][startY]] != LEntity.EMPTY)
            throw new UnsupportedOperationException("wormSecondDebug: LEntity.values()[labyrinth[startX][startY]] != LEntity.EMPTY");

        Map<Vector2i, Integer> puffinsTmp = new HashMap<>();
        boolean escape = setPuffins(puffinsTmp, prevPoses, 0, startX, startY, new Vector2i(startX, startY), exitWhenFindEscape);
        this.prevPoses.addAll(prevPoses);

        if (escape && exitWhenFindEscape) return true;
        if (!puffinsTmp.isEmpty()) {
            List<Vector2i> sorted = puffinsTmp.entrySet().stream()
                .sorted((e1, e2) ->
                    sortedByEscapeDistance ?
                        e1.getKey().getDistance(this.escape).compareTo(e2.getKey().getDistance(this.escape)) :
                        e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            // divarication
            puffins.add(sorted.get(0));
            if (!escape) puffins.add(sorted.get(sorted.size() - 1));
            else puffins.add(sorted.get((int) (sorted.size() / 1.6)));
        }
        for (Vector2i puffin : puffins) {
            puff(puffin, true);
        }
        return escape;
    }

    public void buildFourth(boolean escape) {
        if (!escape) {
            Vector2i escapePos = new Vector2i(this.escape);
            do {
                // ToDo другие координаты
                if (Math.random() > 0.5) escapePos.x--;
                else escapePos.y--;
            }
            while (LEntity.values()[labyrinth[escapePos.x][escapePos.y]] == LEntity.EMPTY);
            labyrinth[escapePos.x][escapePos.y] = LEntity.EMPTY.ordinal();
        }
        for (int j = height - 2; j >= 1; j--) {
            for (int i = 1; i < width - 1; i++) {
                if (this.escape.x == i && this.escape.y == j) continue;
                Info info = new Info(labyrinth, i, j, width, height);
                if (info.notEmptyEntities == 4 && info.entity == LEntity.EMPTY) {
                    switch (MathUtils.random(1, 4)) {
                        case 1:
                            labyrinth[i - 1][j] = LEntity.EMPTY.ordinal();
                            break;
                        case 2:
                            labyrinth[i + 1][j] = LEntity.EMPTY.ordinal();
                            break;
                        case 3:
                            labyrinth[i][j + 1] = LEntity.EMPTY.ordinal();
                            break;
                        case 4:
                            labyrinth[i][j - 1] = LEntity.EMPTY.ordinal();
                            break;
                    }
                }
                /*LEntity left2 = LEntity.values()[labyrinth[i - 2][j]];
                LEntity right2 = LEntity.values()[labyrinth[i + 2][j]];
                LEntity up2 = LEntity.values()[labyrinth[i][j + 2]];
                LEntity down2 = LEntity.values()[labyrinth[i][j - 2]];
                boolean l2e = left2 == LEntity.EMPTY;
                boolean r2e = right2 == LEntity.EMPTY;
                boolean u2e = up2 == LEntity.EMPTY;
                boolean d2e = down2 == LEntity.EMPTY;
                if (entity == LEntity.EMPTY && notEmptyEntities <= 1) {
                    if (Math.random() > 0.5) entity = LEntity.HORIZONTAL_WALL;
                    else entity = LEntity.VERTICAL_WALL;
                    labyrinth[i][j] = entity.ordinal();
                    continue;
                }
                if (entity == LEntity.HORIZONTAL_WALL &&
                    (up2 == LEntity.HORIZONTAL_WALL) && (down2 == LEntity.HORIZONTAL_WALL) &&
                    (left  == LEntity.HORIZONTAL_WALL  || right == LEntity.HORIZONTAL_WALL)) {
                    entity = LEntity.VERTICAL_WALL;
                    // |----
                    // |----
                    // |----
                } else if (entity == LEntity.VERTICAL_WALL &&
                    (left2 == LEntity.VERTICAL_WALL || l2e) && (right2 == LEntity.VERTICAL_WALL || r2e) &&
                    (up == LEntity.HORIZONTAL_WALL || down == LEntity.HORIZONTAL_WALL)) {
                    entity = LEntity.HORIZONTAL_WALL;
                    // (|_|_|) / ( |_) / (_| )
                }
                labyrinth[i][j] = entity.ordinal();*/
            }
        }
        convertToMapWithCorners();
    }

    private List<Direction> getDirections(int x, int y, boolean dig, boolean usePrevPoses) {
        List<Direction> directions = new ArrayList<>();
        LEntity lEntity1 = LEntity.values()[labyrinth[x - 1][y]];
        LEntity lEntity2 = LEntity.values()[labyrinth[x + 1][y]];
        LEntity lEntity3 = LEntity.values()[labyrinth[x][y + 1]];
        LEntity lEntity4 = LEntity.values()[labyrinth[x][y - 1]];
        boolean maybe1 = x > 1;
        boolean maybe2 = x < width - 2;
        boolean maybe3 = y < height - 2;
        boolean maybe4 = y > 1;
        boolean haveWindow1 = !dig == (lEntity1 == LEntity.EMPTY) && maybe1;
        boolean haveWindow2 = !dig == (lEntity2 == LEntity.EMPTY) && maybe2;
        boolean haveWindow3 = !dig == (lEntity3 == LEntity.EMPTY) && maybe3;
        boolean haveWindow4 = !dig == (lEntity4 == LEntity.EMPTY) && maybe4;
        if (dig) {
            haveWindow1 = haveWindow1 && lEntity1 != LEntity.HORIZONTAL_WALL;
            haveWindow2 = haveWindow2 && lEntity2 != LEntity.HORIZONTAL_WALL;
            haveWindow3 = haveWindow3 && lEntity3 != LEntity.VERTICAL_WALL;
            haveWindow4 = haveWindow4 && lEntity4 != LEntity.VERTICAL_WALL;
            if (usePrevPoses && !prevPoses.isEmpty()) {
                maybe1 = x > 2;
                maybe2 = x < width - 3;
                maybe3 = y < height - 3;
                maybe4 = y > 2;
                Vector2i throughWall1 = new Vector2i(x - 2, y);
                Vector2i throughWall2 = new Vector2i(x + 2, y);
                Vector2i throughWall3 = new Vector2i(x, y + 2);
                Vector2i throughWall4 = new Vector2i(x, y - 2);
                haveWindow1 = haveWindow1 && maybe1 && !prevPoses.contains(throughWall1);
                haveWindow2 = haveWindow2 && maybe2 && !prevPoses.contains(throughWall2);
                haveWindow3 = haveWindow3 && maybe3 && !prevPoses.contains(throughWall3);
                haveWindow4 = haveWindow4 && maybe4 && !prevPoses.contains(throughWall4);
            }
        }
        if (haveWindow1) directions.add(Direction.LEFT);
        if (haveWindow2) directions.add(Direction.RIGHT);
        if (haveWindow3) directions.add(Direction.UP);
        if (haveWindow4) directions.add(Direction.DOWN);
        return directions;
    }

    private void dig(Direction direction, int x, int y) {
        switch (direction) {
            case LEFT:
                labyrinth[x - 1][y] = LEntity.EMPTY.ordinal();
                //labyrinth[x - 1][y - 1] = LEntity.HORIZONTAL_WALL.ordinal();
                break;
            case RIGHT:
                labyrinth[x + 1][y] = LEntity.EMPTY.ordinal();
                //labyrinth[x + 1][y + 1] = LEntity.HORIZONTAL_WALL.ordinal();
                break;
            case UP:
                labyrinth[x][y + 1] = LEntity.EMPTY.ordinal();
                //labyrinth[x - 1][y + 1] = LEntity.VERTICAL_WALL.ordinal();
                break;
            case DOWN:
                labyrinth[x][y - 1] = LEntity.EMPTY.ordinal();
                //labyrinth[x - 1][y - 1] = LEntity.VERTICAL_WALL.ordinal();
                break;
        }
    }

    private boolean setPuffins(
            Map<Vector2i, Integer> puffins, Set<Vector2i> prevPoses,
            int distance,
            int x, int y, final Vector2i prevPos,
            boolean exitWhenFindEscape
    ) {
        if (LEntity.values()[labyrinth[x][y]] != LEntity.EMPTY) {
            throw new UnsupportedOperationException("setPuffins: LEntity.values()[labyrinth[x][y]] != LEntity.EMPTY");
        }
        Vector2i currentPos = new Vector2i(x, y);
        if (prevPoses.contains(currentPos)) {
            return false;
        }
        prevPoses.add(currentPos);
        if (currentPos.equals(this.escape)) {
            return true;
        }
        distance++;
        if (distance > maxDistance) {
            return false;
        }

        List<Direction> maybeDirections = getDirections(x, y, false, false);
        List<Direction> directions = new ArrayList<>();
        boolean cycle = false;
        int nextX, nextY;
        for (Direction direction : maybeDirections) {
            switch (direction) {
                case LEFT:
                    nextX = x - 1;
                    nextY = y;
                    if (!prevPoses.contains(new Vector2i(nextX, nextY))) directions.add(direction);
                    else if (prevPos.x != nextX && prevPos.y != nextY) cycle = true;
                    break;
                case RIGHT:
                    nextX = x + 1;
                    nextY = y;
                    if (!prevPoses.contains(new Vector2i(nextX, nextY))) directions.add(direction);
                    else if (prevPos.x != nextX && prevPos.y != nextY) cycle = true;
                    break;
                case UP:
                    nextX = x;
                    nextY = y + 1;
                    if (!prevPoses.contains(new Vector2i(nextX, nextY))) directions.add(direction);
                    else if (prevPos.x != nextX && prevPos.y != nextY) cycle = true;
                    break;
                case DOWN:
                    nextX = x;
                    nextY = y - 1;
                    if (!prevPoses.contains(new Vector2i(nextX, nextY))) directions.add(direction);
                    else if (prevPos.x != nextX && prevPos.y != nextY) cycle = true;
                    break;
            }
        }
        if (directions.isEmpty()) {
            if (cycle) distance /= 1.5;
            if (!getDirections(x, y, true, true).isEmpty()) {
                puffins.put(currentPos, distance);
            }
            return false;
        }

        boolean escape = false;
        for (Direction direction : directions) {
            switch (direction) {
                case LEFT:
                    nextX = x - 1;
                    nextY = y;
                    if (setPuffins(puffins, prevPoses, distance, nextX, nextY, currentPos, exitWhenFindEscape)) {
                        if (exitWhenFindEscape) return true;
                        escape = true;
                        if (distance > Math.pow(width * height, 0.65))
                            distance /= 2; // lead the remaining branches away from the exit
                    }
                    break;
                case RIGHT:
                    nextX = x + 1;
                    nextY = y;
                    if (setPuffins(puffins, prevPoses, distance, nextX, nextY, currentPos, exitWhenFindEscape)) {
                        if (exitWhenFindEscape) return true;
                        escape = true;
                        if (distance > Math.pow(width * height, 0.65))
                            distance /= 2; // lead the remaining branches away from the exit
                    }
                    break;
                case UP:
                    nextX = x;
                    nextY = y + 1;
                    if (setPuffins(puffins, prevPoses, distance, nextX, nextY, currentPos, exitWhenFindEscape)) {
                        if (exitWhenFindEscape) return true;
                        escape = true;
                        if (distance > Math.pow(width * height, 0.65))
                            distance /= 2; // lead the remaining branches away from the exit
                    }
                    break;
                case DOWN:
                    nextX = x;
                    nextY = y - 1;
                    if (setPuffins(puffins, prevPoses, distance, nextX, nextY, currentPos, exitWhenFindEscape)) {
                        if (exitWhenFindEscape) return true;
                        escape = true;
                        if (distance > Math.pow(width * height, 0.65))
                            distance /= 2; // lead the remaining branches away from the exit
                    }
                    break;
            }
        }

        return escape;
    }

    private void puff(Vector2i puffin, boolean usePrevPoses) {
        List<Direction> directions = getDirections(puffin.x, puffin.y, true, usePrevPoses);
        if (directions.isEmpty()) return;
        int window = MathUtils.random(0, directions.size() - 1);
        Direction direction = directions.get(window);
        dig(direction, puffin.x, puffin.y);
    }

    private void convertToMapWithCorners() {
        convertedLabyrinth = new int[width][height];
        for (int i = 0; i < width; i++)
            System.arraycopy(labyrinth[i], 0, convertedLabyrinth[i], 0, height);
        // walls
        for (int i = 1; i < width; i++) {
            convertedLabyrinth[i][height - 1] = LEntity.HORIZONTAL_WALL.ordinal();
        }
        for (int j = height - 2; j >= 1; j--) {
            convertedLabyrinth[0][j] = LEntity.VERTICAL_WALL.ordinal();
            convertedLabyrinth[width - 1][j] = LEntity.VERTICAL_WALL.ordinal();
        }
        convertedLabyrinth[0][0] = LEntity.LU_CORNER.ordinal();
        convertedLabyrinth[width - 1][0] = LEntity.RU_CORNER.ordinal();
        convertedLabyrinth[0][height - 1] = LEntity.LD_CORNER.ordinal();
        convertedLabyrinth[width - 1][height - 1] = LEntity.RD_CORNER.ordinal();
        for (int j = height - 2; j >= 1; j--) {
            for (int i = 1; i < width - 1; i++) {
                Info info = new Info(convertedLabyrinth, i, j, width, height);
                LEntity entity = info.entity;

                if (entity == LEntity.HORIZONTAL_WALL && info.up == LEntity.VERTICAL_WALL && info.down == LEntity.VERTICAL_WALL
                    || info.notEmptyEntities == 1 && (info.up == LEntity.VERTICAL_WALL || info.down == LEntity.VERTICAL_WALL)) {
                    entity = LEntity.VERTICAL_WALL;
                    convertedLabyrinth[i][j] = entity.ordinal();
                }

                setCorner(convertedLabyrinth, i, j, width, height);
            }
        }
        System.out.println("Finally:");
        for (int j = height - 1; j >= 0; j--) {
            for (int i = 0; i < width; i++) {
                System.out.print(convertedLabyrinth[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    private void setCorner(int[][] labyrinth, int x, int y, int width, int height) {
        Info info = new Info(labyrinth, x, y, width, height);
        if (info.isCorner) {
            labyrinth[x][y] = info.cornerType.ordinal();
        }
    }

    public int[][] getLabyrinth() {
        return labyrinth;
    }

    public int[][] getConvertedLabyrinth() {
        return convertedLabyrinth;
    }
}
