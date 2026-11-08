package hai913i.tp1;

import hai913i.tp1.cli.CliOptions;
import hai913i.tp1.parse.*;
import hai913i.tp1.report.ConsoleReporter;
import hai913i.tp1.metrics.MetricsService;
import hai913i.tp1.callgraph.CallGraphService;
import java.util.List;

/** Point d'entrée : validation des options, orchestration et erreurs utilisateur. */
public final class Main {
    private Main() {}
    public static void main(String[] args) { System.exit(run(args)); }

    public static int run(String[] args) {
        CliOptions options;
        try { options = CliOptions.parse(args); }
        catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.err.print(CliOptions.USAGE);
            return 2;
        }
        if (options.help()) { System.out.print(CliOptions.USAGE); return 0; }
        try {
            var sources = ProjectSources.of(options.project());
            if (sources.javaFiles().isEmpty()) {
                System.err.println("Aucun fichier Java dans : " + sources.sourceRoot());
                return 3;
            }
            var files = JdtParser.parse(sources, List.of());
            var facts = FactExtractor.extract(sources.sourceRoot(), files);
            var reporter = new ConsoleReporter(System.out);
            reporter.displaySummary(sources.sourceRoot(), facts.files().size(), facts.errors());
            if (options.wants("ast")) {
                System.out.println("-----------------PARTIE A1---------------------");
                for (var file : files) {
                    // L'action ast explicite explore tout projet ; all garde la démonstration A1.
                    String name = file.path().getFileName().toString();
                    if (options.action().equals("ast") || name.equals("Dvd.java") || name.equals("Category.java")) {
                        System.out.println("Analyzing " + file.path());
                        file.unit().accept(new JdtVisitor(options.lite()));
                    }
                }
            }
            if (options.wants("structure")) reporter.displayStructure(facts.types());
            if (options.wants("calls")) reporter.displayCalls(facts.types());
            if (options.action().equals("all")) reporter.displayTotals(facts.types());
            if (options.wants("metrics")) reporter.displayMetrics(new MetricsService().calculate(facts, options.threshold()));
            if (options.wants("graph")) {
                var graph = new CallGraphService().build(facts);
                if (options.callers() != null) graph.callers(options.callers());
                if (options.callees() != null) graph.callees(options.callees());
                reporter.displayGraph(graph);
                if (options.callers() != null) reporter.displayGraphQuery(graph, options.callers(), true);
                if (options.callees() != null) reporter.displayGraphQuery(graph, options.callees(), false);
            }
            return 0; // Des diagnostics source n'empêchent pas une analyse partielle.
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            return 3;
        } catch (Exception e) {
            System.err.println("Analyse impossible : " + e.getClass().getSimpleName() + " : " + e.getMessage());
            return 4;
        }
    }
}
