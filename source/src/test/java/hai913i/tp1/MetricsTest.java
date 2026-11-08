package hai913i.tp1;

import static org.junit.jupiter.api.Assertions.*;
import hai913i.tp1.metrics.*;
import hai913i.tp1.metrics.MetricsResult.Ranked;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.parse.*;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class MetricsTest {
    @Test void validationCheckpoint() throws Exception {
        var sources = ProjectSources.of(Path.of("resources/validation"));
        var facts = FactExtractor.extract(sources.sourceRoot(), JdtParser.parse(sources, List.of()));
        var service = new MetricsService();
        var m = service.calculate(facts, 5);
        assertEquals(15, m.types());
        assertEquals(493, m.sourceLoc());
        assertEquals(59, m.methods());
        assertEquals(4, m.packages());
        assertEquals(59.0 / 15, m.methodsPerType(), 0.00001);
        assertEquals(246, m.bodyLoc());
        assertEquals(53, m.bodyCount());
        assertEquals(246.0 / 53, m.linesPerMethod(), 0.00001);
        assertEquals(1.4, m.fieldsPerType(), 0.00001);
        assertEquals(List.of("library.model.Item", "library.model.Member", "library.service.LoanService"),
                m.mostMethods().stream().map(Ranked::name).toList());
        assertEquals(List.of("library.model.Item", "library.service.LoanService"), m.intersection());
        assertEquals(m.intersection(), m.mostFields().stream().map(Ranked::name).toList());
        assertEquals(3, m.aboveThreshold().size());
        assertEquals(6, service.calculate(facts, 4).aboveThreshold().size());
        assertEquals(6, m.longestMethods().get("library.model.Member").size());
        assertTrue(m.longestMethods().get("library.model.Loanable").isEmpty());
        assertEquals(7, m.longestMethods().get("library.model.Item").get(0).value());
        assertEquals(14, m.longestMethods().get("library.service.LoanService").get(0).value());
        assertEquals(5, m.maxParameters());
        assertEquals(List.of("library.util.Statistics#report(java.lang.String,int,int,double,int)"), m.maxParameterMethods());
    }
    @Test void roundingTiesZerosAndEmptyModel() {
        List<Ranked> candidates = new ArrayList<>();
        candidates.add(new Ranked("first", 10));
        candidates.add(new Ranked("second", 6));
        candidates.add(new Ranked("third", 6));
        for (int i = 0; i < 12; i++) candidates.add(new Ranked("zero" + i, 0));
        assertEquals(3, MetricsService.topTenPercent(candidates).size());
        assertTrue(MetricsService.topTenPercent(List.of(new Ranked("zero", 0))).isEmpty());
        var empty = new ProjectFacts(List.of(), List.of(), List.of());
        var metrics = new MetricsService().calculate(empty, 0);
        assertEquals(0.0, metrics.linesPerMethod());
        assertEquals(0.0, metrics.methodsPerType());
        assertEquals(0.0, metrics.fieldsPerType());
        assertTrue(metrics.maxParameterMethods().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new MetricsService().calculate(empty, -1));
    }
}
