package org.example.clustering;

import org.example.graph.Pair;
import java.util.*;

/**
 * Implémente un algorithme de clustering hiérarchique pour regrouper des classes Java
 * en modules basés sur leur couplage.
 * Cette classe permet de:
 * - Regrouper des classes en clusters selon leur niveau de couplage
 * - Maintenir une hiérarchie de clusters (structure en arbre)
 * - Visualiser le processus de clustering via un dendrogramme
 * - Analyser la cohésion des modules résultants
 */
public class HierarchicalClustering {
    // Matrice stockant les valeurs de couplage entre paires de classes
    private final Map<Pair<String, String>, Double> couplingMatrix;
    // Liste des clusters actuels
    private List<Cluster> clusters;
    // Historique des étapes de clustering pour traçabilité
    private List<ClusteringStep> clusteringHistory;

    /**
     * Représente un cluster de classes avec sa structure hiérarchique.
     * Chaque cluster peut être:
     * - Une feuille (contenant une seule classe)
     * - Un nœud interne (résultat de la fusion de deux clusters)
     */
    public class Cluster {
        private Set<String> classes;          // Ensemble des classes dans ce cluster
        private Cluster leftChild;            // Sous-cluster gauche
        private Cluster rightChild;           // Sous-cluster droit
        private double mergeCoupling;         // Valeur de couplage lors de la fusion

        /**
         * Crée un nouveau cluster avec une seule classe (cluster feuille)
         * @param initialClass Nom de la classe initiale
         */
        public Cluster(String initialClass) {
            this.classes = new HashSet<>();
            this.classes.add(initialClass);
            this.leftChild = null;
            this.rightChild = null;
            this.mergeCoupling = 0.0;
        }

        /**
         * Crée un cluster à partir de la fusion de deux sous-clusters
         * @param classes Ensemble des classes du nouveau cluster
         * @param left Sous-cluster gauche
         * @param right Sous-cluster droit
         * @param coupling Valeur de couplage lors de la fusion
         */
        public Cluster(Set<String> classes, Cluster left, Cluster right, double coupling) {
            this.classes = new HashSet<>(classes);
            this.leftChild = left;
            this.rightChild = right;
            this.mergeCoupling = coupling;
        }

        // Getters
        public Set<String> getClasses() {
            return classes;
        }

        public Cluster getLeftChild() {
            return leftChild;
        }

        public Cluster getRightChild() {
            return rightChild;
        }

        public double getMergeCoupling() {
            return mergeCoupling;
        }

        @Override
        public String toString() {
            return "Cluster{classes=" + classes + ", mergeCoupling=" + String.format("%.3f", mergeCoupling) + "}";
        }
    }

    /**
     * Représente une étape dans le processus de clustering
     * Stocke les informations sur la fusion de deux clusters
     */
    public class ClusteringStep {
        private final Cluster cluster1;       // Premier cluster fusionné
        private final Cluster cluster2;       // Second cluster fusionné
        private final double coupling;        // Valeur de couplage lors de la fusion
        private final Cluster resultCluster;  // Cluster résultant de la fusion

        public ClusteringStep(Cluster c1, Cluster c2, double coupling, Cluster result) {
            this.cluster1 = c1;
            this.cluster2 = c2;
            this.coupling = coupling;
            this.resultCluster = result;
        }

        // Getters
        public Cluster getCluster1() { return cluster1; }
        public Cluster getCluster2() { return cluster2; }
        public double getCoupling() { return coupling; }
        public Cluster getResultCluster() { return resultCluster; }

        @Override
        public String toString() {
            return String.format("Merged %s and %s (coupling: %.3f)",
                    cluster1.getClasses(), cluster2.getClasses(), coupling);
        }
    }

    /**
     * Constructeur initialisant le clustering avec une matrice de couplage
     * @param couplingMatrix Matrice des valeurs de couplage entre classes
     */
    public HierarchicalClustering(Map<Pair<String, String>, Double> couplingMatrix) {
        this.couplingMatrix = couplingMatrix;
        this.clusters = new ArrayList<>();
        this.clusteringHistory = new ArrayList<>();
    }

    /**
     * Effectue le clustering hiérarchique
     * @param minCoupling Seuil minimum de couplage pour la fusion
     * @param maxModules Nombre maximum de modules souhaités
     * @return Liste des clusters finaux
     */
    public List<Cluster> performClustering(double minCoupling, int maxModules) {
        // Initialisation : chaque classe dans son propre cluster
        for (String className : getUniqueClasses()) {
            clusters.add(new Cluster(className));
        }

        // Continue la fusion tant que les conditions ne sont pas atteintes
        while (clusters.size() > maxModules && clusters.size() > 1) {
            // Trouve la meilleure paire de clusters à fusionner
            Pair<Cluster, Cluster> bestPair = findMostCoupledClusters();
            if (bestPair == null) break;

            // Calcule le couplage moyen entre les clusters
            double avgCoupling = calculateAverageClusterCoupling(
                    bestPair.getFirst(), bestPair.getSecond());

            // Vérifie si le couplage est suffisant pour la fusion
            if (avgCoupling >= minCoupling) {
                // Crée le nouveau cluster
                Cluster newCluster = new Cluster(
                        new HashSet<>(bestPair.getFirst().getClasses()),
                        bestPair.getFirst(),
                        bestPair.getSecond(),
                        avgCoupling
                );

                // Enregistre l'étape de clustering
                clusteringHistory.add(new ClusteringStep(
                        bestPair.getFirst(),
                        bestPair.getSecond(),
                        avgCoupling,
                        newCluster
                ));

                // Met à jour la liste des clusters
                clusters.remove(bestPair.getFirst());
                clusters.remove(bestPair.getSecond());
                clusters.add(newCluster);

                System.out.println("Fusion des clusters : " + bestPair.getFirst() +
                        " et " + bestPair.getSecond() +
                        " (couplage : " + String.format("%.3f", avgCoupling) + ")");
            } else {
                break;
            }
        }

        return new ArrayList<>(clusters);
    }

    /**
     * Trouve la paire de clusters ayant le plus fort couplage
     * @return La paire de clusters avec le meilleur couplage, ou null si aucune paire valide
     */
    private Pair<Cluster, Cluster> findMostCoupledClusters() {
        double maxCoupling = 0;
        Pair<Cluster, Cluster> bestPair = null;

        // Parcourt toutes les paires possibles de clusters
        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                double coupling = calculateAverageClusterCoupling(
                        clusters.get(i), clusters.get(j));
                // Met à jour si meilleur couplage trouvé
                if (coupling > maxCoupling) {
                    maxCoupling = coupling;
                    bestPair = new Pair<>(clusters.get(i), clusters.get(j));
                }
            }
        }

        return bestPair;
    }

    /**
     * Calcule le couplage moyen entre deux clusters
     * @param c1 Premier cluster
     * @param c2 Second cluster
     * @return Valeur moyenne de couplage
     */
    private double calculateAverageClusterCoupling(Cluster c1, Cluster c2) {
        double totalCoupling = 0;
        int relationCount = 0;

        // Calcule pour chaque paire de classes entre les clusters
        for (String classA : c1.classes) {
            for (String classB : c2.classes) {
                // Vérifie le couplage dans les deux sens
                Pair<String, String> pair = new Pair<>(classA, classB);
                Double coupling = couplingMatrix.get(pair);
                if (coupling != null) {
                    totalCoupling += coupling;
                    relationCount++;
                }
                pair = new Pair<>(classB, classA);
                coupling = couplingMatrix.get(pair);
                if (coupling != null) {
                    totalCoupling += coupling;
                    relationCount++;
                }
            }
        }

        return relationCount > 0 ? totalCoupling / relationCount : 0;
    }

    /**
     * Extrait l'ensemble des classes uniques de la matrice de couplage
     */
    private Set<String> getUniqueClasses() {
        Set<String> classes = new HashSet<>();
        couplingMatrix.keySet().forEach(pair -> {
            classes.add(pair.getFirst());
            classes.add(pair.getSecond());
        });
        return classes;
    }

    /**
     * Affiche le dendrogramme du clustering de manière textuelle
     */
    public void printDendrogram() {
        System.out.println("\nDendrogramme du clustering :");
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            printClusterHierarchy(cluster, "", i + 1);
        }
    }

    /**
     * Affiche récursivement la hiérarchie d'un cluster
     */
    private void printClusterHierarchy(Cluster cluster, String prefix, int index) {
        System.out.println(prefix + "Cluster " + index + ":");
        System.out.println(prefix + "  Classes: " + cluster.getClasses());
        System.out.println(prefix + "  Couplage: " + String.format("%.3f", cluster.getMergeCoupling()));

        // Affiche récursivement les sous-clusters
        if (cluster.getLeftChild() != null) {
            System.out.println(prefix + "  Sous-cluster gauche:");
            printClusterHierarchy(cluster.getLeftChild(), prefix + "    ", index);
        }
        if (cluster.getRightChild() != null) {
            System.out.println(prefix + "  Sous-cluster droit:");
            printClusterHierarchy(cluster.getRightChild(), prefix + "    ", index);
        }
    }

    /**
     * Affiche l'historique détaillé du processus de clustering
     */
    public void printDetailedDendrogram() {
        System.out.println("\nHistorique détaillé du clustering :");
        for (ClusteringStep step : clusteringHistory) {
            System.out.println(step);
            System.out.println("  Résultat: " + step.getResultCluster().getClasses());
            System.out.println("  Couplage: " + String.format("%.3f", step.getCoupling()));
            System.out.println();
        }
    }

    /**
     * Identifie les modules à partir d'un seuil de couplage donné
     * @param couplingThreshold Seuil minimal de couplage
     * @return Liste des ensembles de classes formant des modules
     */
    public List<Set<String>> getModulesAtThreshold(double couplingThreshold) {
        List<Set<String>> modules = new ArrayList<>();

        for (Cluster cluster : clusters) {
            double cohesion = calculateModuleCohesion(cluster);
            if (cohesion >= couplingThreshold) {
                modules.add(new HashSet<>(cluster.getClasses()));
            } else {
                // Vérifie les sous-clusters si le cluster principal ne satisfait pas le seuil
                if (cluster.getLeftChild() != null &&
                        calculateModuleCohesion(cluster.getLeftChild()) >= couplingThreshold) {
                    modules.add(new HashSet<>(cluster.getLeftChild().getClasses()));
                }
                if (cluster.getRightChild() != null &&
                        calculateModuleCohesion(cluster.getRightChild()) >= couplingThreshold) {
                    modules.add(new HashSet<>(cluster.getRightChild().getClasses()));
                }
            }
        }

        return modules;
    }

    /**
     * Analyse et affiche les caractéristiques de chaque cluster
     */
    public void analyzeClusters() {
        System.out.println("\nAnalyse des clusters :");
        Map<Cluster, Double> cohesions = calculateAllModuleCohesion();

        for (Map.Entry<Cluster, Double> entry : cohesions.entrySet()) {
            Cluster cluster = entry.getKey();
            double cohesion = entry.getValue();

            System.out.println("\nCluster: " + cluster.getClasses());
            System.out.println("Cohésion: " + String.format("%.3f", cohesion));

            if (cluster.getLeftChild() != null && cluster.getRightChild() != null) {
                System.out.println("Composé de:");
                System.out.println("  - " + cluster.getLeftChild().getClasses());
                System.out.println("  - " + cluster.getRightChild().getClasses());
            }
        }
    }

    /**
     * Calcule la cohésion d'un module (cluster)
     * @param cluster Cluster à analyser
     * @return Valeur de cohésion entre 0 et 1
     */
    public double calculateModuleCohesion(Cluster cluster) {
        if (cluster.classes.size() <= 1) return 1.0;

        double totalCoupling = 0;
        int relationships = 0;

        // Calcule le couplage moyen entre toutes les classes du cluster
        for (String classA : cluster.classes) {
            for (String classB : cluster.classes) {
                if (!classA.equals(classB)) {
                    Pair<String, String> pair = new Pair<>(classA, classB);
                    Double coupling = couplingMatrix.get(pair);
                    if (coupling != null) {
                        totalCoupling += coupling;
                        relationships++;
                    }
                }
            }
        }

        return relationships > 0 ? totalCoupling / relationships : 0;
    }

    /**
     * Calcule la cohésion pour tous les clusters actuels
     * @return Map associant chaque cluster à sa valeur de cohésion
     */
    public Map<Cluster, Double> calculateAllModuleCohesion() {
        Map<Cluster, Double> cohesions = new HashMap<>();
        for (Cluster cluster : clusters) {
            cohesions.put(cluster, calculateModuleCohesion(cluster));
        }
        return cohesions;
    }

    public List<ClusteringStep> getClusteringHistory() {
        return clusteringHistory;
    }

    public List<Cluster> getCurrentClusters() {
        return new ArrayList<>(clusters);
    }

    public Map<Pair<String, String>, Double> getCouplingMatrix() {
        return couplingMatrix;
    }
}