package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-specific representation of an Aruba AOS-CX OSPFv3 process. */
public final class AosCxOspfv3Process
    implements Serializable {

  /** AOS-CX default reference bandwidth: 100000 Mbps. */
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100_000_000_000D;

  public AosCxOspfv3Process(int processId) {
    _processId = processId;
  }

  public int getProcessId() {
    return _processId;
  }

  public boolean getRedistributeConnected() {
    return _redistributeConnected;
  }

  public void setRedistributeConnected(
      boolean redistributeConnected) {
    _redistributeConnected =
        redistributeConnected;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public void setPassiveInterfaceDefault(
      boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault =
        passiveInterfaceDefault;
  }

  public double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public void setReferenceBandwidthMbps(
      long bandwidthMbps) {
    _referenceBandwidth =
        bandwidthMbps * 1_000_000D;
  }

  public void resetReferenceBandwidth() {
    _referenceBandwidth =
        DEFAULT_REFERENCE_BANDWIDTH;
  }

  public @Nonnull Set<String> getAreas() {
    return _areas;
  }

  public void addArea(String area) {
    _areas.add(area);
  }

  /**
   * Return stub areas keyed by configured area ID.
   *
   * <p>The Boolean value is true for {@code no-summary}.
   */
  public @Nonnull Map<String, Boolean>
      getStubAreas() {
    return _stubAreas;
  }

  public void setStubArea(
      String area,
      boolean suppressInterArea) {
    _areas.add(area);
    _stubAreas.put(
        area,
        suppressInterArea);
  }

  /**
   * Convert an area back to normal while retaining the area itself.
   */
  public void clearStubArea(String area) {
    _areas.add(area);
    _stubAreas.remove(area);
  }

  /**
   * Retain stub status while clearing no-summary.
   */
  public void clearStubNoSummary(String area) {
    _areas.add(area);

    if (_stubAreas.containsKey(area)) {
      _stubAreas.put(area, false);
    }
  }

  public @Nonnull Map<String, Long>
      getAreaDefaultMetrics() {
    return _areaDefaultMetrics;
  }

  public void setAreaDefaultMetric(
      String area,
      long metric) {
    _areas.add(area);
    _areaDefaultMetrics.put(area, metric);
  }

  public void clearAreaDefaultMetric(
      String area) {
    _areas.add(area);
    _areaDefaultMetrics.remove(area);
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  private final int _processId;
  private boolean _redistributeConnected;
  private boolean _passiveInterfaceDefault;
  private double _referenceBandwidth =
      DEFAULT_REFERENCE_BANDWIDTH;
  private final @Nonnull Set<String> _areas =
      new HashSet<>();
  private final @Nonnull Map<String, Boolean>
      _stubAreas = new HashMap<>();
  private final @Nonnull Map<String, Long>
      _areaDefaultMetrics = new HashMap<>();
  private @Nullable Ip _routerId;
}
