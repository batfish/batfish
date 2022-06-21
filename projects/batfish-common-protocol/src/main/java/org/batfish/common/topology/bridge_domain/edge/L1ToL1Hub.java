package org.batfish.common.topology.bridge_domain.edge;

import static org.batfish.common.topology.bridge_domain.function.StateFunctions.identity;

import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.node.L1Hub;
import org.batfish.common.topology.bridge_domain.node.L1Interface;

/** An edge from a {@link L1Interface} to an {@link L1Hub}. */
public final class L1ToL1Hub extends Edge {

  public static @Nonnull L1ToL1Hub instance() {
    return INSTANCE;
  }

  private static final L1ToL1Hub INSTANCE = new L1ToL1Hub();

  private L1ToL1Hub() {
    super(identity());
  }
}
