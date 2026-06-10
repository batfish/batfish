package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The {@code service vprn "<name>" bgp-ipvpn mpls} settings: the {@code route-distinguisher} and
 * the {@code vrf-target} import/export route-targets that drive MPLS L3VPN (RFC 4364) route
 * import/export.
 *
 * <p>The {@code vrf-target} has two forms: a single {@code community <rt>} (used for both import
 * and export), or separate {@code import-community}/{@code export-community}. Both are captured as
 * import + export RT lists (the single-community form populates both).
 *
 * <p>Note: the route-distinguisher converts onto the VI VRF, but inter-PE VPN-IPv4 route import
 * (PE-to-PE MP-BGP L3VPN) is <em>not</em> reproduced by the Batfish VI dataplane (no VPNv4 address
 * family; cross-VRF leaking is intra-node only). These fields are extracted for completeness and
 * structure tracking; see docs/parsing/vendors/sros.md and the tracked L3VPN follow-up issue.
 */
public final class BgpIpvpn implements Serializable {

  /** The {@code route-distinguisher} (e.g. {@code 65000:1}), or {@code null} if unset. */
  public @Nullable String getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setRouteDistinguisher(@Nullable String routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  /** The {@code vrf-target} import route-target communities (e.g. {@code target:65000:1}). */
  public @Nonnull List<String> getImportRouteTargets() {
    return _importRouteTargets;
  }

  /** The {@code vrf-target} export route-target communities (e.g. {@code target:65000:1}). */
  public @Nonnull List<String> getExportRouteTargets() {
    return _exportRouteTargets;
  }

  private @Nullable String _routeDistinguisher;
  private final @Nonnull List<String> _importRouteTargets = new ArrayList<>();
  private final @Nonnull List<String> _exportRouteTargets = new ArrayList<>();
}
