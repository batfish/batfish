package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public final class StandardCommunityList implements Serializable {

  private final List<StandardCommunityListLine> _lines;

  private final String _name;

  public StandardCommunityList(@Nonnull String name) {
    this._name = name;
    _lines = new ArrayList<>();
  }

  public List<StandardCommunityListLine> getLines() {
    return _lines;
  }

  public String getName() {
    return _name;
  }
}
