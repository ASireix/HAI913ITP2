package hai913i.tp2.graph;

import hai913i.tp2.coupling.Coupling;
import java.util.*;

/** Graphe non orienté : tous les sommets, seulement les arêtes de poids positif. */
public record WeightedGraph(List<String> nodes, List<Edge> edges) {
    public record Edge(String first, String second, double weight) {
        public Edge {
            if (first.equals(second) || !Double.isFinite(weight) || weight <= 0 || weight > 1)
                throw new IllegalArgumentException("Arete ou poids invalide");
            if (first.compareTo(second) > 0) {
                String swap = first; first = second; second = swap;
            }
        }
    }

    public WeightedGraph {
        if (new HashSet<>(nodes).size() != nodes.size())
            throw new IllegalArgumentException("Sommet duplique");
        nodes = nodes.stream().sorted().toList();
        var known = new HashSet<>(nodes);
        var pairs = new HashSet<Coupling.Pair>();
        for (var edge : edges) {
            if (!known.contains(edge.first()) || !known.contains(edge.second())
                    || !pairs.add(new Coupling.Pair(edge.first(), edge.second())))
                throw new IllegalArgumentException("Extremite absente ou arete dupliquee");
        }
        edges = edges.stream().sorted(Comparator.comparing(Edge::first).thenComparing(Edge::second)).toList();
    }

    public static WeightedGraph from(Coupling coupling) {
        return new WeightedGraph(coupling.classes(), coupling.ranked().stream()
                .map(e -> new Edge(e.pair().first(), e.pair().second(), e.value())).toList());
    }

    public List<Edge> top(int limit) {
        return edges.stream().sorted(Comparator.comparingDouble(Edge::weight).reversed()
                .thenComparing(Edge::first).thenComparing(Edge::second)).limit(limit).toList();
    }

    public List<String> isolated() {
        var connected = new HashSet<String>();
        edges.forEach(e -> { connected.add(e.first()); connected.add(e.second()); });
        return nodes.stream().filter(n -> !connected.contains(n)).toList();
    }
}
