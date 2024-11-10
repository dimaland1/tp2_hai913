Programme d'analyse statique Java permettant de calculer le couplage entre classes et d'identifier des modules via clustering hiérarchique.

## Prérequis

- Java JDK
- Maven (pour la gestion des dépendances)
- Spoon (inclus dans les dépendances Maven)
- Graphviz (optionnel, recommandé pour visualiser les dendrogrammes)

## Installation

1. Clonez le dépôt :
   ```
   git clone https://github.com/dimaland1/tp2_hai913.git
   ```
2. Naviguez dans le répertoire du projet :
   ```
   cd tp2_hai913
   ```
3. Compilez le projet avec Maven :
   ```
   mvn clean install
   ```

## Utilisation

### Mode CLI (Interface en Ligne de Commande)

1. Exécutez le programme :
   ```
   MainCLI.java
   ```
2. Choisissez l'option 1 pour initialiser le chemin du projet et définir le seuil de couplage :
  - Entrez le chemin absolu du projet Java que vous souhaitez analyser.
  - Entrez le seuil de couplage entre 0 et 1 (le seuil permet de filtrer les relations entre classes selon leur intensité).

3. Choisissez l'option 2 pour afficher les graphes d'appels :
  - Cette option génère un graphe d'appels montrant les relations entre les méthodes des différentes classes analysées.

4. Choisissez l'option 3 pour générer les visualisations des graphes de couplage pondérés :
  - Les visualisations seront disponibles dans le répertoire du projet nommé coupling_graph.png, et le dendrogramme sera affiché directement dans le terminal. une représentation graphique via Graphviz du dendogramme n'a pas pu etre faite.

5. Choisissez l'option 4 pour exécuter l'analyse à l'aide de Spoon :
  - Un fichier image nommé spoon_coupling_graph.png contenant le graphe de couplage pondéré sera généré dans le répertoire du projet.

6. Choisissez l'option 5 pour quitter le programme.

### Mode Direct

Le projet peut également être utilisé en mode direct pour simplifier l'exécution de toutes les étapes d'analyse d'un coup.

1. Exécutez le programme :
   ```
   Main.java
   ```
   
2. Entrez le chemin absolu du projet Java que vous souhaitez analyser lorsque le programme le demande.

3. Entrez le seuil de couplage entre 0 et 1 lorsque le programme le demande.

4. Le programme effectuera automatiquement l'analyse complète :
  - Construction du graphe d'appels.
  - Génération des visualisations, y compris le dendrogramme et le graphe de couplage image nommé coupling_graph.png.
  - Création d'un fichier image nommé spoon_coupling_graph.png contenant le graphe de couplage pondéré dans le répertoire du projet.












