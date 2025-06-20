package io.github.labyrinthgenerator.additional;

public class Test {

    public static void main(String[] args) {
        Object obj = new Object();
        synchronized (obj) {
            System.out.println("Synchronized on null");
        }
    }
}
