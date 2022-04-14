package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/**
 * An edge from an {@link org.batfish.common.topology.bridge_domain.node.L1Hub} to an {@link
 * org.batfish.common.topology.bridge_domain.node.L1Interface}.
 */
public final class L1HubToL1 extends Edge {

  public static @Nonnull L1HubToL1 instance() {
    return INSTANCE;
  }

  private static final L1HubToL1 INSTANCE = new L1HubToL1();

  private L1HubToL1() {
    super(identity());
  }
}
