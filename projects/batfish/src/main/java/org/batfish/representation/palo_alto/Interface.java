package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;

/** PAN datamodel component containing interface configuration */
public final class Interface implements Serializable {
  public static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final long serialVersionUID = 1L;

  private boolean _active;
  @Nullable private InterfaceAddress _address;
  @Nonnull private final Set<InterfaceAddress> _allAddresses;
  @Nullable private String _comment;
  @Nullable private Integer _mtu;
  @Nonnull private final String _name;
  @Nullable private Interface _parent;
  @Nullable private Integer _tag;
  @Nonnull private final SortedMap<String, Interface> _units;
  @Nullable private Zone _zone;

  public Interface(@Nonnull String name) {
    _active = true;
    _allAddresses = new LinkedHashSet<>();
    _mtu = DEFAULT_INTERFACE_MTU;
    _name = name;
    _units = new TreeMap<>();
  }

  public boolean getActive() {
    return _active;
  }

  @Nullable
  public InterfaceAddress getAddress() {
    return _address;
  }

  @Nonnull
  public Set<InterfaceAddress> getAllAddresses() {
    return _allAddresses;
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

  public void setAddress(@Nullable InterfaceAddress address) {
    _address = address;
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
