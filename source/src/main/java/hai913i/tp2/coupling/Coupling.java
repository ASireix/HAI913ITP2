package hai913i.tp2.coupling;

import java.util.*;

/** Table creuse des couplages : les couples absents valent zéro. */
public record Coupling(List<String> classes, int intraClassArcs, int interClassArcs,
                       Map<Pair, Integer> relations) {
    /** Couple non orienté, rangé par noms qualifiés. */
    public record Pair(String first, String second) {
        public Pair {
            if (first.compareTo(second) > 0) {
                String swap = first;
                first = second;
                second = swap;
            }
        }
    }

    public record Entry(Pair pair, int arcs, double value) {}

    public Coupling {
        classes = classes.stream().sorted().toList();
        relations = Map.copyOf(relations);
    }

    public double value(String a, String b) {
        if (a.equals(b) || interClassArcs == 0) return 0.0;
        return (double) relations.getOrDefault(new Pair(a, b), 0) / interClassArcs;
    }

    /** Chaque couple non orienté apparaît une seule fois ; égalités triées par nom. */
    public List<Entry> ranked() {
        return relations.entrySet().stream()
                .map(e -> new Entry(e.getKey(), e.getValue(),
                        (double) e.getValue() / interClassArcs))
                .sorted(Comparator.comparingDouble(Entry::value).reversed()
                        .thenComparing(e -> e.pair().first())
                        .thenComparing(e -> e.pair().second()))
                .toList();
    }

    public double sum() {
        return ranked().stream().mapToDouble(Entry::value).sum();
    }
}
