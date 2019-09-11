package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link CommunitySetExpr} representing the set of all communities of all types in the BGP route
 * that is the input to the {@link MatchCommunities} or {@link SetCommunities} in which this
 * expression is evaluated.
 */
public final class InputCommunities extends CommunitySetExpr {

  public static @Nonnull InputCommunities instance() {
    return INSTANCE;
  }

  @Override
  public <T> T accept(CommunitySetExprVisitor<T> visitor) {
    return visitor.visitInputCommunities(this);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof InputCommunities;
  }

  @Override
  public int hashCode() {
    return 0xDA34A708; // randomly generated
  }

  private static final InputCommunities INSTANCE = new InputCommunities();
}
