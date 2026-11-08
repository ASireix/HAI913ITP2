# Exports conservés pour la remise

Ces fichiers sont produits par le programme, à partir des sources Java du projet de validation, avec les appels seuls et le lien moyen. Ils permettent de consulter les résultats sans exécuter l'outil.

| Dossier | CP | Résultat |
|---|---:|---|
| `cp-0.015/` | 0.015 | 4 modules acceptés, dont un de 11 classes |
| `cp-0.025/` | 0.025 | 7 modules acceptés, référence retenue |
| `cp-0.045/` | 0.045 | 10 modules rejetés : limite de 7 dépassée |

Chaque dossier contient le graphe (sommets/arêtes CSV et DOT), la trace des fusions, le dendrogramme (texte, DOT et SVG), les modules CSV et les paramètres de liaison et de relation. La racine du graphe de faits compte 15 classes, 59 méthodes et 44 arcs ; son graphe de couplage compte 15 sommets et 19 arêtes.

Pour régénérer, depuis `source/`, exécuter sur une ligne par commande :

```powershell
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.015 --output ../sorties/cp-0.015
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.025 --output ../sorties/cp-0.025
java -jar target/hai913i-tp2.jar --tp1 resources/validation --cp 0.045 --output ../sorties/cp-0.045
```

La dernière commande retourne le code 3 attendu pour le rejet. Depuis la racine du dépôt, après l'installation optionnelle des outils de rendu :

```powershell
node tools/graphviz/render.mjs sorties/cp-0.015 sorties/cp-0.025 sorties/cp-0.045
```

Les nouveaux exports remplacent les fichiers homonymes. Les exemples supplémentaires C1/C2 se reproduisent avec les commandes du README principal.
