package org.batfish.datamodel.questions;

import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.bgp.community.Community;

public class BgpRouteCommunityDiff {

  private final SortedSet<Community> _added;
  private final SortedSet<Community> _removed;

  private final SortedSet<Community> _oldValue;

  private final SortedSet<Community> _newValue;

  public BgpRouteCommunityDiff(SortedSet<Community> oldValue, SortedSet<Community> newValue) {
    _oldValue = oldValue;
    _newValue = newValue;
    _added =
        newValue.stream()
            .filter(c -> !oldValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
    _removed =
        oldValue.stream()
            .filter(c -> !newValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
  }

  public BgpRouteDiff toRouteDiff() {
    return new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, _oldValue.toString(), _newValue.toString());
  }

  public SortedSet<Community> getAdded() {
    return _added;
  }

  public SortedSet<Community> getRemoved() {
    return _removed;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + _added.hashCode();
    result = 31 * result + _removed.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BgpRouteCommunityDiff that = (BgpRouteCommunityDiff) o;

    if (!_added.equals(that._added)) {
      return false;
    }
    return _removed.equals(that._removed);
  }
}
