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
    graph.putEdge("a", "b");
    SerializableGraph<String> sgraph = new SerializableGraph<>(graph);
    SerializableGraph<String> cloned = SerializationUtils.clone(sgraph);

    assertEquals(sgraph, cloned);

    // make sure deserialized version preserves equality with other Graph implementations
    assertEquals(cloned, graph);
  }
}
