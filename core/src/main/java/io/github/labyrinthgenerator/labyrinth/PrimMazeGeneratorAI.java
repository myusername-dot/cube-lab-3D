package io.github.labyrinthgenerator.labyrinth;

import java.io.IOException;
import java.util.PriorityQueue;
import java.util.Random;

public class PrimMazeGeneratorAI {
    private final int width;
    private final int height;
    private final int[][] maze;
    private int playerX;
    private int playerY;
    private final Random random = new Random();
    private volatile boolean running = true;

    public PrimMazeGeneratorAI(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new int[height][width];

        // Инициализируем лабиринт стенами
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = 1; // 1 - стена
            }
        }

        // Начинаем с точки (1, 1)
        playerX = 1;
        playerY = 1;
        generateMaze(1, 1); // Генерация лабиринта

        // Запускаем поток для обработки ввода
        new Thread(this::handleInput).start();
    }

    private void generateMaze(int startX, int startY) {
        PriorityQueue<Edge> edges = new PriorityQueue<>();
        maze[startY][startX] = 0; // Открываем начальную точку

        addEdges(startX, startY, edges);

        while (!edges.isEmpty()) {
            Edge edge = edges.poll();
            int x = edge.x;
            int y = edge.y;

            if (maze[y][x] == 1) {
                maze[y][x] = 0; // Открываем клетку
                maze[(edge.prevY + y) / 2][(edge.prevX + x) / 2] = 0; // Убираем стену

                addEdges(x, y, edges);
            }
        }
    }

    private void addEdges(int x, int y, PriorityQueue<Edge> edges) {
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (int direction = 0; direction < 4; direction++) {
            int nx = x + dx[direction] * 2;
            int ny = y + dy[direction] * 2;

            if (nx > 0 && ny > 0 && nx < width && ny < height && maze[ny][nx] == 1) {
                edges.add(new Edge(nx, ny, x, y));
            }
        }
    }

    public void printMaze() {
        clearConsole();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == playerX && y == playerY) {
                    System.out.print("@"); // Игрок
                } else if (maze[y][x] == 1) {
                    System.out.print("|"); // Вертикальная стена
                } else {
                    System.out.print(" "); // Проход
                }
            }
            System.out.println();
        }
    }

    private void clearConsole() {
        // Очистка консоли (работает в большинстве терминалов)
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void movePlayer(int direction) {
        int newX = playerX;
        int newY = playerY;

        switch (direction) {
            case 119: newY--; break; // Вверх
            case 115: newY++; break; // Вниз
            case 97: newX--; break; // Влево
            case 100: newX++; break; // Вправо
        }

        // Проверяем, можно ли переместить игрока
        if (maze[newY][newX] == 0) {
            playerX = newX;
            playerY = newY;
        }
    }

    private void handleInput() {
        try {
            int input;
            while ((input = System.in.read()) != -1) {
                if (!running) { // Ctrl+C
                    break;
                }
                movePlayer(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Edge implements Comparable<Edge> {
        int x, y;
        int prevX, prevY;

        public Edge(int x, int y, int prevX, int prevY) {
            this.x = x;
            this.y = y;
            this.prevX = prevX;
            this.prevY = prevY;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(new Random().nextInt(), new Random().nextInt()); // Случайный порядок
        }
    }

    public static void main(String[] args) {
        int width = 21; // Ширина (должна быть нечетной)
        int height = 21; // Высота (должна быть нечетной)

        PrimMazeGeneratorAI mazeGenerator = new PrimMazeGeneratorAI(width, height);

        // Основной игровой цикл
        while (true) {
            mazeGenerator.printMaze();
            try {
                Thread.sleep(33); // Примерно 30 кадров в секунду
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
