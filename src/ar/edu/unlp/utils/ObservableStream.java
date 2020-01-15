package ar.edu.unlp.utils;

import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ObservableStream extends Observable {
    private final Queue<String> lines = new ConcurrentLinkedQueue<>();

    public void addLine(String line) {
        lines.add(line);
        setChanged();
        notifyObservers();
    }

    public String nextLine() {
        return lines.poll();
    }
}