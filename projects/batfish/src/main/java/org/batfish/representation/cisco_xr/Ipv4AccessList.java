package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

public final class Ipv4AccessList implements Serializable {

  private final @Nonnull SortedMap<Long, Ipv4AccessListLine> _lines;
  @Nonnull private final String _name;

  public Ipv4AccessList(@Nonnull String id) {
    _name = id;
    _lines = new TreeMap<>();
  }

  public void addLine(@Nonnull Ipv4AccessListLine line) {
    _lines.put(line.getSeq(), line);
  }

  public @Nonnull SortedMap<Long, Ipv4AccessListLine> getLines() {
    return _lines;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public long getNextSeq() {
    if (_lines.isEmpty()) {
      return 10L;
    }
    return _lines.lastKey() + 10L;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (Ipv4AccessListLine line : _lines.values()) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
