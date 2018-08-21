package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StandardAccessList implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<StandardAccessListLine> _lines;

  private final String _name;

  public StandardAccessList(String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(StandardAccessListLine all) {
    _lines.add(all);
  }

  public List<StandardAccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public ExtendedAccessList toExtendedAccessList() {
    ExtendedAccessList eal = new ExtendedAccessList(_name);
    eal.setParent(this);
    eal.getLines().clear();
    for (StandardAccessListLine sall : _lines) {
      eal.addLine(sall.toExtendedAccessListLine());
    }
    return eal;
  }
}
