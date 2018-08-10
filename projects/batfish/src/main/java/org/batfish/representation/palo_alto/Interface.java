package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.InterfaceAddress;

/** PAN datamodel component containing interface configuration */
public final class Interface implements Serializable {
  public static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final long serialVersionUID = 1L;

  private boolean _active;

  private InterfaceAddress _address;

  private final Set<InterfaceAddress> _allAddresses;

  private String _comment;

  private Integer _mtu;

  private final String _name;

  private Interface _parent;

  private Integer _tag;

  private final SortedMap<String, Interface> _units;

  private Zone _zone;

  public Interface(String name) {
    _active = true;
    _allAddresses = new LinkedHashSet<>();
    _mtu = DEFAULT_INTERFACE_MTU;
    _name = name;
    _units = new TreeMap<>();
  }

  public boolean getActive() {
    return _active;
  }

  public InterfaceAddress getAddress() {
    return _address;
  }

  public Set<InterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  public String getComment() {
    return _comment;
  }

  public Integer getMtu() {
    return _mtu;
  }

  public String getName() {
    return _name;
  }

  public Interface getParent() {
    return _parent;
  }

  public Integer getTag() {
    return _tag;
  }

  public SortedMap<String, Interface> getUnits() {
    return _units;
  }

  public Zone getZone() {
    return _zone;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public void setAddress(InterfaceAddress address) {
    _address = address;
  }

  public void setComment(String comment) {
    _comment = comment;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setParent(Interface parent) {
    _parent = parent;
  }

  public void setTag(Integer tag) {
    _tag = tag;
  }

  public void setZone(Zone zone) {
    _zone = zone;
  }
}
