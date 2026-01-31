package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
// import org.batfish.datamodel.AclLine; // TODO: Add when needed
import org.batfish.datamodel.Ip;

/**
 * Represents a NAT rule on a Huawei VRP device.
 *
 * <p>This is a stub class for future NAT implementation. It will store NAT configuration including
 * NAT type (static, dynamic, easy-ip, NAT server), ACL references, and address pools.
 */
public class HuaweiNatRule implements Serializable {

  private static final long serialVersionUID = 1L;

  /** NAT type enumeration */
  public enum NatType {
    STATIC, // Static one-to-one NAT
    DYNAMIC, // Dynamic NAT with address pool
    EASY_IP, // Easy IP (PAT/NAPT)
    NAT_SERVER, // NAT server (port forwarding)
    NAT_ALG // NAT Application Level Gateway
  }

  /** NAT rule name/identifier */
  private @Nonnull String _name;

  /** NAT type */
  private @Nonnull NatType _type;

  /** ACL name for traffic matching */
  private @Nullable String _aclName;

  /** Global address pool name */
  private @Nullable String _poolName;

  /** Global IP address (for static NAT or NAT server) */
  private @Nullable Ip _globalIp;

  /** Global port (for NAT server) */
  private @Nullable Integer _globalPort;

  /** Inside local IP address */
  private @Nullable Ip _insideLocalIp;

  /** Inside local port */
  private @Nullable Integer _insideLocalPort;

  /** Interface name (for Easy IP) */
  private @Nullable String _interfaceName;

  /** Protocol (tcp, udp, or null for any) */
  private @Nullable String _protocol;

  /** VRF name */
  private @Nullable String _vrfName;

  /** Address pool (list of global IP addresses for dynamic NAT) */
  private @Nullable List<Ip> _addressPool;

  public HuaweiNatRule(@Nonnull String name, @Nonnull NatType type) {
    _name = name;
    _type = type;
    _addressPool = new ArrayList<>();
  }

  /**
   * Gets the NAT rule name.
   *
   * @return The rule name
   */
  @Nonnull
  public String getName() {
    return _name;
  }

  /**
   * Sets the NAT rule name.
   *
   * @param name The rule name to set
   */
  public void setName(@Nonnull String name) {
    _name = name;
  }

  /**
   * Gets the NAT type.
   *
   * @return The NAT type
   */
  @Nonnull
  public NatType getType() {
    return _type;
  }

  /**
   * Sets the NAT type.
   *
   * @param type The NAT type to set
   */
  public void setType(@Nonnull NatType type) {
    _type = type;
  }

  /**
   * Gets the ACL name.
   *
   * @return The ACL name, or null if not set
   */
  @Nullable
  public String getAclName() {
    return _aclName;
  }

  /**
   * Sets the ACL name.
   *
   * @param aclName The ACL name to set
   */
  public void setAclName(@Nullable String aclName) {
    _aclName = aclName;
  }

  /**
   * Gets the global address pool name.
   *
   * @return The pool name, or null if not set
   */
  @Nullable
  public String getPoolName() {
    return _poolName;
  }

  /**
   * Sets the global address pool name.
   *
   * @param poolName The pool name to set
   */
  public void setPoolName(@Nullable String poolName) {
    _poolName = poolName;
  }

  /**
   * Gets the global IP address.
   *
   * @return The global IP, or null if not set
   */
  @Nullable
  public Ip getGlobalIp() {
    return _globalIp;
  }

  /**
   * Sets the global IP address.
   *
   * @param globalIp The global IP to set
   */
  public void setGlobalIp(@Nullable Ip globalIp) {
    _globalIp = globalIp;
  }

  /**
   * Gets the global port.
   *
   * @return The global port, or null if not set
   */
  @Nullable
  public Integer getGlobalPort() {
    return _globalPort;
  }

  /**
   * Sets the global port.
   *
   * @param globalPort The global port to set
   */
  public void setGlobalPort(@Nullable Integer globalPort) {
    _globalPort = globalPort;
  }

  /**
   * Gets the inside local IP address.
   *
   * @return The inside local IP, or null if not set
   */
  @Nullable
  public Ip getInsideLocalIp() {
    return _insideLocalIp;
  }

  /**
   * Sets the inside local IP address.
   *
   * @param insideLocalIp The inside local IP to set
   */
  public void setInsideLocalIp(@Nullable Ip insideLocalIp) {
    _insideLocalIp = insideLocalIp;
  }

  /**
   * Gets the inside local port.
   *
   * @return The inside local port, or null if not set
   */
  @Nullable
  public Integer getInsideLocalPort() {
    return _insideLocalPort;
  }

  /**
   * Sets the inside local port.
   *
   * @param insideLocalPort The inside local port to set
   */
  public void setInsideLocalPort(@Nullable Integer insideLocalPort) {
    _insideLocalPort = insideLocalPort;
  }

  /**
   * Gets the interface name.
   *
   * @return The interface name, or null if not set
   */
  @Nullable
  public String getInterfaceName() {
    return _interfaceName;
  }

  /**
   * Sets the interface name.
   *
   * @param interfaceName The interface name to set
   */
  public void setInterfaceName(@Nullable String interfaceName) {
    _interfaceName = interfaceName;
  }

  /**
   * Gets the protocol.
   *
   * @return The protocol, or null if not set
   */
  @Nullable
  public String getProtocol() {
    return _protocol;
  }

  /**
   * Sets the protocol.
   *
   * @param protocol The protocol to set
   */
  public void setProtocol(@Nullable String protocol) {
    _protocol = protocol;
  }

  /**
   * Gets the VRF name.
   *
   * @return The VRF name, or null if not set
   */
  @Nullable
  public String getVrfName() {
    return _vrfName;
  }

  /**
   * Sets the VRF name.
   *
   * @param vrfName The VRF name to set
   */
  public void setVrfName(@Nullable String vrfName) {
    _vrfName = vrfName;
  }

  /**
   * Gets the address pool.
   *
   * @return The list of IP addresses in the pool, or null if not set
   */
  @Nullable
  public List<Ip> getAddressPool() {
    return _addressPool;
  }

  /**
   * Sets the address pool.
   *
   * @param addressPool The list of IP addresses to set
   */
  public void setAddressPool(@Nullable List<Ip> addressPool) {
    _addressPool = addressPool;
  }

  /**
   * Adds an IP address to the address pool.
   *
   * @param ip The IP address to add
   */
  public void addAddressPoolIp(@Nonnull Ip ip) {
    if (_addressPool == null) {
      _addressPool = new ArrayList<>();
    }
    _addressPool.add(ip);
  }
}
