package io.github.labyrinthgenerator.labyrinth;

import java.util.Objects;

public class Pair<A, B> {

    public final A fst;
    public final B snd;

    public Pair(A fst, B snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public String toString() {
        return "Pair[" + fst + "," + snd + "]";
    }

    public boolean equals(Object other) {
        if (!(other instanceof Pair<?, ?>)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) other;
        return Objects.equals(fst, pair.fst) &&
            Objects.equals(snd, pair.snd);
    }

    public int hashCode() {
        if (fst == null) return (snd == null) ? 0 : snd.hashCode() + 1;
        else if (snd == null) return fst.hashCode() + 2;
        else return fst.hashCode() * 17 + snd.hashCode();
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

}
