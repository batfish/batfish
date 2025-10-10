package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A set of {@link Community} objects. */
public final class CommunitySet implements Serializable {

  public static @Nonnull CommunitySet empty() {
    return EMPTY;
  }

  public static @Nonnull CommunitySet of(Community... communities) {
    return of(ImmutableSet.copyOf(communities));
  }

  public static @Nonnull CommunitySet of(Iterable<? extends Community> communities) {
    return of(ImmutableSet.copyOf(communities));
  }

  public static @Nonnull CommunitySet of(Set<? extends Community> communities) {
    if (communities.isEmpty()) {
      // Skip cache if the collection is empty.
      return empty();
    }
    if (communities instanceof ImmutableSet) {
      // Skip a cache operation if the input is immutable.
      @SuppressWarnings("unchecked") // safe since you cannot insert into ImmutableSet
      ImmutableSet<Community> immutableKey = (ImmutableSet<Community>) communities;
      return CACHE.getUnchecked(immutableKey);
    }
    // Skip a copy if a mutable copy of the key is already present.
    CommunitySet ret = CACHE.getIfPresent(communities);
    if (ret != null) {
      return ret;
    }
    // The input communities might be mutable, so freeze them before caching.
    ImmutableSet<Community> immutableKey = ImmutableSet.copyOf(communities);
    ret = new CommunitySet(immutableKey);
    CACHE.put(immutableKey, ret);
    return ret;
  }

  public @Nonnull Set<Community> getCommunities() {
    return _communities;
  }

  public @Nonnull Set<ExtendedCommunity> getExtendedCommunities() {
    Set<ExtendedCommunity> extended = _extendedCommunities;
    if (extended == null) {
      ImmutableSet.Builder<ExtendedCommunity> ret = ImmutableSet.builder();
      for (Community c : _communities) {
        if (c instanceof ExtendedCommunity) {
          ret.add((ExtendedCommunity) c);
        }
      }
      extended = ret.build();
      _extendedCommunities = extended;
    }
    return extended;
  }

  public @Nonnull Set<StandardCommunity> getStandardCommunities() {
    Set<StandardCommunity> standard = _standardCommunities;
    if (standard == null) {
      ImmutableSet.Builder<StandardCommunity> ret = ImmutableSet.builder();
      for (Community c : _communities) {
        if (c instanceof StandardCommunity) {
          ret.add((StandardCommunity) c);
        }
      }
      standard = ret.build();
      _standardCommunities = standard;
    }
    return standard;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySet)) {
      return false;
    }
    CommunitySet other = (CommunitySet) obj;
    return _communities.equals(other._communities);
  }

  @Override
  public int hashCode() {
    return _communities.hashCode();
  }

  @Override
  public @Nonnull String toString() {
    return toStringHelper(getClass()).add("communities", _communities).toString();
  }

  private static final CommunitySet EMPTY = new CommunitySet(ImmutableSet.of());

  @JsonCreator
  private static @Nonnull CommunitySet create(@Nullable Iterable<Community> communities) {
    return communities != null ? of(communities) : empty();
  }

  private final @Nonnull Set<Community> _communities;

  private CommunitySet(ImmutableSet<Community> communities) {
    _communities = communities;
  }

  @JsonValue
  private @Nonnull SortedSet<Community> getCommunitiesSorted() {
    // sorted for refs
    return ImmutableSortedSet.copyOf(_communities);
  }

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^24: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<Set<Community>, CommunitySet> CACHE =
      CacheBuilder.newBuilder()
          .softValues()
          .maximumSize(1 << 24)
          .build(
              CacheLoader.from(
                  set -> {
                    assert set instanceof ImmutableSet;
                    return new CommunitySet((ImmutableSet<Community>) set);
                  }));

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return of(_communities);
  }

  /* Cache conversions to _extendedCommunities and _standardCommunities. */
  private transient @Nullable Set<ExtendedCommunity> _extendedCommunities;
  private transient @Nullable Set<StandardCommunity> _standardCommunities;
}
