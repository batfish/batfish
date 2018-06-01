package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;

public class SecurityZone extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  public SecurityZone(String name, int definitionLine) {
    super(name, definitionLine);
  }
}
