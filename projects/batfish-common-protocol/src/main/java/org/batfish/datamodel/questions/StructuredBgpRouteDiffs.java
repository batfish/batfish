package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Iterator;
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

  public SortedSet<BgpRouteDiff> get_diffs() {
    return _diffs;
  }

  public Optional<BgpRouteCommunityDiff> get_communityDiff() {
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

  /**
   * Compares two sorted sets in lexicographic order.
   *
   * @param o1 the first sorted set
   * @param o2 the second sorted set
   * @return an integer indicating whether the first set is less than, equal to, or greater than the
   *     second set
   * @param <T> the type of the sets' elements
   */
  static <T extends Comparable<T>> int sortedSetCompareTo(SortedSet<T> o1, SortedSet<T> o2) {
    Iterator<T> o1Iter = o1.iterator();
    Iterator<T> o2Iter = o2.iterator();
    while (o1Iter.hasNext() && o2Iter.hasNext()) {
      int comp = o1Iter.next().compareTo(o2Iter.next());
      if (comp != 0) {
        return comp;
      }
    }
    return o1Iter.hasNext() ? 1 : o2Iter.hasNext() ? -1 : 0;
  }

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
    boolean thisPresent = this._communityDiff.isPresent();
    boolean otherPresent = other._communityDiff.isPresent();
    if (thisPresent && otherPresent) {
      int commComp = this._communityDiff.get().compareTo(other._communityDiff.get());
      if (commComp != 0) {
        return commComp;
      }
    }
    if (thisPresent == otherPresent) {
      return sortedSetCompareTo(this._diffs, other._diffs);
    } else if (thisPresent) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public int hashCode() {
    int result = _diffs.hashCode();
    result = 31 * result + _communityDiff.hashCode();
    return result;
  }
}
