package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

public final class InlineCommunitySet extends CommunitySet {
  /*
    private class CachedCommunitiesSupplier implements Serializable, Supplier<SortedSet<Long>> {

      private static final long serialVersionUID = 1L;

      @Override
      public SortedSet<Long> get() {
        return initCommunities();
      }
    }
  */
  private static final long serialVersionUID = 1L;

  // private final Supplier<SortedSet<Long>> _cachedCommunities;

  public InlineCommunitySet(@Nonnull Collection<Long> communities) {
    this(communities.stream().map(CommunitySetElem::new).collect(ImmutableList.toImmutableList()));
  }

  public InlineCommunitySet(@Nonnull List<CommunitySetElem> communities) {
    // _cachedCommunities = Suppliers.memoize(new CachedCommunitiesSupplier());
    super(communities);
  }
  /*
    @Override
    public SortedSet<Long> asLiteralCommunities(Environment environment) {
      return _cachedCommunities.get();
    }

  private SortedSet<Long> initCommunities() {
    return _communities
        .stream()
        .map(CommunitySetElem::community)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }
  @Override
  public SortedSet<Long> matchedCommunities(
      Environment environment, Set<Long> communityCandidates) {
    return ImmutableSortedSet.copyOf(
        Sets.intersection(asLiteralCommunities(environment), communityCandidates));
  }

  @Override
  public boolean isDynamic() {
    // TODO: can be true after {@link VarCommunitySetElemHalf} is implemented
    return false;
  }

  @Override
  public boolean matchCommunity(Environment environment, long community) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
  */
}
