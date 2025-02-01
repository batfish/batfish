package org.batfish.vendor.check_point_gateway.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Check Point gateway datamodel component containing interface configuration */
public class Interface implements Serializable {
  public enum LinkSpeed {
    TEN_M_FULL,
    TEN_M_HALF,
    HUNDRED_M_FULL,
    HUNDRED_M_HALF,
    THOUSAND_M_FULL,
  }

  public static final double DEFAULT_ETH_SPEED = 1E9;
  public static final int DEFAULT_INTERFACE_MTU = 1500;
  public static final int DEFAULT_LOOPBACK_MTU = 65536;

  public Interface(String name) {
    _state = true;
    _name = name;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  /**
   * Boolean representation of {@code auto-negotiate}, where {@code true} corresponds to {@code on}.
   */
  public @Nullable Boolean getAutoNegotiate() {
    return _autoNegotiate;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  public @Nullable LinkSpeed getLinkSpeed() {
    return _linkSpeed;
  }

  /**
   * Returns effective link speed in bits per second, or null if not applicable (e.g., this is an
   * aggregated or logical interface)
   */
  public @Nullable Double getLinkSpeedEffective() {
    if (_linkSpeed == null) {
      return getDefaultSpeed(_name);
    }
    return switch (_linkSpeed) {
      case TEN_M_FULL, TEN_M_HALF -> 10E6;
      case HUNDRED_M_FULL, HUNDRED_M_HALF -> 100E6;
      case THOUSAND_M_FULL -> 1000E6;
    };
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  /** Returns the effective MTU for this interface, even if not explicitly configured. */
  public int getMtuEffective() {
    return _mtu != null ? _mtu : getDefaultMtu(_name);
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getParentInterface() {
    return _parentInterface;
  }

  /** Boolean representation of {@code state}, where {@code true} corresponds to {@code on}. */
  public boolean getState() {
    return _state;
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  public void setAutoNegotiate(boolean autoNegotiate) {
    _autoNegotiate = autoNegotiate;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public void setLinkSpeed(LinkSpeed linkSpeed) {
    _linkSpeed = linkSpeed;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setParentInterface(@Nullable String parentInterface) {
    _parentInterface = parentInterface;
  }

  public void setState(boolean state) {
    _state = state;
  }

  public void setVlanId(@Nullable Integer vlanId) {
    _vlanId = vlanId;
  }

  /** Default MTU for an interface with the given name */
  public static int getDefaultMtu(String name) {
    if (name.startsWith("lo")) {
      return DEFAULT_LOOPBACK_MTU;
    }
    return DEFAULT_INTERFACE_MTU;
  }

  /** Default link speed in bits per second for an interface with the given name */
  public static @Nullable Double getDefaultSpeed(String name) {
    // Use default ethernet speed for physical interfaces.
    // Exclude subinterfaces (their speed should equal their parent's speed).
    if (name.startsWith("eth") && !name.contains(".")) {
      return DEFAULT_ETH_SPEED;
    }
    return null;
  }

  private @Nullable ConcreteInterfaceAddress _address;
  private @Nullable Boolean _autoNegotiate;
  private @Nullable String _comments;
  private @Nullable LinkSpeed _linkSpeed;
  private @Nullable Integer _mtu;
  private final @Nonnull String _name;
  private @Nullable String _parentInterface;
  private boolean _state;
  private @Nullable Integer _vlanId;
}
