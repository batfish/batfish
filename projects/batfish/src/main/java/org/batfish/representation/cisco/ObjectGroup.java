package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;

abstract class ObjectGroup extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  ObjectGroup(String name) {
    super(name);
  }
}
