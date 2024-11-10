package org.example.visualization;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.example.graph.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import static guru.nidi.graphviz.model.Factory.*;

/**
 * Classe pour générer une représentation visuelle du graphe de couplage
 * en utilisant la bibliothèque Graphviz.
 * Crée une image PNG montrant les classes comme nœuds et les couplages comme arêtes pondérées.
 */
public class CouplingGraphVisualizer {

    public static void generateCouplingGraph(Map<Pair<String, String>, Double> couplingMatrix, String outputPath) {
        try {
            MutableGraph graph = mutGraph("Coupling Graph").setDirected(true);
            graph.graphAttrs().add(Label.of("Graphe de couplage pondéré"));

            // Collecter toutes les classes uniques
            Set<String> classes = new HashSet<>();
            couplingMatrix.keySet().forEach(pair -> {
                classes.add(pair.getFirst());
                classes.add(pair.getSecond());
            });

            // Créer les nœuds
            Map<String, MutableNode> nodes = new java.util.HashMap<>();
            for (String className : classes) {
                MutableNode node = mutNode(className);
                node.add(Style.FILLED);
                node.add(Color.rgb(200, 200, 255));
                nodes.put(className, node);
                graph.add(node);
            }

            // Ajouter les arêtes avec les poids
            for (Map.Entry<Pair<String, String>, Double> entry : couplingMatrix.entrySet()) {
                if (entry.getValue() > 0) {  // Ne montrer que les couplages non nuls
                    String from = entry.getKey().getFirst();
                    String to = entry.getKey().getSecond();
                    double weight = entry.getValue();

                    // Calculer l'épaisseur de la ligne en fonction du poids
                    double penWidth = 1 + (weight * 5);

                    // Calculer la couleur en fonction du poids (bleu plus foncé pour un couplage plus fort)
                    int blue = 255 - (int)(weight * 200);

                    MutableNode fromNode = nodes.get(from);
                    MutableNode toNode = nodes.get(to);

                    fromNode.addLink(
                            to(toNode)
                                    .with(
                                            Label.of(String.format("%.3f", weight)),
                                            Color.rgb(0, 0, blue),
                                            Style.lineWidth(penWidth)
                                    )
                    );
                }
            }

            // Générer le fichier
            Graphviz.fromGraph(graph)
                    .width(1000)
                    .render(Format.PNG)
                    .toFile(new File(outputPath));

            System.out.println("Graphe de couplage généré : " + outputPath);

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du graphe : " + e.getMessage());
            e.printStackTrace();
        }
    }
}