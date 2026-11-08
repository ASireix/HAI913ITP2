package hai913i.tp2.clustering;

import hai913i.tp2.coupling.Coupling;
import java.util.*;

/** B3 : coupe descendante puis contrôle global du nombre de modules. */
public final class ModuleCut {
    public record Module(int groupId, List<String> classes, OptionalDouble mean) {
        public Module { classes = List.copyOf(classes); }
    }
    public record Result(double threshold, int maxModules, List<Module> modules) {
        public Result { modules = List.copyOf(modules); }
        public boolean valid() { return modules.size() <= maxModules; }
        public String csv() {
            var text = new StringBuilder("cp;valid;maxModules;group;size;mean;classes\n");
            for (var module : modules) {
                text.append(threshold).append(';').append(valid()).append(';').append(maxModules)
                        .append(';').append(module.groupId()).append(';').append(module.classes().size()).append(';')
                        .append(module.mean().isPresent() ? Double.toString(module.mean().getAsDouble()) : "NA")
                        .append(';').append(String.join(",", module.classes())).append('\n');
            }
            return text.toString();
        }
    }

    public double internalMean(Coupling coupling, List<String> classes) {
        if (classes.size() < 2) throw new IllegalArgumentException("Moyenne non definie pour un singleton");
        double sum = 0;
        for (int i = 0; i < classes.size(); i++)
            for (int j = i + 1; j < classes.size(); j++)
                sum += coupling.value(classes.get(i), classes.get(j));
        return sum / ((double) classes.size() * (classes.size() - 1) / 2);
    }

    public Result cut(Dendrogram tree, Coupling coupling, double threshold) {
        if (!Double.isFinite(threshold) || threshold < 0)
            throw new IllegalArgumentException("CP doit etre fini et positif ou nul");
        if (!tree.root().map(Dendrogram.Node::members).orElse(List.of()).equals(coupling.classes()))
            throw new IllegalArgumentException("Arbre et couplage portent sur des classes differentes");
        List<Module> modules = new ArrayList<>();
        tree.root().ifPresent(n -> visit(n, coupling, threshold, modules));
        modules.sort(Comparator.comparing(m -> m.classes().get(0)));
        return new Result(threshold, coupling.classes().size() / 2, modules);
    }

    private void visit(Dendrogram.Node node, Coupling coupling, double threshold, List<Module> modules) {
        if (node.isLeaf()) {
            modules.add(new Module(node.id(), node.members(), OptionalDouble.empty()));
            return;
        }
        double mean = internalMean(coupling, node.members());
        // Comparaison strictement supérieure : ne pas réutiliser la similarité de fusion.
        if (mean > threshold) modules.add(new Module(node.id(), node.members(), OptionalDouble.of(mean)));
        else {
            visit(node.left(), coupling, threshold, modules);
            visit(node.right(), coupling, threshold, modules);
        }
    }
}
