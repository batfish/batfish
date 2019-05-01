package org.batfish.common.topology;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link SerializableGraph}. */
public final class SerializableGraphTest {

  @Test
  public void testJavaSerialization() {
    MutableGraph<String> graph = GraphBuilder.directed().build();
    graph.addNode("a");
    graph.addNode("b");
    graph.putEdge("a", "b");
    SerializableGraph<String> sgraph = new SerializableGraph<>(graph);

    assertEquals(sgraph, SerializationUtils.clone(sgraph));
  }
}
