package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.EthernetHub} to a {@link
 * org.batfish.common.topology.bridge_domain.node.PhysicalInterface}.
 */
public final class EthernetHubToPhysical extends Edge {

  public static @Nonnull EthernetHubToPhysical instance() {
    return INSTANCE;
  }

  private static final EthernetHubToPhysical INSTANCE = new EthernetHubToPhysical();

  private EthernetHubToPhysical() {
    super(identity());
  }
}
