package hai913i.tp1;

import static org.junit.jupiter.api.Assertions.*;
import hai913i.tp1.callgraph.*;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.parse.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CallGraphTest {
    private CallGraph graph(String path) throws Exception {
        var sources = ProjectSources.of(Path.of(path));
        return new CallGraphService().build(FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of())));
    }
    @Test void checkpointAndQueries() throws Exception {
        var graph = graph("resources/validation");
        assertEquals(59, graph.nodes().size());
        assertEquals(44, graph.edges().size());
        assertEquals(46, graph.internalCalls());
        assertEquals(48, graph.externalCalls());
        assertEquals(0, graph.unresolvedCalls());
        String add = "library.service.Catalog#add(library.model.Item,library.model.Item)";
        assertEquals(List.of(new CallGraph.Edge(add, "library.service.Catalog#add(library.model.Item)", 2)), graph.callees(add));
        String repeat = "library.util.TextUtils#repeat(java.lang.String,int)";
        assertTrue(graph.callees(repeat).stream().anyMatch(e -> e.callee().equals(repeat)));
        assertEquals(List.of("library.service.LoanService#borrow(library.model.Member,java.lang.String,int)"),
                graph.callers("library.model.Loanable#checkOut(library.model.Member)").stream().map(CallGraph.Edge::caller).toList());
        assertTrue(graph.callers("library.model.Item#checkOut(library.model.Member)").isEmpty());
        assertTrue(graph.nodes().contains("library.util.TextUtils#<init>()"));
        assertEquals(graph, graph("resources/validation"));
        assertThrows(IllegalArgumentException.class, () -> graph.callers("missing"));
    }
    @Test void unresolvedAndEmptyGraphs() throws Exception {
        var graph = graph("resources/robustness/missing-dep");
        assertEquals(1, graph.unresolvedCalls());
        assertEquals(1, graph.internalCalls());
        var empty = new CallGraphService().build(new ProjectFacts(List.of(), List.of(), List.of()));
        assertTrue(empty.nodes().isEmpty());
        assertTrue(empty.edges().isEmpty());
    }
}
