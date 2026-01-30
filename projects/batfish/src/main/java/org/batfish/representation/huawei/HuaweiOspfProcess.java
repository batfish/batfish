package org.batfish.representation.huawei;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.ospf.OspfArea;

/**
 * Represents an OSPF process on a Huawei VRP device.
 *
 * <p>This is a stub class for future OSPF implementation. It will store OSPF configuration
 * information including process ID, areas, interfaces, and other OSPF-specific settings.
 */
public class HuaweiOspfProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  /** OSPF process ID */
  private long _processId;

  /** Router ID */
  @Nullable private Ip _routerId;

  /** OSPF areas: area ID to area configuration */
  private Map<Long, OspfArea> _areas;

  /** OSPF interfaces */
  private Map<String, Object> _interfaces;

  /** Default originate enabled */
  private boolean _defaultOriginate;

  /** Default originate route map */
  @Nullable private String _defaultOriginateRouteMap;

  public HuaweiOspfProcess(long processId) {
    _processId = processId;
    _areas = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _defaultOriginate = false;
  }

  /**
   * Gets the OSPF process ID.
   *
   * @return The process ID
   */
  public long getProcessId() {
    return _processId;
  }

  /**
   * Sets the OSPF process ID.
   *
   * @param processId The process ID to set
   */
  public void setProcessId(long processId) {
    _processId = processId;
  }

  /**
   * Gets the router ID.
   *
   * @return The router ID, or null if not set
   */
  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  /**
   * Sets the router ID.
   *
   * @param routerId The router ID to set
   */
  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  /**
   * Gets the OSPF areas.
   *
   * @return A map of area IDs to area configurations
   */
  @Nonnull
  public Map<Long, OspfArea> getAreas() {
    return _areas;
  }

  /**
   * Sets the OSPF areas.
   *
   * @param areas The map of area IDs to area configurations
   */
  public void setAreas(@Nonnull Map<Long, OspfArea> areas) {
    _areas = areas;
  }

  /**
   * Adds an OSPF area.
   *
   * @param areaId The area ID
   * @param area The area configuration
   */
  public void addArea(Long areaId, OspfArea area) {
    _areas.put(areaId, area);
  }

  /**
   * Gets the OSPF interfaces.
   *
   * @return A map of interface names to configurations
   */
  @Nonnull
  public Map<String, Object> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the OSPF interfaces.
   *
   * @param interfaces The map of interface names to configurations
   */
  public void setInterfaces(@Nonnull Map<String, Object> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Checks if default originate is enabled.
   *
   * @return true if default originate is enabled, false otherwise
   */
  public boolean getDefaultOriginate() {
    return _defaultOriginate;
  }

  /**
   * Sets whether default originate is enabled.
   *
   * @param defaultOriginate true to enable default originate, false to disable
   */
  public void setDefaultOriginate(boolean defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  /**
   * Gets the default originate route map.
   *
   * @return The route map name, or null if not set
   */
  @Nullable
  public String getDefaultOriginateRouteMap() {
    return _defaultOriginateRouteMap;
  }

  /**
   * Sets the default originate route map.
   *
   * @param defaultOriginateRouteMap The route map name
   */
  public void setDefaultOriginateRouteMap(@Nullable String defaultOriginateRouteMap) {
    _defaultOriginateRouteMap = defaultOriginateRouteMap;
  }
}
