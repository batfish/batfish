package org.batfish.representation.arista;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class StandardAccessList implements Serializable {

  @Nonnull private List<StandardAccessListLine> _lines;
  @Nonnull private final String _name;

  public StandardAccessList(@Nonnull String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(@Nonnull StandardAccessListLine line) {
    _lines.add(line);
  }

  @Nonnull
  public List<StandardAccessListLine> getLines() {
    return _lines;
  }

  @Nonnull
  public String getName() {
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
