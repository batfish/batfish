package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;

/**
 * An edge from a {@link org.batfish.common.topology.bridge_domain.node.PhysicalInterface} to an
 * {@link org.batfish.common.topology.bridge_domain.node.EthernetHub}.
 */
public final class PhysicalToEthernetHub extends Edge {

  public static @Nonnull PhysicalToEthernetHub instance() {
    return INSTANCE;
  }

  private static final PhysicalToEthernetHub INSTANCE = new PhysicalToEthernetHub();

  private PhysicalToEthernetHub() {
    super(identity());
  }
}
