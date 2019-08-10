package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** An ip community list that matches a route's communities. */
public class IpCommunityList implements Serializable {

  private final @Nonnull String _name;

  public IpCommunityList(String name) {
    _name = name;
  }

  public final @Nonnull String getName() {
    return _name;
  }
}
