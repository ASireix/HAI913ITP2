package hai913i.tp2.clustering;

import java.util.*;

/** Arbre binaire immuable reconstruit à partir de la trace de B1. */
public record Dendrogram(Optional<Node> root) {
    public record Node(int id, List<String> members, double similarity, Node left, Node right) {
        public Node { members = List.copyOf(members); }
        public boolean isLeaf() { return left == null && right == null; }
        public int leafCount() { return isLeaf() ? 1 : left.leafCount() + right.leafCount(); }
        public int internalCount() { return isLeaf() ? 0 : 1 + left.internalCount() + right.internalCount(); }
    }

    public static Dendrogram from(ClusteringService.Result result) {
        Map<Integer, Node> active = new TreeMap<>();
        for (var group : result.leaves()) {
            if (group.members().size() != 1 || active.putIfAbsent(group.id(),
                    new Node(group.id(), group.members(), 0, null, null)) != null)
                throw new IllegalArgumentException("Feuille invalide");
        }
        for (var merge : result.merges()) {
            Node left = active.remove(merge.left().id());
            Node right = active.remove(merge.right().id());
            if (left == null || right == null || !left.members().equals(merge.left().members())
                    || !right.members().equals(merge.right().members()))
                throw new IllegalArgumentException("Fusion de groupes inactifs ou incoherents");
            List<String> union = new ArrayList<>(left.members());
            union.addAll(right.members());
            union.sort(String::compareTo);
            if (new HashSet<>(union).size() != union.size() || !union.equals(merge.result().members()))
                throw new IllegalArgumentException("Membres de fusion incoherents");
            if (active.putIfAbsent(merge.result().id(), new Node(merge.result().id(), union,
                    merge.similarity(), left, right)) != null)
                throw new IllegalArgumentException("Identifiant de fusion duplique");
        }
        if (active.size() > 1) throw new IllegalArgumentException("Clustering incomplet");
        return new Dendrogram(active.values().stream().findFirst());
    }

    public String text() {
        var text = new StringBuilder();
        root.ifPresentOrElse(n -> appendText(n, 0, text), () -> text.append("Arbre vide\n"));
        return text.toString();
    }

    private static void appendText(Node n, int depth, StringBuilder text) {
        text.append("  ".repeat(depth)).append(n.id()).append(" : ");
        if (n.isLeaf()) text.append(n.members().get(0));
        else text.append("S=").append(Double.toString(n.similarity())).append(" ").append(n.members());
        text.append('\n');
        if (!n.isLeaf()) { appendText(n.left(), depth + 1, text); appendText(n.right(), depth + 1, text); }
    }

    /** Disposition topologique : les valeurs de similarité sont portées par les étiquettes. */
    public String dot() {
        return dot(ClusteringService.Linkage.AVERAGE);
    }

    public String dot(ClusteringService.Linkage linkage) {
        String title = linkage == ClusteringService.Linkage.SINGLE ? "lien simple" : "lien moyen";
        var text = new StringBuilder("digraph dendrogram {\n  rankdir=LR;\n  graph [label=\"Dendrogramme - " + title + " (disposition topologique)\", labelloc=t];\n  node [fontname=\"Arial\"];\n");
        root.ifPresent(n -> appendDot(n, text));
        return text.append("}\n").toString();
    }

    private static void appendDot(Node n, StringBuilder text) {
        String label = n.isLeaf() ? n.members().get(0)
                : "G" + n.id() + "\\nS=" + String.format(Locale.ROOT, "%.4f", n.similarity());
        text.append("  n").append(n.id()).append(" [shape=")
                .append(n.isLeaf() ? "box" : "ellipse").append(", label=\"")
                .append(label).append("\"];\n");
        if (!n.isLeaf()) {
            text.append("  n").append(n.id()).append(" -> n").append(n.left().id()).append(";\n");
            text.append("  n").append(n.id()).append(" -> n").append(n.right().id()).append(";\n");
            appendDot(n.left(), text); appendDot(n.right(), text);
        }
    }
}
