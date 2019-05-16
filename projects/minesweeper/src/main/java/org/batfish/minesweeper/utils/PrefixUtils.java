package org.batfish.minesweeper.utils;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;

public class PrefixUtils {

  /*
   * Checks if two prefixes ever overlap
   */
  public static boolean overlap(Prefix p1, Prefix p2) {
    long l1 = p1.getStartIp().asLong();
    long l2 = p2.getStartIp().asLong();
    long u1 = p1.getEndIp().asLong();
    long u2 = p2.getEndIp().asLong();
    return (l1 >= l2 && l1 <= u2)
        || (u1 <= u2 && u1 >= l2)
        || (u2 >= l1 && u2 <= u1)
        || (l2 >= l1 && l2 <= u1);
  }

  public static boolean overlap(Prefix p, Collection<Prefix> ps) {
    for (Prefix p2 : ps) {
      if (overlap(p, p2)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isContainedBy(Prefix p, @Nullable Collection<Prefix> ps) {
    if (ps == null) {
      return false;
    }
    for (Prefix p2 : ps) {
      if (p2.containsPrefix(p)) {
        return true;
      }
    }
    return false;
  }

  public static SortedSet<IpWildcard> asPositiveIpWildcards(IpSpace ipSpace) {
    // TODO use an IpSpace visitor
    if (ipSpace == null) {
      return null;
    } else if (ipSpace instanceof IpWildcardIpSpace) {
      return ImmutableSortedSet.of(((IpWildcardIpSpace) ipSpace).getIpWildcard());
    } else if (ipSpace instanceof IpWildcardSetIpSpace) {
      return ((IpWildcardSetIpSpace) ipSpace).getWhitelist();
    } else if (ipSpace instanceof UniverseIpSpace) {
      return ImmutableSortedSet.of();
    } else {
      throw new BatfishException(
          String.format("Cannot represent as SortedSet<IpWildcard>: %s", ipSpace));
    }
  }

  public static SortedSet<IpWildcard> asNegativeIpWildcards(IpSpace ipSpace) {
    // TODO use an IpSpace visitor
    if (ipSpace == null) {
      return null;
    } else if (ipSpace instanceof IpWildcardIpSpace) {
      return ImmutableSortedSet.of(((IpWildcardIpSpace) ipSpace).getIpWildcard());
    } else if (ipSpace instanceof IpWildcardSetIpSpace) {
      return ((IpWildcardSetIpSpace) ipSpace).getWhitelist();
    } else if (ipSpace instanceof EmptyIpSpace) {
      return ImmutableSortedSet.of();
    } else {
      throw new BatfishException(
          String.format("Cannot represent as SortedSet<IpWildcard>: %s", ipSpace));
    }
  }
}
