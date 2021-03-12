package org.batfish.representation.cisco_asa;

import java.io.Serializable;

public class SecurityZone implements Serializable {

  private final String _name;

  public SecurityZone(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
