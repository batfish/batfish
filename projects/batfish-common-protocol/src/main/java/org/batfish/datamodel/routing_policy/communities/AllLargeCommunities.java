package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/** Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is a large community. */
public class AllLargeCommunities extends CommunityMatchExpr {

  public static @Nonnull AllLargeCommunities instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof AllLargeCommunities;
  }

  @Override
  public int hashCode() {
    return 0x77CDFD17; // randomly generated
  }

  @Override
  protected <T> T accept(CommunityMatchExprVisitor<T> visitor) {
    return visitor.visitAllLargeCommunities(this);
  }

  private static final AllLargeCommunities INSTANCE = new AllLargeCommunities();

  @JsonCreator
  private static @Nonnull AllLargeCommunities create() {
    return INSTANCE;
  }

  private AllLargeCommunities() {}
}
