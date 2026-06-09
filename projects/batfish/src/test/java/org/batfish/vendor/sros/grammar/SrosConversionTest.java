package org.batfish.vendor.sros.grammar;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNoUndefinedReferences;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasRedFlagWarning;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.sros.representation.SrosStructureType;
import org.batfish.vendor.sros.representation.SrosStructureUsage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of SR-OS conversion (P5) from the typed feature model to the vendor-independent model. */
public final class SrosConversionTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  /** The captured r1 config converts to a vendor-independent {@link Configuration}. */
  @Test
  public void testR1Conversion() throws IOException {
    Configuration c = parseConfig("r1_admin_show_configuration.txt");
    assertThat(c.getConfigurationFormat().getVendorString(), equalTo("nokia_sros"));
    assertThat(c.getDeviceModel(), equalTo(DeviceModel.NOKIA_SROS_UNSPECIFIED));

    // The "Base" router instance is the default VRF.
    assertThat(c.getVrfs(), hasKey(Configuration.DEFAULT_VRF_NAME));

    // Interfaces: system (loopback, no port), the L3 router-interface to-r2 (LOGICAL, holds the
    // address), and its physical port 1/1/c1/1 (PHYSICAL, addressless, the Layer-1 endpoint).
    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder("system", "to-r2", "1/1/c1/1"));
    Interface system = c.getAllInterfaces().get("system");
    assertThat(system.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(system.getConcreteAddress().toString(), equalTo("1.1.1.1/32"));
    assertThat(system.getVrfName(), equalTo(Configuration.DEFAULT_VRF_NAME));
    assertTrue(system.getAdminUp());

    Interface toR2 = c.getAllInterfaces().get("to-r2");
    assertThat(toR2.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertThat(toR2.getConcreteAddress().toString(), equalTo("10.0.0.0/31"));
    // The L3 interface binds its physical port: a BIND dependency lets a user Layer-1 topology
    // (which names the port) drive this interface's L3 adjacency and fate. (P6.)
    assertThat(
        toR2.getDependencies(),
        contains(new Interface.Dependency("1/1/c1/1", Interface.DependencyType.BIND)));

    Interface port = c.getAllInterfaces().get("1/1/c1/1");
    assertThat(port.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(port.getConcreteAddress(), nullValue());
    assertTrue(port.getAdminUp());

    // SR-OS installs the connected route but not a local /32 host route for the interface IP;
    // the address metadata suppresses Batfish's local-route generation (P5-V finding).
    ConnectedRouteMetadata toR2Meta = toR2.getAddressMetadata().get(toR2.getConcreteAddress());
    assertThat(toR2Meta, not(nullValue()));
    assertThat(toR2Meta.getGenerateLocalRoute(), equalTo(Boolean.FALSE));
  }

  /** The prefix-list converts to a RouteFilterList with an exact-length permit line. */
  @Test
  public void testPrefixListConversion() throws IOException {
    Configuration c = parseConfig("r1_admin_show_configuration.txt");
    assertThat(c.getRouteFilterLists(), hasKey("system-pfx"));
    RouteFilterList rfl = c.getRouteFilterLists().get("system-pfx");
    // exact type: matches 1.1.1.1/32 exactly, not a more-specific (no more-specific exists at /32).
    assertTrue(rfl.permits(Prefix.parse("1.1.1.1/32")));
    assertFalse(rfl.permits(Prefix.parse("2.2.2.2/32")));
  }

  /**
   * The BGP process is on the default VRF; the single neighbor inherits its {@code peer-as} from
   * the group, is treated as eBGP, and is keyed by peer IP.
   */
  @Test
  public void testBgpNeighborGroupInheritance() throws IOException {
    Configuration c = parseConfig("r1_admin_show_configuration.txt");
    org.batfish.datamodel.BgpProcess proc = c.getDefaultVrf().getBgpProcess();
    assertNotNull(proc);
    assertThat(proc.getRouterId(), equalTo(Ip.parse("1.1.1.1")));

    Map<Ip, BgpActivePeerConfig> neighbors = proc.getActiveNeighbors();
    assertThat(neighbors, hasKey(Ip.parse("10.0.0.1")));
    BgpActivePeerConfig peer = neighbors.get(Ip.parse("10.0.0.1"));
    // peer-as 65002 inherited from group "ebgp"; local-as 65001 from the router instance.
    assertThat(peer.getRemoteAsns(), equalTo(org.batfish.datamodel.LongSpace.of(65002L)));
    assertThat(peer.getLocalAs(), equalTo(65001L));
    // local-ip is left unset: SR-OS auto-selects the source address per peer, and for a
    // directly-connected eBGP peer Batfish resolves it from the connected interface toward the
    // peer. Forcing the system address (1.1.1.1) here would put the local IP off the peering
    // subnet and the session would never establish (caught by lab validation, P5-V).
    assertThat(peer.getLocalIp(), nullValue());
    assertNotNull(peer.getIpv4UnicastAddressFamily());
  }

  /**
   * eBGP default-reject + policy semantics, evaluated behaviorally on the generated peer policies:
   *
   * <ul>
   *   <li>export: the group's {@code export-system} policy accepts only the system prefix
   *       (1.1.1.1/32); everything else is rejected by the eBGP default-reject backstop.
   *   <li>import: the group's {@code import-all} policy has a default-action accept, so all routes
   *       are accepted.
   * </ul>
   */
  @Test
  public void testEbgpDefaultRejectAndPolicies() throws IOException {
    Configuration c = parseConfig("r1_admin_show_configuration.txt");
    BgpActivePeerConfig peer =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("10.0.0.1"));

    RoutingPolicy exportPolicy =
        c.getRoutingPolicies().get(peer.getIpv4UnicastAddressFamily().getExportPolicy());
    assertNotNull(exportPolicy);
    // export-system accepts the system prefix...
    assertTrue(routeAccepted(exportPolicy, Prefix.parse("1.1.1.1/32"), Environment.Direction.OUT));
    // ...and rejects anything else (eBGP default-reject, since no policy entry matched).
    assertFalse(routeAccepted(exportPolicy, Prefix.parse("9.9.9.9/32"), Environment.Direction.OUT));

    RoutingPolicy importPolicy =
        c.getRoutingPolicies().get(peer.getIpv4UnicastAddressFamily().getImportPolicy());
    assertNotNull(importPolicy);
    // import-all has default-action accept, so any prefix is accepted.
    assertTrue(routeAccepted(importPolicy, Prefix.parse("9.9.9.9/32"), Environment.Direction.IN));
  }

  /**
   * iBGP default-accept (an iBGP group with no import/export policy, or whose policy falls through)
   * accepts BGP routes but does not pull connected/static routes into BGP. Verified behaviorally on
   * the generated peer policies of an iBGP neighbor:
   *
   * <ul>
   *   <li>import: a received BGP route is accepted by default (no import policy).
   *   <li>export: a BGP route is accepted, but a connected route that no explicit export policy
   *       matches is rejected by the default-accept backstop — so connected interface prefixes are
   *       not leaked into iBGP (confirmed against the L3 lab, where SR-OS advertised only its
   *       policy-matched system prefix and its BGP-learned routes, not its connected /31s).
   * </ul>
   */
  @Test
  public void testIbgpDefaultAcceptOnlyBgpRoutes() throws IOException {
    Configuration c = parseConfig("ibgp_default_accept.txt");
    BgpActivePeerConfig peer =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("10.0.1.1"));
    assertNotNull(peer);

    // import: no import policy on the iBGP group -> default-accept any received BGP route.
    RoutingPolicy importPolicy =
        c.getRoutingPolicies().get(peer.getIpv4UnicastAddressFamily().getImportPolicy());
    assertNotNull(importPolicy);
    assertTrue(
        bgpRouteAccepted(importPolicy, Prefix.parse("9.9.9.9/32"), Environment.Direction.IN));

    RoutingPolicy exportPolicy =
        c.getRoutingPolicies().get(peer.getIpv4UnicastAddressFamily().getExportPolicy());
    assertNotNull(exportPolicy);
    // export: a BGP route is accepted by the iBGP default-accept backstop...
    assertTrue(
        bgpRouteAccepted(exportPolicy, Prefix.parse("2.2.2.2/32"), Environment.Direction.OUT));
    // ...the explicit export-system policy still accepts the system prefix...
    assertTrue(
        connectedRouteAccepted(
            exportPolicy, Prefix.parse("1.1.1.1/32"), Environment.Direction.OUT));
    // ...but a connected route that no policy matches is NOT pulled into iBGP by default-accept.
    assertFalse(
        connectedRouteAccepted(
            exportPolicy, Prefix.parse("10.0.1.0/31"), Environment.Direction.OUT));
  }

  /**
   * Policy set-clauses convert and take effect when the entry matches: {@code metric set} sets the
   * MED, {@code as-path-prepend} prepends the AS the configured number of times, {@code community
   * add} unions the named community onto the route, and a {@code through} prefix-list converts to
   * an exact length window (not the old over-approximation).
   */
  @Test
  public void testPolicySetClausesConversion() throws IOException {
    Configuration c = parseConfig("policy_set_clauses.txt");

    // through-length 32 on a /16 -> exact window [16,32], so a /32 inside the block matches but a
    // shorter (e.g. /15) does not. (Previously over-approximated to [16,32-or-longer] with a warn.)
    RouteFilterList loRange = c.getRouteFilterLists().get("lo-range");
    assertNotNull(loRange);
    assertTrue(loRange.permits(Prefix.parse("192.168.1.0/24")));
    assertTrue(loRange.permits(Prefix.parse("192.168.1.1/32")));
    assertFalse(loRange.permits(Prefix.parse("192.0.0.0/15")));

    // range start-length 24 end-length 32 on 10.0.0.0/8 -> window [24,32].
    RouteFilterList hostRange = c.getRouteFilterLists().get("host-range");
    assertNotNull(hostRange);
    assertTrue(hostRange.permits(Prefix.parse("10.1.2.0/24")));
    assertTrue(hostRange.permits(Prefix.parse("10.1.2.3/32")));
    assertFalse(hostRange.permits(Prefix.parse("10.1.0.0/16")));

    // entry 10 matches 1.1.1.1/32 (system-pfx) and applies metric 50 + prepend 65001 x2 +
    // community.
    RoutingPolicy exportPolicy = c.getRoutingPolicies().get("export-to-r2");
    assertNotNull(exportPolicy);
    Bgpv4Route in =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(org.batfish.datamodel.OriginType.IGP)
            .setProtocol(org.batfish.datamodel.RoutingProtocol.BGP)
            .build();
    Bgpv4Route.Builder out = in.toBuilder();
    boolean permitted = exportPolicy.process(in, out, Environment.Direction.OUT);
    assertTrue(permitted);
    Bgpv4Route result = out.build();
    assertThat(result.getMetric(), equalTo(50L));
    // as-path: 65001 prepended twice.
    assertThat(
        result.getAsPath(),
        equalTo(org.batfish.datamodel.AsPath.ofSingletonAsSets(65001L, 65001L)));
    // community 65001:100 added.
    assertThat(
        result.getCommunities().getCommunities(),
        org.hamcrest.Matchers.hasItem(
            org.batfish.datamodel.bgp.community.StandardCommunity.parse("65001:100")));
  }

  /**
   * Hardware (cards/ports) is parsed but not converted; conversion emits a red-flag warning rather
   * than silently dropping it.
   */
  @Test
  public void testHardwareConversionWarning() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "r1_admin_show_configuration.txt");
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(ccae, hasRedFlagWarning("r1", containsString("hardware provisioning")));
  }

  /**
   * The captured r1 config records its named structures with their definition lines, and the
   * references that resolve (no undefined references): the BGP group {@code ebgp}, the prefix-list
   * {@code system-pfx}, and the policy-statements are all defined and referenced.
   */
  @Test
  public void testStructureDefinitionsAndReferences() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "r1_admin_show_configuration.txt");
    String filename = "configs/r1_admin_show_configuration.txt";
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // The prefix-list is defined and referenced once (by export-system's entry 10).
    assertThat(ccae, hasDefinedStructure(filename, SrosStructureType.PREFIX_LIST, "system-pfx"));
    assertThat(ccae, hasNumReferrers(filename, SrosStructureType.PREFIX_LIST, "system-pfx", 1));
    // The BGP group is defined and referenced once (by neighbor 10.0.0.1).
    assertThat(ccae, hasDefinedStructure(filename, SrosStructureType.BGP_GROUP, "ebgp"));
    assertThat(ccae, hasNumReferrers(filename, SrosStructureType.BGP_GROUP, "ebgp", 1));
    // Both policy-statements are defined.
    assertThat(
        ccae, hasDefinedStructure(filename, SrosStructureType.POLICY_STATEMENT, "export-system"));
    assertThat(
        ccae, hasDefinedStructure(filename, SrosStructureType.POLICY_STATEMENT, "import-all"));
    // No dangling references in a well-formed config.
    assertThat(ccae, hasNoUndefinedReferences());
  }

  /**
   * A multi-line brace block records more than one source line as the structure's definition lines
   * (the {@code export-system} policy-statement is a multi-line brace block in r1).
   */
  @Test
  public void testStructureDefinitionSpansBraceBlockLines() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "r1_admin_show_configuration.txt");
    String filename = "configs/r1_admin_show_configuration.txt";
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    // The definition spans more than one line (a brace block), not a single line.
    assertThat(
        ccae,
        hasDefinedStructureWithDefinitionLines(
            filename,
            SrosStructureType.POLICY_STATEMENT,
            "export-system",
            hasSize(greaterThan(1))));
  }

  /** A reference to a non-existent group/prefix-list is reported as an undefined reference. */
  @Test
  public void testUndefinedReferences() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "undefined_references.txt");
    String filename = "configs/undefined_references.txt";
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            SrosStructureType.BGP_GROUP,
            "missing-group",
            SrosStructureUsage.BGP_NEIGHBOR_GROUP));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename,
            SrosStructureType.PREFIX_LIST,
            "missing-pfx",
            SrosStructureUsage.POLICY_STATEMENT_FROM_PREFIX_LIST));
  }

  /** A prefix-list defined but never referenced is recorded with zero referrers (unused). */
  @Test
  public void testUnusedStructure() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "unused_structure.txt");
    String filename = "configs/unused_structure.txt";
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(ccae, hasDefinedStructure(filename, SrosStructureType.PREFIX_LIST, "unused-pfx"));
    assertThat(ccae, hasNumReferrers(filename, SrosStructureType.PREFIX_LIST, "unused-pfx", 0));
  }

  /**
   * A prefix-list match type whose bounds are not modeled (through/range/to/address-mask) converts
   * as an over-approximation and emits a warning rather than silently broadening the filter.
   */
  @Test
  public void testUnmodeledPrefixTypeWarns() throws IOException {
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(
            _folder, TESTCONFIGS_PREFIX + "bgp_type_and_inheritance.txt");
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasRedFlagWarning("bgp-type-and-inheritance", containsString("is not fully modeled")));
  }

  /**
   * static-routes conversion: an admin-state-enable next-hop route and a blackhole route become VI
   * {@link org.batfish.datamodel.StaticRoute}s (NextHopIp / NextHopDiscard) with the SR-OS
   * preference as admin distance and the metric; a route whose next-hop has no admin-state is NOT
   * installed (matching the device).
   */
  @Test
  public void testStaticRoutesConversion() throws IOException {
    Configuration c = parseConfig("static_routes.txt");
    Map<Prefix, org.batfish.datamodel.StaticRoute> byPrefix =
        c.getDefaultVrf().getStaticRoutes().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    org.batfish.datamodel.AbstractRoute::getNetwork, r -> r));

    // The next-hop route (default preference 5 / metric 1).
    org.batfish.datamodel.StaticRoute nh = byPrefix.get(Prefix.parse("192.0.2.0/24"));
    assertNotNull(nh);
    assertThat(
        nh.getNextHop(),
        equalTo(org.batfish.datamodel.route.nh.NextHopIp.of(Ip.parse("10.0.0.1"))));
    assertThat(nh.getAdministrativeCost(), equalTo(5L));
    assertThat(nh.getMetric(), equalTo(1L));

    // The blackhole route -> discard next-hop.
    org.batfish.datamodel.StaticRoute bh = byPrefix.get(Prefix.parse("198.51.100.0/24"));
    assertNotNull(bh);
    assertThat(bh.getNextHop(), equalTo(org.batfish.datamodel.route.nh.NextHopDiscard.instance()));

    // The next-hop route with explicit preference 100 / metric 50.
    org.batfish.datamodel.StaticRoute nhPref = byPrefix.get(Prefix.parse("203.0.113.0/24"));
    assertNotNull(nhPref);
    assertThat(nhPref.getAdministrativeCost(), equalTo(100L));
    assertThat(nhPref.getMetric(), equalTo(50L));

    // The route whose next-hop has no admin-state is not installed (SR-OS leaves it out of the
    // RIB).
    assertThat(byPrefix, not(hasKey(Prefix.parse("100.64.0.0/24"))));
  }

  private @Nonnull Configuration parseConfig(String filename) throws IOException {
    SortedMap<String, Configuration> configs =
        BatfishTestUtils.parseTextConfigs(_folder, TESTCONFIGS_PREFIX + filename);
    assertThat(configs.size(), equalTo(1));
    return configs.values().iterator().next();
  }

  private static boolean routeAccepted(
      RoutingPolicy policy, Prefix network, Environment.Direction direction) {
    return bgpRouteAccepted(policy, network, direction);
  }

  /** Whether {@code policy} accepts a BGP route for {@code network}. */
  private static boolean bgpRouteAccepted(
      RoutingPolicy policy, Prefix network, Environment.Direction direction) {
    Bgpv4Route route =
        Bgpv4Route.testBuilder().setNetwork(network).setOriginatorIp(Ip.parse("1.1.1.1")).build();
    return policy.process(route, route.toBuilder(), direction);
  }

  /**
   * Whether {@code policy} accepts a <em>connected</em> route for {@code network} (used to confirm
   * the iBGP default-accept backstop does not pull non-BGP routes into BGP). The output builder is
   * a BGP route builder, as in an export context.
   */
  private static boolean connectedRouteAccepted(
      RoutingPolicy policy, Prefix network, Environment.Direction direction) {
    org.batfish.datamodel.ConnectedRoute route =
        new org.batfish.datamodel.ConnectedRoute(network, "iface");
    return policy.process(
        route,
        Bgpv4Route.testBuilder().setNetwork(network).setOriginatorIp(Ip.parse("1.1.1.1")),
        direction);
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";
}
