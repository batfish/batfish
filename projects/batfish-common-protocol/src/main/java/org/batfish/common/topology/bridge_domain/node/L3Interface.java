package org.batfish.common.topology.bridge_domain.node;

import javax.annotation.Nonnull;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Represents any interface with an IP address.
 *
 * <ul>
 *   <li>{@link L3BridgedInterface}: SVI/IRB/Vlan/BVI interfaces connect to the {@link
 *       NonVlanAwareBridgeDomain} they are associated with.
 *   <li>{@link L3NonBridgedInterface}: Ethernet (sub)interfaces connect to the {@link L1Interface}
 *       they are bound to. Even if - the configuration has an IP address directly assigned to
 *       Ethernet1, say, there will be both an {@link L3Interface} and {@link L1Interface} for
 *       Ethernet1.
 *   <li>{@link L3DisconnectedInterface}: L3 Tunnel interfaces for which adjacencies are computed
 *       elsewhere; and invalid L3 interfaces.
 * </ul>
 */
public interface L3Interface extends Node {

  @Nonnull
  NodeInterfacePair getInterface();
}
