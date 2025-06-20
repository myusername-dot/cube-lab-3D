package io.github.labyrinthgenerator.additional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        String fileName = "C:\\Users\\andre\\Documents\\log.txt";
        List<String> lines = new ArrayList<>();
        File txtFile = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            do {
                line = reader.readLine();
                if (line != null) {
                    lines.add(line);
                    assert line.length() != 0;
                }
            }
            while (line != null);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        assert lines.size() != 0;
        assert lines.get(lines.size() - 1).length() != 0;

        System.out.println("Start\n");
        List<String> finder = new ArrayList<>();
        int matchedLines = 0;
        for (String line : lines) {
            if (finder.contains(line)) continue;
            if (line.matches(".*Block synchronized.*")) {
                matchedLines++;
                String endLine = line.replace("Block synchronized", "Block end synchronized");
                long countBlock = lines.stream().filter(l -> l.equals(line)).count();
                long countEndBlock = lines.stream().filter(l -> l.equals(endLine)).count();
                if (countBlock != countEndBlock) {
                    System.out.println("line: " + line + "\nCount blocks: " + countBlock + " count end blocks: " + countEndBlock);
                    finder.add(line);
                }
            }
        }
        System.out.println("Matched lines: " + matchedLines);
        System.out.println("End");
    }
}
