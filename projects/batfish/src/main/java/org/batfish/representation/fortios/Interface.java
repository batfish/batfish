package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** FortiOS datamodel component containing interface configuration */
public final class Interface implements Serializable {
  public enum Type {
    AGGREGATE,
    EMAC_VLAN,
    LOOPBACK,
    PHYSICAL,
    REDUNDANT,
    TUNNEL,
    UNKNOWN,
    VLAN,
    WL_MESH,
  }

  public static final int DEFAULT_INTERFACE_MTU = 1500;
  public static final int DEFAULT_VRF = 0;
  public static final boolean DEFAULT_STATUS = true;

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public String getVdom() {
    return _vdom;
  }

  @Nullable
  public ConcreteInterfaceAddress getIp() {
    return _ip;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  @Nullable
  public Boolean getStatus() {
    return _status;
  }

  /**
   * Get the effective status of the interface, inferring the value even if not explicitly
   * configured. If {@code true}, that interface is up, if {@code false} the interface is down.
   */
  public boolean getStatusEffective() {
    return _status == null ? DEFAULT_STATUS : _status;
  }

  /**
   * Get the effective mtu of the interface, inferring the value even if not explicitly configured.
   */
  public int getMtuEffective() {
    return _mtu == null || _mtuOverride == null ? DEFAULT_INTERFACE_MTU : _mtu;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  /**
   * Get the effective vrf of the interface, inferring the value even if not explicitly configured.
   */
  public int getVrfEffective() {
    return _vrf == null ? DEFAULT_VRF : _vrf;
  }

  public void setVdom(String vdom) {
    _vdom = vdom;
  }

  public void setIp(ConcreteInterfaceAddress ip) {
    _ip = ip;
  }

  public void setType(Type type) {
    _type = type;
  }

  public void setStatus(boolean status) {
    _status = status;
  }

  public void setMtuOverride(boolean mtuOverride) {
    _mtuOverride = mtuOverride;
  }

  public void setMtu(int mtu) {
    _mtu = mtu;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setVrf(int vrf) {
    _vrf = vrf;
  }

  public Interface(String name) {
    _name = name;
    _type = Type.UNKNOWN;
  }

  @Nonnull private final String _name;
  @Nullable private String _vdom;
  @Nullable private ConcreteInterfaceAddress _ip;
  @Nonnull private Type _type;
  @Nullable private Boolean _status;
  @Nullable private Boolean _mtuOverride;
  @Nullable private Integer _mtu;
  @Nullable private String _description;
  @Nullable private Integer _vrf;
}
