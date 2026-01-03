package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is a standard community.
 */
public final class AllStandardCommunities extends CommunityMatchExpr {

  public static @Nonnull AllStandardCommunities instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof AllStandardCommunities;
  }

  @Override
  public int hashCode() {
    return 0xC1E7ADB7; // randomly generated
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAllStandardCommunities(this, arg);
  }

  private static final AllStandardCommunities INSTANCE = new AllStandardCommunities();

  @JsonCreator
  private static @Nonnull AllStandardCommunities create() {
    return INSTANCE;
  }

  private AllStandardCommunities() {}
}
