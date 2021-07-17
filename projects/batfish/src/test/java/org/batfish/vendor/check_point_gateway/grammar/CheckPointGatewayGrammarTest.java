package org.batfish.vendor.check_point_gateway.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.CHECK_POINT_GATEWAY;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
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
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
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
    assertThat(c.getHostname(), equalTo(hostname));
  }

  @Test
  public void testHostnameChassis() {
    String hostname = "hostname_chassis";
    CheckPointGatewayConfiguration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm the `%m` is replaced with a chassis identifier
    assertThat(c.getHostname(), equalTo("hostname_chassis-ch01-01"));
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
    assertFalse(eth1.getState());
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
}
