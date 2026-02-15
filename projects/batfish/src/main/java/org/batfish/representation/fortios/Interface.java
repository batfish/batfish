package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

  public enum Speed {
    AUTO,
    TEN_FULL,
    TEN_HALF,
    HUNDRED_FULL,
    HUNDRED_HALF,
    THOUSAND_FULL,
    THOUSAND_HALF,
    TEN_THOUSAND_FULL,
    TEN_THOUSAND_HALF,
    HUNDRED_AUTO,
    THOUSAND_AUTO,
    TWO_THOUSAND_FIVE_HUNDRED_AUTO,
    FIVE_THOUSAND_AUTO,
    TEN_THOUSAND_AUTO,
    TWENTY_FIVE_THOUSAND_FULL,
    TWENTY_FIVE_THOUSAND_AUTO,
    FORTY_THOUSAND_FULL,
    FORTY_THOUSAND_AUTO,
    FIFTY_THOUSAND_FULL,
    FIFTY_THOUSAND_AUTO,
    HUNDRED_GAUTO,
    TWO_HUNDRED_GFULL,
    TWO_HUNDRED_GAUTO,
    FOUR_HUNDRED_G_FULL,
    FOUR_HUNDRED_G_AUTO,
    HUNDRED_GFULL,
    HUNDRED_GHALF,
  }

  public enum Status {
    UP,
    DOWN,
    UNKNOWN,
  }

  public static final int DEFAULT_INTERFACE_MTU = 1500;
  public static final boolean DEFAULT_SECONDARY_IP_ENABLED = false;
  public static final Speed DEFAULT_SPEED = Speed.AUTO;
  public static final int DEFAULT_VRF = 0;
  public static final Type DEFAULT_TYPE = Type.VLAN;
  public static final boolean DEFAULT_STATUS = true;

  @Override
  public <T> T accept(InterfaceOrZoneVisitor<T> visitor) {
    return visitor.visitInterface(this);
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getAlias() {
    return _alias;
  }

  public @Nullable String getVdom() {
    return _vdom;
  }

  public @Nullable ConcreteInterfaceAddress getIp() {
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
  public @Nullable Integer getMtu() {
    return _mtu;
  }

  /**
   * Get the effective mtu of the interface, inferring the value even if not explicitly configured.
   */
  public int getMtuEffective() {
    return _mtu == null || _mtuOverride == null || !_mtuOverride ? DEFAULT_INTERFACE_MTU : _mtu;
  }

  @VisibleForTesting
  public @Nullable Boolean getMtuOverride() {
    return _mtuOverride;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable String getInterface() {
    return _interface;
  }

  public @Nonnull Set<String> getMembers() {
    return _members;
  }

  public @Nullable Boolean getSecondaryIp() {
    return _secondaryIp;
  }

  /**
   * Get the effective secondaryip enabled-status of the interface, inferring the value even if not
   * explicitly configured.
   */
  public boolean getSecondaryIpEffective() {
    return firstNonNull(_secondaryIp, DEFAULT_SECONDARY_IP_ENABLED);
  }

  public @Nonnull Map<String, SecondaryIp> getSecondaryip() {
    return _secondaryip;
  }

  public @Nullable Speed getSpeed() {
    return _speed;
  }

  public @Nonnull Speed getSpeedEffective() {
    return firstNonNull(_speed, DEFAULT_SPEED);
  }

  public @Nullable Integer getVlanid() {
    return _vlanid;
  }

  @VisibleForTesting
  public @Nullable Integer getVrf() {
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

  public void setInterface(String iface) {
    _interface = iface;
  }

  public void setSecondaryIp(Boolean secondaryIp) {
    _secondaryIp = secondaryIp;
  }

  public void setSpeed(Speed speed) {
    _speed = speed;
  }

  public void setVlanid(int vlanid) {
    _vlanid = vlanid;
  }

  public void setVrf(int vrf) {
    _vrf = vrf;
  }

  public Interface(String name) {
    _name = name;
    _status = Status.UNKNOWN;

    _secondaryip = new HashMap<>();
    _members = new HashSet<>();
  }

  private final @Nonnull String _name;
  private @Nullable String _alias;
  private @Nullable String _vdom;
  private @Nullable ConcreteInterfaceAddress _ip;
  private @Nullable Type _type;
  private @Nonnull Status _status;
  private @Nullable Boolean _mtuOverride;
  private @Nullable Integer _mtu;
  private @Nullable String _description;
  private @Nullable String _interface;
  private final @Nonnull Set<String> _members;

  /** Boolean indicating if secondary-IP is enabled, i.e. if secondaryip can be populated */
  private @Nullable Boolean _secondaryIp;

  /** Map of name/number to {@code SecondaryIp} */
  private @Nonnull Map<String, SecondaryIp> _secondaryip;

  private @Nullable Speed _speed;
  private @Nullable Integer _vlanid;
  private @Nullable Integer _vrf;
}
