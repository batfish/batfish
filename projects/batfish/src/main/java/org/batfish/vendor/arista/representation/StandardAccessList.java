package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

public class StandardAccessList implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull SortedMap<Long, StandardAccessListLine> _lines;

  public StandardAccessList(@Nonnull String id) {
    _name = id;
    _lines = new TreeMap<>();
  }

  public void addLine(@Nonnull StandardAccessListLine line) {
    _lines.put(line.getSeq(), line);
  }

  public @Nonnull SortedMap<Long, StandardAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public ExtendedAccessList toExtendedAccessList() {
    ExtendedAccessList eal = new ExtendedAccessList(_name);
    eal.setParent(this);
    eal.getLines().clear();
    _lines.forEach((seq, line) -> line.toExtendedAccessListLine().ifPresent(eal::addLine));
    return eal;
  }
}
