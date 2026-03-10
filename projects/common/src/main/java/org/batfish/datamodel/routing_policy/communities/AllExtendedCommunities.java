package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is an extended community.
 */
public final class AllExtendedCommunities extends CommunityMatchExpr {

  public static @Nonnull AllExtendedCommunities instance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof AllExtendedCommunities;
  }

  @Override
  public int hashCode() {
    return 0xEEC56E38; // randomly generated
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAllExtendedCommunities(this, arg);
  }

  private static final AllExtendedCommunities INSTANCE = new AllExtendedCommunities();

  @JsonCreator
  private static @Nonnull AllExtendedCommunities create() {
    return INSTANCE;
  }

  private AllExtendedCommunities() {}
}
