package org.batfish.common.topology;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;

/**
 * Provides access to various views of the {@link Layer1Topology} for a given snapshot, in a
 * centralized place.
 */
@ParametersAreNonnullByDefault
public final class Layer1Topologies {

  /* Returns a {@link Layer1Topologies} in which all topologies are empty. */
  public static @Nonnull Layer1Topologies empty() {
    return new Layer1Topologies(
        Layer1Topology.EMPTY, Layer1Topology.EMPTY, Layer1Topology.EMPTY, Layer1Topology.EMPTY);
  }

  /**
   * A placeholder for an interface not in the network. Note that its hostname is invalid, so it
   * cannot ever conflict with a {@link Configuration}.
   */
  public static final Layer1Node INVALID_INTERFACE = new Layer1Node("~INVALID~", "~INTERFACE~");

  /**
   * Returns a version {@link #getLogicalL1()} filtered to edges between active interfaces, and
   * adding reverse edges if necessary to ensure all edges are bidirectional.
   */
  public @Nonnull Layer1Topology getActiveLogicalL1() {
    return _activeLogicalL1;
  }

  /**
   * Returns a {@link Layer1Topology} that contains all edges in either {@link #getUserProvidedL1()}
   * or {@link #getSynthesizedL1()}.
   *
   * <p>Note: edges are uni-directional as in their input; they may refer to non-existent devices or
   * interfaces; they may refer to inactive interfaces.
   */
  public @Nonnull Layer1Topology getCombinedL1() {
    return _combinedL1;
  }

  /**
   * Returns a version of {@link #getCombinedL1() the combined L1 topology} in which physical
   * interfaces may have been replaced by the aggregate interfaces they correspond to. Downstream
   * computations only use logical interfaces for modeling L2/L3 adjacencies. Additionally,
   * interfaces that do not exist are replaced by {@link #INVALID_INTERFACE}.
   *
   * <p>Note: the topology is still unidirectional and includes edges where one or both sides are
   * invalid or inactive.
   *
   * @see #getCombinedL1()
   * @see #getActiveLogicalL1()
   */
  public @Nonnull Layer1Topology getLogicalL1() {
    return _logicalL1;
  }

  /**
   * Returns any Layer-1 edges that were created automatically, either by vendors or by features
   * like ISP modeling.
   */
  public @Nonnull Layer1Topology getSynthesizedL1() {
    return _synthesizedL1;
  }

  /**
   * Returns the user-provided {@link Layer1Topology}, potentially modified to clean up {@link
   * Layer1Node nodes} that indicated interfaces in non-canonical form.
   */
  public @Nonnull Layer1Topology getUserProvidedL1() {
    return _canonicalUserL1;
  }

  // Internal details

  /**
   * This is only exposed for testing. Use {@link Layer1TopologiesFactory} to create {@link
   * Layer1Topologies}.
   *
   * @see Layer1TopologiesFactory#create(Layer1Topology, Layer1Topology, Map)
   * @see #empty()
   */
  @VisibleForTesting
  public Layer1Topologies(
      Layer1Topology canonicalUserL1,
      Layer1Topology synthesizedL1,
      Layer1Topology logicalL1,
      Layer1Topology activeLogicalL1) {
    _canonicalUserL1 = canonicalUserL1;
    _synthesizedL1 = synthesizedL1;
    _combinedL1 =
        new Layer1Topology(
            Stream.concat(_canonicalUserL1.edgeStream(), _synthesizedL1.edgeStream()));
    _logicalL1 = logicalL1;
    _activeLogicalL1 = activeLogicalL1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Layer1Topologies)) {
      return false;
    }
    Layer1Topologies that = (Layer1Topologies) o;
    return _synthesizedL1.equals(that._synthesizedL1)
        && _canonicalUserL1.equals(that._canonicalUserL1)
        // && _combinedL1.equals(that._combinedL1) // redundant, since derived from others
        && _logicalL1.equals(that._logicalL1)
        && _activeLogicalL1.equals(that._activeLogicalL1);
  }

  @Override
  public int hashCode() {
    // _combinedL1 is redundant, since derived from others.
    return Objects.hash(_synthesizedL1, _canonicalUserL1, _logicalL1, _activeLogicalL1);
  }

  /** Extra Layer-1 edges created during conversion (vendor-specific) or post-processing (ISPs). */
  private final @Nonnull Layer1Topology _synthesizedL1;

  /**
   * The user's provided {@link Layer1Topology} in canonical form, where we have attempted to fix
   * typos / normalize interface names to match those in the given network.
   */
  private final @Nonnull Layer1Topology _canonicalUserL1;

  /**
   * A {@link Layer1Topology} that contains all edges in either {@link #_canonicalUserL1} or {@link
   * #_synthesizedL1}.
   *
   * <p>Note: edges are uni-directional as in their input; they may refer to non-existent devices or
   * interfaces; they may refer to inactive interfaces.
   */
  private final Layer1Topology _combinedL1;

  /**
   * A version of {@link #_combinedL1} where physical interfaces may have been replaced by the
   * aggregate interfaces they correspond to. Additionally, interfaces that do not exist are
   * replaced by {@link #INVALID_INTERFACE}.
   *
   * <p>Note: the topology is still unidirectional and includes edges where one or both sides are
   * invalid or inactive.
   */
  private final Layer1Topology _logicalL1;

  /** {@link #_logicalL1} filtered to active-active edges and making all edges bidirectional. */
  private final Layer1Topology _activeLogicalL1;
}
