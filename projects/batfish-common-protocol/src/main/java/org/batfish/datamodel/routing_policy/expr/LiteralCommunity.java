package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.visitors.CommunitySetExprVisitor;
import org.batfish.datamodel.visitors.VoidCommunitySetExprVisitor;

/**
 * A {@link CommunitySetExpr} matching community-sets that contain at least the community returned
 * by {@link #getCommunity()}.
 */
public class LiteralCommunity extends CommunitySetExpr {

  private static final String PROP_COMMUNITY = "community";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull LiteralCommunity create(@JsonProperty(PROP_COMMUNITY) Long community) {
    return new LiteralCommunity(requireNonNull(community));
  }

  private final long _community;

  public LiteralCommunity(long community) {
    _community = community;
  }

  @Override
  public <T> T accept(CommunitySetExprVisitor<T> visitor) {
    return visitor.visitLiteralCommunity(this);
  }

  @Override
  public void accept(VoidCommunitySetExprVisitor visitor) {
    visitor.visitLiteralCommunity(this);
  }

  /**
   * When treated as a literal set of communities, {@link LiteralCommunity} represents the singleton
   * set of the community returned by {@link #getCommunity}.
   */
  @Override
  public SortedSet<Long> asLiteralCommunities(Environment environment) {
    return ImmutableSortedSet.of(_community);
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralCommunity)) {
      return false;
    }
    return _community == ((LiteralCommunity) obj)._community;
  }

  @JsonProperty(PROP_COMMUNITY)
  public long getCommunity() {
    return _community;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_community);
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Long> communitySetCandidate) {
    return communitySetCandidate.contains(_community);
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    return community == _community;
  }

  @Override
  public boolean reducible() {
    return true;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_COMMUNITY, _community).toString();
  }
}
