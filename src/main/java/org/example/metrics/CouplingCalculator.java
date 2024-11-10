package org.example.metrics;

import org.example.graph.CallGraph;

import java.util.*;

/**
 * Classe qui calcule et affiche les métriques de couplage entre les classes
 * d'un projet Java en se basant sur les appels de méthodes.
 */
public class CouplingCalculator {

    /**
     * Calcule une matrice de couplage entre toutes les classes du graphe.
     * Le couplage est déterminé par le nombre de fois qu'une classe appelle des méthodes
     * d'une autre classe.
     *
     * Structure de retour :
     * - Première clé : Classe appelante
     * - Deuxième clé : Classe appelée
     * - Valeur : Nombre d'appels entre ces classes
     *
     * @param graph Le graphe d'appels contenant les relations entre classes et méthodes
     * @return Map imbriquée représentant la matrice de couplage
     */
    public static Map<String, Map<String, Integer>> calculateCoupling(CallGraph graph) {
        // Matrice de couplage : classe_appelante -> (classe_appelée -> nombre_appels)
        Map<String, Map<String, Integer>> couplingMatrix = new HashMap<>();

        // Parcourt chaque classe du graphe comme potentielle classe appelante
        for (String callerClass : graph.getAllClasses()) {
            // Récupère toutes les méthodes appelées par cette classe
            Set<String> calledMethods = graph.getCalledMethods(callerClass);

            // Pour chaque méthode appelée, cherche à quelle classe elle appartient
            for (String calledMethod : calledMethods) {
                // Vérifie chaque classe comme propriétaire potentiel de la méthode
                for (String targetClass : graph.getAllClasses()) {
                    // Vérifie si :
                    // 1. La méthode appartient à la classe cible
                    // 2. La classe appelante est différente de la classe cible (pas d'auto-couplage)
                    if (graph.getClassMethods(targetClass).contains(calledMethod)
                            && !callerClass.equals(targetClass)) {
                        // Met à jour le compteur de couplage :
                        // - Crée la map pour la classe appelante si nécessaire
                        // - Incrémente le compteur d'appels entre les deux classes
                        couplingMatrix
                                .computeIfAbsent(callerClass, k -> new HashMap<>())
                                .merge(targetClass, 1, Integer::sum);
                    }
                }
            }
        }

        return couplingMatrix;
    }

    /**
     * Affiche la matrice de couplage de manière formatée et lisible.
     * Pour chaque classe, montre le nombre d'appels vers les autres classes.
     * @param matrix La matrice de couplage calculée précédemment
     */
    public static void printCouplingMatrix(Map<String, Map<String, Integer>> matrix) {
        System.out.println("\nMatrice de Couplage :");

        // Vérifie si des relations de couplage ont été trouvées
        if (matrix.isEmpty()) {
            System.out.println("Aucune relation de couplage trouvée.");
        } else {
            // Pour chaque classe appelante
            for (String classA : matrix.keySet()) {
                System.out.println("Couplage pour " + classA + ":");
                // Pour chaque classe appelée par cette classe
                for (String classB : matrix.get(classA).keySet()) {
                    // Récupère et affiche le nombre d'appels
                    int value = matrix.get(classA).get(classB);
                    System.out.println("\t" + classB + " -> " + value);
                }
            }
        }
    }
}