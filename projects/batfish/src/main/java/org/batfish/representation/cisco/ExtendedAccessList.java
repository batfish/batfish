package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExtendedAccessList implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<ExtendedAccessListLine> _lines;

  private final String _name;

  private StandardAccessList _parent;

  public ExtendedAccessList(String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(ExtendedAccessListLine all) {
    _lines.add(all);
  }

  public List<ExtendedAccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public StandardAccessList getParent() {
    return _parent;
  }

  public void setParent(StandardAccessList parent) {
    _parent = parent;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (ExtendedAccessListLine line : _lines) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
