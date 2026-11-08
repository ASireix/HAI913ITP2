# HAI913I — TP2 : Compréhension et architectures

Université de Montpellier · Faculté des Sciences · 2026–2027 · Enseignant : Bachar Rima

**Auteur :** Alexandre SIREIX · **Numéro étudiant :** 22414718 · **Parcours :** GL · **Rendu :** monôme.

**Date de remise prévue :** 8 novembre 2026.

**Dépôt Git :** [ASireix/HAI913ITP2](https://github.com/ASireix/HAI913ITP2).

**Vidéo de démonstration :** [voir la vidéo](https://youtu.be/EB8bpRMm6iI). Les liens de remise sont également regroupés dans [liens.txt](liens.txt).

Ce README constitue le guide de reconstruction, d'exécution et de vérification du projet. Le rapport à lire est [Compte-Rendu.pdf](Compte-Rendu.pdf) ; sa source est dans `rapport/Compte-Rendu.tex`.

## Périmètre

L'outil transforme les faits d'un projet Java en modules : couplage entre classes, graphe pondéré, clustering hiérarchique, dendrogramme et coupe sous contraintes.

- **A0 à B4** : réalisés et documentés.
- **C1** : comparaison du lien moyen et du lien simple.
- **C2** : ajout d'héritages et d'interfaces directs entre types du projet.
- **C3** : interface graphique Swing d'exploration.
- **C4** : non réalisé, faute de second projet Java disponible.

Le chemin principal reprend **notre modèle de faits du TP1**, derrière `InputFacts` et `Tp1FactsAdapter`. Le lancement `--tp1` exécute l'extracteur une fois en amont ; les calculs TP2 utilisent ensuite uniquement les faits. Le chemin CSV fourni sert de référence et de micro jeu. Le programme ne charge pas les JSON fournis.

Les modules calculés ne sont pas automatiquement des composants déployables ou des microservices. La validité du calcul ne dispense pas d'interpréter les regroupements.

## Organisation du dépôt

```text
TP2/
├── Compte-Rendu.pdf              rapport à lire
├── README.md
├── liens.txt                     dépôt Git et vidéo externe
├── .gitignore
├── .gitattributes
├── source/                       projet Maven autonome du TP2
│   ├── pom.xml
│   ├── src/main/java/hai913i/
│   │   ├── tp1/                  producteur de faits réutilisé
│   │   └── tp2/                  calculs, exports, CLI et interface
│   ├── src/test/                 tests et ressources unitaires
│   └── resources/                sources Java de validation et robustesse
├── resources/                    faits canoniques du projet de validation
│   └── mini/                     micro jeu de faits et explications fournies
├── docs/ARCHITECTURE.md           chaîne des traitements et responsabilités
├── rapport/
│   ├── Compte-Rendu.tex
│   └── figures/                  images nécessaires au rapport
├── tools/
│   └── graphviz/                 script et dépendances verrouillées de rendu
└── sorties/                      exports conservés pour la remise
```

Les figures nécessaires à la compilation du rapport sont dans `rapport/figures/`. Les dépendances du script de rendu sont verrouillées dans `tools/graphviz/package-lock.json`.

`sorties/` contient les graphes, dendrogrammes et modules de référence pour les trois seuils testés. Ce dossier est versionné ; les exemples de commandes utilisent `output/` pour leurs nouvelles expériences. L'organisation conserve les sources LaTeX et les ressources nécessaires à une reconstruction complète, en complément des livrables à la racine.

Pour le détail des services, voir [l'architecture](docs/ARCHITECTURE.md).

## Environnement et dépendances

| Élément | Version ou exigence |
|---|---|
| Système utilisé pour les vérifications | Windows 11, PowerShell |
| Java utilisé | Amazon Corretto JDK 17.0.13 |
| Java minimum | JDK 17, avec `javac` |
| Maven utilisé | 3.10.0 |
| Eclipse JDT Core | 3.46.0, dépendance du producteur TP1 |
| JUnit Jupiter | 5.14.4, tests uniquement |
| Interface graphique | Swing, fourni par le JDK ; session graphique nécessaire |
| Rendu optionnel des DOT | Graphviz 16.1.0, fourni par `@viz-js/viz` 3.31.0 |
| Outils utilisés pour ce rendu | Node.js 20.14.0 et npm 10.7.0 |
| Rapport | Source LaTeX compilée avec le compilateur intégré de l'éditeur Codex |

Le projet se construit en ligne de commande ; aucun IDE n'est imposé. Importer `source/pom.xml` dans un IDE si souhaité. Les sources ciblent Java 17 et utilisent UTF-8. Les dépendances et versions des plugins Maven sont dans `source/pom.xml` ; le rendu Graphviz est verrouillé dans `tools/graphviz/package-lock.json`.

**Node.js, npm, Graphviz et LaTeX ne sont pas nécessaires pour construire ou exécuter le JAR.** Node.js/npm servent uniquement à produire les SVG. La première construction Maven et la première installation npm nécessitent un accès réseau si les caches ne sont pas déjà remplis.

## Reconstruire depuis un clone propre

Après récupération du dépôt, placer le terminal dans son dossier racine, celui qui contient ce README.

```powershell
cd source
java -version
javac -version
mvn -v
mvn clean package
```

Résultat attendu : **36 tests, aucun échec**, `BUILD SUCCESS`, puis `source/target/hai913i-tp2.jar` depuis la racine du dépôt. Ce JAR inclut les dépendances d'exécution. Maven doit utiliser un JDK, pas uniquement un JRE.

Pour exécuter uniquement les tests :

```powershell
mvn test
```

`mvn -o clean package` fonctionne seulement si les dépendances et plugins sont déjà en cache. Pour un clone neuf, utiliser d'abord la commande normale sans `-o`.

## Exécution : répertoire de travail et arguments

**Toutes les commandes Java de cette section et des exemples suivants se lancent depuis `source/`.** Si le terminal est à la racine du dépôt, faire d'abord `cd source`. Entourer de guillemets tout chemin contenant des espaces.

```text
java -jar target/hai913i-tp2.jar
  (--tp1 <projet-java> | --csv <dossier-faits>)
  [--output <dossier>] [--cp <seuil>]
  [--linkage average|single] [--relations calls|enriched] [--gui]
```

La syntaxe ci-dessus est descriptive : saisir une commande sur **une seule ligne**, sans les parenthèses ni crochets.

| Argument | Signification |
|---|---|
| `--tp1 <chemin>` | Analyse les sources Java du projet avec le producteur TP1 puis adapte les faits. |
| `--csv <chemin>` | Charge trois CSV de faits dans un dossier ; ce n'est pas un dossier d'exports de graphe. |
| `--cp <valeur>` | Déclenche la coupe ; nombre fini positif ou nul, avec un point décimal. Aucun seuil par défaut. |
| `--output <chemin>` | Crée le dossier et écrit/remplace les exports. Sans cette option, affichage seulement. |
| `--linkage average` | Lien moyen, comportement de référence et valeur par défaut. |
| `--linkage single` | Lien simple, bonus C1. |
| `--relations calls` | Arcs d'appels seuls, référence et valeur par défaut. |
| `--relations enriched` | Ajoute héritage/interfaces directs ; nécessite `--tp1`. |
| `--gui` | Ouvre l'explorateur Swing avec les paramètres du lancement. |

Sans `--cp`, le programme effectue A0 à B2 et indique qu'aucune coupe n'a été demandée. Les options peuvent être données dans un ordre quelconque après le mode d'entrée et son chemin. Les options répétées ou inconnues sont refusées. Il n'y a pas de mode interactif de saisie des arguments dans le terminal.

### Référence : nos faits TP1 et coupe acceptée

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.025 --output ../output/reference
```

Résultat : 15 classes, 59 méthodes, 44 arcs ; dénominateur 37 ; graphe de 15 sommets et 19 arêtes ; 14 fusions ; 7 modules acceptés. Les sorties longues correspondent aux différentes étapes du TP.

### Référence canonique et micro jeu

```powershell
java -jar target/hai913i-tp2.jar --csv ../resources --cp 0.025 --output ../output/canonical
java -jar target/hai913i-tp2.jar --csv ../resources/mini --cp 0.20 --output ../output/mini
```

Le dossier CSV doit contenir exactement un jeu de trois fichiers `*-classes.csv`, `*-methods.csv` et `*-calls.csv`, au même préfixe. Le préfixe est découvert automatiquement. Les formats et conventions sont documentés dans `resources/README.txt` et `resources/mini/README.md`.

### Variation du seuil et rejet attendu

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.015 --output ../output/cp-0015
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.045 --output ../output/cp-0045
$LASTEXITCODE
```

À 0.015 : 4 modules acceptés, dont un de 11 classes. À 0.045 : 10 modules, coupe rejetée car le maximum est 7 ; le code de sortie est **3**, résultat attendu. Les exports restent disponibles pour examiner ce rejet. Utiliser des dossiers distincts pour garder plusieurs expériences.

### C1 : comparer les liaisons

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --linkage average --cp 0.025 --output ../output/c1-average
java -jar target/hai913i-tp2.jar --tp1 resources/validation --linkage single --cp 0.025 --output ../output/c1-single
```

Les arbres divergent à la deuxième fusion : le lien moyen réunit LibraryApp et Catalog à 3/37 ; le lien simple rattache Loan à Item–LoanService à 4/37. À CP=0.025, les deux coupes donnent 7 modules, avec des compositions différentes.

### C2 : comparer les dépendances

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --relations calls --cp 0.025 --output ../output/c2-calls
java -jar target/hai913i-tp2.jar --tp1 resources/validation --relations enriched --cp 0.025 --output ../output/c2-enriched
java -jar target/hai913i-tp2.jar --tp1 resources/validation --relations enriched --cp 0.0225609756097561 --output ../output/c2-scaled
```

Les quatre ajouts sont Book–Item, Dvd–Item, Item–Loanable et ConsoleNotifier–Notifier. Chaque relation compte pour 1, comme un arc de méthodes. Le dénominateur passe de 37 à 41 et le graphe à 22 arêtes. Dvd n'est plus isolée ; LoanException le reste.

À 0.025 : 8 modules et rejet. L'essai complémentaire au seuil `0.025*37/41` donne 7 modules acceptés, mais ne restaure pas la partition initiale : Loanable rejoint le noyau, Notifier en sort. Le seuil ne se saisit pas comme une expression : utiliser la valeur numérique complète dans la commande ci-dessus.

### C3 : exploration graphique

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.025 --gui
```

- Déplier les branches et sélectionner un groupe pour consulter ses classes, sa similarité de fusion et sa moyenne interne.
- Rechercher `Dvd`, même avec un nom partiel : les chemins sont ouverts et les feuilles trouvées sélectionnées.
- Modifier CP puis cliquer **Appliquer CP** : la table et le statut de coupe sont actualisés.
- Cliquer une ligne de module pour sélectionner sa branche.
- Choisir AVERAGE ou SINGLE pour comparer les liaisons.

Sans seuil initial, la fenêtre demande de saisir CP. Une coupe rejetée reste affichée pour permettre de modifier le seuil. Le terminal se libère à la fermeture de la fenêtre. Les interactions ne réécrivent pas les exports : ceux de `--output` correspondent uniquement aux paramètres initiaux. La relation calls/enriched se choisit au lancement. L'interface nécessite un environnement graphique ; un lancement en environnement headless est refusé.

## Sorties produites

Les fichiers suivants sont écrits dans le dossier fourni à `--output` :

| Fichier | Contenu |
|---|---|
| `coupling-nodes.csv` | En-tête `class`, puis tous les sommets, isolés compris. |
| `coupling-edges.csv` | `first;second;weight`, une arête non orientée positive par ligne. |
| `coupling.dot` | Graphe pondéré Graphviz. |
| `merges.csv` | `step;left;right;result;similarity;members`, ordre des fusions. |
| `dendrogram.txt` | Arbre indenté avec classes, groupes et similarités. |
| `dendrogram.dot` | Arbre Graphviz dont le titre identifie la liaison. |
| `linkage.txt` | AVERAGE ou SINGLE. |
| `relations.txt` | CALLS ou ENRICHED. |
| `modules.csv` | Si CP est fourni : seuil, validité, limite, groupe, effectif, moyenne et classes. |
| `structural-relations.csv` | En mode enrichi : source, cible et nature des ajouts. |

Les fichiers texte sont en UTF-8 ; les CSV utilisent le point-virgule. Les listes de membres utilisent la virgule. Les CSV conservent la précision double ; les étiquettes graphiques sont arrondies. Les singletons ont une moyenne `NA`.

`GraphIO.read(Path)` recharge les deux CSV du graphe. Le lancement vérifie leur égalité avec le graphe original après chaque export. **Ces exports ne sont pas les trois CSV de faits lus par `--csv`.**

Les fichiers homonymes sont remplacés. Un ancien `modules.csv` n'est pas actualisé par un lancement sans CP, et un ancien fichier structurel n'est pas actualisé en mode calls : privilégier un dossier neuf par expérience.

## Produire les images (optionnel)

Revenir à la **racine du dépôt** depuis `source` :

```powershell
cd ..
node --version
npm --version
npm ci --prefix tools/graphviz
node tools/graphviz/render.mjs output/reference output/c1-single
```

Le script rend chaque `dendrogram.dot` en `dendrogram.svg` dans son dossier et affiche la version du moteur Graphviz. Les dossiers doivent avoir été produits par les commandes Java correspondantes. Le rendu est topologique : les distances dessinées ne sont pas une échelle de similarité.

Un Graphviz natif peut aussi rendre les exports :

```powershell
dot -V
dot -Tsvg output/reference/coupling.dot -o output/reference/coupling.svg
```

La commande native `dot` n'était pas dans le PATH lors du développement ; le rendu validé utilise Graphviz WebAssembly. Consigner la version si un autre moteur est utilisé.

## Validation et conventions

| Contrôle | Valeur de référence |
|---|---|
| A0 validation | 15 classes, 59 méthodes, 44 arcs pour 46 sites |
| A0 micro jeu | 3 classes, 5 méthodes, 4 arcs pour 5 sites |
| A1 | 7 arcs intra-classe écartés ; 37 inter-classes ; 19 couples non nuls |
| Plus fort couplage | Item–LoanService et LoanService–Loan : 4/37 ≈ 0.1081 |
| A2 | 15 sommets, 19 arêtes ; Dvd et LoanException isolées |
| B1/B2 | 14 fusions ; 15 feuilles, 14 nœuds internes ; deux dernières fusions à zéro |
| B3 | CP 0.015 → 4 modules ; 0.025 → 7 ; 0.045 → 10 et rejet |
| C1 | Première divergence structurelle à la fusion 2 |
| C2 | 4 relations ajoutées, dénominateur 41, 22 arêtes |
| Tests | 36 tests : TP1, A0–B3, C1–C3 |

Le couplage compte des **arcs distincts**, pas la somme des sites. Il est symétrique, de diagonale nulle et de somme 1 sur les couples non orientés. Le lien moyen inclut les couples nuls. Les égalités de fusion sont départagées à `1e-9` près du maximum par le plus petit couple d'identifiants stables. La coupe utilise le couplage interne moyen, strictement supérieur à CP, jamais la similarité de fusion. Elle conserve les feuilles et contrôle ensuite la limite entière `M/2`.

Les tests comparent les faits au jeu canonique, les poids, le rechargement exact, les partitions, les branches, les fusions et les cas limites. Les composants Swing sont testés sur le thread événementiel ; leur disposition a été inspectée par un rendu hors écran. L'essai manuel renseigné confirme l'ouverture de la fenêtre native et le dépliage de l'arbre. La validation détaillée de la recherche, de l'inspection des modules, de la modification de CP et du changement de liaison repose sur les tests automatisés.

## Codes de sortie

| Code | Signification |
|---|---|
| 0 | Traitement réussi ; coupe acceptée si demandée. En mode GUI, ouverture réussie : un rejet reste modifiable dans la fenêtre. |
| 1 | Arguments invalides, valeur manquante, option inconnue/répétée ou seuil invalide. |
| 2 | Erreur de chargement, d'analyse, de calcul, d'export ou d'ouverture graphique. |
| 3 | En CLI seul : coupe rejetée car trop de modules. |

## Dépannage

| Symptôme | Vérification ou solution |
|---|---|
| Maven ne trouve pas de compilateur | Installer/sélectionner un JDK 17+, contrôler `JAVA_HOME`, `javac -version` et le Java affiché par `mvn -v`. |
| JAR absent | Lancer `mvn clean package` depuis `source`, vérifier `BUILD SUCCESS`. |
| Dossier ou CSV introuvable | Vérifier le répertoire courant ; les exemples Java supposent `source/`. Entourer les chemins avec espaces de guillemets. |
| Plusieurs jeux CSV détectés | Mettre chaque jeu dans un dossier distinct ; utiliser `../resources/mini` pour le micro jeu. |
| Erreur avec CP | Utiliser `0.025`, pas `0,025` ; refuser NaN, infinis et nombres négatifs. |
| Échec Maven en mode hors ligne | Retirer `-o` pour remplir le cache et vérifier l'accès aux dépôts Maven. |
| Diagnostic source avec `--tp1` | Corriger les sources ou les dépendances du projet analysé ; la CLI TP2 refuse les diagnostics avant validation des faits. |
| Mode enriched avec CSV | Utiliser `--tp1` : le chargeur CSV ne contient pas les relations structurelles. |
| Fenêtre absente | Utiliser une session graphique et un JDK avec Swing ; attendre la fin de l'extraction des faits. |
| npm/Node absent | Facultatif pour le JAR ; installer ces outils seulement si le rendu SVG est nécessaire. |
| `dot` introuvable | Utiliser le script WebAssembly après `npm ci`, sans installation native. |
| Exports ressemblant à une ancienne expérience | Utiliser un nouveau dossier `--output` et contrôler `linkage.txt` et `relations.txt`. |

## Limites

Le modèle statique ne mesure pas la fréquence d'exécution ni les responsabilités métier. La référence ignore héritage, signatures, attributs, instanciations et exceptions ; le bonus C2 ne complète que l'héritage et les interfaces directs internes au projet. Le seuil dépend de la normalisation et du projet. Le regroupement est glouton et les coupes sont limitées aux branches de son arbre. Les calculs Swing synchrones sont destinés aux petits projets du TP.

Le code est validé sur le projet de bibliothèque et le micro jeu fournis, pas sur un deuxième projet indépendant (C4 non réalisé). L'interprétation et les limites sont développées dans le rapport.

Pour le rapport, conserver `Compte-Rendu.tex` et son dossier `figures` ensemble. Il a été compilé avec le compilateur intégré de l'éditeur ; un environnement LaTeX équipé de pdfLaTeX et des paquets utilisés peut également le compiler deux fois depuis `rapport/`.

L'aide de l'IA a porté sur les réponses aux questions, la rédaction d'une partie du code, les exemples et la mise en forme du rapport ; la déclaration figure dans celui-ci.
