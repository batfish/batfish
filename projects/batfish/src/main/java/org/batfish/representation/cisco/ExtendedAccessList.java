package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ExtendedAccessList implements Serializable {

  @Nonnull private List<ExtendedAccessListLine> _lines;
  @Nonnull private final String _name;
  private StandardAccessList _parent;

  public ExtendedAccessList(@Nonnull String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(@Nonnull ExtendedAccessListLine line) {
    _lines.add(line);
  }

  @Nonnull
  public List<ExtendedAccessListLine> getLines() {
    return _lines;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public StandardAccessList getParent() {
    return _parent;
  }

  public void setParent(StandardAccessList parent) {
    _parent = parent;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (ExtendedAccessListLine line : _lines) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
