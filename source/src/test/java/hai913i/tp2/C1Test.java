package hai913i.tp2;

import hai913i.tp2.clustering.*;
import hai913i.tp2.coupling.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class C1Test {
    @Test void premiereDivergenceEtInvariants() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        var average = new ClusteringService().cluster(c);
        var service = new ClusteringService(ClusteringService.Linkage.SINGLE);
        var single = service.cluster(c);
        assertEquals(average.merges().get(0), single.merges().get(0));
        assertNotEquals(average.merges().get(1).result().members(), single.merges().get(1).result().members());
        assertEquals(List.of("library.model.Item", "library.service.LoanService", "library.service.LoanService.Loan"),
                single.merges().get(1).result().members());
        assertEquals(4.0 / 37, single.merges().get(1).similarity(), 1e-9);
        assertEquals(14, single.merges().size());
        var root = Dendrogram.from(single).root().orElseThrow();
        assertEquals(c.classes(), root.members());
        assertEquals(15, root.leafCount());
        assertEquals(14, root.internalCount());
        double previous = Double.POSITIVE_INFINITY;
        for (var merge : single.merges()) {
            assertTrue(merge.similarity() <= previous + 1e-9);
            previous = merge.similarity();
        }
        assertEquals(0, single.merges().get(12).similarity());
        assertEquals(0, single.merges().get(13).similarity());
        assertEquals(single.trace(), service.cluster(c).trace());
    }

    @Test void microJeuMaximumEtMoyenne() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources/mini")));
        var average = new ClusteringService().cluster(c);
        var single = new ClusteringService(ClusteringService.Linkage.SINGLE).cluster(c);
        assertEquals(average.merges().get(0), single.merges().get(0));
        assertEquals(1.0 / 3, single.merges().get(1).similarity(), 1e-9);
        assertEquals(1.0 / 6, average.merges().get(1).similarity(), 1e-9);
        assertEquals(average.merges().get(1).result().members(), single.merges().get(1).result().members());
    }

    @Test void egalitesEtEntreesLimites() {
        var service = new ClusteringService(ClusteringService.Linkage.SINGLE);
        var zero = service.cluster(new Coupling(List.of("A", "B", "C"), 0, 0, Map.of()));
        assertEquals(0, zero.merges().get(0).left().id());
        assertEquals(1, zero.merges().get(0).right().id());
        assertTrue(service.cluster(new Coupling(List.of(), 0, 0, Map.of())).merges().isEmpty());
        assertTrue(service.cluster(new Coupling(List.of("A"), 0, 0, Map.of())).merges().isEmpty());
        assertEquals(1, Main.run(new String[]{"--csv", "../resources", "--linkage", "invalid"}));
    }
}
