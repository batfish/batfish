package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;

/**
 * Compute the intersection of two HeaderSpaces. Tries to detect empty intersections, but isn't
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

  @Nullable
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
    checkArgument(isUnconstrained(h1.getSrcOrDstIps()));
    checkArgument(isUnconstrained(h2.getSrcOrDstIps()));
    checkArgument(isUnconstrained(h1.getSrcOrDstPorts()));
    checkArgument(isUnconstrained(h2.getSrcOrDstPorts()));
    checkArgument(isUnconstrained(h1.getSrcOrDstProtocols()));
    checkArgument(isUnconstrained(h2.getSrcOrDstProtocols()));

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
    throw new UnsupportedOperationException("Nontrivial intersection of TcpFlagMatchConditions");
  }

  private static boolean isUnconstrained(IpSpace srcOrDstIps) {
    return srcOrDstIps == null;
  }

  private static <T> boolean isUnconstrained(@Nullable Set<T> set) {
    return set == null || set.isEmpty();
  }

  static <T extends Comparable<T>> SortedSet<T> intersectSimpleSets(
      SortedSet<T> s1, SortedSet<T> s2) throws NoIntersection {
    if (isUnconstrained(s1)) {
      return s2;
    }
    if (isUnconstrained(s2)) {
      return s1;
    }
    SortedSet<T> intersection =
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
    if (isUnconstrained(s1)) {
      return s2;
    }
    if (isUnconstrained(s2)) {
      return s1;
    }
    SortedSet<SubRange> intersection =
        s1.stream()
            .flatMap(r1 -> s2.stream().map(r1::intersection))
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
}
