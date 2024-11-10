package org.example;

import org.example.graph.CallGraph;
import org.example.parser.MethodCallCollector;
import org.example.metrics.CouplingMetrics;
import org.example.clustering.HierarchicalClustering;
import org.example.visualization.CouplingGraphVisualizer;
import org.example.visualization.DendrogramVisualizer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class MainCLI {
    protected static File directory;
    protected static CallGraph callGraph = new CallGraph();
    protected static Scanner scanner = new Scanner(System.in);
    protected static String projectDir;
    protected static double minCouplingThreshold;

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Analyseur de Projet Java ===");
            System.out.println("1. Analyser un projet");
            System.out.println("2. Afficher le graphe d'appels");
            System.out.println("3. Générer les visualisations");
            System.out.println("4. Exécuter l'analyse Spoon");
            System.out.println("5. Quitter");
            System.out.print("\nVotre choix : ");

            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    analyzeProject();
                    break;
                case 2:
                    displayCallGraph();
                    break;
                case 3:
                    generateVisualizations();
                    break;
                case 4:
                    runSpoonAnalysis();
                    break;
                case 5:
                    System.out.println("Au revoir !");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez réessayer.");
            }
        }
    }

    private static int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void analyzeProject() {
        System.out.print("Chemin du projet à analyser : ");
        projectDir = scanner.nextLine();

        System.out.print("Seuil de couplage (entre 0 et 1) : ");
        minCouplingThreshold = Double.parseDouble(scanner.nextLine());

        directory = new File(projectDir);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Erreur : Chemin invalide");
            return;
        }

        List<File> javaFiles = listJavaFiles(directory);
        System.out.println("\nAnalyse des fichiers Java...");

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
        System.out.println("Analyse terminée !");
    }

    private static void displayCallGraph() {
        if (!isProjectConfigured()) return;
        System.out.println("\nGraphe d'appels :");
        callGraph.printGraph();
    }

    private static void generateVisualizations() {
        if (!isProjectConfigured()) return;

        System.out.println("\nGénération des visualisations...");
        CouplingMetrics metrics = new CouplingMetrics(callGraph);
        metrics.calculateNormalizedCoupling();

        CouplingGraphVisualizer.generateCouplingGraph(
                metrics.getNormalizedCoupling(),
                "coupling_graph.png"
        );

        HierarchicalClustering clustering = new HierarchicalClustering(metrics.getNormalizedCoupling());
        int maxModules = Math.max(callGraph.getAllClasses().size() / 2, 1);
        List<HierarchicalClustering.Cluster> clusters =
                clustering.performClustering(minCouplingThreshold, maxModules);

        DendrogramVisualizer.generateDendrogram(clusters.get(0), "dendrogram.png");

        // Analyse et affichage des clusters
        System.out.println("\nAnalyse des clusters :");
        clustering.analyzeClusters();

        // Identifier et afficher les modules selon le seuil
        List<Set<String>> modules = clustering.getModulesAtThreshold(minCouplingThreshold);
        System.out.println("\nModules identifiés (seuil = " + minCouplingThreshold + ") :");
        for (int i = 0; i < modules.size(); i++) {
            Set<String> module = modules.get(i);
            System.out.println("\nModule " + (i + 1) + ":");
            for (String className : module) {
                System.out.println("  - " + className);
            }
        }

        // Afficher le dendrogramme textuel
        System.out.println("\nDendrogramme textuel :");
        clustering.printDendrogram();

        // Afficher l'historique détaillé du clustering
        System.out.println("\nHistorique détaillé du clustering :");
        clustering.printDetailedDendrogram();

        System.out.println("\nFichiers générés :");
        System.out.println("- coupling_graph.png");
        //System.out.println("- dendrogram.png");
    }

    private static void runSpoonAnalysis() {
        if (!isProjectConfigured()) return;

        System.out.println("\nExécution de l'analyse Spoon...");
        SpoonAnalyzer spoonAnalyzer = new SpoonAnalyzer();
        CallGraph spoonCallGraph = spoonAnalyzer.analyzeProject(projectDir);

        CouplingMetrics spoonMetrics = new CouplingMetrics(spoonCallGraph);
        spoonMetrics.calculateNormalizedCoupling();

        CouplingGraphVisualizer.generateCouplingGraph(
                spoonMetrics.getNormalizedCoupling(),
                "spoon_coupling_graph.png"
        );

        System.out.println("Analyse Spoon terminée !");
    }

    private static boolean isProjectConfigured() {
        if (directory == null || !directory.exists()) {
            System.out.println("Erreur : Veuillez d'abord analyser un projet (option 1)");
            return false;
        }
        return true;
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