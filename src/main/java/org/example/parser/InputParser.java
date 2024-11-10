package org.example.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.example.graph.CallGraph;

/**
 * Parseur qui analyse le code source Java pour construire un graphe d'appels.
 * Utilisé pour parcourir l'AST (Abstract Syntax Tree) généré par JavaParser.
 * Hérite de VoidVisitorAdapter pour n'implémenter que les méthodes de visite nécessaires.
 */
public class InputParser extends VoidVisitorAdapter<Void> {
    // Graphe des appels à remplir pendant l'analyse
    private final CallGraph callGraph;

    /**
     * Constructeur du parseur.
     * @param callGraph Le graphe des appels à remplir pendant l'analyse
     */
    public InputParser(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    /**
     * Visite une déclaration de classe ou d'interface.
     * Cette méthode est appelée automatiquement par JavaParser pour chaque classe/interface trouvée.
     *
     * @param classOrInterface La déclaration de classe/interface à visiter
     * @param arg Argument non utilisé (requis par le pattern Visitor)
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterface, Void arg) {
        // Récupère le nom de la classe/interface
        String className = classOrInterface.getNameAsString();

        // Enregistre la classe dans le graphe des appels
        callGraph.addClass(className);

        // Continue la visite des éléments enfants (méthodes, champs, etc.)
        super.visit(classOrInterface, arg);
    }

    /**
     * Visite une déclaration de méthode.
     * Cette méthode est appelée automatiquement par JavaParser pour chaque méthode trouvée.
     * Elle analyse :
     * 1. La classe contenante
     * 2. Le nom de la méthode
     * 3. Tous les appels de méthodes contenus dans cette méthode
     *
     * @param method La déclaration de méthode à visiter
     * @param arg Argument non utilisé (requis par le pattern Visitor)
     */
    @Override
    public void visit(MethodDeclaration method, Void arg) {
        // Recherche la classe contenante en remontant l'arbre AST
        String callerClass = method.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("UnknownClass"); // Valeur par défaut si la classe n'est pas trouvée

        // Récupère le nom de la méthode
        String methodName = method.getNameAsString();

        // Enregistre la méthode comme appartenant à sa classe
        callGraph.addMethod(callerClass, methodName);

        // Recherche et analyse tous les appels de méthodes dans le corps de la méthode
        method.findAll(MethodCallExpr.class).forEach(methodCall -> {
            // Pour chaque appel trouvé, enregistre la relation dans le graphe
            String calledMethodName = methodCall.getNameAsString();
            callGraph.addMethodCall(callerClass, calledMethodName);
        });

        // Continue la visite des éléments enfants
        super.visit(method, arg);
    }
}