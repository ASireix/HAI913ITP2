package hai913i.tp2.clustering;

import hai913i.tp2.coupling.Coupling;
import java.util.*;

/** B1 : regroupement ascendant, lien moyen calculé depuis les couplages de classes. */
public final class ClusteringService {
    public static final double EPSILON = 1e-9;
    public enum Linkage { AVERAGE, SINGLE }
    private final Linkage linkage;

    public ClusteringService() { this(Linkage.AVERAGE); }
    public ClusteringService(Linkage linkage) { this.linkage = Objects.requireNonNull(linkage); }

    public record Group(int id, List<String> members) {
        public Group { members = members.stream().sorted().toList(); }
    }
    public record Merge(Group left, Group right, Group result, double similarity) {}
    public record Result(List<Group> leaves, List<Merge> merges) {
        public Result { leaves = List.copyOf(leaves); merges = List.copyOf(merges); }

        /** Trace stable : les identifiants permettent de reconstruire chaque fusion. */
        public String trace() {
            var text = new StringBuilder("step;left;right;result;similarity;members\n");
            for (int i = 0; i < merges.size(); i++) {
                var m = merges.get(i);
                text.append(i + 1).append(';').append(m.left().id()).append(';')
                        .append(m.right().id()).append(';').append(m.result().id()).append(';')
                        .append(Double.toString(m.similarity())).append(';')
                        .append(String.join(",", m.result().members())).append('\n');
            }
            return text.toString();
        }
    }

    public double similarity(Coupling coupling, Group a, Group b) {
        double sum = 0;
        double maximum = 0;
        for (String first : a.members())
            for (String second : b.members()) {
                double value = coupling.value(first, second);
                sum += value;
                maximum = Math.max(maximum, value);
            }
        if (linkage == Linkage.SINGLE) return maximum;
        // Tous les couples comptent au dénominateur, même ceux de couplage nul.
        return sum / ((double) a.members().size() * b.members().size());
    }

    public Result cluster(Coupling coupling) {
        List<Group> leaves = new ArrayList<>();
        for (String name : coupling.classes()) leaves.add(new Group(leaves.size(), List.of(name)));
        List<Group> active = new ArrayList<>(leaves);
        List<Merge> merges = new ArrayList<>();
        int nextId = leaves.size();
        while (active.size() > 1) {
            // Les groupes actifs sont triés par identifiant stable.
            active.sort(Comparator.comparingInt(Group::id));
            double maximum = -1;
            for (int i = 0; i < active.size(); i++)
                for (int j = i + 1; j < active.size(); j++)
                    maximum = Math.max(maximum, similarity(coupling, active.get(i), active.get(j)));
            Group left = null;
            Group right = null;
            double selected = 0;
            // Premier couple lexicographique à moins de EPSILON du maximum réel.
            search:
            for (int i = 0; i < active.size(); i++) {
                for (int j = i + 1; j < active.size(); j++) {
                    double value = similarity(coupling, active.get(i), active.get(j));
                    if (maximum - value <= EPSILON) {
                        left = active.get(i); right = active.get(j); selected = value;
                        break search;
                    }
                }
            }
            List<String> members = new ArrayList<>(left.members());
            members.addAll(right.members());
            var merged = new Group(nextId++, members);
            merges.add(new Merge(left, right, merged, selected));
            active.remove(left);
            active.remove(right);
            active.add(merged);
        }
        return new Result(leaves, merges);
    }
}
