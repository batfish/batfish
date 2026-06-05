package org.batfish.vendor.sros.grammar;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasRedFlagWarning;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
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

    // Interfaces: system (loopback, no port) and to-r2 (physical, port-bound).
    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder("system", "to-r2"));
    Interface system = c.getAllInterfaces().get("system");
    assertThat(system.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(system.getConcreteAddress().toString(), equalTo("1.1.1.1/32"));
    assertThat(system.getVrfName(), equalTo(Configuration.DEFAULT_VRF_NAME));
    assertTrue(system.getAdminUp());
    Interface toR2 = c.getAllInterfaces().get("to-r2");
    assertThat(toR2.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(toR2.getConcreteAddress().toString(), equalTo("10.0.0.0/31"));

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
    // r1 has no `system name`, so the hostname is guessed from the filename.
    assertThat(
        ccae,
        hasRedFlagWarning(
            "r1_admin_show_configuration.txt", containsString("hardware provisioning")));
  }

  private @Nonnull Configuration parseConfig(String filename) throws IOException {
    SortedMap<String, Configuration> configs =
        BatfishTestUtils.parseTextConfigs(_folder, TESTCONFIGS_PREFIX + filename);
    assertThat(configs.size(), equalTo(1));
    return configs.values().iterator().next();
  }

  private static boolean routeAccepted(
      RoutingPolicy policy, Prefix network, Environment.Direction direction) {
    Bgpv4Route route =
        Bgpv4Route.testBuilder().setNetwork(network).setOriginatorIp(Ip.parse("1.1.1.1")).build();
    return policy.process(route, route.toBuilder(), direction);
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/sros/grammar/testconfigs/";
}
