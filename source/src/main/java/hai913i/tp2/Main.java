package hai913i.tp2;

import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.FactExtractor;
import hai913i.tp1.callgraph.CallGraphService;
import hai913i.tp2.input.InputFacts;
import hai913i.tp2.input.Tp1FactsAdapter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import hai913i.tp2.coupling.CouplingService;
import hai913i.tp2.graph.GraphIO;
import hai913i.tp2.graph.WeightedGraph;
import hai913i.tp2.clustering.ClusteringService;
import hai913i.tp2.clustering.Dendrogram;
import hai913i.tp2.clustering.ModuleCut;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

/** Orchestration A0 : production TP1 en amont ou chargement CSV de référence. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        int code = run(args);
        if (code != 0) System.exit(code);
    }

    public static int run(String[] args) {
        Path output = null;
        Double cp = null;
        ClusteringService.Linkage linkage = ClusteringService.Linkage.AVERAGE;
        boolean linkageSet = false;
        CouplingService.RelationMode relationMode = CouplingService.RelationMode.CALLS;
        boolean relationsSet = false;
        boolean gui = false;
        try {
            if (args.length < 2 || !(args[0].equals("--tp1") || args[0].equals("--csv")))
                throw new IllegalArgumentException("Choisir --tp1 ou --csv avec un chemin");
            for (int i = 2; i < args.length; i++) {
                if (args[i].equals("--gui") && !gui) { gui = true; continue; }
                if (i + 1 >= args.length) throw new IllegalArgumentException("Valeur d'option manquante");
                if (args[i].equals("--output") && output == null) output = Path.of(args[i + 1]);
                else if (args[i].equals("--cp") && cp == null) {
                    cp = Double.parseDouble(args[i + 1]);
                    if (!Double.isFinite(cp) || cp < 0) throw new IllegalArgumentException("CP doit etre fini et positif ou nul");
                } else if (args[i].equals("--linkage") && !linkageSet) {
                    linkage = switch (args[i + 1]) {
                        case "average" -> ClusteringService.Linkage.AVERAGE;
                        case "single" -> ClusteringService.Linkage.SINGLE;
                        default -> throw new IllegalArgumentException("Liaison attendue : average ou single");
                    };
                    linkageSet = true;
                } else if (args[i].equals("--relations") && !relationsSet) {
                    relationMode = switch (args[i + 1]) {
                        case "calls" -> CouplingService.RelationMode.CALLS;
                        case "enriched" -> CouplingService.RelationMode.ENRICHED;
                        default -> throw new IllegalArgumentException("Relations attendues : calls ou enriched");
                    };
                    relationsSet = true;
                } else throw new IllegalArgumentException("Option inconnue ou repetee : " + args[i]);
                i++;
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage : java -jar target/hai913i-tp2.jar (--tp1 <projet-Java> | --csv <dossier-faits>) [--output <dossier>] [--cp <seuil>] [--linkage average|single] [--relations calls|enriched] [--gui]");
            return 1;
        }
        try {
            InputFacts input;
            if (args[0].equals("--csv")) {
                input = Facts.load(Path.of(args[1]));
            } else {
                var sources = ProjectSources.of(Path.of(args[1]));
                if (sources.javaFiles().isEmpty()) {
                    throw new IllegalArgumentException("Aucun fichier Java dans le projet");
                }
                var parsed = JdtParser.parse(sources, List.of());
                var project = FactExtractor.extract(sources.sourceRoot(), parsed);
                if (!project.errors().isEmpty()) {
                    project.errors().forEach(System.err::println);
                    throw new IllegalArgumentException("Corriger les diagnostics avant de valider A0");
                }
                input = new Tp1FactsAdapter(project, new CallGraphService().build(project));
            }
            System.out.println("Classes : " + input.classNames().size());
            System.out.println("Methodes : " + input.methodOwners().size());
            System.out.println("Arcs du graphe : " + input.calls().size());
            var coupling = new CouplingService().calculate(input, relationMode);
            System.out.println("Relations : " + relationMode);
            if (relationMode == CouplingService.RelationMode.ENRICHED) {
                System.out.println("C2 - Relations structurelles ajoutees : " + input.structuralRelations().size());
                input.structuralRelations().forEach(r -> System.out.println(r.source() + " -- " + r.target() + " : " + r.kind()));
            }
            System.out.println("A1 - Arcs intra-classe ecartes : " + coupling.intraClassArcs());
            System.out.println("Relations inter-classes (denominateur) : " + coupling.interClassArcs());
            System.out.println("Couples non nuls : " + coupling.relations().size());
            for (var entry : coupling.ranked()) {
                System.out.printf(Locale.ROOT, "%s -- %s : r=%d, C=%.4f%n",
                        entry.pair().first(), entry.pair().second(), entry.arcs(), entry.value());
            }
            System.out.printf(Locale.ROOT, "Somme des couplages (couples non orientes) : %.4f%n", coupling.sum());
            var graph = WeightedGraph.from(coupling);
            System.out.printf("A2 - Graphe : %d noeuds, %d aretes%n", graph.nodes().size(), graph.edges().size());
            System.out.println("Les cinq couples les plus couples :");
            for (var edge : graph.top(5))
                System.out.printf(Locale.ROOT, "%s -- %s : %.4f%n", edge.first(), edge.second(), edge.weight());
            System.out.println("Classes isolees : " + graph.isolated());
            var clustering = new ClusteringService(linkage).cluster(coupling);
            System.out.println("Liaison : " + linkage);
            System.out.println("B1 - Fusions : " + clustering.merges().size());
            for (var merge : clustering.merges())
                System.out.printf(Locale.ROOT, "%d + %d -> %d : %.4f %s + %s%n",
                        merge.left().id(), merge.right().id(), merge.result().id(),
                        merge.similarity(), merge.left().members(), merge.right().members());
            var tree = Dendrogram.from(clustering);
            System.out.println("B2 - Dendrogramme :");
            System.out.print(tree.text());
            ModuleCut.Result cut = null;
            if (cp != null) {
                cut = new ModuleCut().cut(tree, coupling, cp);
                System.out.printf(Locale.ROOT, "B3 - CP=%.4f : %d modules (maximum %d)%n", cp, cut.modules().size(), cut.maxModules());
                for (var module : cut.modules())
                    System.out.printf(Locale.ROOT, "G%d : %d classes, moyenne=%s %s%n", module.groupId(),
                            module.classes().size(), module.mean().isPresent()
                                    ? String.format(Locale.ROOT, "%.6f", module.mean().getAsDouble()) : "NA (singleton)", module.classes());
                System.out.println(cut.valid() ? "Coupe acceptee : contrainte M/2 respectee."
                        : "Coupe rejetee : contrainte M/2 violee.");
            } else System.out.println("B3 : fournir --cp <seuil> pour effectuer une coupe (aucun seuil par defaut).");
            if (output != null) {
                GraphIO.write(graph, output);
                Files.writeString(output.resolve("merges.csv"), clustering.trace(), StandardCharsets.UTF_8);
                Files.writeString(output.resolve("dendrogram.txt"), tree.text(), StandardCharsets.UTF_8);
                Files.writeString(output.resolve("dendrogram.dot"), tree.dot(linkage), StandardCharsets.UTF_8);
                Files.writeString(output.resolve("linkage.txt"), linkage.name() + "\n", StandardCharsets.UTF_8);
                Files.writeString(output.resolve("relations.txt"), relationMode.name() + "\n", StandardCharsets.UTF_8);
                if (relationMode == CouplingService.RelationMode.ENRICHED) {
                    var structural = new StringBuilder("source;target;kind\n");
                    input.structuralRelations().forEach(r -> structural.append(r.source()).append(';')
                            .append(r.target()).append(';').append(r.kind()).append('\n'));
                    Files.writeString(output.resolve("structural-relations.csv"), structural.toString(), StandardCharsets.UTF_8);
                }
                if (cut != null) Files.writeString(output.resolve("modules.csv"), cut.csv(), StandardCharsets.UTF_8);
                if (!graph.equals(GraphIO.read(output)))
                    throw new IllegalStateException("Le graphe recharge differe de l'original");
                System.out.println("Exports CSV et DOT : " + output.toAbsolutePath());
                System.out.println("Rechargement CSV verifie : graphe identique.");
            }
            if (gui) {
                hai913i.tp2.gui.ExplorerPanel.open(coupling, linkage, cp, relationMode.name());
                return 0; // La fenêtre reste active, même si la coupe initiale est rejetée.
            }
            return cut != null && !cut.valid() ? 3 : 0;
        } catch (Exception e) {
            System.err.println("Analyse : " + e.getMessage());
            return 2;
        }
    }
}
