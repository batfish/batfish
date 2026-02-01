package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;

/**
 * Represents a BGP process on a Huawei VRP device.
 *
 * <p>This is a stub class for future BGP implementation. It will store BGP configuration
 * information including AS number, neighbors, route maps, and other BGP-specific settings.
 */
public class HuaweiBgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  /** BGP AS number */
  private long _asNum;

  /** Router ID */
  private @Nullable Ip _routerId;

  /** BGP neighbors: IP address to neighbor configuration */
  private Map<Ip, BgpPeerConfig> _neighbors;

  /** BGP peer groups */
  private Map<String, Object> _peerGroups;

  /** BGP address families */
  private Map<String, Object> _addressFamilies;

  public HuaweiBgpProcess(long asNum) {
    _asNum = asNum;
    _neighbors = new TreeMap<>();
    _peerGroups = new TreeMap<>();
    _addressFamilies = new TreeMap<>();
  }

  /**
   * Gets the BGP AS number.
   *
   * @return The AS number
   */
  public long getAsNum() {
    return _asNum;
  }

  /**
   * Sets the BGP AS number.
   *
   * @param asNum The AS number to set
   */
  public void setAsNum(long asNum) {
    _asNum = asNum;
  }

  /**
   * Gets the router ID.
   *
   * @return The router ID, or null if not set
   */
  public @Nullable Ip getRouterId() {
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
   * Gets the BGP neighbors.
   *
   * @return A map of neighbor IP addresses to neighbor configurations
   */
  public @Nonnull Map<Ip, BgpPeerConfig> getNeighbors() {
    return _neighbors;
  }

  /**
   * Sets the BGP neighbors.
   *
   * @param neighbors The map of neighbor IP addresses to neighbor configurations
   */
  public void setNeighbors(@Nonnull Map<Ip, BgpPeerConfig> neighbors) {
    _neighbors = neighbors;
  }

  /**
   * Adds a BGP neighbor.
   *
   * @param ip The neighbor IP address
   * @param neighbor The neighbor configuration
   */
  public void addNeighbor(Ip ip, BgpPeerConfig neighbor) {
    _neighbors.put(ip, neighbor);
  }

  /**
   * Gets the BGP peer groups.
   *
   * @return A map of peer group names to configurations
   */
  public @Nonnull Map<String, Object> getPeerGroups() {
    return _peerGroups;
  }

  /**
   * Sets the BGP peer groups.
   *
   * @param peerGroups The map of peer group names to configurations
   */
  public void setPeerGroups(@Nonnull Map<String, Object> peerGroups) {
    _peerGroups = peerGroups;
  }

  /**
   * Gets the BGP address families.
   *
   * @return A map of address family names to configurations
   */
  public @Nonnull Map<String, Object> getAddressFamilies() {
    return _addressFamilies;
  }

  /**
   * Sets the BGP address families.
   *
   * @param addressFamilies The map of address family names to configurations
   */
  public void setAddressFamilies(@Nonnull Map<String, Object> addressFamilies) {
    _addressFamilies = addressFamilies;
  }
}
