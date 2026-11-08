package hai913i.tp2;

import hai913i.tp1.parse.*;
import hai913i.tp1.callgraph.CallGraphService;
import hai913i.tp2.input.*;
import hai913i.tp2.coupling.*;
import hai913i.tp2.clustering.*;
import hai913i.tp2.graph.WeightedGraph;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class C2Test {
    @Test void relationsDirectesEtNormalisation() throws Exception {
        var sources = ProjectSources.of(Path.of("resources/validation"));
        var project = FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of()));
        var input = new Tp1FactsAdapter(project, new CallGraphService().build(project));
        var extra = input.structuralRelations();
        assertEquals(List.of(
                new InputFacts.StructuralRelation("library.model.Book", "library.model.Item", "extends"),
                new InputFacts.StructuralRelation("library.model.Dvd", "library.model.Item", "extends"),
                new InputFacts.StructuralRelation("library.model.Item", "library.model.Loanable", "interface"),
                new InputFacts.StructuralRelation("library.service.ConsoleNotifier", "library.service.Notifier", "interface")), extra);
        var service = new CouplingService();
        var base = service.calculate(input);
        var enriched = service.calculate(input, CouplingService.RelationMode.ENRICHED);
        assertEquals(37, base.interClassArcs());
        assertEquals(41, enriched.interClassArcs());
        assertEquals(22, enriched.relations().size());
        assertEquals(1.0, enriched.sum(), 1e-9);
        assertEquals(1.0 / 41, enriched.value("library.model.Dvd", "library.model.Item"), 1e-9);
        assertEquals(2.0 / 41, enriched.value("library.model.Book", "library.model.Item"), 1e-9);
        assertEquals(List.of("library.service.LoanException"), WeightedGraph.from(enriched).isolated());
        for (String a : enriched.classes()) for (String b : enriched.classes())
            assertEquals(enriched.value(a, b), enriched.value(b, a));
        var tree = Dendrogram.from(new ClusteringService().cluster(enriched));
        assertEquals(15, tree.root().orElseThrow().leafCount());
        var cut = new ModuleCut().cut(tree, enriched, 0.025);
        assertEquals(8, cut.modules().size());
        assertFalse(cut.valid());
        var scaled = new ModuleCut().cut(tree, enriched, 0.025 * 37 / 41);
        assertEquals(7, scaled.modules().size());
        assertTrue(scaled.valid());
        assertEquals(7, new ModuleCut().cut(Dendrogram.from(new ClusteringService().cluster(base)), base, 0.025).modules().size());
        assertEquals(enriched.classes(), cut.modules().stream().flatMap(m -> m.classes().stream()).sorted().toList());
    }

    @Test void refuseLesCsvSansRelationsStructurelles() throws Exception {
        var facts = Facts.load(Path.of("../resources"));
        assertThrows(UnsupportedOperationException.class, () -> new CouplingService().calculate(facts,
                CouplingService.RelationMode.ENRICHED));
    }
}
