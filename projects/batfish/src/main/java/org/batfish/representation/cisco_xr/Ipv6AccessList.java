package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ipv6AccessList implements Serializable {

  private List<Ipv6AccessListLine> _lines;

  private final String _name;

  public Ipv6AccessList(String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(Ipv6AccessListLine all) {
    _lines.add(all);
  }

  public List<Ipv6AccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (Ipv6AccessListLine line : _lines) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
