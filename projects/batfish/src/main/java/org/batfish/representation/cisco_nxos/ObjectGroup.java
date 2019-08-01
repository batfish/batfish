package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nonnull;

public abstract class ObjectGroup implements Serializable {

  protected ObjectGroup(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public abstract <T> T accept(ObjectGroupVisitor<T> visitor);

  private final String _name;
}
