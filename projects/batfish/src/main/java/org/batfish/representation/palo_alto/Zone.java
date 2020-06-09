package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.Zone.Type.LAYER3;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public final class Zone implements Serializable {
  /**
   * The type of the zone.
   *
   * <p>Zone and interface types must match: "How do zones protect the network?" (PAN-OS®
   * Administrator’s Guide Version 8.1, page 1140 ).
   */
  public enum Type {
    EXTERNAL,
    LAYER2,
    LAYER3,
    TAP,
    TUNNEL,
    VIRTUAL_WIRE,
  }

  private final Set<String> _externalNames;

  private final Set<String> _interfaceNames;

  private final String _name;

  private Type _type;

  private Vsys _vsys;

  public Zone(String name, Vsys vsys) {
    _name = name;
    _externalNames = new HashSet<>();
    _interfaceNames = new TreeSet<>();
    _type = LAYER3; // `set vsys vsys1 zone foo` - it shows up as Layer3 in the GUI
    _vsys = vsys;
  }

  public Set<String> getExternalNames() {
    return _externalNames;
  }

  public Set<String> getInterfaceNames() {
    return _interfaceNames;
  }

  public String getName() {
    return _name;
  }

  public Type getType() {
    return _type;
  }

  public void setType(Type type) {
    _type = type;
  }

  public Vsys getVsys() {
    return _vsys;
  }

  public void setVsys(Vsys vsys) {
    _vsys = vsys;
  }
}
