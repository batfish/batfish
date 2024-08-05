package org.batfish.minesweeper.bdd;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IntegerSpace;

/**
 * This class represents the results of symbolic analysis of a {@link
 * org.batfish.datamodel.routing_policy.communities.SetCommunities} routing statement, as performed
 * by the {@link SetCommunitiesVisitor}.
 *
 * <p>This class can be viewed as representing a non-standard form of "three-valued" set of
 * communities, where some communities are "maybe" in the set. This generalization of community sets
 * is necessary because setting communities depends on the current communities in the route, which
 * are represented by the {@link org.batfish.datamodel.routing_policy.communities.InputCommunities}
 * expression. Hence, symbolic analysis has the effect of partitioning the atomic predicates into
 * three sets: those that are definitely set by the statement; those that are definitely removed by
 * the statement; and those that are in the route announcement after the statement if and only if
 * they were beforehand. We maintain the first two sets explicitly, while the third set is implicit
 * (everything not in the other two sets).
 *
 * <p>For example, consider representing the effects of setting (InputCommunities U 20:30), which is
 * how community addition is modeled. In this case, we know that after the statement 20:30 is
 * definitely set; nothing is definitely removed; and everything else is in the route announcement
 * if and only if it was originally.
 */
@ParametersAreNonnullByDefault
public class CommunityAPDispositions {

  private final int _numAPs;
  // the atomic predicates that are definitely on the route announcement
  private final @Nonnull IntegerSpace _mustExist;
  // the atomic predicates that are definitely not on the route announcement
  private final @Nonnull IntegerSpace _mustNotExist;

  public CommunityAPDispositions(int numAPs, IntegerSpace mustExist, IntegerSpace mustNotExist) {
    checkArgument(numAPs > 0, "Must have at least one atomic predicate, not %s", numAPs);
    IntegerSpace valid = IntegerSpace.of(Range.closed(0, numAPs - 1));
    checkArgument(
        valid.contains(mustExist),
        "Must exist %s is not contained in the valid range %s",
        mustExist,
        valid);
    checkArgument(
        valid.contains(mustNotExist),
        "Must not exist %s is not contained in the valid range %s",
        mustNotExist,
        valid);
    checkArgument(
        mustExist.intersection(mustNotExist).isEmpty(),
        "Must exist %s cannot overlap must not exist %s",
        mustExist,
        mustNotExist);
    _numAPs = numAPs;
    _mustExist = mustExist;
    _mustNotExist = mustNotExist;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityAPDispositions)) {
      return false;
    }
    CommunityAPDispositions other = (CommunityAPDispositions) obj;
    return _numAPs == other._numAPs
        && _mustExist.equals(other._mustExist)
        && _mustNotExist.equals(other._mustNotExist);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_numAPs, _mustExist, _mustNotExist);
  }

  /**
   * Produces the set difference of two CommunityAPDispositions; the result is only representable as
   * a CommunityAPDispositions object if the right-hand object is exact (see the isExact method
   * below), so we require that to be the case.
   */
  public CommunityAPDispositions diff(CommunityAPDispositions other) {
    assert _numAPs == other._numAPs
        : "diffed CommunityAPDispositions must have the same number of atomic predicates";
    assert other.isExact() : "the right-hand CommunityAPDisposition in a diff must be exact";
    return new CommunityAPDispositions(
        _numAPs,
        _mustExist.intersection(other.getMustNotExist()),
        _mustNotExist.union(other.getMustExist()));
  }

  /** Produces the set union of two CommunityAPDispositions */
  public CommunityAPDispositions union(CommunityAPDispositions other) {
    assert _numAPs == other._numAPs
        : "unioned CommunityAPDispositions must have the same number of atomic predicates";
    return new CommunityAPDispositions(
        _numAPs,
        _mustExist.union(other.getMustExist()),
        _mustNotExist.intersection(other.getMustNotExist()));
  }

  /**
   * Produces a CommunityAPDisposition object representing the empty set --- all atomic predicates
   * are known to not be in the set.
   */
  public static CommunityAPDispositions empty(BDDRoute bddRoute) {
    return exactly(ImmutableSet.of(), bddRoute);
  }

  /**
   * Produces a CommunityAPDispositions object representing exactly the given set of atomic
   * predicates; all other atomic predicates are put in the mustNotExist set.
   */
  public static CommunityAPDispositions exactly(Set<Integer> aps, BDDRoute bddRoute) {
    int numAPs = bddRoute.getCommunityAtomicPredicates().length;
    IntegerSpace mustExist = IntegerSpace.builder().includingAll(aps).build();
    IntegerSpace mustNotExist = IntegerSpace.of(Range.closed(0, numAPs - 1)).difference(mustExist);
    return new CommunityAPDispositions(numAPs, mustExist, mustNotExist);
  }

  @VisibleForTesting
  boolean isExact() {
    return IntegerSpace.of(Range.closed(0, _numAPs - 1)).equals(_mustExist.union(_mustNotExist));
  }

  public int getNumAPs() {
    return _numAPs;
  }

  public IntegerSpace getMustExist() {
    return _mustExist;
  }

  public IntegerSpace getMustNotExist() {
    return _mustNotExist;
  }
}
