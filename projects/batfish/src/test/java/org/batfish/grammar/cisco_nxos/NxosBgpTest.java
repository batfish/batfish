package org.batfish.grammar.cisco_nxos;

import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.PATH_LENGTH;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasAllowRemoteAsOut;
import static org.batfish.datamodel.matchers.AddressFamilyMatchers.hasAddressFamilyCapabilites;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasIpv4UnicastAddressFamily;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEquivalentAsPathMatchMode;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathIbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.main.BatfishTestUtils.getBatfishForTextConfigs;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.matchers.ConfigurationMatchers;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NxosBgpTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco_nxos/bgp/";
  private static final String TEST_SNAPSHOTS_PREFIX =
      "org/batfish/grammar/cisco_nxos/bgp/snapshots/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.peekNetworkSnapshotStack());
  }

  @Test
  public void testMaximumPaths() throws IOException {
    Configuration c = parseConfig("nxos-bgp-maximum-paths");
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
  public void testMultipathAsPathRelax() throws IOException {
    Configuration c = parseConfig("nxos-bgp-multipath-relax");
    assertThat(
        c, hasVrf("default", hasBgpProcess(hasMultipathEquivalentAsPathMatchMode(EXACT_PATH))));
    assertThat(
        c, hasVrf("enabled", hasBgpProcess(hasMultipathEquivalentAsPathMatchMode(PATH_LENGTH))));
  }

  @Test
  public void testNxosBgpVrf() throws IOException {
    Configuration c = parseConfig("nxos-bgp-vrf");
    assertThat(c, ConfigurationMatchers.hasVrf("bar", any(Vrf.class)));
    assertThat(c.getVrfs().get("bar").getBgpProcess().getActiveNeighbors().values(), hasSize(2));
    assertThat(
        c,
        ConfigurationMatchers.hasVrf(
            "bar",
            hasBgpProcess(
                hasActiveNeighbor(
                    Prefix.parse("2.2.2.2/32"),
                    allOf(
                        hasRemoteAs(2L),
                        hasLocalAs(1L),
                        hasIpv4UnicastAddressFamily(
                            hasAddressFamilyCapabilites(hasAllowRemoteAsOut(true))))))));
    assertThat(
        c,
        ConfigurationMatchers.hasVrf(
            "bar",
            hasBgpProcess(
                hasActiveNeighbor(
                    Prefix.parse("3.3.3.3/32"),
                    hasIpv4UnicastAddressFamily(
                        hasAddressFamilyCapabilites(hasAllowRemoteAsOut(false)))))));
  }

  @Test
  public void testRouterId() throws IOException {
    Configuration c = parseConfig("nxos-bgp-router-id");
    // default VRF has manually set router id.
    assertThat(c, hasVrf("default", hasBgpProcess(hasRouterId(Ip.parse("4.4.4.4")))));
    // vrf1 has manually set router id.
    assertThat(c, hasVrf("vrf1", hasBgpProcess(hasRouterId(Ip.parse("2.3.1.4")))));
    // vrf2 has no configured router id, but there is an associated loopback.
    assertThat(c, hasVrf("vrf2", hasBgpProcess(hasRouterId(Ip.parse("1.1.1.1")))));
    // vrf3 has no configured router id and no interfaces. Cisco uses 0.0.0.0. Note that it does NOT
    // inherit from default VRF's manual config or pickup Loopback0 in another VRF.
    assertThat(c, hasVrf("vrf3", hasBgpProcess(hasRouterId(Ip.ZERO))));
    // vrf4 has loopback0.
    assertThat(c, hasVrf("vrf4", hasBgpProcess(hasRouterId(Ip.parse("1.2.3.4")))));
  }

  private Batfish getBatfishForSnapshot(String snapshot, String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames)
            .map(s -> Paths.get(TEST_SNAPSHOTS_PREFIX, snapshot, "configs", s).toString())
            .toArray(String[]::new);
    Batfish batfish = getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private Set<AbstractRoute> parseDpAndGetRib(
      String snapshotName, String hubName, String listenerName) throws IOException {

    Batfish batfish = getBatfishForSnapshot(snapshotName, hubName, listenerName);
    batfish.loadConfigurations(batfish.peekNetworkSnapshotStack());
    batfish.computeDataPlane(); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane();

    return dp.getRibs().get(listenerName).get(Configuration.DEFAULT_VRF_NAME).getRoutes();
  }

  // Neighbor default-originate overrides outbound route map.
  @Test
  public void testDefaultOriginate() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-originate", "ios-bgp-listener");
    assertThat(listenerRoutes, hasItem(hasPrefix(Prefix.ZERO)));
  }

  // static route and default-information originate, but no redistribute is not advertised.
  @Test
  public void testDefaultInformationOriginateNoRedistribute() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib("nxos-bgp-default-route", "nxos-bgp-default-inf-only", "ios-bgp-listener");
    assertThat(listenerRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // static route and redistribution, but no default-information originate is not advertised.
  @Test
  public void testStaticRedistributionNoDefaultInformationOriginate() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-static-redist-only", "ios-bgp-listener");
    assertThat(listenerRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // default-information originate, static route, redistribute. Outbound route maps are honored.
  @Test
  public void testDefaultInformationOriginateRedistribute() throws Exception {
    Set<AbstractRoute> outboundAllowRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-inf-working", "ios-bgp-listener");
    assertThat(outboundAllowRoutes, hasItem(hasPrefix(Prefix.ZERO)));

    Set<AbstractRoute> outboundBlockRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-default-inf-working", "ios-bgp-listener-2");
    assertThat(outboundBlockRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // network, static route. Outbound route maps are honored.
  @Test
  public void testDefaultNetwork() throws Exception {
    Set<AbstractRoute> outboundAllowRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-network-statement", "ios-bgp-listener");
    assertThat(outboundAllowRoutes, hasItem(hasPrefix(Prefix.ZERO)));

    Set<AbstractRoute> outboundBlockRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "nxos-bgp-network-statement", "ios-bgp-listener-2");
    assertThat(outboundBlockRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }
}
