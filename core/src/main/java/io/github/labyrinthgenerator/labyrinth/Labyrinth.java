package io.github.labyrinthgenerator.labyrinth;

import com.badlogic.gdx.math.MathUtils;
import io.github.labyrinthgenerator.additional.Vector2iSeedHash;
import io.github.labyrinthgenerator.pages.game3d.vectors.Vector2i;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final int startX, startY;
    private boolean escape;
    private int[][] labyrinthFin;
    private final Vector2iSeedHash escapePos;
    private final Set<Vector2i> prevPosses;
    private final Set<Vector2i> puffins;

    public Labyrinth(int startX, int startY, int width, int height) {
        this.startX = startX + 1;
        this.startY = startY + 1;
        this.width = width;
        this.height = height;
        this.escapePos = new Vector2iSeedHash(width - 2, height - 2);
        this.prevPosses = new HashSet<>();
        this.puffins = new HashSet<>();
        maxDistance = Integer.MAX_VALUE;
        create();
    }

    public void create() {
        initializeLabyrinth();
        printLabyrinth();
        wormFirst(false, false, 0);
    }

    private void initializeLabyrinth() {
        labyrinth = new int[width][height];
        createWalls();
        createBorders();
    }

    private void createWalls() {
        for (int j = height - 2; j >= 0; j -= 2) {
            for (int i = 0; i < width - 1; i += 2) {
                labyrinth[i][j + 1] = LEntity.HORIZONTAL_WALL.ordinal();
                labyrinth[i + 1][j + 1] = LEntity.HORIZONTAL_WALL.ordinal();
                labyrinth[i][j] = LEntity.VERTICAL_WALL.ordinal();
                labyrinth[i + 1][j] = LEntity.EMPTY.ordinal();
            }
        }
    }

    private void createBorders() {
        Arrays.fill(labyrinth[0], LEntity.VERTICAL_WALL.ordinal());
        Arrays.fill(labyrinth[width - 1], LEntity.VERTICAL_WALL.ordinal());
        for (int i = 0; i < width; i++) {
            labyrinth[i][0] = LEntity.HORIZONTAL_WALL.ordinal();
            labyrinth[i][height - 1] = LEntity.HORIZONTAL_WALL.ordinal();
        }
    }

    private void printLabyrinth() {
        for (int j = height - 1; j >= 0; j--) {
            System.out.println(Arrays.toString(labyrinth[j]));
        }
    }

    public void wormFirst(boolean sortedByEscapeDistance, boolean sortedByDistance, int limit) {
        Map<Vector2iSeedHash, Integer> puffinsMap = new HashMap<>();
        for (int j = height - 2; j >= 1; j -= 2) {
            for (int i = 1; i <= width - 2; i += 2) {
                if (LEntity.values()[labyrinth[i][j]] != LEntity.EMPTY) continue;
                Set<Vector2iSeedHash> prevPosesTmp = new HashSet<>();
                Map<Vector2iSeedHash, Integer> puffinsTmp = new HashMap<>();
                boolean foundEscape = setPuffins(puffinsTmp, prevPosesTmp, 0, i, j, new Vector2iSeedHash(i, j), false);
                if (foundEscape) {
                    log.info("Escape found at x: {} y: {}", i, j);
                    continue;
                }
                puffinsMap.putAll(puffinsTmp);
            }
        }

        List<Vector2iSeedHash> puffinsList = sortPuffins(puffinsMap, sortedByEscapeDistance, sortedByDistance, limit);
        puffinsList.forEach(puffin -> puff(puffin, false));
        puffinsMap.clear();
    }

    private List<Vector2iSeedHash> sortPuffins(Map<Vector2iSeedHash, Integer> puffinsMap, boolean sortedByEscapeDistance, boolean sortedByDistance, int limit) {
        return puffinsMap.entrySet().stream()
            .sorted((e1, e2) -> {
                if (sortedByEscapeDistance) {
                    return e1.getKey().getDistance(escapePos).compareTo(e2.getKey().getDistance(escapePos));
                }
                return sortedByDistance ? e1.getValue().compareTo(e2.getValue()) : 0;
            })
            .limit(limit > 0 ? limit : Integer.MAX_VALUE)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public boolean passage(boolean skip) {
        return passage(false, false);
    }

    public boolean passage(boolean sortedByEscapeDistance, boolean exitWhenFindEscape) {
        validateStartPosition();

        Set<Vector2iSeedHash> prevPosesTmp = new HashSet<>();
        Map<Vector2iSeedHash, Integer> puffinsTmp = new HashMap<>();
        escape = setPuffins(puffinsTmp, prevPosesTmp, 0, startX, startY, new Vector2iSeedHash(startX, startY), exitWhenFindEscape);
        prevPosses.addAll(prevPosesTmp);

        Set<Vector2i> puffinsSet = new HashSet<>();
        if (escape && exitWhenFindEscape) return true;

        if (!puffinsTmp.isEmpty()) {
            puffinsSet.addAll(getSortedPuffins(puffinsTmp, sortedByEscapeDistance));
        }

        puffinsSet.forEach(puffin -> puff(puffin, true));
        puffins.clear();
        puffins.addAll(puffinsSet);

        return escape;
    }

    private void validateStartPosition() {
        if (LEntity.values()[labyrinth[startX][startY]] != LEntity.EMPTY) {
            throw new UnsupportedOperationException("Starting position is not empty.");
        }
    }

    private List<Vector2i> getSortedPuffins(Map<Vector2iSeedHash, Integer> puffinsTmp, boolean sortedByEscapeDistance) {
        List<Vector2iSeedHash> sortedPuffins = puffinsTmp.entrySet().stream()
            .sorted((e1, e2) -> sortedByEscapeDistance ?
                e1.getKey().getDistance(this.escapePos).compareTo(e2.getKey().getDistance(this.escapePos)) :
                e1.getValue().compareTo(e2.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        List<Vector2i> puffinsSet = new ArrayList<>();
        puffinsSet.add(sortedPuffins.get(0));
        if (!escape) puffinsSet.add(sortedPuffins.get(sortedPuffins.size() - 1));
        else puffinsSet.add(sortedPuffins.get((int) (sortedPuffins.size() / 1.6)));

        return puffinsSet;
    }

    public int[][] get2D() {
        return labyrinth;
    }

    public int[][] get3D() {
        return labyrinthFin;
    }

    public boolean isFin() {
        return puffins.isEmpty();
    }

    public void convertTo3dGame() {
        prevPosses.clear();
        puffins.clear();
        buildFourth();
    }

    public void buildFourth() {
        if (!escape) {
            Vector2iSeedHash escapePos = new Vector2iSeedHash(this.escapePos.x, this.escapePos.y);
            do {
                escapePos.x = MathUtils.random(0, width - 1);
                escapePos.y = MathUtils.random(0, height - 1);
            } while (LEntity.values()[labyrinth[escapePos.x][escapePos.y]] == LEntity.EMPTY);
            labyrinth[escapePos.x][escapePos.y] = LEntity.EMPTY.ordinal();
        }

        for (int j = height - 2; j >= 1; j--) {
            for (int i = 1; i < width - 1; i++) {
                if (this.escapePos.x == i && this.escapePos.y == j) continue;
                Info info = new Info(labyrinth, i, j, width, height);
                if (info.notEmptyEntities == 4 && info.entity == LEntity.EMPTY) {
                    int randomDirection = MathUtils.random(0, 3);
                    Vector2iSeedHash digPos = getNextPos(i, j, Direction.values()[randomDirection]);
                    labyrinth[digPos.x][digPos.y] = LEntity.EMPTY.ordinal();
                }
            }
        }
        convertToMapWithCorners();
    }

    private List<Direction> getDirections(int x, int y, Vector2iSeedHash prefPos, boolean dig, boolean usePrevPoses) {
        List<Direction> directions = new ArrayList<>();
        LEntity leftEntity = LEntity.values()[labyrinth[x - 1][y]];
        LEntity rightEntity = LEntity.values()[labyrinth[x + 1][y]];
        LEntity upEntity = LEntity.values()[labyrinth[x][y + 1]];
        LEntity downEntity = LEntity.values()[labyrinth[x][y - 1]];

        boolean canGoLeft = x > 1 && (prefPos == null || prefPos.x != x - 1);
        boolean canGoRight = x < width - 2 && (prefPos == null || prefPos.x != x + 1);
        boolean canGoUp = y < height - 2 && (prefPos == null || prefPos.y != y + 1);
        boolean canGoDown = y > 1 && (prefPos == null || prefPos.y != y - 1);

        boolean isLeftOpen = !dig == (leftEntity == LEntity.EMPTY) && canGoLeft;
        boolean isRightOpen = !dig == (rightEntity == LEntity.EMPTY) && canGoRight;
        boolean isUpOpen = !dig == (upEntity == LEntity.EMPTY) && canGoUp;
        boolean isDownOpen = !dig == (downEntity == LEntity.EMPTY) && canGoDown;

        if (dig) {
            isLeftOpen = isLeftOpen && leftEntity != LEntity.HORIZONTAL_WALL;
            isRightOpen = isRightOpen && rightEntity != LEntity.HORIZONTAL_WALL;
            isUpOpen = isUpOpen && upEntity != LEntity.VERTICAL_WALL;
            isDownOpen = isDownOpen && downEntity != LEntity.VERTICAL_WALL;

            if (usePrevPoses && !prevPosses.isEmpty()) {
                canGoLeft = x > 2;
                canGoRight = x < width - 3;
                canGoUp = y < height - 3;
                canGoDown = y > 2;

                Vector2iSeedHash throughWallLeft = new Vector2iSeedHash(x - 2, y);
                Vector2iSeedHash throughWallRight = new Vector2iSeedHash(x + 2, y);
                Vector2iSeedHash throughWallUp = new Vector2iSeedHash(x, y + 2);
                Vector2iSeedHash throughWallDown = new Vector2iSeedHash(x, y - 2);

                isLeftOpen = isLeftOpen && canGoLeft && !prevPosses.contains(throughWallLeft);
                isRightOpen = isRightOpen && canGoRight && !prevPosses.contains(throughWallRight);
                isUpOpen = isUpOpen && canGoUp && !prevPosses.contains(throughWallUp);
                isDownOpen = isDownOpen && canGoDown && !prevPosses.contains(throughWallDown);
            }
        }

        if (isLeftOpen) directions.add(Direction.LEFT);
        if (isRightOpen) directions.add(Direction.RIGHT);
        if (isUpOpen) directions.add(Direction.UP);
        if (isDownOpen) directions.add(Direction.DOWN);

        return directions;
    }

    private void dig(Direction direction, int x, int y) {
        Vector2iSeedHash digPos = getNextPos(x, y, direction);
        labyrinth[digPos.x][digPos.y] = LEntity.EMPTY.ordinal();
    }

    private boolean setPuffins(Map<Vector2iSeedHash, Integer> puffins, Set<Vector2iSeedHash> prevPoses,
                               int distance, int x, int y, final Vector2iSeedHash prevPos, boolean exitWhenFindEscape) {
        if (LEntity.values()[labyrinth[x][y]] != LEntity.EMPTY) {
            throw new UnsupportedOperationException("Current position is not empty.");
        }
        Vector2iSeedHash currentPos = new Vector2iSeedHash(x, y);
        if (prevPoses.contains(currentPos)) {
            return false;
        }
        prevPoses.add(currentPos);
        if (currentPos.equals(this.escapePos)) {
            return true;
        }
        distance++;
        if (distance > maxDistance) {
            return false;
        }

        List<Direction> availableDirections = getDirections(x, y, prevPos, false, false);
        List<Direction> directions = new ArrayList<>();
        boolean cycleDetected = false;

        for (Direction direction : availableDirections) {
            Vector2iSeedHash nextPos = getNextPos(x, y, direction);
            if (!prevPoses.contains(nextPos)) {
                directions.add(direction);
            } else {
                cycleDetected = true;
            }
        }

        if (directions.isEmpty()) {
            if (cycleDetected) distance /= 1.5;
            if (!getDirections(x, y, prevPos, true, true).isEmpty()) {
                puffins.put(currentPos, distance);
            }
            return false;
        }

        boolean foundEscape = false;
        for (Direction direction : directions) {
            Vector2iSeedHash nextPos = getNextPos(x, y, direction);
            if (setPuffins(puffins, prevPoses, distance, nextPos.x, nextPos.y, currentPos, exitWhenFindEscape)) {
                if (exitWhenFindEscape) return true;
                foundEscape = true;
                if (distance > Math.pow(width * height, 0.65)) {
                    distance /= 2; // lead the remaining branches away from the exit
                }
            }
        }

        return foundEscape;
    }

    private Vector2iSeedHash getNextPos(int x, int y, Direction direction) {
        switch (direction) {
            case LEFT:
                return new Vector2iSeedHash(x - 1, y);
            case RIGHT:
                return new Vector2iSeedHash(x + 1, y);
            case UP:
                return new Vector2iSeedHash(x, y + 1);
            case DOWN:
                return new Vector2iSeedHash(x, y - 1);
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    private void puff(Vector2i puffin, boolean usePrevPoses) {
        List<Direction> directions = getDirections(puffin.x, puffin.y, null, true, usePrevPoses);
        if (directions.isEmpty()) return;
        int randomIndex = MathUtils.random(0, directions.size() - 1);
        dig(directions.get(randomIndex), puffin.x, puffin.y);
    }

    private void convertToMapWithCorners() {
        labyrinthFin = new int[width][height];
        for (int i = 0; i < width; i++) {
            System.arraycopy(labyrinth[i], 0, labyrinthFin[i], 0, height);
        }

        for (int i = 1; i < width; i++) {
            labyrinthFin[i][height - 1] = LEntity.HORIZONTAL_WALL.ordinal();
        }
        for (int j = height - 2; j >= 1; j--) {
            labyrinthFin[0][j] = LEntity.VERTICAL_WALL.ordinal();
            labyrinthFin[width - 1][j] = LEntity.VERTICAL_WALL.ordinal();
        }
        labyrinthFin[0][0] = LEntity.LU_CORNER.ordinal();
        labyrinthFin[width - 1][0] = LEntity.RU_CORNER.ordinal();
        labyrinthFin[0][height - 1] = LEntity.LD_CORNER.ordinal();
        labyrinthFin[width - 1][height - 1] = LEntity.RD_CORNER.ordinal();

        for (int j = height - 2; j >= 1; j--) {
            for (int i = 1; i < width - 1; i++) {
                setCorner(labyrinthFin, i, j, width, height);
            }
        }
        printFinalLabyrinth();
    }

    private void setCorner(int[][] labyrinth, int x, int y, int width, int height) {
        Info info = new Info(labyrinth, x, y, width, height);
        if (info.isCorner) {
            labyrinth[x][y] = info.cornerType.ordinal();
        }
    }

    private void printFinalLabyrinth() {
        System.out.println("Final Labyrinth:");
        for (int j = height - 1; j >= 0; j--) {
            System.out.println(Arrays.toString(labyrinthFin[j]));
        }
        System.out.println();
    }

    public int[][] getLabyrinth() {
        return labyrinth;
    }

    public int[][] getLabyrinthFin() {
        return labyrinthFin;
    }

    public Set<Vector2i> getPrevPosses() {
        return prevPosses;
    }

    public Set<Vector2i> getPuffins() {
        return puffins;
    }
}
