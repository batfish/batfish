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

  /** AOS-CX default administrative distance for OSPFv3. */
  public static final int DEFAULT_ADMIN_DISTANCE =
      110;

  /** AOS-CX default reference bandwidth: 100000 Mbps. */
  public static final double DEFAULT_REFERENCE_BANDWIDTH =
      100_000_000_000D;

  /** AOS-CX default metric for redistributed OSPFv3 routes. */
  public static final long DEFAULT_REDISTRIBUTION_METRIC =
      25L;

  /** AOS-CX default metric for default-information origination. */
  public static final long DEFAULT_INFORMATION_METRIC =
      1L;

  public AosCxOspfv3Process(int processId) {
    _processId = processId;
  }

  public int getProcessId() {
    return _processId;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public int getIntraAreaDistance() {
    return _intraAreaDistance;
  }

  public int getInterAreaDistance() {
    return _interAreaDistance;
  }

  public int getExternalDistance() {
    return _externalDistance;
  }

  public void setDistance(int distance) {
    _intraAreaDistance = distance;
    _interAreaDistance = distance;
    _externalDistance = distance;
  }

  public void setIntraAreaDistance(int distance) {
    _intraAreaDistance = distance;
  }

  public void setInterAreaDistance(int distance) {
    _interAreaDistance = distance;
  }

  public void setExternalDistance(int distance) {
    _externalDistance = distance;
  }

  public void resetDistance() {
    setDistance(DEFAULT_ADMIN_DISTANCE);
  }

  public void resetIntraAreaDistance() {
    _intraAreaDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public void resetInterAreaDistance() {
    _interAreaDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public void resetExternalDistance() {
    _externalDistance =
        DEFAULT_ADMIN_DISTANCE;
  }

  public boolean getRedistributeConnected() {
    return _redistributeConnected;
  }

  public void setRedistributeConnected(
      boolean redistributeConnected) {
    setRedistributeConnected(
        redistributeConnected,
        null);
  }

  public void setRedistributeConnected(
      boolean redistributeConnected,
      @Nullable String routeMap) {
    _redistributeConnected =
        redistributeConnected;
    _redistributeConnectedRouteMap =
        redistributeConnected
            ? routeMap
            : null;
  }

  public @Nullable String
      getRedistributeConnectedRouteMap() {
    return _redistributeConnectedRouteMap;
  }

  public boolean getRedistributeStatic() {
    return _redistributeStatic;
  }

  public void setRedistributeStatic(
      boolean redistributeStatic) {
    setRedistributeStatic(
        redistributeStatic,
        null);
  }

  public void setRedistributeStatic(
      boolean redistributeStatic,
      @Nullable String routeMap) {
    _redistributeStatic =
        redistributeStatic;
    _redistributeStaticRouteMap =
        redistributeStatic
            ? routeMap
            : null;
  }

  public @Nullable String
      getRedistributeStaticRouteMap() {
    return _redistributeStaticRouteMap;
  }

  public long getRedistributionMetric() {
    return _redistributionMetric;
  }

  public void setRedistributionMetric(
      long redistributionMetric) {
    _redistributionMetric =
        redistributionMetric;
  }

  public void resetRedistributionMetric() {
    _redistributionMetric =
        DEFAULT_REDISTRIBUTION_METRIC;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public boolean getDefaultInformationOriginateAlways() {
    return _defaultInformationOriginateAlways;
  }

  public long getDefaultInformationMetric() {
    return _defaultInformationMetric;
  }

  public void setDefaultInformationOriginate(
      boolean always,
      long metric) {
    _defaultInformationOriginate = true;
    _defaultInformationOriginateAlways = always;
    _defaultInformationMetric = metric;
  }

  public void disableDefaultInformationOriginate() {
    _defaultInformationOriginate = false;
    _defaultInformationOriginateAlways = false;
    _defaultInformationMetric =
        DEFAULT_INFORMATION_METRIC;
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

  /** Convert an area back to normal while retaining the area itself. */
  public void clearStubArea(String area) {
    _areas.add(area);
    _stubAreas.remove(area);
  }

  /** Retain stub status while clearing no-summary. */
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

  private boolean _enabled = true;
  private int _intraAreaDistance =
      DEFAULT_ADMIN_DISTANCE;
  private int _interAreaDistance =
      DEFAULT_ADMIN_DISTANCE;
  private int _externalDistance =
      DEFAULT_ADMIN_DISTANCE;

  private boolean _defaultInformationOriginate;
  private boolean _defaultInformationOriginateAlways;
  private long _defaultInformationMetric =
      DEFAULT_INFORMATION_METRIC;

  private boolean _redistributeConnected;
  private @Nullable String
      _redistributeConnectedRouteMap;
  private boolean _redistributeStatic;
  private @Nullable String
      _redistributeStaticRouteMap;
  private long _redistributionMetric =
      DEFAULT_REDISTRIBUTION_METRIC;

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
