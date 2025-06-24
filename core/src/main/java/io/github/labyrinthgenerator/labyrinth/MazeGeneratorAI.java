package io.github.labyrinthgenerator.labyrinth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGeneratorAI {
    private final int width;
    private final int height;
    private final int[][] maze;
    private final Random random = new Random();

    public MazeGeneratorAI(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new int[height][width];

        // Инициализируем лабиринт стенами
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = 1; // 1 - стена
            }
        }

        generateMaze(1, 1); // Начинаем с точки (1, 1)
    }

    private void generateMaze(int startX, int startY) {
        List<int[]> stack = new ArrayList<>();
        stack.add(new int[]{startX, startY});
        maze[startY][startX] = 0; // Открываем начальную точку

        // Определяем возможные направления (верх, низ, лево, право)
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        while (!stack.isEmpty()) {
            int[] current = stack.remove(stack.size() - 1);
            int x = current[0];
            int y = current[1];

            List<Integer> directions = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                directions.add(i);
            }
            Collections.shuffle(directions); // Перемешиваем направления

            // Проходим по всем направлениям
            for (int direction : directions) {
                int nx = x + dx[direction] * 2;
                int ny = y + dy[direction] * 2;

                // Проверяем границы
                if (nx > 0 && ny > 0 && nx < width && ny < height && maze[ny][nx] == 1) {
                    maze[y + dy[direction]][x + dx[direction]] = 0; // Убираем стену
                    maze[ny][nx] = 0; // Открываем путь
                    stack.add(new int[]{nx, ny}); // Добавляем новую точку в стек
                }
            }
        }
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (maze[y][x] == 1) {
                    // Проверяем, является ли текущая клетка стеной и выводим соответствующий символ
                    if (x > 0 && maze[y][x - 1] == 1) {
                        System.out.print("_"); // Вертикальная стена
                    } else {
                        System.out.print("|"); // Горизонтальная стена
                    }
                } else {
                    System.out.print(" "); // Проход
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int width = 51; // Ширина (должна быть нечетной)
        int height = 51; // Высота (должна быть нечетной)

        MazeGeneratorAI mazeGenerator = new MazeGeneratorAI(width, height);
        mazeGenerator.printMaze();
    }
}
