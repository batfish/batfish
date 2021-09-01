package org.batfish.vendor.check_point_gateway.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.CHECK_POINT_GATEWAY;
import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.InterfaceType.AGGREGATED;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasOutgoingFilter;
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
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration.INTERFACE_ACL_NAME;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_ETH_SPEED;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_INTERFACE_MTU;
import static org.batfish.vendor.check_point_gateway.representation.Interface.DEFAULT_LOOPBACK_MTU;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
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
import org.batfish.vendor.check_point_management.Domain;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.GatewayOrServerPolicy;
import org.batfish.vendor.check_point_management.InterfaceTopology;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.ManagementPackage;
import org.batfish.vendor.check_point_management.ManagementServer;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.Network;
import org.batfish.vendor.check_point_management.Package;
import org.batfish.vendor.check_point_management.RulebaseAction;
import org.batfish.vendor.check_point_management.SimpleGateway;
import org.batfish.vendor.check_point_management.TypedManagementObject;
import org.batfish.vendor.check_point_management.Uid;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class CheckPointGatewayGrammarTest {
  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/vendor/check_point_gateway/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull CheckPointGatewayConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CheckPointGatewayCombinedParser parser = new CheckPointGatewayCombinedParser(src, settings);
    CheckPointGatewayControlPlaneExtractor extractor =
        new CheckPointGatewayControlPlaneExtractor(
            src, parser, new Warnings(), new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    CheckPointGatewayConfiguration vendorConfiguration =
        (CheckPointGatewayConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
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
      Map<Uid, GatewayOrServer> gateways, Map<Uid, ManagementPackage> packages) {
    return new CheckpointManagementConfiguration(
        ImmutableMap.of(
            "s",
            new ManagementServer(
                ImmutableMap.of(
                    "d", new ManagementDomain(new Domain("d", Uid.of("0")), gateways, packages)),
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
                        Uid.of("4"), new Network("n1", Ip.ZERO, Ip.ZERO, Uid.of("n1uid"))),
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
                        Uid.of("8"), new Network("n2", Ip.MAX, Ip.MAX, Uid.of("n2uid"))),
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

    CheckpointManagementConfiguration mgmt = toCheckpointMgmtConfig(gateways, packages);
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
    assertThat(c3.getIpSpaces(), anEmptyMap());
    assertThat(c4.getIpSpaces(), anEmptyMap());
    assertThat(c5.getIpSpaces(), anEmptyMap());
  }

  @Test
  public void testAccessRulesConversion() throws IOException {
    Uid cpmiAnyUid = Uid.of("99999");
    CpmiAnyObject any = new CpmiAnyObject(cpmiAnyUid);
    Uid acceptUid = Uid.of("31");
    Uid dropUid = Uid.of("32");
    Uid net1Uid = Uid.of("11");
    Uid net2Uid = Uid.of("12");
    String accessLayerName = "accessLayerFoo";

    ImmutableMap<Uid, TypedManagementObject> objs =
        ImmutableMap.<Uid, TypedManagementObject>builder()
            .put(cpmiAnyUid, any)
            .put(
                net1Uid,
                new Network(
                    "networkEth1", Ip.parse("10.0.1.0"), Ip.parse("255.255.255.0"), net1Uid))
            .put(
                net2Uid,
                new Network(
                    "networkEth2", Ip.parse("10.0.2.0"), Ip.parse("255.255.255.0"), net2Uid))
            .put(acceptUid, new RulebaseAction("Accept", acceptUid, "Accept"))
            .put(dropUid, new RulebaseAction("Drop", dropUid, "Drop"))
            .build();
    ImmutableList<AccessRuleOrSection> rulebase =
        ImmutableList.of(
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(acceptUid)
                .setDestination(ImmutableList.of(net2Uid))
                .setSource(ImmutableList.of(net1Uid))
                .setUid(Uid.of("100"))
                .setName("acceptNet1ToNet2")
                .build(),
            AccessRule.testBuilder(cpmiAnyUid)
                .setAction(dropUid)
                .setUid(Uid.of("101"))
                .setName("dropAll")
                .build());

    ImmutableMap<Uid, ManagementPackage> packages =
        ImmutableMap.of(
            Uid.of("2"),
            new ManagementPackage(
                ImmutableList.of(new AccessLayer(objs, rulebase, Uid.of("3"), accessLayerName)),
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

    CheckpointManagementConfiguration mgmt = toCheckpointMgmtConfig(gateways, packages);
    Map<String, Configuration> configs = parseTextConfigs(mgmt, "access_rules");
    Configuration c = configs.get("access_rules");

    // eth1 to eth2
    Flow permitted = createFlow("10.0.1.10", "10.0.2.10");
    // eth1 to eth3
    Flow denied = createFlow("10.0.1.10", "10.0.3.10");

    // Confirm access-layer, composite ACL, and interface ACLs have the same, correct behavior
    // Access-layer
    assertThat(c, hasIpAccessList(accessLayerName, accepts(permitted, "eth1", c)));
    assertThat(c, hasIpAccessList(accessLayerName, rejects(denied, "eth1", c)));
    // Composite ACL (same as access-layer since there is only one access-layer here)
    assertThat(c, hasIpAccessList(INTERFACE_ACL_NAME, accepts(permitted, "eth1", c)));
    assertThat(c, hasIpAccessList(INTERFACE_ACL_NAME, rejects(denied, "eth1", c)));
    // Iface ACLs
    assertThat(c, hasInterface("eth1", hasOutgoingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth1", hasOutgoingFilter(rejects(denied, "eth1", c))));
    assertThat(c, hasInterface("eth2", hasOutgoingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth2", hasOutgoingFilter(rejects(denied, "eth1", c))));
    assertThat(c, hasInterface("eth3", hasOutgoingFilter(accepts(permitted, "eth1", c))));
    assertThat(c, hasInterface("eth3", hasOutgoingFilter(rejects(denied, "eth1", c))));
  }
}
