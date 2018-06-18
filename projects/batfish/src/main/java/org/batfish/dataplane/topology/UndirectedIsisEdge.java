package org.batfish.dataplane.topology;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IsisLevel;

public final class UndirectedIsisEdge extends IsisEdge {

  UndirectedIsisEdge(DirectedIsisEdge edge) {
    this(edge.getCircuitType(), edge.getNode1(), edge.getNode2());
  }

  public UndirectedIsisEdge(
      @Nonnull IsisLevel circuitType, @Nonnull IsisNode node1, @Nonnull IsisNode node2) {
    super(
        circuitType,
        Collections.min(ImmutableList.of(node1, node2)),
        Collections.min(ImmutableList.of(node1, node2)));
  }
}
