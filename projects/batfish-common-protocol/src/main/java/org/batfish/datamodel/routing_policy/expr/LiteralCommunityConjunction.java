package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;

public final class LiteralCommunityConjunction extends CommunitySetExpr {

  private static final String PROP_REQUIRED_COMMUNITIES = "requiredCommunities";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull LiteralCommunityConjunction create(
      @JsonProperty(PROP_REQUIRED_COMMUNITIES) SortedSet<Long> requiredCommunities) {
    return new LiteralCommunityConjunction(
        firstNonNull(requiredCommunities, ImmutableSortedSet.of()));
  }

  private SortedSet<Long> _requiredCommunities;

  public LiteralCommunityConjunction(@Nonnull Collection<Long> requiredCommunities) {
    _requiredCommunities = ImmutableSortedSet.copyOf(requiredCommunities);
  }

  @Override
  public SortedSet<Long> asLiteralCommunities(Environment environment) {
    throw new UnsupportedOperationException(
        "Cannot be represented as a set of literal communities");
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralCommunityConjunction)) {
      return false;
    }
    return _requiredCommunities.equals(((LiteralCommunityConjunction) obj)._requiredCommunities);
  }

  @JsonProperty(PROP_REQUIRED_COMMUNITIES)
  public SortedSet<Long> getRequiredCommunities() {
    return _requiredCommunities;
  }

  @Override
  public int hashCode() {
    return _requiredCommunities.hashCode();
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Long> communitySetCandidate) {
    return Sets.intersection(_requiredCommunities, communitySetCandidate).size()
        == communitySetCandidate.size();
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    throw new UnsupportedOperationException("Can only be used to match a set of communities");
  }
}
