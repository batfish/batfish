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

  /**
   * Return the {@link Layer1Topology} with respect to logical layer-1 edges for a given {@link
   * NetworkSnapshot}. The logical layer-1 topology is constructed from the physical layer-1
   * topology by collapsing each set of edges betweeen physical interfaces belonging to the same
   * aggregates on each side into a single edge between the aggregate interfaces.<br>
   * <br>
   * So for example the two edges (n1:i1,n2:i1), (n1:i2,n2:i2) would be aggregated into a single
   * edge (n1:a1,n2:a1) if on both n1 and n2 the interfaces i1 and i2 are members of an aggregate
   * interface a1.
   */
  @Nonnull
  Layer1Topology getLayer1LogicalTopology(NetworkSnapshot networkSnapshot);

  /** Return the {@link VxlanTopology} for a given {@link NetworkSnapshot}. */
  @Nonnull
  VxlanTopology getVxlanTopology(NetworkSnapshot snapshot);
}
