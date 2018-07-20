package org.batfish.datamodel.routing_policy.expr;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.Environment;

public class NamedCommunitySet extends CommunitySetExpr {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static NamedCommunitySet create(@JsonProperty(PROP_NAME) String name) {
    return new NamedCommunitySet(requireNonNull(name));
  }

  private final String _name;

  public NamedCommunitySet(@Nonnull String name) {
    _name = name;
  }

  @Override
  public SortedSet<Long> asLiteralCommunities(Environment environment) {
    return resolve(environment).asLiteralCommunities(environment);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NamedCommunitySet)) {
      return false;
    }
    return _name.equals(((NamedCommunitySet) obj)._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public final boolean dynamicMatchCommunity() {
    return true;
  }

  @Override
  public boolean matchAnyCommunity(Environment environment, Set<Long> communityCandidates) {
    CommunitySetExpr resolved = resolve(environment);
    return communityCandidates
        .stream()
        .anyMatch(communityCandidate -> resolved.matchCommunity(environment, communityCandidate));
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    return environment
        .getConfiguration()
        .getCommunityLists()
        .get(_name)
        .matchCommunity(environment, community);
  }

  @Override
  public SortedSet<Long> matchedCommunities(
      Environment environment, Set<Long> communityCandidates) {
    CommunitySetExpr resolved = resolve(environment);
    return communityCandidates
        .stream()
        .filter(communityCandidate -> resolved.matchCommunity(environment, communityCandidate))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  private @Nonnull CommunitySetExpr resolve(@Nonnull Environment environment) {
    return requireNonNull(environment.getConfiguration().getCommunityLists().get(_name));
  }
}
