package hai913i.tp1;

import static org.junit.jupiter.api.Assertions.*;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.parse.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectFactsTest {
    private ProjectFacts extract(String path) throws Exception {
        var sources = ProjectSources.of(Path.of(path));
        return FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of()));
    }
    @Test void identitiesLinksAndDeterminism() throws Exception {
        var facts = extract("resources/validation");
        assertEquals(facts, extract("resources/validation"));
        assertEquals(14, facts.files().size());
        assertEquals(59, facts.methodsById().size());
        assertEquals(94, facts.calls().size());
        assertEquals(46, facts.calls().stream().filter(c -> c.internal()).count());
        var index = facts.methodsById();
        var addMethods = index.keySet().stream().filter(id -> id.startsWith("library.service.Catalog#add(")).toList();
        assertEquals(2, addMethods.size());
        assertTrue(index.containsKey("library.service.Catalog#add(library.model.Item)"));
        for (var call : facts.calls()) {
            assertTrue(index.containsKey(call.callerId()));
            assertTrue(call.targetId().isPresent());
            if (call.internal()) assertTrue(index.containsKey(call.targetId().orElseThrow()), call.targetId().toString());
        }
        assertTrue(facts.methods().stream().anyMatch(m -> m.bodyLoc() == 0));
        assertTrue(facts.sourceLoc() > 0);
        assertThrows(UnsupportedOperationException.class, () -> facts.types().clear());
    }
    @Test void unresolvedTargetHasNoInventedId() throws Exception {
        var facts = extract("resources/robustness/missing-dep");
        var call = facts.calls().stream().filter(c -> c.name().equals("capitalize")).findFirst().orElseThrow();
        assertFalse(call.resolved());
        assertTrue(call.targetId().isEmpty());
        assertTrue(facts.methodsById().containsKey(call.callerId()));
    }
}
