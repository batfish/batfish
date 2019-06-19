package org.batfish.representation.cisco;

import java.util.List;
import javax.annotation.Nonnull;

public class NamedCommunitySet extends CommunitySet {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public NamedCommunitySet(@Nonnull String name, @Nonnull List<CommunitySetElem> elements) {
    super(elements);
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
