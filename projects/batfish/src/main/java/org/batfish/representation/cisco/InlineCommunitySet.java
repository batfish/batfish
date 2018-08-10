package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

public final class InlineCommunitySet extends CommunitySet {

  private static final long serialVersionUID = 1L;

  public InlineCommunitySet(@Nonnull Collection<Long> communities) {
    this(
        communities
            .stream()
            .map(CommunitySetElemHalves::new)
            .collect(ImmutableList.toImmutableList()));
  }

  public InlineCommunitySet(@Nonnull List<CommunitySetElem> communities) {
    super(communities);
  }
}
