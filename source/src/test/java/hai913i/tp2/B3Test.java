package hai913i.tp2;

import hai913i.tp2.clustering.*;
import hai913i.tp2.coupling.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class B3Test {
    private final ModuleCut service = new ModuleCut();
    private Dendrogram tree(Coupling c) { return Dendrogram.from(new ClusteringService().cluster(c)); }

    @Test void checkpointsEtBranches() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        var tree = tree(c);
        double[] thresholds = {0.015, 0.025, 0.045};
        int[] sizes = {4, 7, 10};
        var branches = new HashMap<Integer, List<String>>();
        collect(tree.root().orElseThrow(), branches);
        for (int i = 0; i < thresholds.length; i++) {
            var result = service.cut(tree, c, thresholds[i]);
            assertEquals(sizes[i], result.modules().size());
            assertEquals(7, result.maxModules());
            assertEquals(i < 2, result.valid());
            assertEquals(c.classes(), result.modules().stream().flatMap(m -> m.classes().stream()).sorted().toList());
            for (var module : result.modules()) {
                assertEquals(branches.get(module.groupId()), module.classes());
                if (module.classes().size() > 1) assertTrue(module.mean().orElseThrow() > thresholds[i]);
                else assertTrue(module.mean().isEmpty());
            }
        }
        assertEquals(11, service.cut(tree, c, 0.015).modules().stream().mapToInt(m -> m.classes().size()).max().orElseThrow());
    }

    private void collect(Dendrogram.Node n, Map<Integer, List<String>> branches) {
        branches.put(n.id(), n.members());
        if (!n.isLeaf()) { collect(n.left(), branches); collect(n.right(), branches); }
    }

    @Test void microJeuEgaliteStricteEtMoyenneDistincteDeFusion() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources/mini")));
        var tree = tree(c);
        assertEquals(1.0 / 3, service.internalMean(c, c.classes()), 1e-9);
        assertEquals(1.0 / 6, tree.root().orElseThrow().similarity(), 1e-9);
        assertEquals(1, service.cut(tree, c, 0.20).modules().size());
        assertTrue(service.cut(tree, c, 0.20).valid());
        assertEquals(2, service.cut(tree, c, 0.50).modules().size());
        assertFalse(service.cut(tree, c, 0.50).valid());
        // À égalité exacte, la racine n'est pas retenue.
        assertEquals(2, service.cut(tree, c, service.internalMean(c, c.classes())).modules().size());
        assertEquals(3, service.cut(tree, c, 2.0 / 3).modules().size());
    }

    @Test void casLimitesEtSeuilsInvalides() {
        var c = new Coupling(List.of("A", "B"), 0, 0, Map.of());
        var tree = tree(c);
        assertEquals(2, service.cut(tree, c, 0).modules().size());
        assertFalse(service.cut(tree, c, 0).valid());
        for (double cp : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class, () -> service.cut(tree, c, cp));
        var single = new Coupling(List.of("A"), 0, 0, Map.of());
        assertFalse(service.cut(tree(single), single, 0).valid()); // 1 module > floor(1/2)
        var empty = new Coupling(List.of(), 0, 0, Map.of());
        assertTrue(service.cut(tree(empty), empty, 0).valid());
        assertEquals(1, Main.run(new String[]{"--csv", "../resources", "--cp", "NaN"}));
        assertEquals(1, Main.run(new String[]{"--csv", "../resources", "--cp"}));
    }
}
