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

  public @Nullable String getExportPolicy() {
    return _exportPolicy;
  }

  public void setExportPolicy(@Nullable String exportPolicy) {
    _exportPolicy = exportPolicy;
  }

  public @Nullable String getExportToDefaultVrfPolicy() {
    return _exportToDefaultVrfPolicy;
  }

  public void setExportToDefaultVrfPolicy(String exportToDefaultVrfPolicy) {
    _exportToDefaultVrfPolicy = exportToDefaultVrfPolicy;
  }

  public @Nullable String getImportPolicy() {
    return _importPolicy;
  }

  public void setImportPolicy(@Nullable String importPolicy) {
    _importPolicy = importPolicy;
  }

  public @Nullable String getImportFromDefaultVrfPolicy() {
    return _importFromDefaultVrfPolicy;
  }

  public void setImportFromDefaultVrfPolicy(String importFromDefaultVrfPolicy) {
    _importFromDefaultVrfPolicy = importFromDefaultVrfPolicy;
  }

  /**
   * The route target values to attach to VPN routes originating from this VRF. Will be empty if it
   * must be auto-derived.
   */
  public @Nonnull Set<ExtendedCommunity> getRouteTargetExport() {
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
  public @Nonnull Set<ExtendedCommunity> getRouteTargetImport() {
    return _routeTargetImport;
  }

  public void addRouteTargetImport(ExtendedCommunity routeTargetImport) {
    _routeTargetImport =
        ImmutableSet.<ExtendedCommunity>builder()
            .addAll(_routeTargetImport)
            .add(routeTargetImport)
            .build();
  }

  private @Nonnull Set<ExtendedCommunity> _routeTargetExport;
  private @Nonnull Set<ExtendedCommunity> _routeTargetImport;
  private @Nullable String _exportPolicy;
  private @Nullable String _exportToDefaultVrfPolicy;
  private @Nullable String _importPolicy;
  private @Nullable String _importFromDefaultVrfPolicy;
}
