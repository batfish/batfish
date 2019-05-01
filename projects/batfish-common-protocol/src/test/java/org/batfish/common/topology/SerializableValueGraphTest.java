package org.batfish.common.topology;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link SerializableValueGraph}. */
public final class SerializableValueGraphTest {

  @Test
  public void testJavaSerialization() {
    MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed().build();
    graph.addNode("a");
    graph.addNode("b");
    graph.putEdgeValue("a", "b", 5);
    SerializableValueGraph<String, Integer> sgraph = new SerializableValueGraph<>(graph);

    assertEquals(sgraph, SerializationUtils.clone(sgraph));
  }
}
