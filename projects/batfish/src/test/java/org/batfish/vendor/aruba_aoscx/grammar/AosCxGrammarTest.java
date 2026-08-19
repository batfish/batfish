package org.batfish.vendor.aruba_aoscx.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.datamodel.ConfigurationFormat.ARUBA_AOSCX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class AosCxGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testHostnameExtraction() {
    AosCxConfiguration vc = parseVendorConfig("aoscx-hostname");
    assertThat(vc.getHostname(), equalTo("ellx-dr-01"));
  }

  @Test
  public void testInterfaceExtraction() {
    AosCxConfiguration vc = parseVendorConfig("aoscx-interfaces");

    AosCxInterface physical = vc.getInterfaces().get("1/1/2");
    assertThat(physical.getEnabled(), equalTo(true));
    assertThat(
        physical.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("10.255.1.2/30")));

    AosCxInterface loopback = vc.getInterfaces().get("loopback 0");
    assertThat(
        loopback.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("129.237.1.41/32")));

    AosCxInterface vlan = vc.getInterfaces().get("vlan 1000");
    assertThat(vlan.getEnabled(), equalTo(false));
    assertThat(
        vlan.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("129.237.2.137/30")));
  }

  @Test
  public void testStaticRouteExtraction() {
    AosCxConfiguration vc = parseVendorConfig("aoscx-static-routes");

    assertThat(vc.getStaticRoutes().size(), equalTo(5));

    AosCxStaticRoute r0 = vc.getStaticRoutes().get(0);
    assertThat(
        r0.getPrefix(),
        equalTo(org.batfish.datamodel.Prefix.parse("0.0.0.0/0")));
    assertThat(r0.getNextHopType(), equalTo(NextHopType.IP));
    assertThat(r0.getNextHop(), equalTo("10.255.1.1"));

    AosCxStaticRoute r1 = vc.getStaticRoutes().get(1);
    assertThat(
        r1.getPrefix(),
        equalTo(org.batfish.datamodel.Prefix.parse("192.0.2.0/24")));
    assertThat(r1.getNextHopType(), equalTo(NextHopType.IP));
    assertThat(r1.getNextHop(), equalTo("10.255.1.5"));

    AosCxStaticRoute r2 = vc.getStaticRoutes().get(2);
    assertThat(
        r2.getPrefix(),
        equalTo(org.batfish.datamodel.Prefix.parse("192.0.2.0/24")));
    assertThat(r2.getNextHopType(), equalTo(NextHopType.INTERFACE));
    assertThat(r2.getNextHop(), equalTo("1/1/2"));

    AosCxStaticRoute r3 = vc.getStaticRoutes().get(3);
    assertThat(r3.getNextHopType(), equalTo(NextHopType.NULL_ROUTE));

    AosCxStaticRoute r4 = vc.getStaticRoutes().get(4);
    assertThat(r4.getNextHopType(), equalTo(NextHopType.REJECT));
  }


  @Test
  public void testOspfExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-ospf");

    assertThat(c.getOspfProcesses(), hasKey(1));
    assertThat(c.getOspfProcesses().get(1).getRouterId(), equalTo(Ip.parse("129.237.1.41")));

    AosCxInterface iface = c.getInterfaces().get("1/1/2");
    assertThat(iface, notNullValue());
    assertThat(iface.getBandwidth(), equalTo(1_000_000_000D));
    assertThat(iface.getOspfProcessId(), equalTo(1));
    assertThat(iface.getOspfArea(), equalTo("0.0.0.0"));
    assertThat(
        iface.getOspfNetworkType(),
        equalTo(AosCxInterface.OspfNetworkType.POINT_TO_POINT));
  }



  @Test
  public void testBgpExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-bgp");

    assertThat(c.getBgpProcess(), notNullValue());
    assertThat(c.getBgpProcess().getLocalAs(), equalTo(65000L));
    assertThat(c.getBgpProcess().getRouterId(), equalTo(Ip.parse("129.237.1.41")));
    assertThat(c.getBgpProcess().getNeighbors(), hasKey(Ip.parse("10.255.1.1")));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getRemoteAs(),
        equalTo(65001L));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getIpv4UnicastActive(),
        equalTo(true));
  }


  @Test
  public void testBgpConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-bgp");
    Configuration c = configs.get("ellx-dr-01");

    assertThat(c, notNullValue());
    assertThat(c.getDefaultVrf().getBgpProcess(), notNullValue());

    BgpProcess process = c.getDefaultVrf().getBgpProcess();
    assertThat(process.getRouterId(), equalTo(Ip.parse("129.237.1.41")));
    assertThat(process.getActiveNeighbors(), hasKey(Ip.parse("10.255.1.1")));

    BgpActivePeerConfig peer =
        process.getActiveNeighbors().get(Ip.parse("10.255.1.1"));

    assertThat(peer.getLocalAs(), equalTo(65000L));
    assertThat(peer.getRemoteAsns().contains(65001L), equalTo(true));
    assertThat(peer.getIpv4UnicastAddressFamily(), notNullValue());
  }

  @Test
  public void testOspfConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-ospf");
    Configuration c = configs.get("ellx-dr-01");

    assertThat(c, notNullValue());
    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKey("1"));

    org.batfish.datamodel.ospf.OspfProcess process =
        c.getDefaultVrf().getOspfProcesses().get("1");

    assertThat(process.getRouterId(), equalTo(Ip.parse("129.237.1.41")));
    assertThat(process.getAreas(), hasKey(0L));
    assertThat(process.getAreas().get(0L).getInterfaces(), hasItem("1/1/2"));

    org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("1/1/2");
    assertThat(iface.getOspfSettings(), notNullValue());
    assertThat(iface.getOspfSettings().getAreaName(), equalTo(0L));
    assertThat(iface.getOspfSettings().getProcess(), equalTo("1"));
    assertThat(iface.getOspfSettings().getNetworkType(), equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(iface.getOspfSettings().getHelloInterval(), equalTo(10));
    assertThat(iface.getOspfSettings().getDeadInterval(), equalTo(40));
  }

  @Test
  public void testStaticRouteConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-static-routes");
    Configuration c = configs.get("ellx-dr-01");

    assertThat(c, notNullValue());
    assertThat(
        c.getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopIp.of(Ip.parse("10.255.1.1")))
                .setAdministrativeCost(1)
                .setRecursive(true)
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("192.0.2.0/24"))
                .setNextHop(NextHopIp.of(Ip.parse("10.255.1.5")))
                .setAdministrativeCost(1)
                .setRecursive(true)
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("192.0.2.0/24"))
                .setNextHop(NextHopInterface.of("1/1/2"))
                .setAdministrativeCost(1)
                .setRecursive(false)
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("198.51.100.0/24"))
                .setNextHop(NextHopDiscard.instance())
                .setAdministrativeCost(1)
                .setRecursive(false)
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("203.0.113.0/24"))
                .setNextHop(NextHopDiscard.instance())
                .setAdministrativeCost(1)
                .setRecursive(false)
                .build()));
  }

  @Test
  public void testInterfaceConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-interfaces");
    Configuration c = configs.get("ellx-dr-01");

    assertThat(c, notNullValue());
    assertThat(c.getConfigurationFormat(), equalTo(ARUBA_AOSCX));

    org.batfish.datamodel.Interface physical = c.getAllInterfaces().get("1/1/2");
    assertThat(physical, notNullValue());
    assertThat(physical.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(physical.getAdminUp(), equalTo(true));
    assertThat(
        physical.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("10.255.1.2/30")));

    org.batfish.datamodel.Interface loopback =
        c.getAllInterfaces().get("loopback 0");
    assertThat(loopback, notNullValue());
    assertThat(loopback.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(
        loopback.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("129.237.1.41/32")));

    org.batfish.datamodel.Interface vlan =
        c.getAllInterfaces().get("vlan 1000");
    assertThat(vlan, notNullValue());
    assertThat(vlan.getInterfaceType(), equalTo(InterfaceType.VLAN));
    assertThat(vlan.getAdminUp(), equalTo(false));
    assertThat(
        vlan.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("129.237.2.137/30")));
  }

  private IBatfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames)
            .map(s -> TESTCONFIGS_PREFIX + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigsAndConversionContext(
        _folder, new ConversionContext(), names);
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish batfish = getBatfishForConfigurationNames(configurationNames);
    return batfish.loadConfigurations(batfish.getSnapshot());
  }

  private AosCxConfiguration parseVendorConfig(String filename) {
    String src = readResource(TESTCONFIGS_PREFIX + filename, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);

    AosCxCombinedParser parser = new AosCxCombinedParser(src, settings);
    Warnings warnings = new Warnings();

    AosCxControlPlaneExtractor extractor =
        new AosCxControlPlaneExtractor(
            src, parser, warnings, new SilentSyntaxCollection());

    ParserRuleContext tree =
        Batfish.parse(
            parser,
            new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false),
            settings);

    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);

    AosCxConfiguration vc =
        (AosCxConfiguration) extractor.getVendorConfiguration();

    vc.setWarnings(warnings);
    return vc;
  }

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/vendor/aruba_aoscx/grammar/testconfigs/";
}
