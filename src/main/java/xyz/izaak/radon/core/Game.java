package xyz.izaak.radon.core;

public class Game {
    private String name;

    public Game(String name) {
        this.name = name;
    }

    public void run() {
        System.out.printf("Game %s has run!%n", name);
    }
}
