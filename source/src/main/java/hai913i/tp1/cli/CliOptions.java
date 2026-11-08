package hai913i.tp1.cli;

import java.nio.file.Path;
import java.util.Set;

public record CliOptions(Path project, String action, boolean lite, int threshold,
                         String callers, String callees, boolean help) {
    public static final String USAGE = """
            Usage : java -jar target/hai913i-tp1-analyzer.jar PROJET [options]
              --action all|summary|ast|structure|calls|metrics|graph (défaut : all)
              --threshold X    entier positif ou nul (défaut : 5)
              --callers ID     appelantes de la méthode (action graph ou all)
              --callees ID     appelées de la méthode (action graph ou all)
              lite | --lite    AST simplifié
              --help          afficher cette aide
            Exemple : java -jar target/hai913i-tp1-analyzer.jar resources/validation --action metrics --threshold 5
            Entourer les chemins avec espaces et les identifiants de méthodes de guillemets.
            """;

    public static CliOptions parse(String[] args) {
        if (java.util.Arrays.asList(args).contains("--help"))
            return new CliOptions(null, "all", false, 5, null, null, true);
        if (args.length == 0 || args[0].startsWith("--"))
            throw new IllegalArgumentException("Chemin du projet manquant.");
        String action = "all", callers = null, callees = null;
        boolean lite = false;
        int threshold = 5;
        for (int i = 1; i < args.length; i++) {
            String option = args[i];
            if (option.equals("lite") || option.equals("--lite")) { lite = true; continue; }
            if (!Set.of("--action", "--threshold", "--callers", "--callees").contains(option))
                throw new IllegalArgumentException("Option inconnue : " + option);
            if (i + 1 == args.length || args[i + 1].startsWith("--") || args[i + 1].isBlank())
                throw new IllegalArgumentException("Valeur manquante pour " + option);
            String value = args[++i];
            switch (option) {
                case "--action" -> action = value;
                case "--callers" -> callers = value;
                case "--callees" -> callees = value;
                case "--threshold" -> {
                    try { threshold = Integer.parseInt(value); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("X doit être un entier positif ou nul : " + value); }
                    if (threshold < 0) throw new IllegalArgumentException("X doit être positif ou nul.");
                }
                default -> throw new IllegalArgumentException(option);
            }
        }
        if (!Set.of("all", "summary", "ast", "structure", "calls", "metrics", "graph").contains(action))
            throw new IllegalArgumentException("Action inconnue : " + action);
        if ((callers != null || callees != null) && !Set.of("all", "graph").contains(action))
            throw new IllegalArgumentException("Les recherches nécessitent --action graph ou all.");
        return new CliOptions(Path.of(args[0]), action, lite, threshold, callers, callees, false);
    }
    public boolean wants(String section) { return action.equals("all") || action.equals(section); }
}
