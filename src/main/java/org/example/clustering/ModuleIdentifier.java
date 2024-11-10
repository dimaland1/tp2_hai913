package org.example.clustering;

import org.example.graph.Pair;
import java.util.*;

/**
 * Classe responsable de l'identification des modules dans un système
 * en se basant sur les résultats d'un clustering hiérarchique.
 * Elle utilise des critères de couplage et de taille pour déterminer
 * les clusters qui constituent des modules valides.
 */
public class ModuleIdentifier {
    // Les clusters résultant du clustering hiérarchique
    private final List<HierarchicalClustering.Cluster> clusters;

    // La matrice de couplage entre toutes les classes
    private final Map<Pair<String, String>, Double> couplingMatrix;

    // Nombre maximum de modules autorisés
    private final int maxModules;

    // Seuil minimal de couplage pour qu'un cluster soit considéré comme module
    private final double couplingThreshold;

    /**
     * Constructeur initialisant l'identificateur de modules.
     *
     * @param clusters Liste des clusters à analyser
     * @param couplingMatrix Matrice des couplages entre classes
     * @param maxModules Nombre maximum de modules à identifier
     * @param couplingThreshold Seuil minimal de couplage pour un module valide
     */
    public ModuleIdentifier(List<HierarchicalClustering.Cluster> clusters,
                            Map<Pair<String, String>, Double> couplingMatrix,
                            int maxModules, double couplingThreshold) {
        this.clusters = clusters;
        this.couplingMatrix = couplingMatrix;
        this.maxModules = maxModules;
        this.couplingThreshold = couplingThreshold;
    }

    /**
     * Identifie les modules valides parmi les clusters disponibles.
     * Un cluster est considéré comme un module valide si :
     * 1. Le nombre maximum de modules n'est pas dépassé
     * 2. Le couplage moyen interne dépasse le seuil défini
     *
     * @return Liste des clusters identifiés comme modules valides
     */
    public List<HierarchicalClustering.Cluster> identifyModules() {
        List<HierarchicalClustering.Cluster> modules = new ArrayList<>();

        // Examine chaque cluster
        for (HierarchicalClustering.Cluster cluster : clusters) {
            // Vérifie les deux conditions pour être un module valide
            if (modules.size() < maxModules &&
                    calculateAverageCoupling(cluster) > couplingThreshold) {
                modules.add(cluster);
            }
        }
        return modules;
    }

    /**
     * Calcule le couplage moyen entre toutes les classes d'un cluster.
     * Cette mesure représente la cohésion interne du cluster.
     *
     * Le calcul :
     * 1. Somme tous les couplages entre paires de classes différentes
     * 2. Divise par le nombre de relations pour obtenir la moyenne
     *
     * @param cluster Le cluster dont on veut calculer la cohésion
     * @return La valeur moyenne du couplage interne (entre 0 et 1)
     */
    private double calculateAverageCoupling(HierarchicalClustering.Cluster cluster) {
        Set<String> classes = cluster.getClasses();
        double totalCoupling = 0;
        int count = 0;

        // Calcule la somme des couplages entre toutes les paires de classes
        for (String classA : classes) {
            for (String classB : classes) {
                // Évite de calculer le couplage d'une classe avec elle-même
                if (!classA.equals(classB)) {
                    // Récupère le couplage entre les deux classes
                    Double coupling = couplingMatrix.get(new Pair<>(classA, classB));
                    if (coupling != null) {
                        totalCoupling += coupling;
                        count++;
                    }
                }
            }
        }

        // Retourne la moyenne ou 0 si aucune relation n'existe
        return count > 0 ? totalCoupling / count : 0;
    }
}