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
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasPassiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasDefaultVrf;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasVrf;
import static org.batfish.datamodel.matchers.VrfMatchers.hasBgpProcess;
import static org.batfish.main.BatfishTestUtils.getBatfishForTextConfigs;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
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
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  @Test
  public void testDynamicRouteMap() throws IOException {
    Configuration c = parseConfig("nxos-bgp-dynamic-route-map");
    assertThat(
        c,
        hasDefaultVrf(
            hasBgpProcess(
                hasPassiveNeighbor(
                    Prefix.parse("1.2.3.0/24"), hasRemoteAs(LongSpace.parse("1-20,101-120"))))));
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

  /**
   * Tests undefined route-map behavior in a simple 3-node IBGP network.
   *
   * <p>The sender is the hub and there are receivers 1 and 2. Sender originates 1.1.1.1/32, R1
   * originates 2.2.2.2/32 and R2 originates 3.3.3.3/32. The configuration on R1 and R2 is trivial
   * and just sets up an IBGP link with no filtering.
   *
   * <p>The sender used undefined export/import maps to R1, and defined export/import maps to R2
   * that permit anything.
   *
   * <p>Expected behavior: 1) R1 learns no bgp routes (nothing is exported), nor does S learn the
   * route it originates (nothing is imported). 2) R2 learns 1.1.1.1/32, and S learns 3.3.3.3/32.
   */
  @Test
  public void testRouteMapUndefined() throws IOException {
    Set<AbstractRoute> undefined =
        parseDpAndGetRib(
                "nxos-bgp-route-map-undefined",
                "nxos-bgp-receiver-1",
                "nxos-bgp-sender",
                "nxos-bgp-receiver-2")
            .stream()
            .filter(r -> r instanceof Bgpv4Route)
            .collect(Collectors.toSet());
    // undefined export map -> no bgp routes on that receiver-1
    assertThat(undefined, empty());
    Set<AbstractRoute> defined =
        parseDpAndGetRib(
                "nxos-bgp-route-map-undefined",
                "nxos-bgp-receiver-2",
                "nxos-bgp-sender",
                "nxos-bgp-receiver-1")
            .stream()
            .filter(r -> r instanceof Bgpv4Route)
            .collect(Collectors.toSet());
    // defined export map -> the network generated locally is sent to receiver-2
    assertThat(
        defined,
        contains(allOf(instanceOf(Bgpv4Route.class), hasPrefix(Prefix.parse("1.1.1.1/32")))));
    Set<AbstractRoute> sender =
        parseDpAndGetRib(
                "nxos-bgp-route-map-undefined",
                "nxos-bgp-sender",
                "nxos-bgp-receiver-1",
                "nxos-bgp-receiver-2")
            .stream()
            .filter(r -> r instanceof Bgpv4Route)
            .collect(Collectors.toSet());
    // undefined import map -> network generated locally on receiver-1 is not present (2.2.2.2/32)
    // defined import map -> network generated locally on receiver-2 is present (3.3.3.3/32)
    assertThat(
        sender,
        contains(allOf(instanceOf(Bgpv4Route.class), hasPrefix(Prefix.parse("3.3.3.3/32")))));
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

  @Test
  public void testUpdateSourceShutdown() throws IOException {
    Configuration c = parseConfig("nxos-bgp-update-source-shutdown");
    BgpProcess p = c.getDefaultVrf().getBgpProcess();
    assertThat(p, notNullValue());
    BgpActivePeerConfig neighbor = p.getActiveNeighbors().get(Prefix.parse("1.2.3.5/32"));
    assertThat(neighbor, notNullValue());
    assertThat(neighbor.getLocalIp(), equalTo(Ip.parse("1.2.3.4")));
  }

  private Batfish getBatfishForSnapshot(String snapshot, Collection<String> configurationNames)
      throws IOException {
    String[] names =
        configurationNames.stream()
            .map(s -> Paths.get(TEST_SNAPSHOTS_PREFIX, snapshot, "configs", s).toString())
            .toArray(String[]::new);
    Batfish batfish = getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private Set<AbstractRoute> parseDpAndGetRib(
      String snapshotName, String listenerName, String... otherDevices) throws IOException {

    List<String> allConfigs =
        ImmutableList.<String>builder()
            .add(listenerName)
            .addAll(Arrays.asList(otherDevices))
            .build();
    Batfish batfish = getBatfishForSnapshot(snapshotName, allConfigs);
    batfish.loadConfigurations(batfish.getSnapshot());
    batfish.computeDataPlane(batfish.getSnapshot()); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());

    return dp.getRibs().get(listenerName).get(Configuration.DEFAULT_VRF_NAME).getRoutes();
  }

  // Neighbor default-originate overrides outbound route map.
  @Test
  public void testDefaultOriginate() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener", "nxos-bgp-default-originate");
    assertThat(listenerRoutes, hasItem(hasPrefix(Prefix.ZERO)));
  }

  // static route and default-information originate, but no redistribute is not advertised.
  @Test
  public void testDefaultInformationOriginateNoRedistribute() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib("nxos-bgp-default-route", "ios-bgp-listener", "nxos-bgp-default-inf-only");
    assertThat(listenerRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // static route and redistribution, but no default-information originate is not advertised.
  @Test
  public void testStaticRedistributionNoDefaultInformationOriginate() throws Exception {
    Set<AbstractRoute> listenerRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener", "nxos-bgp-static-redist-only");
    assertThat(listenerRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // default-information originate, static route, redistribute. Outbound route maps are honored.
  @Test
  public void testDefaultInformationOriginateRedistribute() throws Exception {
    Set<AbstractRoute> outboundAllowRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener", "nxos-bgp-default-inf-working");
    assertThat(outboundAllowRoutes, hasItem(hasPrefix(Prefix.ZERO)));

    Set<AbstractRoute> outboundBlockRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener-2", "nxos-bgp-default-inf-working");
    assertThat(outboundBlockRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }

  // network, static route. Outbound route maps are honored.
  @Test
  public void testDefaultNetwork() throws Exception {
    Set<AbstractRoute> outboundAllowRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener", "nxos-bgp-network-statement");
    assertThat(outboundAllowRoutes, hasItem(hasPrefix(Prefix.ZERO)));

    Set<AbstractRoute> outboundBlockRoutes =
        parseDpAndGetRib(
            "nxos-bgp-default-route", "ios-bgp-listener-2", "nxos-bgp-network-statement");
    assertThat(outboundBlockRoutes, not(hasItem(hasPrefix(Prefix.ZERO))));
  }
}
