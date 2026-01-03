package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A route or route builder with readable communities. */
@ParametersAreNonnullByDefault
public interface HasReadableCommunities {

  @Nonnull
  CommunitySet getCommunities();

  // TODO: transition all usages to getCommunities and remove
  @JsonIgnore
  @Nonnull
  Set<Community> getCommunitiesAsSet();
}
