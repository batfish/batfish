package org.batfish.datamodel.questions;

import static com.google.common.collect.Ordering.natural;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.bgp.community.Community;

public class BgpRouteCommunityDiff extends BgpRouteDiff {

  @JsonIgnore private final SortedSet<Community> _added;
  @JsonIgnore private final SortedSet<Community> _removed;

  public BgpRouteCommunityDiff(SortedSet<Community> oldValue, SortedSet<Community> newValue) {
    super(BgpRoute.PROP_COMMUNITIES, oldValue.toString(), newValue.toString());
    _added =
        newValue.stream()
            .filter(c -> !oldValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
    _removed =
        oldValue.stream()
            .filter(c -> !newValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
  }

  @JsonIgnore
  public SortedSet<Community> getAdded() {
    return _added;
  }

  @JsonIgnore
  public SortedSet<Community> getRemoved() {
    return _removed;
  }

  /**
   * Not overriding the hash/equals methods here as this would change the semantics of Hashsets,
   * etc, in an undesirable way. Instead, one can use these methods to define their own custom
   * sets/wrapper classes.
   */
  public int deltaHash() {
    int result = 1;
    result = 31 * result + _added.hashCode();
    result = 31 * result + _removed.hashCode();
    return result;
  }

  public boolean deltaEquals(Object o) {
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
