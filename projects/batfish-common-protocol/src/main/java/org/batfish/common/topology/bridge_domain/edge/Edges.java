package org.batfish.common.topology.bridge_domain.edge;

import org.batfish.common.topology.bridge_domain.node.L1Hub;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L2VniHub;

/**
 * Utility class containing helper methods for generating all the directed edges between a set of
 * inter-related {@link org.batfish.common.topology.bridge_domain.node.Node}s.
 */
public final class Edges {

  /** Generate edges connecting an {@link L1Hub} to every one of a list of {@link L1Interface}s. */
  public static void connectToHub(L1Hub hub, L1Interface... phys) {
    for (L1Interface p : phys) {
      hub.addAttachedInterface(p);
      p.connectToHub(hub);
    }
  }

  /** Generate edges connecting an {@link L2VniHub} to every one of a list of {@link L2Vni}s. */
  public static void connectToL2VniHub(L2VniHub l2VniHub, L2Vni... l2Vnis) {
    for (L2Vni l2Vni : l2Vnis) {
      l2VniHub.attachL2Vni(l2Vni);
      l2Vni.connectToL2VniHub(l2VniHub);
    }
  }

  private Edges() {} // prevent instantiation
}
