package org.batfish.common.topology;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Topology;
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

  /**
   * Return the {@link Layer1Topology} with respect to physical layer-1 edges for a given {@link
   * NetworkSnapshot}. The physical layer-1 topology is constructed from the raw physical layer-1
   * edges input by the user by trimming the edges whose nodes do not correspond to active physical
   * interfaces.
   */
  @Nonnull
  Layer1Topology getLayer1PhysicalTopology(NetworkSnapshot networkSnapshot);

  /**
   * Return the {@link Layer2Topology} for a given {@link NetworkSnapshot}. The layer-2 topology is
   * constructed from the layer-1 logical topology and switching information in the configurations.
   */
  @Nonnull
  Layer2Topology getLayer2Topology(NetworkSnapshot networkSnapshot);

  /**
   * Return the layer-3 {@link Topology} for a given {@link NetworkSnapshot} that is a subset of the
   * raw layer-3 topology such that edges corresponding to blacklisted entities or failed tunnels
   * have been pruned.
   */
  @Nonnull
  Topology getLayer3Topology(NetworkSnapshot networkSnapshot);

  /** Return the raw {@link Layer1Topology} provided by the user in the snapshot. */
  @Nonnull
  Optional<Layer1Topology> getRawLayer1PhysicalTopology(NetworkSnapshot networkSnapshot);

  /**
   * Return the raw layer-3 {@link Topology} for a given {@link NetworkSnapshot}. The layer-3
   * topology is constructed by inferring layer-3 adjacencies via the layer-3 information in the
   * configurations, and pruning edges not in the same broadcast domain according to the layer-2
   * topology. No pruning of edges in different layer-2 broadcast domains occurs if raw layer-1
   * topology is not provided in snapshot.
   */
  @Nonnull
  Topology getRawLayer3Topology(NetworkSnapshot networkSnapshot);

  /** Return the {@link VxlanTopology} for a given {@link NetworkSnapshot}. */
  @Nonnull
  VxlanTopology getVxlanTopology(NetworkSnapshot snapshot);
}
