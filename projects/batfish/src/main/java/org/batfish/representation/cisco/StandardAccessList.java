package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class StandardAccessList implements Serializable {

  private @Nonnull List<StandardAccessListLine> _lines;
  private final @Nonnull String _name;

  public StandardAccessList(@Nonnull String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(@Nonnull StandardAccessListLine line) {
    _lines.add(line);
  }

  public @Nonnull List<StandardAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public ExtendedAccessList toExtendedAccessList() {
    ExtendedAccessList eal = new ExtendedAccessList(_name);
    eal.setParent(this);
    eal.getLines().clear();
    for (StandardAccessListLine sall : _lines) {
      eal.addLine(sall.toExtendedAccessListLine());
    }
    return eal;
  }
}
