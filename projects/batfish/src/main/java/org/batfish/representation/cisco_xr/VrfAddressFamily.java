package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** Address-family-specific configuration for a {@link Vrf}. */
@ParametersAreNonnullByDefault
public final class VrfAddressFamily implements Serializable {

  public VrfAddressFamily() {
    _routeTargetExport = ImmutableList.of();
    _routeTargetImport = ImmutableList.of();
    _exportPolicyByVrf = ImmutableMap.of();
    _importPolicyByVrf = ImmutableMap.of();
  }

  @Nullable
  public String getExportPolicy() {
    return _exportPolicy;
  }

  public void setExportPolicy(@Nullable String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  @Nonnull
  public Map<String, String> getExportPolicyByVrf() {
    return _exportPolicyByVrf;
  }

  public void setExportPolicyForVrf(String vrf, String policy) {
    _exportPolicyByVrf =
        ImmutableMap.<String, String>builder().putAll(_exportPolicyByVrf).put(vrf, policy).build();
  }

  @Nullable
  public String getImportPolicy() {
    return _importPolicy;
  }

  public void setImportPolicy(@Nullable String importPolicy) {
    _importPolicy = importPolicy;
  }

  @Nonnull
  public Map<String, String> getImportPolicyByVrf() {
    return _importPolicyByVrf;
  }

  public void setImportPolicyForVrf(String vrf, String policy) {
    _importPolicyByVrf =
        ImmutableMap.<String, String>builder().putAll(_importPolicyByVrf).put(vrf, policy).build();
  }

  /**
   * The route target values to attach to VPN routes originating from this VRF. Will be empty if it
   * must be auto-derived.
   */
  @Nonnull
  public List<ExtendedCommunity> getRouteTargetExport() {
    return _routeTargetExport;
  }

  public void addRouteTargetExport(ExtendedCommunity routeTargetExport) {
    _routeTargetExport =
        ImmutableList.<ExtendedCommunity>builder()
            .addAll(_routeTargetExport)
            .add(routeTargetExport)
            .build();
  }

  /**
   * Routes that contain any of these route target community should be merged into this VRF. Will be
   * empty if it must be auto-derived.
   */
  @Nonnull
  public List<ExtendedCommunity> getRouteTargetImport() {
    return _routeTargetImport;
  }

  public void addRouteTargetImport(ExtendedCommunity routeTargetImport) {
    _routeTargetImport =
        ImmutableList.<ExtendedCommunity>builder()
            .addAll(_routeTargetImport)
            .add(routeTargetImport)
            .build();
  }

  @Nonnull private List<ExtendedCommunity> _routeTargetExport;
  @Nonnull private List<ExtendedCommunity> _routeTargetImport;
  @Nonnull private Map<String, String> _exportPolicyByVrf;
  @Nonnull private Map<String, String> _importPolicyByVrf;
  @Nullable private String _exportPolicy;
  @Nullable private String _importPolicy;
}
