package org.batfish.smt.utils;


import org.batfish.datamodel.Interface;
import org.batfish.smt.Graph;
import org.batfish.smt.GraphEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {


    public static List<String> findMatchingNodes(Graph graph, PathRegexes p) {
        return findMatchingNodes(graph, p.getSrcRegex(), p.getNotSrcRegex());
    }

    public static List<String> findMatchingNodes(Graph graph, Pattern p1, Pattern p2) {
        List<String> acc = new ArrayList<>();
        graph.getConfigurations().forEach((router, conf) -> {
            Matcher m1 = p1.matcher(router);
            Matcher m2 = p2.matcher(router);
            if (m1.matches() && !m2.matches()) {
                acc.add(router);
            }
        });
        return acc;
    }


    public static List<GraphEdge> findMatchingEdges(Graph graph, PathRegexes p) {
        return findMatchingEdges(graph, p.getDstRegex(), p.getNotDstRegex(), p.getIfaceRegex(), p.getNotIfaceRegex());
    }

    public static List<GraphEdge> findMatchingEdges(Graph graph, Pattern p1, Pattern p2, Pattern p3, Pattern p4) {
        List<GraphEdge> acc = new ArrayList<>();
        graph.getEdgeMap().forEach((router, edges) -> {
            Matcher m1 = p1.matcher(router);
            Matcher m2 = p2.matcher(router);
            if (m1.matches() && !m2.matches()) {
                for (GraphEdge edge : edges) {
                    Interface i = edge.getStart();
                    String ifaceName = i.getName();
                    Matcher m3 = p3.matcher(ifaceName);
                    Matcher m4 = p4.matcher(ifaceName);
                    if (m3.matches() && !m4.matches()) {
                        acc.add(edge);
                    }
                }
            }
        });
        return acc;
    }

}
