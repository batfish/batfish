package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L2Vni} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L2VniHub}.
 */
public final class L2VniToL2VniHub extends Edge {

  public static @Nonnull L2VniToL2VniHub instance() {
    return INSTANCE;
  }

  private static final L2VniToL2VniHub INSTANCE = new L2VniToL2VniHub();

  private L2VniToL2VniHub() {
    super(identity());
  }
}
