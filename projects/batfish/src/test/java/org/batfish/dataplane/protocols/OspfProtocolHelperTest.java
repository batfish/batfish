package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.protocols.OspfProtocolHelper.isOspfInterAreaDefaultOriginationAllowed;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfProcess.Builder;
import org.batfish.datamodel.ospf.StubType;
import org.junit.Test;

/** Tests for {@link OspfProtocolHelper} */
public class OspfProtocolHelperTest {

  /** Tests for proper handling of RFC 2328 */
  @Test
  public void testGetBetterOspfRouteMetric() {
    Prefix ospfInterAreaRoutePrefix = Prefix.parse("1.1.1.1/24");
    long definedMetric = 5;
    long definedArea = 1;
    OspfInterAreaRoute route =
        new OspfInterAreaRoute(
            ospfInterAreaRoutePrefix,
            Ip.MAX,
            RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
            definedMetric,
            0);

    // The route is in the prefix and existing metric is null, so return the route's metric
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.ZERO, null, definedArea, true),
        equalTo(definedMetric));
    // Return the lower metric if the existing not null and using old RFC
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.ZERO, 10L, definedArea, true),
        equalTo(definedMetric));
    // Return the higher metric if the existing metric is not null and using new RFC
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.ZERO, 10L, definedArea, false),
        equalTo(10L));
    // The route is in the prefix but the existing metric is lower, so return the existing metric
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.ZERO, 4L, definedArea, true),
        equalTo(4L));
    // The route is in the prefix but the existing metric is lower, so return the existing metric
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.ZERO, 4L, definedArea, false),
        equalTo(definedMetric));
    // The route is not in the area's prefix, return the current metric
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            route, Prefix.parse("2.0.0.0/8"), 4L, definedArea, true),
        equalTo(4L));

    OspfInterAreaRoute sameAreaRoute =
        new OspfInterAreaRoute(
            ospfInterAreaRoutePrefix,
            Ip.MAX,
            RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS),
            definedMetric,
            1); // the area is the same as definedArea
    // Thus the metric should remain null
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            sameAreaRoute, Prefix.ZERO, null, definedArea, true),
        equalTo(null));
  }

  @Test
  public void testisOspfInterAreaDefaultOriginationAllowed() {
    NetworkFactory nf = new NetworkFactory();
    Builder b = nf.ospfProcessBuilder();
    OspfProcess proc = b.build();
    OspfProcess neighborProc = b.build();
    OspfArea area1 = new OspfArea(1L);
    area1.setStubType(StubType.STUB);
    neighborProc.setAreas(ImmutableSortedMap.of(0L, new OspfArea(0L), 1L, area1));

    // Receiving process is NOT an ABR
    proc.setAreas(ImmutableSortedMap.of(1L, area1));
    assertThat(isOspfInterAreaDefaultOriginationAllowed(proc, neighborProc, area1), equalTo(true));

    // Receiving process IS an ABR
    proc.setAreas(ImmutableSortedMap.of(0L, new OspfArea(0L), 1L, area1));
    assertThat(isOspfInterAreaDefaultOriginationAllowed(proc, neighborProc, area1), equalTo(false));
  }
}
