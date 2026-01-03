package org.batfish.datamodel.questions;

import static com.google.common.collect.Ordering.natural;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;

public class BgpRouteCommunityDiff implements Comparable<BgpRouteCommunityDiff> {

  private final SortedSet<Community> _added;
  private final SortedSet<Community> _removed;

  private final SortedSet<Community> _oldValue;

  private final SortedSet<Community> _newValue;

  public BgpRouteCommunityDiff(SortedSet<Community> oldValue, SortedSet<Community> newValue) {
    _oldValue = ImmutableSortedSet.copyOf(oldValue);
    _newValue = ImmutableSortedSet.copyOf(newValue);
    _added =
        newValue.stream()
            .filter(c -> !oldValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
    _removed =
        oldValue.stream()
            .filter(c -> !newValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
  }

  /**
   * @return projects this community difference to a {@link BgpRouteDiff} that describes the old and
   *     the new value for the community field.
   */
  public BgpRouteDiff toRouteDiff() {
    return new BgpRouteDiff(BgpRoute.PROP_COMMUNITIES, _oldValue.toString(), _newValue.toString());
  }

  public SortedSet<Community> getAdded() {
    return _added;
  }

  public SortedSet<Community> getRemoved() {
    return _removed;
  }

  public SortedSet<Community> getOldValue() {
    return _oldValue;
  }

  public SortedSet<Community> getNewValue() {
    return _newValue;
  }

  /**
   * The hash (and resp. the equality function) of this object only considers the _added and
   * _removed fields, ignoring the _oldValue and _newValue. This facilitates distinguishing
   * community differences when they add or remove a different community, hence allows us to
   * deduplicate two differences where the same community was added/removed even though the set of
   * communities in each case is not the same.
   */
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

  private static final Comparator<BgpRouteCommunityDiff> COMPARATOR =
      Comparator.comparing(
              BgpRouteCommunityDiff::getAdded, Comparators.lexicographical(Ordering.natural()))
          .thenComparing(
              BgpRouteCommunityDiff::getRemoved, Comparators.lexicographical(Ordering.natural()));

  @Override
  public int compareTo(@Nonnull BgpRouteCommunityDiff that) {
    return COMPARATOR.compare(this, that);
  }
}
