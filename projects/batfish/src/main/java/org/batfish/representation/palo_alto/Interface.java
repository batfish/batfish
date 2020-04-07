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
import org.batfish.datamodel.ConcreteInterfaceAddress;

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
    VLAN,
  }

  public static final int DEFAULT_INTERFACE_MTU = 1500;

  private boolean _active;
  @Nullable private ConcreteInterfaceAddress _address;
  @Nullable private String _aggregateGroup;
  @Nonnull private final Set<ConcreteInterfaceAddress> _allAddresses;
  @Nullable private String _comment;
  @Nullable private Integer _mtu;
  @Nonnull private final String _name;
  @Nullable private Interface _parent;
  @Nullable private Integer _tag;
  @Nonnull private final Type _type;
  @Nonnull private final SortedMap<String, Interface> _units;
  @Nullable private Zone _zone;

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

  public void addAddress(ConcreteInterfaceAddress address) {
    if (_address == null) {
      _address = address;
    }
    _allAddresses.add(address);
  }

  @Nullable
  public ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  @Nullable
  public String getAggregateGroup() {
    return _aggregateGroup;
  }

  public void setAggregateGroup(@Nullable String aggregateGroup) {
    _aggregateGroup = aggregateGroup;
  }

  @Nonnull
  public Set<ConcreteInterfaceAddress> getAllAddresses() {
    return Collections.unmodifiableSet(_allAddresses);
  }

  @Nullable
  public String getComment() {
    return _comment;
  }

  @Nullable
  public Integer getMtu() {
    return _mtu;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Interface getParent() {
    return _parent;
  }

  @Nullable
  public Integer getTag() {
    return _tag;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  @Nonnull
  public SortedMap<String, Interface> getUnits() {
    return _units;
  }

  @Nullable
  public Zone getZone() {
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
