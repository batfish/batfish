package org.batfish.common.matchers;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.matchers.Layer2TopologyMatchersImpl.InSameBroadcastDomain;
import org.batfish.common.topology.Layer2Node;
import org.batfish.common.topology.Layer2Topology;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link org.batfish.common.topology.Layer2Topology}. */
@ParametersAreNonnullByDefault
public class Layer2TopologyMatchers {
  /** Matches a {@link Layer2Topology} with the given L2 nodes in the same broadcast domain. */
  public static Matcher<Layer2Topology> inSameBroadcastDomain(Layer2Node node1, Layer2Node node2) {
    return new InSameBroadcastDomain(node1, node2);
  }

  private Layer2TopologyMatchers() {}
}
