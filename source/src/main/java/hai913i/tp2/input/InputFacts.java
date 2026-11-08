package hai913i.tp2.input;

import hai913i.tp2.Facts;
import java.util.List;
import java.util.Map;

/** Frontière commune : les calculs TP2 ne voient ni JDT ni le modèle TP1. */
public interface InputFacts {
    List<String> classNames();
    Map<String, String> methodOwners();
    List<Facts.CallArc> calls();
    record StructuralRelation(String source, String target, String kind) {}
    /** Les CSV d'appels seuls ne contiennent pas ces faits : ne pas simuler une liste vide. */
    default List<StructuralRelation> structuralRelations() {
        throw new UnsupportedOperationException("Le mode enrichi requiert --tp1 : les CSV ne contiennent pas les relations structurelles");
    }
}
