package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.visitors.CommunitySetExprVisitor;
import org.batfish.datamodel.visitors.VoidCommunitySetExprVisitor;

/**
 * Represents a named access-list whose matching criteria is restricted to regexes on community
 * attributes sent with a bgp advertisement.
 */
public class CommunityList extends CommunitySetExpr {

  private final class CommunityCacheSupplier
      implements Supplier<LoadingCache<Community, Boolean>>, Serializable {

    @Override
    public LoadingCache<Community, Boolean> get() {
      return CacheBuilder.newBuilder()
          .softValues()
          .build(
              new CacheLoader<Community, Boolean>() {
                @Override
                public Boolean load(@Nonnull Community community) {
                  return computeIfMatches(community, null);
                }
              });
    }
  }

  private static final String PROP_INVERT_MATCH = "invertMatch";
  private static final String PROP_LINES = "lines";
  private static final String PROP_NAME = "name";

  @JsonCreator
  private static @Nonnull CommunityList create(
      @JsonProperty(PROP_LINES) List<CommunityListLine> lines,
      @JsonProperty(PROP_INVERT_MATCH) boolean invertMatch,
      @JsonProperty(PROP_NAME) String name) {
    return new CommunityList(
        firstNonNull(name, ""), firstNonNull(lines, ImmutableList.of()), invertMatch);
  }

  private final Supplier<LoadingCache<Community, Boolean>> _communityCache;

  private final boolean _invertMatch;

  @Nonnull private final List<CommunityListLine> _lines;

  @Nonnull private final String _name;

  // Derived properties, computed lazily at runtime.
  @Nullable private Boolean _dynamic;
  @Nullable private Set<Community> _literalCommunities;
  @Nullable private Boolean _reducible;

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
    _communityCache = Suppliers.memoize(new CommunityCacheSupplier());
  }

  @Override
  public <T> T accept(CommunitySetExprVisitor<T> visitor) {
    return visitor.visitCommunityList(this);
  }

  @Override
  public void accept(VoidCommunitySetExprVisitor visitor) {
    visitor.visitCommunityList(this);
  }

  @Override
  @Nonnull
  public Set<Community> asLiteralCommunities(@Nonnull Environment environment)
      throws UnsupportedOperationException {
    Set<Community> literal = _literalCommunities;
    if (literal == null) {
      literal =
          _lines.stream()
              .map(CommunityListLine::getMatchCondition)
              .map(lineMatchCondition -> lineMatchCondition.asLiteralCommunities(environment))
              .flatMap(Collection::stream)
              .collect(ImmutableSet.toImmutableSet());
      _literalCommunities = literal;
    }
    return literal;
  }

  /** Check if any line matches given community */
  private boolean computeIfMatches(Community community, @Nullable Environment environment) {
    Optional<CommunityListLine> matchingLine =
        _lines.stream()
            .filter(line -> line.getMatchCondition().matchCommunity(environment, community))
            .findFirst();

    // "invert != condition" is a concise way of inverting a boolean
    return matchingLine.isPresent()
        && _invertMatch != (matchingLine.get().getAction() == LineAction.PERMIT);
  }

  @Override
  public boolean dynamicMatchCommunity() {
    Boolean b = _dynamic;
    if (b == null) {
      b =
          _lines.stream()
              .map(CommunityListLine::getMatchCondition)
              .anyMatch(CommunitySetExpr::dynamicMatchCommunity);
      _dynamic = b;
    }
    return b;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof CommunityList)) {
      return false;
    }
    CommunityList rhs = (CommunityList) o;
    return _invertMatch == rhs._invertMatch && _lines.equals(rhs._lines) && _name.equals(rhs._name);
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
  @Nonnull
  public List<CommunityListLine> getLines() {
    return _lines;
  }

  /** The name of this community list. */
  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_invertMatch, _lines, _name);
  }

  @Override
  public boolean matchCommunities(Environment environment, Set<Community> communitySetCandidate) {
    if (reducible()) {
      return communitySetCandidate.stream()
          .anyMatch(community -> matchCommunity(environment, community));
    }
    Optional<CommunityListLine> matchingLine =
        _lines.stream()
            .filter(
                line ->
                    line.getMatchCondition().matchCommunities(environment, communitySetCandidate))
            .findFirst();

    // "invert != condition" is a concise way of inverting a boolean
    return matchingLine.isPresent()
        && _invertMatch != (matchingLine.get().getAction() == LineAction.PERMIT);
  }

  /**
   * Returns true iff a given {@code community} is permitted/accepted by this {@link CommunityList}
   * under the provided {@code environment}.
   *
   * @throws UnsupportedOperationException if {@code environment} is {@code null} and this is a
   *     dynamic {@link CommunityList}.
   */
  @Override
  public boolean matchCommunity(@Nullable Environment environment, Community community) {
    if (dynamicMatchCommunity()) {
      if (environment == null) {
        throw new UnsupportedOperationException(
            "Supplied environment must not be null for a dynamic CommunityList");
      }
      return computeIfMatches(community, environment);
    }
    try {
      return _communityCache.get().get(community);
    } catch (ExecutionException | UncheckedExecutionException e) {
      throw new UnsupportedOperationException(
          "At least one line of this CommunityList has a match expression not supporting"
              + " matchCommunity",
          e);
    }
  }

  @Override
  public boolean reducible() {
    Boolean b = _reducible;
    if (b == null) {
      b =
          _lines.stream()
              .map(CommunityListLine::getMatchCondition)
              .allMatch(CommunitySetExpr::reducible);
      _reducible = b;
    }
    return b;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_INVERT_MATCH, _invertMatch)
        .add(PROP_NAME, _name)
        .add(PROP_LINES, _lines)
        .toString();
  }
}
