package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** FortiOS datamodel component containing interface configuration */
public final class Interface implements InterfaceOrZone, Serializable {
  public enum Type {
    AGGREGATE,
    EMAC_VLAN,
    LOOPBACK,
    PHYSICAL,
    REDUNDANT,
    TUNNEL,
    VLAN,
    WL_MESH;
  }

  public enum Status {
    UP,
    DOWN,
    UNKNOWN,
  }

  public static final int DEFAULT_INTERFACE_MTU = 1500;
  public static final int DEFAULT_VRF = 0;
  public static final Type DEFAULT_TYPE = Type.VLAN;
  public static final boolean DEFAULT_STATUS = true;

  @Override
  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public String getAlias() {
    return _alias;
  }

  @Nullable
  public String getVdom() {
    return _vdom;
  }

  @Nullable
  public ConcreteInterfaceAddress getIp() {
    return _ip;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public @Nonnull Type getTypeEffective() {
    return firstNonNull(_type, DEFAULT_TYPE);
  }

  @VisibleForTesting
  public Status getStatus() {
    return _status;
  }

  /**
   * Get the effective status of the interface, inferring the value even if not explicitly
   * configured. If {@code true}, that interface is up, if {@code false} the interface is down.
   */
  public boolean getStatusEffective() {
    return _status == Status.UNKNOWN ? DEFAULT_STATUS : _status == Status.UP;
  }

  @VisibleForTesting
  @Nullable
  public Integer getMtu() {
    return _mtu;
  }

  /**
   * Get the effective mtu of the interface, inferring the value even if not explicitly configured.
   */
  public int getMtuEffective() {
    return _mtu == null || _mtuOverride == null || !_mtuOverride ? DEFAULT_INTERFACE_MTU : _mtu;
  }

  @VisibleForTesting
  @Nullable
  public Boolean getMtuOverride() {
    return _mtuOverride;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  @VisibleForTesting
  @Nullable
  public Integer getVrf() {
    return _vrf;
  }

  /**
   * Get the effective vrf of the interface, inferring the value even if not explicitly configured.
   */
  public int getVrfEffective() {
    return _vrf == null ? DEFAULT_VRF : _vrf;
  }

  public void setAlias(String alias) {
    _alias = alias;
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

  public void setStatus(Status status) {
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
    _status = Status.UNKNOWN;
  }

  @Nonnull private final String _name;
  @Nullable private String _alias;
  @Nullable private String _vdom;
  @Nullable private ConcreteInterfaceAddress _ip;
  @Nullable private Type _type;
  @Nonnull private Status _status;
  @Nullable private Boolean _mtuOverride;
  @Nullable private Integer _mtu;
  @Nullable private String _description;
  @Nullable private Integer _vrf;
}
