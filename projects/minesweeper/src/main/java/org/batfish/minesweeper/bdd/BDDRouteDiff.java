package org.batfish.minesweeper.bdd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** A class to compute and explain differences between two symbolic routes (BDDRoute). */
public final class BDDRouteDiff {

  public enum DifferenceType {
    OSPF_METRIC,
    LOCAL_PREF,
    COMMUNITIES,
    AS_PATH,
    MED,
    NEXTHOP,
    NEXTHOP_TYPE,
    NEXTHOP_SET,
    TAG,
    ADMIN_DIST,
    TUNNEL_ENCAPSULATION_ATTRIBUTE,
    WEIGHT,
    UNSUPPORTED
  }

  public static List<DifferenceType> computeDifferences(BDDRoute r1, BDDRoute r2) {
    List<DifferenceType> result = new ArrayList<>();

    // TODO: Ignore the OSPF metric until we add support in BgpRoute/BgpRouteDiff.
    //    if (!Objects.equals(r1.getOspfMetric(), r2.getOspfMetric())) {
    //      result.add(DifferenceType.OSPF_METRIC);
    //    }
    if (!Objects.equals(r1.getLocalPref(), r2.getLocalPref())) {
      result.add(DifferenceType.LOCAL_PREF);
    }
    if (!Arrays.equals(r1.getCommunityAtomicPredicates(), r2.getCommunityAtomicPredicates())) {
      result.add(DifferenceType.COMMUNITIES);
    }
    if (!Objects.equals(r1.getPrependedASes(), r2.getPrependedASes())) {
      result.add(DifferenceType.AS_PATH);
    }
    if (!Objects.equals(r1.getMed(), r2.getMed())) {
      result.add(DifferenceType.MED);
    }
    if (!Objects.equals(r1.getNextHop(), r2.getNextHop())) {
      result.add(DifferenceType.NEXTHOP);
    }
    if (!Objects.equals(r1.getNextHopType(), r2.getNextHopType())) {
      result.add(DifferenceType.NEXTHOP_TYPE);
    }
    if (!Objects.equals(r1.getNextHopSet(), r2.getNextHopSet())) {
      result.add(DifferenceType.NEXTHOP_SET);
    }
    if (!Objects.equals(r1.getTag(), r2.getTag())) {
      result.add(DifferenceType.TAG);
    }
    if (!Objects.equals(r1.getAdminDist(), r2.getAdminDist())) {
      result.add(DifferenceType.ADMIN_DIST);
    }
    if (!r1.getTunnelEncapsulationAttribute().equals(r2.getTunnelEncapsulationAttribute())) {
      result.add(DifferenceType.TUNNEL_ENCAPSULATION_ATTRIBUTE);
    }
    if (!Objects.equals(r1.getWeight(), r2.getWeight())) {
      result.add(DifferenceType.WEIGHT);
    }
    if (!Objects.equals(r1.getUnsupported(), r2.getUnsupported())) {
      result.add(DifferenceType.UNSUPPORTED);
    }
    return result;
  }
}
