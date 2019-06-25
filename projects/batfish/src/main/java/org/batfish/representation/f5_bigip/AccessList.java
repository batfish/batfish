package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An access-control list for filtering packets when applied to an interface, or for filtering
 * routes by prefix when used as a match condition of a route-map.
 */
@ParametersAreNonnullByDefault
public class AccessList implements Serializable {

  private final List<AccessListLine> _lines;
  private final @Nonnull String _name;

  public AccessList(String name) {
    _name = name;
    _lines = new ArrayList<>();
  }

  public @Nonnull List<AccessListLine> getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
