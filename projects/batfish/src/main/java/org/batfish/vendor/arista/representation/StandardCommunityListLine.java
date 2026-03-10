package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;

@ParametersAreNonnullByDefault
public class StandardCommunityListLine implements Serializable {

  private final @Nonnull LineAction _action;
  private final @Nonnull Set<StandardCommunity> _communities;

  public StandardCommunityListLine(LineAction action, Iterable<StandardCommunity> communities) {
    _action = action;
    _communities = ImmutableSet.copyOf(communities);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull Set<StandardCommunity> getCommunities() {
    return _communities;
  }
}
