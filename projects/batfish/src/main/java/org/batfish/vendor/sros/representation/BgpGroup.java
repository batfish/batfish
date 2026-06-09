package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * An SR-OS BGP peer group (template), keyed by group-name (e.g. {@code bgp group "ebgp"}). Groups
 * and neighbors share the same per-peer leaf set; a neighbor inherits unset values from its group
 * (see {@link BgpNeighbor#inheritFrom}). Import/export policy leaf-lists are {@code ordered-by
 * user}, so order is preserved.
 */
public final class BgpGroup implements Serializable {

  public BgpGroup(String name) {
    _name = name;
    _importPolicies = new ArrayList<>();
    _exportPolicies = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The peer {@code type} (internal/external) for this group, or {@code null} if unset. */
  public @Nullable PeerType getType() {
    return _type;
  }

  public void setType(@Nullable PeerType type) {
    _type = type;
  }

  /** The {@code peer-as} for this group, or {@code null} if unset. */
  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }

  /** The ordered {@code import policy [...]} leaf-list. */
  public @Nonnull List<String> getImportPolicies() {
    return _importPolicies;
  }

  /** The ordered {@code export policy [...]} leaf-list. */
  public @Nonnull List<String> getExportPolicies() {
    return _exportPolicies;
  }

  /**
   * The route-reflector {@code cluster cluster-id} for this group, or {@code null} if not a route
   * reflector. Peers in a group with a cluster-id are route-reflector clients.
   */
  public @Nullable Ip getClusterId() {
    return _clusterId;
  }

  public void setClusterId(@Nullable Ip clusterId) {
    _clusterId = clusterId;
  }

  /** The {@code next-hop-self} flag, or {@code null} if unset. */
  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(@Nullable Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  private final @Nonnull String _name;
  private @Nullable PeerType _type;
  private @Nullable Long _peerAs;
  private final @Nonnull List<String> _importPolicies;
  private final @Nonnull List<String> _exportPolicies;
  private @Nullable Ip _clusterId;
  private @Nullable Boolean _nextHopSelf;
}
