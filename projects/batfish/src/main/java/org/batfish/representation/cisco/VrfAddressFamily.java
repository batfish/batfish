package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** Address family configuration inside a VRF. Modeled based on IOS XE. */
@ParametersAreNonnullByDefault
public class VrfAddressFamily implements Serializable {

  public VrfAddressFamily() {}

  @Nullable
  public String getExportMap() {
    return _exportMap;
  }

  public void setExportMap(@Nullable String exportMap) {
    _exportMap = exportMap;
  }

  @Nullable
  public String getImportMap() {
    return _importMap;
  }

  public void setImportMap(@Nullable String importMap) {
    _importMap = importMap;
  }

  /**
   * The route target values to attach to VPNv4 routes originating from this VRF. Returns empty set
   * if none are configured.
   */
  @Nonnull
  public Set<ExtendedCommunity> getRouteTargetExport() {
    return firstNonNull(_routeTargetExport, ImmutableSet.of());
  }

  public void addRouteTargetExport(ExtendedCommunity routeTargetExport) {
    if (_routeTargetExport == null) {
      _routeTargetExport = new HashSet<>(1);
    }
    _routeTargetExport.add(routeTargetExport);
  }

  /**
   * Routes that contain this route target community should be merged into this VRF. Returns empty
   * set if none are configured.
   */
  @Nonnull
  public Set<ExtendedCommunity> getRouteTargetImport() {
    return firstNonNull(_routeTargetImport, ImmutableSet.of());
  }

  public void addRouteTargetImport(ExtendedCommunity routeTargetImport) {
    if (_routeTargetImport == null) {
      _routeTargetImport = new HashSet<>(1);
    }
    _routeTargetImport.add(routeTargetImport);
  }

  public void inherit(VrfAddressFamily other) {
    if (_routeTargetExport == null) {
      _routeTargetExport = other._routeTargetExport;
    }
    if (_routeTargetImport == null) {
      _routeTargetImport = other._routeTargetImport;
    }
  }

  @Nullable private String _exportMap;
  @Nullable private String _importMap;
  @Nullable private Set<ExtendedCommunity> _routeTargetExport;
  @Nullable private Set<ExtendedCommunity> _routeTargetImport;
}
