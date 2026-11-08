package hai913i.tp2.gui;

import hai913i.tp2.Facts;
import hai913i.tp2.coupling.CouplingService;
import hai913i.tp2.clustering.ClusteringService;
import java.nio.file.Path;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class C3Test {
    @Test void rechercheCoupeEtInspection() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        SwingUtilities.invokeAndWait(() -> {
            var panel = new ExplorerPanel(c, ClusteringService.Linkage.AVERAGE, 0.025, "CALLS");
            assertEquals(7, panel.modules.getRowCount());
            panel.threshold.setText("0.045"); panel.apply.doClick();
            assertEquals(10, panel.modules.getRowCount());
            assertTrue(panel.status.getText().contains("rejetée"));
            panel.search.setText("Dvd"); panel.find.doClick();
            assertEquals(1, panel.tree.getSelectionCount());
            assertTrue(panel.details.getText().contains("library.model.Dvd"));
            panel.search.setText("inexistante"); panel.find.doClick();
            assertEquals(0, panel.tree.getSelectionCount());
            panel.threshold.setText("NaN"); panel.apply.doClick();
            assertEquals(0, panel.modules.getRowCount());
            assertTrue(panel.status.getText().contains("invalide"));
            panel.threshold.setText("0.025"); panel.apply.doClick();
            panel.table.setRowSelectionInterval(0, 0);
            assertTrue(panel.details.getText().contains("LibraryApp"));
        });
    }

    @Test void changementDeLiaisonEtSeuilAbsent() throws Exception {
        var c = new CouplingService().calculate(Facts.load(Path.of("../resources")));
        SwingUtilities.invokeAndWait(() -> {
            var panel = new ExplorerPanel(c, ClusteringService.Linkage.AVERAGE, null, "CALLS");
            assertEquals(0, panel.modules.getRowCount());
            panel.threshold.setText("0.025"); panel.apply.doClick();
            panel.linkage.setSelectedItem(ClusteringService.Linkage.SINGLE);
            assertEquals(7, panel.modules.getRowCount());
            assertEquals(8, panel.modules.getValueAt(0, 1));
        });
    }
}
