package org.batfish.dataplane.ibdp;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.NetworkConfigurations;

/**
 * A hook for modifying the topology used in each iteration of incremental data-plane computation.
 */
@ParametersAreNonnullByDefault
public interface TopologyContextModifier {

  /** Called once before the first topology iteration. */
  default void initialize(NetworkConfigurations configurations) {}

  /** Returns the topology context to use in the current iteration. */
  @Nonnull
  TopologyContext updateTopologyContext(TopologyContext context);

  /**
   * Returns whether this modifier no longer requires additional topology iterations.
   *
   * <p>Once this method returns {@code true}, it must continue to return {@code true}. The modifier
   * may continue to apply an idempotent transformation to newly computed topology contexts.
   */
  boolean isDone();
}
