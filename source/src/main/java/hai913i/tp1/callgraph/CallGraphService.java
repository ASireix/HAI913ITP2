package hai913i.tp1.callgraph;

import hai913i.tp1.model.ProjectFacts;
import java.util.HashMap;
import java.util.Map;

/** Construit les arcs depuis les faits, sans parcourir d'AST. */
public final class CallGraphService {
    private record Pair(String caller, String callee) {}

    public CallGraph build(ProjectFacts facts) {
        var nodes = facts.methodsById().keySet();
        Map<Pair, Integer> weights = new HashMap<>();
        int external = 0;
        int unresolved = 0;
        for (var call : facts.calls()) {
            if (!call.resolved() || call.targetId().isEmpty()) {
                unresolved++;
                continue;
            }
            String target = call.targetId().orElseThrow();
            // Seules les méthodes effectivement comptées dans le modèle sont des nœuds.
            if (!nodes.contains(target)) {
                external++;
                continue;
            }
            if (!nodes.contains(call.callerId()))
                throw new IllegalArgumentException("Appelante absente du modèle : " + call.callerId());
            weights.merge(new Pair(call.callerId(), target), 1, Integer::sum);
        }
        var edges = weights.entrySet().stream().map(e ->
                new CallGraph.Edge(e.getKey().caller(), e.getKey().callee(), e.getValue())).toList();
        return new CallGraph(nodes, edges, external, unresolved);
    }
}
