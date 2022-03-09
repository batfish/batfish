package org.batfish.common.topology.bridge_domain.node;

import javax.annotation.Nonnull;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Represents any interface with an IP address.
 *
 * <p>SVI/IRB/Vlan/BVI interfaces connect to the {@link BridgeDomain} they are associated with.
 *
 * <p>Ethernet (sub)interfaces connect to the {@link PhysicalInterface} they are bound to. Even if -
 * the configuration has an IP address directly assigned to Ethernet1, say, there will be both an
 * {@link L3Interface} and {@link PhysicalInterface} for Ethernet1.
 */
public interface L3Interface extends Node {

  @Nonnull
  NodeInterfacePair getInterface();
}
