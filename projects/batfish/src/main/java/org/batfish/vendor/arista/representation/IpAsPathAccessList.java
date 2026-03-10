package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** The configuration of {@code s_ipap_access_list}. */
@ParametersAreNonnullByDefault
public class IpAsPathAccessList implements Serializable {

  private final @Nonnull List<IpAsPathAccessListLine> _lines;
  private final @Nonnull String _name;

  public IpAsPathAccessList(String name) {
    _name = name;
    _lines = new ArrayList<>(1);
  }

  public void addLine(IpAsPathAccessListLine line) {
    _lines.add(line);
  }

  public @Nonnull List<IpAsPathAccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
