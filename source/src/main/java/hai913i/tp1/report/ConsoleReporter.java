package hai913i.tp1.report;

import hai913i.tp1.model.AnalysisFacts.*;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Mise en forme des résultats : aucune dépendance à JDT. */
public final class ConsoleReporter {
    private final PrintStream out;
    public ConsoleReporter(PrintStream out) { this.out = out; }

    public void displaySummary(Path root, int fileCount, List<String> errors) {
        out.println("-----------------PARTIE A0---------------------");
        out.println("Racine des sources     : " + root);
        out.println("Unites de compilation  : " + fileCount);
        out.println("Erreurs de compilation : " + errors.size());
        errors.forEach(out::println);
    }

    public void displayStructure(List<TypeFact> types) {
        out.println("-----------------PARTIE A2---------------------");
        for (TypeFact type : sorted(types)) {
            out.println(type.kind() + " : " + type.name() + " [" + type.file() + "]");
            out.println("  Paquetage : " + type.packageName());
            out.println("  Superclasses : " + String.join(", ", type.superclasses()));
            out.println("  Interfaces directes : " + String.join(", ", type.interfaces()));
            for (FieldFact field : type.fields())
                out.println("  Attribut : " + field.visibility() + " " + field.type() + " " + field.name());
            for (MethodFact method : type.methods())
                out.println("  Methode : " + method.name() + " (" + method.parameterCount()
                        + " parametres)" + (method.constructor() ? " — constructeur" : ""));
            out.println("  Bilan : " + type.fields().size() + " attributs, " + type.methods().size() + " methodes");
        }
    }

    public void displayCalls(List<TypeFact> types) {
        out.println("-----------------PARTIE A3---------------------");
        for (TypeFact type : sorted(types)) {
            for (MethodFact method : type.methods()) {
                out.println(type.name() + "#" + method.name() + " (" + method.parameterCount() + " parametres)");
                if (method.calls().isEmpty()) out.println("  Aucun appel");
                for (CallFact call : method.calls()) {
                    out.println("  Ligne " + call.line() + " : " + call.name());
                    out.println("    Cible : " + call.target());
                    out.println("    Receveur : " + call.receiver());
                    out.println("    Statut : " + (!call.resolved() ? "non résolue" : call.internal() ? "interne" : "externe"));
                }
            }
        }
    }

    public void displayTotals(List<TypeFact> types) {
        List<MethodFact> methods = types.stream().flatMap(t -> t.methods().stream()).toList();
        List<CallFact> calls = methods.stream().flatMap(m -> m.calls().stream()).toList();
        out.println("-----------------TOTAUX---------------------");
        out.println("Types : " + types.size());
        for (String kind : List.of("Classe", "Interface", "Enumeration", "Record"))
            out.println("  " + kind + " : " + types.stream().filter(t -> t.kind().equals(kind)).count());
        out.println("Attributs : " + types.stream().mapToInt(t -> t.fields().size()).sum());
        out.println("Methodes : " + methods.size() + ", constructeurs : " + methods.stream().filter(MethodFact::constructor).count());
        out.println("Appels : " + calls.size() + ", ordinaires : " + calls.stream().filter(c -> !c.superCall()).count()
                + ", super : " + calls.stream().filter(CallFact::superCall).count());
        out.println("Internes : " + calls.stream().filter(c -> c.resolved() && c.internal()).count()
                + ", externes : " + calls.stream().filter(c -> c.resolved() && !c.internal()).count()
                + ", non resolus : " + calls.stream().filter(c -> !c.resolved()).count());
    }

    public void displayMetrics(hai913i.tp1.metrics.MetricsResult metrics) {
        out.println("-----------------PARTIE B2---------------------");
        out.println("Q1 Types : " + metrics.types());
        out.println("Q2 Lignes de l'application : " + metrics.sourceLoc());
        out.println("Q3 Methodes : " + metrics.methods());
        out.println("Q4 Paquetages : " + metrics.packages());
        out.printf(java.util.Locale.FRANCE, "Q5 Methodes par classe : %.2f%n", metrics.methodsPerType());
        out.printf(java.util.Locale.FRANCE, "Q6 Lignes par methode avec corps : %.2f (%d lignes / %d corps)%n",
                metrics.linesPerMethod(), metrics.bodyLoc(), metrics.bodyCount());
        out.printf(java.util.Locale.FRANCE, "Q7 Attributs par classe : %.2f%n", metrics.fieldsPerType());
        displayRanking("Q8 Classes avec le plus de methodes", metrics.mostMethods());
        displayRanking("Q9 Classes avec le plus d'attributs", metrics.mostFields());
        out.println("Q10 Intersection : " + String.join(", ", metrics.intersection()));
        displayRanking("Q11 Classes avec plus de " + metrics.threshold() + " methodes", metrics.aboveThreshold());
        out.println("Q12 Methodes les plus longues par classe :");
        metrics.longestMethods().forEach((type, methods) -> displayRanking("  " + type, methods));
        out.println("Q13 Maximum de parametres : " + metrics.maxParameters());
        metrics.maxParameterMethods().forEach(name -> out.println("  " + name));
    }

    private void displayRanking(String title, List<hai913i.tp1.metrics.MetricsResult.Ranked> entries) {
        out.println(title + " :");
        if (entries.isEmpty()) out.println("  Aucun");
        entries.forEach(entry -> out.println("  " + entry.name() + " : " + entry.value()));
    }

    public void displayGraph(hai913i.tp1.callgraph.CallGraph graph) {
        out.println("-----------------PARTIE B3---------------------");
        out.println("Noeuds : " + graph.nodes().size() + ", arcs : " + graph.edges().size());
        out.println("Sites internes : " + graph.internalCalls() + ", externes : " + graph.externalCalls()
                + ", non resolus : " + graph.unresolvedCalls());
        out.println("Methodes (y compris isolees) :");
        graph.nodes().forEach(node -> out.println("  " + node));
        out.println("Arcs :");
        displayEdges(graph.edges());
    }

    public void displayGraphQuery(hai913i.tp1.callgraph.CallGraph graph, String method, boolean callers) {
        var edges = callers ? graph.callers(method) : graph.callees(method);
        out.println((callers ? "Appelantes de " : "Appelees de ") + method + " :");
        displayEdges(edges);
    }

    private void displayEdges(List<hai913i.tp1.callgraph.CallGraph.Edge> edges) {
        if (edges.isEmpty()) out.println("  Aucun arc");
        edges.forEach(edge -> out.println("  " + edge.caller() + " -> " + edge.callee() + " [poids=" + edge.weight() + "]"));
    }

    private List<TypeFact> sorted(List<TypeFact> types) {
        return types.stream().sorted(Comparator.comparing(TypeFact::name)).toList();
    }
}
