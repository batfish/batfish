package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/** A set of {@link Community} objects. */
public final class CommunitySet implements Serializable {

  public static @Nonnull CommunitySet empty() {
    return EMPTY;
  }

  public static @Nonnull CommunitySet of(Community... communities) {
    return of(Arrays.asList(communities));
  }

  public static @Nonnull CommunitySet of(Iterable<Community> communities) {
    return new CommunitySet(communities);
  }

  public @Nonnull Set<Community> getCommunities() {
    return _communities;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySet)) {
      return false;
    }
    return _communities.equals(((CommunitySet) obj)._communities);
  }

  @Override
  public int hashCode() {
    return _communities.hashCode();
  }

  private static final CommunitySet EMPTY = new CommunitySet(ImmutableSet.of());

  @JsonCreator
  private static @Nonnull CommunitySet create(@Nullable Iterable<Community> communities) {
    return communities != null ? of(communities) : empty();
  }

  private final @Nonnull Set<Community> _communities;

  private CommunitySet(Iterable<Community> communities) {
    _communities = ImmutableSet.copyOf(communities);
  }

  @JsonValue
  private @Nonnull SortedSet<Community> getCommunitiesSorted() {
    // sorted for refs
    return ImmutableSortedSet.copyOf(_communities);
  }
}
