package org.batfish.vendor.check_point_gateway.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.CHECK_POINT_GATEWAY;
import static org.batfish.datamodel.FirewallSessionInterfaceInfo.Action.POST_NAT_FIB_LOOKUP;
import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.InterfaceType.AGGREGATED;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasIncomingFilter;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRedFlagWarning;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroup;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroupMembers;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDependencies;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration.INTERFACE_ACL_NAME;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration.SYNC_INTERFACE_NAME;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.aclName;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.HIDE_BEHIND_GATEWAY_IP;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_ETH_SPEED;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_INTERFACE_MTU;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_LOOPBACK_MTU;
import static org.batfish.vendor.check_point_management.NatMethod.HIDE;
import static org.batfish.vendor.check_point_management.TestSharedInstances.NAT_SETTINGS_TEST_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.matchers.ParseWarningMatchers;
import org.batfish.common.matchers.WarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.IngressInterfaceVrf;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.PacketPolicyEvaluator;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.LacpRate;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.Mode;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.XmitHashPolicy;
import org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration;
import org.batfish.vendor.check_point_gateway.representation.Interface;
import org.batfish.vendor.check_point_gateway.representation.Interface.LinkSpeed;
import org.batfish.vendor.check_point_gateway.representation.Nexthop;
import org.batfish.vendor.check_point_gateway.representation.NexthopAddress;
import org.batfish.vendor.check_point_gateway.representation.NexthopBlackhole;
import org.batfish.vendor.check_point_gateway.representation.NexthopLogical;
import org.batfish.vendor.check_point_gateway.representation.NexthopReject;
import org.batfish.vendor.check_point_gateway.representation.NexthopTarget;
import org.batfish.vendor.check_point_gateway.representation.StaticRoute;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AccessRule;
import org.batfish.vendor.check_point_management.AccessRuleOrSection;
import org.batfish.vendor.check_point_management.AllInstallationTargets;
import org.batfish.vendor.check_point_management.CheckpointManagementConfiguration;
import org.batfish.vendor.check_point_management.CpmiAnyObject;
import org.batfish.vendor.check_point_management.CpmiClusterMember;
import org.batfish.vendor.check_point_management.CpmiGatewayCluster;
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.GatewayOrServerPolicy;
import org.batfish.vendor.check_point_management.Host;
import org.batfish.vendor.check_point_management.InterfaceTopology;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.ManagementPackage;
import org.batfish.vendor.check_point_management.ManagementServer;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatHideBehindGateway;
import org.batfish.vendor.check_point_management.NatHideBehindIp;
import org.batfish.vendor.check_point_management.NatMethod;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.NatSettings;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Original;
import org.batfish.vendor.check_point_management.Package;
import org.batfish.vendor.check_point_management.PolicyTargets;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.SimpleGateway;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnknownTypedManagementObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class CheckPointGatewayGrammarTest {

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/vendor/check_point_gateway/grammar/testconfigs/";
  private static final String SNAPSHOTS_PREFIX =
      "org/batfish/vendor/check_point_gateway/grammar/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull CheckPointGatewayConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CheckPointGatewayCombinedParser parser = new CheckPointGatewayCombinedParser(src, settings);
    Warnings parseWarnings = new Warnings();
    CheckPointGatewayControlPlaneExtractor extractor =
        new CheckPointGatewayControlPlaneExtractor(
            src, parser, parseWarnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    CheckPointGatewayConfiguration vendorConfiguration =
        (CheckPointGatewayConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    CheckPointGatewayConfiguration vc = SerializationUtils.clone(vendorConfiguration);
    vc.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    vc.setWarnings(parseWarnings);
    return vc;
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    return getBatfishForConfigurationNames(null, configurationNames);
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(
      @Nullable CheckpointManagementConfiguration mgmt, String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    ConversionContext conversionContext = new ConversionContext();
    conversionContext.setCheckpointManagementConfiguration(mgmt);
    return BatfishTestUtils.getBatfishForTextConfigsAndConversionContext(
        _folder, conversionContext, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    return parseConfig(hostname, null);
  }

  private @Nonnull Configuration parseConfig(
      String hostname, @Nullable CheckpointManagementConfiguration mgmt) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(mgmt, hostname);
      String canonicalHostname = hostname.toLowerCase();
      String canonicalChassisHostname = canonicalHostname + "-ch01-01";
      assertThat(configs, anyOf(hasKey(canonicalHostname), hasKey(canonicalChassisHostname)));
      Configuration c =
          configs.getOrDefault(canonicalHostname, configs.get(canonicalChassisHostname));
      assertThat(c, hasConfigurationFormat(CHECK_POINT_GATEWAY));
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(
      @Nullable CheckpointManagementConfiguration mgmt, String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(mgmt, configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  /**
   * Build a simple {@link CheckpointManagementConfiguration} with a single {@link ManagementDomain}
   * with the specified {@code gateways} and {@code packages}.
   */
  private CheckpointManagementConfiguration toCheckpointMgmtConfig(
      Map<Uid, GatewayOrServer> gateways,
      Map<Uid, ManagementPackage> packages,
      List<TypedManagementObject> objects) {
    return new CheckpointManagementConfiguration(
        ImmutableMap.of(
            "s",
            new ManagementServer(
                ImmutableMap.of(
                    "d",
                    new ManagementDomain(
                        new Domain("d", Uid.of("0")), gateways, packages, objects)),
                "s")));
  }

  private static Flow createFlow(IpProtocol protocol, int sourcePort, int destinationPort) {
    Flow.Builder fb = Flow.builder();
    fb.setIngressNode("node");
    fb.setIpProtocol(protocol);
    fb.setDstPort(destinationPort);
    fb.setSrcPort(sourcePort);
    return fb.build();
  }

  private static Flow createFlow(String sourceAddress, String destinationAddress) {
    return createFlow(sourceAddress, destinationAddress, IpProtocol.TCP, 1, 1);
  }

  private static Flow createFlow(
      String sourceAddress,
      String destinationAddress,
      IpProtocol protocol,
      int sourcePort,
      int destinationPort) {
    return Flow.builder()
        .setIngressNode("node")
        .setSrcIp(Ip.parse(sourceAddress))
        .setDstIp(Ip.parse(destinationAddress))
        .setIpProtocol(protocol)
        .setDstPort(destinationPort)
        .setSrcPort(sourcePort)
        .build();
  }

  @Test
  public void testHostname() {
    String hostname = "hostname";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm hostname extracted as-typed; will be lower-cased in conversion.
    assertThat(c.getHostname(), equalTo("HOSTNAME"));
  }

  @Test
  public void testHostnameChassis() {
    String hostname = "hostname_chassis";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm the `%m` is replaced with a chassis identifier
    assertThat(c.getHostname(), equalTo("hostname_CHASSIS-ch01-01"));
  }

  @Test
  public void testHumanName() {
    Configuration c = parseConfig("hostname");
    assertThat(c, notNullValue());
    assertThat(c.getHostname(), equalTo("hostname"));
    assertThat(c.getHumanName(), equalTo("HOSTNAME"));

    c = parseConfig("hostname_chassis");
    assertThat(c, notNullValue());
    assertThat(c.getHostname(), equalTo("hostname_chassis-ch01-01"));
    assertThat(c.getHumanName(), equalTo("hostname_CHASSIS-ch01-01"));
  }

  @Test
  public void testDeviceModel() {
    String hostname = "hostname";
    Configuration c = parseConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(c.getDeviceModel(), equalTo(DeviceModel.CHECK_POINT_GATEWAY));
  }

  @Test
  public void testHostnameInvalid() throws IOException {
    String filename = "hostname_invalid";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(warnings, hasParseWarning(hasComment("Illegal value for device hostname")));
  }

  @Test
  public void testInterfaceExtraction() {
    String hostname = "interface";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(c.getInterfaces(), hasKeys("eth0", "eth1", "eth2", "eth3", "eth4", "eth5", "lo"));

    Interface eth0 = c.getInterfaces().get("eth0");
    Interface eth1 = c.getInterfaces().get("eth1");
    Interface eth2 = c.getInterfaces().get("eth2");
    Interface eth3 = c.getInterfaces().get("eth3");
    Interface eth4 = c.getInterfaces().get("eth4");
    Interface eth5 = c.getInterfaces().get("eth5");
    Interface lo = c.getInterfaces().get("lo");

    assertThat(eth0.getAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.1.1/24")));
    assertTrue(eth0.getAutoNegotiate());
    assertThat(eth0.getComments(), equalTo("double quoted\" comments#!with_txt_after_quote"));
    assertThat(eth0.getMtu(), equalTo(1234));
    assertTrue(eth0.getState());
    assertThat(eth2.getAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.100.1/17")));
    assertFalse(eth2.getAutoNegotiate());
    assertThat(eth2.getComments(), equalTo("single quoted comments"));
    assertFalse(eth2.getState());
    assertThat(lo.getComments(), equalTo("unquoted_comments"));
    // All possible link speeds
    assertThat(eth0.getLinkSpeed(), equalTo(LinkSpeed.THOUSAND_M_FULL));
    assertThat(eth2.getLinkSpeed(), equalTo(LinkSpeed.HUNDRED_M_FULL));
    assertThat(eth3.getLinkSpeed(), equalTo(LinkSpeed.HUNDRED_M_HALF));
    assertThat(eth4.getLinkSpeed(), equalTo(LinkSpeed.TEN_M_HALF));
    assertThat(eth5.getLinkSpeed(), equalTo(LinkSpeed.TEN_M_FULL));

    // Unset and defaults
    assertNull(eth1.getAddress());
    assertNull(eth1.getAutoNegotiate());
    assertNull(eth1.getComments());
    assertNull(eth1.getLinkSpeed());
    assertTrue(eth1.getState());
    assertNull(eth2.getMtu());
    assertThat(eth2.getMtuEffective(), equalTo(Interface.DEFAULT_INTERFACE_MTU));
  }

  @Test
  public void testInterfaceConversion() {
    String hostname = "interface_conversion";
    Configuration c = parseConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(c.getAllInterfaces(), hasKeys("eth0", "eth1", "eth2", "eth3", "eth4", "eth5", "lo"));

    org.batfish.datamodel.Interface eth0 = c.getAllInterfaces().get("eth0");
    org.batfish.datamodel.Interface eth1 = c.getAllInterfaces().get("eth1");
    org.batfish.datamodel.Interface eth2 = c.getAllInterfaces().get("eth2");
    org.batfish.datamodel.Interface eth3 = c.getAllInterfaces().get("eth3");
    org.batfish.datamodel.Interface eth4 = c.getAllInterfaces().get("eth4");
    org.batfish.datamodel.Interface eth5 = c.getAllInterfaces().get("eth5");
    org.batfish.datamodel.Interface lo = c.getAllInterfaces().get("lo");

    assertTrue(eth0.getActive());
    assertThat(eth0.getAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.1.1/24")));
    assertThat(eth0.getMtu(), equalTo(1234));
    assertThat(eth0.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(eth0.getBandwidth(), equalTo(1000e6));

    assertFalse(eth1.getActive());
    assertNull(eth1.getAddress());
    assertThat(eth1.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(eth1.getMtu(), equalTo(DEFAULT_INTERFACE_MTU));
    assertThat(eth1.getBandwidth(), equalTo(DEFAULT_ETH_SPEED));

    assertThat(eth2.getBandwidth(), equalTo(100e6));
    assertThat(eth3.getBandwidth(), equalTo(100e6));
    assertThat(eth4.getBandwidth(), equalTo(10e6));
    assertThat(eth5.getBandwidth(), equalTo(10e6));

    assertThat(lo.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.10/32")));
    assertThat(lo.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(lo.getMtu(), equalTo(DEFAULT_LOOPBACK_MTU));
    assertNull(lo.getBandwidth());
  }

  @Test
  public void testInterfaceWarning() throws IOException {
    String hostname = "interface_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected mask-length in range 1-32, but got '0'"),
                hasComment("Expected mask-length in range 1-32, but got '33'"),
                hasComment("Expected mtu in range 68-16000, but got '67'"),
                hasComment("Expected mtu in range 68-16000, but got '16001'"),
                hasComment("Subnet-mask 250.255.255.0 is not valid."),
                allOf(
                    hasComment("Illegal value for interface name"),
                    hasText(containsString("interface invalid+name"))))));
  }

  @Test
  public void testStaticRouteExtraction() {
    String hostname = "static_route";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);

    Prefix defPrefix = Prefix.ZERO;
    Prefix addrPrefix = Prefix.parse("10.10.0.0/16");
    Prefix blackholePrefix = Prefix.parse("10.11.0.0/16");
    Prefix rejectPrefix = Prefix.parse("10.12.0.0/16");

    NexthopTarget defTarget = new NexthopLogical("eth0");
    NexthopTarget addrTarget1 = new NexthopAddress(Ip.parse("10.10.10.11"));
    NexthopTarget addrTarget2 = new NexthopAddress(Ip.parse("10.10.10.12"));

    assertThat(c.getStaticRoutes(), hasKeys(defPrefix, addrPrefix, blackholePrefix, rejectPrefix));
    assertThat(
        c.getStaticRoutes().keySet(),
        containsInAnyOrder(defPrefix, blackholePrefix, rejectPrefix, addrPrefix));

    // Logical nexthop
    {
      StaticRoute def = c.getStaticRoutes().get(defPrefix);
      assertThat(def.getDestination(), equalTo(defPrefix));
      assertThat(def.getComment(), equalTo("this is a default route"));
      assertThat(def.getNexthops().keySet(), contains(defTarget));

      Nexthop nexthop = def.getNexthops().get(defTarget);
      assertNull(nexthop.getPriority());
      assertThat(nexthop.getNexthopTarget(), equalTo(defTarget));
    }

    // Blackhole nexthop
    {
      StaticRoute blackhole = c.getStaticRoutes().get(blackholePrefix);
      assertThat(blackhole.getDestination(), equalTo(blackholePrefix));
      assertThat(blackhole.getNexthops().keySet(), contains(NexthopBlackhole.INSTANCE));

      Nexthop nexthop = blackhole.getNexthops().get(NexthopBlackhole.INSTANCE);
      assertNull(nexthop.getPriority());
      assertThat(nexthop.getNexthopTarget(), equalTo(NexthopBlackhole.INSTANCE));
    }

    // Reject nexthop
    {
      StaticRoute reject = c.getStaticRoutes().get(rejectPrefix);
      assertThat(reject.getDestination(), equalTo(rejectPrefix));
      assertThat(reject.getNexthops().keySet(), contains(NexthopReject.INSTANCE));

      Nexthop nexthop = reject.getNexthops().get(NexthopReject.INSTANCE);
      assertNull(nexthop.getPriority());
      assertThat(nexthop.getNexthopTarget(), equalTo(NexthopReject.INSTANCE));
    }

    // Address nexthops
    {
      StaticRoute addr = c.getStaticRoutes().get(addrPrefix);
      assertThat(addr.getDestination(), equalTo(addrPrefix));
      assertThat(addr.getNexthops().keySet(), containsInAnyOrder(addrTarget1, addrTarget2));

      Nexthop nexthop1 = addr.getNexthops().get(addrTarget1);
      assertThat(nexthop1.getPriority(), equalTo(7));
      assertThat(nexthop1.getNexthopTarget(), equalTo(addrTarget1));

      Nexthop nexthop2 = addr.getNexthops().get(addrTarget2);
      assertNull(nexthop2.getPriority());
      assertThat(nexthop2.getNexthopTarget(), equalTo(addrTarget2));
    }
  }

  @Test
  public void testStaticRouteOffExtraction() {
    String hostname = "static_route_off";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);

    Prefix prefix = Prefix.parse("10.1.0.0/16");
    NexthopTarget target = new NexthopAddress(Ip.parse("10.1.0.2"));

    // Removed static route shouldn't show up, and removing the only nexthop should remove the route
    assertThat(c.getStaticRoutes(), hasKeys(prefix));

    // Removed nexthop shouldn't show up
    StaticRoute route = c.getStaticRoutes().get(prefix);
    assertThat(route.getNexthops(), hasKeys(target));

    assertThat(
        c.getWarnings().getParseWarnings(),
        containsInAnyOrder(
            allOf(
                hasComment("Cannot remove non-existent static route"),
                ParseWarningMatchers.hasText(containsString("10.4.0.0/16"))),
            allOf(
                hasComment("Cannot remove non-existent static route nexthop"),
                ParseWarningMatchers.hasText(containsString("nexthop gateway address 10.5.0.1")))));
  }

  @Test
  public void testStaticRouteWarning() throws IOException {
    String hostname = "static_route_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected static-route nexthop priority in range 1-8, but got '0'"),
                hasComment("Expected static-route nexthop priority in range 1-8, but got '9'"),
                allOf(
                    hasComment("Cannot set nexthop gateway to non-existent interface"),
                    hasText(containsString("eth0"))),
                hasComment(
                    "Static-route prefix 0.0.0.0/0 is not valid, use the 'default' keyword"
                        + " instead."),
                hasComment("Illegal value for static-route comment"))));
  }

  @Test
  public void testStaticRouteConversion() {
    String hostname = "static_route_convert";
    Configuration c = parseConfig(hostname);
    Vrf vrf = c.getDefaultVrf();

    assertThat(
        vrf,
        hasStaticRoutes(
            equalTo(
                ImmutableSet.of(
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.ZERO)
                        .setNextHop(NextHopInterface.of("eth0"))
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.1.0.0/16"))
                        .setNextHop(NextHopDiscard.instance())
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.2.0.0/16"))
                        .setNextHop(NextHopDiscard.instance())
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(8)
                        .setNetwork(Prefix.parse("10.3.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.4.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.3")))
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(1)
                        .setNetwork(Prefix.parse("10.4.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.4")))
                        .setRecursive(false)
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(3)
                        .setNetwork(Prefix.parse("10.5.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.5")))
                        .setRecursive(false)
                        .build()))));
  }

  @Test
  public void testBondInterfaceExtraction() {
    String hostname = "bond_interface";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(
        c.getInterfaces(),
        hasKeys("bond0", "bond1", "bond2", "bond3", "bond4", "bond1024", "eth0", "eth1"));
    assertThat(c.getBondingGroups(), hasKeys(containsInAnyOrder(0, 1, 2, 3, 4, 1024)));

    Interface bond0Iface = c.getInterfaces().get("bond0");
    assertThat(bond0Iface.getState(), equalTo(true));
    assertThat(bond0Iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.10/24")));
    BondingGroup bond0 = c.getBondingGroups().get(0);
    assertThat(bond0.getInterfaces(), containsInAnyOrder("eth0", "eth1"));
    assertThat(bond0.getMode(), equalTo(Mode.EIGHT_ZERO_TWO_THREE_AD));
    assertThat(bond0.getLacpRate(), equalTo(LacpRate.SLOW));
    assertThat(bond0.getXmitHashPolicy(), equalTo(XmitHashPolicy.LAYER2));

    BondingGroup bond1 = c.getBondingGroups().get(1);
    assertThat(bond1.getMode(), equalTo(Mode.EIGHT_ZERO_TWO_THREE_AD));
    assertThat(bond1.getLacpRate(), equalTo(LacpRate.FAST));
    assertThat(bond1.getXmitHashPolicy(), equalTo(XmitHashPolicy.LAYER3_4));

    BondingGroup bond2 = c.getBondingGroups().get(2);
    assertThat(bond2.getMode(), equalTo(Mode.ROUND_ROBIN));

    BondingGroup bond3 = c.getBondingGroups().get(3);
    assertThat(bond3.getMode(), equalTo(Mode.XOR));

    BondingGroup bond4 = c.getBondingGroups().get(4);
    assertThat(bond4.getMode(), equalTo(Mode.ACTIVE_BACKUP));

    // Defaults
    Interface bond1024Iface = c.getInterfaces().get("bond1024");
    assertThat(bond1024Iface.getState(), equalTo(true));
    BondingGroup bond1024 = c.getBondingGroups().get(1024);
    assertThat(bond1024.getInterfaces(), emptyIterable());
    assertNull(bond1024.getMode());
    assertThat(bond1024.getModeEffective(), equalTo(BondingGroup.DEFAULT_MODE));
    assertNull(bond1024.getLacpRate());
    assertNull(bond1024.getXmitHashPolicy());
  }

  @Test
  public void testBondInterfaceWarning() throws IOException {
    String hostname = "bond_interface_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment("Expected bonding group number in range 0-1024, but got '1025'"),
                hasComment("Expected bonding group number in range 0-1024, but got '1026'"),
                allOf(
                    hasComment(
                        "Illegal value for bonding group member interface name (must be eth"
                            + " interface)"),
                    hasText(containsString("interface lo"))),
                hasComment("Cannot configure non-existent bonding group, add it first."),
                allOf(
                    hasComment(
                        "Cannot add an interface with a configured address to a bonding group."),
                    hasText(containsString("interface eth1"))),
                allOf(
                    hasComment(
                        "Interface is a member of a bonding group and cannot be configured"
                            + " directly."),
                    hasText(containsString("state off"))),
                allOf(
                    hasComment(
                        "Interface is a member of a bonding group and cannot be configured"
                            + " directly."),
                    hasText(containsString("mtu"))),
                allOf(
                    hasComment(
                        "Interface is a member of a bonding group and cannot be configured"
                            + " directly."),
                    hasText(containsString("ipv4-address"))),
                allOf(
                    hasComment(
                        "Interface is a member of a bonding group and cannot be configured"
                            + " directly."),
                    hasText(containsString("link-speed"))),
                allOf(
                    hasComment(
                        "Interface is a member of a bonding group and cannot be configured"
                            + " directly."),
                    hasText(containsString("auto-negotiation"))),
                hasComment("Interface can only be added to one bonding group."))));

    // No bonding groups or bond interfaces should be created from invalid add/set lines
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(c.getInterfaces(), hasKeys("bond1000", "eth0", "eth1", "lo"));
    assertThat(c.getBondingGroups(), hasKeys(contains(1000)));
  }

  @Test
  public void testBondInterfaceConversion() {
    String hostname = "bond_interface_conversion";
    Configuration c = parseConfig(hostname);

    String bond0Name = "bond0";
    String bond1Name = "bond1";
    String eth0Name = "eth0";
    String eth1Name = "eth1";
    String eth2Name = "eth2";
    assertThat(c.getAllInterfaces(), hasKeys(bond0Name, bond1Name, eth0Name, eth1Name, eth2Name));
    {
      org.batfish.datamodel.Interface bond0 = c.getAllInterfaces().get(bond0Name);
      assertThat(bond0, isActive(true));
      assertThat(bond0, hasInterfaceType(AGGREGATED));
      assertThat(
          bond0,
          hasDependencies(
              containsInAnyOrder(
                  new Dependency(eth0Name, AGGREGATE),
                  new Dependency(eth1Name, AGGREGATE),
                  new Dependency(eth2Name, AGGREGATE))));
      assertThat(bond0, hasChannelGroupMembers(containsInAnyOrder(eth0Name, eth1Name, eth2Name)));
      assertThat(bond0, hasChannelGroup(nullValue()));
      assertThat(bond0, hasMtu(1234));
      assertThat(bond0, hasBandwidth(3E9));
    }
    {
      org.batfish.datamodel.Interface bond1 = c.getAllInterfaces().get(bond1Name);
      assertThat(bond1, isActive(false));
      assertThat(bond1, hasInterfaceType(AGGREGATED));
      assertThat(bond1, hasDependencies(emptyIterable()));
      assertThat(bond1, hasChannelGroupMembers(emptyIterable()));
      assertThat(bond1, hasChannelGroup(nullValue()));
      assertThat(bond1, hasBandwidth(0D));
    }
    {
      org.batfish.datamodel.Interface eth0 = c.getAllInterfaces().get(eth0Name);
      assertThat(eth0, isActive(true));
      assertThat(eth0, hasInterfaceType(PHYSICAL));
      assertThat(eth0, hasChannelGroup("bond0"));
      assertThat(eth0, hasMtu(1234));
      assertThat(eth0, hasBandwidth(1E9));
    }
    {
      org.batfish.datamodel.Interface eth1 = c.getAllInterfaces().get(eth1Name);
      assertThat(eth1, isActive(true));
      assertThat(eth1, hasInterfaceType(PHYSICAL));
      assertThat(eth1, hasChannelGroup("bond0"));
      assertThat(eth1, hasBandwidth(1E9));
    }
    {
      // Only referenced when adding to bonding group
      org.batfish.datamodel.Interface eth2 = c.getAllInterfaces().get(eth2Name);
      assertThat(eth2, isActive(true));
      assertThat(eth2, hasInterfaceType(PHYSICAL));
      assertThat(eth2, hasChannelGroup("bond0"));
      assertThat(eth2, hasBandwidth(1E9));
    }
  }

  @Test
  public void testBondInterfaceConversionWarn() throws IOException {
    String hostname = "bond_interface_conversion_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasRedFlagWarning(
            hostname,
            containsString(
                "Bonding group mode active-backup is not yet supported in Batfish. Deactivating"
                    + " interface bond1001.")));

    // Unsupported bonding group mode should be deactivated
    Configuration c = parseConfig(hostname);
    assertThat(c, hasInterface("bond1001", isActive(false)));
  }

  @Test
  public void testVlanInterfaceExtraction() {
    String hostname = "vlan_interface";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(
        c.getInterfaces(),
        hasKeys(
            "bond2",
            "bond3",
            "eth0",
            "eth1",
            "eth2",
            "eth10",
            "eth11",
            "eth12",
            "bond2.2",
            "bond3.3",
            "eth10.4092",
            "eth11.4093",
            "eth12.4094"));
    {
      Interface iface = c.getInterfaces().get("bond2.2");
      assertTrue(iface.getState());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("2.2.2.1/24")));
      assertThat(iface.getVlanId(), equalTo(2));
      assertThat(iface.getParentInterface(), equalTo("bond2"));
    }
    {
      Interface iface = c.getInterfaces().get("bond3.3");
      assertFalse(iface.getState());
      assertThat(iface.getVlanId(), equalTo(3));
      assertThat(iface.getParentInterface(), equalTo("bond3"));
    }
    {
      Interface iface = c.getInterfaces().get("eth10.4092");
      assertTrue(iface.getState());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.1/24")));
      assertThat(iface.getVlanId(), equalTo(4092));
      assertThat(iface.getParentInterface(), equalTo("eth10"));
    }
    {
      Interface iface = c.getInterfaces().get("eth11.4093");
      assertTrue(iface.getState());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("11.11.11.1/24")));
      assertThat(iface.getVlanId(), equalTo(4093));
      assertThat(iface.getParentInterface(), equalTo("eth11"));
    }
    {
      Interface iface = c.getInterfaces().get("eth12.4094");
      assertFalse(iface.getState());
      assertThat(iface.getVlanId(), equalTo(4094));
      assertThat(iface.getParentInterface(), equalTo("eth12"));
    }
  }

  @Test
  public void testVlanInterfaceConversion() {
    String hostname = "vlan_interface";
    Configuration c = parseConfig(hostname);
    assertThat(
        c.getAllInterfaces(),
        hasKeys(
            "bond2",
            "bond3",
            "eth0",
            "eth1",
            "eth2",
            "eth10",
            "eth11",
            "eth12",
            "bond2.2",
            "bond3.3",
            "eth10.4092",
            "eth11.4093",
            "eth12.4094"));
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("bond2.2");
      assertTrue(iface.getActive());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("2.2.2.1/24")));
      assertThat(iface.getEncapsulationVlan(), equalTo(2));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
      // Parent bond group contains two physical interfaces
      assertThat(iface.getBandwidth(), equalTo(2 * DEFAULT_ETH_SPEED));
    }
    {
      // bond3.3 is on, but bond3 is off
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("bond3.3");
      assertFalse(iface.getActive());
      assertThat(iface.getEncapsulationVlan(), equalTo(3));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
      // Parent bond group contains one physical interface
      assertThat(iface.getBandwidth(), equalTo(DEFAULT_ETH_SPEED));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth10.4092");
      assertTrue(iface.getActive());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.1/24")));
      assertThat(iface.getEncapsulationVlan(), equalTo(4092));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
      // Parent interface has configured speed 10Mbps
      assertThat(iface.getBandwidth(), equalTo(10e6));
    }
    {
      // eth11.4093 is on, but eth11 is off
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth11.4093");
      assertFalse(iface.getActive());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("11.11.11.1/24")));
      assertThat(iface.getEncapsulationVlan(), equalTo(4093));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
      // Parent interface has no configured speed
      assertThat(iface.getBandwidth(), equalTo(DEFAULT_ETH_SPEED));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth12.4094");
      assertFalse(iface.getActive());
      assertThat(iface.getEncapsulationVlan(), equalTo(4094));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    }
  }

  @Test
  public void testConvertWithCorrectPackage() throws IOException {
    // Create a checkpoint mangement config with the following gateways:
    // - g1 uses access policy p1, which contains a network n1
    // - g2 uses access policy p2, which contains a network n2
    // - g3 uses an access policy, but the nat rulebase is missing
    // - g4 uses no access policy
    // There is a 5th gateway that is not mentioned in the mgmt config
    // The appropriate IP spaces should be generated in the resulting VI configurations.
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            Uid.of("1"),
            new SimpleGateway(
                Ip.parse("1.0.0.1"),
                "g1",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("1")),
            Uid.of("13"),
            new SimpleGateway(
                Ip.parse("2.0.0.1"),
                "g2",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p2", null),
                Uid.of("13")),
            Uid.of("14"),
            new SimpleGateway(
                Ip.parse("3.0.0.1"),
                "g3",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p3", null),
                Uid.of("14")),
            Uid.of("15"),
            new SimpleGateway(
                Ip.parse("4.0.0.1"),
                "g4",
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                Uid.of("15")));
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            Uid.of("2"),
            new ManagementPackage(
                ImmutableList.of(),
                new NatRulebase(
                    ImmutableMap.of(
                        Uid.of("4"),
                        new Network(
                            "n1", NAT_SETTINGS_TEST_INSTANCE, Ip.ZERO, Ip.ZERO, Uid.of("n1uid"))),
                    ImmutableList.of(),
                    Uid.of("6")),
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    false,
                    true,
                    Uid.of("2"))),
            Uid.of("7"),
            new ManagementPackage(
                ImmutableList.of(),
                new NatRulebase(
                    ImmutableMap.of(
                        Uid.of("8"),
                        new Network(
                            "n2", NAT_SETTINGS_TEST_INSTANCE, Ip.MAX, Ip.MAX, Uid.of("n2uid"))),
                    ImmutableList.of(),
                    Uid.of("10")),
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p2",
                    false,
                    true,
                    Uid.of("11"))),
            Uid.of("16"),
            new ManagementPackage(
                ImmutableList.of(),
                null,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p3",
                    false,
                    true,
                    Uid.of("16"))));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, packages, ImmutableList.of());
    Map<String, Configuration> configs =
        parseTextConfigs(
            mgmt,
            "gw_package_selection_1",
            "gw_package_selection_2",
            "gw_package_selection_3",
            "gw_package_selection_4",
            "gw_package_selection_5");
    Configuration c1 = configs.get("gw_package_selection_1");
    Configuration c2 = configs.get("gw_package_selection_2");
    Configuration c3 = configs.get("gw_package_selection_3");
    Configuration c4 = configs.get("gw_package_selection_4");
    Configuration c5 = configs.get("gw_package_selection_5");
    assertThat(c1.getIpSpaces(), hasKey("n1"));
    assertThat(c2.getIpSpaces(), hasKey("n2"));
    assertThat(c3.getIpSpaces(), allOf(not(hasKey("n1")), not(hasKey("n2"))));
    assertThat(c4.getIpSpaces(), allOf(not(hasKey("n1")), not(hasKey("n2"))));
    assertThat(c5.getIpSpaces(), allOf(not(hasKey("n1")), not(hasKey("n2"))));
  }

  @Test
  public void testConvertDomainObjects() throws IOException {
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            Uid.of("1"),
            new SimpleGateway(
                Ip.parse("1.0.0.1"),
                "g1",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("1")));
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            Uid.of("2"),
            new ManagementPackage(
                ImmutableList.of(),
                null,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    false,
                    true,
                    Uid.of("2"))));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(
            gateways,
            packages,
            ImmutableList.of(
                new Network(
                    "networkObject",
                    NAT_SETTINGS_TEST_INSTANCE,
                    Ip.parse("10.11.12.0"),
                    Ip.parse("255.255.255.0"),
                    Uid.of("100"))));

    Map<String, Configuration> configs = parseTextConfigs(mgmt, "gw_package_selection_1");
    Configuration c1 = configs.get("gw_package_selection_1");
    // Network object from domain should make it to VI model
    assertThat(c1.getIpSpaces(), hasKey("networkObject"));
  }

  @Test
  public void testConvertGatewaysAndServers() throws IOException {
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            Uid.of("1"),
            new SimpleGateway(
                Ip.parse("1.0.0.1"),
                "g1",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("1")));
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            Uid.of("2"),
            new ManagementPackage(
                ImmutableList.of(),
                null,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    false,
                    true,
                    Uid.of("2"))));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, packages, ImmutableList.of());

    Map<String, Configuration> configs = parseTextConfigs(mgmt, "gw_package_selection_1");
    Configuration c1 = configs.get("gw_package_selection_1");
    // Simple-gateway object from show-gateways-and-servers should make it to VI model
    assertThat(c1.getIpSpaces(), hasKey("g1"));
  }

  @Test
  public void testAccessRulesConversion() throws IOException {
    Uid cpmiAnyUid = Uid.of("99999");
    CpmiAnyObject any = new CpmiAnyObject(cpmiAnyUid);
    Uid acceptUid = Uid.of("31");
    Uid dropUid = Uid.of("32");
    Uid policyTargetsUid = Uid.of("33");
    Uid net1Uid = Uid.of("11");
    Uid net2Uid = Uid.of("12");
    // Attached to domain, NOT in access layer object dict
    ImmutableList<TypedManagementObject> domainObjs =
        ImmutableList.<TypedManagementObject>builder()
            .add(
                new Network(
                    "networkEth2",
                    NAT_SETTINGS_TEST_INSTANCE,
                    Ip.parse("10.0.2.0"),
                    Ip.parse("255.255.255.0"),
                    net2Uid))
            .build();

    ImmutableMap<Uid, NamedManagementObject> objs =
        ImmutableMap.<Uid, NamedManagementObject>builder()
            .put(cpmiAnyUid, any)
            .put(
                net1Uid,
                new Network(
                    "networkEth1",
                    NAT_SETTINGS_TEST_INSTANCE,
                    Ip.parse("10.0.1.0"),
                    Ip.parse("255.255.255.0"),
                    net1Uid))
            .put(acceptUid, new RulebaseAction("Accept", acceptUid, "Accept"))
            .put(dropUid, new RulebaseAction("Drop", dropUid, "Drop"))
            .put(policyTargetsUid, new PolicyTargets(policyTargetsUid))
            .build();
    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(acceptUid)
                .setDestination(ImmutableList.of(net2Uid))
                .setSource(ImmutableList.of(net1Uid))
                .setInstallOn(ImmutableList.of(policyTargetsUid))
                .setUid(Uid.of("100"))
                .setName("acceptNet1ToNet2")
                .build(),
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(dropUid)
                .setInstallOn(ImmutableList.of(policyTargetsUid))
                .setUid(Uid.of("101"))
                .setName("dropAll")
                .build());

    AccessLayer accessLayer = new AccessLayer(objs, rulebase, Uid.of("uid-al"), "accessLayerFoo");
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            Uid.of("2"),
            new ManagementPackage(
                ImmutableList.of(accessLayer),
                null,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    true,
                    false,
                    Uid.of("2"))));
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            Uid.of("1"),
            new SimpleGateway(
                Ip.parse("10.0.0.1"),
                "access_rules",
                ImmutableList.of(
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth1", new InterfaceTopology(false), Ip.parse("10.0.1.1"), 24),
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth2", new InterfaceTopology(true), Ip.parse("10.0.2.1"), 24),
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth3", new InterfaceTopology(true), Ip.parse("10.0.3.1"), 24)),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("1")));

    CheckpointManagementConfiguration mgmt = toCheckpointMgmtConfig(gateways, packages, domainObjs);
    Map<String, Configuration> configs = parseTextConfigs(mgmt, "access_rules");
    Configuration c = configs.get("access_rules");

    // eth1 to eth2
    Flow permitted = createFlow("10.0.1.10", "10.0.2.10");
    // eth1 to eth3
    Flow denied = createFlow("10.0.1.10", "10.0.3.10");

    // Confirm access-layer, composite ACL, and interface ACLs have the same, correct behavior
    // Access-layer
    assertThat(c, hasIpAccessList(aclName(accessLayer), accepts(permitted, "eth1", c)));
    assertThat(c, hasIpAccessList(aclName(accessLayer), rejects(denied, "eth1", c)));
    // Composite ACL (same as access-layer since there is only one access-layer here)
    assertThat(c, hasIpAccessList(INTERFACE_ACL_NAME, accepts(permitted, "eth1", c)));
    assertThat(c, hasIpAccessList(INTERFACE_ACL_NAME, rejects(denied, "eth1", c)));
    // Iface ACLs
    assertThat(c, hasInterface("eth1", hasIncomingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth1", hasIncomingFilter(rejects(denied, "eth1", c))));
    assertThat(c, hasInterface("eth2", hasIncomingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth2", hasIncomingFilter(rejects(denied, "eth1", c))));
    assertThat(c, hasInterface("eth3", hasIncomingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth3", hasIncomingFilter(rejects(denied, "eth1", c))));
  }

  /**
   * Test that access rules are correctly associated with cluster members when attached *only* to
   * their cluster.
   */
  @Test
  public void testAccessRulesForClusterMember() throws IOException {
    Uid cpmiAnyUid = Uid.of("99999");
    CpmiAnyObject any = new CpmiAnyObject(cpmiAnyUid);
    Uid acceptUid = Uid.of("10");
    Uid policyTargetsUid = Uid.of("11");
    Uid packageUid = Uid.of("12");
    Uid clusterMemberUid = Uid.of("13");
    Uid clusterUid = Uid.of("14");

    ImmutableMap<Uid, NamedManagementObject> objs =
        ImmutableMap.<Uid, NamedManagementObject>builder()
            .put(cpmiAnyUid, any)
            .put(acceptUid, new RulebaseAction("Accept", acceptUid, "Accept"))
            .put(policyTargetsUid, new PolicyTargets(policyTargetsUid))
            .build();
    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(acceptUid)
                .setInstallOn(ImmutableList.of(policyTargetsUid))
                .setUid(Uid.of("100"))
                .setName("rule1")
                .build());

    AccessLayer accessLayer = new AccessLayer(objs, rulebase, Uid.of("uid-al"), "accessLayerFoo");
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            packageUid,
            new ManagementPackage(
                ImmutableList.of(accessLayer),
                null,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    true,
                    false,
                    packageUid)));

    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            clusterMemberUid,
            new CpmiClusterMember(
                Ip.parse("10.0.0.1"),
                "access_rules_cluster_member",
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                clusterMemberUid),
            clusterUid,
            new CpmiGatewayCluster(
                ImmutableList.of("access_rules_cluster_member"),
                Ip.parse("10.0.2.1"),
                "access_rules_cluster",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                clusterUid));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, packages, ImmutableList.of());
    Map<String, Configuration> configs = parseTextConfigs(mgmt, "access_rules");
    Configuration c = configs.get("access_rules");

    // Just check for existence, extraction/conversion is tested elsewhere
    assertThat(c.getIpAccessLists(), hasKey(aclName(accessLayer)));
  }

  @Test
  public void testObjectConversionWarnings() throws IOException {
    Uid unknownUid = Uid.of("10");
    Uid packageUid = Uid.of("12");
    String accessLayerName = "accessLayerFoo";
    String access_rules = "access_rules"; // Any config will do, just need to convert mgmt objs

    ImmutableMap<Uid, NamedManagementObject> objs =
        ImmutableMap.<Uid, NamedManagementObject>builder()
            .put(
                unknownUid,
                new UnknownTypedManagementObject("unknownObjectType", unknownUid, "UnknownType"))
            .build();

    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            packageUid,
            new ManagementPackage(
                ImmutableList.of(
                    new AccessLayer(objs, ImmutableList.of(), Uid.of("13"), accessLayerName)),
                null,
                new Package(
                    new Domain("d", Uid.of("14")),
                    AllInstallationTargets.instance(),
                    "p1",
                    true,
                    false,
                    packageUid)));
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            Uid.of("1"),
            new SimpleGateway(
                Ip.parse("10.0.0.1"),
                access_rules,
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("1")));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, packages, ImmutableList.of());
    Batfish batfish = getBatfishForConfigurationNames(mgmt, access_rules);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasRedFlagWarning(
            access_rules,
            containsString(
                "Batfish does not handle converting objects of type UnknownType. These objects will"
                    + " be ignored.")));
  }

  @Test
  public void testNatConversion() throws IOException {
    /*
    NAT rules:
    1. Manual static: Destination 5.5.5.5 is translated to 6.6.6.6
    2. Manual hide: Source 7.7.7.7 is translated to 8.8.8.8
    3. Automatic static 1: Source 10.10.10.10 translates to NAT IP 20.20.20.20
                         (implies dest 20.20.20.20 translates to 10.10.10.10)
    4. Automatic static 2: Source 20.20.20.21 translates to NAT IP 10.10.10.11
                         (implies dest 10.10.10.11 translates to 20.20.20.21)
    5. Automatic hide: Network 10.10.10.0/24 is hidden behind IP 30.30.30.30
    6. Manual (low priority) hide: Source 10.10.0.0/16 is translated to 9.9.9.9
        note: this source network overlaps with a preceding automatic rule
     */
    String hostname = "nat_rules";
    Ip manualStaticOriginalDst = Ip.parse("5.5.5.5");
    Ip manualStaticTranslatedDst = Ip.parse("6.6.6.6");
    Ip manualHideOriginalSrc = Ip.parse("7.7.7.7");
    Ip manualHideTranslatedSrc = Ip.parse("8.8.8.8");
    Ip autoStatic1HostIp = Ip.parse("10.10.10.10");
    Ip autoStatic1NatIp = Ip.parse("20.20.20.20");
    Ip autoStatic2HostIp = Ip.parse("20.20.20.21");
    Ip autoStatic2NatIp = Ip.parse("10.10.10.11");
    Prefix autoHideNetwork = Prefix.parse("10.10.10.0/24");
    Ip autoHideNatIp = Ip.parse("30.30.30.30");
    Prefix manualLowPriorityHideOriginalSrc = Prefix.parse("10.10.0.0/16");
    Ip manualLowPriorityHideTranslatedSrc = Ip.parse("9.9.9.9");

    AtomicInteger uidGenerator = new AtomicInteger();
    NatSettings emptyNatSettings = new NatSettings(false, null, null, null, null);

    // Create manual rules and put them in rulebase
    Uid cpmiAnyUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    CpmiAnyObject any = new CpmiAnyObject(cpmiAnyUid);
    Uid originalUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Original original = new Original(cpmiAnyUid);
    Uid policyTargetsUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    PolicyTargets policyTargets = new PolicyTargets(policyTargetsUid);
    Uid manualStaticOriginalHostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host manualStaticOriginalHost =
        new Host(
            manualStaticOriginalDst,
            emptyNatSettings,
            "manualStaticOriginalHost",
            manualStaticOriginalHostUid);
    Uid manualStaticTranslatedHostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host manualStaticTranslatedHost =
        new Host(
            manualStaticTranslatedDst,
            emptyNatSettings,
            "manualStaticTranslatedHost",
            manualStaticTranslatedHostUid);
    Uid manualHideOriginalHostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host manualHideOriginalHost =
        new Host(
            manualHideOriginalSrc,
            emptyNatSettings,
            "manualHideOriginalHost",
            manualHideOriginalHostUid);
    Uid manualHideTranslatedHostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host manualHideTranslatedHost =
        new Host(
            manualHideTranslatedSrc,
            emptyNatSettings,
            "manualHideTranslatedHost",
            manualHideTranslatedHostUid);
    Uid manualLowPriorityHideOriginalNetworkUid =
        Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Network manualLowPriorityHideOriginalNetwork =
        new Network(
            "manualLowPriorityHideOriginalNetwork",
            emptyNatSettings,
            manualLowPriorityHideOriginalSrc.getStartIp(),
            manualLowPriorityHideOriginalSrc.getPrefixWildcard().inverted(),
            manualLowPriorityHideOriginalNetworkUid);
    Uid manualLowPriorityHideTranslatedHostUid =
        Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host manualLowPriorityHideTranslatedHost =
        new Host(
            manualLowPriorityHideTranslatedSrc,
            emptyNatSettings,
            "manualLowPriorityHideTranslatedHost",
            manualLowPriorityHideTranslatedHostUid);
    Uid manualStaticRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule manualStaticRule =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.STATIC,
            manualStaticOriginalHostUid,
            cpmiAnyUid,
            cpmiAnyUid,
            1,
            manualStaticTranslatedHostUid,
            originalUid,
            originalUid,
            manualStaticRuleUid);
    Uid manualHideRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule manualHideRule =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.HIDE,
            cpmiAnyUid,
            cpmiAnyUid,
            manualHideOriginalHostUid,
            2,
            originalUid,
            originalUid,
            manualHideTranslatedHostUid,
            manualHideRuleUid);
    Uid manualLowPriorityHideRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule manualLowPriorityHideRule =
        new NatRule(
            false,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.HIDE,
            cpmiAnyUid,
            cpmiAnyUid,
            manualLowPriorityHideOriginalNetworkUid,
            6,
            originalUid,
            originalUid,
            manualLowPriorityHideTranslatedHostUid,
            manualLowPriorityHideRuleUid);

    // Create automatic rules
    NatSettings autoStatic1NatSettings =
        new NatSettings(true, null, "All", autoStatic1NatIp, NatMethod.STATIC);
    NatSettings autoStatic2NatSettings =
        new NatSettings(true, null, "All", autoStatic2NatIp, NatMethod.STATIC);
    NatSettings autoHideNatSettings =
        new NatSettings(true, new NatHideBehindIp(autoHideNatIp), "All", null, NatMethod.HIDE);
    Uid autoStatic1HostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host autoStatic1Host =
        new Host(autoStatic1HostIp, autoStatic1NatSettings, "autoStatic1Host", autoStatic1HostUid);
    Uid autoStatic2HostUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Host autoStatic2Host =
        new Host(autoStatic2HostIp, autoStatic2NatSettings, "autoStatic2Host", autoStatic2HostUid);
    Uid autoHideNetworkUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Network autoHideNetworkObj =
        new Network(
            "autoHideNetwork",
            autoHideNatSettings,
            autoHideNetwork.getStartIp(),
            autoHideNetwork.getPrefixWildcard().inverted(),
            autoHideNetworkUid);
    Uid autoStatic1RuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule autoStatic1Rule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.STATIC,
            cpmiAnyUid,
            cpmiAnyUid,
            autoStatic1HostUid,
            3,
            originalUid,
            originalUid,
            // TODO Translated source should really be populated but doesn't matter for current
            // automatic rule conversion
            originalUid,
            autoStatic1RuleUid);
    Uid autoStatic2RuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule autoStatic2Rule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.STATIC,
            cpmiAnyUid,
            cpmiAnyUid,
            autoStatic2HostUid,
            4,
            originalUid,
            originalUid,
            // TODO Translated source should really be populated but doesn't matter for current
            // automatic rule conversion
            originalUid,
            autoStatic2RuleUid);
    Uid autoHideRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule autoHideRule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.HIDE,
            cpmiAnyUid,
            cpmiAnyUid,
            autoHideNetworkUid,
            5,
            originalUid,
            originalUid,
            // TODO Translated source should really be populated but doesn't matter for current
            // automatic rule conversion
            originalUid,
            autoHideRuleUid);

    ImmutableMap<Uid, TypedManagementObject> natObjs =
        ImmutableMap.<Uid, TypedManagementObject>builder()
            .put(cpmiAnyUid, any)
            .put(originalUid, original)
            .put(policyTargetsUid, policyTargets)
            .put(manualStaticOriginalHostUid, manualStaticOriginalHost)
            .put(manualStaticTranslatedHostUid, manualStaticTranslatedHost)
            .put(manualHideOriginalHostUid, manualHideOriginalHost)
            .put(manualHideTranslatedHostUid, manualHideTranslatedHost)
            .put(manualLowPriorityHideOriginalNetworkUid, manualLowPriorityHideOriginalNetwork)
            .put(manualLowPriorityHideTranslatedHostUid, manualLowPriorityHideTranslatedHost)
            .put(autoStatic1HostUid, autoStatic1Host)
            .put(autoStatic2HostUid, autoStatic2Host)
            .put(autoHideNetworkUid, autoHideNetworkObj)
            .build();
    Uid rulebaseUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRulebase rulebase =
        new NatRulebase(
            natObjs,
            ImmutableList.of(
                manualStaticRule,
                manualHideRule,
                autoStatic1Rule,
                autoStatic2Rule,
                autoHideRule,
                manualLowPriorityHideRule),
            rulebaseUid);

    // Create simple access layer to permit all traffic
    Uid permitUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    RulebaseAction permit = new RulebaseAction("Accept", permitUid, "");
    Uid accessRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Uid accessListUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    AccessLayer accessLayer =
        new AccessLayer(
            ImmutableMap.of(cpmiAnyUid, any, permitUid, permit, policyTargetsUid, policyTargets),
            ImmutableList.of(
                AccessRule.testBuilder(cpmiAnyUid)
                    .setUid(accessRuleUid)
                    .setAction(permitUid)
                    .setInstallOn(ImmutableList.of(policyTargetsUid))
                    .build()),
            accessListUid,
            "accessLayer");

    // TODO May need more complete list of objects later depending on implementation changes
    ImmutableList<TypedManagementObject> domainObjs =
        ImmutableList.of(any, autoStatic1Host, autoStatic2Host, autoHideNetworkObj, permit);

    Uid packageUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            packageUid,
            new ManagementPackage(
                ImmutableList.of(accessLayer),
                rulebase,
                new Package(
                    new Domain("d", Uid.of("0")),
                    AllInstallationTargets.instance(),
                    "p1",
                    false,
                    true,
                    packageUid)));
    Uid gwUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            gwUid,
            new SimpleGateway(
                Ip.parse("10.0.0.1"),
                hostname,
                ImmutableList.of(
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth0", new InterfaceTopology(false), Ip.parse("10.0.0.1"), 24),
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth1", new InterfaceTopology(true), Ip.parse("10.0.1.1"), 24)),
                new GatewayOrServerPolicy("p1", null),
                gwUid));

    CheckpointManagementConfiguration mgmt = toCheckpointMgmtConfig(gateways, packages, domainObjs);
    IBatfish bf = getBatfishForConfigurationNames(mgmt, hostname);
    Map<String, Configuration> configs = bf.loadConfigurations(bf.getSnapshot());
    Configuration c = configs.get(hostname);
    bf.computeDataPlane(bf.getSnapshot());
    Map<String, Fib> fibs = bf.loadDataPlane(bf.getSnapshot()).getFibs().get(hostname);

    org.batfish.datamodel.Interface eth0 = c.getAllInterfaces().get("eth0");
    org.batfish.datamodel.Interface eth1 = c.getAllInterfaces().get("eth1");
    PacketPolicy eth0Policy = c.getPacketPolicies().get(eth0.getPacketPolicyName());
    PacketPolicy eth1Policy = c.getPacketPolicies().get(eth1.getPacketPolicyName());

    // Function to test that a given flow is transformed to the expected result flow
    Action fibLookup = new FibLookup(IngressInterfaceVrf.instance());
    BiFunction<Flow, Flow, Void> testFunction =
        (testFlow, expectedFlow) -> {
          PacketPolicyEvaluator.PacketPolicyResult eth0PolicyResult =
              PacketPolicyEvaluator.evaluate(
                  testFlow,
                  eth0.getName(),
                  eth0.getVrfName(),
                  eth0Policy,
                  c.getIpAccessLists(),
                  c.getIpSpaces(),
                  fibs);
          PacketPolicyEvaluator.PacketPolicyResult eth1PolicyResult =
              PacketPolicyEvaluator.evaluate(
                  testFlow,
                  eth1.getName(),
                  eth1.getVrfName(),
                  eth1Policy,
                  c.getIpAccessLists(),
                  c.getIpSpaces(),
                  fibs);
          assertThat(eth0PolicyResult.getFinalFlow(), equalTo(expectedFlow));
          assertThat(eth1PolicyResult.getFinalFlow(), equalTo(expectedFlow));
          assertThat(eth0PolicyResult.getAction(), equalTo(fibLookup));
          assertThat(eth1PolicyResult.getAction(), equalTo(fibLookup));
          return null;
        };

    // Set up test flows mapped to their expected transformed versions.
    {
      // Traffic may match no rules
      Flow flow =
          Flow.builder()
              .setSrcIp(Ip.parse("1.1.1.1"))
              .setDstIp(Ip.parse("1.1.1.2"))
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow);
    }
    {
      // Traffic matching manual static rule cannot match further manual rules
      Flow flow =
          Flow.builder()
              .setSrcIp(manualHideOriginalSrc) // would get translated by manual hide rule...
              .setDstIp(manualStaticOriginalDst) // ...but matches manual static rule first
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setDstIp(manualStaticTranslatedDst).build());
    }
    {
      // Traffic matching manual static rule cannot match automatic rules
      Flow flow =
          Flow.builder()
              .setSrcIp(autoStatic1HostIp) // would get translated by auto static rule...
              .setDstIp(manualStaticOriginalDst) // ...but matches manual static rule first
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setDstIp(manualStaticTranslatedDst).build());
    }
    {
      // Traffic matching manual hide rule cannot match automatic rules
      Flow flow =
          Flow.builder()
              .setSrcIp(manualHideOriginalSrc) // Matches manual hide rule, so...
              .setDstIp(autoStatic1NatIp) // ...ineligible for auto static 1 rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setSrcIp(manualHideTranslatedSrc).build());
    }
    {
      // Traffic can match an auto static dest rule and no source rule
      Flow flow =
          Flow.builder()
              .setSrcIp(
                  manualLowPriorityHideOriginalSrc
                      .getFirstHostIp()) // Source Ip would match the low-priority manual rule, but
              // shouldn't because processing stops after matching any
              // auto rules
              .setDstIp(autoStatic1NatIp) // Matches dst translation for auto static 1 rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setDstIp(autoStatic1HostIp).build());
    }
    {
      // Traffic can match an auto static source rule and no dest rule
      Flow flow =
          Flow.builder()
              .setSrcIp(autoStatic1HostIp) // Matches source translation for auto static 1 rule
              .setDstIp(Ip.parse("1.1.1.1"))
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setSrcIp(autoStatic1NatIp).build());
    }
    {
      // Traffic can match an auto hide rule and no dest rule
      Flow flow =
          Flow.builder()
              .setSrcIp(autoHideNetwork.getStartIp()) // Matches auto hide rule
              .setDstIp(Ip.parse("1.1.1.1"))
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow.toBuilder().setSrcIp(autoHideNatIp).build());
    }
    {
      // Traffic can match both an auto static dest rule and an auto static source rule
      Flow flow =
          Flow.builder()
              .setSrcIp(autoStatic2HostIp) // Matches src translation for auto static 2 rule
              .setDstIp(autoStatic1NatIp) // Matches dst translation for auto static 1 rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(
          flow, flow.toBuilder().setSrcIp(autoStatic2NatIp).setDstIp(autoStatic1HostIp).build());
    }
    {
      // Traffic will still match both auto static dest rule and auto static source rule even if it
      // would match intranet traffic for an auto hide rule
      // (note that the hide rule's network contains both autoStatic1HostIp and autoStatic2NatIp,
      // but auto static source rules take precedence over hide rules)
      Flow flow =
          Flow.builder()
              .setSrcIp(autoStatic1HostIp) // Matches src translation for auto static 1 rule
              .setDstIp(autoStatic2NatIp) // Matches dst translation for auto static 2 rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(
          flow, flow.toBuilder().setSrcIp(autoStatic1NatIp).setDstIp(autoStatic2HostIp).build());
    }
    {
      // Traffic can match both an auto static dest rule and an auto hide source rule
      // Also note: this rule partly overlaps with the low-priority manual rule exercised below
      Flow flow =
          Flow.builder()
              .setSrcIp(autoHideNetwork.getStartIp()) // Matches src translation for auto hide rule
              .setDstIp(autoStatic1NatIp) // Matches dst translation for auto static 1 rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(
          flow, flow.toBuilder().setSrcIp(autoHideNatIp).setDstIp(autoStatic1HostIp).build());
    }
    {
      // Intranet traffic for an auto hide rule (that doesn't match any auto static source rule) is
      // not translated, even if it would match an auto static dst rule
      // (note that autoHideNetwork contains autoStatic2NatIp)
      // TODO Test that intranet traffic can't undergo dest translation due to another rule
      Flow flow =
          Flow.builder()
              .setSrcIp(autoHideNetwork.getStartIp()) // Matches auto hide rule, so...
              .setDstIp(autoStatic2NatIp) // ...ineligible for auto static 2 dst rule
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow);
    }
    {
      // Traffic should match low priority NAT rules last
      Flow flow =
          Flow.builder()
              .setSrcIp(Ip.parse("10.10.11.11"))
              .setDstIp(Ip.parse("1.1.1.2"))
              .setIngressNode(hostname)
              .build();
      testFunction.apply(
          flow,
          flow.toBuilder().setSrcIp(manualLowPriorityHideTranslatedHost.getIpv4Address()).build());
    }

    // Check session properties
    assertThat(
        eth0.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                POST_NAT_FIB_LOOKUP, ImmutableList.of("eth0"), null, null)));
    assertThat(
        eth1.getFirewallSessionInterfaceInfo(),
        equalTo(
            new FirewallSessionInterfaceInfo(
                POST_NAT_FIB_LOOKUP, ImmutableList.of("eth1"), null, null)));
  }

  @Test
  public void testAutomaticHideBehindGatewayNatConversion() throws IOException {
    String hostname = "nat_rules";
    AtomicInteger uidGenerator = new AtomicInteger();

    // Define two networks, one of which applies hide NAT
    Prefix natted = Prefix.parse("10.0.0.0/16");
    Prefix unnatted = Prefix.parse("20.0.0.0/16");
    Uid natNetworkUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Uid noNatNetworkUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatSettings natSettings =
        new NatSettings(true, NatHideBehindGateway.INSTANCE, "All", null, HIDE);
    Network natNetwork =
        new Network(
            "network1",
            natSettings,
            natted.getStartIp(),
            natted.getPrefixWildcard().inverted(),
            natNetworkUid);
    Network noNatNetwork =
        new Network(
            "network2",
            new NatSettings(false, null, null, null, null),
            unnatted.getStartIp(),
            unnatted.getPrefixWildcard().inverted(),
            noNatNetworkUid);

    Uid anyUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    CpmiAnyObject any = new CpmiAnyObject(anyUid);
    Uid originalUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Original original = new Original(anyUid);
    Uid policyTargetsUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    PolicyTargets policyTargets = new PolicyTargets(policyTargetsUid);
    Uid autoHideRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRule autoHideRule =
        new NatRule(
            true,
            "",
            true,
            ImmutableList.of(policyTargetsUid),
            NatMethod.HIDE,
            anyUid,
            anyUid,
            natNetworkUid,
            1,
            originalUid,
            originalUid,
            // TODO Translated source should really be populated but doesn't matter for current
            // automatic rule conversion
            originalUid,
            autoHideRuleUid);
    ImmutableMap<Uid, TypedManagementObject> natObjs =
        ImmutableMap.<Uid, TypedManagementObject>builder()
            .put(anyUid, any)
            .put(originalUid, original)
            .put(policyTargetsUid, policyTargets)
            .put(natNetworkUid, natNetwork)
            .build();
    Uid natUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    NatRulebase rulebase = new NatRulebase(natObjs, ImmutableList.of(autoHideRule), natUid);

    // Create access layer to permit traffic to the NATted network and the other network.
    Uid alUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Uid accessRuleUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Uid permitUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    RulebaseAction permit = new RulebaseAction("Accept", permitUid, "");
    AccessLayer accessLayer =
        new AccessLayer(
            ImmutableMap.<Uid, NamedManagementObject>builder()
                .put(anyUid, any)
                .put(natNetworkUid, natNetwork)
                .put(noNatNetworkUid, noNatNetwork)
                .put(permitUid, permit)
                .put(policyTargetsUid, policyTargets)
                .build(),
            ImmutableList.of(
                AccessRule.testBuilder(anyUid)
                    .setUid(accessRuleUid)
                    .setAction(permitUid)
                    .setSource(ImmutableList.of(natNetworkUid, noNatNetworkUid))
                    .setInstallOn(ImmutableList.of(policyTargetsUid))
                    .build()),
            alUid,
            "accessLayer");

    Uid packageUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Uid domainUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            packageUid,
            new ManagementPackage(
                ImmutableList.of(accessLayer),
                rulebase,
                new Package(
                    new Domain("d", domainUid),
                    AllInstallationTargets.instance(),
                    "p1",
                    false,
                    true,
                    packageUid)));

    Uid gwUid = Uid.of(String.valueOf(uidGenerator.getAndIncrement()));
    Ip eth0Ip = Ip.parse("10.0.0.1");
    Ip eth1Ip = Ip.parse("10.0.1.1");
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            gwUid,
            new SimpleGateway(
                Ip.parse("10.0.0.1"),
                hostname,
                ImmutableList.of(
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth0", new InterfaceTopology(false), eth0Ip, 24),
                    new org.batfish.vendor.check_point_management.Interface(
                        "eth1", new InterfaceTopology(true), eth1Ip, 24)),
                new GatewayOrServerPolicy("p1", null),
                gwUid));

    // TODO This objects list may need to be more complete later depending on implementation changes
    ImmutableList<TypedManagementObject> domainObjs =
        ImmutableList.of(natNetwork, noNatNetwork, any, permit);
    CheckpointManagementConfiguration mgmt = toCheckpointMgmtConfig(gateways, packages, domainObjs);
    IBatfish bf = getBatfishForConfigurationNames(mgmt, hostname);
    Map<String, Configuration> configs = bf.loadConfigurations(bf.getSnapshot());
    Configuration c = configs.get(hostname);
    bf.computeDataPlane(bf.getSnapshot());
    Map<String, Fib> fibs = bf.loadDataPlane(bf.getSnapshot()).getFibs().get(hostname);

    // Test interfaces. This test uses two interfaces for two reasons:
    // 1. Some interface must own the gateway IP for the management data to match up, and we want to
    //    test that the transformation is a function of the egress interface IP, not the gateway IP.
    // 2. We want to test that the transformation is the same on internal and external interfaces.
    org.batfish.datamodel.Interface eth0 = c.getAllInterfaces().get("eth0");
    org.batfish.datamodel.Interface eth1 = c.getAllInterfaces().get("eth1");
    PacketPolicy eth0Policy = c.getPacketPolicies().get(eth0.getPacketPolicyName());
    PacketPolicy eth1Policy = c.getPacketPolicies().get(eth1.getPacketPolicyName());

    Ip ip1InNatNetwork = Ip.parse("10.0.10.10");
    Ip ip2InNatNetwork = Ip.parse("10.0.10.11");
    Ip ipInNoNatNetwork = Ip.parse("20.0.0.1");
    Ip otherIp = Ip.parse("30.0.0.0"); // does not match either permitted network
    // Function to test that a given flow is transformed to the expected result flow
    TriFunction<Flow, Flow, Action, Void> testFunction =
        (testFlow, expectedFlow, expectedAction) -> {
          PacketPolicyEvaluator.PacketPolicyResult eth0PolicyResult =
              PacketPolicyEvaluator.evaluate(
                  testFlow,
                  eth0.getName(),
                  eth0.getVrfName(),
                  eth0Policy,
                  c.getIpAccessLists(),
                  c.getIpSpaces(),
                  fibs);
          PacketPolicyEvaluator.PacketPolicyResult eth1PolicyResult =
              PacketPolicyEvaluator.evaluate(
                  testFlow,
                  eth1.getName(),
                  eth1.getVrfName(),
                  eth1Policy,
                  c.getIpAccessLists(),
                  c.getIpSpaces(),
                  fibs);
          assertThat(eth0PolicyResult.getFinalFlow(), equalTo(expectedFlow));
          assertThat(eth1PolicyResult.getFinalFlow(), equalTo(expectedFlow));
          assertThat(eth0PolicyResult.getAction(), equalTo(expectedAction));
          assertThat(eth1PolicyResult.getAction(), equalTo(expectedAction));
          return null;
        };

    Action fibLookup = new FibLookup(IngressInterfaceVrf.instance());
    {
      // A flow destined to eth0's IP should not be transformed, regardless of ingress interface
      Flow flow =
          Flow.builder()
              .setSrcIp(ip1InNatNetwork) // would get translated by hide rule...
              .setDstIp(eth0Ip) // ...but is destined for firewall
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow, fibLookup);
    }
    {
      // A flow destined to eth1's IP should not be transformed, regardless of ingress interface
      Flow flow =
          Flow.builder()
              .setSrcIp(ip1InNatNetwork) // would get translated by hide rule...
              .setDstIp(eth1Ip) // ...but is destined for firewall
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow, fibLookup);
    }
    {
      // A flow destined to another IP in the NATted network should not be transformed because it is
      // intranet traffic
      Flow flow =
          Flow.builder()
              .setSrcIp(ip1InNatNetwork) // would get translated by hide rule...
              .setDstIp(ip2InNatNetwork) // ...but is intranet traffic
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow, fibLookup);
    }
    {
      // A flow from an IP in the NATted network destined to an IP outside it should be transformed
      Flow flow =
          Flow.builder()
              .setSrcIp(ip1InNatNetwork)
              .setDstIp(otherIp)
              .setIngressNode(hostname)
              .build();
      testFunction.apply(
          flow, flow.toBuilder().setSrcIp(HIDE_BEHIND_GATEWAY_IP).build(), fibLookup);
    }
    {
      // A flow from an IP in the non-NATted network should not be transformed
      Flow flow =
          Flow.builder()
              .setSrcIp(ipInNoNatNetwork)
              .setDstIp(otherIp)
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow, fibLookup);
    }
    {
      // A flow from an IP not in either network should be dropped
      Flow flow =
          Flow.builder()
              .setSrcIp(otherIp)
              .setDstIp(eth0Ip) // dst IP doesn't matter
              .setIngressNode(hostname)
              .build();
      testFunction.apply(flow, flow, Drop.instance());
    }

    // Both interfaces should have an outgoing transformation to apply egress iface IP
    assertThat(
        eth0.getOutgoingTransformation(),
        equalTo(when(matchSrc(HIDE_BEHIND_GATEWAY_IP)).apply(assignSourceIp(eth0Ip)).build()));
    assertThat(
        eth1.getOutgoingTransformation(),
        equalTo(when(matchSrc(HIDE_BEHIND_GATEWAY_IP)).apply(assignSourceIp(eth1Ip)).build()));
  }

  @Test
  public void testParsingMgmtAndGateway() throws IOException {
    String snapshotName = "parsetest";
    List<String> checkpointMgmtFiles =
        ImmutableList.of(
            "cp_mgmt/Parent/show-address-ranges.json",
            "cp_mgmt/Parent/show-dynamic-objects.json",
            "cp_mgmt/Parent/show-gateways-and-servers.json",
            "cp_mgmt/Parent/show-groups.json",
            "cp_mgmt/Parent/show-hosts.json",
            "cp_mgmt/Parent/show-multicast-address-ranges.json",
            "cp_mgmt/Parent/show-networks.json",
            "cp_mgmt/Parent/show-packages.json",
            "cp_mgmt/Parent/show-security-zones.json",
            "cp_mgmt/Parent/show-service-groups.json",
            "cp_mgmt/Parent/show-services-dce-rpc.json",
            "cp_mgmt/Parent/show-services-icmp6.json",
            "cp_mgmt/Parent/show-services-icmp.json",
            "cp_mgmt/Parent/show-services-other.json",
            "cp_mgmt/Parent/show-services-rpc.json",
            "cp_mgmt/Parent/show-services-tcp.json",
            "cp_mgmt/Parent/show-services-udp.json",
            "cp_mgmt/Parent/show-simple-gateways.json",
            "cp_mgmt/Parent/show-vpn-communities-meshed.json",
            "cp_mgmt/Parent/Standard/show-access-rulebase.json",
            "cp_mgmt/Parent/Standard/show-nat-rulebase.json",
            "cp_mgmt/Parent/Standard/show-package.json",
            "cp_mgmt/Parent/test1/show-access-rulebase.json",
            "cp_mgmt/Parent/test1/show-nat-rulebase.json",
            "cp_mgmt/Parent/test1/show-package.json");
    Batfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(
                    SNAPSHOTS_PREFIX + snapshotName,
                    ImmutableList.of("cp_gw1/show_configuration.txt"))
                .setCheckpointMgmtFiles(SNAPSHOTS_PREFIX + snapshotName, checkpointMgmtFiles)
                .build(),
            _folder);
    batfish.getSettings().setDisableUnrecognized(false);
    Map<String, Configuration> configurations = batfish.loadConfigurations(batfish.getSnapshot());
    assertThat(configurations, hasKey("cp_gw1"));
    assertThat(configurations.get("cp_gw1").getIpSpaces(), hasKey("somehost"));
  }

  @Test
  public void testClusterVirtualIpAssignment() throws IOException {
    String hostname = "cluster_member";
    Uid memberUid = Uid.of("1");
    Uid clusterUid = Uid.of("2");
    String memberName = "cluster_member";
    String receivingIfaceName = "eth0";
    int prefixLength = 24;
    Ip syncIp = Ip.parse("192.0.2.1");
    Ip clusterIp = Ip.parse("10.0.0.1");
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            memberUid,
            new CpmiClusterMember(
                Ip.parse("1.0.0.1"),
                memberName,
                ImmutableList.of(
                    new org.batfish.vendor.check_point_management.Interface(
                        receivingIfaceName, new InterfaceTopology(false), syncIp, prefixLength)),
                new GatewayOrServerPolicy(null, null),
                memberUid),
            clusterUid,
            new CpmiGatewayCluster(
                // member should have second priority
                ImmutableList.of("master", memberName),
                Ip.parse("1.0.0.2"),
                "cluster",
                ImmutableList.of(
                    new org.batfish.vendor.check_point_management.Interface(
                        receivingIfaceName, new InterfaceTopology(false), clusterIp, prefixLength)),
                new GatewayOrServerPolicy(null, null),
                memberUid));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, ImmutableMap.of(), ImmutableList.of());

    Map<String, Configuration> configs = parseTextConfigs(mgmt, hostname);
    Configuration c1 = configs.get(hostname);

    assertThat(c1.getAllInterfaces(), hasKeys(receivingIfaceName, SYNC_INTERFACE_NAME, "eth1"));

    VrrpGroup vrrpGroup = c1.getAllInterfaces().get(SYNC_INTERFACE_NAME).getVrrpGroups().get(0);

    assertNotNull(vrrpGroup);
    assertThat(
        vrrpGroup.getVirtualAddresses(),
        equalTo(ImmutableMap.of(receivingIfaceName, ImmutableSet.of(clusterIp))));
    assertThat(
        vrrpGroup.getSourceAddress(),
        equalTo(ConcreteInterfaceAddress.create(syncIp, prefixLength)));
    assertTrue(vrrpGroup.getPreempt());
    assertThat(vrrpGroup.getPriority(), equalTo(VrrpGroup.MAX_PRIORITY - 1));
  }

  @Test
  public void testClusterGeneratedEdges() throws IOException {
    // Test generation of L1 edges between Sync interfaces
    // There should be edges only between cluster members 1 and 2.
    // - member 3 is missing a sync interface
    // - member 4 is missing a gateway configuration

    String hostname1 = "cluster_member";
    String hostname2 = "cluster_member2";
    String hostname3 = "cluster_member3";
    Uid member1Uid = Uid.of("1");
    Uid member2Uid = Uid.of("2");
    Uid member3Uid = Uid.of("3");
    Uid member4Uid = Uid.of("4");
    Uid clusterUid = Uid.of("5");
    String memberName1 = "cm1";
    String memberName2 = "cm2";
    String memberName3 = "cm3";
    String memberName4 = "cm4";
    ImmutableMap<Uid, GatewayOrServer> gateways =
        ImmutableMap.of(
            member1Uid,
            new CpmiClusterMember(
                Ip.parse("1.0.0.1"),
                memberName1,
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                member1Uid),
            member2Uid,
            new CpmiClusterMember(
                Ip.parse("1.0.0.2"),
                memberName2,
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                member2Uid),
            member3Uid,
            new CpmiClusterMember(
                Ip.parse("1.0.0.3"),
                memberName3,
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                member3Uid),
            member4Uid,
            new CpmiClusterMember(
                Ip.parse("1.0.0.4"),
                memberName4,
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                member4Uid),
            clusterUid,
            new CpmiGatewayCluster(
                ImmutableList.of(memberName1, memberName2, memberName3, memberName4),
                Ip.parse("1.0.0.5"),
                "cluster",
                ImmutableList.of(),
                new GatewayOrServerPolicy(null, null),
                clusterUid));

    CheckpointManagementConfiguration mgmt =
        toCheckpointMgmtConfig(gateways, ImmutableMap.of(), ImmutableList.of());

    Batfish batfish = getBatfishForConfigurationNames(mgmt, hostname1, hostname2, hostname3);
    Map<String, Warnings> warnings =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot()).getWarnings();
    Layer1Topology generatedTopology =
        batfish.getTopologyProvider().getLayer1Topologies(batfish.getSnapshot()).getSynthesizedL1();

    assertThat(
        generatedTopology.edgeStream().collect(Collectors.toList()),
        containsInAnyOrder(
            new Layer1Edge(hostname1, SYNC_INTERFACE_NAME, hostname2, SYNC_INTERFACE_NAME),
            new Layer1Edge(hostname2, SYNC_INTERFACE_NAME, hostname1, SYNC_INTERFACE_NAME)));
    ImmutableList.of(hostname1, hostname2)
        .forEach(
            host ->
                assertThat(
                    warnings.get(host).getRedFlagWarnings().stream()
                        .filter(w -> w.getText().contains("Cannot generate Sync interface edge"))
                        .collect(ImmutableList.toImmutableList()),
                    containsInAnyOrder(
                        WarningMatchers.hasText(
                            "Cannot generate Sync interface edge to remote host 'cluster_member3'"
                                + " because it lacks a Sync interface"),
                        WarningMatchers.hasText(
                            "Cannot generate Sync interface edge to cluster member 'cm4' whose"
                                + " hostname cannot be determined"))));
    assertThat(
        warnings.get(hostname3).getRedFlagWarnings().stream()
            .filter(w -> w.getText().contains("Cannot generate Sync interface edge"))
            .collect(ImmutableList.toImmutableList()),
        contains(
            WarningMatchers.hasText(
                "Cannot generate Sync interface edges because Sync interface is missing")));
  }

  /**
   * Test that appropriate conversion warnings are added when different management data is missing.
   */
  @Test
  public void testManagementConfigurationWarnings() throws IOException {
    // Any config will do, just need to check mgmt warnings
    String gatewayName = "access_rules";

    // No management configuration
    {
      Batfish batfish = getBatfishForConfigurationNames(gatewayName);
      ConvertConfigurationAnswerElement ccae =
          batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
      assertThat(
          ccae,
          hasRedFlagWarning(
              gatewayName, containsString("No CheckPoint management configuration found")));
    }

    // Management configuration but no domains
    {
      CheckpointManagementConfiguration mgmt =
          new CheckpointManagementConfiguration(
              ImmutableMap.of("s", new ManagementServer(ImmutableMap.of(), "s")));
      Batfish batfish = getBatfishForConfigurationNames(mgmt, gatewayName);
      ConvertConfigurationAnswerElement ccae =
          batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
      assertThat(
          ccae, hasRedFlagWarning(gatewayName, containsString("No domain found for this gateway")));
    }

    // Management configuration with a domain, but no access package
    {
      ImmutableMap<Uid, GatewayOrServer> gateways =
          ImmutableMap.of(
              Uid.of("1"),
              new SimpleGateway(
                  Ip.parse("10.0.0.1"),
                  gatewayName,
                  ImmutableList.of(),
                  new GatewayOrServerPolicy(null, null),
                  Uid.of("1")));
      CheckpointManagementConfiguration mgmt =
          toCheckpointMgmtConfig(gateways, ImmutableMap.of(), ImmutableList.of());
      Batfish batfish = getBatfishForConfigurationNames(mgmt, gatewayName);
      ConvertConfigurationAnswerElement ccae =
          batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
      assertThat(
          ccae,
          hasRedFlagWarning(gatewayName, containsString("No access package found for gateway")));
    }
  }
}
