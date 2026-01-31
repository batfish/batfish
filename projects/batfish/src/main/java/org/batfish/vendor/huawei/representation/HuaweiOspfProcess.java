package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Represents an OSPF process on a Huawei VRP device.
 *
 * <p>This class stores OSPF configuration information including process ID, router ID, areas,
 * networks, interfaces, and other OSPF-specific settings.
 */
public class HuaweiOspfProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  /** OSPF process ID */
  private long _processId;

  /** Router ID */
  private @Nullable Ip _routerId;

  /** OSPF networks: list of network advertisements with associated area IDs */
  private @Nonnull List<HuaweiOspfNetwork> _networks;

  /** OSPF areas: area ID to area configuration */
  private @Nonnull Map<Long, HuaweiOspfArea> _areas;

  /** OSPF interfaces */
  private @Nonnull Map<String, HuaweiOspfInterfaceSettings> _interfaces;

  /** Default originate enabled */
  private boolean _defaultOriginate;

  /** Default originate route map */
  private @Nullable String _defaultOriginateRouteMap;

  public HuaweiOspfProcess(long processId) {
    _processId = processId;
    _networks = new ArrayList<>();
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
   * Gets the OSPF networks.
   *
   * @return A list of network advertisements
   */
  @Nonnull
  public List<HuaweiOspfNetwork> getNetworks() {
    return _networks;
  }

  /**
   * Sets the OSPF networks.
   *
   * @param networks The list of network advertisements
   */
  public void setNetworks(@Nonnull List<HuaweiOspfNetwork> networks) {
    _networks = networks;
  }

  /**
   * Adds an OSPF network.
   *
   * @param network The network to add
   */
  public void addNetwork(HuaweiOspfNetwork network) {
    _networks.add(network);
  }

  /**
   * Gets the OSPF areas.
   *
   * @return A map of area IDs to area configurations
   */
  @Nonnull
  public Map<Long, HuaweiOspfArea> getAreas() {
    return _areas;
  }

  /**
   * Sets the OSPF areas.
   *
   * @param areas The map of area IDs to area configurations
   */
  public void setAreas(@Nonnull Map<Long, HuaweiOspfArea> areas) {
    _areas = areas;
  }

  /**
   * Gets or creates an OSPF area.
   *
   * @param areaId The area ID
   * @return The area configuration
   */
  @Nonnull
  public HuaweiOspfArea getOrCreateArea(long areaId) {
    return _areas.computeIfAbsent(areaId, HuaweiOspfArea::new);
  }

  /**
   * Adds an OSPF area.
   *
   * @param areaId The area ID
   * @param area The area configuration
   */
  public void addArea(Long areaId, HuaweiOspfArea area) {
    _areas.put(areaId, area);
  }

  /**
   * Gets the OSPF interfaces.
   *
   * @return A map of interface names to configurations
   */
  @Nonnull
  public Map<String, HuaweiOspfInterfaceSettings> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the OSPF interfaces.
   *
   * @param interfaces The map of interface names to configurations
   */
  public void setInterfaces(@Nonnull Map<String, HuaweiOspfInterfaceSettings> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Adds an OSPF interface setting.
   *
   * @param ifaceName The interface name
   * @param settings The interface settings
   */
  public void addInterface(String ifaceName, HuaweiOspfInterfaceSettings settings) {
    _interfaces.put(ifaceName, settings);
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

  /** Represents an OSPF network advertisement with associated area. */
  public static class HuaweiOspfNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private Prefix _network;
    private long _areaId;

    public HuaweiOspfNetwork(Prefix network, long areaId) {
      _network = network;
      _areaId = areaId;
    }

    public Prefix getNetwork() {
      return _network;
    }

    public void setNetwork(Prefix network) {
      _network = network;
    }

    public long getAreaId() {
      return _areaId;
    }

    public void setAreaId(long areaId) {
      _areaId = areaId;
    }
  }

  /** Represents an OSPF area configuration. */
  public static class HuaweiOspfArea implements Serializable {
    private static final long serialVersionUID = 1L;

    private long _areaId;
    private String _areaIdStr;

    public HuaweiOspfArea(long areaId) {
      _areaId = areaId;
    }

    public long getAreaId() {
      return _areaId;
    }

    public void setAreaId(long areaId) {
      _areaId = areaId;
    }

    public String getAreaIdStr() {
      return _areaIdStr;
    }

    public void setAreaIdStr(String areaIdStr) {
      _areaIdStr = areaIdStr;
    }
  }

  /** Represents OSPF interface settings. */
  public static class HuaweiOspfInterfaceSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long _areaId;
    private Integer _cost;
    private Integer _helloInterval;
    private Integer _deadInterval;
    private Integer _retransmitInterval;

    public Long getAreaId() {
      return _areaId;
    }

    public void setAreaId(Long areaId) {
      _areaId = areaId;
    }

    public Integer getCost() {
      return _cost;
    }

    public void setCost(Integer cost) {
      _cost = cost;
    }

    public Integer getHelloInterval() {
      return _helloInterval;
    }

    public void setHelloInterval(Integer helloInterval) {
      _helloInterval = helloInterval;
    }

    public Integer getDeadInterval() {
      return _deadInterval;
    }

    public void setDeadInterval(Integer deadInterval) {
      _deadInterval = deadInterval;
    }

    public Integer getRetransmitInterval() {
      return _retransmitInterval;
    }

    public void setRetransmitInterval(Integer retransmitInterval) {
      _retransmitInterval = retransmitInterval;
    }
  }
}
