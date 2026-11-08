package hai913i.tp2.graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** CSV UTF-8 séparé par ;, comme les faits fournis. Les noms Java ne contiennent pas ;. */
public final class GraphIO {
    private GraphIO() {}

    public static void write(WeightedGraph graph, Path directory) throws IOException {
        Files.createDirectories(directory);
        List<String> nodes = new ArrayList<>(List.of("class"));
        nodes.addAll(graph.nodes());
        List<String> edges = new ArrayList<>(List.of("first;second;weight"));
        for (var e : graph.edges())
            edges.add(e.first() + ";" + e.second() + ";" + Double.toString(e.weight()));
        Files.write(directory.resolve("coupling-nodes.csv"), nodes, StandardCharsets.UTF_8);
        Files.write(directory.resolve("coupling-edges.csv"), edges, StandardCharsets.UTF_8);
        Files.writeString(directory.resolve("coupling.dot"), dot(graph), StandardCharsets.UTF_8);
    }

    public static WeightedGraph read(Path directory) throws IOException {
        var nodes = Files.readAllLines(directory.resolve("coupling-nodes.csv"), StandardCharsets.UTF_8);
        var lines = Files.readAllLines(directory.resolve("coupling-edges.csv"), StandardCharsets.UTF_8);
        if (nodes.isEmpty() || !nodes.get(0).equals("class")
                || lines.isEmpty() || !lines.get(0).equals("first;second;weight"))
            throw new IOException("En-tete CSV du graphe invalide");
        List<WeightedGraph.Edge> edges = new ArrayList<>();
        try {
            for (int i = 1; i < lines.size(); i++) {
                String[] fields = lines.get(i).split(";", -1);
                if (fields.length != 3) throw new IllegalArgumentException("3 colonnes attendues ligne " + (i + 1));
                edges.add(new WeightedGraph.Edge(fields[0], fields[1], Double.parseDouble(fields[2])));
            }
            return new WeightedGraph(nodes.subList(1, nodes.size()), edges);
        } catch (IllegalArgumentException e) {
            throw new IOException("Graphe CSV invalide : " + e.getMessage(), e);
        }
    }

    public static String dot(WeightedGraph graph) {
        var text = new StringBuilder("graph coupling {\n");
        for (String node : graph.nodes()) text.append("  ").append(quote(node)).append(";\n");
        for (var edge : graph.edges()) {
            text.append("  ").append(quote(edge.first())).append(" -- ").append(quote(edge.second()))
                    .append(" [label=\"").append(String.format(Locale.ROOT, "%.4f", edge.weight()))
                    .append("\", weight=\"").append(Double.toString(edge.weight())).append("\"];\n");
        }
        return text.append("}\n").toString();
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
