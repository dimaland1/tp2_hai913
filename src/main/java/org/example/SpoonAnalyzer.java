package org.example;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.code.CtInvocation;
import org.example.graph.CallGraph;
import java.util.List;

/**
 * Classe d'analyse statique de code Java utilisant Spoon pour extraire
 * les relations entre classes et méthodes.
 * Construit un graphe d'appels représentant la structure du projet.
 */
public class SpoonAnalyzer {
    private final CallGraph callGraph;

    public SpoonAnalyzer() {
        this.callGraph = new CallGraph();
    }

    public CallGraph analyzeProject(String projectPath) {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setComplianceLevel(9);
        launcher.addInputResource(projectPath);
        launcher.buildModel();

        // Analyser les classes
        List<CtClass<?>> classes = launcher.getModel().getElements(new TypeFilter<>(CtClass.class));
        for (CtClass<?> clazz : classes) {
            String className = clazz.getSimpleName();
            callGraph.addClass(className);

            // Analyser les méthodes
            for (CtMethod<?> method : clazz.getMethods()) {
                String methodName = method.getSimpleName();
                callGraph.addMethod(className, methodName);

                // Analyser les appels de méthodes
                for (CtInvocation<?> invocation : method.getElements(new TypeFilter<>(CtInvocation.class))) {
                    if (invocation.getExecutable().getDeclaration() != null) {
                        String calledMethodName = invocation.getExecutable().getSimpleName();
                        callGraph.addMethodCall(className, calledMethodName);
                    }
                }
            }
        }

        return callGraph;
    }
}