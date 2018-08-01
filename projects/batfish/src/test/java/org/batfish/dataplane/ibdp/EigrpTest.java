package org.batfish.dataplane.ibdp;

import static java.util.Objects.requireNonNull;
import static org.batfish.datamodel.RoutingProtocol.EIGRP;
import static org.batfish.datamodel.RoutingProtocol.EIGRP_EX;
import static org.batfish.datamodel.RoutingProtocol.OSPF;
import static org.batfish.dataplane.ibdp.TestUtils.assertNoRoute;
import static org.batfish.dataplane.ibdp.TestUtils.assertRoute;
import static org.batfish.representation.cisco.Interface.getDefaultBandwidth;
import static org.batfish.representation.cisco.Interface.getDefaultDelay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collections;
import java.util.List;
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
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.LiteralEigrpMetric;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetEigrpMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Ignore;
import org.junit.Test;

public class EigrpTest {

  private static final InterfaceAddress R1_E1_2_ADDR = new InterfaceAddress("10.12.0.1/24");
  private static final InterfaceAddress R1_L0_ADDR = new InterfaceAddress("1.1.1.1/32");
  private static final InterfaceAddress R1_E1_4_ADDR = new InterfaceAddress("10.14.0.1/24");
  private static final String R1 = "R1";

  private static final InterfaceAddress R2_E2_1_ADDR = new InterfaceAddress("10.12.0.2/24");
  private static final InterfaceAddress R2_E2_3_ADDR = new InterfaceAddress("10.23.0.2/24");
  private static final InterfaceAddress R2_L0_ADDR = new InterfaceAddress("2.2.2.2/32");
  private static final String R2 = "R2";

  private static final InterfaceAddress R3_E3_2_ADDR = new InterfaceAddress("10.23.0.3/24");
  private static final InterfaceAddress R3_E3_4_ADDR = new InterfaceAddress("10.34.0.3/24");
  private static final InterfaceAddress R3_L0_ADDR = new InterfaceAddress("3.3.3.3/32");
  private static final String R3 = "R3";

  private static final InterfaceAddress R4_E4_1_ADDR = new InterfaceAddress("10.14.0.4/24");
  private static final InterfaceAddress R4_E4_3_ADDR = new InterfaceAddress("10.34.0.4/24");
  private static final InterfaceAddress R4_L0_ADDR = new InterfaceAddress("4.4.4.4/32");
  private static final String R4 = "R4";

  private static final double externalBandwidth = 1E8;
  private static final double externalDelay = 1E9;

  /*
   * Int:1/2   2/1      2/3   3/2      3/4   4/3
   * R1 <=========> R2 <=========> R3 <=========> R4
   */
  private static IncrementalDataPlane computeLinearDataPlane(
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
            .put(c1.getHostname(), c1)
            .put(c2.getHostname(), c2)
            .put(c3.getHostname(), c3)
            .put(c4.getHostname(), c4)
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

  private static List<Statement> getExportPolicyStatements(RoutingProtocol protocol) {
    EigrpMetric metric =
        requireNonNull(
            EigrpMetric.builder()
                .setBandwidth(externalBandwidth)
                .setDelay(externalDelay)
                .setMode(EigrpProcessMode.CLASSIC)
                .build());

    If exportIfMatchProtocol = new If();
    exportIfMatchProtocol.setGuard(new MatchProtocol(protocol));
    exportIfMatchProtocol.setTrueStatements(
        ImmutableList.of(
            new SetEigrpMetric(new LiteralEigrpMetric(metric)),
            Statements.ExitAccept.toStaticStatement()));
    exportIfMatchProtocol.setFalseStatements(
        ImmutableList.of(Statements.ExitReject.toStaticStatement()));
    return ImmutableList.of(exportIfMatchProtocol);
  }

  /*
   *           2/3   3/2
   *   R2.O,E <=========> R3.E
   *    2/1|              | 3/4
   *       |              |
   *       |              |
   *    1/2|   1/4   4/1  | 4/3
   *     R1.O <=========> R4.O,E
   */
  private static IncrementalDataPlane computeMultipathDataPlaneWithRedistribution(
      EigrpProcessMode mode2,
      EigrpProcessMode mode3,
      EigrpProcessMode mode4,
      String interfacePrefix) {

    long asn = 1L;
    ConfigurationFormat format = ConfigurationFormat.CISCO_IOS;
    String l0Name = "Loopback0";
    String c1E1To2Name = interfacePrefix + "1/2";
    String c1E1To4Name = interfacePrefix + "1/4";
    String c2E2To1Name = interfacePrefix + "2/1";
    String c2E2To3Name = interfacePrefix + "2/3";
    double c2E2To3DelayMult = 40000.0;
    String c3E3To2Name = interfacePrefix + "3/2";
    double c3E3To2DelayMult = 500000.0;
    String c3E3To4Name = interfacePrefix + "3/4";
    double c3E3To4DelayMult = 6000000.0;
    String c4E4To1Name = interfacePrefix + "4/1";
    String c4E4To3Name = interfacePrefix + "4/3";
    double c4E4To3DelayMult = 800000000.0;

    NetworkFactory nf = new NetworkFactory();
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    Configuration.Builder cb = nf.configurationBuilder().setConfigurationFormat(format);
    String name;
    InterfaceAddress addr;

    EigrpProcess.Builder epb = EigrpProcess.builder().setAsNumber(asn);
    EigrpMetric.Builder emb = EigrpMetric.builder();
    EigrpInterfaceSettings.Builder esb =
        EigrpInterfaceSettings.builder().setAsn(asn).setEnabled(true);
    Interface.Builder eib = nf.interfaceBuilder().setActive(true).setOspfCost(1);

    OspfProcess.Builder opb = nf.ospfProcessBuilder();
    OspfArea.Builder oab = nf.ospfAreaBuilder().setNumber(1L);
    Interface.Builder oib =
        nf.interfaceBuilder().setActive(true).setOspfCost(1).setOspfEnabled(true);
    RoutingPolicy.Builder rpb = nf.routingPolicyBuilder();

    /* Configuration 1 */
    Configuration c1 = cb.setHostname(R1).build();
    Vrf v1 = vb.setOwner(c1).build();
    oib.setOwner(c1).setVrf(v1).setOspfArea(oab.setOspfProcess(opb.setVrf(v1).build()).build());

    name = l0Name;
    addr = R1_L0_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(true).build();

    name = c1E1To2Name;
    addr = R1_E1_2_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(false).build();

    name = c1E1To4Name;
    addr = R1_E1_4_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(false).build();

    /* Configuration 2 */
    Configuration c2 = cb.setHostname(R2).build();
    Vrf v2 = vb.setOwner(c2).build();
    RoutingPolicy c2ExportOspf =
        rpb.setOwner(c2).setStatements(getExportPolicyStatements(RoutingProtocol.OSPF)).build();
    epb.setExportPolicy(c2ExportOspf.getName()).setMode(mode2).setVrf(v2).build();
    emb.setMode(mode2);
    eib.setOwner(c2).setVrf(v2);
    oib.setOwner(c2).setVrf(v2).setOspfArea(oab.setOspfProcess(opb.setVrf(v2).build()).build());

    name = l0Name;
    addr = R2_L0_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(true).build();

    name = c2E2To1Name;
    addr = R2_E2_1_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(false).build();

    name = c2E2To3Name;
    addr = R2_E2_3_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format))
        .setDelay(getDefaultDelay(name, format) * c2E2To3DelayMult);
    esb.setPassive(false).setMetric(emb.build());
    eib.setName(name).setAddress(addr).setEigrp(esb.build()).build();

    /* Configuration 3 */
    Configuration c3 = cb.setHostname(R3).build();
    Vrf v3 = vb.setOwner(c3).build();
    RoutingPolicy c3ExportConnected =
        rpb.setOwner(c3)
            .setStatements(getExportPolicyStatements(RoutingProtocol.CONNECTED))
            .build();
    epb.setExportPolicy(c3ExportConnected.getName()).setMode(mode3).setVrf(v3).build();
    emb.setMode(mode3);
    eib.setOwner(c3).setVrf(v3);

    name = l0Name;
    addr = R3_L0_ADDR;
    eib.setName(name).setAddress(addr).setEigrp(null).build();

    name = c3E3To2Name;
    addr = R3_E3_2_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format))
        .setDelay(getDefaultDelay(name, format) * c3E3To2DelayMult);
    esb.setPassive(false).setMetric(emb.build());
    eib.setName(name).setAddress(addr).setEigrp(esb.build()).build();

    name = c3E3To4Name;
    addr = R3_E3_4_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format))
        .setDelay(getDefaultDelay(name, format) * c3E3To4DelayMult);
    esb.setPassive(false).setMetric(emb.build());
    eib.setName(name).setAddress(addr).setEigrp(esb.build()).build();

    /* Configuration 4 */
    Configuration c4 = cb.setHostname(R4).build();
    Vrf v4 = vb.setOwner(c4).build();
    RoutingPolicy c4ExportOspf =
        rpb.setOwner(c4).setStatements(getExportPolicyStatements(RoutingProtocol.OSPF)).build();
    epb.setExportPolicy(c4ExportOspf.getName()).setMode(mode4).setVrf(v4).build();
    emb.setMode(mode4);
    eib.setOwner(c4).setVrf(v4);
    oib.setOwner(c4).setVrf(v4).setOspfArea(oab.setOspfProcess(opb.setVrf(v4).build()).build());

    name = l0Name;
    addr = R4_L0_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format))
        .setDelay(null);
    esb.setPassive(true).setMetric(emb.build());
    eib.setName(name).setAddress(addr).setEigrp(esb.build()).build();

    name = c4E4To1Name;
    addr = R4_E4_1_ADDR;
    oib.setAddress(addr).setName(name).setOspfPassive(false).build();

    name = c4E4To3Name;
    addr = R4_E4_3_ADDR;
    emb.setDefaultBandwidth(getDefaultBandwidth(name, format))
        .setDefaultDelay(getDefaultDelay(name, format))
        .setDelay(getDefaultDelay(name, format) * c4E4To3DelayMult);
    esb.setPassive(false).setMetric(emb.build());
    eib.setName(name).setAddress(addr).setEigrp(esb.build()).build();

    SortedMap<String, Configuration> configurations =
        new ImmutableSortedMap.Builder<String, Configuration>(String::compareTo)
            .put(c1.getHostname(), c1)
            .put(c2.getHostname(), c2)
            .put(c3.getHostname(), c3)
            .put(c4.getHostname(), c4)
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
  public void testEigrpClassicLinearRoutes() {
    IncrementalDataPlane dp =
        computeLinearDataPlane(
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    long scale = 256L;
    // GigabitEthernet values
    long bandwidth = 10L;
    long delay = 1L;
    // Loopback values
    long lDelay = 500L;

    bandwidth *= scale;
    delay *= scale;
    lDelay *= scale;

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R1, R3_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R1, R4_L0_ADDR, bandwidth + delay * 3 + lDelay);
    assertRoute(routes, EIGRP, R1, R2_E2_3_ADDR, bandwidth + delay * 2);
    assertRoute(routes, EIGRP, R1, R3_E3_4_ADDR, bandwidth + delay * 3);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, EIGRP, R2, R1_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R2, R3_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R2, R3_E3_4_ADDR, bandwidth + delay * 2);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP, R3, R1_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R3, R2_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R3, R1_E1_2_ADDR, bandwidth + delay * 2);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, EIGRP, R4, R1_L0_ADDR, bandwidth + delay * 3 + lDelay);
    assertRoute(routes, EIGRP, R4, R2_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R4, R3_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R4, R1_E1_2_ADDR, bandwidth + delay * 3);
    assertRoute(routes, EIGRP, R4, R2_E2_3_ADDR, bandwidth + delay * 2);
  }

  /** Test route computation, propagation, and one-way redistribution for EIGRP in classic mode */
  @Test
  public void testEigrpClassicMultipathRoutes() {
    IncrementalDataPlane dp =
        computeMultipathDataPlaneWithRedistribution(
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            EigrpProcessMode.CLASSIC,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    long scale = 256L;
    // GigabitEthernet values
    long bandwidth = 10L;
    // Loopback values
    long lDelay = 500L;
    long exMetric =
        EigrpMetric.namedToClassicBandwidth((long) (externalBandwidth / 1000.0))
            + EigrpMetric.namedToClassicDelay((long) externalDelay);

    bandwidth *= scale;
    lDelay *= scale;
    exMetric *= scale;

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, OSPF, R1, R2_L0_ADDR, 2L);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, OSPF, R2, R1_L0_ADDR, 2L);
    assertRoute(routes, EIGRP_EX, R2, R3_L0_ADDR, exMetric + 40000L * scale);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, bandwidth + (6000000L + 40000L) * scale + lDelay);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP_EX, R3, R1_L0_ADDR, exMetric + 500000L * scale);
    assertRoute(routes, EIGRP_EX, R3, R2_L0_ADDR, exMetric + 6000000L * scale);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, bandwidth + 6000000L * scale + lDelay);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, OSPF, R4, R1_L0_ADDR, 2L);
    assertRoute(routes, OSPF, R4, R2_L0_ADDR, 3L);
    assertRoute(routes, EIGRP_EX, R4, R3_L0_ADDR, exMetric + 800000000L * scale);
  }

  /** Test route computation and propagation for EIGRP in named mode */
  @Test
  public void testEigrpNamedRoutes() {
    IncrementalDataPlane dp =
        computeLinearDataPlane(
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            EigrpProcessMode.NAMED,
            "GigabitEthernet");
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        IncrementalBdpEngine.getRoutes(dp);

    long scale = 65536L;
    // GigabitEthernet values
    long bandwidth = 10L;
    long delay = 10L;
    // Loopback values
    long lDelay = 5000L;

    bandwidth *= scale;
    delay *= scale;
    lDelay *= scale;

    // r1
    assertNoRoute(routes, R1, Prefix.ZERO);
    assertRoute(routes, EIGRP, R1, R2_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R1, R3_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R1, R4_L0_ADDR, bandwidth + delay * 3 + lDelay);
    assertRoute(routes, EIGRP, R1, R2_E2_3_ADDR, bandwidth + delay * 2);
    assertRoute(routes, EIGRP, R1, R3_E3_4_ADDR, bandwidth + delay * 3);

    // r2
    assertNoRoute(routes, R2, Prefix.ZERO);
    assertRoute(routes, EIGRP, R2, R1_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R2, R3_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R2, R4_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R2, R3_E3_4_ADDR, bandwidth + delay * 2);

    // r3
    assertNoRoute(routes, R3, Prefix.ZERO);
    assertRoute(routes, EIGRP, R3, R1_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R3, R2_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R3, R4_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R3, R1_E1_2_ADDR, bandwidth + delay * 2);

    // r4
    assertNoRoute(routes, R4, Prefix.ZERO);
    assertRoute(routes, EIGRP, R4, R1_L0_ADDR, bandwidth + delay * 3 + lDelay);
    assertRoute(routes, EIGRP, R4, R2_L0_ADDR, bandwidth + delay * 2 + lDelay);
    assertRoute(routes, EIGRP, R4, R3_L0_ADDR, bandwidth + delay + lDelay);
    assertRoute(routes, EIGRP, R4, R1_E1_2_ADDR, bandwidth + delay * 3);
    assertRoute(routes, EIGRP, R4, R2_E2_3_ADDR, bandwidth + delay * 2);
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
        computeLinearDataPlane(
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
