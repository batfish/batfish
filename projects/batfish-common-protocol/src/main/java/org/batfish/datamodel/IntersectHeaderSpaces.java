package org.batfish.datamodel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

/**
 * Compute the intersection of two HeaderSpaces.  Tries to detect empty intersections, but isn't
 * always able to. In particular, empty intersections of {@link IpSpace} fields are not detected.
 */
public final class IntersectHeaderSpaces {
  private IntersectHeaderSpaces() {}

  static final class NoIntersection extends Exception {
    public static final long serialVersionUID = 1;
  }

  private static boolean isUniverse(@Nullable IpSpace ipSpace) {
    return ipSpace == null
        || ipSpace == UniverseIpSpace.INSTANCE
        || ipSpace.equals(IpWildcard.ANY.toIpSpace())
        || ipSpace.equals(new IpWildcardIpSpace(IpWildcard.ANY));
  }

  private static IpSpace intersection(IpSpace ipSpace1, IpSpace ipSpace2) {
    if (isUniverse(ipSpace1)) {
      return ipSpace2;
    }
    if (isUniverse(ipSpace2)) {
      return ipSpace1;
    }
    return AclIpSpace.intersection(ipSpace1, ipSpace2);
  }

  public static Optional<HeaderSpace> intersect(HeaderSpace h1, HeaderSpace h2) {
    Preconditions.checkArgument(unconstrained(h1.getSrcOrDstIps()));
    Preconditions.checkArgument(unconstrained(h2.getSrcOrDstIps()));
    Preconditions.checkArgument(unconstrained(h1.getSrcOrDstPorts()));
    Preconditions.checkArgument(unconstrained(h2.getSrcOrDstPorts()));
    Preconditions.checkArgument(unconstrained(h1.getSrcOrDstProtocols()));
    Preconditions.checkArgument(unconstrained(h2.getSrcOrDstProtocols()));

    try {
      return Optional.of(
          HeaderSpace.builder()
              .setDscps(intersectSimpleSets(h1.getDscps(), h2.getDscps()))
              .setDstIps(intersection(h1.getDstIps(), h2.getDstIps()))
              .setDstPorts(intersectSubRangeSets(h1.getDstPorts(), h2.getDstPorts()))
              .setDstProtocols(intersectSimpleSets(h1.getDstProtocols(), h2.getDstProtocols()))
              .setIpProtocols(intersectSimpleSets(h1.getIpProtocols(), h2.getIpProtocols()))
              .setIcmpCodes(intersectSubRangeSets(h1.getIcmpCodes(), h2.getIcmpCodes()))
              .setIcmpTypes(intersectSubRangeSets(h1.getIcmpTypes(), h2.getIcmpTypes()))
              .setNotDstIps(AclIpSpace.union(h1.getNotDstIps(), h2.getNotDstIps()))
              .setNotDstPorts(Sets.union(h1.getNotDstPorts(), h2.getNotDstPorts()))
              .setNotSrcIps(AclIpSpace.union(h1.getNotSrcIps(), h2.getNotSrcIps()))
              .setNotSrcPorts(Sets.union(h1.getNotSrcPorts(), h2.getNotSrcPorts()))
              .setSrcIps(AclIpSpace.intersection(h1.getSrcIps(), h2.getSrcIps()))
              .setSrcOrDstPorts(intersectSubRangeSets(h1.getSrcOrDstPorts(), h2.getSrcOrDstPorts()))
              .setSrcPorts(intersectSubRangeSets(h1.getSrcPorts(), h2.getSrcPorts()))
              .setTcpFlags(intersectTcpFlagMatchConditions(h1.getTcpFlags(), h2.getTcpFlags()))
              .build());
    } catch (NoIntersection e) {
      return Optional.empty();
    }
  }

  private static Iterable<TcpFlagsMatchConditions> intersectTcpFlagMatchConditions(
      List<TcpFlagsMatchConditions> tcpFlags1, List<TcpFlagsMatchConditions> tcpFlags2) {
    if (tcpFlags1 == null || tcpFlags1.isEmpty()) {
      return tcpFlags2;
    }
    if (tcpFlags2 == null || tcpFlags2.isEmpty()) {
      return tcpFlags1;
    }
    throw new BatfishException(
        "Not implemented: nontrivial intersection of TcpFlagMatchConditions");
  }

  private static boolean unconstrained(IpSpace srcOrDstIps) {
    return srcOrDstIps == null;
  }

  private static <A> boolean unconstrained(@Nullable Set<A> set) {
    return set == null || set.isEmpty();
  }

  static <A extends Comparable<A>> SortedSet<A> intersectSimpleSets(
      SortedSet<A> s1, SortedSet<A> s2) throws NoIntersection {
    if (s1.isEmpty()) {
      return s2;
    }
    if (s2.isEmpty()) {
      return s1;
    }
    SortedSet<A> intersection =
        s1.stream()
            .filter(s2::contains)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
    if (intersection.isEmpty()) {
      throw new NoIntersection();
    }
    return intersection;
  }

  static SortedSet<SubRange> intersectSubRangeSets(SortedSet<SubRange> s1, SortedSet<SubRange> s2)
      throws NoIntersection {
    if (s1.isEmpty()) {
      // s1 is unconstrained
      return s2;
    }
    if (s2.isEmpty()) {
      // s2 is unconstrained
      return s1;
    }

    SortedSet<SubRange> intersection =
        s1.stream()
            .flatMap(r1 -> s2.stream().map(r2 -> intersect(r1, r2)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));

    /* We started with two sets of constraints. If the intersection is empty, then the constraint
     * is unsatisfiable.
     */
    if (intersection.isEmpty()) {
      throw new NoIntersection();
    }
    return intersection;
  }

  private static Optional<SubRange> intersect(SubRange r1, SubRange r2) {
    int start = Integer.max(r1.getStart(), r2.getStart());
    int end = Integer.min(r1.getEnd(), r2.getEnd());
    return start <= end ? Optional.of(new SubRange(start, end)) : Optional.empty();
  }
}
