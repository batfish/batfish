package org.batfish.dataplane.topology;

import com.google.common.collect.ImmutableList;
import java.util.Collections;

public final class UndirectedIsisEdge extends IsisEdge {

  UndirectedIsisEdge(DirectedIsisEdge edge) {
    super(
        edge.getCircuitType(),
        Collections.min(ImmutableList.of(edge.getNode1(), edge.getNode2())),
        Collections.max(ImmutableList.of(edge.getNode1(), edge.getNode2())));
  }
}
