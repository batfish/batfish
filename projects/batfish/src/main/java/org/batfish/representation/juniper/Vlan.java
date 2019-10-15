package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Vlan implements Serializable {

  private final String _name;

  private @Nonnull Set<String> _interfaces;
  private @Nullable String _l3Interface;
  private @Nullable Integer _vlanId;

  public Vlan(String name) {
    _name = name;
    _interfaces = new HashSet<>(0);
  }

  public void addInterface(String ifname) {
    _interfaces.add(ifname);
  }

  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  public @Nullable String getL3Interface() {
    return _l3Interface;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public void setVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  public void setL3Interface(String l3Interface) {
    _l3Interface = l3Interface;
  }
}
