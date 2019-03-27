package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A physical or logical interface */
public class Interface implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;

  public Interface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
