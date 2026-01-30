package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** PAN datamodel component containing interface configuration */
@ParametersAreNonnullByDefault
public final class Interface implements Serializable {
  public enum Type {
    AGGREGATED_ETHERNET,
    LAYER2,
    LAYER3,
    LOOPBACK,
    PHYSICAL,
    TUNNEL,
    TUNNEL_UNIT,
    VLAN,
    VLAN_UNIT,
  }

  public static final int DEFAULT_INTERFACE_MTU = 1500;

  private boolean _active;
  private @Nullable InterfaceAddress _address;
  private @Nullable String _aggregateGroup;
  private final @Nonnull Set<InterfaceAddress> _allAddresses;
  private @Nullable String _comment;
  private @Nullable Boolean _ha;
  private @Nullable Boolean _lldpEnabled;
  private @Nullable Integer _mtu;
  private final @Nonnull String _name;
  private @Nullable Boolean _ndpProxy;
  private @Nullable Interface _parent;
  private @Nullable Boolean _routerAdvertisement;
  private @Nullable Integer _tag;
  private final @Nonnull Type _type;
  private final @Nonnull SortedMap<String, Interface> _units;
  private @Nullable Zone _zone;

  public Interface(String name, Type type) {
    _active = true;
    _allAddresses = new LinkedHashSet<>();
    _mtu = DEFAULT_INTERFACE_MTU;
    _name = name;
    _type = type;
    _units = new TreeMap<>();
  }

  public boolean getActive() {
    return _active;
  }

  public void addAddress(InterfaceAddress address) {
    if (_address == null) {
      _address = address;
    }
    _allAddresses.add(address);
  }

  public @Nullable InterfaceAddress getAddress() {
    return _address;
  }

  public @Nullable String getAggregateGroup() {
    return _aggregateGroup;
  }

  public void setAggregateGroup(@Nullable String aggregateGroup) {
    _aggregateGroup = aggregateGroup;
  }

  public @Nonnull Set<InterfaceAddress> getAllAddresses() {
    return Collections.unmodifiableSet(_allAddresses);
  }

  public @Nullable String getComment() {
    return _comment;
  }

  public @Nullable Boolean getHa() {
    return _ha;
  }

  public void setHa(@Nullable Boolean ha) {
    _ha = ha;
  }

  public @Nullable Boolean getLldpEnabled() {
    return _lldpEnabled;
  }

  public void setLldpEnabled(@Nullable Boolean lldpEnabled) {
    _lldpEnabled = lldpEnabled;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getNdpProxy() {
    return _ndpProxy;
  }

  public void setNdpProxy(@Nullable Boolean ndpProxy) {
    _ndpProxy = ndpProxy;
  }

  public @Nullable Interface getParent() {
    return _parent;
  }

  public @Nullable Boolean getRouterAdvertisement() {
    return _routerAdvertisement;
  }

  public void setRouterAdvertisement(@Nullable Boolean routerAdvertisement) {
    _routerAdvertisement = routerAdvertisement;
  }

  public @Nullable Integer getTag() {
    return _tag;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nonnull SortedMap<String, Interface> getUnits() {
    return _units;
  }

  public @Nullable Zone getZone() {
    return _zone;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setComment(@Nullable String comment) {
    _comment = comment;
  }

  public void setMtu(@Nullable Integer mtu) {
    _mtu = mtu;
  }

  public void setParent(@Nullable Interface parent) {
    _parent = parent;
  }

  public void setTag(@Nullable Integer tag) {
    _tag = tag;
  }

  public void setZone(@Nullable Zone zone) {
    _zone = zone;
  }
}
