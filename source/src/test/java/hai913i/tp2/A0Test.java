package hai913i.tp2;

import hai913i.tp1.callgraph.CallGraphService;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.parse.*;
import hai913i.tp2.input.Tp1FactsAdapter;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class A0Test {
    @Test void adapteLeTp1EtRetrouveTousLesFaitsCanoniques() throws Exception {
        var sources = ProjectSources.of(Path.of("resources/validation"));
        var project = FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of()));
        assertTrue(project.errors().isEmpty());
        var input = new Tp1FactsAdapter(project, new CallGraphService().build(project));
        var canonical = Facts.load(Path.of("../resources"));
        assertEquals(15, input.classNames().size());
        assertEquals(59, input.methodOwners().size());
        assertEquals(44, input.calls().size());
        assertEquals(canonical.classNames(), input.classNames());
        // Les CSV nomment les constructeurs avec le nom court, notre TP1 avec <init>.
        var normalizedOwners = new TreeMap<String, String>();
        canonical.methodOwners().forEach((id, owner) -> {
            String constructor = "#" + owner.substring(owner.lastIndexOf('.') + 1) + "(";
            normalizedOwners.put(id.replace(constructor, "#<init>("), owner);
        });
        assertEquals(normalizedOwners, input.methodOwners());
        assertEquals(new HashSet<>(canonical.calls()), new HashSet<>(input.calls()));
        assertEquals(44, new HashSet<>(input.calls()).size());
        assertEquals(46, input.calls().stream().mapToInt(Facts.CallArc::sites).sum());
        assertEquals(7, input.calls().stream().filter(a -> a.callerClass().equals(a.calleeClass())).count());
        assertTrue(input.classNames().contains("library.model.Dvd"));
        assertTrue(input.classNames().contains("library.service.LoanException"));
        assertTrue(input.methodOwners().containsKey("library.util.TextUtils#<init>()"));
        assertThrows(UnsupportedOperationException.class, () -> input.classNames().clear());
        assertThrows(UnsupportedOperationException.class, () -> input.methodOwners().clear());
        assertThrows(UnsupportedOperationException.class, () -> input.calls().clear());
    }

    @Test void chargeLeMicroJeuSansConfondreArcsEtSites() throws Exception {
        var mini = Facts.load(Path.of("../resources/mini"));
        assertEquals(List.of("demo.A", "demo.B", "demo.C"), mini.classNames());
        assertEquals(5, mini.methodOwners().size());
        assertEquals(4, mini.calls().size());
        assertEquals(5, mini.calls().stream().mapToInt(Facts.CallArc::sites).sum());
        for (var arc : mini.calls()) {
            assertEquals(arc.callerClass(), mini.methodOwners().get(arc.caller()));
            assertEquals(arc.calleeClass(), mini.methodOwners().get(arc.callee()));
        }
    }

    @Test void accepteUnModeleVide() {
        var empty = new ProjectFacts(List.of(), List.of(), List.of());
        var input = new Tp1FactsAdapter(empty, new CallGraphService().build(empty));
        assertTrue(input.classNames().isEmpty());
        assertTrue(input.methodOwners().isEmpty());
        assertTrue(input.calls().isEmpty());
    }
}
