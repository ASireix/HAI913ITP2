package hai913i.tp1.metrics;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

/** Résultats des questions B2, sans dépendance au parseur. */
public record MetricsResult(int types, int sourceLoc, int methods, long packages,
        double methodsPerType, double linesPerMethod, double fieldsPerType,
        int bodyCount, int bodyLoc, List<Ranked> mostMethods, List<Ranked> mostFields,
        List<String> intersection, int threshold, List<Ranked> aboveThreshold,
        Map<String, List<Ranked>> longestMethods, int maxParameters, List<String> maxParameterMethods) {
    public record Ranked(String name, int value) {}
    public MetricsResult {
        mostMethods = List.copyOf(mostMethods);
        mostFields = List.copyOf(mostFields);
        intersection = List.copyOf(intersection);
        aboveThreshold = List.copyOf(aboveThreshold);
        Map<String, List<Ranked>> copy = new TreeMap<>();
        longestMethods.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        longestMethods = Collections.unmodifiableMap(copy);
        maxParameterMethods = List.copyOf(maxParameterMethods);
    }
}
