package org.batfish.question.neighbors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.RoleEdge;
import org.batfish.datamodel.VerboseEdge;
import org.batfish.question.NeighborsQuestionPlugin.NeighborsAnswerElement;
import org.junit.Test;

/** Tests for {@link NeighborsAnswerElement} */
public class NeighborsAnswerElementTest {

  @Test
  public void testPrettyPrint() {
    NeighborsAnswerElement neighborsAnswerElement = new NeighborsAnswerElement();

    Edge testEdge1 = new Edge("node11", "interface11", "node12", "interface12");
    Edge testEdge2 = new Edge("node21", "interface21", "node22", "interface22");

    RoleEdge testRoleEdge1 = new RoleEdge("role11", "role12");

    VerboseEdge verboseEdge1 =
        new VerboseEdge(
            Interface.builder().setName("interface11").build(),
            Interface.builder().setName("interface12").build(),
            testEdge1);

    SortedSet<Edge> edges = new TreeSet<>();
    edges.add(testEdge1);

    SortedSet<RoleEdge> roleEdges = new TreeSet<>();
    roleEdges.add(testRoleEdge1);

    SortedSet<VerboseEdge> verboseEdges = new TreeSet<>();
    verboseEdges.add(verboseEdge1);

    neighborsAnswerElement.setLayer3Neighbors(edges);
    neighborsAnswerElement.addLayer3Edge(testEdge2);

    neighborsAnswerElement.setRoleLayer3Neighbors(roleEdges);

    neighborsAnswerElement.setVerboseLayer3Neighbors(verboseEdges);

    assertThat(
        neighborsAnswerElement.prettyPrint(),
        equalTo(
            "Results for neighbors\n"
                + "  Layer 3 neighbors\n"
                + "    <node11:interface11, node12:interface12>\n"
                + "    <node21:interface21, node22:interface22>\n"
                + "  Layer 3 neighbors\n"
                + "    <node11:interface11, node12:interface12>\n"
                + "  Layer 3 neighbors\n"
                + "    <role11 --> role12>\n"));
  }
}
