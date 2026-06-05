package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS BGP neighbor (e.g. {@code bgp neighbor "10.0.0.1"}), keyed by peer IP string. A neighbor
 * references its template {@link BgpGroup} via the mandatory, immutable {@code group} leafref;
 * unset per-peer attributes are inherited from that group (resolved at conversion). Per-peer values
 * configured directly on the neighbor override the group's.
 */
public final class BgpNeighbor implements Serializable {

  public BgpNeighbor(String ipAddress) {
    _ipAddress = ipAddress;
    _importPolicies = new ArrayList<>();
    _exportPolicies = new ArrayList<>();
  }

  public @Nonnull String getIpAddress() {
    return _ipAddress;
  }

  /** The name of the {@link BgpGroup} this neighbor inherits from (mandatory in valid config). */
  public @Nullable String getGroup() {
    return _group;
  }

  public void setGroup(@Nullable String group) {
    _group = group;
  }

  /** The {@code peer-as} configured directly on the neighbor, or {@code null} (inherit group). */
  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }

  /** The ordered {@code import policy [...]} configured directly on the neighbor. */
  public @Nonnull List<String> getImportPolicies() {
    return _importPolicies;
  }

  /** The ordered {@code export policy [...]} configured directly on the neighbor. */
  public @Nonnull List<String> getExportPolicies() {
    return _exportPolicies;
  }

  private final @Nonnull String _ipAddress;
  private @Nullable String _group;
  private @Nullable Long _peerAs;
  private final @Nonnull List<String> _importPolicies;
  private final @Nonnull List<String> _exportPolicies;
}
