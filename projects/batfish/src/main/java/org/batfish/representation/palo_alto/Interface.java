package org.batfish.representation.palo_alto;

import java.util.LinkedHashSet;
import java.util.Set;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.InterfaceAddress;

/** Datamodel component containing interface configuration */
public class Interface extends ComparableStructure<String> {
  private static final int DEFAULT_INTERFACE_MTU = 1500;

  private static final long serialVersionUID = 1L;

  private boolean _active;

  private InterfaceAddress _address;

  private final Set<InterfaceAddress> _allAddresses;

  private String _comment;

  private Integer _mtu;

  public Interface(String name) {
    super(name);
    _active = true;
    _allAddresses = new LinkedHashSet<>();
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
}
