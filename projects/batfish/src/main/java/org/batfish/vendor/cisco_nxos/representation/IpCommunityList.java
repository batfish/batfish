package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An access-list matches a route's standard communities attribute against some criteria. */
public abstract class IpCommunityList implements Serializable {

  private final @Nonnull String _name;

  protected IpCommunityList(String name) {
    _name = name;
  }

  public abstract <T> T accept(IpCommunityListVisitor<T> visitor);

  public final @Nonnull String getName() {
    return _name;
  }
}
