package hai913i.tp1.callgraph;

import java.util.*;

/** Graphe orienté des méthodes comptées ; collections ordonnées et non modifiables. */
public record CallGraph(Set<String> nodes, List<Edge> edges, int externalCalls, int unresolvedCalls) {
    public record Edge(String caller, String callee, int weight) {}
    public CallGraph {
        nodes = Collections.unmodifiableSet(new TreeSet<>(nodes));
        edges = edges.stream().sorted(Comparator.comparing(Edge::caller).thenComparing(Edge::callee)).toList();
    }
    public int internalCalls() { return edges.stream().mapToInt(Edge::weight).sum(); }
    public List<Edge> callees(String method) {
        requireMethod(method);
        return edges.stream().filter(e -> e.caller().equals(method)).toList();
    }
    public List<Edge> callers(String method) {
        requireMethod(method);
        return edges.stream().filter(e -> e.callee().equals(method)).toList();
    }
    private void requireMethod(String method) {
        if (!nodes.contains(method)) throw new IllegalArgumentException("Méthode inconnue : " + method);
    }
}
