package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS BGP neighbor (e.g. {@code bgp neighbor "10.0.0.1"}), keyed by peer IP string. A neighbor
 * references its template {@link BgpGroup} via the mandatory, immutable {@code group} leafref and
 * inherits the group's per-peer attributes for any it does not configure directly (see {@link
 * #inheritFrom}). Per-peer values configured directly on the neighbor override the group's.
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

  /** The peer {@code type} (internal/external), or {@code null} if not set on the neighbor. */
  public @Nullable PeerType getType() {
    return _type;
  }

  public void setType(@Nullable PeerType type) {
    _type = type;
  }

  /** The {@code peer-as}, or {@code null} if not set on the neighbor. */
  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }

  /** The ordered {@code import policy [...]} on the neighbor. */
  public @Nonnull List<String> getImportPolicies() {
    return _importPolicies;
  }

  /** The ordered {@code export policy [...]} on the neighbor. */
  public @Nonnull List<String> getExportPolicies() {
    return _exportPolicies;
  }

  /**
   * Fill any attribute not set directly on this neighbor from its {@code group} (per-neighbor
   * config wins). This resolves the SR-OS {@code group}→{@code neighbor} inheritance in the
   * representation, so conversion reads a fully-populated neighbor — mirroring how the NX-OS model
   * resolves template inheritance with a {@code doInherit} pass before conversion rather than
   * inline in conversion. Idempotent.
   */
  public void inheritFrom(@Nullable BgpGroup group) {
    if (group == null) {
      return;
    }
    if (_type == null) {
      _type = group.getType();
    }
    if (_peerAs == null) {
      _peerAs = group.getPeerAs();
    }
    if (_importPolicies.isEmpty()) {
      _importPolicies.addAll(group.getImportPolicies());
    }
    if (_exportPolicies.isEmpty()) {
      _exportPolicies.addAll(group.getExportPolicies());
    }
  }

  private final @Nonnull String _ipAddress;
  private @Nullable String _group;
  private @Nullable PeerType _type;
  private @Nullable Long _peerAs;
  private final @Nonnull List<String> _importPolicies;
  private final @Nonnull List<String> _exportPolicies;
}
