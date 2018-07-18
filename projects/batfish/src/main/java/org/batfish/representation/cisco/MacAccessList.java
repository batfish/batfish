package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MacAccessList implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<MacAccessListLine> _lines;

  private final String _name;

  public MacAccessList(String name) {
    _lines = new ArrayList<>();
    _name = name;
  }

  public List<MacAccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
