package org.example.graph;

import java.util.*;

/**
 * Cette classe représente un graphe d'appels entre classes et méthodes Java.
 * Elle permet de suivre quelles méthodes sont définies dans chaque classe
 * et quelles méthodes sont appelées par chaque classe.
 */
public class CallGraph {
    // Stocke les méthodes définies dans chaque classe (classe -> ensemble de ses méthodes)
    private Map<String, Set<String>> classMethods = new HashMap<>();

    // Stocke les méthodes appelées par chaque classe (classe appelante -> ensemble des méthodes appelées)
    private Map<String, Set<String>> methodCallsByClass = new HashMap<>();

    /**
     * Ajoute une nouvelle classe au graphe.
     * Initialise les ensembles vides pour les méthodes définies et appelées.
     * @param className Le nom de la classe à ajouter
     */
    public void addClass(String className) {
        classMethods.putIfAbsent(className, new HashSet<>());
        methodCallsByClass.putIfAbsent(className, new HashSet<>());
    }

    /**
     * Ajoute une méthode à une classe spécifique.
     * Si la classe n'existe pas, crée automatiquement une nouvelle entrée.
     * @param className Le nom de la classe
     * @param methodName Le nom de la méthode à ajouter
     */
    public void addMethod(String className, String methodName) {
        classMethods.computeIfAbsent(className, k -> new HashSet<>()).add(methodName);
    }

    /**
     * Enregistre un appel de méthode depuis une classe.
     * Si la classe appelante n'existe pas, crée automatiquement une nouvelle entrée.
     * @param callerClass La classe qui fait l'appel
     * @param calledMethod La méthode qui est appelée
     */
    public void addMethodCall(String callerClass, String calledMethod) {
        methodCallsByClass.computeIfAbsent(callerClass, k -> new HashSet<>()).add(calledMethod);
    }

    /**
     * Retourne l'ensemble de toutes les classes enregistrées dans le graphe.
     * @return Set<String> contenant les noms de toutes les classes
     */
    public Set<String> getAllClasses() {
        return classMethods.keySet();
    }

    /**
     * Récupère l'ensemble des méthodes appelées par une classe spécifique.
     * @param callerClass La classe dont on veut connaître les appels
     * @return Set<String> des méthodes appelées, ou ensemble vide si la classe n'existe pas
     */
    public Set<String> getCalledMethods(String callerClass) {
        return methodCallsByClass.getOrDefault(callerClass, Collections.emptySet());
    }

    /**
     * Récupère l'ensemble des méthodes définies dans une classe spécifique.
     * @param className La classe dont on veut connaître les méthodes
     * @return Set<String> des méthodes définies, ou ensemble vide si la classe n'existe pas
     */
    public Set<String> getClassMethods(String className) {
        return classMethods.getOrDefault(className, Collections.emptySet());
    }

    /**
     * Affiche une représentation textuelle complète du graphe d'appels.
     * Montre :
     * 1. Les relations de couplage (quelles classes appellent quelles méthodes)
     * 2. Les méthodes définies dans chaque classe
     */
    public void printGraph() {
        // Affichage des relations de couplage
        System.out.println("\nGraphe des appels :");
        if (methodCallsByClass.isEmpty()) {
            System.out.println("Aucune relation de couplage trouvée dans le graphe.");
        } else {
            for (String caller : methodCallsByClass.keySet()) {
                System.out.println(caller + " appelle : " + methodCallsByClass.get(caller));
            }
        }

        // Affichage des méthodes par classe
        System.out.println("\nMéthodes définies dans chaque classe :");
        for (String className : classMethods.keySet()) {
            System.out.println(className + " : " + classMethods.get(className));
        }
    }
}