package org.batfish.datamodel.questions;

import static com.google.common.collect.Ordering.natural;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.bgp.community.Community;

public class BgpRouteCommunityDiff extends BgpRouteDiff {

  @JsonIgnore private final SortedSet<Community> added;
  @JsonIgnore private final SortedSet<Community> removed;

  public BgpRouteCommunityDiff(
      String fieldName, SortedSet<Community> oldValue, SortedSet<Community> newValue) {
    super(fieldName, oldValue.toString(), newValue.toString());
    added =
        newValue.stream()
            .filter(c -> !oldValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
    removed =
        oldValue.stream()
            .filter(c -> !newValue.contains(c))
            .collect(ImmutableSortedSet.toImmutableSortedSet(natural()));
  }

  public SortedSet<Community> getAdded() {
    return added;
  }

  public SortedSet<Community> getRemoved() {
    return removed;
  }

  public int deltaHash() {
    int result = 1;
    result = 31 * result + added.hashCode();
    result = 31 * result + removed.hashCode();
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

    if (!added.equals(that.added)) {
      return false;
    }
    return removed.equals(that.removed);
  }
}
