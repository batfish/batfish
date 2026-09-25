package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A VLAN configured within a MAC-VRF routing instance. */
@ParametersAreNonnullByDefault
public final class MacVrfVlan implements Serializable {

  private @Nullable String _description;
  private @Nullable String _l3Interface;
  private final String _name;
  private @Nullable Integer _vlanId;
  private @Nullable Integer _vniId;

  public MacVrfVlan(String name) {
    _name = name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable String getL3Interface() {
    return _l3Interface;
  }

  public String getName() {
    return _name;
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public @Nullable Integer getVniId() {
    return _vniId;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setL3Interface(String l3Interface) {
    _l3Interface = l3Interface;
  }

  public void setVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  public void setVniId(int vniId) {
    _vniId = vniId;
  }
}
