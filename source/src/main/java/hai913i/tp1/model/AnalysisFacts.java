package hai913i.tp1.model;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;

/** Données de la partie A, indépendantes de JDT. */
public final class AnalysisFacts {
    private AnalysisFacts() {}
    public record FieldFact(String name, String type, String visibility) {}
    public record CallFact(String name, int line, String target, String receiver,
                           boolean resolved, boolean internal, boolean superCall,
                           String callerId, Optional<String> targetId) {}
    public record MethodFact(String name, List<String> parameterTypes, boolean constructor,
                             List<CallFact> calls, String id, int bodyLoc) {
        public MethodFact {
            parameterTypes = List.copyOf(parameterTypes);
            calls = List.copyOf(calls);
        }
        public int parameterCount() { return parameterTypes.size(); }
    }
    public record TypeFact(String file, String name, String kind, String packageName,
                           List<String> superclasses, List<String> interfaces,
                           List<FieldFact> fields, List<MethodFact> methods) {
        public TypeFact {
            superclasses = List.copyOf(superclasses);
            interfaces = List.copyOf(interfaces);
            fields = List.copyOf(fields);
            methods = methods.stream().sorted(Comparator.comparing(MethodFact::id)).toList();
        }
    }
}
