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
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRedFlagWarning;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroup;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroupMembers;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDependencies;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.matchers.VrfMatchers.hasStaticRoutes;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.allOf;
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

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
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
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
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
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
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

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
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
    assertThat(c.getAllInterfaces(), hasKeys("eth0", "eth1", "lo"));

    org.batfish.datamodel.Interface eth0 = c.getAllInterfaces().get("eth0");
    org.batfish.datamodel.Interface eth1 = c.getAllInterfaces().get("eth1");
    org.batfish.datamodel.Interface lo = c.getAllInterfaces().get("lo");

    assertTrue(eth0.getActive());
    assertThat(eth0.getAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.1.1/24")));
    assertThat(eth0.getMtu(), equalTo(1234));
    assertThat(eth0.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));

    assertFalse(eth1.getActive());
    assertNull(eth1.getAddress());
    assertThat(eth1.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));

    assertThat(lo.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.10/32")));
    assertThat(lo.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
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
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.1.0.0/16"))
                        .setNextHop(NextHopDiscard.instance())
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.2.0.0/16"))
                        .setNextHop(NextHopDiscard.instance())
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(8)
                        .setNetwork(Prefix.parse("10.3.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(0)
                        .setNetwork(Prefix.parse("10.4.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.3")))
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(1)
                        .setNetwork(Prefix.parse("10.4.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.4")))
                        .build(),
                    org.batfish.datamodel.StaticRoute.testBuilder()
                        .setAdministrativeCost(3)
                        .setNetwork(Prefix.parse("10.5.0.0/16"))
                        .setNextHop(NextHopIp.of(Ip.parse("10.0.0.5")))
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
                    hasComment("Cannot add non-existent interface to a bonding group."),
                    hasText(containsString("interface eth2"))),
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
    assertThat(c, notNullValue());
    assertThat(c.getAllInterfaces(), hasKeys("bond0", "bond1", "eth0", "eth1"));

    String bond0Name = "bond0";
    String bond1Name = "bond1";
    String eth0Name = "eth0";
    String eth1Name = "eth1";

    assertThat(c, hasInterface(bond0Name, isActive(true)));
    assertThat(c, hasInterface(bond0Name, hasInterfaceType(AGGREGATED)));
    assertThat(
        c,
        hasInterface(
            bond0Name,
            hasDependencies(
                containsInAnyOrder(
                    new Dependency(eth0Name, AGGREGATE), new Dependency(eth1Name, AGGREGATE)))));
    assertThat(
        c, hasInterface(bond0Name, hasChannelGroupMembers(containsInAnyOrder(eth0Name, eth1Name))));
    assertThat(c, hasInterface(bond0Name, hasChannelGroup(nullValue())));
    assertThat(c, hasInterface(bond0Name, hasMtu(1234)));

    assertThat(c, hasInterface(bond1Name, isActive(false)));
    assertThat(c, hasInterface(bond1Name, hasInterfaceType(AGGREGATED)));
    assertThat(c, hasInterface(bond1Name, hasDependencies(emptyIterable())));
    assertThat(c, hasInterface(bond1Name, hasChannelGroupMembers(emptyIterable())));
    assertThat(c, hasInterface(bond1Name, hasChannelGroup(nullValue())));

    assertThat(c, hasInterface(eth0Name, isActive(true)));
    assertThat(c, hasInterface(eth0Name, hasInterfaceType(PHYSICAL)));
    assertThat(c, hasInterface(eth0Name, hasChannelGroup(equalTo("0"))));
    assertThat(c, hasInterface(eth0Name, hasMtu(1234)));

    assertThat(c, hasInterface(eth1Name, isActive(true)));
    assertThat(c, hasInterface(eth1Name, hasInterfaceType(PHYSICAL)));
    assertThat(c, hasInterface(eth1Name, hasChannelGroup(equalTo("0"))));
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
            containsString("Cannot reference non-existent interface eth1 in bonding group 1000.")));
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
    }
    {
      // bond3.3 is on, but bond3 is off
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("bond3.3");
      assertFalse(iface.getActive());
      assertThat(iface.getEncapsulationVlan(), equalTo(3));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.AGGREGATE_CHILD));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth10.4092");
      assertTrue(iface.getActive());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("10.10.10.1/24")));
      assertThat(iface.getEncapsulationVlan(), equalTo(4092));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    }
    {
      // eth11.4093 is on, but eth11 is off
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth11.4093");
      assertFalse(iface.getActive());
      assertThat(iface.getAddress(), equalTo(ConcreteInterfaceAddress.parse("11.11.11.1/24")));
      assertThat(iface.getEncapsulationVlan(), equalTo(4093));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    }
    {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("eth12.4094");
      assertFalse(iface.getActive());
      assertThat(iface.getEncapsulationVlan(), equalTo(4094));
      assertThat(iface.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    }
  }
}
