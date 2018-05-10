package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;

abstract class ObjectGroup extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  ObjectGroup(String name, int definitionLine) {
    super(name, definitionLine);
  }
}
