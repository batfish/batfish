package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IpAsPathAccessList implements Serializable {

  private List<IpAsPathAccessListLine> _lines;

  private final String _name;

  public IpAsPathAccessList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public void addLine(IpAsPathAccessListLine line) {
    _lines.add(line);
  }

  public List<IpAsPathAccessListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
