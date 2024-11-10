package org.example;

import org.example.graph.CallGraph;
import org.example.parser.MethodCallCollector;
import org.example.metrics.CouplingMetrics;
import org.example.clustering.HierarchicalClustering;
import org.example.graph.Pair;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.example.visualization.CouplingGraphVisualizer;
import org.example.visualization.DendrogramVisualizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {
    protected static File directory;
    // Initialisation du graphe des appels
    protected static CallGraph callGraph = new CallGraph();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Demande à l'utilisateur de fournir le chemin du répertoire du projet
        System.out.print("Veuillez entrer le chemin vers le répertoire du projet Java à analyser : ");
        String projectDir = scanner.nextLine();

        System.out.print("Entrez le seuil minimal de couplage (entre 0 et 1) : ");
        double minCouplingThreshold = scanner.nextDouble();


        directory = new File(projectDir);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Le chemin fourni n'est pas un répertoire valide.");
            return;
        }

        // Analyser chaque fichier .java dans le répertoire
        List<File> javaFiles = listJavaFiles(directory);
        System.out.println("\n====================Analyse des fichiers Java...============================");
        for (File file : javaFiles) {
            try {
                System.out.println("Analyse de : " + file.getName());
                FileInputStream in = new FileInputStream(file);
                CompilationUnit cu = StaticJavaParser.parse(in);
                in.close();

                cu.accept(new MethodCallCollector(callGraph), null);
            } catch (IOException e) {
                System.err.println("Erreur lors de l'analyse de " + file.getName() + ": " + e.getMessage());
            }
        }

        // Calculer les métriques de couplage
        CouplingMetrics metrics = new CouplingMetrics(callGraph);
        metrics.calculateNormalizedCoupling();

        // Afficher le graphe d'appels initial
        System.out.println("\n=======================Graphe d'appels initial :===============================");
        callGraph.printGraph();

        // Générer et sauvegarder le graphe de couplage
        System.out.println("\n========================Génération du graphe de couplage...========================");
        String couplingGraphPath = "coupling_graph.png";
        CouplingGraphVisualizer.generateCouplingGraph(metrics.getNormalizedCoupling(), couplingGraphPath);


        // Paramètres pour le clustering
        int maxModules = Math.max(callGraph.getAllClasses().size() / 2, 1);
        System.out.println("\n======================Paramètres de clustering :===================================");
        System.out.println("- Nombre maximum de modules : " + maxModules);
        System.out.println("- Seuil de couplage minimum : " + minCouplingThreshold);

        // Effectuer le clustering hiérarchique
        System.out.println("\n===============================Exécution du clustering hiérarchique...===========================");
        HierarchicalClustering clustering = new HierarchicalClustering(metrics.getNormalizedCoupling());
        List<HierarchicalClustering.Cluster> clusters = clustering.performClustering(minCouplingThreshold, maxModules);

        // Afficher le dendrogramme textuel
        clustering.printDendrogram();

        // Afficher l'historique détaillé du clustering
        clustering.printDetailedDendrogram();

        // Générer le dendrogramme visuel
        //System.out.println("\n===============================Génération du dendrogramme visuel...============================");
        //DendrogramVisualizer.generateDendrogram(clusters.get(0), "dendrogram.png");

        // Analyser les clusters résultants
        System.out.println("\n====================================Analyse des clusters résultants :==================================");
        clustering.analyzeClusters();

        // Identifier les modules selon le seuil de couplage
        List<Set<String>> modules = clustering.getModulesAtThreshold(minCouplingThreshold);
        System.out.println("\nModules identifiés (seuil = " + minCouplingThreshold + ") :");
        for (int i = 0; i < modules.size(); i++) {
            Set<String> module = modules.get(i);
            System.out.println("\nModule " + (i + 1) + ":");
            for (String className : module) {
                System.out.println("  - " + className);
            }
        }

        //SPOON
        System.out.println("\n======================================Analyse avec Spoon :========================================");
        SpoonAnalyzer spoonAnalyzer = new SpoonAnalyzer();
        CallGraph spoonCallGraph = spoonAnalyzer.analyzeProject(projectDir);

        // Calculer les métriques pour l'analyse Spoon
        CouplingMetrics spoonMetrics = new CouplingMetrics(spoonCallGraph);
        spoonMetrics.calculateNormalizedCoupling();

        // Clustering pour l'analyse Spoon
        HierarchicalClustering spoonClustering = new HierarchicalClustering(spoonMetrics.getNormalizedCoupling());
        List<HierarchicalClustering.Cluster> spoonModules = spoonClustering.performClustering(minCouplingThreshold, maxModules);

        System.out.println("\nRésultats de l'analyse Spoon :");
        spoonCallGraph.printGraph();
        System.out.println("\nModules identifiés par Spoon :");
        for (int i = 0; i < spoonModules.size(); i++) {
            System.out.println("Module " + (i + 1) + ": " + spoonModules.get(i).getClasses());
        }

        // Générer et sauvegarder le graphe de couplage
        System.out.println("\nGénération du graphe de couplage...");
        String couplingGraphSpoonPath = "spoon_coupling_graph.png";
        CouplingGraphVisualizer.generateCouplingGraph(spoonMetrics.getNormalizedCoupling(), couplingGraphSpoonPath);


        // Statistiques finales
        System.out.println("\n=============================Statistiques finales :============================================");
        System.out.println("- Nombre de classes analysées : " + callGraph.getAllClasses().size());
        System.out.println("- Nombre de modules identifiés : " + modules.size());
        System.out.println("- Fichiers générés :");
        System.out.println("  * Graphe de couplage : " + couplingGraphPath);
        System.out.println("  * Graphe de couplage spoon: " + couplingGraphSpoonPath);
        System.out.println("  * Dendrogramme : dendrogram.png");

        scanner.close();
    }

    private static List<File> listJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        if (directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    javaFiles.addAll(listJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }
}