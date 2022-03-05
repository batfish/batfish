package org.batfish.common.topology.bridge_domain.node;

import javax.annotation.Nonnull;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** A {@link BridgedL3Interface} or a {@link NonBridgedL3Interface}. */
public interface L3Interface extends Node {

  @Nonnull
  NodeInterfacePair getInterface();
}
