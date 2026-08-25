package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3ExternalSummary;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.batfish.datamodel.ospf.Ospfv3VirtualLink;
import org.junit.Test;

/** Tests for {@link Ospfv3Process}. */
public final class Ospfv3ProcessTest {

  @Test
  public void testDefaults() {
    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .build();

    assertThat(
        process.getAdminCost(),
        equalTo(Ospfv3Process.DEFAULT_ADMIN_COST));
    assertThat(
        process.getIntraAreaAdminCost(),
        equalTo(Ospfv3Process.DEFAULT_ADMIN_COST));
    assertThat(
        process.getInterAreaAdminCost(),
        equalTo(Ospfv3Process.DEFAULT_ADMIN_COST));
    assertThat(
        process.getExternalAdminCost(),
        equalTo(Ospfv3Process.DEFAULT_ADMIN_COST));
    assertThat(
        process.getEnabled(),
        equalTo(true));
    assertThat(
        process.getActiveBackboneStubDefaultRoute(),
        equalTo(true));
    assertThat(
        process.getReferenceBandwidth(),
        equalTo(
            Ospfv3Process.DEFAULT_REFERENCE_BANDWIDTH));
    assertThat(
        process.getExternalSummaries(),
        equalTo(ImmutableSet.of()));
    assertThat(
        process.getVirtualLinks(),
        equalTo(ImmutableSet.of()));
    assertThat(
        process.getRedistributeConnected(),
        equalTo(false));
    assertThat(
        process.getRedistributeLocalLoopback(),
        equalTo(false));
    assertThat(
        process.getRedistributeLocalLoopbackRouteMap(),
        equalTo(null));
    assertThat(
        process.getRedistributeStatic(),
        equalTo(false));
    assertThat(
        process.getRedistributionMetric(),
        equalTo(
            Ospfv3Process.DEFAULT_REDISTRIBUTION_METRIC));
    assertThat(
        process.getDefaultInformationOriginate(),
        equalTo(false));
    assertThat(
        process.getDefaultInformationOriginateAlways(),
        equalTo(false));
    assertThat(
        process.getDefaultInformationMetric(),
        equalTo(
            Ospfv3Process.DEFAULT_INFORMATION_METRIC));
  }

  @Test
  public void testSerialization() {
    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    33L,
                    44L)));

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface("Ethernet1")
            .setStub(true)
            .setSuppressInterArea(true)
            .setDefaultMetric(17L)
            .build();

    Ospfv3Area nssaArea =
        Ospfv3Area.builder()
            .setNumber(1L)
            .addInterface("Ethernet2")
            .setNssa(true)
            .setSuppressInterArea(true)
            .setDefaultMetric(19L)
            .build();

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .setAreas(
                ImmutableMap.of(
                    0L, area,
                    1L, nssaArea))
            .setAdminCost(111)
            .setInterAreaAdminCost(112)
            .setExternalAdminCost(113)
            .setEnabled(false)
            .setActiveBackboneStubDefaultRoute(
                false)
            .setVirtualLinks(
                ImmutableSet.of(
                    new Ospfv3VirtualLink(
                        1L,
                        Ip.parse(
                            "192.0.2.200"))))
            .setExternalSummaries(
                ImmutableSet.of(
                    new Ospfv3ExternalSummary(
                        Prefix6.parse(
                            "2001:db8:100::/48"),
                        true,
                        1234L),
                    new Ospfv3ExternalSummary(
                        Prefix6.parse(
                            "2001:db8:200::/48"),
                        false,
                        null)))
            .setReferenceBandwidth(40_000_000_000D)
            .setRedistributeConnected(true)
            .setRedistributeConnectedRouteMap(
                routeMap)
            .setRedistributeLocalLoopback(true)
            .setRedistributeLocalLoopbackRouteMap(
                routeMap)
            .setRedistributeStatic(true)
            .setRedistributeStaticRouteMap(
                routeMap)
            .setRedistributionMetric(37L)
            .setDefaultInformationOriginate(true)
            .setDefaultInformationOriginateAlways(true)
            .setDefaultInformationMetric(9L)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            process, Ospfv3Process.class);

    assertThat(clone.getProcessId(), equalTo("1"));
    assertThat(
        clone.getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        clone.getAreas().keySet(),
        equalTo(process.getAreas().keySet()));
    assertThat(
        clone.getAreas().get(0L).getStub(),
        equalTo(true));
    assertThat(
        clone.getAreas()
            .get(0L)
            .getSuppressInterArea(),
        equalTo(true));
    assertThat(
        clone.getAreas()
            .get(0L)
            .getDefaultMetric(),
        equalTo(17L));

    assertThat(
        clone.getAreas()
            .get(1L)
            .getNssa(),
        equalTo(true));

    assertThat(
        clone.getAreas()
            .get(1L)
            .getStub(),
        equalTo(false));

    assertThat(
        clone.getAreas()
            .get(1L)
            .getSuppressInterArea(),
        equalTo(true));

    assertThat(
        clone.getAreas()
            .get(1L)
            .getDefaultMetric(),
        equalTo(19L));
    assertThat(clone.getAdminCost(), equalTo(111));
    assertThat(
        clone.getIntraAreaAdminCost(),
        equalTo(111));
    assertThat(
        clone.getInterAreaAdminCost(),
        equalTo(112));
    assertThat(
        clone.getExternalAdminCost(),
        equalTo(113));
    assertThat(
        clone.getEnabled(),
        equalTo(false));
    assertThat(
        clone.getActiveBackboneStubDefaultRoute(),
        equalTo(false));
    assertThat(
        clone.getReferenceBandwidth(),
        equalTo(40_000_000_000D));
    assertThat(
        clone.getExternalSummaries(),
        equalTo(
            process.getExternalSummaries()));
    assertThat(
        clone.getVirtualLinks(),
        equalTo(
            process.getVirtualLinks()));
    assertThat(
        clone.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        clone.getRedistributeStatic(),
        equalTo(true));
    assertThat(
        clone.getRedistributeConnectedRouteMap(),
        equalTo(routeMap));
    assertThat(
        clone.getRedistributeLocalLoopback(),
        equalTo(true));
    assertThat(
        clone.getRedistributeLocalLoopbackRouteMap(),
        equalTo(routeMap));
    assertThat(
        clone.getRedistributeStaticRouteMap(),
        equalTo(routeMap));
    assertThat(
        clone.getRedistributionMetric(),
        equalTo(37L));
    assertThat(
        clone.getDefaultInformationOriginate(),
        equalTo(true));
    assertThat(
        clone.getDefaultInformationOriginateAlways(),
        equalTo(true));
    assertThat(
        clone.getDefaultInformationMetric(),
        equalTo(9L));
  }
  @Test
  public void testDistributeListSerialization() {
    PrefixList6 prefixList =
        new PrefixList6(
            java.util.List.of(
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.ZERO,
                    new SubRange(
                        0, 128))));

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(
                Ip.parse("192.0.2.1"))
            .setInboundDistributeList(
                prefixList)
            .setOutboundDistributeList(
                prefixList)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            process,
            Ospfv3Process.class);

    assertThat(
        clone.getInboundDistributeList(),
        equalTo(prefixList));

    assertThat(
        clone.getOutboundDistributeList(),
        equalTo(prefixList));
  }

  @Test
  public void testMaximumPathsDefaultsAndSerialization() {
    Ospfv3Process defaults =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(
                Ip.parse("192.0.2.1"))
            .build();

    assertThat(
        defaults.getMaximumPaths(),
        equalTo(
            Ospfv3Process
                .DEFAULT_MAXIMUM_PATHS));

    Ospfv3Process configured =
        Ospfv3Process.builder()
            .setProcessId("2")
            .setRouterId(
                Ip.parse("192.0.2.2"))
            .setMaximumPaths(12)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            configured,
            Ospfv3Process.class);

    assertThat(
        clone.getMaximumPaths(),
        equalTo(12));
  }

  @Test
  public void testMaxMetricRouterLsaSerialization() {

    Ospfv3Process defaults =
        Ospfv3Process.builder()
            .setProcessId("default")
            .setRouterId(
                Ip.parse(
                    "192.0.2.1"))
            .build();

    assertThat(
        defaults.getMaxMetricRouterLsa(),
        equalTo(false));

    assertThat(
        defaults
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(null));

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("configured")
            .setRouterId(
                Ip.parse(
                    "192.0.2.2"))
            .setMaxMetricRouterLsa(true)
            .setMaxMetricRouterLsaOnStartupSeconds(
                3000)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            process,
            Ospfv3Process.class);

    assertThat(
        clone.getMaxMetricRouterLsa(),
        equalTo(true));

    assertThat(
        clone
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(3000));
  }

  @Test
  public void testOspfProcessRedistributionSerialization() {

    Ospfv3Process defaults =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(
                Ip.parse(
                    "192.0.2.1"))
            .build();

    assertThat(
        defaults
            .getRedistributeOspfProcesses(),
        equalTo(
            ImmutableSet.of()));

    assertThat(
        defaults
            .getRedistributeOspfRouteMaps(),
        equalTo(
            ImmutableMap.of()));

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    44L,
                    999L)));

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(
                Ip.parse(
                    "192.0.2.1"))
            .setRedistributeOspfProcesses(
                ImmutableSet.of(
                    "2",
                    "3"))
            .setRedistributeOspfRouteMaps(
                ImmutableMap.of(
                    "2",
                    routeMap))
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            process,
            Ospfv3Process.class);

    assertThat(
        clone
            .getRedistributeOspfProcesses(),
        equalTo(
            ImmutableSet.of(
                "2",
                "3")));

    assertThat(
        clone
            .getRedistributeOspfRouteMaps(),
        equalTo(
            ImmutableMap.of(
                "2",
                routeMap)));
  }

  @Test
  public void testVirtualLinkAuthenticationSerialization() {

    org.batfish.datamodel.ospf.Ospfv3Authentication
        authentication =
            new org.batfish.datamodel.ospf.Ospfv3Authentication(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .KeyType.PLAINTEXT,
                "vlink-secret");

    Ospfv3VirtualLink link =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.2"),
            authentication);

    Ospfv3VirtualLink clone =
        BatfishObjectMapper.clone(
            link,
            Ospfv3VirtualLink.class);

    assertThat(
        clone,
        equalTo(
            link));

    assertThat(
        clone.getAuthentication(),
        equalTo(
            authentication));

    /*
     * Existing two-argument constructor remains unauthenticated.
     */
    assertThat(
        new Ospfv3VirtualLink(
                1L,
                Ip.parse(
                    "192.0.2.2"))
            .getAuthentication(),
        equalTo(
            null));
  }

  @Test
  public void testVirtualLinkEncryptionSerialization() {

    org.batfish.datamodel.ospf.Ospfv3Encryption
        encryption =
            new org.batfish.datamodel.ospf.Ospfv3Encryption(
                400L,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "vlink-auth",
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .EncryptionType.AES,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "0123456789abcdef");

    Ospfv3VirtualLink link =
        new Ospfv3VirtualLink(
            1L,
            Ip.parse(
                "192.0.2.2"),
            null,
            encryption);

    Ospfv3VirtualLink clone =
        BatfishObjectMapper.clone(
            link,
            Ospfv3VirtualLink.class);

    assertThat(
        clone,
        equalTo(
            link));

    assertThat(
        clone.getAuthentication(),
        equalTo(
            null));

    assertThat(
        clone.getEncryption(),
        equalTo(
            encryption));

    /*
     * Existing two- and three-argument constructors continue to mean
     * no ESP encryption unless explicitly configured.
     */
    assertThat(
        new Ospfv3VirtualLink(
                1L,
                Ip.parse(
                    "192.0.2.2"))
            .getEncryption(),
        equalTo(
            null));

    assertThat(
        new Ospfv3VirtualLink(
                1L,
                Ip.parse(
                    "192.0.2.2"),
                null)
            .getEncryption(),
        equalTo(
            null));
  }

  @Test
  public void testGracefulRestartDefaultsAndSerialization() {

    Ospfv3Process defaults =
        Ospfv3Process.builder()
            .setProcessId(
                "1")
            .setRouterId(
                Ip.parse(
                    "192.0.2.1"))
            .build();

    assertThat(
        defaults
            .getGracefulRestartIntervalSeconds(),
        equalTo(
            Ospfv3Process
                .DEFAULT_GRACEFUL_RESTART_INTERVAL_SECONDS));

    assertThat(
        defaults.getGracefulRestartHelper(),
        equalTo(
            false));

    assertThat(
        defaults
            .getGracefulRestartHelperStrictLsaCheck(),
        equalTo(
            false));

    assertThat(
        defaults
            .getGracefulRestartIgnoreLostInterface(),
        equalTo(
            false));

    Ospfv3Process configured =
        Ospfv3Process.builder()
            .setProcessId(
                "2")
            .setRouterId(
                Ip.parse(
                    "192.0.2.2"))
            .setGracefulRestartIntervalSeconds(
                40)
            .setGracefulRestartHelper(
                true)
            .setGracefulRestartHelperStrictLsaCheck(
                true)
            .setGracefulRestartIgnoreLostInterface(
                true)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            configured,
            Ospfv3Process.class);

    assertThat(
        clone.getGracefulRestartIntervalSeconds(),
        equalTo(
            40));

    assertThat(
        clone.getGracefulRestartHelper(),
        equalTo(
            true));

    assertThat(
        clone
            .getGracefulRestartHelperStrictLsaCheck(),
        equalTo(
            true));

    assertThat(
        clone
            .getGracefulRestartIgnoreLostInterface(),
        equalTo(
            true));
  }

  @Test
  public void testConvergenceTimerDefaultsAndSerialization() {

    Ospfv3Process defaults =
        Ospfv3Process.builder()
            .setProcessId(
                "1")
            .setRouterId(
                Ip.parse(
                    "192.0.2.1"))
            .build();

    assertThat(
        defaults.getSpfThrottleStartTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_SPF_THROTTLE_START_TIME_MS));

    assertThat(
        defaults.getSpfThrottleHoldTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_SPF_THROTTLE_HOLD_TIME_MS));

    assertThat(
        defaults.getSpfThrottleMaxWaitTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_SPF_THROTTLE_MAX_WAIT_TIME_MS));

    assertThat(
        defaults.getLsaThrottleStartTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_LSA_THROTTLE_START_TIME_MS));

    assertThat(
        defaults.getLsaThrottleHoldTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_LSA_THROTTLE_HOLD_TIME_MS));

    assertThat(
        defaults.getLsaThrottleMaxWaitTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_LSA_THROTTLE_MAX_WAIT_TIME_MS));

    assertThat(
        defaults.getLsaArrivalTimeMs(),
        equalTo(
            Ospfv3Process
                .DEFAULT_LSA_ARRIVAL_TIME_MS));

    Ospfv3Process configured =
        Ospfv3Process.builder()
            .setProcessId(
                "2")
            .setRouterId(
                Ip.parse(
                    "192.0.2.2"))
            .setSpfThrottleTimers(
                10,
                20,
                30)
            .setLsaThrottleTimers(
                100,
                200,
                300)
            .setLsaArrivalTimeMs(
                400)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            configured,
            Ospfv3Process.class);

    assertThat(
        clone.getSpfThrottleStartTimeMs(),
        equalTo(10));

    assertThat(
        clone.getSpfThrottleHoldTimeMs(),
        equalTo(20));

    assertThat(
        clone.getSpfThrottleMaxWaitTimeMs(),
        equalTo(30));

    assertThat(
        clone.getLsaThrottleStartTimeMs(),
        equalTo(100));

    assertThat(
        clone.getLsaThrottleHoldTimeMs(),
        equalTo(200));

    assertThat(
        clone.getLsaThrottleMaxWaitTimeMs(),
        equalTo(300));

    assertThat(
        clone.getLsaArrivalTimeMs(),
        equalTo(400));
  }

}
