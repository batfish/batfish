package org.batfish.representation.arista;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class StandardCommunityListLine implements Serializable {

  private final @Nonnull LineAction _action;
  private final @Nonnull Set<Long> _communities;

  public StandardCommunityListLine(LineAction action, Iterable<Long> communities) {
    _action = action;
    _communities = ImmutableSet.copyOf(communities);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull Set<Long> getCommunities() {
    return _communities;
  }
}
