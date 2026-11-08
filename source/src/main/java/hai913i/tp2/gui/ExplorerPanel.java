package hai913i.tp2.gui;

import hai913i.tp2.clustering.*;
import hai913i.tp2.coupling.Coupling;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Seconde interface du même modèle : aucune formule de clustering ou de coupe ici. */
public final class ExplorerPanel extends JPanel {
    private final Coupling coupling;
    final JTree tree = new JTree();
    final JTextField search = new JTextField(18);
    final JButton find = new JButton("Rechercher");
    final JTextField threshold = new JTextField(10);
    final JButton apply = new JButton("Appliquer CP");
    final JComboBox<ClusteringService.Linkage> linkage = new JComboBox<>(ClusteringService.Linkage.values());
    final JLabel status = new JLabel("Saisir CP pour calculer une coupe (aucune valeur par défaut).");
    final JLabel searchStatus = new JLabel("Recherche par nom de classe, même partiel.");
    final JTextArea details = new JTextArea();
    final DefaultTableModel modules = new DefaultTableModel(new String[]{"Groupe", "Classes", "Moyenne interne"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    final JTable table = new JTable(modules);
    private final Map<Integer, DefaultMutableTreeNode> nodes = new HashMap<>();
    private Dendrogram dendrogram;

    public ExplorerPanel(Coupling coupling, ClusteringService.Linkage selected, Double cp, String relationLabel) {
        super(new BorderLayout(10, 10));
        this.coupling = coupling;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        var header = new JPanel(new BorderLayout());
        var title = new JLabel("Exploration des modules — " + relationLabel);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, BorderLayout.NORTH);
        var controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Liaison :")); controls.add(linkage);
        controls.add(new JLabel("CP :")); controls.add(threshold); controls.add(apply);
        header.add(controls, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        var left = new JPanel(new BorderLayout(6, 6));
        var searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(search); searchBar.add(find);
        var searchHeader = new JPanel(new BorderLayout());
        searchHeader.add(searchBar, BorderLayout.NORTH); searchHeader.add(searchStatus, BorderLayout.SOUTH);
        left.add(searchHeader, BorderLayout.NORTH); left.add(new JScrollPane(tree), BorderLayout.CENTER);
        details.setEditable(false); details.setLineWrap(true); details.setWrapStyleWord(true);
        var right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(details), new JScrollPane(table));
        right.setResizeWeight(0.55);
        var split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.50); add(split, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override public Component getTreeCellRendererComponent(JTree t, Object value, boolean selected,
                    boolean expanded, boolean leaf, int row, boolean focus) {
                super.getTreeCellRendererComponent(t, value, selected, expanded, leaf, row, focus);
                Object item = ((DefaultMutableTreeNode) value).getUserObject();
                if (item instanceof Dendrogram.Node n)
                    setText(n.isLeaf() ? n.members().get(0) : String.format(Locale.ROOT,
                            "G%d — %d classes — S=%.4f", n.id(), n.members().size(), n.similarity()));
                return this;
            }
        });
        tree.addTreeSelectionListener(e -> inspect());
        find.addActionListener(e -> findClasses()); search.addActionListener(e -> findClasses());
        apply.addActionListener(e -> updateCut()); threshold.addActionListener(e -> updateCut());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) select((Integer) modules.getValueAt(row, 0));
        });
        linkage.setSelectedItem(selected);
        linkage.addActionListener(e -> { rebuild(); if (!threshold.getText().isBlank()) updateCut(); });
        rebuild();
        if (cp != null) { threshold.setText(Double.toString(cp)); updateCut(); }
        setPreferredSize(new Dimension(1180, 720));
    }

    private void rebuild() {
        dendrogram = Dendrogram.from(new ClusteringService((ClusteringService.Linkage) linkage.getSelectedItem()).cluster(coupling));
        nodes.clear(); modules.setRowCount(0);
        DefaultMutableTreeNode root = dendrogram.root().map(this::branch).orElseGet(() -> new DefaultMutableTreeNode("Jeu vide"));
        tree.setModel(new DefaultTreeModel(root));
        tree.expandRow(0); tree.setSelectionRow(0);
        status.setText("Arbre recalculé — saisir CP puis appliquer pour sélectionner des modules.");
    }

    private DefaultMutableTreeNode branch(Dendrogram.Node n) {
        var item = new DefaultMutableTreeNode(n); nodes.put(n.id(), item);
        if (!n.isLeaf()) { item.add(branch(n.left())); item.add(branch(n.right())); }
        return item;
    }

    private void inspect() {
        var selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selected == null || !(selected.getUserObject() instanceof Dendrogram.Node n)) return;
        String mean = n.isLeaf() ? "Non applicable (singleton)"
                : Double.toString(new ModuleCut().internalMean(coupling, n.members()));
        details.setText("Groupe G" + n.id() + "\nNombre de classes : " + n.members().size()
                + "\nSimilarité de fusion : " + (n.isLeaf() ? "Non applicable" : n.similarity())
                + "\nCouplage interne moyen : " + mean + "\n\nClasses :\n" + String.join("\n", n.members()));
        details.setCaretPosition(0);
    }

    private void select(int id) {
        var node = nodes.get(id);
        if (node != null) { var path = new TreePath(node.getPath()); tree.setSelectionPath(path); tree.scrollPathToVisible(path); }
    }

    private void findClasses() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) { searchStatus.setText("Saisir un nom de classe."); return; }
        List<TreePath> matches = nodes.values().stream()
                .filter(n -> ((Dendrogram.Node) n.getUserObject()).isLeaf())
                .filter(n -> ((Dendrogram.Node) n.getUserObject()).members().get(0).toLowerCase(Locale.ROOT).contains(query))
                .sorted(Comparator.comparing(n -> ((Dendrogram.Node) n.getUserObject()).members().get(0)))
                .map(n -> new TreePath(n.getPath())).toList();
        tree.clearSelection();
        for (var path : matches) tree.makeVisible(path);
        tree.setSelectionPaths(matches.toArray(TreePath[]::new));
        if (!matches.isEmpty()) tree.scrollPathToVisible(matches.get(0));
        searchStatus.setText(matches.size() + " classe(s) trouvée(s).");
        if (matches.isEmpty()) details.setText("Aucune classe ne correspond à : " + search.getText());
    }

    private void updateCut() {
        try {
            double cp = Double.parseDouble(threshold.getText().strip());
            var result = new ModuleCut().cut(dendrogram, coupling, cp);
            modules.setRowCount(0);
            for (var module : result.modules()) modules.addRow(new Object[]{module.groupId(), module.classes().size(),
                    module.mean().isPresent() ? String.format(Locale.ROOT, "%.6f", module.mean().getAsDouble()) : "NA (singleton)"});
            status.setForeground(result.valid() ? new Color(20, 100, 55) : new Color(170, 30, 30));
            status.setText(String.format(Locale.ROOT, "CP=%s — %d modules / maximum %d — %s. Cliquer une ligne pour inspecter le module.",
                    Double.toString(cp), result.modules().size(), result.maxModules(), result.valid() ? "Coupe acceptée" : "Coupe rejetée"));
        } catch (IllegalArgumentException e) {
            modules.setRowCount(0);
            status.setForeground(new Color(170, 30, 30));
            status.setText("CP invalide : saisir un nombre fini positif ou nul avec un point décimal.");
        }
    }

    public static void open(Coupling coupling, ClusteringService.Linkage linkage, Double cp, String relations) throws Exception {
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Environnement graphique indisponible");
        SwingUtilities.invokeAndWait(() -> {
            var window = new JFrame("HAI913I — Exploration des modules");
            window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            window.setContentPane(new ExplorerPanel(coupling, linkage, cp, relations));
            window.pack(); window.setLocationRelativeTo(null); window.setVisible(true);
        });
    }
}
