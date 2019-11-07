package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrefixList implements Serializable {

  private List<PrefixListLine> _lines;

  private final String _name;

  public PrefixList(String name) {
    _lines = new ArrayList<>();
    _name = name;
  }

  public void addLine(PrefixListLine r) {
    _lines.add(r);
  }

  public void addLines(List<PrefixListLine> r) {
    _lines.addAll(r);
  }

  public List<PrefixListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
