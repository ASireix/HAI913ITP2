package hai913i.tp2;

import hai913i.tp2.coupling.CouplingService;
import hai913i.tp2.graph.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class A2Test {
    @TempDir Path temp;

    @Test void checkpointEtExportSansPerte() throws Exception {
        var graph = WeightedGraph.from(new CouplingService().calculate(Facts.load(Path.of("../resources"))));
        assertEquals(15, graph.nodes().size());
        assertEquals(19, graph.edges().size());
        assertEquals(List.of("library.model.Dvd", "library.service.LoanException"), graph.isolated());
        assertEquals(List.of(
            new WeightedGraph.Edge("library.model.Item", "library.service.LoanService", 4.0 / 37),
            new WeightedGraph.Edge("library.service.LoanService", "library.service.LoanService.Loan", 4.0 / 37),
            new WeightedGraph.Edge("library.app.LibraryApp", "library.model.Item", 3.0 / 37),
            new WeightedGraph.Edge("library.app.LibraryApp", "library.service.Catalog", 3.0 / 37),
            new WeightedGraph.Edge("library.app.LibraryApp", "library.service.LoanService", 3.0 / 37)), graph.top(5));
        GraphIO.write(graph, temp);
        assertEquals(graph, GraphIO.read(temp));
        assertTrue(Files.readString(temp.resolve("coupling.dot")).contains("\"library.model.Dvd\";"));
        var second = temp.resolve("second");
        GraphIO.write(graph, second);
        for (String file : List.of("coupling-nodes.csv", "coupling-edges.csv", "coupling.dot"))
            assertArrayEquals(Files.readAllBytes(temp.resolve(file)), Files.readAllBytes(second.resolve(file)));
    }

    @Test void microJeuVideEtSommetsSansArete() throws Exception {
        var mini = WeightedGraph.from(new CouplingService().calculate(Facts.load(Path.of("../resources/mini"))));
        assertEquals(3, mini.nodes().size());
        assertEquals(2, mini.edges().size());
        assertTrue(mini.isolated().isEmpty());
        for (var graph : List.of(mini, new WeightedGraph(List.of(), List.of()),
                new WeightedGraph(List.of("demo.Alone"), List.of()))) {
            GraphIO.write(graph, temp);
            assertEquals(graph, GraphIO.read(temp));
        }
    }

    @Test void refuseUnFichierAuxExtremitesAbsentes() throws Exception {
        GraphIO.write(new WeightedGraph(List.of("A"), List.of()), temp);
        Files.writeString(temp.resolve("coupling-edges.csv"), "first;second;weight\nA;B;0.5\n");
        assertThrows(java.io.IOException.class, () -> GraphIO.read(temp));
    }
}
