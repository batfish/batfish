package org.batfish.dataplane.protocols;

import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OspfInternalRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.StubType;

/** Helper class with functions that implement various bits of OSPF protocol logic. */
public class OspfProtocolHelper {

  /**
   * Decides whether the current OSPF summary route metric needs to be changed based on the given
   * route's metric.
   *
   * <p>Routes from the same area or outside of areaPrefix have no effect on the summary metric.
   *
   * @param route The route in question, whose metric is considered
   * @param areaPrefix The Ip prefix of the OSPF area
   * @param currentMetric The current summary metric for the area
   * @param areaNum Area number.
   * @param useMin Whether to use the older RFC 1583 computation, which takes the minimum of metrics
   *     as opposed to the newer RFC 2328, which uses the maximum
   * @return the newly computed summary metric.
   */
  @Nullable
  public static Long computeUpdatedOspfSummaryMetric(
      OspfInternalRoute route,
      Prefix areaPrefix,
      @Nullable Long currentMetric,
      long areaNum,
      boolean useMin) {
    Prefix contributingRoutePrefix = route.getNetwork();
    // Only update metric for different areas that can contribute to the summary
    if (areaNum != route.getArea() || !areaPrefix.containsPrefix(contributingRoutePrefix)) {
      return currentMetric;
    }
    long contributingRouteMetric = route.getMetric();
    // Definitely update if we have no previous metric
    if (currentMetric == null) {
      return contributingRouteMetric;
    }
    // Take the best metric between the route's and current available.
    // Routers (at least Cisco and Juniper) default to min metric unless using RFC2328 with
    // RFC1583 compatibility explicitly disabled, in which case they default to max.
    if (useMin) {
      return Math.min(currentMetric, contributingRouteMetric);
    }
    return Math.max(currentMetric, contributingRouteMetric);
  }

  /**
   * Decide whether a default inter-area OSPF route should be originated by neighbor
   *
   * @param process the receiving node's {@link OspfProcess}
   * @param neighborProc The adjacent {@link OspfProcess}
   * @param neighborArea The propagator's OSPF area configuration
   * @return {@code true} iff the route should be considered for installation into the OSPF RIB
   */
  public static boolean isOspfInterAreaDefaultOriginationAllowed(
      OspfProcess process, OspfProcess neighborProc, OspfArea area, OspfArea neighborArea) {
    return neighborProc.isAreaBorderRouter()
        && !process.isAreaBorderRouter()
        && neighborArea.getInjectDefaultRoute()
        && (neighborArea.getStubType() == StubType.STUB
            || (neighborArea.getStubType() == StubType.NSSA
                && neighborArea.getNssa().getDefaultOriginateType()
                    == OspfDefaultOriginateType.INTER_AREA));
  }

  /**
   * Decide whether an inter-area OSPF route can be sent from neighbor's area to given area.
   *
   * @param proc The process of the receiver
   * @param linkAreaNum The area ID of the link
   * @param neighbor The adjacent node with which route exchange is happening
   * @param neighborProc The adjacent {@link OspfProcess}
   * @param neighborRoute {@link OspfInternalRoute} in questions
   * @param neighborArea The propagator's OSPF area configuration
   * @return {@code true} iff the route should be considered for installation into the OSPF RIB
   */
  public static boolean isOspfInterAreaFromInterAreaPropagationAllowed(
      OspfProcess proc,
      long linkAreaNum,
      Configuration neighbor,
      OspfProcess neighborProc,
      OspfInternalRoute neighborRoute,
      OspfArea neighborArea) {
    long neighborRouteAreaNum = neighborRoute.getArea();
    /*
     * Once an inter-area route has been propagated across a link of a given area,
     * it may continue to propagate throughout that area.
     * To propagate into a different area, the propagator *must* be an ABR.
     */

    if (linkAreaNum != neighborRouteAreaNum && !neighborProc.isAreaBorderRouter()) {
      // trying to cross an area boundary but neighbor is not an ABR
      return false;
    }
    /*
     * Don't propagate inter-area routes into a [not-so-stubby-]stub area for which type-3 LSAs
     * are suppressed. Stub types are known/configured at the *neighbor* because the neighbor is an ABR.
     */
    if ((neighborArea.getStubType() == StubType.STUB && neighborArea.getStub().getSuppressType3())
        || (neighborArea.getStubType() == StubType.NSSA
            && neighborArea.getNssa().getSuppressType3())) {
      return false;
    }

    // ABR should not accept OSPF internal default route
    if (proc.isAreaBorderRouter() && neighborRoute.getNetwork().equals(Prefix.ZERO)) {
      return false;
    }

    Prefix neighborRouteNetwork = neighborRoute.getNetwork();
    String neighborSummaryFilterName = neighborArea.getSummaryFilter();
    boolean hasSummaryFilter = neighborSummaryFilterName != null;
    boolean allowed = !hasSummaryFilter;

    // If there is a summary filter, run the route through it
    if (hasSummaryFilter) {
      RouteFilterList neighborSummaryFilter =
          neighbor.getRouteFilterLists().get(neighborSummaryFilterName);
      allowed = neighborSummaryFilter.permits(neighborRouteNetwork);
    }
    return allowed;
  }

  /**
   * Decide whether an intra-area OSPF route can be sent from neighbor's area to given area.
   *
   * @param linkAreaNum The area ID of the link
   * @param neighbor The adjacent node with which route exchange is happening
   * @param neighborProc The adjacent {@link OspfProcess}
   * @param neighborRoute {@link OspfInternalRoute} in questions
   * @param neighborArea The propagator's OSPF area configuration
   * @return {@code true} iff the route should considered for installation into the OSPF RIB
   */
  public static boolean isOspfInterAreaFromIntraAreaPropagationAllowed(
      long linkAreaNum,
      Configuration neighbor,
      OspfProcess neighborProc,
      OspfInternalRoute neighborRoute,
      OspfArea neighborArea) {
    long neighborRouteAreaNum = neighborRoute.getArea();
    // Not inter-area if link area and route area are same
    if (linkAreaNum == neighborRouteAreaNum) {
      return false;
    }

    // Only ABR (router with some link in area 0 and some link not in area 0) is allowed to
    // create inter-area route from intra-area route in another area.
    if (!neighborProc.isAreaBorderRouter()) {
      return false;
    }

    // Don't propagate inter-area routes into a [not-so-stubby-]stub area for which type-3 LSAs are
    // suppressed.
    if ((neighborArea.getStubType() == StubType.STUB && neighborArea.getStub().getSuppressType3())
        || (neighborArea.getStubType() == StubType.NSSA
            && neighborArea.getNssa().getSuppressType3())) {
      return false;
    }

    /*
     * Neighbor proc is an ABR, so *it* will the have summary filters configured.
     * Get the summary filter for the **route's area** on the neighbor, because a filter
     * on the link's are does not suppress routes for other areas.
     */
    Prefix neighborRouteNetwork = neighborRoute.getNetwork();
    String neighborSummaryFilterName =
        neighborProc.getAreas().get(neighborRouteAreaNum).getSummaryFilter();
    boolean hasSummaryFilter = neighborSummaryFilterName != null;
    boolean allowed = !hasSummaryFilter;

    // If there is a summary filter, run the route through it
    if (hasSummaryFilter) {
      RouteFilterList neighborSummaryFilter =
          neighbor.getRouteFilterLists().get(neighborSummaryFilterName);
      allowed = neighborSummaryFilter.permits(neighborRouteNetwork);
    }
    return allowed;
  }
}
