package org.batfish.common.topology;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.vxlan.VxlanTopology;

/**
 * A provider of various topology-type data structures (e.g., IpOwners, L3 Topology,
 * protocol-specific topology graphs) for network snapshots.
 */
@ParametersAreNonnullByDefault
public interface TopologyProvider {
  /**
   * Return {@link IpOwners} computed (based on configurations only) for a given {@link
   * NetworkSnapshot}
   */
  @Nonnull
  IpOwners getIpOwners(NetworkSnapshot snapshot);

  /** Return the {@link VxlanTopology} for a given {@link NetworkSnapshot}. */
  @Nonnull
  VxlanTopology getVxlanTopology(NetworkSnapshot snapshot);
}
