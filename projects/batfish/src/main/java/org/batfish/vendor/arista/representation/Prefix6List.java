package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Prefix6List implements Serializable {

  private List<Prefix6ListLine> _lines;

  private final String _name;

  public Prefix6List(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public void addLine(Prefix6ListLine r) {
    _lines.add(r);
  }

  public void addLines(List<Prefix6ListLine> r) {
    _lines.addAll(r);
  }

  public List<Prefix6ListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
