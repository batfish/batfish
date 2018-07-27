package org.batfish.representation.cisco;

import java.io.Serializable;

abstract class ObjectGroup implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  ObjectGroup(String name) {
    _name = name;
  }

  public final String getName() {
    return _name;
  }
}
