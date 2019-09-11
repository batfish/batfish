package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is a route-target extended
 * community.
 */
public class RouteTargetExtendedCommunities extends CommunityMatchExpr {

  public static @Nonnull RouteTargetExtendedCommunities instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof RouteTargetExtendedCommunities;
  }

  @Override
  public int hashCode() {
    return 0xA0087E18; // randomly generated
  }

  @Override
  protected <T> T accept(CommunityMatchExprVisitor<T> visitor) {
    return visitor.visitRouteTargetExtendedCommunities(this);
  }

  private static final RouteTargetExtendedCommunities INSTANCE =
      new RouteTargetExtendedCommunities();

  @JsonCreator
  private static @Nonnull RouteTargetExtendedCommunities create() {
    return INSTANCE;
  }

  private RouteTargetExtendedCommunities() {}
}
