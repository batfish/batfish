package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * A class for structured BGP Route differences. This class might keep more information than just
 * the old/new value for a field. Currently, only community sets have a richer representation that
 * includes a delta between old/new value but in the future we might do the same for as-paths.
 */
public class StructuredBgpRouteDiffs {

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

  @Override
  public int hashCode() {
    int result = _diffs.hashCode();
    result = 31 * result + _communityDiff.hashCode();
    return result;
  }
}
