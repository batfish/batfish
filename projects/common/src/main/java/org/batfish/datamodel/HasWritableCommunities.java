package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;

/** A route builder with writable communities */
@ParametersAreNonnullByDefault
public interface HasWritableCommunities<
        B extends AbstractRouteBuilder<B, R>, R extends AbstractRoute>
    extends HasReadableCommunities {

  B setCommunities(CommunitySet communities);
}
