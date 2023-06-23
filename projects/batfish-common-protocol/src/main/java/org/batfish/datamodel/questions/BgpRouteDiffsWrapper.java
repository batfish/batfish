package org.batfish.datamodel.questions;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Iterator;

/**
 * A class that wraps {@link BgpRouteDiffs} and overrides its equality/hash methods to operate over
 * deltas rather than full-value differences when applicable. Currently, only community sets have a
 * delta difference but in the future we might do the same for as-paths.
 */
public class BgpRouteDiffsWrapper {

  private final BgpRouteDiffs _diffs;

  public BgpRouteDiffsWrapper(BgpRouteDiffs diffs) {
    _diffs = diffs;
  }

  public BgpRouteDiffsWrapper() {
    _diffs = new BgpRouteDiffs(ImmutableSortedSet.of());
  }

  public BgpRouteDiffs get_diffs() {
    return _diffs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BgpRouteDiffsWrapper that = (BgpRouteDiffsWrapper) o;

    Iterator<BgpRouteDiff> i = _diffs.getDiffs().iterator();
    Iterator<BgpRouteDiff> j = that._diffs.getDiffs().iterator();
    while ((i.hasNext()) && (j.hasNext())) {
      Object obji = i.next();
      Object objj = j.next();
      // If it's a community difference then do not use the default equality, but rather compare
      // based on the delta.
      if ((obji instanceof BgpRouteCommunityDiff) && (objj instanceof BgpRouteCommunityDiff)) {
        if (!((BgpRouteCommunityDiff) obji).deltaEquals(objj)) {
          return false;
        }
      } else {
        if (!obji.equals(objj)) {
          return false;
        }
      }
    }
    return !i.hasNext() && !j.hasNext();
  }

  @Override
  public int hashCode() {
    int hashCode = 1;
    Iterator<BgpRouteDiff> i = _diffs.getDiffs().iterator();
    while (i.hasNext()) {
      Object obj = i.next();
      if (obj instanceof BgpRouteCommunityDiff) {
        // Use special hash function for community differences.
        hashCode = 31 * hashCode + ((BgpRouteCommunityDiff) obj).deltaHash();
      } else {
        hashCode = 31 * hashCode + obj.hashCode();
      }
    }
    return hashCode;
  }
}
