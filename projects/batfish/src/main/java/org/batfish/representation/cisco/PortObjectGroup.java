package org.batfish.representation.cisco;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an {@code ip port-group}.
 *
 * <p>This group type is not available on all IOS versions. It is available in at least IOS-XE
 * Version 15.5(1)SY1, RELEASE SOFTWARE (fc6). See
 * https://github.com/batfish/batfish/issues/7681#issuecomment-970130335
 */
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
