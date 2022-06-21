package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2VniHub} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2Vni}.
 */
public final class L2VniHubToL2Vni extends Edge {

  public static @Nonnull L2VniHubToL2Vni instance() {
    return INSTANCE;
  }

  private static final L2VniHubToL2Vni INSTANCE = new L2VniHubToL2Vni();

  private L2VniHubToL2Vni() {
    super(identity());
  }
}
