package hai913i.tp1;

import static org.junit.jupiter.api.Assertions.*;
import hai913i.tp1.model.AnalysisFacts.*;
import hai913i.tp1.parse.*;
import hai913i.tp1.report.ConsoleReporter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PartAReportTest {
    private List<TypeFact> extract(String project) throws Exception {
        List<TypeFact> types = new ArrayList<>();
        for (var file : JdtParser.parse(ProjectSources.of(Path.of(project)), List.of())) {
            var visitor = new JdtVisitorClasses(file.path().getFileName().toString());
            file.unit().accept(visitor);
            types.addAll(visitor.types());
        }
        return types;
    }

    @Test void validationTotalsAndOwnership() throws Exception {
        var types = extract("resources/validation");
        assertEquals(15, types.size());
        var service = types.stream().filter(t -> t.name().equals("library.service.LoanService")).findFirst().orElseThrow();
        assertEquals(6, service.methods().size());
        assertEquals(5, service.fields().size());
        var bytes = new ByteArrayOutputStream();
        new ConsoleReporter(new PrintStream(bytes)).displayTotals(types);
        String text = bytes.toString();
        assertTrue(text.contains("Attributs : 21"));
        assertTrue(text.contains("Methodes : 59, constructeurs : 9"));
        assertTrue(text.contains("Appels : 94, ordinaires : 93, super : 1"));
        assertTrue(text.contains("Internes : 46, externes : 48, non resolus : 0"));
        var calls = types.stream().flatMap(t -> t.methods().stream()).flatMap(m -> m.calls().stream()).toList();
        assertTrue(calls.stream().anyMatch(c -> c.superCall() && c.receiver().equals("library.model.Item")));
        assertTrue(calls.stream().anyMatch(c -> c.name().equals("getTitle") && c.receiver().equals("library.model.Book")
                && c.target().equals("library.model.Item#getTitle()")));
    }

    @Test void unresolvedCallsRemainInTheReport() throws Exception {
        var types = extract("resources/robustness/missing-dep");
        var calls = types.stream().flatMap(t -> t.methods().stream()).flatMap(m -> m.calls().stream()).toList();
        assertEquals(2, calls.size());
        assertTrue(calls.stream().anyMatch(c -> c.name().equals("capitalize") && !c.resolved()));
        assertTrue(calls.stream().anyMatch(c -> c.name().equals("decorate") && c.resolved()));
    }
}
