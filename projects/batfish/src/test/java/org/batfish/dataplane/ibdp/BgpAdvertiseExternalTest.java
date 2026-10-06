package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.List;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * A session advertising external keeps advertising the best eBGP path while another path is the
 * overall best.
 */
public final class BgpAdvertiseExternalTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final Prefix PREFIX = Prefix.parse("192.0.2.0/24");
  private static final long AS_INTERNAL = 65000L;

  private final NetworkFactory _nf = new NetworkFactory();

  private Configuration node(String hostname) {
    return _nf.configurationBuilder()
        .setHostname(hostname)
        .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
        .build();
  }

  private Vrf vrf(Configuration c) {
    return _nf.vrfBuilder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
  }

  private BgpProcess bgpProcess(Vrf vrf, String routerId) {
    return _nf.bgpProcessBuilder()
        .setRouterId(Ip.parse(routerId))
        .setVrf(vrf)
        .setEbgpAdminCost(20)
        .setIbgpAdminCost(200)
        .setLocalAdminCost(200)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .build();
  }

  private void iface(Configuration c, Vrf vrf, String address) {
    _nf.interfaceBuilder()
        .setOwner(c)
        .setVrf(vrf)
        .setAddress(ConcreteInterfaceAddress.parse(address))
        .build();
  }

  private String policy(Configuration c, Statement... statements) {
    return _nf.routingPolicyBuilder()
        .setOwner(c)
        .setStatements(ImmutableList.copyOf(statements))
        .build()
        .getName();
  }

  private static Statement accept() {
    return Statements.ExitAccept.toStaticStatement();
  }

  private void neighbor(
      BgpProcess proc,
      String localIp,
      String peerIp,
      long localAs,
      long remoteAs,
      String importPolicy,
      String exportPolicy,
      boolean advertiseExternal) {
    _nf.bgpNeighborBuilder()
        .setBgpProcess(proc)
        .setLocalIp(Ip.parse(localIp))
        .setPeerAddress(Ip.parse(peerIp))
        .setLocalAs(localAs)
        .setRemoteAs(remoteAs)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setImportPolicy(importPolicy)
                .setExportPolicy(exportPolicy)
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setAdvertiseExternal(advertiseExternal)
                        .build())
                .build())
        .build();
  }

  /** An eBGP speaker originating {@link #PREFIX} toward {@code peerIp}. */
  private Configuration originator(
      String hostname, long as, String address, String localIp, String peerIp) {
    Configuration c = node(hostname);
    Vrf v = vrf(c);
    iface(c, v, address);
    v.getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(PREFIX)
                .setNextHop(NextHopDiscard.instance())
                .setAdministrativeCost(1)
                .build());
    neighbor(
        bgpProcess(v, localIp), localIp, peerIp, as, AS_INTERNAL, null, policy(c, accept()), false);
    return c;
  }

  /**
   * r2 learns {@link #PREFIX} over eBGP from r1, then a path it prefers over iBGP from r3, which
   * learned it from r6 one hop further away. r2 advertises external to r4 and reflects nothing, so
   * r4 can learn the prefix only as r2's best eBGP path.
   */
  private List<String> pathsAtR4(boolean r6Originates) throws IOException {
    Configuration r1 = originator("r1", 65001L, "10.0.12.0/31", "10.0.12.0", "10.0.12.1");
    Configuration r6 = originator("r6", 65006L, "10.0.36.0/31", "10.0.36.0", "10.0.36.1");
    if (!r6Originates) {
      r6.getDefaultVrf().getStaticRoutes().clear();
    }

    Configuration r3 = node("r3");
    Vrf v3 = vrf(r3);
    iface(r3, v3, "10.0.36.1/31");
    iface(r3, v3, "10.0.23.1/31");
    BgpProcess p3 = bgpProcess(v3, "3.3.3.3");
    neighbor(
        p3,
        "10.0.36.1",
        "10.0.36.0",
        AS_INTERNAL,
        65006L,
        policy(r3, new SetLocalPreference(new LiteralLong(200)), accept()),
        policy(r3, Statements.ExitReject.toStaticStatement()),
        false);
    neighbor(
        p3,
        "10.0.23.1",
        "10.0.23.0",
        AS_INTERNAL,
        AS_INTERNAL,
        policy(r3, accept()),
        policy(r3, new SetNextHop(SelfNextHop.getInstance()), accept()),
        false);

    Configuration r2 = node("r2");
    Vrf v2 = vrf(r2);
    iface(r2, v2, "10.0.12.1/31");
    iface(r2, v2, "10.0.23.0/31");
    iface(r2, v2, "10.0.24.0/31");
    BgpProcess p2 = bgpProcess(v2, "2.2.2.2");
    String acceptAll = policy(r2, accept());
    neighbor(p2, "10.0.12.1", "10.0.12.0", AS_INTERNAL, 65001L, acceptAll, acceptAll, false);
    neighbor(p2, "10.0.23.0", "10.0.23.1", AS_INTERNAL, AS_INTERNAL, acceptAll, acceptAll, false);
    neighbor(
        p2,
        "10.0.24.0",
        "10.0.24.1",
        AS_INTERNAL,
        AS_INTERNAL,
        acceptAll,
        policy(r2, new SetNextHop(SelfNextHop.getInstance()), accept()),
        true);

    Configuration r4 = node("r4");
    Vrf v4 = vrf(r4);
    iface(r4, v4, "10.0.24.1/31");
    neighbor(
        bgpProcess(v4, "4.4.4.4"),
        "10.0.24.1",
        "10.0.24.0",
        AS_INTERNAL,
        AS_INTERNAL,
        policy(r4, accept()),
        policy(r4, Statements.ExitReject.toStaticStatement()),
        false);

    Batfish batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of("r1", r1, "r2", r2, "r3", r3, "r4", r4, "r6", r6), _folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish
        .loadDataPlane(batfish.getSnapshot())
        .getBgpRoutes()
        .get("r4", DEFAULT_VRF_NAME)
        .stream()
        .filter(r -> r.getNetwork().equals(PREFIX))
        .map(Bgpv4Route::getAsPath)
        .map(p -> p.getAsPathString())
        .toList();
  }

  @Test
  public void testADemotedBestEbgpPathStaysAdvertised() throws IOException {
    // r2 first selects r1's path and advertises it, then prefers r3's. The eBGP path is still the
    // best eBGP path, which advertise-external is for, so it must not be withdrawn.
    assertThat(pathsAtR4(true), contains("65001"));
  }

  @Test
  public void testTheBestEbgpPathIsAdvertisedWhereItIsTheBest() throws IOException {
    // The companion, so the case above cannot fail by r4 never learning anything.
    assertThat(pathsAtR4(false), contains("65001"));
  }
}
