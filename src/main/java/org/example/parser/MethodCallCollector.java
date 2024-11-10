package org.example.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.example.graph.CallGraph;

/**
 * Collecteur qui analyse le code source Java pour identifier et enregistrer
 * tous les appels de méthodes entre les classes.
 */
public class MethodCallCollector extends VoidVisitorAdapter<Void> {
    // Le graphe qui stockera toutes les relations d'appels collectées
    private final CallGraph callGraph;

    /**
     * Constructeur du collecteur.
     *
     * @param callGraph Le graphe des appels à remplir pendant la collecte
     */
    public MethodCallCollector(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    /**
     * Visite une déclaration de classe ou d'interface.
     * Enregistre la classe dans le graphe et affiche un message de logging.
     *
     * @param classOrInterface La déclaration de classe/interface à visiter
     * @param arg Argument non utilisé (requis par le pattern Visitor)
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterface, Void arg) {
        // Extrait le nom de la classe
        String className = classOrInterface.getNameAsString();

        // Enregistre la classe dans le graphe
        callGraph.addClass(className);

        // Log pour le debugging
        System.out.println("Classe détectée : " + className);

        // Continue la visite des éléments enfants
        super.visit(classOrInterface, arg);
    }

    /**
     * Visite une déclaration de méthode.
     * Pour chaque méthode :
     * 1. Identifie sa classe contenante
     * 2. Enregistre la méthode dans le graphe
     * 3. Collecte et enregistre tous les appels de méthodes qu'elle contient
     * @param method La déclaration de méthode à visiter
     * @param arg Argument non utilisé (requis par le pattern Visitor)
     */
    @Override
    public void visit(MethodDeclaration method, Void arg) {
        // Trouve la classe qui contient cette méthode
        String callerClass = method.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("UnknownClass");

        // Récupère le nom de la méthode
        String methodName = method.getNameAsString();

        // Enregistre la méthode dans sa classe
        callGraph.addMethod(callerClass, methodName);

        // Log commenté pour la définition de méthode
        //System.out.println("Méthode définie : " + callerClass + "." + methodName);

        // Analyse tous les appels de méthodes dans le corps de la méthode
        method.findAll(MethodCallExpr.class).forEach(methodCall -> {
            // Pour chaque appel, enregistre la relation dans le graphe
            String calledMethodName = methodCall.getNameAsString();
            callGraph.addMethodCall(callerClass, calledMethodName);

            // Log commenté pour les appels de méthodes
            //System.out.println("Méthode appelée par " + callerClass + ": " + calledMethodName);
        });

        // Continue la visite des éléments enfants
        super.visit(method, arg);
    }
}