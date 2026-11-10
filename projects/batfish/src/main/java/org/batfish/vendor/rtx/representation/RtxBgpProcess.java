package org.batfish.vendor.rtx.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** The device's BGP configuration, from the {@code bgp} commands. */
public final class RtxBgpProcess implements Serializable {

  private final @Nonnull Map<Integer, RtxBgpNeighbor> _neighbors;
  private final @Nonnull Map<Integer, RtxBgpRouteFilter> _importFilters;
  private final @Nonnull Map<Integer, RtxBgpRouteFilter> _exportFilters;
  private final @Nonnull List<RtxBgpImportRule> _importRules;
  private final @Nonnull List<RtxBgpExportRule> _exportRules;
  private @Nullable Long _localAs;
  private boolean _enabled;

  public RtxBgpProcess() {
    _neighbors = new LinkedHashMap<>();
    _importFilters = new LinkedHashMap<>();
    _exportFilters = new LinkedHashMap<>();
    _importRules = new ArrayList<>();
    _exportRules = new ArrayList<>();
  }

  /** The local AS number, from {@code bgp autonomous-system}. */
  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(long localAs) {
    _localAs = localAs;
  }

  /** Whether {@code bgp use on} was configured. */
  public boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public @Nonnull Map<Integer, RtxBgpNeighbor> getNeighbors() {
    return _neighbors;
  }

  /** Route filters used while selecting local routes for BGP advertisement. */
  public @Nonnull Map<Integer, RtxBgpRouteFilter> getImportFilters() {
    return _importFilters;
  }

  /** Route filters applied to routes received from BGP peers. */
  public @Nonnull Map<Integer, RtxBgpRouteFilter> getExportFilters() {
    return _exportFilters;
  }

  public @Nonnull List<RtxBgpImportRule> getImportRules() {
    return _importRules;
  }

  public @Nonnull List<RtxBgpExportRule> getExportRules() {
    return _exportRules;
  }
}
