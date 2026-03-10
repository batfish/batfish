package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExtendedIpv6AccessList implements Serializable {

  private List<ExtendedIpv6AccessListLine> _lines;

  private final String _name;

  private StandardIpv6AccessList _parent;

  public ExtendedIpv6AccessList(String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(ExtendedIpv6AccessListLine all) {
    _lines.add(all);
  }

  public List<ExtendedIpv6AccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public StandardIpv6AccessList getParent() {
    return _parent;
  }

  public void setParent(StandardIpv6AccessList parent) {
    _parent = parent;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (ExtendedIpv6AccessListLine line : _lines) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
