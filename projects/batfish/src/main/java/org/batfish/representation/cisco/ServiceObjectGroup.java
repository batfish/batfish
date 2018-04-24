package org.batfish.representation.cisco;

import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class ServiceObjectGroup extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private final List<ServiceObjectGroupLine> _lines;

  public ServiceObjectGroup(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new LinkedList<>();
  }

  public List<ServiceObjectGroupLine> getLines() {
    return _lines;
  }
}
