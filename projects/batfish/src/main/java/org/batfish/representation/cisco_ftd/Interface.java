package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Represents a Cisco FTD interface configuration */
public class Interface implements Serializable {

  public Interface(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public boolean getActive() {
    return _active;
  }

  public void setAddress(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setNameif(@Nullable String nameif) {
    _nameif = nameif;
  }

  public @Nullable String getNameif() {
    return _nameif;
  }

  public void setSecurityLevel(@Nullable Integer securityLevel) {
    _securityLevel = securityLevel;
  }

  public @Nullable Integer getSecurityLevel() {
    return _securityLevel;
  }

  public void setVlan(@Nullable Integer vlan) {
    _vlan = vlan;
  }

  public @Nullable Integer getVlan() {
    return _vlan;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setMtu(@Nullable Integer mtu) {
    _mtu = mtu;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  private final String _name;
  private @Nullable String _description;
  private boolean _active = true; // Interfaces are active by default
  private @Nullable ConcreteInterfaceAddress _address;
  private @Nullable String _nameif;
  private @Nullable Integer _securityLevel;
  private @Nullable Integer _vlan;
  private @Nullable String _vrf;
  private @Nullable Integer _mtu;
}
