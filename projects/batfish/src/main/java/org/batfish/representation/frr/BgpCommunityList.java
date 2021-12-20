package org.batfish.representation.frr;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An ip community list that matches a route's communities. */
public abstract class BgpCommunityList implements Serializable {

  private final @Nonnull String _name;

  protected BgpCommunityList(String name) {
    _name = name;
  }

  public abstract <T> T accept(BgpCommunityListVisitor<T> visitor);

  public final @Nonnull String getName() {
    return _name;
  }
}
