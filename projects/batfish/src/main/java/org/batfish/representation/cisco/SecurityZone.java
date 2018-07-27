package org.batfish.representation.cisco;

import java.io.Serializable;

public class SecurityZone implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public SecurityZone(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
