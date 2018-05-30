package org.batfish.dataplane.protocols;

import javax.annotation.Nullable;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfInternalRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.dataplane.ibdp.Node;

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
    // Only update metric for different areas and if the area prefix contains the route prefix
    if (areaNum == route.getArea() || !areaPrefix.containsPrefix(contributingRoutePrefix)) {
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
   * Decide whether an inter-area OSPF route can be sent from neighbor's area to given area.
   *
   * @param areaNum "Our" area number
   * @param neighbor an adjacent {@link Node} with which route exchange is happening
   * @param neighborRoute {@link OspfInternalRoute} in questions
   * @param neighborArea neighbor's area number
   * @return true if the route should considered for installation into the OSPF RIB
   */
  public static boolean isOspfInterAreaFromInterAreaPropagationAllowed(
      long areaNum, Node neighbor, OspfInternalRoute neighborRoute, OspfArea neighborArea) {
    long neighborRouteAreaNum = neighborRoute.getArea();
    // May only propagate to or from area 0
    if (areaNum != neighborRouteAreaNum && areaNum != 0L && neighborRouteAreaNum != 0L) {
      return false;
    }
    Prefix neighborRouteNetwork = neighborRoute.getNetwork();
    String neighborSummaryFilterName = neighborArea.getSummaryFilter();
    boolean hasSummaryFilter = neighborSummaryFilterName != null;
    boolean allowed = !hasSummaryFilter;

    // If there is a summary filter, run the route through it
    if (hasSummaryFilter) {
      RouteFilterList neighborSummaryFilter =
          neighbor.getConfiguration().getRouteFilterLists().get(neighborSummaryFilterName);
      allowed = neighborSummaryFilter.permits(neighborRouteNetwork);
    }
    return allowed;
  }

  /**
   * Decide whether an intra-area OSPF route can be sent from neighbor's area to given area.
   *
   * @param areaNum "Our" area number
   * @param neighbor an adjacent {@link Node} with which route exchange is happening
   * @param neighborRoute {@link OspfInternalRoute} in questions
   * @param neighborArea neighbor's area number
   * @return true if the route should considered for installation into the OSPF RIB
   */
  public static boolean isOspfInterAreaFromIntraAreaPropagationAllowed(
      long areaNum, Node neighbor, OspfInternalRoute neighborRoute, OspfArea neighborArea) {
    long neighborRouteAreaNum = neighborRoute.getArea();
    // May only propagate to or from area 0
    if (areaNum == neighborRouteAreaNum || (areaNum != 0L && neighborRouteAreaNum != 0L)) {
      return false;
    }
    Prefix neighborRouteNetwork = neighborRoute.getNetwork();
    String neighborSummaryFilterName = neighborArea.getSummaryFilter();
    boolean hasSummaryFilter = neighborSummaryFilterName != null;
    boolean allowed = !hasSummaryFilter;

    // If there is a summary filter, run the route through it
    if (hasSummaryFilter) {
      RouteFilterList neighborSummaryFilter =
          neighbor.getConfiguration().getRouteFilterLists().get(neighborSummaryFilterName);
      allowed = neighborSummaryFilter.permits(neighborRouteNetwork);
    }
    return allowed;
  }
}
