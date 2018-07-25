package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.RoutingProtocol.EIGRP;
import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;
import static org.batfish.representation.cisco.Interface.getDefaultBandwidth;
import static org.batfish.representation.cisco.Interface.getDefaultDelay;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Collections;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.junit.Ignore;
import org.junit.Test;

public class EigrpTest {

  private static final InterfaceAddress R1_E1_2_ADDR = new InterfaceAddress("10.12.0.1/24");
  private static final InterfaceAddress R1_L0_ADDR = new InterfaceAddress("1.1.1.1/32");
  private static final String R1 = "R1";

  private static final InterfaceAddress R2_E2_1_ADDR = new InterfaceAddress("10.12.0.2/24");
  private static final InterfaceAddress R2_E2_3_ADDR = new InterfaceAddress("10.23.0.2/24");
  private static final InterfaceAddress R2_L0_ADDR = new InterfaceAddress("2.2.2.2/32");
  private static final String R2 = "R2";

  private static final InterfaceAddress R3_E3_2_ADDR = new InterfaceAddress("10.23.0.3/24");
  private static final InterfaceAddress R3_E3_4_ADDR = new InterfaceAddress("10.34.0.3/24");
  private static final InterfaceAddress R3_L0_ADDR = new InterfaceAddress("3.3.3.3/32");
  private static final String R3 = "R3";

  private static final InterfaceAddress R4_E4_3_ADDR = new InterfaceAddress("10.34.0.4/24");
  private static final InterfaceAddress R4_L0_ADDR = new InterfaceAddress("4.4.4.4/32");
  private static final String R4 = "R4";

  /*
   * Int:1/2   2/1      2/3   3/2      3/4   4/3
   * R1 <=========> R2 <=========> R3 <=========> R4
   */
  private static IncrementalDataPlane computeDataPlane(
      EigrpProcessMode mode1,
      EigrpProcessMode mode2,
      EigrpProcessMode mode3,
      EigrpProcessMode mode4,
      String interfacePrefix) {

    long asn = 1L;
    ConfigurationFormat format = ConfigurationFormat.CISCO_IOS;
    String l0Name = "Loopback0";
    String c1E1To2Name = interfacePrefix + "1/2";
    String c2E2To1Name = interfacePrefix + "2/1";
    String c2E2To3Name = interfacePrefix + "2/3";
    String c3E3To2Name = interfacePrefix + "3/2";
    String c3E3To4Name = interfacePrefix + "3/4";
    String c4E4To3Name = interfacePrefix + "4/3";

    NetworkFactory nf = new NetworkFactory();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Configuration.Builder cb = nf.configurationBuilder().setConfigurationFormat(format);
    String name;
    InterfaceAddress addr;

    EigrpProcess.Builder epb = EigrpProcess.builder().setAsNumber(asn);
    EigrpMetric.Builder emb = EigrpMetric.builder();
    EigrpInterfaceSettings.Builder eib =
        EigrpInterfaceSettings.builder().setAsn(asn).setEnabled(true);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);

    /* Configuration 1 */
    Configuration c1 = cb.setHostname(R1).build();
    Vrf v1 = vb.setOwner(c1).build();
    epb.setMode(mode1).setVrf(v1).build();
    emb.setMode(mode1);
    ib.setOwner(c1).setVrf(v1);

    name = l0Name;
    addr = R1_L0_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c1E1To2Name;
    addr = R1_E1_2_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    /* Configuration 2 */
    Configuration c2 = cb.setHostname(R2).build();
    Vrf v2 = vb.setOwner(c2).build();
    epb.setMode(mode2).setVrf(v2).build();
    emb.setMode(mode2);
    ib.setOwner(c2).setVrf(v2);

    name = l0Name;
    addr = R2_L0_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c2E2To1Name;
    addr = R2_E2_1_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c2E2To3Name;
    addr = R2_E2_3_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();
    emb.setDelay(null).setBandwidth(null);

    /* Configuration 3 */
    Configuration c3 = cb.setHostname(R3).build();
    Vrf v3 = vb.setOwner(c3).build();
    epb.setMode(mode3).setVrf(v3).build();
    emb.setMode(mode3);
    ib.setOwner(c3).setVrf(v3);

    name = l0Name;
    addr = R3_L0_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c3E3To2Name;
    addr = R3_E3_2_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c3E3To4Name;
    addr = R3_E3_4_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    /* Configuration 4 */
    Configuration c4 = cb.setHostname(R4).build();
    Vrf v4 = vb.setOwner(c4).build();
    epb.setMode(mode4).setVrf(v4).build();
    emb.setMode(mode4);
    ib.setOwner(c4).setVrf(v4);

    name = l0Name;
    addr = R4_L0_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    name = c4E4To3Name;
    addr = R4_E4_3_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format));
    eib.setMetric(emb.build());
    ib.setName(name).setAddress(addr).setEigrp(eib.build()).build();

    SortedMap<String, Configuration> configurations =
        new ImmutableSortedMap.Builder<String, Configuration>(String::compareTo)
            .put(c1.getName(), c1)
            .put(c2.getName(), c2)
            .put(c3.getName(), c3)
            .put(c4.getName(), c4)
            .build();
    IncrementalBdpEngine engine =
        new IncrementalBdpEngine(
            new IncrementalDataPlaneSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            (s, i) -> new AtomicInteger());
    Topology topology = CommonUtil.synthesizeTopology(configurations);
    return (IncrementalDataPlane)
        engine.computeDataPlane(false, configurations, topology, Collections.emptySet())._dataPlane;
  }

  /** Test route computation and propagation for EIGRP in classic mode */
  @Test
  public void testEigrpClassicRoutes() {
    IncrementalDataPlane dp =
        computeDataPlane(
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R1, R3_L0_ADDR, 10240L);
    assertRoute(routes, EIGRP, R1, R4_L0_ADDR, 12800L);
    assertRoute(routes, EIGRP, R1, R2_E2_3_ADDR, 7680L);
    assertRoute(routes, EIGRP, R1, R3_E3_4_ADDR, 10240L);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, EIGRP, R2, R1_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R2, R3_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, 10240L);
    assertRoute(routes, EIGRP, R2, R3_E3_4_ADDR, 7680L);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP, R3, R1_L0_ADDR, 10240L);
    assertRoute(routes, EIGRP, R3, R2_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R3, R1_E1_2_ADDR, 7680L);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, EIGRP, R4, R1_L0_ADDR, 12800L);
    assertRoute(routes, EIGRP, R4, R2_L0_ADDR, 10240L);
    assertRoute(routes, EIGRP, R4, R3_L0_ADDR, 7680L);
    assertRoute(routes, EIGRP, R4, R1_E1_2_ADDR, 10240L);
    assertRoute(routes, EIGRP, R4, R2_E2_3_ADDR, 7680L);
  }

  /** Test route computation and propagation for EIGRP in named mode */
  @Test
  public void testEigrpNamedRoutes() {
    IncrementalDataPlane dp =
        computeDataPlane(
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R1, R3_L0_ADDR, 329646080L);
    assertRoute(routes, EIGRP, R1, R4_L0_ADDR, 330301440L);
    assertRoute(routes, EIGRP, R1, R2_E2_3_ADDR, 1966080L);
    assertRoute(routes, EIGRP, R1, R3_E3_4_ADDR, 2621440L);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, EIGRP, R2, R1_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R2, R3_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, 329646080L);
    assertRoute(routes, EIGRP, R2, R3_E3_4_ADDR, 1966080L);
    assertRoute(routes, EIGRP, R2, R4_E4_3_ADDR, 1966080L);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP, R3, R1_L0_ADDR, 329646080L);
    assertRoute(routes, EIGRP, R3, R2_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R3, R2_E2_1_ADDR, 1966080L);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, EIGRP, R4, R1_L0_ADDR, 330301440L);
    assertRoute(routes, EIGRP, R4, R2_L0_ADDR, 329646080L);
    assertRoute(routes, EIGRP, R4, R3_L0_ADDR, 328990720L);
    assertRoute(routes, EIGRP, R4, R2_E2_1_ADDR, 2621440L);
    assertRoute(routes, EIGRP, R4, R3_E3_2_ADDR, 1966080L);
  }

  /**
   * Test route computation and propagation for EIGRP with a mixture of named and classic routers
   *
   * <p>{@link Ignore Ignored} because further testing is necessary in GNS3 to determine the correct
   * costs
   */
  @Ignore
  @Test
  public void testEigrpMixedRoutes() {
    IncrementalDataPlane dp =
        computeDataPlane(
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.NAMED,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.NAMED,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R1, R3_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R1, R4_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R1, R2_E2_3_ADDR, 0L);
    assertRoute(routes, EIGRP, R1, R3_E3_4_ADDR, 0L);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, EIGRP, R2, R1_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R2, R3_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R2, R3_E3_4_ADDR, 0L);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP, R3, R1_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R3, R2_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R3, R1_E1_2_ADDR, 0L);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, EIGRP, R4, R1_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R4, R2_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R4, R3_L0_ADDR, 0L);
    assertRoute(routes, EIGRP, R4, R1_E1_2_ADDR, 0L);
    assertRoute(routes, EIGRP, R4, R2_E2_3_ADDR, 0L);
  }
}
