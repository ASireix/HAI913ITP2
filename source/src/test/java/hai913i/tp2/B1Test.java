package hai913i.tp2;

import hai913i.tp2.clustering.ClusteringService;
import hai913i.tp2.clustering.ClusteringService.Group;
import hai913i.tp2.coupling.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class B1Test {
    private final ClusteringService service = new ClusteringService();

    @Test void checkpointEtPartitions() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        var result = service.cluster(c);
        assertEquals(14, result.merges().size());
        var first = result.merges().get(0);
        assertEquals(List.of("library.model.Item"), first.left().members());
        assertEquals(List.of("library.service.LoanService"), first.right().members());
        assertEquals(4.0 / 37, first.similarity(), 1e-9);
        var second = result.merges().get(1);
        assertEquals(List.of("library.app.LibraryApp"), second.left().members());
        assertEquals(List.of("library.service.Catalog"), second.right().members());
        assertEquals(3.0 / 37, second.similarity(), 1e-9);
        var third = result.merges().get(2);
        assertEquals(first.result(), third.left());
        assertEquals(second.result(), third.right());
        assertEquals(9.0 / 148, third.similarity(), 1e-9);
        var active = new HashMap<Integer, Group>();
        result.leaves().forEach(g -> active.put(g.id(), g));
        double previous = Double.POSITIVE_INFINITY;
        int next = 15;
        for (var merge : result.merges()) {
            assertEquals(merge.left(), active.remove(merge.left().id()));
            assertEquals(merge.right(), active.remove(merge.right().id()));
            assertEquals(next++, merge.result().id());
            assertTrue(merge.similarity() <= previous + 1e-9);
            previous = merge.similarity();
            var union = new TreeSet<>(merge.left().members());
            union.addAll(merge.right().members());
            assertEquals(new ArrayList<>(union), merge.result().members());
            active.put(merge.result().id(), merge.result());
            var members = active.values().stream().flatMap(g -> g.members().stream()).sorted().toList();
            assertEquals(c.classes(), members); // partition complète, sans doublon
        }
        assertEquals(0.0, result.merges().get(12).similarity());
        assertEquals(0.0, result.merges().get(13).similarity());
        assertEquals(result, service.cluster(c));
        assertArrayEquals(result.trace().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                service.cluster(c).trace().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test void microJeuMoyenneIncluantLesZeros() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources/mini")));
        var result = service.cluster(c);
        assertEquals(2, result.merges().size());
        assertEquals(2.0 / 3, result.merges().get(0).similarity(), 1e-9);
        assertEquals(1.0 / 6, result.merges().get(1).similarity(), 1e-9);
    }

    @Test void egalitesToleranceEtCasSansRelation() {
        var c = new Coupling(List.of("A", "B", "C", "D"), 0, 4, Map.of(
                new Coupling.Pair("A", "C"), 1,
                new Coupling.Pair("B", "D"), 1,
                new Coupling.Pair("C", "D"), 1,
                new Coupling.Pair("A", "B"), 1));
        var first = service.cluster(c).merges().get(0);
        assertEquals(0, first.left().id());
        assertEquals(1, first.right().id());
        // Deux valeurs distantes de moins de 1e-9 sont départagées par les identifiants.
        var near = new Coupling(List.of("A", "B", "C"), 0, 2_000_000_000, Map.of(
                new Coupling.Pair("A", "B"), 999_999_999,
                new Coupling.Pair("A", "C"), 1_000_000_000));
        assertEquals(1, service.cluster(near).merges().get(0).right().id());
        var zero = service.cluster(new Coupling(List.of("A", "B", "C"), 0, 0, Map.of()));
        assertEquals(0, zero.merges().get(0).left().id());
        assertEquals(1, zero.merges().get(0).right().id());
        assertEquals(2, zero.merges().size());
        assertTrue(service.cluster(new Coupling(List.of(), 0, 0, Map.of())).merges().isEmpty());
        assertTrue(service.cluster(new Coupling(List.of("A"), 0, 0, Map.of())).merges().isEmpty());
    }
}
