package org.batfish.bdp;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Test;

public class VirtualRouterTest {

  @Test
  public void testGetBetterOspfRouteMetric() {
    long definedMetric = 5;
    long definedArea = 1;
    OspfInterAreaRoute route =
        new OspfInterAreaRoute(
            new Prefix("1.1.1.1/24"),
            Ip.MAX,
            RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
            definedMetric,
            0);

    // The route is in the prefix and existing metric is null, so return the route's metric
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(route, Prefix.ZERO, null, definedArea, true),
        equalTo(definedMetric));
    // Return the lower metric if the existing not null and using old RFC
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(route, Prefix.ZERO, 10L, definedArea, true),
        equalTo(definedMetric));
    // Return the higher metric if the existing metric is not null and using new RFC
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(route, Prefix.ZERO, 10L, definedArea, false),
        equalTo(10L));
    // The route is in the prefix but the existing metric is lower, so return the existing metric
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(route, Prefix.ZERO, 4L, definedArea, true),
        equalTo(4L));
    // The route is in the prefix but the existing metric is lower, so return the existing metric
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(route, Prefix.ZERO, 4L, definedArea, false),
        equalTo(definedMetric));
    // The route is not in the area's prefix, return the current metric
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(
            route, new Prefix("2.0.0.0/8"), 4L, definedArea, true),
        equalTo(4L));

    OspfInterAreaRoute sameAreaRoute =
        new OspfInterAreaRoute(
            new Prefix("1.1.1.1/24"),
            Ip.MAX,
            RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
            definedMetric,
            1); // the area is the same as definedArea
    // Thus the metric should remain null
    assertThat(
        VirtualRouter.computeUpdatedOspfSummaryMetric(
            sameAreaRoute, Prefix.ZERO, null, definedArea, true),
        equalTo(null));
  }
}
