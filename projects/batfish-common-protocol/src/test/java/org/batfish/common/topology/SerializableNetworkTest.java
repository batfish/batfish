package org.batfish.common.topology;

import static org.junit.Assert.assertEquals;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Test of {@link SerializableNetwork}. */
public final class SerializableNetworkTest {

  @Test
  public void testJavaSerialization() {
    MutableNetwork<String, Integer> network = NetworkBuilder.directed().build();
    network.addEdge("a", "b", 5);
    SerializableNetwork<String, Integer> snetwork = new SerializableNetwork<>(network);
    SerializableNetwork<String, Integer> cloned = SerializationUtils.clone(snetwork);

    assertEquals(snetwork, cloned);

    // make sure deserialized version preserves equality with other Network implementations
    assertEquals(cloned, network);
  }
}
