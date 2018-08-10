package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.util.CommonUtil;

public class AsPath implements Serializable, Comparable<AsPath> {

  private static final long serialVersionUID = 1L;

  /**
   * Returns true iff the provided AS number is reserved for private use by RFC 6696:
   * https://tools.ietf.org/html/rfc6996#section-5
   */
  public static boolean isPrivateAs(long as) {
    return (as >= 64512L && as <= 65534L) || (as >= 4200000000L && as <= 4294967294L);
  }

  public static AsPath ofSingletonAsSets(Long... asNums) {
    return ofSingletonAsSets(Arrays.asList(asNums));
  }

  public static AsPath ofSingletonAsSets(List<Long> asNums) {
    return new AsPath(asNums.stream().map(ImmutableSortedSet::of).collect(Collectors.toList()));
  }

  public static List<SortedSet<Long>> removePrivateAs(List<SortedSet<Long>> asPath) {
    return asPath
        .stream()
        .map(
            asSet ->
                asSet
                    .stream()
                    .filter(as -> !AsPath.isPrivateAs(as))
                    .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())))
        .filter(asSet -> !asSet.isEmpty())
        .collect(ImmutableList.toImmutableList());
  }

  private final List<SortedSet<Long>> _asSets;

  @JsonCreator
  public AsPath(List<SortedSet<Long>> asSets) {
    _asSets = copyAsSets(asSets);
  }

  @Override
  public int compareTo(@Nonnull AsPath rhs) {
    Iterator<SortedSet<Long>> l = _asSets.iterator();
    Iterator<SortedSet<Long>> r = rhs._asSets.iterator();
    while (l.hasNext()) {
      if (!r.hasNext()) {
        return 1;
      }
      SortedSet<Long> lVal = l.next();
      SortedSet<Long> rVal = r.next();
      int ret = CommonUtil.compareCollection(lVal, rVal);
      if (ret != 0) {
        return ret;
      }
    }
    if (r.hasNext()) {
      return -1;
    }
    return 0;
  }

  public boolean containsAs(long as) {
    return _asSets.stream().anyMatch(a -> a.contains(as));
  }

  private List<SortedSet<Long>> copyAsSets(List<SortedSet<Long>> asSets) {
    List<SortedSet<Long>> newAsSets = new ArrayList<>(asSets.size());
    for (SortedSet<Long> asSet : asSets) {
      SortedSet<Long> newAsSet = ImmutableSortedSet.copyOf(asSet);
      newAsSets.add(newAsSet);
    }
    return newAsSets;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AsPath other = (AsPath) obj;
    if (_asSets == null) {
      if (other._asSets != null) {
        return false;
      }
    } else if (!_asSets.equals(other._asSets)) {
      return false;
    }
    return true;
  }

  public String getAsPathString() {
    StringBuilder sb = new StringBuilder();
    for (Set<Long> asSet : _asSets) {
      if (asSet.size() == 1) {
        long elem = asSet.iterator().next();
        sb.append(elem);
      } else {
        sb.append("{");
        Iterator<Long> i = asSet.iterator();
        sb.append(i.next());
        while (i.hasNext()) {
          sb.append(",");
          sb.append(i.next());
        }
        sb.append("}");
      }
      sb.append(" ");
    }
    return sb.toString().trim();
  }

  @JsonValue
  public List<SortedSet<Long>> getAsSets() {
    return copyAsSets(_asSets);
  }

  @Override
  public int hashCode() {
    return _asSets.hashCode();
  }

  public int size() {
    return _asSets.size();
  }

  @Override
  public String toString() {
    return _asSets.toString();
  }
}
