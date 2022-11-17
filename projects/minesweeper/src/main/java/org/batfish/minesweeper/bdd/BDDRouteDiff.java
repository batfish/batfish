package org.batfish.minesweeper.bdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BDDRouteDiff {

  public class Difference {
    private final BDDRoute r1;
    private final BDDRoute r2;
    private final DifferenceType type;

    public Difference(BDDRoute leftVal, BDDRoute rightVal, DifferenceType type) {
      this.r1 = leftVal;
      this.r2 = rightVal;
      this.type = type;
    }

    public BDDRoute getR1() {
      return r1;
    }

    public BDDRoute getR2() {
      return r2;
    }

    public DifferenceType getType() {
      return type;
    }
  }

  public enum DifferenceType {
    OSPF_METRIC,
    LOCAL_PREF,
    COMMUNITIES,
    AS_PATH,
    MED,
    NEXTHOP,
    NEXTHOP_DISCARDED,
    NEXTHOP_SET,
    TAG,
    ADMIN_DIST,
    UNSUPPORTED
  }

  public List<Difference> computeDifferences(BDDRoute r1, BDDRoute r2) {
    List<Difference> result = new ArrayList<>();

    // TODO: Ignore the OSPF metric until we add support in BgpRoute/BgpRouteDiff.
//    if (!Objects.equals(r1.getOspfMetric(), r2.getOspfMetric())) {
//      result.add(new Difference(r1, r2, DifferenceType.OSPF_METRIC));
//    }
    if (!Objects.equals(r1.getLocalPref(), (r2.getLocalPref()))) {
      result.add(new Difference(r1, r2, DifferenceType.LOCAL_PREF));
    }
    if (!Arrays.equals(r1.getCommunityAtomicPredicates(), r2.getCommunityAtomicPredicates())) {
      result.add(new Difference(r1, r2, DifferenceType.COMMUNITIES));
    }
    if (!Arrays.equals(r1.getAsPathRegexAtomicPredicates(), r2.getAsPathRegexAtomicPredicates())) {
      result.add(new Difference(r1, r2, DifferenceType.AS_PATH));
    }
    if (!Objects.equals(r1.getMed(), r2.getMed())) {
      result.add(new Difference(r1, r2, DifferenceType.MED));
    }
    if (!Objects.equals(r1.getNextHop(), r2.getNextHop())) {
      result.add(new Difference(r1, r2, DifferenceType.NEXTHOP));
    }
    if (!Objects.equals(r1.getNextHopDiscarded(), r2.getNextHopDiscarded())) {
      result.add(new Difference(r1, r2, DifferenceType.NEXTHOP_DISCARDED));
    }
    if (!Objects.equals(r1.getNextHopSet(), r2.getNextHopSet())) {
      result.add(new Difference(r1, r2, DifferenceType.NEXTHOP_SET));
    }
    if (!Objects.equals(r1.getTag(), r2.getTag())) {
      result.add(new Difference(r1, r2, DifferenceType.TAG));
    }
    //TODO: Administrative distance is not properly supported in TRP/RouteDiff.
//    if (!Objects.equals(r1.getAdminDist(), r2.getAdminDist())) {
//      result.add(new Difference(r1, r2, DifferenceType.ADMIN_DIST));
//    }
    if (!Objects.equals(r1.getUnsupported(), r2.getUnsupported())) {
      result.add(new Difference(r1, r2, DifferenceType.UNSUPPORTED));
    }
    return result;
  }
}
