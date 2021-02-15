package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/**
 * A line of an {@link IpCommunityListStandard}.
 *
 * <p>A route must contain every community from {@link #getCommunities} to be matched by this line.
 */
public final class IpCommunityListStandardLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final @Nonnull Set<StandardCommunity> _communities;

  public IpCommunityListStandardLine(LineAction action, Set<StandardCommunity> communities) {
    _action = action;
    _communities = communities;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull Set<StandardCommunity> getCommunities() {
    return _communities;
  }
}
