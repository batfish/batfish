package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.protocols.OspfProtocolHelper.isOspfInterAreaDefaultOriginationAllowed;
import static org.batfish.dataplane.protocols.OspfProtocolHelper.isOspfInterAreaFromInterAreaPropagationAllowed;
import static org.batfish.dataplane.protocols.OspfProtocolHelper.isOspfInterAreaFromIntraAreaPropagationAllowed;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OspfInterAreaRoute;
import org.batfish.datamodel.OspfInternalRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.OspfProcess.Builder;
import org.batfish.datamodel.ospf.StubSettings;
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
        (OspfInterAreaRoute)
            OspfInternalRoute.builder()
                .setProtocol(RoutingProtocol.OSPF_IA)
                .setNetwork(ospfInterAreaRoutePrefix)
                .setNextHopIp(Ip.MAX)
                .setAdmin(
                    RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(
                        ConfigurationFormat.CISCO_IOS))
                .setMetric(definedMetric)
                .setArea(definedArea)
                .build();

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
        (OspfInterAreaRoute)
            OspfInternalRoute.builder()
                .setProtocol(RoutingProtocol.OSPF_IA)
                .setNetwork(ospfInterAreaRoutePrefix)
                .setNextHopIp(Ip.MAX)
                .setAdmin(
                    RoutingProtocol.OSPF_IA.getDefaultAdministrativeCost(
                        ConfigurationFormat.CISCO_IOS))
                .setArea(99L)
                .setMetric(definedMetric)
                .build();
    // The area is different from definedArea thus the metric should remain null
    assertThat(
        OspfProtocolHelper.computeUpdatedOspfSummaryMetric(
            sameAreaRoute, Prefix.ZERO, null, definedArea, true),
        nullValue());
  }

  @Test
  public void testisOspfInterAreaDefaultOriginationAllowed() {
    NetworkFactory nf = new NetworkFactory();
    Builder b = nf.ospfProcessBuilder();
    OspfProcess proc = b.build();
    OspfProcess neighborProc = b.build();
    OspfArea area0 = nf.ospfAreaBuilder().setNumber(0L).build();
    OspfArea.Builder area1 = nf.ospfAreaBuilder().setNumber(1L).setStubType(StubType.STUB);
    neighborProc.setAreas(ImmutableSortedMap.of(0L, area0, 1L, area1.build()));

    // Receiving process is NOT an ABR
    proc.setAreas(ImmutableSortedMap.of(1L, area1.build()));
    assertThat(
        isOspfInterAreaDefaultOriginationAllowed(proc, neighborProc, area0, area1.build()),
        equalTo(true));

    // Area1 is stub without default route injection
    area1.setInjectDefaultRoute(false);
    assertThat(
        isOspfInterAreaDefaultOriginationAllowed(proc, neighborProc, area0, area1.build()),
        equalTo(false));

    // Area injects the default route, but the receiving process IS an ABR
    area0 = OspfArea.builder(nf).setNumber(0L).setInjectDefaultRoute(true).build();
    proc.setAreas(ImmutableSortedMap.of(0L, area0, 1L, area1.build()));
    assertThat(
        isOspfInterAreaDefaultOriginationAllowed(proc, neighborProc, area0, area1.build()),
        equalTo(false));
  }

  @Test
  public void testIsOspfInterAreaFromIntraAreaPropagationAllowedWithSummary() {
    NetworkFactory nf = new NetworkFactory();
    Builder b = nf.ospfProcessBuilder();
    OspfProcess abrProc = b.build();
    final String FILTER_NAME = "FILTER";
    OspfArea area0 = nf.ospfAreaBuilder().setNumber(0L).setSummaryFilter(FILTER_NAME).build();
    OspfArea area1 =
        nf.ospfAreaBuilder()
            .setNumber(1L)
            .setStub(StubSettings.builder().setSuppressType3(true).build())
            .build();
    abrProc.setAreas(ImmutableSortedMap.of(0L, area0, 1L, area1));
    Configuration abrConfig =
        nf.configurationBuilder()
            .setHostname("abr")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    // Deny-all filter list (in the place of a summary filter list)
    abrConfig.setRouteFilterLists(
        ImmutableSortedMap.of(
            FILTER_NAME,
            new RouteFilterList(
                FILTER_NAME,
                ImmutableList.of(
                    new RouteFilterLine(
                        LineAction.DENY,
                        Prefix.ZERO,
                        new SubRange(0, Prefix.MAX_PREFIX_LENGTH))))));

    Prefix network = Prefix.parse("1.1.1.1/32");
    OspfIntraAreaRoute route =
        (OspfIntraAreaRoute)
            OspfInternalRoute.builder()
                .setProtocol(RoutingProtocol.OSPF)
                .setNetwork(network)
                .setNextHopIp(new Ip("9.9.9.9"))
                .setAdmin(20)
                .setMetric(10)
                .setArea(0L)
                .build();

    // Test: Area-0 route going from area 0 to area 1. Area 0 has a filter list suppressing routes.
    // Denied propagation because of the filter list.
    assertThat(
        isOspfInterAreaFromIntraAreaPropagationAllowed(1, abrConfig, abrProc, route, area1),
        equalTo(false));
  }

  @Test
  public void testisOspfInterAreaFromInterAreaPropagationAllowedType3Suppression() {
    NetworkFactory nf = new NetworkFactory();
    Builder b = nf.ospfProcessBuilder();
    OspfProcess proc = b.build();
    OspfProcess abrProc = b.build();
    OspfArea area0 = nf.ospfAreaBuilder().setNumber(0L).build();
    OspfArea area1 =
        nf.ospfAreaBuilder()
            .setNumber(1L)
            .setStub(StubSettings.builder().setSuppressType3(true).build())
            .build();
    abrProc.setAreas(ImmutableSortedMap.of(0L, area0, 1L, area1));
    Configuration abrConfig =
        nf.configurationBuilder()
            .setHostname("abr")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    Prefix network = Prefix.parse("1.1.1.1/32");
    OspfIntraAreaRoute route =
        (OspfIntraAreaRoute)
            OspfInternalRoute.builder()
                .setProtocol(RoutingProtocol.OSPF)
                .setNetwork(network)
                .setNextHopIp(new Ip("9.9.9.9"))
                .setAdmin(20)
                .setMetric(10)
                .setArea(1L)
                .build();

    // Test: route going from 0 to 1, no filter lists; denied propagation because of type3
    // suppression.
    assertThat(
        isOspfInterAreaFromInterAreaPropagationAllowed(proc, 0, abrConfig, abrProc, route, area1),
        equalTo(false));
  }
}
