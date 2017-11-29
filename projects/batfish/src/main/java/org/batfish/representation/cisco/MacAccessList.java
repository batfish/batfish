package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.DefinedStructure;

public class MacAccessList extends DefinedStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<MacAccessListLine> _lines;

  public MacAccessList(String name, int definitionLine) {
    super(name, definitionLine);
    _lines = new ArrayList<>();
  }

  public List<MacAccessListLine> getLines() {
    return _lines;
  }
}
