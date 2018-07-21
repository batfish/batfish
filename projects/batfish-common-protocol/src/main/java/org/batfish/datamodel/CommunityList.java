package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

/**
 * Represents a named access-list whose matching criteria is restricted to regexes on community
 * attributes sent with a bgp advertisement.
 */
public class CommunityList extends CommunitySetExpr {

  private final class CommunityCacheSupplier
      implements Supplier<LoadingCache<Long, Boolean>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public LoadingCache<Long, Boolean> get() {
      return CacheBuilder.newBuilder()
          .build(
              new CacheLoader<Long, Boolean>() {
                @Override
                public Boolean load(@Nonnull Long community) {
                  return computeIfMatches(community, null);
                }
              });
    }
  }

  private final class DynamicSupplier implements Supplier<Boolean>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Boolean get() {
      return initDynamic();
    }
  }

  private final class LiteralCommunitiesSupplier
      implements Supplier<SortedSet<Long>>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public SortedSet<Long> get() {
      return initLiteralCommunities();
    }
  }

  private final class ReducibleSupplier implements Supplier<Boolean>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Boolean get() {
      return initReducible();
    }
  }

  private static final String PROP_INVERT_MATCH = "invertMatch";

  private static final String PROP_LINES = "lines";

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull CommunityList create(
      @JsonProperty(PROP_LINES) List<CommunityListLine> lines,
      @JsonProperty(PROP_INVERT_MATCH) boolean invertMatch,
      @JsonProperty(PROP_NAME) String name) {
    return new CommunityList(
        requireNonNull(name), firstNonNull(lines, ImmutableList.of()), invertMatch);
  }

  private final Supplier<LoadingCache<Long, Boolean>> _communityCache;

  private final Supplier<Boolean> _dynamic;

  private final boolean _invertMatch;

  @Nonnull private final List<CommunityListLine> _lines;

  private final Supplier<SortedSet<Long>> _literalCommunities;

  @Nonnull private final String _name;

  private final Supplier<Boolean> _reducible;

  /**
   * Constructs a CommunityList with the given name for {@link #_name}, and lines for {@link
   * #_lines}
   *
   * @param name The name of the structure
   * @param lines The lines in the list
   */
  public CommunityList(
      @Nonnull String name, @Nonnull List<CommunityListLine> lines, boolean invertMatch) {
    _name = name;
    _lines = lines;
    _invertMatch = invertMatch;
    _literalCommunities = Suppliers.memoize(new LiteralCommunitiesSupplier());
    _communityCache = Suppliers.memoize(new CommunityCacheSupplier());
    _dynamic = Suppliers.memoize(new DynamicSupplier());
    _reducible = Suppliers.memoize(new ReducibleSupplier());
  }
  
  @Override
  public @Nonnull SortedSet<Long> asLiteralCommunities(Environment environment)
      throws UnsupportedOperationException {
    return _literalCommunities.get();
  }

  /** Check if any line matches given community */
  private boolean computeIfMatches(long community, @Nullable Environment environment) {
    Optional<LineAction> action =
        _lines
            .stream()
            .map(
                line ->
                    line.getMatchCondition().matchCommunity(environment, community)
                        ? line.getAction()
                        : null)
            .filter(Objects::nonNull)
            .findFirst();

    // "invert != condition" is a concise way of inverting a boolean
    return action.isPresent() && _invertMatch != (action.get() == LineAction.ACCEPT);
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return _dynamic.get();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof CommunityList)) {
      return false;
    }
    CommunityList other = (CommunityList) o;
    return _invertMatch == other._invertMatch && Objects.equals(_lines, other._lines);
  }

  /**
   * Specifies whether or not lines should match the complement of their criteria (does not change
   * whether a line permits or denies).
   */
  @JsonProperty(PROP_INVERT_MATCH)
  public boolean getInvertMatch() {
    return _invertMatch;
  }

  /**
   * The list of lines that are checked in order against the community attribute(s) of a bgp
   * advertisement.
   */
  @JsonProperty(PROP_LINES)
  public List<CommunityListLine> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_NAME)
  @JsonPropertyDescription("The name of this community list")
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_invertMatch, _lines);
  }

  public boolean initDynamic() {
    return _lines
        .stream()
        .map(CommunityListLine::getMatchCondition)
        .anyMatch(CommunitySetExpr::dynamicMatchCommunity);
  }

  private SortedSet<Long> initLiteralCommunities() {
    return _lines
        .stream()
        .map(CommunityListLine::getMatchCondition)
        .map(lineMatchCondition -> lineMatchCondition.asLiteralCommunities(null))
        .flatMap(Collection::stream)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  public boolean initReducible() {
    return _lines
        .stream()
        .map(CommunityListLine::getMatchCondition)
        .allMatch(CommunitySetExpr::reducible);
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Long> communitySetCandidate) {
    if (_reducible.get()) {
      return communitySetCandidate
          .stream()
          .anyMatch(community -> matchCommunity(environment, community));
    }
    Optional<LineAction> action =
        _lines
            .stream()
            .map(
                line ->
                    line.getMatchCondition().matchCommunities(environment, communitySetCandidate)
                        ? line.getAction()
                        : null)
            .filter(Objects::nonNull)
            .findFirst();

    // "invert != condition" is a concise way of inverting a boolean
    return action.isPresent() && _invertMatch != (action.get() == LineAction.ACCEPT);
  }

  /**
   * Check if a given community is permitted/accepted by this list.
   *
   * @throws {@link UnsupportedOperationException} if {@code environment} is {@code null} and this
   *     is a dynamic {@link CommunityList}.
   */
  @Override
  public boolean matchCommunity(@Nullable Environment environment, long community) {
    if (_dynamic.get()) {
      if (environment == null) {
        throw new UnsupportedOperationException(
            "Supplied environment must not be null for a dynamic CommunityList");
      }
      return computeIfMatches(community, environment);
    }
    return _communityCache.get().getUnchecked(community);
  }

  @Override
  public boolean reducible() {
    return _reducible.get();
  }
}
