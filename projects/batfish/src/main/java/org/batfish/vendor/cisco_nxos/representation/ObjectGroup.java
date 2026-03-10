package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** A named group of IPv4/IPv6 addresses or TCP/UDP ports. */
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
