package hai913i.tp2;

import hai913i.tp2.coupling.*;
import hai913i.tp2.input.Tp1FactsAdapter;
import hai913i.tp1.parse.*;
import hai913i.tp1.callgraph.CallGraphService;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class A1Test {
    private final CouplingService service = new CouplingService();

    @Test void checkpointSurNotreTp1EtComparaisonCanonique() throws Exception {
        var sources = ProjectSources.of(Path.of("resources/validation"));
        var project = FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of()));
        var c = service.calculate(new Tp1FactsAdapter(project, new CallGraphService().build(project)));
        assertEquals(service.calculate(Facts.load(Path.of("../resources"))), c);
        assertEquals(15, c.classes().size());
        assertEquals(7, c.intraClassArcs());
        assertEquals(37, c.interClassArcs());
        assertEquals(19, c.relations().size());
        assertEquals(1.0, c.sum(), 1e-9);
        assertEquals(4.0 / 37, c.value("library.model.Item", "library.service.LoanService"), 1e-9);
        for (String a : c.classes()) {
            assertEquals(0.0, c.value(a, a));
            for (String b : c.classes()) {
                assertEquals(c.value(a, b), c.value(b, a));
                assertTrue(c.value(a, b) >= 0 && c.value(a, b) <= 1);
            }
            assertEquals(0.0, c.value(a, "library.model.Dvd"));
            assertEquals(0.0, c.value(a, "library.service.LoanException"));
        }
        assertEquals(4, c.ranked().get(0).arcs());
    }

    @Test void microJeuEtAppelsDansLesDeuxSens() throws Exception {
        var c = service.calculate(Facts.load(Path.of("../resources/mini")));
        assertEquals(1, c.intraClassArcs());
        assertEquals(3, c.interClassArcs());
        assertEquals(2.0 / 3, c.value("demo.A", "demo.B"), 1e-9);
        assertEquals(1.0 / 3, c.value("demo.B", "demo.C"), 1e-9);
        assertEquals(0.0, c.value("demo.A", "demo.C"));
        var pair = new Coupling.Pair("demo.B", "demo.A");
        assertEquals(new Coupling.Pair("demo.A", "demo.B"), pair);
        // Deux arcs opposés sont deux relations de méthodes, agrégées dans UN couple de classes.
        var facts = new hai913i.tp2.input.InputFacts() {
            public List<String> classNames() { return List.of("A", "B"); }
            public java.util.Map<String, String> methodOwners() { return java.util.Map.of("a", "A", "b", "B"); }
            public List<Facts.CallArc> calls() { return List.of(
                    new Facts.CallArc("a", "A", "b", "B", 9),
                    new Facts.CallArc("b", "B", "a", "A", 1)); }
        };
        var both = service.calculate(facts);
        assertEquals(2, both.interClassArcs());
        assertEquals(1, both.relations().size());
        assertEquals(1.0, both.value("A", "B"));
    }

    @Test void aucunArcInterClasseNeDivisePasParZero() {
        var facts = new hai913i.tp2.input.InputFacts() {
            public List<String> classNames() { return List.of("A"); }
            public java.util.Map<String, String> methodOwners() { return java.util.Map.of("a", "A"); }
            public List<Facts.CallArc> calls() { return List.of(new Facts.CallArc("a", "A", "a", "A", 2)); }
        };
        var c = service.calculate(facts);
        assertEquals(0, c.interClassArcs());
        assertEquals(0.0, c.value("A", "A"));
        assertEquals(0.0, c.sum());
        assertTrue(c.ranked().isEmpty());
    }
}
