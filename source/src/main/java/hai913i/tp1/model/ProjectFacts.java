package hai913i.tp1.model;

import hai913i.tp1.model.AnalysisFacts.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;

/** Résultat complet de l'extraction, utilisable sans AST ni binding JDT. */
public record ProjectFacts(List<SourceFile> files, List<TypeFact> types, List<String> errors) {
    public record SourceFile(String path, int lineCount) {}
    public ProjectFacts {
        files = files.stream().sorted(Comparator.comparing(SourceFile::path)).toList();
        types = types.stream().sorted(Comparator.comparing(TypeFact::name)).toList();
        errors = errors.stream().sorted().toList();
    }
    public int sourceLoc() { return files.stream().mapToInt(SourceFile::lineCount).sum(); }
    public List<MethodFact> methods() { return types.stream().flatMap(t -> t.methods().stream()).toList(); }
    public List<CallFact> calls() { return methods().stream().flatMap(m -> m.calls().stream()).toList(); }
    public Map<String, MethodFact> methodsById() {
        Map<String, MethodFact> index = new TreeMap<>();
        for (MethodFact method : methods()) {
            if (index.putIfAbsent(method.id(), method) != null)
                throw new IllegalStateException("Identifiant de méthode dupliqué : " + method.id());
        }
        return Collections.unmodifiableMap(index);
    }
}
