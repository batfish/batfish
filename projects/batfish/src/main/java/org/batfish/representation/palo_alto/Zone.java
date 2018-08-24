package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public final class Zone implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Set<String> _interfaceNames;

  private final String _name;

  private final Vsys _vsys;

  public Zone(String name, Vsys vsys) {
    _name = name;
    _interfaceNames = new TreeSet<>();
    _vsys = vsys;
  }

  public Set<String> getInterfaceNames() {
    return _interfaceNames;
  }

  public String getName() {
    return _name;
  }

  public Vsys getVsys() {
    return _vsys;
  }
}
