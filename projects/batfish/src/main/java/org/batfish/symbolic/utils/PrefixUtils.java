package org.batfish.symbolic.utils;

import java.util.Collection;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public class PrefixUtils {

  /*
 * Checks if a prefix overlaps with the destination in a headerspace
 */
  public static boolean overlap(HeaderSpace h, Prefix p) {
    if (h.getDstIps().isEmpty()) {
      return true;
    }
    for (IpWildcard ipWildcard : h.getDstIps()) {
      Prefix p2 = ipWildcard.toPrefix();
      if (overlap(p, p2)) {
        return true;
      }
    }
    return false;
  }

  /*
   * Checks if two prefixes ever overlap
   */
  public static boolean overlap(Prefix p1, Prefix p2) {
    long l1 = p1.getNetworkPrefix().getAddress().asLong();
    long l2 = p2.getNetworkPrefix().getAddress().asLong();
    long u1 = p1.getNetworkPrefix().getEndAddress().asLong();
    long u2 = p2.getNetworkPrefix().getEndAddress().asLong();
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
    p = p.getNetworkPrefix();
    for (Prefix p2 : ps) {
      p2 = p2.getNetworkPrefix();
      if (p2.containsPrefix(p) || p2.equals(p)) {
        return true;
      }
    }
    return false;
  }

}
