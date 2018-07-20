package org.batfish.datamodel.routing_policy.expr;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralCommunity extends CommunitySetExpr {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull LiteralCommunity create(Long community) {
    return new LiteralCommunity(requireNonNull(community));
  }

  private final long _community;

  public LiteralCommunity(long community) {
    _community = community;
  }

  @Override
  public SortedSet<Long> asLiteralCommunities(Environment environment) {
    return ImmutableSortedSet.of(_community);
  }

  @Override
  public boolean dynamicMatchCommunity() {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Long> communitySetCandidate) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
