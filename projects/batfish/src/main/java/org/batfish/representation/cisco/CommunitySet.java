package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class CommunitySet implements Serializable {

  private final List<CommunitySetElem> _elements;

  public CommunitySet(@Nonnull List<CommunitySetElem> elements) {
    _elements = ImmutableList.copyOf(elements);
  }

  public @Nonnull List<CommunitySetElem> getElements() {
    return _elements;
  }
}
