package hai913i.tp1.metrics;

import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.AnalysisFacts.MethodFact;
import hai913i.tp1.metrics.MetricsResult.Ranked;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class MetricsService {
    private static final Comparator<Ranked> ORDER = Comparator.comparingInt(Ranked::value)
            .reversed().thenComparing(Ranked::name);

    public MetricsResult calculate(ProjectFacts project, int threshold) {
        if (threshold < 0) throw new IllegalArgumentException("X doit être positif ou nul");
        var types = project.types();
        var methods = project.methods();
        var byMethods = types.stream().map(t -> new Ranked(t.name(), t.methods().size())).toList();
        var byFields = types.stream().map(t -> new Ranked(t.name(), t.fields().size())).toList();
        var topMethods = topTenPercent(byMethods);
        var topFields = topTenPercent(byFields);
        var fieldNames = topFields.stream().map(Ranked::name).collect(Collectors.toSet());
        var intersection = topMethods.stream().map(Ranked::name).filter(fieldNames::contains).sorted().toList();
        Map<String, List<Ranked>> longest = new TreeMap<>();
        for (var type : types) {
            longest.put(type.name(), topTenPercent(type.methods().stream()
                    .map(m -> new Ranked(m.id(), m.bodyLoc())).toList()));
        }
        // Un corps présent couvre au moins une ligne, même lorsqu'il est vide.
        int bodyCount = (int) methods.stream().filter(m -> m.bodyLoc() > 0).count();
        int bodyLoc = methods.stream().mapToInt(MethodFact::bodyLoc).sum();
        int fields = types.stream().mapToInt(t -> t.fields().size()).sum();
        int max = methods.stream().mapToInt(MethodFact::parameterCount).max().orElse(0);
        return new MetricsResult(types.size(), project.sourceLoc(), methods.size(),
                types.stream().map(t -> t.packageName()).distinct().count(),
                mean(methods.size(), types.size()), mean(bodyLoc, bodyCount), mean(fields, types.size()),
                bodyCount, bodyLoc, topMethods, topFields, intersection, threshold,
                byMethods.stream().filter(r -> r.value() > threshold).sorted(ORDER).toList(), longest, max,
                methods.stream().filter(m -> m.parameterCount() == max).map(MethodFact::id).sorted().toList());
    }

    /** k est calculé sur tous les candidats, puis les zéros sont exclus ; les ex aequo restent. */
    public static List<Ranked> topTenPercent(List<Ranked> candidates) {
        if (candidates.isEmpty()) return List.of();
        var sorted = candidates.stream().sorted(ORDER).toList();
        int k = (candidates.size() + 9) / 10;
        int cutoff = sorted.get(k - 1).value();
        return sorted.stream().filter(r -> r.value() > 0 && r.value() >= cutoff).toList();
    }

    private double mean(int sum, int count) { return count == 0 ? 0.0 : (double) sum / count; }
}
