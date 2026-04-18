package org.batfish.representation.juniper;

import static org.batfish.common.Warnings.TAG_PEDANTIC;
import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasUnimplementedWarning;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.matchers.AclLineMatchers.hasTraceElement;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_DEAD_INTERVAL;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_HELLO_INTERVAL;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_ISIS_COST;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_NBMA_DEAD_INTERVAL;
import static org.batfish.representation.juniper.JuniperConfiguration.DEFAULT_NBMA_HELLO_INTERVAL;
import static org.batfish.representation.juniper.JuniperConfiguration.MAX_ISIS_COST_WITHOUT_WIDE_METRICS;
import static org.batfish.representation.juniper.JuniperConfiguration.OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER;
import static org.batfish.representation.juniper.JuniperConfiguration.buildScreen;
import static org.batfish.representation.juniper.JuniperConfiguration.getDefaultSourceAddress;
import static org.batfish.representation.juniper.JuniperConfiguration.getJuniperConnectedRouteMetadata;
import static org.batfish.representation.juniper.JuniperConfiguration.getPrimaryInterface;
import static org.batfish.representation.juniper.JuniperConfiguration.getRouterId;
import static org.batfish.representation.juniper.JuniperConfiguration.matchingFirewallFilter;
import static org.batfish.representation.juniper.JuniperConfiguration.mergeIpAccessListLines;
import static org.batfish.representation.juniper.JuniperConfiguration.toOspfDeadInterval;
import static org.batfish.representation.juniper.JuniperConfiguration.toOspfHelloInterval;
import static org.batfish.representation.juniper.JuniperConfiguration.toRibId;
import static org.batfish.representation.juniper.JuniperConfiguration.toRouteFilterList;
import static org.batfish.representation.juniper.JuniperConfiguration.toTraceableStatement;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER_TERM;
import static org.batfish.representation.juniper.JuniperStructureType.SECURITY_POLICY;
import static org.batfish.representation.juniper.JuniperStructureType.SECURITY_POLICY_TERM;
import static org.batfish.representation.juniper.NatPacketLocation.interfaceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.routingInstanceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.zoneLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.datamodel.vxlan.Vni;
import org.batfish.representation.juniper.OspfInterfaceSettings.OspfInterfaceType;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Test for {@link JuniperConfiguration} */
public class JuniperConfigurationTest {

  private static JuniperConfiguration createConfig() {
    JuniperConfiguration config = new JuniperConfiguration();
    config.setFilename("file");
    config._c = new Configuration("host", ConfigurationFormat.JUNIPER);
    return config;
  }

  @Test
  public void testFilterToIpAccessList_noTerm() {
    JuniperConfiguration config = createConfig();
    ConcreteFirewallFilter filter = new ConcreteFirewallFilter("filter", Family.INET);
    IpAccessList emptyAcl = config.filterToIpAccessList(filter);

    // ACL from empty filter should have no lines
    assertThat(emptyAcl.getLines(), iterableWithSize(0));
  }

  @Test
  public void testFilterToIpAccessList_hasTerm() {
    JuniperConfiguration config = createConfig();
    ConcreteFirewallFilter filter = new ConcreteFirewallFilter("filter", Family.INET);
    FwTerm term = new FwTerm("term");
    String ipAddrPrefix = "1.2.3.0/24";
    term.getFroms().add(new FwFromSourceAddress(IpWildcard.parse(ipAddrPrefix), ipAddrPrefix));
    term.getThens().add(FwThenAccept.INSTANCE);
    filter.getTerms().put("term", term);
    IpAccessList headerSpaceAcl = config.filterToIpAccessList(filter);

    // ACL from headerSpace filter should have one line
    AclLine headerSpaceAclLine = Iterables.getOnlyElement(headerSpaceAcl.getLines());
    // It should have a MatchHeaderSpace match condition, matching the ipAddrPrefix from above
    VendorStructureId vsId =
        new VendorStructureId("file", FIREWALL_FILTER_TERM.getDescription(), "filter term");
    assertThat(
        headerSpaceAclLine,
        equalTo(
            ExprAclLine.builder()
                .setName("term")
                .setAction(LineAction.PERMIT)
                .setTraceElement(TraceElement.builder().add("Matched ").add("term", vsId).build())
                .setVendorStructureId(vsId)
                .setMatchCondition(
                    matchSrc(
                        IpWildcard.parse(ipAddrPrefix).toIpSpace(),
                        TraceElement.of("Matched source-address 1.2.3.0/24")))
                .build()));
  }

  /** Validate vrf assigned to l3vni is the same as the l3interface on a vlan. */
  @Test
  public void testL3VniVrf() {
    JuniperConfiguration config = createConfig();
    String iface1Name = "iface1";
    Vrf vrf = new Vrf("vrf");
    org.batfish.datamodel.TestInterface.builder()
        .setName(iface1Name)
        .setOwner(config._c)
        .setVrf(vrf)
        .build();
    config._c.setVrfs(ImmutableMap.of("vrf", vrf));
    Vlan vlan10 = new Vlan("VLAN-10");
    vlan10.setVniId(10110);
    vlan10.setVlanId(10);
    vlan10.setL3Interface(iface1Name);
    config.getMasterLogicalSystem().getNamedVlans().put("VLAN-10", vlan10);
    Layer3Vni l3vni =
        Layer3Vni.builder()
            .setVni(10100)
            .setUdpPort(Vni.DEFAULT_UDP_PORT)
            .setSrcVrf(vrf.getName())
            .build();
    config._c.getVrfs().get("vrf").addLayer3Vni(l3vni);
    assertThat(l3vni, equalTo(config._c.getVrfs().get("vrf").getLayer3Vnis().get(10100)));
  }

  @Test
  public void testGetJuniperConnectedRouteMetadata() {
    ConnectedRouteMetadata normalAddr =
        getJuniperConnectedRouteMetadata(ConcreteInterfaceAddress.parse("1.2.3.4/24"));
    assertThat(normalAddr.getAdmin(), nullValue());
    assertThat(normalAddr.getTag(), nullValue());
    assertThat(normalAddr.getGenerateConnectedRoute(), nullValue());
    assertThat(normalAddr.getGenerateLocalRoute(), nullValue());
    assertThat(normalAddr.getGenerateLocalNullRouteIfDown(), equalTo(true));

    for (ConnectedRouteMetadata loopbackAddr :
        new ConnectedRouteMetadata[] {
          getJuniperConnectedRouteMetadata(ConcreteInterfaceAddress.parse("127.0.0.1/32")),
          getJuniperConnectedRouteMetadata(ConcreteInterfaceAddress.parse("127.255.0.1/32")),
          getJuniperConnectedRouteMetadata(ConcreteInterfaceAddress.parse("127.255.0.1/16")),
        }) {
      assertThat(loopbackAddr.getAdmin(), nullValue());
      assertThat(loopbackAddr.getTag(), nullValue());
      assertThat(loopbackAddr.getGenerateConnectedRoute(), equalTo(false));
      assertThat(loopbackAddr.getGenerateLocalRoute(), equalTo(false));
      assertThat(loopbackAddr.getGenerateLocalNullRouteIfDown(), equalTo(false));
    }
  }

  @Test
  public void testSecurityPolicyToIpAccessList() {
    JuniperConfiguration config = createConfig();
    ConcreteFirewallFilter filter = new ConcreteFirewallFilter("filter", Family.INET);
    FwTerm term = new FwTerm("term");
    String ipAddrPrefix = "1.2.3.0/24";
    term.getFroms().add(new FwFromSourceAddress(IpWildcard.parse(ipAddrPrefix), ipAddrPrefix));
    term.getThens().add(FwThenAccept.INSTANCE);
    filter.getTerms().put("term", term);

    Zone zone = new Zone("zone", new AddressBook("global", null));
    String interface1Name = "interface1";
    zone.getInterfaces().add(interface1Name);
    String interface2Name = "interface2";
    zone.getInterfaces().add(interface2Name);
    config.getMasterLogicalSystem().getZones().put("zone", zone);
    filter.setFromZone("zone");
    IpAccessList headerSpaceAndSrcInterfaceAcl = config.securityPolicyToIpAccessList(filter);

    // ACL from headerSpace and zone filter should have one line
    AclLine comboAclLine = Iterables.getOnlyElement(headerSpaceAndSrcInterfaceAcl.getLines());
    // It should have an AndMatchExpr match condition, containing both a MatchSrcInterface
    // condition and a MatchHeaderSpace condition
    VendorStructureId vsId =
        new VendorStructureId("file", SECURITY_POLICY_TERM.getDescription(), "filter term");
    assertThat(
        comboAclLine,
        equalTo(
            ExprAclLine.builder()
                .setName("term")
                .setAction(LineAction.PERMIT)
                .setTraceElement(TraceElement.builder().add("Matched ").add("term", vsId).build())
                .setVendorStructureId(vsId)
                .setMatchCondition(
                    new AndMatchExpr(
                        ImmutableList.of(
                            matchSrc(
                                IpWildcard.parse(ipAddrPrefix).toIpSpace(),
                                TraceElement.of("Matched source-address 1.2.3.0/24")),
                            new MatchSrcInterface(zone.getInterfaces()))))
                .build()));
  }

  @Test
  public void testCompositeFirewallFilter() {
    JuniperConfiguration config = createConfig();

    ConcreteFirewallFilter concrete = new ConcreteFirewallFilter("F", Family.INET);
    CompositeFirewallFilter composite =
        new CompositeFirewallFilter("composite", ImmutableList.of(concrete));
    IpAccessList compositeAcl = config.filterToIpAccessList(composite);

    assertThat(
        compositeAcl.getLines(),
        contains(hasTraceElement(matchingFirewallFilter(config.getFilename(), "F"))));
  }

  /**
   * Sets up the given configuration with one VRF called vrf containing one interface called iface.
   *
   * @return the created interface
   */
  private static org.batfish.datamodel.Interface createInterface(Configuration c) {
    Vrf vrf = new Vrf("vrf");
    c.setVrfs(ImmutableMap.of("vrf", vrf));
    org.batfish.datamodel.Interface iface =
        org.batfish.datamodel.TestInterface.builder()
            .setName("iface")
            .setOwner(c)
            .setVrf(vrf)
            .build();
    return iface;
  }

  /**
   * Creates a {@link RoutingInstance} configured with one VRF called vrf containing one enabled
   * Juniper {@link Interface} called iface with the given {@code ifaceBandwidth}. If {@code
   * configuredIfaceIsisMetric} is not null, interface's level 1 IS-IS settings will use it. If
   * {@code referenceBandwidth} is not null, routing instance's IS-IS settings will use it.
   *
   * @param ifaceBandwidth Bandwidth of interface
   * @param configuredIfaceIsisMetric IS-IS metric for the interface's level 1 IS-IS settings
   * @param referenceBandwidth Reference bandwidth for the routing instance's {@link IsisSettings}
   */
  private static RoutingInstance createRoutingInstance(
      @Nonnull Double ifaceBandwidth,
      @Nullable Long configuredIfaceIsisMetric,
      @Nullable Double referenceBandwidth) {
    Interface iface = new Interface("iface");
    iface.setBandwidth(ifaceBandwidth);
    iface.getOrInitIsisSettings();
    if (configuredIfaceIsisMetric != null) {
      iface.getIsisSettings().getLevel1Settings().setMetric(configuredIfaceIsisMetric);
    }

    RoutingInstance routingInstance = new RoutingInstance("vrf");
    routingInstance.getInterfaces().put("iface", iface);
    if (referenceBandwidth != null) {
      routingInstance.getIsisSettings().setReferenceBandwidth(referenceBandwidth);
    }

    return routingInstance;
  }

  @Test
  public void testProcessIsisInterfaceSettings() {
    // Config with loopback and two other interfaces
    JuniperConfiguration config = createConfig();
    String loopbackName = "lo0.0";
    String iface1Name = "iface1";
    String iface2Name = "iface2";
    Vrf vrf = new Vrf("vrf");
    org.batfish.datamodel.TestInterface.builder()
        .setOwner(config._c)
        .setName(loopbackName)
        .setVrf(vrf)
        .setType(InterfaceType.LOOPBACK)
        .build();
    org.batfish.datamodel.TestInterface.builder()
        .setName(iface1Name)
        .setOwner(config._c)
        .setVrf(vrf)
        .build();
    org.batfish.datamodel.TestInterface.builder()
        .setName(iface2Name)
        .setOwner(config._c)
        .setVrf(vrf)
        .build();
    config._c.setVrfs(ImmutableMap.of("vrf", vrf));

    // Loopback has IS-IS enabled at both levels; other interfaces' IS-IS settings vary by test
    RoutingInstance routingInstance = new RoutingInstance("vrf");
    Interface vsLoopback = new Interface(loopbackName);
    vsLoopback.getOrInitIsisSettings();
    routingInstance.getInterfaces().put(loopbackName, vsLoopback);

    {
      // LEVEL_1_2 if both enabled in process and each enabled on at least one interface
      Interface vsIface1 = new Interface(iface1Name);
      Interface vsIface2 = new Interface(iface2Name);
      vsIface1.getOrInitIsisSettings().getLevel1Settings().setEnabled(false);
      vsIface2.getOrInitIsisSettings().getLevel2Settings().setEnabled(false);
      vsIface1.setRoutingInstance(routingInstance);
      vsIface2.setRoutingInstance(routingInstance);
      routingInstance.getInterfaces().put(iface1Name, vsIface1);
      routingInstance.getInterfaces().put(iface2Name, vsIface2);
      assertThat(
          config.processIsisInterfaceSettings(routingInstance, true, true),
          equalTo(IsisLevel.LEVEL_1_2));

      // LEVEL_1 if process doesn't have L2 enabled; LEVEL_2 if process doesn't have L1 enabled
      assertThat(
          config.processIsisInterfaceSettings(routingInstance, true, false),
          equalTo(IsisLevel.LEVEL_1));
      assertThat(
          config.processIsisInterfaceSettings(routingInstance, false, true),
          equalTo(IsisLevel.LEVEL_2));
    }

    {
      // LEVEL_1 if both enabled in process but only L1 enabled on non-loopback interfaces
      Interface vsIface1 = new Interface(iface1Name);
      Interface vsIface2 = new Interface(iface2Name);
      vsIface1.getOrInitIsisSettings().getLevel2Settings().setEnabled(false);
      vsIface1.setRoutingInstance(routingInstance);
      vsIface2.setRoutingInstance(routingInstance);
      routingInstance.getInterfaces().put(iface1Name, vsIface1);
      routingInstance.getInterfaces().put(iface2Name, vsIface2);
      assertThat(
          config.processIsisInterfaceSettings(routingInstance, true, true),
          equalTo(IsisLevel.LEVEL_1));
    }

    {
      // LEVEL_2 if both enabled in process but only L2 enabled on non-loopback interfaces
      Interface vsIface1 = new Interface(iface1Name);
      Interface vsIface2 = new Interface(iface2Name);
      vsIface1.getOrInitIsisSettings().getLevel1Settings().setEnabled(false);
      vsIface1.setRoutingInstance(routingInstance);
      vsIface2.setRoutingInstance(routingInstance);
      routingInstance.getInterfaces().put(iface1Name, vsIface1);
      routingInstance.getInterfaces().put(iface2Name, vsIface2);
      assertThat(
          config.processIsisInterfaceSettings(routingInstance, true, true),
          equalTo(IsisLevel.LEVEL_2));
    }
  }

  @Test
  public void testIsisCostFromInterfaceSettings() {
    // Reference bandwidth and interface IS-IS cost are both set. Test that interface IS-IS cost
    // takes precedence.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(100D, 5L, 10000D);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo(5L));
  }

  @Test
  public void testIsisCostFromReferenceBandwidth() {
    // Reference bandwidth is set and interface IS-IS cost is not. Test that reference bandwidth is
    // used to produce IS-IS cost.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(500D, null, 10000D);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo(20L));
  }

  @Test
  public void testIsisCostFromSmallReferenceBandwidth() {
    // Interface IS-IS cost is not set. Reference bandwidth is set smaller than interface bandwidth.
    // IS-IS cost should be 1.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(100D, null, 10D);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo(1L));
  }

  @Test
  public void testDefaultIsisCost() {
    // Neither reference bandwidth nor interface IS-IS cost is set. Test that IS-IS cost is default.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(100D, null, null);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo((long) DEFAULT_ISIS_COST));
  }

  @Test
  public void testIsisCostInterfaceBandwidth0() {
    // Reference bandwidth is set, but IS-IS cost can't be calculated because interface bandwidth is
    // 0. IS-IS cost should be default value; conversion warning should be generated.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(0D, null, 10000D);
    config.setWarnings(new Warnings(true, false, false));

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo((long) DEFAULT_ISIS_COST));
    assertThat(
        config.getWarnings().getPedanticWarnings(),
        contains(
            new Warning(
                "Cannot use IS-IS reference bandwidth for interface 'iface' because interface"
                    + " bandwidth is 0.",
                TAG_PEDANTIC)));
  }

  @Test
  public void testRefBandwidthBasedIsisCostClipped() {
    // Reference bandwidth = 10000, bandwidth = 10. IS-IS cost would be 1000 if wide-metrics-only
    // were set, but since it is not, cost should be clipped to 63.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(10D, null, 10000D);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(
        viIface.getIsis().getLevel1().getCost(),
        equalTo((long) MAX_ISIS_COST_WITHOUT_WIDE_METRICS));

    // Now ensure that when wide-metrics-only is set, IS-IS cost doesn't get clipped.
    routingInstance.getIsisSettings().getLevel1Settings().setWideMetricsOnly(true);
    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo(1000L));
  }

  @Test
  public void testInterfaceSettingsBasedIsisCostClipped() {
    // Interface IS-IS metric = 100, but wide-metrics-only is not set. Cost should be clipped to 63.
    JuniperConfiguration config = createConfig();
    org.batfish.datamodel.Interface viIface = createInterface(config._c);
    RoutingInstance routingInstance = createRoutingInstance(10D, 100L, null);

    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(
        viIface.getIsis().getLevel1().getCost(),
        equalTo((long) MAX_ISIS_COST_WITHOUT_WIDE_METRICS));

    // Now ensure that when wide-metrics-only is set, IS-IS cost doesn't get clipped.
    routingInstance.getIsisSettings().getLevel1Settings().setWideMetricsOnly(true);
    config.processIsisInterfaceSettings(routingInstance, true, false);
    assertThat(viIface.getIsis().getLevel1().getCost(), equalTo(100L));
  }

  @Test
  public void testFromNatPacketLocationMatchExprs() {
    JuniperConfiguration config = createConfig();

    Interface parent = new Interface("parent");

    String interface1Name = "interface1";
    parent.getUnits().put(interface1Name, new Interface(interface1Name));
    config.getMasterLogicalSystem().getInterfaces().put(parent.getName(), parent);

    Zone zone = new Zone("zone", new AddressBook("global", null));
    String zoneInterface1Name = "zoneInterface1";
    zone.getInterfaces().add(zoneInterface1Name);
    String zoneInterface2Name = "zoneInterface2";
    zone.getInterfaces().add(zoneInterface2Name);
    config.getMasterLogicalSystem().getZones().put(zone.getName(), zone);

    RoutingInstance routingInstance = config.getMasterLogicalSystem().getDefaultRoutingInstance();
    String routingInstanceInterface1Name = "routingInstanceInterface1";
    routingInstance
        .getInterfaces()
        .put(routingInstanceInterface1Name, new Interface(routingInstanceInterface1Name));
    String routingInstanceInterface2Name = "routingInstanceInterface2";
    routingInstance
        .getInterfaces()
        .put(routingInstanceInterface2Name, new Interface(routingInstanceInterface2Name));

    Map<NatPacketLocation, AclLineMatchExpr> matchExprs = config.fromNatPacketLocationMatchExprs();
    assertThat(
        matchExprs,
        equalTo(
            ImmutableMap.of(
                interfaceLocation(interface1Name), matchSrcInterface(interface1Name),
                zoneLocation(zone.getName()),
                    matchSrcInterface(zoneInterface1Name, zoneInterface2Name),
                routingInstanceLocation(routingInstance.getName()),
                    matchSrcInterface(
                        routingInstanceInterface1Name, routingInstanceInterface2Name))));
  }

  @Test
  public void testBuildScreen() {
    List<ScreenOption> screenOptionList =
        ImmutableList.of(
            IcmpLarge.INSTANCE,
            IpUnknownProtocol.INSTANCE,
            TcpFinNoAck.INSTANCE,
            TcpSynFin.INSTANCE,
            TcpNoFlag.INSTANCE);

    Screen screen = new Screen("screen");
    for (ScreenOption option : screenOptionList) {
      screen.getScreenOptions().add(option);
    }

    IpAccessList screenAcl = buildScreen(screen, "~SCREEN~screen");

    assertThat(
        screenAcl,
        equalTo(
            IpAccessList.builder()
                .setName("~SCREEN~screen")
                .setLines(
                    ImmutableList.of(
                        ExprAclLine.rejecting(
                            new OrMatchExpr(
                                screenOptionList.stream()
                                    .map(ScreenOption::getAclLineMatchExpr)
                                    .collect(Collectors.toList()))),
                        ExprAclLine.ACCEPT_ALL))
                .build()));

    Screen screen2 = new Screen("screen2");
    IpAccessList screenAcl2 = buildScreen(screen2, "~SCREEN~screen2");
    assertThat(screenAcl2, nullValue());
  }

  @Test
  public void testBuildScreensPerZone() {
    JuniperConfiguration config = createConfig();

    List<ScreenOption> screenOptionList1 =
        ImmutableList.of(IcmpLarge.INSTANCE, IpUnknownProtocol.INSTANCE, TcpFinNoAck.INSTANCE);

    List<ScreenOption> screenOptionList2 = ImmutableList.of(TcpSynFin.INSTANCE, TcpNoFlag.INSTANCE);

    Screen screen1 = new Screen("screen1");
    for (ScreenOption option : screenOptionList1) {
      screen1.getScreenOptions().add(option);
    }

    Screen screen2 = new Screen("screen2");
    for (ScreenOption option : screenOptionList2) {
      screen2.getScreenOptions().add(option);
    }
    screen2.setAction(ScreenAction.ALARM_WITHOUT_DROP);

    Screen screen3 = new Screen("screen3");

    config.getMasterLogicalSystem().getScreens().put("screen1", screen1);
    config.getMasterLogicalSystem().getScreens().put("screen2", screen2);
    config.getMasterLogicalSystem().getScreens().put("screen3", screen3);

    Zone zone = new Zone("zone", null);
    zone.getScreens().add("screen1");
    zone.getScreens().add("screen2");
    zone.getScreens().add("screen3");

    IpAccessList screenAcl = config.buildScreensPerZone(zone, "~SCREEN_ZONE~zone");

    // screenAcl should only have screen1, because screen2 has an alarm action and screen3 has not
    // options.
    assertThat(
        screenAcl,
        equalTo(
            IpAccessList.builder()
                .setName("~SCREEN_ZONE~zone")
                .setLines(
                    ImmutableList.of(ExprAclLine.accepting(new PermittedByAcl("~SCREEN~screen1"))))
                .build()));
    assertThat(config._c.getIpAccessLists().get("~SCREEN~screen1"), notNullValue());
    assertThat(config._c.getIpAccessLists().get("~SCREEN~screen2"), nullValue());
    assertThat(config._c.getIpAccessLists().get("~SCREEN~screen3"), nullValue());
    assertThat(
        config._c.getIpAccessLists().get("~SCREEN~screen1"),
        equalTo(
            IpAccessList.builder()
                .setName("~SCREEN~screen1")
                .setLines(
                    ImmutableList.of(
                        ExprAclLine.rejecting(
                            new OrMatchExpr(
                                screenOptionList1.stream()
                                    .map(ScreenOption::getAclLineMatchExpr)
                                    .collect(Collectors.toList()))),
                        ExprAclLine.ACCEPT_ALL))
                .build()));
  }

  @Test
  public void testBuildScreensPerInterface() {
    JuniperConfiguration config = createConfig();
    Interface iface = new Interface("iface");

    List<ScreenOption> screenOptionList =
        ImmutableList.of(IcmpLarge.INSTANCE, IpUnknownProtocol.INSTANCE, TcpFinNoAck.INSTANCE);

    Screen screen = new Screen("screen");
    for (ScreenOption option : screenOptionList) {
      screen.getScreenOptions().add(option);
    }

    config.getMasterLogicalSystem().getScreens().put("screen", screen);

    Zone zone = new Zone("zone", null);
    zone.getScreens().add("screen");

    config.getMasterLogicalSystem().getInterfaceZones().put(iface.getName(), zone);

    IpAccessList screenAcl = config.buildScreensPerInterface(iface);

    // screenAcl should only have screen, because screen2 has an alarm action and screen3 has not
    // options.
    assertThat(
        screenAcl,
        equalTo(
            IpAccessList.builder()
                .setName("~SCREEN_INTERFACE~iface")
                .setLines(
                    ImmutableList.of(
                        ExprAclLine.accepting(new PermittedByAcl("~SCREEN_ZONE~zone"))))
                .build()));
    assertThat(config._c.getIpAccessLists().get("~SCREEN_ZONE~zone"), notNullValue());
  }

  @Test
  public void testToRibId() {
    String hostname = "host";
    assertThat(
        toRibId(hostname, "inet.0", null),
        equalTo(new RibId(hostname, Configuration.DEFAULT_VRF_NAME, RibId.DEFAULT_RIB_NAME)));
    assertThat(
        toRibId(hostname, "inet.1", null),
        equalTo(new RibId(hostname, Configuration.DEFAULT_VRF_NAME, "inet.1")));
    assertThat(
        toRibId(hostname, "VRF1.inet.0", null),
        equalTo(new RibId(hostname, "VRF1", RibId.DEFAULT_RIB_NAME)));
    assertThat(
        toRibId(hostname, "VRF1.inet.1", null), equalTo(new RibId(hostname, "VRF1", "inet.1")));

    Warnings w = new Warnings(true, true, true);
    assertThat(toRibId(hostname, "inet6.0", w), nullValue());
    assertThat(
        w,
        hasUnimplementedWarning(
            hasText("Rib name conversion: inet6 address family is not supported")));
  }

  @Test
  public void testToOspfDeadIntervalExplicit() {
    OspfInterfaceSettings ospfInterfaceSettings = new OspfInterfaceSettings(Ip.ZERO);
    ospfInterfaceSettings.setOspfDeadInterval(7);
    // Explicitly set dead interval should be preferred over inference
    assertThat(toOspfDeadInterval(ospfInterfaceSettings), equalTo(7));
  }

  @Test
  public void testToOspfDeadIntervalFromHello() {
    int helloInterval = 1;
    OspfInterfaceSettings ospfInterfaceSettings = new OspfInterfaceSettings(Ip.ZERO);
    ospfInterfaceSettings.setOspfHelloInterval(1);
    // Since the dead interval is not set, it should be inferred as four times the hello interval
    assertThat(
        toOspfDeadInterval(ospfInterfaceSettings),
        equalTo(OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval));
  }

  @Test
  public void testToOspfDeadIntervalFromType() {
    OspfInterfaceSettings ospfInterfaceSettings = new OspfInterfaceSettings(Ip.ZERO);
    // Since the dead interval and hello interval are not set, it should be inferred from the
    // network type
    ospfInterfaceSettings.setOspfInterfaceType(OspfInterfaceType.P2P);
    assertThat(toOspfDeadInterval(ospfInterfaceSettings), equalTo(DEFAULT_DEAD_INTERVAL));
    ospfInterfaceSettings.setOspfInterfaceType(OspfInterfaceType.NBMA);
    assertThat(toOspfDeadInterval(ospfInterfaceSettings), equalTo(DEFAULT_NBMA_DEAD_INTERVAL));
  }

  @Test
  public void testToOspfHelloIntervalExplicit() {
    OspfInterfaceSettings ospfInterfaceSettings = new OspfInterfaceSettings(Ip.ZERO);
    ospfInterfaceSettings.setOspfHelloInterval(7);
    // Explicitly set hello interval should be preferred over inference
    assertThat(toOspfHelloInterval(ospfInterfaceSettings), equalTo(7));
  }

  @Test
  public void testToOspfHelloIntervalFromType() {
    OspfInterfaceSettings ospfInterfaceSettings = new OspfInterfaceSettings(Ip.ZERO);
    // Since the hello interval is not set, it should be inferred from the network type
    ospfInterfaceSettings.setOspfInterfaceType(OspfInterfaceType.P2P);
    assertThat(toOspfHelloInterval(ospfInterfaceSettings), equalTo(DEFAULT_HELLO_INTERVAL));
    ospfInterfaceSettings.setOspfInterfaceType(OspfInterfaceType.NBMA);
    assertThat(toOspfHelloInterval(ospfInterfaceSettings), equalTo(DEFAULT_NBMA_HELLO_INTERVAL));
  }

  @Test
  public void testBuildSecurityPolicyAcl_traceElement_fromHost() {
    JuniperConfiguration config = createConfig();
    Zone zone = new Zone("toZone", null);

    ConcreteFirewallFilter crossZoneFilter =
        new ConcreteFirewallFilter("crossZonePolicy", Family.INET);

    zone.getFromZonePolicies().put("crossZonePolicy", crossZoneFilter);

    config.buildSecurityPolicyAcl("acl", zone);
    IpAccessList acl = config._c.getIpAccessLists().get("acl");
    assertNotNull(acl);
    assertThat(acl.getLines(), hasSize(3));

    assertThat(
        acl.getLines().get(0).getTraceElement(),
        equalTo(TraceElement.of("Matched Juniper semantics on traffic originated from device")));

    assertThat(
        acl.getLines().get(1).getTraceElement(),
        equalTo(
            TraceElement.builder()
                .add("Matched ")
                .add(
                    "security policy from junos-host to zone toZone",
                    new VendorStructureId(
                        config.getFilename(),
                        SECURITY_POLICY.getDescription(),
                        zoneToZoneFilter("junos-host", "toZone")))
                .build()));

    assertThat(
        acl.getLines().get(2).getTraceElement(),
        equalTo(TraceElement.of("Matched default policy")));
  }

  @Test
  public void testBuildSecurityPolicyAcl_traceElement_fromZone() {
    JuniperConfiguration config = createConfig();
    Zone zone = new Zone("toZone", null);

    ConcreteFirewallFilter crossZoneFilter =
        new ConcreteFirewallFilter("crossZonePolicy", Family.INET);

    crossZoneFilter.setFromZone("fromZone");

    zone.getFromZonePolicies().put("crossZonePolicy", crossZoneFilter);

    config.buildSecurityPolicyAcl("acl", zone);
    IpAccessList acl = config._c.getIpAccessLists().get("acl");
    assertNotNull(acl);
    assertThat(acl.getLines(), hasSize(3));

    assertThat(
        acl.getLines().get(0).getTraceElement(),
        equalTo(TraceElement.of("Matched Juniper semantics on traffic originated from device")));

    assertThat(
        acl.getLines().get(1).getTraceElement(),
        equalTo(
            TraceElement.builder()
                .add("Matched ")
                .add(
                    "security policy from zone fromZone to zone toZone",
                    new VendorStructureId(
                        config.getFilename(),
                        SECURITY_POLICY.getDescription(),
                        zoneToZoneFilter("fromZone", "toZone")))
                .build()));

    assertThat(
        acl.getLines().get(2).getTraceElement(),
        equalTo(TraceElement.of("Matched default policy")));
  }

  @Test
  public void testFwTermToIpAccessList_traceElement() {
    JuniperConfiguration config = createConfig();
    FwTerm term = new FwTerm("term");
    term.getThens().add(FwThenAccept.INSTANCE);
    ConcreteFirewallFilter filter = new ConcreteFirewallFilter("acl", Family.INET);
    filter.getTerms().put(term.getName(), term);
    IpAccessList acl = config.fwTermsToIpAccessList("acl", filter, null, FIREWALL_FILTER);

    assertThat(acl.getLines(), hasSize(1));

    TraceElement expected =
        TraceElement.builder()
            .add("Matched ")
            .add(
                "term",
                new VendorStructureId("file", FIREWALL_FILTER_TERM.getDescription(), "acl term"))
            .build();
    TraceElement actual = acl.getLines().get(0).getTraceElement();
    assertThat(actual, equalTo(expected));
    assertThat(actual.getText(), equalTo("Matched term"));
  }

  @Test
  public void testMergeIpAccessListLines() {
    List<ExprAclLine> lines =
        ImmutableList.of(
            new ExprAclLine(
                LineAction.PERMIT,
                new MatchHeaderSpace(
                    HeaderSpace.builder().setSrcIps(Ip.parse("1.1.1.1").toIpSpace()).build()),
                "match1",
                TraceElement.of("trace of match1"),
                null));

    MatchHeaderSpace conj =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstIps(Ip.parse("2.2.2.2").toIpSpace()).build());

    List<AclLine> aclLines = mergeIpAccessListLines(lines, conj);

    assertThat(aclLines, hasSize(1));
    assertThat(aclLines.get(0).getTraceElement(), equalTo(TraceElement.of("trace of match1")));
  }

  @Test
  public void testRouterIdInference() {
    RoutingInstance ri = new RoutingInstance("RI");
    Ip routerId = Ip.parse("9.9.9.9");
    Ip loopBackIp1 = Ip.parse("1.1.1.2");
    Ip loopBackIp2 = Ip.parse("1.1.1.3");
    Ip ifaceIp1 = Ip.parse("1.1.1.4");
    Ip ifaceIp2 = Ip.parse("1.1.1.5");

    ri.setRouterId(routerId);
    Interface lo0 = new Interface("lo0.0");
    lo0.setPrimaryAddress(ConcreteInterfaceAddress.create(loopBackIp1, Prefix.MAX_PREFIX_LENGTH));
    Interface lo1 = new Interface("lo0.1");
    lo1.setPrimaryAddress(ConcreteInterfaceAddress.create(loopBackIp2, Prefix.MAX_PREFIX_LENGTH));
    Interface i1 = new Interface("ge-0/0/0.0");
    i1.setPrimaryAddress(ConcreteInterfaceAddress.create(ifaceIp1, Prefix.MAX_PREFIX_LENGTH - 1));
    Interface i2 = new Interface("ge-0/0/0.1");
    i2.setPrimaryAddress(ConcreteInterfaceAddress.create(ifaceIp2, Prefix.MAX_PREFIX_LENGTH - 1));
    ri.getInterfaces().put(lo0.getName(), lo0);
    ri.getInterfaces().put(lo1.getName(), lo1);
    ri.getInterfaces().put(i1.getName(), i1);
    ri.getInterfaces().put(i2.getName(), i2);
    // Return configured router id
    assertThat(getRouterId(ri), equalTo(routerId));
    ri.setRouterId(null); // clear router id
    // Lowest loopback address is returned
    assertThat(getRouterId(ri), equalTo(loopBackIp1));
    // Remove loopbacks
    ri.getInterfaces().remove(lo0.getName());
    ri.getInterfaces().remove(lo1.getName());
    // Lowest IP is returned
    assertThat(getRouterId(ri), equalTo(ifaceIp1));
    // Remove interfaces
    ri.getInterfaces().clear();
    // Zero is returned
    assertThat(getRouterId(ri), equalTo(Ip.ZERO));
  }

  /** Check that vendor structure id is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", JuniperStructureType.PREFIX_LIST.getDescription(), "name")));
  }

  /** Check that tracing is properly added during routing policy conversion */
  @Test
  public void toRoutingPolicy_trace() {
    JuniperConfiguration vsC = new JuniperConfiguration();
    vsC.setFilename("file");

    PolicyStatement ps = new PolicyStatement("ps");
    PsTerm term = new PsTerm("psterm");
    term.getThens().addPsThen(new PsThenTag(234L));
    ps.getTerms().put(term.getName(), term);

    // no Froms case: expect a traceable (for the then we added) and end of policy
    List<Statement> statements = vsC.toRoutingPolicy(ps).getStatements();
    assertThat(statements, contains(instanceOf(TraceableStatement.class), instanceOf(If.class)));
    TraceableStatement traceableStatement = (TraceableStatement) statements.get(0);
    assertThat(traceableStatement.getInnerStatements(), contains(instanceOf(SetTag.class)));
    assertThat(
        traceableStatement.getTraceElement(),
        equalTo(
            toTraceableStatement(ImmutableList.of(), "psterm", "ps", "file").getTraceElement()));

    // Froms case: expect an If with traceable then, and another If that is end-of-policy
    term.getFroms().addFromTag(new PsFromTag(23L));
    List<Statement> statementsWithFroms = vsC.toRoutingPolicy(ps).getStatements();
    assertThat(statementsWithFroms, contains(instanceOf(If.class), instanceOf(If.class)));
    If ifStatement = (If) statementsWithFroms.get(0);
    assertThat(ifStatement.getTrueStatements(), contains(instanceOf(TraceableStatement.class)));
    assertThat(
        traceableStatement.getTraceElement(),
        equalTo(
            toTraceableStatement(ImmutableList.of(), "psterm", "ps", "file").getTraceElement()));
  }

  private Configuration createViConfig(List<String> ifaceNames) {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    ifaceNames.forEach(
        ifaceName ->
            org.batfish.datamodel.TestInterface.builder().setName(ifaceName).setOwner(c).build());
    return c;
  }

  @Test
  public void testGetDefaultSourceAddress() {
    {
      // neither lo0 nor primary exists
      assertThat(
          getDefaultSourceAddress(new RoutingInstance("ri"), createViConfig(ImmutableList.of())),
          equalTo(Optional.empty()));
    }

    {
      // when both lo0 and primary are present in the routing instance
      Interface lo0 = new Interface("lo0.0");
      Interface em0 = new Interface("em0.0");
      em0.setPrimary(true);
      em0.setPrimaryAddress(ConcreteInterfaceAddress.parse("10.10.10.1/31"));

      RoutingInstance ri = new RoutingInstance("ri");
      ri.getInterfaces().put(lo0.getName(), lo0);
      ri.getInterfaces().put(em0.getName(), em0);

      Configuration c = createViConfig(ImmutableList.of(lo0.getName(), em0.getName()));

      // l0 has no address
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.of(Ip.parse("10.10.10.1"))));

      // l0 has legal (not 127.0.0.1) address
      lo0.setPrimaryAddress(ConcreteInterfaceAddress.parse("192.168.1.1/32"));
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.of(Ip.parse("192.168.1.1"))));

      // l0 is not active
      c.getAllInterfaces().get(lo0.getName()).adminDown();
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.of(Ip.parse("10.10.10.1"))));
    }

    {
      // only primary interface is present in the routing instance
      Interface em0 = new Interface("em0.0");
      em0.setPrimary(true);

      RoutingInstance ri = new RoutingInstance("ri");
      ri.getInterfaces().put(em0.getName(), em0);

      Configuration c = createViConfig(ImmutableList.of(em0.getName()));

      // primary interface has no address
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.empty()));

      // primary interface has address
      em0.getAllAddresses().add(ConcreteInterfaceAddress.parse("10.10.10.1/31"));
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.of(Ip.parse("10.10.10.1"))));

      // primary interface is inactive
      c.getAllInterfaces().get(em0.getName()).adminDown();
      assertThat(getDefaultSourceAddress(ri, c), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testGetPrimaryInterface() {
    {
      // prefer explicit primary
      Interface explicitPrimary = new Interface("xe-0/0/0.0");
      explicitPrimary.setPrimary(true);

      Interface implicitPrimary = new Interface("em0.0");
      implicitPrimary.getAllAddresses().add(ConcreteInterfaceAddress.parse("10.10.10.1/31"));

      Configuration c =
          createViConfig(ImmutableList.of(explicitPrimary.getName(), implicitPrimary.getName()));

      RoutingInstance ri = new RoutingInstance("ri");
      ri.getInterfaces().put(explicitPrimary.getName(), explicitPrimary);
      ri.getInterfaces().put(implicitPrimary.getName(), implicitPrimary);

      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(explicitPrimary)));

      // inactive explicit primary
      c.getAllInterfaces().get(explicitPrimary.getName()).adminDown();
      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(implicitPrimary)));
    }
    {
      // prefer management if it has an address
      Interface mgmtInterface = new Interface("em0.0");

      Interface otherInterface = new Interface("xe-0/0/0.0");
      otherInterface.getAllAddresses().add(ConcreteInterfaceAddress.parse("10.10.10.1/31"));

      RoutingInstance ri = new RoutingInstance("ri");
      ri.getInterfaces().put(mgmtInterface.getName(), mgmtInterface);
      ri.getInterfaces().put(otherInterface.getName(), otherInterface);

      Configuration c =
          createViConfig(ImmutableList.of(mgmtInterface.getName(), otherInterface.getName()));

      // mgmt interface without address
      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(otherInterface)));

      // mgmt interface with address
      mgmtInterface.getAllAddresses().add(ConcreteInterfaceAddress.parse("10.10.10.1/31"));
      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(mgmtInterface)));

      // inactive mgmt interface
      c.getAllInterfaces().get(mgmtInterface.getName()).adminDown();
      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(otherInterface)));
    }
    {
      // return other interfaces with address
      Interface noAddressInterface = new Interface("xe-0/0/0.0");

      Interface addressedInterface = new Interface("xe-1/0/0.0");
      addressedInterface.getAllAddresses().add(ConcreteInterfaceAddress.parse("10.10.10.1/31"));

      Configuration c =
          createViConfig(
              ImmutableList.of(noAddressInterface.getName(), addressedInterface.getName()));

      RoutingInstance ri = new RoutingInstance("ri");
      ri.getInterfaces().put(addressedInterface.getName(), addressedInterface);
      ri.getInterfaces().put(noAddressInterface.getName(), noAddressInterface);

      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.of(addressedInterface)));

      // inactive addressed interface
      // inactive mgmt interface
      c.getAllInterfaces().get(addressedInterface.getName()).adminDown();
      assertThat(getPrimaryInterface(ri, c), equalTo(Optional.empty()));
    }
  }

  @Test
  public void testDefaultSourceAddress() {
    ConcreteInterfaceAddress ignoredAddress = ConcreteInterfaceAddress.parse("127.0.0.1/32");
    ConcreteInterfaceAddress addr1 = ConcreteInterfaceAddress.parse("1.1.1.1/32");
    ConcreteInterfaceAddress addr2 = ConcreteInterfaceAddress.parse("2.2.2.2/32");
    {
      // ignored IP is always ignored
      Interface iface = new Interface("em0.0");
      iface.setPrimaryAddress(ignoredAddress);
      iface.setPreferredAddress(ignoredAddress);
      iface.getAllAddresses().add(ignoredAddress);
      assertThat(getDefaultSourceAddress(iface), equalTo(Optional.empty()));
    }
    {
      // first pref: explicit primary
      Interface iface = new Interface("em0.0");
      iface.setPrimaryAddress(addr2);
      iface.setPreferredAddress(addr1);
      iface.getAllAddresses().add(addr1);
      assertThat(getDefaultSourceAddress(iface), equalTo(Optional.of(addr2.getIp())));
    }
    {
      // second pref: preferred address
      Interface iface = new Interface("em0.0");
      iface.setPreferredAddress(addr2);
      iface.getAllAddresses().add(addr1);
      assertThat(getDefaultSourceAddress(iface), equalTo(Optional.of(addr2.getIp())));
    }
    {
      // last pref: lowest address
      Interface iface = new Interface("em0.0");
      iface.getAllAddresses().add(addr1);
      iface.getAllAddresses().add(addr2);
      assertThat(getDefaultSourceAddress(iface), equalTo(Optional.of(addr1.getIp())));
    }
  }

  @Test
  public void testConvertEvpnVrfLeaking_BothPolicies() {
    JuniperConfiguration config = createConfig();
    config._c.setVrfs(
        ImmutableMap.of(
            Configuration.DEFAULT_VRF_NAME,
            new Vrf(Configuration.DEFAULT_VRF_NAME),
            "tenant",
            new Vrf("tenant")));

    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    defaultRi.setRouterId(Ip.parse("1.1.1.1"));
    defaultRi.setAs(65000L);
    RoutingInstance tenantRi = new RoutingInstance("tenant");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setVni(100);
    tenantRi
        .getOrCreateEvpnIpPrefixRoutes()
        .setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
    tenantRi.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));
    tenantRi.setVrfTargetExport(ExtendedCommunity.target(1, 1));

    tenantRi.setVrfImportPolicy("vrf-import-policy");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setImportPolicy("ipr-import-policy");

    config
        .getMasterLogicalSystem()
        .getRoutingInstances()
        .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
    config.getMasterLogicalSystem().getRoutingInstances().put("tenant", tenantRi);

    config.convertEvpnVrfLeaking();

    Vrf tenantVrf = config._c.getVrfs().get("tenant");
    assertNotNull(tenantVrf.getVrfLeakConfig());
    String importPolicyName =
        tenantVrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs().get(0).getImportPolicy();
    org.batfish.datamodel.routing_policy.RoutingPolicy importPolicy =
        config._c.getRoutingPolicies().get(importPolicyName);
    assertNotNull(importPolicy);

    // Expecting two if statements that call the respective policies
    assertThat(importPolicy.getStatements(), hasSize(2));
    org.batfish.datamodel.routing_policy.statement.If firstIf =
        (org.batfish.datamodel.routing_policy.statement.If) importPolicy.getStatements().get(0);
    org.batfish.datamodel.routing_policy.statement.If secondIf =
        (org.batfish.datamodel.routing_policy.statement.If) importPolicy.getStatements().get(1);

    assertThat(
        firstIf.getGuard(), instanceOf(org.batfish.datamodel.routing_policy.expr.CallExpr.class));
    assertThat(
        ((org.batfish.datamodel.routing_policy.expr.CallExpr) firstIf.getGuard())
            .getCalledPolicyName(),
        equalTo("vrf-import-policy"));

    assertThat(
        secondIf.getGuard(), instanceOf(org.batfish.datamodel.routing_policy.expr.CallExpr.class));
    assertThat(
        ((org.batfish.datamodel.routing_policy.expr.CallExpr) secondIf.getGuard())
            .getCalledPolicyName(),
        equalTo("ipr-import-policy"));
  }

  @Test
  public void testConvertEvpnVrfLeaking_SinglePolicy() {
    JuniperConfiguration config = createConfig();
    config._c.setVrfs(
        ImmutableMap.of(
            Configuration.DEFAULT_VRF_NAME,
            new Vrf(Configuration.DEFAULT_VRF_NAME),
            "tenant",
            new Vrf("tenant")));

    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    defaultRi.setRouterId(Ip.parse("1.1.1.1"));
    defaultRi.setAs(65000L);
    RoutingInstance tenantRi = new RoutingInstance("tenant");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setVni(100);
    tenantRi
        .getOrCreateEvpnIpPrefixRoutes()
        .setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
    tenantRi.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));
    tenantRi.setVrfTargetExport(ExtendedCommunity.target(1, 1));

    tenantRi.setVrfImportPolicy("vrf-import-policy");

    config
        .getMasterLogicalSystem()
        .getRoutingInstances()
        .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
    config.getMasterLogicalSystem().getRoutingInstances().put("tenant", tenantRi);

    config.convertEvpnVrfLeaking();

    Vrf tenantVrf = config._c.getVrfs().get("tenant");
    assertNotNull(tenantVrf.getVrfLeakConfig());
    String importPolicyName =
        tenantVrf.getVrfLeakConfig().getEvpnToBgpv4VrfLeakConfigs().get(0).getImportPolicy();
    assertThat(importPolicyName, equalTo("vrf-import-policy"));
  }

  @Test
  public void testConvertEvpnVrfLeaking_VrfExportOnly() {
    JuniperConfiguration config = createConfig();
    config._c.setVrfs(
        ImmutableMap.of(
            Configuration.DEFAULT_VRF_NAME,
            new Vrf(Configuration.DEFAULT_VRF_NAME),
            "tenant",
            new Vrf("tenant")));

    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    defaultRi.setRouterId(Ip.parse("1.1.1.1"));
    defaultRi.setAs(65000L);
    RoutingInstance tenantRi = new RoutingInstance("tenant");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setVni(100);
    tenantRi
        .getOrCreateEvpnIpPrefixRoutes()
        .setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
    tenantRi.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));
    tenantRi.setVrfTargetExport(ExtendedCommunity.target(1, 1));
    tenantRi.setVrfTargetImport(ExtendedCommunity.target(1, 1));

    tenantRi.setVrfExportPolicy("vrf-export-policy");
    // Register the policy so containsKey check passes
    config
        ._c
        .getRoutingPolicies()
        .put(
            "vrf-export-policy",
            RoutingPolicy.builder().setOwner(config._c).setName("vrf-export-policy").build());

    config
        .getMasterLogicalSystem()
        .getRoutingInstances()
        .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
    config.getMasterLogicalSystem().getRoutingInstances().put("tenant", tenantRi);

    config.convertEvpnVrfLeaking();

    Vrf tenantVrf = config._c.getVrfs().get("tenant");
    assertNotNull(tenantVrf.getBgpProcess());
    String redistPolicyName = tenantVrf.getBgpProcess().getRedistributionPolicy();
    org.batfish.datamodel.routing_policy.RoutingPolicy redistPolicy =
        config._c.getRoutingPolicies().get(redistPolicyName);
    assertNotNull(redistPolicy);

    // Expecting: If(call vrf-export-policy -> return true), then return false
    assertThat(redistPolicy.getStatements(), hasSize(2));
    org.batfish.datamodel.routing_policy.statement.If firstIf =
        (org.batfish.datamodel.routing_policy.statement.If) redistPolicy.getStatements().get(0);
    assertThat(
        firstIf.getGuard(), instanceOf(org.batfish.datamodel.routing_policy.expr.CallExpr.class));
    assertThat(
        ((org.batfish.datamodel.routing_policy.expr.CallExpr) firstIf.getGuard())
            .getCalledPolicyName(),
        equalTo("vrf-export-policy"));
  }

  @Test
  public void testConvertEvpnVrfLeaking_BothExportPolicies() {
    JuniperConfiguration config = createConfig();
    config._c.setVrfs(
        ImmutableMap.of(
            Configuration.DEFAULT_VRF_NAME,
            new Vrf(Configuration.DEFAULT_VRF_NAME),
            "tenant",
            new Vrf("tenant")));

    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    defaultRi.setRouterId(Ip.parse("1.1.1.1"));
    defaultRi.setAs(65000L);
    RoutingInstance tenantRi = new RoutingInstance("tenant");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setVni(100);
    tenantRi
        .getOrCreateEvpnIpPrefixRoutes()
        .setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
    tenantRi.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));
    tenantRi.setVrfTargetExport(ExtendedCommunity.target(1, 1));
    tenantRi.setVrfTargetImport(ExtendedCommunity.target(1, 1));

    tenantRi.setVrfExportPolicy("vrf-export-policy");
    tenantRi.getOrCreateEvpnIpPrefixRoutes().setExportPolicy("ipr-export-policy");
    // Register both policies so containsKey checks pass
    config
        ._c
        .getRoutingPolicies()
        .put(
            "vrf-export-policy",
            RoutingPolicy.builder().setOwner(config._c).setName("vrf-export-policy").build());
    config
        ._c
        .getRoutingPolicies()
        .put(
            "ipr-export-policy",
            RoutingPolicy.builder().setOwner(config._c).setName("ipr-export-policy").build());

    config
        .getMasterLogicalSystem()
        .getRoutingInstances()
        .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
    config.getMasterLogicalSystem().getRoutingInstances().put("tenant", tenantRi);

    config.convertEvpnVrfLeaking();

    Vrf tenantVrf = config._c.getVrfs().get("tenant");
    assertNotNull(tenantVrf.getBgpProcess());
    String redistPolicyName = tenantVrf.getBgpProcess().getRedistributionPolicy();
    org.batfish.datamodel.routing_policy.RoutingPolicy redistPolicy =
        config._c.getRoutingPolicies().get(redistPolicyName);
    assertNotNull(redistPolicy);

    // Expecting two if statements that chain the export policies
    assertThat(redistPolicy.getStatements(), hasSize(2));
    org.batfish.datamodel.routing_policy.statement.If firstIf =
        (org.batfish.datamodel.routing_policy.statement.If) redistPolicy.getStatements().get(0);
    org.batfish.datamodel.routing_policy.statement.If secondIf =
        (org.batfish.datamodel.routing_policy.statement.If) redistPolicy.getStatements().get(1);

    assertThat(
        firstIf.getGuard(), instanceOf(org.batfish.datamodel.routing_policy.expr.CallExpr.class));
    assertThat(
        ((org.batfish.datamodel.routing_policy.expr.CallExpr) firstIf.getGuard())
            .getCalledPolicyName(),
        equalTo("vrf-export-policy"));

    assertThat(
        secondIf.getGuard(), instanceOf(org.batfish.datamodel.routing_policy.expr.CallExpr.class));
    assertThat(
        ((org.batfish.datamodel.routing_policy.expr.CallExpr) secondIf.getGuard())
            .getCalledPolicyName(),
        equalTo("ipr-export-policy"));
  }

  @Test
  public void testConvertEvpnVrfLeaking_MissingRequiredConfig() {
    String riName = "tenant";
    Map<Consumer<RoutingInstance>, String> omissions = new LinkedHashMap<>();
    omissions.put(
        ri -> ri.getEvpnIpPrefixRoutes().setVni(null),
        "IP-VRF not created for routing-instance " + riName + ": missing \"ip-prefix-routes vni\"");
    omissions.put(
        ri -> ri.getEvpnIpPrefixRoutes().setAdvertise(null),
        "IP-VRF not created for routing-instance "
            + riName
            + ": missing \"ip-prefix-routes advertise\"");
    omissions.put(
        ri -> ri.getEvpnIpPrefixRoutes().setEncapsulation(null),
        "IP-VRF not created for routing-instance "
            + riName
            + ": missing \"ip-prefix-routes encapsulation\"");
    omissions.put(
        ri -> ri.setRouteDistinguisher(null),
        "IP-VRF not created for routing-instance " + riName + ": missing \"route-distinguisher\"");
    omissions.put(
        ri -> ri.setVrfTargetExport(null),
        "IP-VRF missing import or export policy in routing-instance " + riName);

    for (Map.Entry<Consumer<RoutingInstance>, String> testCase : omissions.entrySet()) {
      JuniperConfiguration config = createConfig();
      RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
      defaultRi.setRouterId(Ip.parse("1.1.1.1"));
      defaultRi.setAs(65000L);
      config
          .getMasterLogicalSystem()
          .getRoutingInstances()
          .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
      config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
      config.setWarnings(new Warnings(true, true, true));

      RoutingInstance ri = new RoutingInstance(riName);
      ri.getOrCreateEvpnIpPrefixRoutes().setVni(100);
      ri.getOrCreateEvpnIpPrefixRoutes().setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
      ri.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
      ri.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));
      ri.setVrfTargetExport(ExtendedCommunity.target(1, 1));

      // Execute omission
      testCase.getKey().accept(ri);

      config._c.setVrfs(
          ImmutableMap.of(
              Configuration.DEFAULT_VRF_NAME,
              new Vrf(Configuration.DEFAULT_VRF_NAME),
              riName,
              new Vrf(riName)));
      config.getMasterLogicalSystem().getRoutingInstances().put(riName, ri);

      config.convertEvpnVrfLeaking();

      assertThat(
          "Testing omission expecting warning: " + testCase.getValue(),
          config.getWarnings().getRedFlagWarnings(),
          contains(new Warning(testCase.getValue(), "MISCELLANEOUS")));
    }
  }

  @Test
  public void testConvertEvpnVrfLeaking_MissingExportOrImportPolicy() {
    String riName = "tenant";
    Map<Consumer<RoutingInstance>, String> omissions = new LinkedHashMap<>();
    omissions.put(
        ri -> {
          ri.setVrfTargetExport(null);
          ri.setVrfImportPolicy("import-policy");
        },
        "IP-VRF missing vrf-target or vrf-target export in routing-instance " + riName);
    omissions.put(
        ri -> {
          ri.setVrfImportPolicy(null);
          ri.setVrfTargetExport(ExtendedCommunity.target(1, 1));
        },
        "IP-VRF missing import policy or vrf-target in routing-instance " + riName);

    for (Map.Entry<Consumer<RoutingInstance>, String> testCase : omissions.entrySet()) {
      JuniperConfiguration config = createConfig();
      RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
      defaultRi.setRouterId(Ip.parse("1.1.1.1"));
      defaultRi.setAs(65000L);
      config
          .getMasterLogicalSystem()
          .getRoutingInstances()
          .put(Configuration.DEFAULT_VRF_NAME, defaultRi);
      config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);
      config.setWarnings(new Warnings(true, true, true));

      RoutingInstance ri = new RoutingInstance(riName);
      ri.getOrCreateEvpnIpPrefixRoutes().setVni(100);
      ri.getOrCreateEvpnIpPrefixRoutes().setAdvertise(EvpnIpPrefixRoutesAdvertise.DIRECT_NEXTHOP);
      ri.getOrCreateEvpnIpPrefixRoutes().setEncapsulation(EvpnEncapsulation.VXLAN);
      ri.setRouteDistinguisher(RouteDistinguisher.from(1L, 1L));

      // Execute omission
      testCase.getKey().accept(ri);

      config._c.setVrfs(
          ImmutableMap.of(
              Configuration.DEFAULT_VRF_NAME,
              new Vrf(Configuration.DEFAULT_VRF_NAME),
              riName,
              new Vrf(riName)));
      config.getMasterLogicalSystem().getRoutingInstances().put(riName, ri);

      config.convertEvpnVrfLeaking();

      assertThat(
          "Testing omission expecting pedantic warning: " + testCase.getValue(),
          config.getWarnings().getPedanticWarnings(),
          contains(new Warning(testCase.getValue(), TAG_PEDANTIC)));
    }
  }

  @Test
  public void testConvertRouteDistinguishers_NoRdId() {
    JuniperConfiguration config = createConfig();
    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);

    RoutingInstance ri1 = new RoutingInstance("VRF1");
    config.getMasterLogicalSystem().getRoutingInstances().put("VRF1", ri1);

    // No route-distinguisher-id set, so convertRouteDistinguishers should be a no-op
    config.convertRouteDistinguishers();

    assertThat(ri1.getRouteDistinguisher(), nullValue());
  }

  @Test
  public void testConvertRouteDistinguishers_AutoGeneratesRds() {
    JuniperConfiguration config = createConfig();
    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    Ip rdId = Ip.parse("10.0.0.1");
    defaultRi.setRouteDistinguisherId(rdId);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);

    RoutingInstance ri1 = new RoutingInstance("VRF1");
    RoutingInstance ri2 = new RoutingInstance("VRF2");
    config.getMasterLogicalSystem().getRoutingInstances().put("VRF1", ri1);
    config.getMasterLogicalSystem().getRoutingInstances().put("VRF2", ri2);

    config.convertRouteDistinguishers();

    assertThat(ri1.getRouteDistinguisher(), equalTo(RouteDistinguisher.from(rdId, 1)));
    assertThat(ri2.getRouteDistinguisher(), equalTo(RouteDistinguisher.from(rdId, 2)));
  }

  @Test
  public void testConvertRouteDistinguishers_SkipsExplicitRd() {
    JuniperConfiguration config = createConfig();
    RoutingInstance defaultRi = new RoutingInstance(Configuration.DEFAULT_VRF_NAME);
    Ip rdId = Ip.parse("10.0.0.1");
    defaultRi.setRouteDistinguisherId(rdId);
    config.getMasterLogicalSystem().setDefaultRoutingInstance(defaultRi);

    RoutingInstance ri1 = new RoutingInstance("VRF1");
    RouteDistinguisher explicitRd = RouteDistinguisher.from(Ip.parse("10.0.0.2"), 99);
    ri1.setRouteDistinguisher(explicitRd);
    RoutingInstance ri2 = new RoutingInstance("VRF2");
    config.getMasterLogicalSystem().getRoutingInstances().put("VRF1", ri1);
    config.getMasterLogicalSystem().getRoutingInstances().put("VRF2", ri2);

    config.convertRouteDistinguishers();

    // ri1 already has an explicit RD, should not be overwritten
    assertThat(ri1.getRouteDistinguisher(), equalTo(explicitRd));
    // ri2 should get an auto-generated RD (index 2)
    assertThat(ri2.getRouteDistinguisher(), equalTo(RouteDistinguisher.from(rdId, 2)));
  }
}
