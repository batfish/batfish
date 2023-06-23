package org.batfish.datamodel.questions;

import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;

/**
 * A class that wraps {@link BgpRouteDiffs} and overrides its equality/hash methods to operate over
 * deltas rather than full-value differences when applicable. Currently, only community sets have a
 * delta difference but in the future we might do the same for as-paths.
 */
public class StructuredBgpRouteDiffs {

  private final SortedSet<BgpRouteDiff> _diffs;
  private final Optional<BgpRouteCommunityDiff> _communityDiff;

  public StructuredBgpRouteDiffs(
      SortedSet<BgpRouteDiff> diffs, Optional<BgpRouteCommunityDiff> communityDiff) {
    _diffs = diffs;
    _communityDiff = communityDiff;
    if (diffs.stream().anyMatch(d -> d.getFieldName().equals(BgpRoute.PROP_COMMUNITIES))) {
      throw new BatfishException("Use of unstructured community differences");
    }
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
