package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AAA Tacacs+ group */
@ParametersAreNonnullByDefault
public final class TacacsPlusServerGroup extends AaaServerGroup {

  public TacacsPlusServerGroup(String name) {
    super(name);
  }
}
