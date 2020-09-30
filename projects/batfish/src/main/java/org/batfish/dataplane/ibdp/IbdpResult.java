package org.batfish.dataplane.ibdp;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.plugin.DataPlanePlugin.ComputeDataPlaneResult;
import org.batfish.common.topology.TopologyContainer;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.answers.DataPlaneAnswerElement;

/**
 * A specific type of {@link ComputeDataPlaneResult} returned by {@link IncrementalBdpEngine} which
 * includes a map of all {@link Node}s.
 *
 * <p>To be used in tests.
 */
@ParametersAreNonnullByDefault
final class IbdpResult extends ComputeDataPlaneResult {

  @Nonnull private final Map<String, Node> _nodes;

  IbdpResult(
      DataPlaneAnswerElement answerElement,
      DataPlane dataPlane,
      TopologyContainer topologies,
      Map<String, Node> nodes) {
    super(answerElement, dataPlane, topologies);
    _nodes = nodes;
  }

  @Nonnull
  Map<String, Node> getNodes() {
    return _nodes;
  }
}
