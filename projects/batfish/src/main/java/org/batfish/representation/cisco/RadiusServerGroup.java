package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AAA RADIUS group */
@ParametersAreNonnullByDefault
public final class RadiusServerGroup extends AaaServerGroup {

  public RadiusServerGroup(String name) {
    super(name);
  }
}
