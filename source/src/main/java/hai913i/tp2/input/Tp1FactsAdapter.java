package hai913i.tp2.input;

import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.callgraph.CallGraph;
import hai913i.tp2.Facts;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

/** Transforme des faits déjà extraits, sans refaire l'analyse. */
public final class Tp1FactsAdapter implements InputFacts {
    private final ProjectFacts project;
    private final CallGraph graph;

    public Tp1FactsAdapter(ProjectFacts project, CallGraph graph) {
        this.project = project;
        this.graph = graph;
    }

    @Override
    public List<String> classNames() {
        // Partir des déclarations conserve également les types isolés.
        return project.types().stream().map(type -> type.name()).sorted().toList();
    }

    @Override
    public Map<String, String> methodOwners() {
        Map<String, String> owners = new TreeMap<>();
        for (var type : project.types()) {
            for (var method : type.methods()) {
                owners.put(method.id(), type.name());
            }
        }
        return Collections.unmodifiableMap(owners);
    }

    @Override
    public List<StructuralRelation> structuralRelations() {
        var known = new java.util.HashSet<>(classNames());
        List<StructuralRelation> relations = new ArrayList<>();
        for (var type : project.types()) {
            // Le modèle TP1 stocke la chaîne des superclasses, de la plus proche à la plus lointaine.
            // Ne prendre que la première AVANT de filtrer les types externes.
            if (!type.superclasses().isEmpty()) {
                String parent = type.superclasses().get(0);
                if (known.contains(parent)) relations.add(new StructuralRelation(type.name(), parent, "extends"));
            }
            for (String iface : type.interfaces())
                if (known.contains(iface)) relations.add(new StructuralRelation(type.name(), iface, "interface"));
        }
        return relations.stream().distinct().sorted(java.util.Comparator
                .comparing(StructuralRelation::source).thenComparing(StructuralRelation::target)
                .thenComparing(StructuralRelation::kind)).toList();
    }

    @Override
    public List<Facts.CallArc> calls() {
        var owners = methodOwners();
        List<Facts.CallArc> arcs = new ArrayList<>();
        for (var edge : graph.edges()) {
            String callerClass = owners.get(edge.caller());
            String calleeClass = owners.get(edge.callee());
            if (callerClass == null || calleeClass == null) {
                throw new IllegalArgumentException("Arc lié à une méthode absente du modèle");
            }
            // Un arc reste un seul élément, quel que soit son nombre de sites.
            arcs.add(new Facts.CallArc(edge.caller(), callerClass,
                    edge.callee(), calleeClass, edge.weight()));
        }
        return List.copyOf(arcs);
    }
}
