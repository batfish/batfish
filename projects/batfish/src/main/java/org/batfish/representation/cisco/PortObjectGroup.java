package org.batfish.representation.cisco;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an 'ip port-group' */
@ParametersAreNonnullByDefault
public final class PortObjectGroup extends ObjectGroup {

  private @Nonnull final List<PortObjectGroupLine> _lines;

  public PortObjectGroup(String name) {
    super(name);
    _lines = new LinkedList<>();
  }

  public @Nonnull List<PortObjectGroupLine> getLines() {
    return _lines;
  }
}
