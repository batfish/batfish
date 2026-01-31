package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class PrefixList implements Serializable {

  private final SortedMap<Long, PrefixListLine> _lines;

  private final String _name;

  public PrefixList(String name) {
    _lines = new TreeMap<>();
    _name = name;
  }

  public void addLine(PrefixListLine r) {
    _lines.put(r.getSeq(), r);
  }

  public SortedMap<Long, PrefixListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }

  public long getNextSeq() {
    if (_lines.isEmpty()) {
      return 10L;
    }
    return _lines.lastKey() + 10L;
  }
}
