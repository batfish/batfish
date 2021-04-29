package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** Address-family-specific configuration for a {@link Vrf}. */
@ParametersAreNonnullByDefault
public final class VrfAddressFamily implements Serializable {

  public VrfAddressFamily() {
    _routeTargetExport = ImmutableSet.of();
    _routeTargetImport = ImmutableSet.of();
  }

  @Nullable
  public String getExportPolicy() {
    return _exportPolicy;
  }

  public void setExportPolicy(@Nullable String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  @Nullable
  public String getExportToDefaultVrfPolicy() {
    return _exportToDefaultVrfPolicy;
  }

  public void setExportToDefaultVrfPolicy(String exportToDefaultVrfPolicy) {
    _exportToDefaultVrfPolicy = exportToDefaultVrfPolicy;
  }

  @Nullable
  public String getImportPolicy() {
    return _importPolicy;
  }

  public void setImportPolicy(@Nullable String importPolicy) {
    _importPolicy = importPolicy;
  }

  @Nullable
  public String getImportFromDefaultVrfPolicy() {
    return _importFromDefaultVrfPolicy;
  }

  public void setImportFromDefaultVrfPolicy(String importFromDefaultVrfPolicy) {
    _importFromDefaultVrfPolicy = importFromDefaultVrfPolicy;
  }

  /**
   * The route target values to attach to VPN routes originating from this VRF. Will be empty if it
   * must be auto-derived.
   */
  @Nonnull
  public Set<ExtendedCommunity> getRouteTargetExport() {
    return _routeTargetExport;
  }

  public void addRouteTargetExport(ExtendedCommunity routeTargetExport) {
    _routeTargetExport =
        ImmutableSet.<ExtendedCommunity>builder()
            .addAll(_routeTargetExport)
            .add(routeTargetExport)
            .build();
  }

  /**
   * Routes that contain any of these route target community should be merged into this VRF. Will be
   * empty if it must be auto-derived.
   */
  @Nonnull
  public Set<ExtendedCommunity> getRouteTargetImport() {
    return _routeTargetImport;
  }

  public void addRouteTargetImport(ExtendedCommunity routeTargetImport) {
    _routeTargetImport =
        ImmutableSet.<ExtendedCommunity>builder()
            .addAll(_routeTargetImport)
            .add(routeTargetImport)
            .build();
  }

  @Nonnull private Set<ExtendedCommunity> _routeTargetExport;
  @Nonnull private Set<ExtendedCommunity> _routeTargetImport;
  @Nullable private String _exportPolicy;
  @Nullable private String _exportToDefaultVrfPolicy;
  @Nullable private String _importPolicy;
  @Nullable private String _importFromDefaultVrfPolicy;
}
