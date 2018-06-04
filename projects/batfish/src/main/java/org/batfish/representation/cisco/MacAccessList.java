package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public class MacAccessList extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<MacAccessListLine> _lines;

  public MacAccessList(String name) {
    super(name);
    _lines = new ArrayList<>();
  }

  public List<MacAccessListLine> getLines() {
    return _lines;
  }
}
