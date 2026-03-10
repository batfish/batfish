package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A class for structured BGP Route differences. This class might keep more information than just
 * the old/new value for a field. Currently, only community sets have a richer representation that
 * includes a delta between old/new value but in the future we might do the same for as-paths.
 */
public class StructuredBgpRouteDiffs implements Comparable<StructuredBgpRouteDiffs> {

  private final SortedSet<BgpRouteDiff> _diffs;
  private final Optional<BgpRouteCommunityDiff> _communityDiff;

  public StructuredBgpRouteDiffs(
      SortedSet<BgpRouteDiff> diffs, Optional<BgpRouteCommunityDiff> communityDiff) {
    _diffs = diffs;
    _communityDiff = communityDiff;
    checkArgument(
        diffs.stream().noneMatch(d -> d.getFieldName().equals(BgpRoute.PROP_COMMUNITIES)),
        "Unexpected use of unstructured community differences");
  }

  public SortedSet<BgpRouteDiff> getDiffs() {
    return _diffs;
  }

  public Optional<BgpRouteCommunityDiff> getCommunityDiff() {
    return _communityDiff;
  }

  public StructuredBgpRouteDiffs() {
    _diffs = ImmutableSortedSet.of();
    _communityDiff = Optional.empty();
  }

  public BgpRouteDiffs toBgpRouteDiffs() {
    return new BgpRouteDiffs(
        Stream.concat(
                _diffs.stream(), _communityDiff.map(BgpRouteCommunityDiff::toRouteDiff).stream())
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural())));
  }

  /**
   * @return true if there are some route field differences represented by this object.
   */
  public boolean hasDifferences() {
    return !_diffs.isEmpty() || _communityDiff.isPresent();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StructuredBgpRouteDiffs that = (StructuredBgpRouteDiffs) o;

    if (!_diffs.equals(that._diffs)) {
      return false;
    }
    return _communityDiff.equals(that._communityDiff);
  }

  private static final Comparator<StructuredBgpRouteDiffs> COMPARATOR =
      Comparator.comparing(
              StructuredBgpRouteDiffs::getCommunityDiff,
              Comparators.emptiesFirst(Ordering.natural()))
          .thenComparing(
              StructuredBgpRouteDiffs::getDiffs, Comparators.lexicographical(Ordering.natural()));

  /**
   * Compares a {@link StructuredBgpRouteDiffs} object against another one. The comparison is in
   * lexicographic order, with community differences compared first, followed by all other
   * differences.
   *
   * @param other the other object to compare against
   * @return an integer indicating whether the first object is less than, equal to, or greater than
   *     the second one
   */
  @Override
  public int compareTo(@Nonnull StructuredBgpRouteDiffs other) {
    return COMPARATOR.compare(this, other);
  }

  @Override
  public int hashCode() {
    int result = _diffs.hashCode();
    result = 31 * result + _communityDiff.hashCode();
    return result;
  }
}
