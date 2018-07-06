package org.batfish.representation.palo_alto;

import java.io.Serializable;
import org.batfish.datamodel.LineAction;

public final class Rule implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private final String _name;

  public Rule(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
