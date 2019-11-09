package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class Ipv4AccessList implements Serializable {

  @Nonnull private List<Ipv4AccessListLine> _lines;
  @Nonnull private final String _name;

  public Ipv4AccessList(@Nonnull String id) {
    _name = id;
    _lines = new ArrayList<>();
  }

  public void addLine(@Nonnull Ipv4AccessListLine line) {
    _lines.add(line);
  }

  @Nonnull
  public List<Ipv4AccessListLine> getLines() {
    return _lines;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    StringBuilder output = new StringBuilder(super.toString() + "\n" + "Identifier: " + _name);
    for (Ipv4AccessListLine line : _lines) {
      output.append("\n").append(line);
    }
    return output.toString();
  }
}
