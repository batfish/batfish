package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

public class Prefix6List extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private List<Prefix6ListLine> _lines;

  public Prefix6List(String name) {
    super(name);
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
}
