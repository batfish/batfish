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

  @Nullable
  public ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  /**
   * Boolean representation of {@code auto-negotiate}, where {@code true} corresponds to {@code on}.
   */
  @Nullable
  public Boolean getAutoNegotiate() {
    return _autoNegotiate;
  }

  @Nullable
  public String getComments() {
    return _comments;
  }

  @Nullable
  public LinkSpeed getLinkSpeed() {
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
    switch (_linkSpeed) {
      case TEN_M_FULL:
      case TEN_M_HALF:
        return 10E6;
      case HUNDRED_M_FULL:
      case HUNDRED_M_HALF:
        return 100E6;
      case THOUSAND_M_FULL:
        return 1000E6;
      default:
        throw new IllegalStateException("Unsupported link speed " + _linkSpeed);
    }
  }

  @Nullable
  public Integer getMtu() {
    return _mtu;
  }

  /** Returns the effective MTU for this interface, even if not explicitly configured. */
  public int getMtuEffective() {
    return _mtu != null ? _mtu : getDefaultMtu(_name);
  }

  @Nonnull
  public String getName() {
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

  @Nullable private ConcreteInterfaceAddress _address;
  @Nullable private Boolean _autoNegotiate;
  @Nullable private String _comments;
  @Nullable private LinkSpeed _linkSpeed;
  @Nullable private Integer _mtu;
  @Nonnull private final String _name;
  @Nullable private String _parentInterface;
  private boolean _state;
  @Nullable private Integer _vlanId;
}
