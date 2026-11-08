package hai913i.tp2;

import hai913i.tp2.clustering.*;
import hai913i.tp2.coupling.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class B2Test {
    private void check(Dendrogram.Node node) {
        if (node.isLeaf()) {
            assertEquals(1, node.members().size());
            return;
        }
        assertNotNull(node.left()); assertNotNull(node.right());
        assertTrue(Collections.disjoint(node.left().members(), node.right().members()));
        var union = new ArrayList<>(node.left().members());
        union.addAll(node.right().members()); union.sort(String::compareTo);
        assertEquals(union, node.members());
        check(node.left()); check(node.right());
    }

    @Test void checkpointEtArbreComplet() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        var result = new ClusteringService().cluster(c);
        var tree = Dendrogram.from(result);
        var root = tree.root().orElseThrow();
        assertEquals(15, root.leafCount());
        assertEquals(14, root.internalCount());
        assertEquals(c.classes(), root.members());
        check(root);
        assertEquals(0, root.similarity());
        assertEquals(0, root.right().similarity());
        assertEquals(28, tree.dot().lines().filter(l -> l.contains(" -> ")).count());
        assertEquals(29, tree.text().lines().count());
        assertEquals(tree.text(), Dendrogram.from(result).text());
        assertEquals(tree.dot(), Dendrogram.from(result).dot());
    }

    @Test void microJeuVideEtSingleton() throws Exception {
        var service = new ClusteringService();
        var mini = Dendrogram.from(service.cluster(new CouplingService().calculate(
                Facts.load(Path.of("../resources/mini")))));
        assertEquals(3, mini.root().orElseThrow().leafCount());
        assertEquals(2, mini.root().orElseThrow().internalCount());
        assertEquals(1.0 / 6, mini.root().orElseThrow().similarity(), 1e-9);
        check(mini.root().orElseThrow());
        var empty = Dendrogram.from(service.cluster(new Coupling(List.of(), 0, 0, Map.of())));
        assertTrue(empty.root().isEmpty());
        var single = Dendrogram.from(service.cluster(new Coupling(List.of("A"), 0, 0, Map.of())));
        assertTrue(single.root().orElseThrow().isLeaf());
        assertEquals(0, single.root().orElseThrow().internalCount());
    }
}
