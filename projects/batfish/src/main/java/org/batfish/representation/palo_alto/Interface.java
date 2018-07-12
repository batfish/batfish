package org.batfish.representation.palo_alto;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.InterfaceAddress;

/** PAN datamodel component containing interface configuration */
public final class Interface extends ComparableStructure<String> {
  public static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final long serialVersionUID = 1L;

  private boolean _active;

  private InterfaceAddress _address;

  private final Set<InterfaceAddress> _allAddresses;

  private String _comment;

  private Integer _mtu;

  private Interface _parent;

  private Integer _tag;

  private final SortedSet<Interface> _units;

  public Interface(String name) {
    super(name);
    _active = true;
    _allAddresses = new LinkedHashSet<>();
    _units = new TreeSet<>();
    _mtu = DEFAULT_INTERFACE_MTU;
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

  public Interface getParent() {
    return _parent;
  }

  public Integer getTag() {
    return _tag;
  }

  public SortedSet<Interface> getUnits() {
    return _units;
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
}
