package org.batfish.grammar.cisco;

import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathIbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.main.BatfishTestUtils.getBatfishForTextConfigs;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CiscoNxosTest {
  private static final String TESTRIGS_PREFIX = "org/batfish/grammar/cisco/testrigs/";
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Configuration parseConfig(String hostname) {
    try {
      return parseTextConfigs(hostname).get(hostname.toLowerCase());
    } catch (IOException e) {
      throw new AssertionError("Failed to parse config " + hostname, e);
    }
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = getBatfishForTextConfigs(_folder, names);
    batfish.getSettings().setEnableCiscoNxParser(true);
    return batfish.loadConfigurations();
  }

  private Batfish getBatfishForTestrig(String testrig, String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames)
            .map(s -> Paths.get(TESTRIGS_PREFIX, testrig, "configs", s).toString())
            .toArray(String[]::new);
    Batfish batfish = getBatfishForTextConfigs(_folder, names);
    batfish.getSettings().setEnableCiscoNxParser(true);
    return batfish;
  }

  @Test
  public void testMaximumPaths() {
    Configuration c = parseConfig("nxosBgpMaximumPaths");
    assertThat(
        c,
        hasVrf("justibgp", hasBgpProcess(allOf(hasMultipathEbgp(false), hasMultipathIbgp(true)))));
    assertThat(
        c,
        hasVrf("justebgp", hasBgpProcess(allOf(hasMultipathEbgp(true), hasMultipathIbgp(false)))));
    assertThat(
        c, hasVrf("both", hasBgpProcess(allOf(hasMultipathEbgp(true), hasMultipathIbgp(true)))));
  }

  @Test
  public void testRouterId() {
    Configuration c = parseConfig("nxosBgpRouterId");
    // default VRF has manually set router id.
    assertThat(c, hasVrf("default", hasBgpProcess(hasRouterId(new Ip("4.4.4.4")))));
    // vrf1 has manually set router id.
    assertThat(c, hasVrf("vrf1", hasBgpProcess(hasRouterId(new Ip("2.3.1.4")))));
    // vrf2 has no configured router id, but there is an associated loopback.
    assertThat(c, hasVrf("vrf2", hasBgpProcess(hasRouterId(new Ip("1.1.1.1")))));
    // vrf3 has no configured router id and no interfaces. Cisco uses 0.0.0.0. Note that it does NOT
    // inherit from default VRF's manual config or pickup Loopback0 in another VRF.
    assertThat(c, hasVrf("vrf3", hasBgpProcess(hasRouterId(Ip.ZERO))));
    // vrf4 has loopback0.
    assertThat(c, hasVrf("vrf4", hasBgpProcess(hasRouterId(new Ip("1.2.3.4")))));
  }

  private GenericRib<AbstractRoute> parseDpAndGetRib(
      String testrigName, String hubName, String listenerName) throws IOException {

    Batfish batfish = getBatfishForTestrig(testrigName, hubName, listenerName);
    batfish.loadConfigurations();
    batfish.computeDataPlane(false); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane();

    return dp.getRibs().get(listenerName).get(Configuration.DEFAULT_VRF_NAME);
  }

  // Neighbor default-originate overrides outbound route map.
  @Test
  public void testDefaultOriginate() throws Exception {
    GenericRib<AbstractRoute> listenerRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-originate", "ios-bgp-listener");
    assertThat(listenerRib.getRoutes(), hasItem(hasPrefix(Prefix.ZERO)));
  }

  // static route and default-information originate, but no redistribute is not advertised.
  @Test
  public void testDefaultInformationOriginateNoRedistribute() throws Exception {
    GenericRib<AbstractRoute> listenerRib =
        parseDpAndGetRib("nxos-bgp-default-route", "nxos-bgp-default-inf-only", "ios-bgp-listener");
    assertThat(listenerRib.getRoutes(), not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // static route and redistribution, but no default-information originate is not advertised.
  @Test
  public void testStaticRedistributionNoDefaultInformationOriginate() throws Exception {
    GenericRib<AbstractRoute> listenerRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-static-redist-only", "ios-bgp-listener");
    assertThat(listenerRib.getRoutes(), not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // default-information originate, static route, redistribute. Outbound route maps are honored.
  @Test
  public void testDefaultInformationOriginateRedistribute() throws Exception {
    GenericRib<AbstractRoute> outboundAllowRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-inf-working", "ios-bgp-listener");
    assertThat(outboundAllowRib.getRoutes(), hasItem(hasPrefix(Prefix.ZERO)));

    GenericRib<AbstractRoute> outboundBlockRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-inf-working", "ios-bgp-listener-2");
    assertThat(outboundBlockRib.getRoutes(), not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // network, static route. Outbound route maps are honored.
  @Test
  public void testDefaultNetwork() throws Exception {
    GenericRib<AbstractRoute> outboundAllowRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-network-statement", "ios-bgp-listener");
    assertThat(outboundAllowRib.getRoutes(), hasItem(hasPrefix(Prefix.ZERO)));

    GenericRib<AbstractRoute> outboundBlockRib =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-network-statement", "ios-bgp-listener-2");
    assertThat(outboundBlockRib.getRoutes(), not(hasItem(hasPrefix(Prefix.ZERO))));
  }
}
