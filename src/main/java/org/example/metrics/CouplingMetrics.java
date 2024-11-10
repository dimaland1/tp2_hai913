package org.example.metrics;

import org.example.graph.CallGraph;
import org.example.graph.Pair;
import java.util.*;

/**
 * Classe qui calcule des métriques de couplage normalisées entre les classes d'un projet.
 * La normalisation permet de comparer le couplage relatif entre différentes paires de classes
 * en ramenant les valeurs entre 0 et 1.
 */
public class CouplingMetrics {
    // Le graphe d'appels contenant les relations entre classes et méthodes
    private final CallGraph callGraph;

    // Map stockant le couplage normalisé entre chaque paire de classes
    // Clé : Paire de noms de classes (classe appelante, classe appelée)
    // Valeur : Couplage normalisé entre 0 et 1
    private Map<Pair<String, String>, Double> normalizedCoupling;

    /**
     * Constructeur initialisant le calculateur de métriques.
     *
     * @param callGraph Le graphe d'appels à analyser
     */
    public CouplingMetrics(CallGraph callGraph) {
        this.callGraph = callGraph;
        this.normalizedCoupling = new HashMap<>();
    }

    /**
     * Calcule le couplage normalisé entre toutes les paires de classes.
     * La normalisation est effectuée en divisant chaque nombre d'appels
     * par le nombre total de relations dans le système.
     */
    public void calculateNormalizedCoupling() {
        // Calcule d'abord le nombre total de relations dans le système
        int totalRelations = calculateTotalRelations();

        // Pour chaque paire de classes différentes
        for (String classA : callGraph.getAllClasses()) {
            for (String classB : callGraph.getAllClasses()) {
                if (!classA.equals(classB)) {  // Évite le couplage d'une classe avec elle-même
                    // Compte le nombre de relations entre ces deux classes
                    int relationCount = countRelationsBetweenClasses(classA, classB);

                    // Calcule la valeur normalisée (évite la division par zéro)
                    double normalizedValue = totalRelations > 0 ?
                            (double) relationCount / totalRelations : 0;

                    // Stocke le résultat
                    normalizedCoupling.put(new Pair<>(classA, classB), normalizedValue);
                }
            }
        }
    }

    /**
     * Calcule le nombre total de relations entre toutes les classes du système.
     * Cette valeur sert de dénominateur pour la normalisation.
     *
     * @return Le nombre total de relations d'appels entre classes différentes
     */
    private int calculateTotalRelations() {
        int total = 0;
        // Pour chaque paire de classes différentes
        for (String classA : callGraph.getAllClasses()) {
            for (String classB : callGraph.getAllClasses()) {
                if (!classA.equals(classB)) {  // Évite le compte des relations internes
                    total += countRelationsBetweenClasses(classA, classB);
                }
            }
        }
        return total;
    }

    /**
     * Compte le nombre de relations d'appels entre deux classes spécifiques.
     * Une relation existe quand une méthode appelée par la classe A
     * est définie dans la classe B.
     *
     * @param classA La classe appelante
     * @param classB La classe potentiellement appelée
     * @return Le nombre de méthodes de B appelées par A
     */
    private int countRelationsBetweenClasses(String classA, String classB) {
        // Récupère toutes les méthodes appelées par la classe A
        Set<String> methodsCalledByA = callGraph.getCalledMethods(classA);
        // Récupère toutes les méthodes définies dans la classe B
        Set<String> methodsInB = callGraph.getClassMethods(classB);

        // Compte le nombre de méthodes qui sont à la fois appelées par A et définies dans B
        return (int) methodsCalledByA.stream()
                .filter(methodsInB::contains)
                .count();
    }

    /**
     * Retourne la map contenant tous les couplages normalisés calculés.
     *
     * @return Map des couplages normalisés entre paires de classes
     */
    public Map<Pair<String, String>, Double> getNormalizedCoupling() {
        return normalizedCoupling;
    }
}