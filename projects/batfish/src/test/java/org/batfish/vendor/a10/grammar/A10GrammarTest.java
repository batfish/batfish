package org.batfish.vendor.a10.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.A10_ACOS;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasAdministrativeCost;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHop;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddressMetadata;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroup;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroupMembers;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDescription;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasHumanName;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.StaticRouteMatchers.hasRecursive;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceName;
import static org.batfish.vendor.a10.representation.A10Conversion.SNAT_PORT_POOL_START;
import static org.batfish.vendor.a10.representation.A10StructureType.INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureType.VRRP_A_FAIL_OVER_POLICY_TEMPLATE;
import static org.batfish.vendor.a10.representation.A10StructureType.VRRP_A_VRID;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VLAN_TAGGED_INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VLAN_UNTAGGED_INTERFACE;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.matchers.WarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.BgpNeighbor;
import org.batfish.vendor.a10.representation.BgpNeighborIdAddress;
import org.batfish.vendor.a10.representation.BgpNeighborUpdateSourceAddress;
import org.batfish.vendor.a10.representation.BgpProcess;
import org.batfish.vendor.a10.representation.Interface;
import org.batfish.vendor.a10.representation.Interface.Type;
import org.batfish.vendor.a10.representation.InterfaceReference;
import org.batfish.vendor.a10.representation.NatPool;
import org.batfish.vendor.a10.representation.Server;
import org.batfish.vendor.a10.representation.ServerPort;
import org.batfish.vendor.a10.representation.ServerTargetAddress;
import org.batfish.vendor.a10.representation.ServiceGroup;
import org.batfish.vendor.a10.representation.ServiceGroupMember;
import org.batfish.vendor.a10.representation.StaticRoute;
import org.batfish.vendor.a10.representation.StaticRouteManager;
import org.batfish.vendor.a10.representation.TrunkGroup;
import org.batfish.vendor.a10.representation.TrunkInterface;
import org.batfish.vendor.a10.representation.VirtualServer;
import org.batfish.vendor.a10.representation.VirtualServerPort;
import org.batfish.vendor.a10.representation.VirtualServerTargetAddress;
import org.batfish.vendor.a10.representation.Vlan;
import org.batfish.vendor.a10.representation.VrrpA;
import org.batfish.vendor.a10.representation.VrrpACommon;
import org.batfish.vendor.a10.representation.VrrpAFailOverPolicyTemplate;
import org.batfish.vendor.a10.representation.VrrpAVrid;
import org.batfish.vendor.a10.representation.VrrpaVridBladeParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests of VS model extraction and VS model-to-VI model conversion for {@link A10Configuration}s.
 */
@ParametersAreNonnullByDefault
public class A10GrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/a10/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull A10Configuration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    A10CombinedParser parser = new A10CombinedParser(src, settings);
    Warnings parseWarnings = new Warnings();
    A10ControlPlaneExtractor extractor =
        new A10ControlPlaneExtractor(src, parser, parseWarnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    A10Configuration vendorConfiguration = (A10Configuration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    A10Configuration vc = SerializationUtils.clone(vendorConfiguration);
    vc.setAnswerElement(new ConvertConfigurationAnswerElement());
    vc.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    vc.setWarnings(parseWarnings);
    return vc;
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    ConversionContext conversionContext = new ConversionContext();
    return BatfishTestUtils.getBatfishForTextConfigsAndConversionContext(
        _folder, conversionContext, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      String canonicalHostname = hostname.toLowerCase();
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
      assertThat(c, hasConfigurationFormat(A10_ACOS));
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

  /**
   * Config with blank lines before, in the middle, and at the end should be correctly recognized
   * and parsed.
   */
  @Test
  public void testNewlines() {
    String hostname = "newlines";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    assertThat(c.getHostname(), equalTo("newlines"));
    assertThat(c.getInterfacesEthernet().keySet(), contains(1));
  }

  @Test
  public void testHostnameExtraction() {
    String hostname = "hostname";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm hostname extracted as-typed; will be lower-cased in conversion.
    assertThat(c.getHostname(), equalTo("HOSTNAME"));
  }

  @Test
  public void testHostnameConversion() {
    String hostname = "hostname";
    Configuration c = parseConfig(hostname);
    assertThat(c, notNullValue());
    // Should be lower-cased
    assertThat(c.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testHostnameAfterRba() {
    String hostname = "hostname_after_rba";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // hostname update after rba should still apply
    assertThat(c.getHostname(), equalTo("hostname_after_rba"));
  }

  @Test
  public void testRbaHostname() {
    String hostname = "rba_hostname";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // rba definition shouldn't interfere with hostname
    assertThat(c.getHostname(), equalTo("rba_hostname"));
  }

  @Test
  public void testStaticRouteExtraction() {
    String hostname = "static_route";
    A10Configuration c = parseVendorConfig(hostname);
    Prefix prefix1 = Prefix.parse("10.100.0.0/16");
    Ip forwardingRouterAddr1a = Ip.parse("10.100.4.2");
    Ip forwardingRouterAddr1b = Ip.parse("10.100.4.3");
    Prefix prefix2 = Prefix.parse("10.100.1.0/24");
    Ip forwardingRouterAddr2 = Ip.parse("10.100.4.4");

    // Prefixes
    assertThat(c.getStaticRoutes().keySet(), containsInAnyOrder(prefix1, prefix2));
    StaticRouteManager srmPrefix1 = c.getStaticRoutes().get(prefix1);
    StaticRouteManager srmPrefix2 = c.getStaticRoutes().get(prefix2);

    // Forwarding router addresses, a.k.a. nexthops
    assertThat(
        srmPrefix1.getVariants().keySet(),
        containsInAnyOrder(forwardingRouterAddr1a, forwardingRouterAddr1b));
    assertThat(srmPrefix2.getVariants().keySet(), contains(forwardingRouterAddr2));
    StaticRoute route1a = srmPrefix1.getVariants().get(forwardingRouterAddr1a);
    StaticRoute route1b = srmPrefix1.getVariants().get(forwardingRouterAddr1b);
    StaticRoute route2 = srmPrefix2.getVariants().get(forwardingRouterAddr2);

    // Route properties
    assertThat(route1a.getForwardingRouterAddress(), equalTo(forwardingRouterAddr1a));
    assertThat(route1a.getDescription(), equalTo("baz"));
    assertThat(route1a.getDistance(), equalTo(255));

    assertThat(route1b.getForwardingRouterAddress(), equalTo(forwardingRouterAddr1b));
    assertThat(route1b.getDescription(), equalTo("foobar"));
    assertThat(route1b.getDistance(), equalTo(1));

    assertThat(route2.getForwardingRouterAddress(), equalTo(forwardingRouterAddr2));
    assertNull(route2.getDescription());
    assertNull(route2.getDistance());
  }

  @Test
  public void testStaticRouteDeleteExtraction() {
    String hostname = "static_route_delete";
    A10Configuration c = parseVendorConfig(hostname);
    Prefix prefix = Prefix.parse("10.100.0.0/16");
    Ip forwardingRouterAddr = Ip.parse("10.100.4.6");

    // Only the last static route was not deleted
    assertThat(c.getStaticRoutes().keySet(), contains(prefix));
    StaticRouteManager srmPrefix = c.getStaticRoutes().get(prefix);

    assertThat(srmPrefix.getVariants().keySet(), contains(forwardingRouterAddr));
    StaticRoute route = srmPrefix.getVariants().get(forwardingRouterAddr);

    assertThat(route.getForwardingRouterAddress(), equalTo(forwardingRouterAddr));
    assertThat(route.getDescription(), equalTo("baz"));
    assertThat(route.getDistance(), equalTo(255));
  }

  @Test
  public void testStaticRouteDeleteWarn() throws IOException {
    String filename = "static_route_delete";
    Batfish batfish = getBatfishForConfigurationNames(filename);
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
                hasComment("No routes exist for prefix 10.101.0.0/16"),
                hasComment("No route exists for forwarding router address 10.100.4.100"),
                hasComment("No route exists with description 'foo'"),
                hasComment("No route exists with distance 1"))));
  }

  @Test
  public void testStaticRouteConversion() {
    Configuration c = parseConfig("static_route_convert");
    Prefix prefix1 = Prefix.parse("10.100.0.0/16");
    Prefix prefix2 = Prefix.parse("10.101.0.0/16");
    assertThat(
        c.getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(
            allOf(
                hasPrefix(prefix1),
                hasAdministrativeCost(StaticRoute.DEFAULT_STATIC_ROUTE_DISTANCE),
                hasNextHop(NextHopIp.of(Ip.parse("10.100.4.1"))),
                hasRecursive(false)),
            allOf(
                hasPrefix(prefix1),
                hasAdministrativeCost(1),
                hasNextHop(NextHopIp.of(Ip.parse("10.100.4.2"))),
                hasRecursive(false)),
            allOf(
                hasPrefix(prefix2),
                hasAdministrativeCost(2),
                hasNextHop(NextHopIp.of(Ip.parse("10.100.4.3"))),
                hasRecursive(false)),
            allOf(
                hasPrefix(Prefix.ZERO),
                hasAdministrativeCost(1),
                hasNextHop(NextHopIp.of(Ip.parse("10.100.4.4"))),
                hasRecursive(false))));
  }

  @Test
  public void testVlanExtraction() {
    String hostname = "vlan";
    A10Configuration c = parseVendorConfig(hostname);

    assertThat(c.getInterfacesVe(), hasKey(2));

    Map<Integer, Vlan> vlans = c.getVlans();
    assertThat(vlans.keySet(), containsInAnyOrder(2, 3, 4, 5));
    Vlan vlan2 = vlans.get(2);
    Vlan vlan3 = vlans.get(3);
    Vlan vlan4 = vlans.get(4);
    Vlan vlan5 = vlans.get(5);

    assertThat(vlan2.getName(), equalTo("Vlan 2 Name"));
    assertThat(vlan2.getNumber(), equalTo(2));
    assertThat(vlan2.getRouterInterface(), equalTo(2));
    assertThat(
        vlan2.getTagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 2),
            new InterfaceReference(Interface.Type.ETHERNET, 3)));
    assertThat(vlan2.getUntagged(), emptyIterable());

    assertNull(vlan3.getName());
    assertThat(vlan3.getNumber(), equalTo(3));
    assertNull(vlan3.getRouterInterface());
    assertThat(
        vlan3.getTagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 4),
            new InterfaceReference(Interface.Type.ETHERNET, 5)));

    assertNull(vlan4.getName());
    assertNull(vlan4.getRouterInterface());
    assertThat(vlan4.getTagged(), emptyIterable());
    assertThat(
        vlan4.getUntagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 6),
            new InterfaceReference(Interface.Type.ETHERNET, 7)));

    assertNull(vlan5.getName());
    assertNull(vlan5.getRouterInterface());
    assertThat(vlan5.getTagged(), emptyIterable());
    assertThat(
        vlan5.getUntagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 8),
            new InterfaceReference(Interface.Type.ETHERNET, 9)));
  }

  @Test
  public void testVlanConversion() {
    String hostname = "vlan_convert";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Ethernet1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "Ethernet2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(Range.closed(2, 3))),
                hasNativeVlan(nullValue()))));
    assertThat(
        c,
        hasInterface(
            "Ethernet3",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.EMPTY),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "VirtualEthernet2",
            allOf(
                hasInterfaceType(InterfaceType.VLAN),
                hasVlan(2),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.100.2.1/24"))))));
  }

  @Test
  public void testTrunkVlanConversion() {
    String hostname = "trunk_vlan_convert";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Trunk1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "Trunk2",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(nullValue()))));
    // Trunk3 only gets VLAN settings directly configured on it (ignore member settings)
    assertThat(
        c,
        hasInterface(
            "Trunk3",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.EMPTY),
                hasNativeVlan(equalTo(5)))));
  }

  /** Testing ACOS v2 trunk VLAN conversion */
  @Test
  public void testTrunkVlanAcos2Conversion() {
    String hostname = "trunk_acos2_convert";
    Configuration c = parseConfig(hostname);

    // Trunk1 inherits member VLAN settings
    assertThat(
        c,
        hasInterface(
            "Trunk1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(equalTo(3)))));

    // Trunk2's members have different VLAN settings, which are ignored
    assertThat(
        c,
        hasInterface(
            "Trunk2",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllowedVlans(IntegerSpace.EMPTY),
                hasNativeVlan(nullValue()))));
  }

  /** Testing ACOS v2 VLAN syntax */
  @Test
  public void testVlanAcos2Extraction() {
    String hostname = "vlan_acos2";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Vlan> vlans = c.getVlans();
    assertThat(vlans.keySet(), containsInAnyOrder(2, 3));
    Vlan vlan2 = vlans.get(2);
    Vlan vlan3 = vlans.get(3);

    assertThat(vlan2.getNumber(), equalTo(2));
    assertThat(
        vlan2.getTagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 2)));
    assertThat(vlan2.getUntagged(), emptyIterable());
    assertThat(vlan3.getTagged(), emptyIterable());
    assertThat(
        vlan3.getUntagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 3),
            new InterfaceReference(Interface.Type.ETHERNET, 4)));
  }

  @Test
  public void testVlanWarn() throws IOException {
    String filename = "vlan_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);
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
                hasComment("Expected vlan number in range 2-4094, but got '1'"),
                hasComment("Expected vlan number in range 2-4094, but got '4095'"),
                allOf(
                    hasComment(
                        "Invalid range for VLAN interface reference, 'from' must not be greater"
                            + " than 'to'."),
                    hasText("ethernet 3 to 2")),
                hasComment("Virtual Ethernet interface number must be the same as VLAN ID."),
                allOf(
                    hasComment(
                        "Cannot create a ve interface for a non-existent or unassociated VLAN."),
                    hasText("ve 123")),
                allOf(
                    hasComment(
                        "Cannot create a ve interface for a non-existent or unassociated VLAN."),
                    hasText("ve 124")),
                hasComment("Expected vlan number in range 2-4094, but got '1'"),
                hasComment("Expected vlan number in range 2-4094, but got '4095'"))));
  }

  @Test
  public void testInterfaceRefs() throws IOException {
    String hostname = "interface_refs";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference counts
    // Referenced from vlan
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 1), 3));
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 2), 3));
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 3), 2));

    // Referenced from vlan
    assertThat(
        ccae, hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.VE, 2), 1));

    // Referenced from both of its members
    assertThat(
        ccae, hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.TRUNK, 10), 2));

    // Just self-refs, to avoid unused warnings
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 10), 1));
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 11), 1));
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.LOOPBACK, 0), 1));

    // Unused trunk should have no references
    assertThat(
        ccae, hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.TRUNK, 200), 0));

    // Confirm undefined references are detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            INTERFACE,
            getInterfaceName(Interface.Type.ETHERNET, 4),
            VLAN_TAGGED_INTERFACE));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            INTERFACE,
            getInterfaceName(Interface.Type.ETHERNET, 5),
            VLAN_UNTAGGED_INTERFACE));
  }

  @Test
  public void testInterfacesExtraction() {
    String hostname = "interfaces";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Interface> eths = c.getInterfacesEthernet();
    Map<Integer, Interface> loops = c.getInterfacesLoopback();
    assertThat(eths.keySet(), containsInAnyOrder(1, 9));
    assertThat(loops.keySet(), containsInAnyOrder(0, 10));

    Interface eth1 = eths.get(1);
    Interface eth9 = eths.get(9);
    assertTrue(eth1.getEnabled());
    assertThat(eth1.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.1.1/24")));
    assertThat(eth1.getMtu(), equalTo(1234));
    assertThat(eth1.getName(), equalTo("this is a comp\"licat'ed name"));
    assertThat(eth1.getNumber(), equalTo(1));
    assertThat(eth1.getType(), equalTo(Interface.Type.ETHERNET));

    assertFalse(eth9.getEnabled());
    assertThat(eth9.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.2.1/24")));
    assertNull(eth9.getMtu());
    assertThat(eth9.getName(), equalTo("baz"));
    assertThat(eth9.getNumber(), equalTo(9));
    assertThat(eth9.getType(), equalTo(Interface.Type.ETHERNET));

    Interface loop0 = loops.get(0);
    Interface loop10 = loops.get(10);
    assertNull(loop0.getEnabled());
    assertThat(loop0.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.0.1/32")));
    assertThat(loop0.getNumber(), equalTo(0));
    assertThat(loop0.getType(), equalTo(Interface.Type.LOOPBACK));

    assertNull(loop10.getIpAddress());
  }

  @Test
  public void testInterfaceWarn() throws IOException {
    String filename = "interface_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);
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
                hasComment(
                    "Expected interface name with length in range 1-63, but got"
                        + " '1234567890123456789012345678901234567890123456789012345678901234'"),
                hasComment("Expected loopback interface number in range 0-10, but got '11'"),
                hasComment("Expected ethernet interface number in range 1-40, but got '0'"))));
  }

  @Test
  public void testInterfaceConversion() {
    String hostname = "interfaces";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Ethernet1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.1.1/24"))),
                hasAddressMetadata(
                    hasEntry(
                        ConcreteInterfaceAddress.parse("10.0.1.1/24"),
                        ConnectedRouteMetadata.builder().setGenerateLocalRoute(false).build())),
                isActive(),
                hasDescription("this is a comp\"licat'ed name"),
                hasHumanName("Ethernet 1"),
                hasMtu(1234))));

    assertThat(
        c,
        hasInterface(
            "Ethernet9",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.2.1/24"))),
                isActive(false),
                hasDescription("baz"),
                hasHumanName("Ethernet 9"),
                hasMtu(DEFAULT_MTU))));

    assertThat(
        c,
        hasInterface(
            "Loopback0",
            allOf(
                hasInterfaceType(InterfaceType.LOOPBACK),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("192.168.0.1/32"))),
                hasDescription(nullValue()),
                hasHumanName("Loopback 0"))));

    assertThat(
        c,
        hasInterface(
            "Loopback10",
            allOf(
                hasInterfaceType(InterfaceType.LOOPBACK),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(empty()),
                hasDescription(nullValue()),
                hasHumanName("Loopback 10"))));
  }

  @Test
  public void testTrunkExtraction() {
    String hostname = "trunk";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Interface> eths = c.getInterfacesEthernet();
    Map<Integer, TrunkInterface> trunks = c.getInterfacesTrunk();
    assertThat(eths.keySet(), containsInAnyOrder(1, 2, 3, 4, 5));
    assertThat(trunks.keySet(), containsInAnyOrder(1, 2, 3, 4));

    Interface eth1 = eths.get(1);
    TrunkGroup tg1 = eths.get(1).getTrunkGroup();
    TrunkInterface ti1 = trunks.get(1);
    TrunkGroup tg2 = eths.get(2).getTrunkGroup();
    TrunkGroup tg3 = eths.get(3).getTrunkGroup();
    TrunkGroup tg4 = eths.get(4).getTrunkGroup();
    TrunkGroup eth5tg1 = eths.get(5).getTrunkGroup();

    assertThat(tg1.getTypeEffective(), equalTo(TrunkGroup.Type.STATIC));
    assertNull(tg1.getMode());
    assertNull(tg1.getTimeout());
    assertThat(tg1.getUserTag(), equalTo("user tag str"));
    assertThat(ti1.getPortsThreshold(), equalTo(2));
    assertThat(
        ti1.getMembers(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 5)));
    assertThat(ti1.getTrunkTypeEffective(), equalTo(TrunkGroup.Type.STATIC));

    // Interface property after trunk-group definition is still applied
    assertThat(eth1.getName(), equalTo("eth name"));

    assertThat(tg2.getTypeEffective(), equalTo(TrunkGroup.Type.STATIC));

    assertThat(tg3.getTypeEffective(), equalTo(TrunkGroup.Type.LACP));
    assertThat(tg3.getMode(), equalTo(TrunkGroup.Mode.ACTIVE));
    assertThat(tg3.getTimeout(), equalTo(TrunkGroup.Timeout.LONG));

    assertThat(tg4.getTypeEffective(), equalTo(TrunkGroup.Type.LACP_UDLD));
    assertThat(tg4.getMode(), equalTo(TrunkGroup.Mode.PASSIVE));
    assertThat(tg4.getTimeout(), equalTo(TrunkGroup.Timeout.SHORT));

    assertThat(eth5tg1.getTypeEffective(), equalTo(TrunkGroup.Type.STATIC));
    assertThat(eth5tg1.getUserTag(), equalTo("other tag"));
  }

  /** Testing ACOS v2 syntax */
  @Test
  public void testTrunkAcos2Extraction() {
    String hostname = "trunk_acos2";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Interface> eths = c.getInterfacesEthernet();
    Map<Integer, TrunkInterface> trunks = c.getInterfacesTrunk();
    assertThat(eths.keySet(), containsInAnyOrder(1, 2, 3, 4));
    assertThat(trunks.keySet(), containsInAnyOrder(1, 2));

    // LACP trunk-group
    TrunkGroup tg1 = eths.get(1).getTrunkGroup();
    TrunkInterface trunkIface1 = trunks.get(1);
    // Static trunk-group
    TrunkInterface trunkIface2 = trunks.get(2);

    // Settings for the member interface
    assertThat(tg1.getTypeEffective(), equalTo(TrunkGroup.Type.LACP));
    assertThat(tg1.getTimeout(), equalTo(TrunkGroup.Timeout.SHORT));

    // Settings for the LACP trunk
    assertThat(trunkIface1.getTrunkTypeEffective(), equalTo(TrunkGroup.Type.LACP));
    assertThat(trunkIface1.getPortsThreshold(), equalTo(2));
    assertThat(
        trunkIface1.getMembers(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 4)));

    // Settings for the Static trunk
    assertThat(trunkIface2.getTrunkTypeEffective(), equalTo(TrunkGroup.Type.STATIC));
    assertNull(trunkIface2.getPortsThreshold());
    assertThat(
        trunkIface2.getMembers(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 2),
            new InterfaceReference(Interface.Type.ETHERNET, 3)));
  }

  /** Testing ACOS v2 trunk datamodel conversion */
  @Test
  public void testTrunkAcos2Conversion() {
    String hostname = "trunk_acos2";
    Configuration c = parseConfig(hostname);

    // LACP trunk
    assertThat(
        c,
        hasInterface(
            "Ethernet1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Ethernet4",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Trunk1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet1", "Ethernet4")))));
    assertThat(
        c.getAllInterfaces().get("Trunk1").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet1", org.batfish.datamodel.Interface.DependencyType.AGGREGATE),
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet4", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));

    // Default trunk
    assertThat(
        c,
        hasInterface(
            "Ethernet2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk2"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Ethernet3",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk2"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Trunk2",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet2", "Ethernet3")))));
    assertThat(
        c.getAllInterfaces().get("Trunk2").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet2", org.batfish.datamodel.Interface.DependencyType.AGGREGATE),
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet3", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));
  }

  /** Testing ACOS v2 trunk conversion warnings */
  @Test
  public void testTrunkAcos2ConversionWarn() throws IOException {
    String hostname = "trunk_acos2_convert_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);

    assertThat(
        warnings,
        hasRedFlags(
            containsInAnyOrder(
                WarningMatchers.hasText("Trunk2 does not contain any member interfaces"),
                WarningMatchers.hasText(
                    containsString(
                        "VLAN settings for members of Trunk1 are different, ignoring their VLAN"
                            + " settings")),
                WarningMatchers.hasText(
                    "Trunk member Ethernet10 does not exist, cannot add to Trunk1"))));
  }

  @Test
  public void testTrunkWarn() throws IOException {
    String filename = "trunk_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Ports-threshold can only be configured on trunk interfaces."),
                hasComment("Expected trunk number in range 1-4096, but got '0'"),
                hasComment("Expected trunk number in range 1-4096, but got '4097'"),
                allOf(
                    hasComment("Trunk-group already exists as a different type (lacp)"),
                    hasText("trunk-group 3 static")),
                allOf(
                    hasComment("Trunk-group already exists as a different type (lacp)"),
                    hasText("trunk-group 3")),
                hasComment("This interface is already a member of trunk-group 3"),
                hasComment("Cannot add an interface with a configured IP address to a trunk-group"),
                hasComment("Cannot configure an IP address on a trunk-group member"))));
  }

  @Test
  public void testTrunkConversionWarn() throws IOException {
    String hostname = "trunk_convert_warn";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Warnings warnings =
        batfish
            .loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot())
            .getWarnings()
            .get(hostname);

    assertThat(
        warnings,
        hasRedFlags(
            containsInAnyOrder(
                WarningMatchers.hasText(
                    "Cannot configure VLAN settings on Trunk1 as well as its members. Member VLAN"
                        + " settings will be ignored."))));
  }

  /** Testing ACOS v2 syntax */
  @Test
  public void testTrunkAcos2Warn() throws IOException {
    String filename = "trunk_acos2_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Cannot configure timeout for non-existent trunk-group"),
                hasComment("Expected trunk ports-threshold in range 2-8, but got '1'"),
                hasComment("Expected trunk ports-threshold in range 2-8, but got '9'"))));
  }

  @Test
  public void testTrunkConversion() {
    String hostname = "trunk_convert";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Ethernet1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));

    assertThat(
        c,
        hasInterface(
            "Ethernet2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));

    assertThat(
        c,
        hasInterface(
            "Trunk1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet1", "Ethernet2")),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.1.1/24"))))));
    assertThat(
        c.getAllInterfaces().get("Trunk1").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet1", org.batfish.datamodel.Interface.DependencyType.AGGREGATE),
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet2", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));
  }

  @Test
  public void testVirtualServerExtraction() {
    String hostname = "virtual_server";
    A10Configuration c = parseVendorConfig(hostname);

    VirtualServerPort.PortAndType tcp80 =
        new VirtualServerPort.PortAndType(80, VirtualServerPort.Type.TCP);
    VirtualServerPort.PortAndType udp81 =
        new VirtualServerPort.PortAndType(81, VirtualServerPort.Type.UDP);
    VirtualServerPort.PortAndType tcpProxy101 =
        new VirtualServerPort.PortAndType(101, VirtualServerPort.Type.TCP_PROXY);
    VirtualServerPort.PortAndType http102 =
        new VirtualServerPort.PortAndType(102, VirtualServerPort.Type.HTTP);
    VirtualServerPort.PortAndType https103 =
        new VirtualServerPort.PortAndType(103, VirtualServerPort.Type.HTTPS);

    assertThat(c.getVirtualServers().keySet(), containsInAnyOrder("VS1", "VS2", "VS3"));

    {
      VirtualServer server1 = c.getVirtualServer("VS1");
      assertNull(server1.getEnable());
      assertThat(server1.getName(), equalTo("VS1"));
      assertThat(server1.getPorts(), anEmptyMap());
      assertNull(server1.getRedistributionFlagged());
      assertThat(
          server1.getTarget(), equalTo(new VirtualServerTargetAddress(Ip.parse("10.0.0.101"))));
      assertNull(server1.getVrid());
    }

    {
      VirtualServer server2 = c.getVirtualServer("VS2");
      assertTrue(server2.getEnable());
      assertThat(server2.getName(), equalTo("VS2"));
      assertTrue(server2.getRedistributionFlagged());
      assertThat(
          server2.getTarget(), equalTo(new VirtualServerTargetAddress(Ip.parse("10.0.0.102"))));
      assertThat(server2.getVrid(), equalTo(1));
      Map<VirtualServerPort.PortAndType, VirtualServerPort> server2Ports = server2.getPorts();
      assertThat(server2Ports.keySet(), contains(tcp80));
      VirtualServerPort server2Port80 = server2Ports.get(tcp80);
      assertThat(server2Port80.getBucketCount(), equalTo(100));
      assertTrue(server2Port80.getDefSelectionIfPrefFailed());
      assertTrue(server2Port80.getEnable());
      assertThat(server2Port80.getName(), equalTo("PORT_NAME"));
      assertThat(server2Port80.getNumber(), equalTo(80));
      assertNull(server2Port80.getRange());
      assertThat(server2Port80.getServiceGroup(), equalTo("SG1"));
      assertThat(server2Port80.getSourceNat(), equalTo("POOL1"));
      assertThat(server2Port80.getType(), equalTo(VirtualServerPort.Type.TCP));
    }

    {
      VirtualServer server3 = c.getVirtualServer("VS3");
      assertFalse(server3.getEnable());
      assertThat(server3.getName(), equalTo("VS3"));
      Map<VirtualServerPort.PortAndType, VirtualServerPort> server3Ports = server3.getPorts();
      assertThat(server3Ports.keySet(), containsInAnyOrder(udp81, tcpProxy101, http102, https103));
      VirtualServerPort server3Port81 = server3Ports.get(udp81);
      assertNull(server3Port81.getBucketCount());
      assertNull(server3Port81.getDefSelectionIfPrefFailed());
      assertFalse(server3Port81.getEnable());
      assertNull(server3Port81.getName());
      assertThat(server3Port81.getNumber(), equalTo(81));
      assertThat(server3Port81.getRange(), equalTo(11));
      assertNull(server3Port81.getServiceGroup());
      assertNull(server3Port81.getSourceNat());
      assertThat(server3Port81.getType(), equalTo(VirtualServerPort.Type.UDP));

      // Check other virtual port types
      assertThat(
          server3Ports.get(tcpProxy101).getType(), equalTo(VirtualServerPort.Type.TCP_PROXY));
      assertThat(server3Ports.get(http102).getType(), equalTo(VirtualServerPort.Type.HTTP));
      assertThat(server3Ports.get(https103).getType(), equalTo(VirtualServerPort.Type.HTTPS));
    }
  }

  @Test
  public void testVirtualServerWarn() throws IOException {
    String filename = "virtual_server_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Server target must be specified for a new virtual-server"),
                hasComment("Expected traffic bucket-count in range 1-256, but got '0'"),
                hasComment("Expected traffic bucket-count in range 1-256, but got '257'"),
                hasComment("Expected port range in range 0-254, but got '255'"),
                hasComment("Cannot assign virtual-server to undefined non-default vrid: 1"),
                hasComment(
                    "Cannot assign virtual-server to vrid 3, it contains a NAT pool in vrid 2"),
                hasComment("Cannot add non-existent service-group to virtual-server VS1"),
                hasComment("Cannot add non-existent nat pool to virtual-server VS1"),
                hasComment("Cannot assign a NAT pool in vrid 2, the virtual-server is in vrid 3"),
                hasComment(
                    "Service-group port type UDP is not compatible with virtual-server port type"
                        + " HTTPS"))));
  }

  @Test
  public void testNatPoolExtraction() {
    String hostname = "nat_pool";
    A10Configuration c = parseVendorConfig(hostname);

    assertThat(c.getNatPools().keySet(), contains("POOL1", "POOL2"));
    NatPool pool1 = c.getNatPools().get("POOL1");
    NatPool pool2 = c.getNatPools().get("POOL2");

    assertThat(pool1.getStart(), equalTo(Ip.parse("10.10.10.10")));
    assertThat(pool1.getEnd(), equalTo(Ip.parse("10.10.10.11")));
    assertThat(pool1.getNetmask(), equalTo(1));
    assertNull(pool1.getGateway());
    assertNull(pool1.getHaGroupId());
    assertFalse(pool1.getIpRr());
    assertFalse(pool1.getPortOverload());
    assertNull(pool1.getScaleoutDeviceId());
    assertNull(pool1.getVrid());

    assertThat(pool2.getStart(), equalTo(Ip.parse("10.10.10.12")));
    assertThat(pool2.getEnd(), equalTo(Ip.parse("10.10.10.12")));
    assertThat(pool2.getNetmask(), equalTo(32));
    assertThat(pool2.getGateway(), equalTo(Ip.parse("10.10.10.1")));
    assertNull(pool2.getHaGroupId());
    assertTrue(pool2.getIpRr());
    assertTrue(pool2.getPortOverload());
    assertThat(pool2.getScaleoutDeviceId(), equalTo(1));
    assertThat(pool2.getVrid(), equalTo(2));
  }

  @Test
  public void testNatPoolAcos2Extraction() {
    String hostname = "nat_pool_acos2";
    A10Configuration c = parseVendorConfig(hostname);

    assertThat(c.getNatPools().keySet(), contains("POOL1"));
    NatPool pool1 = c.getNatPools().get("POOL1");

    assertThat(pool1.getStart(), equalTo(Ip.parse("10.10.10.1")));
    assertThat(pool1.getEnd(), equalTo(Ip.parse("10.10.10.1")));
    assertThat(pool1.getNetmask(), equalTo(32));
    assertNull(pool1.getGateway());
    assertThat(pool1.getHaGroupId(), equalTo(1));
    assertFalse(pool1.getIpRr());
    assertFalse(pool1.getPortOverload());
    assertNull(pool1.getScaleoutDeviceId());
    assertNull(pool1.getVrid());
  }

  @Test
  public void testNatPoolWarn() throws IOException {
    String filename = "nat_pool_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Cannot modify an existing NAT pool"),
                hasComment(
                    "Invalid NAT pool range, the end address cannot be lower than the start"
                        + " address"),
                hasComment("Invalid NAT pool netmask, must be > 0"),
                hasComment("Invalid NAT pool range, all addresses must fit in specified netmask"),
                hasComment("Invalid NAT pool range, overlaps with existing NAT pool"),
                hasComment("Expected scaleout-device-id in range 1-16, but got '17'"),
                hasComment("Expected non-default vrid number in range 1-31, but got '0'"),
                hasComment("Expected non-default vrid number in range 1-31, but got '32'"),
                hasComment("Cannot assign nat pool to undefined non-default vrid: 5"))));
  }

  @Test
  public void testVrrpAExtraction() {
    String hostname = "vrrp-a";
    A10Configuration c = parseVendorConfig(hostname);

    VrrpA v = c.getVrrpA();
    assertThat(v, notNullValue());

    // vrrp-a common
    VrrpACommon vc = v.getCommon();
    assertThat(vc, notNullValue());
    assertThat(vc.getDeviceId(), equalTo(2));
    assertTrue(vc.getDisableDefaultVrid());
    assertThat(vc.getSetId(), equalTo(1));
    assertTrue(vc.getEnable());

    // vrrp-a fail-over-policy-template
    VrrpAFailOverPolicyTemplate fopt = v.getFailOverPolicyTemplates().get("gateway");
    assertThat(fopt, notNullValue());
    assertThat(fopt.getGateways().get(Ip.parse("10.0.0.1")), equalTo(100));

    // vrrp-a interface
    assertThat(v.getInterface(), equalTo(new InterfaceReference(Type.TRUNK, 2)));

    // vrrp-a peer-group
    assertThat(v.getPeerGroup(), contains(Ip.parse("10.0.1.1")));

    // vrrp-a vrid
    VrrpAVrid vrid = v.getVrids().get(0);
    assertThat(vrid, notNullValue());
    assertTrue(vrid.getPreemptModeDisable());
    assertThat(vrid.getPreemptModeThreshold(), equalTo(1));
    VrrpaVridBladeParameters vridbp = vrid.getBladeParameters();
    assertThat(vridbp, notNullValue());
    assertThat(vridbp.getPriority(), equalTo(200));
    assertThat(vridbp.getFailOverPolicyTemplate(), equalTo("gateway"));

    // vrrp-a vrid-lead
    assertThat(v.getVridLead(), equalTo("default-vrid-lead"));
  }

  @Test
  public void testVrrpAReferences() throws IOException {
    String hostname = "vrrp-a";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Confirm reference counts
    // referenced from vrrp-a interface, self
    assertThat(
        ccae,
        hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.ETHERNET, 1), 2));
    // referenced from vrrp-a interface, vlan 4094, ethernet1
    assertThat(
        ccae, hasNumReferrers(filename, INTERFACE, getInterfaceName(Interface.Type.TRUNK, 2), 3));

    // self-reference for vrid 0 only
    assertThat(ccae, hasNumReferrers(filename, VRRP_A_VRID, "0", 1));
    // Referenced from ip nat-pools with vrid 1
    assertThat(ccae, hasNumReferrers(filename, VRRP_A_VRID, "1", 2));

    // Referenced from vrrp-a vrid blade-parameters
    assertThat(ccae, hasNumReferrers(filename, VRRP_A_FAIL_OVER_POLICY_TEMPLATE, "gateway", 1));

    // Confirm definitions
    assertThat(ccae, hasDefinedStructure(filename, VRRP_A_FAIL_OVER_POLICY_TEMPLATE, "gateway"));
    assertThat(ccae, hasDefinedStructure(filename, VRRP_A_VRID, "0"));
    assertThat(ccae, hasDefinedStructure(filename, VRRP_A_VRID, "1"));
  }

  @Test
  public void testVrrpAAcos2Parsing() {
    // TODO: extraction
    String hostname = "vrrp-a_acos2";
    assertNotNull(parseVendorConfig(hostname));
  }

  @Test
  public void testVrrpAWarn() throws IOException {
    String filename = "vrrp-a_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);
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
                hasComment("Expected vrrp-a device-id in range 1-4, but got '5'"),
                hasComment("Expected vrrp-a set-id in range 1-15, but got '16'"),
                hasComment(
                    "Expected fail-over-policy-template name with length in range 1-63, but got"
                        + " '0000000000111111111122222222223333333333444444444455555555556666'"),
                hasComment(
                    "Expected fail-over-policy-template gateway weight in range 1-255, but got"
                        + " '0'"),
                hasComment("Expected ethernet interface number in range 1-40, but got '50'"),
                hasComment("Expected trunk number in range 1-4096, but got '5000'"),
                hasComment("Expected vrrp-a vrid number in range 0-31, but got '32'"),
                hasComment(
                    "Expected vrrp-a vrid blade-paramters priority in range 1-255, but got '0'"),
                hasComment("Cannot assign non-existent fail-over-policy-template 'undefined'"))));
  }

  @Test
  public void testServerExtraction() {
    String hostname = "server";
    A10Configuration c = parseVendorConfig(hostname);

    ServerPort.ServerPortAndType tcp80 = new ServerPort.ServerPortAndType(80, ServerPort.Type.TCP);
    ServerPort.ServerPortAndType udp81 = new ServerPort.ServerPortAndType(81, ServerPort.Type.UDP);

    assertThat(c.getServers().keySet(), containsInAnyOrder("SERVER1", "SERVER2", "SERVER3"));
    Server server1 = c.getServers().get("SERVER1");
    Server server2 = c.getServers().get("SERVER2");
    Server server3 = c.getServers().get("SERVER3");

    assertNull(server1.getConnLimit());
    assertNull(server1.getEnable());
    assertThat(server1.getName(), equalTo("SERVER1"));
    assertThat(server1.getPorts(), anEmptyMap());
    assertNull(server1.getServerTemplate());
    assertNull(server1.getStatsDataEnable());
    assertThat(server1.getTarget(), equalTo(new ServerTargetAddress(Ip.parse("10.0.0.1"))));
    assertNull(server1.getWeight());

    assertThat(server2.getConnLimit(), equalTo(64000000));
    assertTrue(server2.getEnable());
    assertThat(server2.getName(), equalTo("SERVER2"));
    assertThat(server2.getServerTemplate(), equalTo("SERVER_TEMPLATE"));
    assertTrue(server2.getStatsDataEnable());
    assertThat(server2.getTarget(), equalTo(new ServerTargetAddress(Ip.parse("10.0.0.2"))));
    assertThat(server2.getWeight(), equalTo(1000));
    Map<ServerPort.ServerPortAndType, ServerPort> server2Ports = server2.getPorts();
    assertThat(server2Ports.keySet(), contains(tcp80));
    ServerPort server2Port80 = server2Ports.get(tcp80);
    assertThat(server2Port80.getConnLimit(), equalTo(10));
    assertTrue(server2Port80.getEnable());
    assertThat(server2Port80.getNumber(), equalTo(80));
    assertThat(server2Port80.getPortTemplate(), equalTo("PORT_TEMPLATE"));
    assertNull(server2Port80.getRange());
    assertTrue(server2Port80.getStatsDataEnable());
    assertThat(server2Port80.getType(), equalTo(ServerPort.Type.TCP));
    assertThat(server2Port80.getWeight(), equalTo(999));

    assertFalse(server3.getEnable());
    assertThat(server3.getName(), equalTo("SERVER3"));
    assertFalse(server3.getStatsDataEnable());
    Map<ServerPort.ServerPortAndType, ServerPort> server3Ports = server3.getPorts();
    assertThat(server3Ports.keySet(), contains(udp81));
    ServerPort server3Port81 = server3Ports.get(udp81);
    assertNull(server3Port81.getConnLimit());
    assertFalse(server3Port81.getEnable());
    assertThat(server3Port81.getNumber(), equalTo(81));
    assertNull(server3Port81.getPortTemplate());
    assertThat(server3Port81.getRange(), equalTo(11));
    assertFalse(server3Port81.getStatsDataEnable());
    assertThat(server3Port81.getType(), equalTo(ServerPort.Type.UDP));
    assertNull(server3Port81.getWeight());
  }

  @Test
  public void testServerWarn() throws IOException {
    String filename = "server_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Server target must be specified for a new server"),
                hasComment("Expected conn-limit in range 1-64000000, but got '0'"),
                hasComment("Expected conn-limit in range 1-64000000, but got '64000001'"),
                hasComment("Expected connection weight in range 1-1000, but got '0'"),
                hasComment("Expected connection weight in range 1-1000, but got '1001'"),
                hasComment("Expected port range in range 0-254, but got '255'"))));
  }

  @Test
  public void testServiceGroupExtraction() {
    String hostname = "service_group";
    A10Configuration c = parseVendorConfig(hostname);

    assertThat(c.getServiceGroups().keySet(), containsInAnyOrder("SG1", "SG2", "SG3"));

    {
      ServiceGroup sg1 = c.getServiceGroups().get("SG1");
      assertNull(sg1.getHealthCheck());
      assertThat(sg1.getName(), equalTo("SG1"));
      assertNull(sg1.getStatsDataEnable());
      assertThat(sg1.getType(), equalTo(ServerPort.Type.TCP));
      assertThat(sg1.getMembers(), anEmptyMap());
    }

    {
      ServiceGroup sg2 = c.getServiceGroups().get("SG2");

      assertThat(sg2.getHealthCheck(), equalTo("HEALTH_CHECK_NAME"));
      assertThat(sg2.getName(), equalTo("SG2"));
      assertThat(sg2.getMethod(), equalTo(ServiceGroup.Method.LEAST_REQUEST));
      assertTrue(sg2.getStatsDataEnable());
      assertThat(sg2.getType(), equalTo(ServerPort.Type.TCP));
      assertThat(
          sg2.getMembers().keySet(),
          containsInAnyOrder(
              new ServiceGroupMember.NameAndPort("SERVER1", 80),
              new ServiceGroupMember.NameAndPort("SERVER2", 80),
              new ServiceGroupMember.NameAndPort("SERVER3", 80),
              new ServiceGroupMember.NameAndPort("SERVER3", 81)));
      // Make sure the new port reference in SG1 created a port for the server
      assertThat(c.getServers(), hasKey("SERVER3"));
      assertThat(
          c.getServers().get("SERVER3").getPorts(),
          hasKey(new ServerPort.ServerPortAndType(81, ServerPort.Type.TCP)));
    }
    {
      ServiceGroup sg3 = c.getServiceGroups().get("SG3");
      assertThat(sg3.getName(), equalTo("SG3"));
      assertThat(sg3.getMethod(), equalTo(ServiceGroup.Method.ROUND_ROBIN));
      assertFalse(sg3.getStatsDataEnable());
      assertThat(sg3.getType(), equalTo(ServerPort.Type.UDP));
      assertThat(
          sg3.getMembers().keySet(), contains(new ServiceGroupMember.NameAndPort("SERVER1", 8080)));
    }
  }

  @Test
  public void testServiceGroupWarn() throws IOException {
    String filename = "service_group_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment(
                    "Cannot modify the service-group type field at runtime, ignoring this"
                        + " service-group block."),
                hasComment("Specified server 'SERVER_UNDEF' does not exist."),
                hasComment("Expected member priority in range 1-16, but got '0'"),
                hasComment("Expected member priority in range 1-16, but got '17'"))));
  }

  /**
   * Test that transformations are applied as expected. More extensive unit testing of conversion
   * occurs elsewhere.
   */
  @Test
  public void testVirtualServerConversion() throws IOException {
    Configuration c = parseConfig("virtual_server_convert");

    Batfish batfish = getBatfish(ImmutableSortedMap.of(c.getHostname(), c), _folder);
    NetworkSnapshot snapshot = batfish.getSnapshot();
    batfish.computeDataPlane(snapshot);

    // This flow matches the virtual-server and should get SNAT and DNAT
    Flow flowMatch =
        Flow.builder()
            .setIngressNode(c.getHostname())
            .setIngressInterface("Ethernet1")
            .setSrcIp(Ip.parse("192.168.0.1"))
            .setDstIp(Ip.parse("10.0.0.100"))
            // Arbitrary src port
            .setSrcPort(123)
            .setDstPort(81)
            .setIpProtocol(IpProtocol.TCP)
            .build();
    // These flows don't match a(n enabled) virtual-server
    Flow flowNoMatchProtocol = flowMatch.toBuilder().setIpProtocol(IpProtocol.UDP).build();
    Flow flowNoMatchEnable = flowMatch.toBuilder().setDstIp(Ip.parse("10.0.0.101")).build();

    SortedMap<Flow, List<Trace>> traces =
        batfish
            .getTracerouteEngine(snapshot)
            .computeTraces(
                ImmutableSet.of(flowMatch, flowNoMatchProtocol, flowNoMatchEnable), false);

    // Flow matching the virtual-server should get both SNAT and DNAT
    assertThat(
        getTransformedFlow(Iterables.getOnlyElement(traces.get(flowMatch))),
        equalTo(
            flowMatch.toBuilder()
                .setSrcIp(Ip.parse("10.10.10.10"))
                .setSrcPort(SNAT_PORT_POOL_START)
                .setDstIp(Ip.parse("10.0.0.3"))
                .setDstPort(80)
                .build()));
    // No transformations for flows *not* matching a virtual-server
    assertNull(getTransformedFlow(Iterables.getOnlyElement(traces.get(flowNoMatchProtocol))));
    assertNull(getTransformedFlow(Iterables.getOnlyElement(traces.get(flowNoMatchEnable))));
  }

  /**
   * Extracts transformed flow from a trace that is expected to contain one hop with one {@link
   * ExitOutputIfaceStep}. The transformed flow is taken from that step.
   */
  private static Flow getTransformedFlow(Trace trace) {
    List<Hop> hops = trace.getHops();
    assert hops.size() == 1;
    List<Step<?>> steps = hops.get(0).getSteps();
    List<ExitOutputIfaceStep> exitIfaceSteps =
        steps.stream()
            .filter(s -> s instanceof ExitOutputIfaceStep)
            .map(ExitOutputIfaceStep.class::cast)
            .collect(ImmutableList.toImmutableList());
    assert exitIfaceSteps.size() == 1;
    return exitIfaceSteps.get(0).getDetail().getTransformedFlow();
  }

  @Test
  public void testBgpExtraction() {
    String hostname = "bgp";
    A10Configuration c = parseVendorConfig(hostname);
    BgpProcess bgp = c.getBgpProcess();

    assertNotNull(bgp);
    assertThat(bgp.getNumber(), equalTo(1L));
    assertThat(bgp.getDefaultLocalPreference(), equalTo(100L));
    assertThat(bgp.getMaximumPaths(), equalTo(1));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("10.10.10.10")));
    assertTrue(bgp.isRedistributeConnected());
    assertTrue(bgp.isRedistributeFloatingIp());
    assertTrue(bgp.isRedistributeIpNat());
    assertTrue(bgp.isRedistributeVipOnlyFlagged());
    assertTrue(bgp.isRedistributeVipOnlyNotFlagged());

    {
      BgpNeighbor neighbor1 = bgp.getNeighbor(new BgpNeighborIdAddress(Ip.parse("10.10.10.100")));
      assertNotNull(neighbor1);
      assertThat(neighbor1.getRemoteAs(), equalTo(4294967295L));
      assertTrue(neighbor1.isActivate());
      assertThat(neighbor1.getDescription(), equalTo("DESCRIPTION"));
      assertThat(neighbor1.getMaximumPrefix(), equalTo(128));
      assertThat(neighbor1.getMaximumPrefixThreshold(), equalTo(64));
      assertThat(neighbor1.getSendCommunity(), equalTo(BgpNeighbor.SendCommunity.BOTH));
      assertThat(neighbor1.getWeight(), equalTo(0));
      assertThat(
          neighbor1.getUpdateSource(),
          equalTo(new BgpNeighborUpdateSourceAddress(Ip.parse("10.10.10.200"))));
    }
    {
      BgpNeighbor neighbor2 = bgp.getNeighbor(new BgpNeighborIdAddress(Ip.parse("10.10.10.101")));
      assertNotNull(neighbor2);
      assertThat(neighbor2.getRemoteAs(), equalTo(65535L));
      assertFalse(neighbor2.isActivate());
      assertNull(neighbor2.getDescription());
      assertNull(neighbor2.getMaximumPrefix());
      assertNull(neighbor2.getMaximumPrefixThreshold());
      assertNull(neighbor2.getSendCommunity());
      assertNull(neighbor2.getWeight());
      assertNull(neighbor2.getUpdateSource());
    }
  }

  @Test
  public void testBgpWarn() throws IOException {
    String filename = "bgp_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);

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
                hasComment("Must specify neighbor remote-as or peer-group first"),
                hasComment("BGP is already configured with asn 1"),
                hasComment("Expected bgp asn in range 1-4294967295, but got '0'"),
                hasComment("Expected maximum-paths in range 1-64, but got '0'"),
                hasComment("Expected maximum-paths in range 1-64, but got '65'"),
                hasComment("Expected neighbor maximum-prefix in range 1-65536, but got '0'"),
                hasComment("Expected neighbor maximum-prefix in range 1-65536, but got '65537'"),
                hasComment(
                    "Expected neighbor maximum-prefix threshold in range 1-100, but got '0'"),
                hasComment(
                    "Expected neighbor maximum-prefix threshold in range 1-100, but got '101'"))));
  }
}
