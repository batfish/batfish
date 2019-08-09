package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An access-list matches a route's standard communities attribute against some criteria. */
public class IpCommunityList implements Serializable {

  private final @Nonnull String _name;

  public IpCommunityList(String name) {
    _name = name;
  }

  public final @Nonnull String getName() {
    return _name;
  }
}
