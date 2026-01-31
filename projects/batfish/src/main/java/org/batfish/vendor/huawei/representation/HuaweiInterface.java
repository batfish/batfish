package org.batfish.vendor.huawei.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/**
 * Represents a network interface on a Huawei VRP device.
 *
 * <p>This class stores the configuration of an interface including its address, description,
 * administrative status, and other interface-specific settings.
 */
public class HuaweiInterface implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Interface name (e.g., "GigabitEthernet0/0/1") */
  @Nonnull private String _name;

  /** Description of the interface */
  @Nullable private String _description;

  /** Interface IP address and netmask */
  @Nullable private ConcreteInterfaceAddress _address;

  /** Administrative status of the interface */
  private boolean _shutdown;

  /** MTU of the interface */
  private int _mtu;

  /** Bandwidth of the interface in bps */
  @Nullable private Double _bandwidth;

  /** ACL applied to inbound traffic */
  @Nullable private String _incomingFilter;

  /** ACL applied to outbound traffic */
  @Nullable private String _outgoingFilter;

  /** Set of IP addresses for DHCP relay */
  private SortedSet<Ip> _dhcpRelayAddresses;

  /** Whether this interface is a DHCP relay client */
  private boolean _dhcpRelayClient;

  public HuaweiInterface(@Nonnull String name) {
    _name = name;
    _shutdown = false;
    _mtu = 1500; // Default MTU
    _dhcpRelayAddresses = new TreeSet<>();
    _dhcpRelayClient = false;
  }

  /**
   * Gets the interface name.
   *
   * @return The interface name
   */
  @Nonnull
  public String getName() {
    return _name;
  }

  /**
   * Sets the interface name.
   *
   * @param name The interface name
   */
  public void setName(@Nonnull String name) {
    _name = name;
  }

  /**
   * Gets the interface description.
   *
   * @return The description, or null if not set
   */
  @Nullable
  public String getDescription() {
    return _description;
  }

  /**
   * Sets the interface description.
   *
   * @param description The description to set
   */
  public void setDescription(@Nullable String description) {
    _description = description;
  }

  /**
   * Gets the interface IP address.
   *
   * @return The interface address, or null if not set
   */
  @Nullable
  public ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  /**
   * Sets the interface IP address.
   *
   * @param address The interface address to set
   */
  public void setAddress(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  /**
   * Checks if the interface is administratively shutdown.
   *
   * @return true if the interface is shutdown, false otherwise
   */
  public boolean getShutdown() {
    return _shutdown;
  }

  /**
   * Sets the administrative status of the interface.
   *
   * @param shutdown true to shutdown the interface, false to enable it
   */
  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  /**
   * Gets the MTU of the interface.
   *
   * @return The MTU value
   */
  public int getMtu() {
    return _mtu;
  }

  /**
   * Sets the MTU of the interface.
   *
   * @param mtu The MTU value to set
   */
  public void setMtu(int mtu) {
    _mtu = mtu;
  }

  /**
   * Gets the bandwidth of the interface.
   *
   * @return The bandwidth in bps, or null if not set
   */
  @Nullable
  public Double getBandwidth() {
    return _bandwidth;
  }

  /**
   * Sets the bandwidth of the interface.
   *
   * @param bandwidth The bandwidth in bps
   */
  public void setBandwidth(@Nullable Double bandwidth) {
    _bandwidth = bandwidth;
  }

  /**
   * Gets the ACL applied to inbound traffic.
   *
   * @return The ACL name, or null if not set
   */
  @Nullable
  public String getIncomingFilter() {
    return _incomingFilter;
  }

  /**
   * Sets the ACL applied to inbound traffic.
   *
   * @param incomingFilter The ACL name
   */
  public void setIncomingFilter(@Nullable String incomingFilter) {
    _incomingFilter = incomingFilter;
  }

  /**
   * Gets the ACL applied to outbound traffic.
   *
   * @return The ACL name, or null if not set
   */
  @Nullable
  public String getOutgoingFilter() {
    return _outgoingFilter;
  }

  /**
   * Sets the ACL applied to outbound traffic.
   *
   * @param outgoingFilter The ACL name
   */
  public void setOutgoingFilter(@Nullable String outgoingFilter) {
    _outgoingFilter = outgoingFilter;
  }

  /**
   * Gets the set of DHCP relay addresses.
   *
   * @return A sorted set of IP addresses
   */
  @Nonnull
  public SortedSet<Ip> getDhcpRelayAddresses() {
    return _dhcpRelayAddresses;
  }

  /**
   * Sets the DHCP relay addresses.
   *
   * @param dhcpRelayAddresses The sorted set of IP addresses
   */
  public void setDhcpRelayAddresses(@Nonnull SortedSet<Ip> dhcpRelayAddresses) {
    _dhcpRelayAddresses = dhcpRelayAddresses;
  }

  /**
   * Adds a DHCP relay address.
   *
   * @param address The IP address to add
   */
  public void addDhcpRelayAddress(Ip address) {
    _dhcpRelayAddresses.add(address);
  }

  /**
   * Checks if this interface is a DHCP relay client.
   *
   * @return true if DHCP relay client is enabled, false otherwise
   */
  public boolean getDhcpRelayClient() {
    return _dhcpRelayClient;
  }

  /**
   * Sets whether this interface is a DHCP relay client.
   *
   * @param dhcpRelayClient true to enable DHCP relay client, false to disable
   */
  public void setDhcpRelayClient(boolean dhcpRelayClient) {
    _dhcpRelayClient = dhcpRelayClient;
  }

  /**
   * Gets the default bandwidth for an interface based on its name.
   *
   * @param name The interface name
   * @return The default bandwidth in bps, or null if no default is available
   */
  public static Double getDefaultBandwidth(String name) {
    if (name.startsWith("GigabitEthernet")) {
      return 1E9D;
    } else if (name.startsWith("10GE")) {
      return 10E9D;
    } else if (name.startsWith("25GE")) {
      return 25E9D;
    } else if (name.startsWith("40GE")) {
      return 40E9D;
    } else if (name.startsWith("100GE")) {
      return 100E9D;
    } else if (name.startsWith("Ethernet")) {
      return 100E6D; // FastEthernet default
    } else if (name.startsWith("Loopback")) {
      return 8E9D; // High bandwidth for loopback
    } else if (name.startsWith("Vlanif")) {
      return 1E9D;
    } else if (name.startsWith("Pos")) {
      return 155E6D; // OC-3 default
    } else if (name.startsWith("Serial")) {
      return 1.544E6D; // T1 default
    } else if (name.startsWith("Tunnel")) {
      return 100E3D;
    } else {
      // Unknown interface type
      return null;
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("name", _name)
        .add("description", _description)
        .add("address", _address)
        .add("shutdown", _shutdown)
        .add("mtu", _mtu)
        .toString();
  }
}
