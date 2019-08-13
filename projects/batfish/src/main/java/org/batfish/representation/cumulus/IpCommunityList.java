package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An ip community list that matches a route's communities. */
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
