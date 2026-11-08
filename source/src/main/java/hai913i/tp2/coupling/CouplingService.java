package hai913i.tp2.coupling;

import hai913i.tp2.input.InputFacts;
import java.util.HashMap;

/** A1 : compte les arcs distincts fournis par A0, jamais les sites d'appel. */
public final class CouplingService {
    public enum RelationMode { CALLS, ENRICHED }
    public Coupling calculate(InputFacts facts) {
        return calculate(facts, RelationMode.CALLS);
    }

    public Coupling calculate(InputFacts facts, RelationMode mode) {
        var counts = new HashMap<Coupling.Pair, Integer>();
        int intra = 0;
        int inter = 0;
        for (var arc : facts.calls()) {
            if (arc.callerClass().equals(arc.calleeClass())) {
                intra++;
            } else {
                inter++;
                counts.merge(new Coupling.Pair(arc.callerClass(), arc.calleeClass()),
                        1, Integer::sum);
            }
        }
        if (mode == RelationMode.ENRICHED) {
            for (var relation : facts.structuralRelations().stream().distinct().toList()) {
                if (!relation.source().equals(relation.target())) {
                    inter++;
                    counts.merge(new Coupling.Pair(relation.source(), relation.target()), 1, Integer::sum);
                }
            }
        }
        return new Coupling(facts.classNames(), intra, inter, counts);
    }
}
