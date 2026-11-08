package hai913i.tp1.parse;

import hai913i.tp1.model.AnalysisFacts.TypeFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.ProjectFacts.SourceFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Frontière JDT : une seule extraction de chaque fichier vers le modèle. */
public final class FactExtractor {
    private FactExtractor() {}
    public static ProjectFacts extract(Path sourceRoot, List<JdtParser.ParsedFile> parsed) throws IOException {
        List<TypeFact> types = new ArrayList<>();
        List<SourceFile> files = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (var file : parsed) {
            String path = sourceRoot.toAbsolutePath().normalize().relativize(file.path().toAbsolutePath().normalize())
                    .toString().replace('\\', '/');
            files.add(new SourceFile(path, Files.readAllLines(file.path(), StandardCharsets.UTF_8).size()));
            JdtVisitorClasses visitor = new JdtVisitorClasses(path);
            file.unit().accept(visitor);
            types.addAll(visitor.types());
            for (var problem : file.unit().getProblems()) {
                if (problem.isError()) errors.add(path + ":" + problem.getSourceLineNumber() + " " + problem.getMessage());
            }
        }
        return new ProjectFacts(files, types, errors);
    }
}
