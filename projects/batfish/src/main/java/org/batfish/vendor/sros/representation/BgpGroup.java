package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS BGP peer group (template), keyed by group-name (e.g. {@code bgp group "ebgp"}). Groups
 * and neighbors share the same per-peer leaf set; a neighbor inherits unset values from its group
 * (resolved at conversion). Import/export policy leaf-lists are {@code ordered-by user}, so order
 * is preserved.
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

  private final @Nonnull String _name;
  private @Nullable Long _peerAs;
  private final @Nonnull List<String> _importPolicies;
  private final @Nonnull List<String> _exportPolicies;
}
