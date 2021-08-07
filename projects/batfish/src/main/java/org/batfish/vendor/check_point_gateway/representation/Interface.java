package org.batfish.vendor.check_point_gateway.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

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

  public static final int DEFAULT_INTERFACE_MTU = 1500;

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

  @Nullable
  public Integer getMtu() {
    return _mtu;
  }

  /** Returns the effective MTU for this interface, even if not explicitly configured. */
  public int getMtuEffective() {
    return firstNonNull(_mtu, DEFAULT_INTERFACE_MTU);
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
