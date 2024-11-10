package org.example.visualization;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.example.clustering.HierarchicalClustering.Cluster;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.model.Factory.*;

/**
 * Classe pour générer une représentation visuelle d'un dendrogramme
 * montrant la hiérarchie des clusters de classes.
 */
public class DendrogramVisualizer {

    public static void generateDendrogram(Cluster root, String outputPath) {
        try {
            MutableGraph graph = mutGraph("Dendrogram").setDirected(true);

            // Set graph attributes using named attributes
            graph.graphAttrs().add(Label.of("Dendrogramme"));

            graph.graphAttrs().add("rankdir", "TB");

            // Générer les nœuds et les liens récursivement
            generateClusterNode(root, graph, new HashMap<>());

            // Générer le fichier
            Graphviz.fromGraph(graph)
                    .width(1200)
                    .height(800)
                    .render(Format.PNG)
                    .toFile(new File(outputPath));

            System.out.println("Dendrogramme généré : " + outputPath);

        } catch (IOException e) {
            System.err.println("Erreur lors de la génération du dendrogramme : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static MutableNode generateClusterNode(
            Cluster cluster,
            MutableGraph graph,
            Map<Set<String>, MutableNode> nodeMap) {

        if (cluster == null) return null;

        // Créer ou récupérer le nœud pour ce cluster
        String nodeId = String.join("_", cluster.getClasses());
        String nodeLabel = cluster.getClasses().size() == 1 ?
                cluster.getClasses().iterator().next() :
                String.format("Cluster\n(%.3f)\n%s",
                        cluster.getMergeCoupling(),
                        cluster.getClasses());

        MutableNode node = mutNode(nodeId).add(Label.of(nodeLabel));

        // Ajouter le style selon le type de nœud
        if (cluster.getClasses().size() == 1) {
            node.add(Style.FILLED, Color.rgb(200, 200, 255));
        } else {
            node.add(Style.FILLED, Color.rgb(255, 200, 200));
        }

        graph.add(node);
        nodeMap.put(cluster.getClasses(), node);

        // Générer récursivement les nœuds enfants
        if (cluster.getLeftChild() != null) {
            MutableNode leftNode = generateClusterNode(cluster.getLeftChild(), graph, nodeMap);
            node.addLink(to(leftNode).with(Style.SOLID));
        }

        if (cluster.getRightChild() != null) {
            MutableNode rightNode = generateClusterNode(cluster.getRightChild(), graph, nodeMap);
            node.addLink(to(rightNode).with(Style.SOLID));
        }

        return node;
    }
}