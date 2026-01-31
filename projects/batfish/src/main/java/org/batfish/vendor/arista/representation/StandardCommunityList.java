package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class StandardCommunityList implements Serializable {

  private final List<StandardCommunityListLine> _lines;

  private final String _name;

  public StandardCommunityList(@Nonnull String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public List<StandardCommunityListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
