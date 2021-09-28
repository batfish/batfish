package org.batfish.vendor.a10.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlags;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.A10_ACOS;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllAddresses;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroup;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasChannelGroupMembers;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasDeclaredNames;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasMtu;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceName;
import static org.batfish.vendor.a10.representation.A10StructureType.INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VLAN_TAGGED_INTERFACE;
import static org.batfish.vendor.a10.representation.A10StructureUsage.VLAN_UNTAGGED_INTERFACE;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
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
import com.google.common.collect.Range;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.matchers.WarningMatchers;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.Interface;
import org.batfish.vendor.a10.representation.InterfaceReference;
import org.batfish.vendor.a10.representation.TrunkGroup;
import org.batfish.vendor.a10.representation.TrunkInterface;
import org.batfish.vendor.a10.representation.Vlan;
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
            "Ethernet 1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "Ethernet 2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(Range.closed(2, 3))),
                hasNativeVlan(nullValue()))));
    assertThat(
        c,
        hasInterface(
            "Ethernet 3",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.EMPTY),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "VirtualEthernet 2",
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
            "Trunk 1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(equalTo(3)))));
    assertThat(
        c,
        hasInterface(
            "Trunk 2",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasSwitchPortMode(SwitchportMode.TRUNK),
                hasAllowedVlans(IntegerSpace.of(2)),
                hasNativeVlan(nullValue()))));
  }

  /** Testing ACOS v2 VLAN syntax */
  @Test
  public void testVlanAcos2Extraction() {
    String hostname = "vlan_acos2";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Vlan> vlans = c.getVlans();
    assertThat(vlans.keySet(), containsInAnyOrder(2));
    Vlan vlan2 = vlans.get(2);

    assertThat(vlan2.getNumber(), equalTo(2));
    assertThat(
        vlan2.getTagged(),
        containsInAnyOrder(
            new InterfaceReference(Interface.Type.ETHERNET, 1),
            new InterfaceReference(Interface.Type.ETHERNET, 2)));
    assertThat(vlan2.getUntagged(), emptyIterable());
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
                hasComment("Expected interface loopback number in range 0-10, but got '11'"),
                hasComment("Expected interface ethernet number in range 1-40, but got '0'"))));
  }

  @Test
  public void testInterfaceConversion() {
    String hostname = "interfaces";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Ethernet 1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.1.1/24"))),
                isActive(),
                hasDeclaredNames(ImmutableList.of("Ethernet 1", "this is a comp\"licat'ed name")),
                hasMtu(1234))));

    assertThat(
        c,
        hasInterface(
            "Ethernet 9",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.2.1/24"))),
                isActive(false),
                hasDeclaredNames(ImmutableList.of("Ethernet 9", "baz")),
                hasMtu(DEFAULT_MTU))));

    assertThat(
        c,
        hasInterface(
            "Loopback 0",
            allOf(
                hasInterfaceType(InterfaceType.LOOPBACK),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("192.168.0.1/32"))),
                hasDeclaredNames(ImmutableList.of("Loopback 0")))));

    assertThat(
        c,
        hasInterface(
            "Loopback 10",
            allOf(
                hasInterfaceType(InterfaceType.LOOPBACK),
                hasSwitchPortMode(SwitchportMode.NONE),
                hasAllAddresses(empty()),
                hasDeclaredNames(ImmutableList.of("Loopback 10")))));
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
    assertThat(eths.keySet(), containsInAnyOrder(1, 2, 3));
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
    assertThat(
        trunkIface1.getMembers(), contains(new InterfaceReference(Interface.Type.ETHERNET, 1)));

    // Settings for the Static trunk
    assertThat(trunkIface2.getTrunkTypeEffective(), equalTo(TrunkGroup.Type.STATIC));
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
            "Ethernet 1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk 1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Trunk 1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet 1")))));
    assertThat(
        c.getAllInterfaces().get("Trunk 1").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet 1", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));

    // Default trunk
    assertThat(
        c,
        hasInterface(
            "Ethernet 2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk 2"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Ethernet 3",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk 2"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));
    assertThat(
        c,
        hasInterface(
            "Trunk 2",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet 2", "Ethernet 3")))));
    assertThat(
        c.getAllInterfaces().get("Trunk 2").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet 2", org.batfish.datamodel.Interface.DependencyType.AGGREGATE),
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet 3", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));
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
                WarningMatchers.hasText("Trunk 2 does not contain any member interfaces"))));
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
                hasComment("Cannot configure timeout for non-existent trunk-group"))));
  }

  @Test
  public void testTrunkConversion() {
    String hostname = "trunk_convert";
    Configuration c = parseConfig(hostname);

    assertThat(
        c,
        hasInterface(
            "Ethernet 1",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk 1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));

    assertThat(
        c,
        hasInterface(
            "Ethernet 2",
            allOf(
                hasInterfaceType(InterfaceType.PHYSICAL),
                hasChannelGroup("Trunk 1"),
                hasChannelGroupMembers(empty()),
                hasAllAddresses(empty()))));

    assertThat(
        c,
        hasInterface(
            "Trunk 1",
            allOf(
                hasInterfaceType(InterfaceType.AGGREGATED),
                hasChannelGroup(nullValue()),
                hasChannelGroupMembers(containsInAnyOrder("Ethernet 1", "Ethernet 2")),
                hasAllAddresses(contains(ConcreteInterfaceAddress.parse("10.0.1.1/24"))))));
    assertThat(
        c.getAllInterfaces().get("Trunk 1").getDependencies(),
        containsInAnyOrder(
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet 1", org.batfish.datamodel.Interface.DependencyType.AGGREGATE),
            new org.batfish.datamodel.Interface.Dependency(
                "Ethernet 2", org.batfish.datamodel.Interface.DependencyType.AGGREGATE)));
  }
}
