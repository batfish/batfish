package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Cumulus AS-path access list. */
@ParametersAreNonnullByDefault
public class IpAsPathAccessList implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull List<IpAsPathAccessListLine> _lines;

  public IpAsPathAccessList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public void addLine(IpAsPathAccessListLine line) {
    _lines.add(line);
  }

  @Nonnull
  public List<IpAsPathAccessListLine> getLines() {
    return _lines;
  }

  @Nonnull
  public String getName() {
    return _name;
  }
}
