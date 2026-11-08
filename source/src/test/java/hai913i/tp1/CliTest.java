package hai913i.tp1;

import static org.junit.jupiter.api.Assertions.*;
import hai913i.tp1.cli.CliOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class CliTest {
    @Test void helpWorksWithProjectWithoutStartingAnalysis() {
        var oldOut = System.out;
        var oldErr = System.err;
        var output = new ByteArrayOutputStream();
        var errors = new ByteArrayOutputStream();
        try (var out = new PrintStream(output); var err = new PrintStream(errors)) {
            System.setOut(out);
            System.setErr(err);
            for (String[] args : new String[][] {
                    {"--help"}, {"resources/validation", "--help"},
                    {"/path/that/does/not/exist", "--action", "metrics", "--help"} }) {
                output.reset();
                errors.reset();
                assertEquals(0, Main.run(args));
                assertTrue(output.toString().contains("Usage :"));
                assertFalse(output.toString().contains("PARTIE A0"));
                assertEquals("", errors.toString());
            }
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }
    @Test void optionsRejectBadInputs() {
        for (String[] args : new String[][] { {}, {"p", "--threshold"}, {"p", "--threshold", "abc"},
                {"p", "--threshold", "-1"}, {"p", "--action", "unknown"}, {"p", "--bad"},
                {"p", "--action", "metrics", "--callers", "x"} })
            assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(args));
        assertTrue(CliOptions.parse(new String[]{"--help"}).help());
        assertEquals(5, CliOptions.parse(new String[]{"p"}).threshold());
    }
    @Test void errorsAndPartialAnalysis() {
        var oldOut = System.out;
        var oldErr = System.err;
        var bytes = new ByteArrayOutputStream();
        try (var stream = new PrintStream(bytes)) {
            System.setOut(stream); System.setErr(stream);
            assertEquals(3, Main.run(new String[]{"resources/robustness/no-java-files"}));
            assertEquals(3, Main.run(new String[]{"/tmp/tp1-path-that-does-not-exist-654322"}));
            assertEquals(2, Main.run(new String[]{"p", "--threshold", "abc"}));
            bytes.reset();
            assertEquals(0, Main.run(new String[]{"resources/robustness/syntax-error", "--action", "structure"}));
            assertTrue(bytes.toString().contains("Broken.java"));
            assertTrue(bytes.toString().contains("demo.Helper"));
            assertFalse(bytes.toString().contains("PARTIE B2"));
            assertFalse(bytes.toString().contains("Exception in thread"));
            assertEquals(0, Main.run(new String[]{"resources/robustness/missing-dep", "--action", "calls"}));
        } finally { System.setOut(oldOut); System.setErr(oldErr); }
    }
}
