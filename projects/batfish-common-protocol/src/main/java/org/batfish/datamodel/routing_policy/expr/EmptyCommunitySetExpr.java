package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;

public class EmptyCommunitySetExpr extends CommunitySetExpr {

  public static final EmptyCommunitySetExpr INSTANCE = new EmptyCommunitySetExpr();

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull EmptyCommunitySetExpr create() {
    return INSTANCE;
  }

  private EmptyCommunitySetExpr() {}

  @Override
  public SortedSet<Long> asLiteralCommunities(Environment environment) {
    return ImmutableSortedSet.of();
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof EmptyCommunitySetExpr;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Long> communitySetCandidate) {
    return false;
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    return false;
  }

  @Override
  public boolean reducible() {
    return true;
  }
}
