package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NamedAsPath implements Serializable {

  private final String _name;

  private final String _regex;

  public NamedAsPath(String name, String regex) {
    _name = name;
    _regex = regex;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getRegex() {
    return _regex;
  }
}
