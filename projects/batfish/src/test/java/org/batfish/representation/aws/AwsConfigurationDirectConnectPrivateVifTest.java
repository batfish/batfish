package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfigurationTestUtils.getAnyFlow;
import static org.batfish.representation.aws.AwsConfigurationTestUtils.testTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * E2e tests for AWS Direct Connect Private VIFs. The topology has two on-prem routers, each
 * connected to the same VPN Gateway over its own VGW-attached Private VIF. Both routers advertise
 * the same on-prem prefix (10.20.0.0/16); {@code onprem-high} tags the advertisement with the AWS
 * DX HIGH-preference community (7224:7300), {@code onprem-low} with LOW (7224:7100). The VGW's
 * per-VIF DX import policy maps these to local-pref values, and BGP best-path selection on the VGW
 * must pick the HIGH path — both in the RIB and in the actual forwarding decision.
 *
 * <p>Preferred path: instance → subnet → VPC → VGW → onprem-high.
 */
public class AwsConfigurationDirectConnectPrivateVifTest {

  private static final String TESTCONFIGS_DIR =
      "org/batfish/representation/aws/test-direct-connect-private-vif";

  private static final List<String> fileNames =
      ImmutableList.of(
          "Addresses.json",
          "CustomerGateways.json",
          "InternetGateways.json",
          "NatGateways.json",
          "NetworkAcls.json",
          "NetworkInterfaces.json",
          "Reservations.json",
          "RouteTables.json",
          "SecurityGroups.json",
          "Subnets.json",
          "VirtualInterfaces.json",
          "VpcEndpoints.json",
          "Vpcs.json",
          "VpnConnections.json",
          "VpnGateways.json");

  private static final List<String> onPremConfigFiles =
      ImmutableList.of("onprem-high.txt", "onprem-low.txt");

  private static final String _onPremHigh = "onprem-high";
  private static final String _vgw = "vgw-priv01";
  private static final String _vpc = "vpc-priv01";
  private static final String _subnet = "subnet-priv01";
  private static final String _instance = "i-priv01";
  private static final String _vifIdHigh = "dxvif-priv01";
  private static final Prefix _onPremPrefix = Prefix.parse("10.20.0.0/16");
  private static final Ip _instanceIp = Ip.parse("10.50.1.10");
  private static final Ip _onPremHighIp = Ip.parse("10.20.0.1");
  private static final Ip _onPremHighVifIp = Ip.parse("169.254.30.2");

  @ClassRule public static TemporaryFolder _folder = new TemporaryFolder();

  private static IBatfish _batfish;

  @BeforeClass
  public static void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setAwsFiles(TESTCONFIGS_DIR, fileNames)
                .setConfigurationFiles(TESTCONFIGS_DIR, onPremConfigFiles)
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  /**
   * Forward (on-prem → AWS) reachability: the HIGH-pref router can reach the VPC instance through
   * its Private VIF.
   */
  @Test
  public void testFromOnPremHighToInstance() {
    testTrace(
        getAnyFlow(_onPremHigh, _instanceIp, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_onPremHigh, _vgw, _vpc, _subnet, _instance),
        _batfish);
  }

  /**
   * Community-driven AWS-side preference, end-to-end: both on-prem routers advertise 10.20.0.0/16,
   * one with the HIGH community and one with the LOW. BGP best-path selection on the VGW must pick
   * the HIGH path. The trace from the AWS instance toward an on-prem destination must traverse
   * {@code onprem-high} and not split ECMP with {@code onprem-low}.
   */
  @Test
  public void testInstanceToOnPremPrefersHighDxCommunity() {
    testTrace(
        getAnyFlow(_instance, _onPremHighIp, _batfish),
        FlowDisposition.ACCEPTED,
        ImmutableList.of(_instance, _subnet, _vpc, _vgw, _onPremHigh),
        _batfish);
  }

  /**
   * The VGW's RIB must have a single best route for the on-prem prefix — the one with HIGH DX
   * local-pref — not an ECMP set. Multiple BGP paths exist (via dxvif-priv01 from onprem-high and
   * dxvif-priv02 from onprem-low) but local-pref breaks the tie before ECMP is considered.
   */
  @Test
  public void testVgwHasOnlyHighPrefRoute() {
    DataPlane dp = _batfish.loadDataPlane(_batfish.getSnapshot());
    Set<AbstractRoute> vgwRoutes =
        dp.getRibs().get(_vgw, "default").getRoutes().stream()
            .filter(r -> r.getNetwork().equals(_onPremPrefix))
            .collect(Collectors.toSet());
    assertThat(vgwRoutes, hasSize(1));
    AbstractRoute route = vgwRoutes.iterator().next();
    assertThat(
        "on-prem prefix should be a BGP route on the VGW", route, instanceOf(Bgpv4Route.class));
    Bgpv4Route bgpRoute = (Bgpv4Route) route;
    assertThat(
        "VGW best-path for on-prem prefix should carry HIGH DX local-preference",
        bgpRoute.getLocalPreference(),
        equalTo(Route.DIRECT_CONNECT_HIGH_LOCAL_PREFERENCE));
    assertThat(
        "VGW best-path for on-prem prefix should be via the HIGH-pref VIF peer",
        bgpRoute.getNextHopIp(),
        equalTo(_onPremHighVifIp));
  }

  /**
   * Verify the AWS DX traffic-engineering communities (7224:7300/7200/7100) translate to the
   * correct local-preference values on the VGW. The VGW's per-VIF DX import policy reads the
   * community on incoming BGP routes and assigns a local-pref accordingly.
   */
  @Test
  public void testDxCommunityToLocalPreferenceMappingOnVgw() {
    Configuration vgwCfg = _batfish.loadConfigurations(_batfish.getSnapshot()).get(_vgw);
    String policyName = VpnGateway.vgwDxImportPolicyName(_vifIdHigh);
    RoutingPolicy policy = vgwCfg.getRoutingPolicies().get(policyName);
    assertThat("DX import policy should exist on the VGW", policy, notNullValue());

    Bgpv4Route base =
        Bgpv4Route.testBuilder()
            .setNetwork(_onPremPrefix)
            .setOriginatorIp(_onPremHighIp)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .build();

    // No community → MEDIUM (default)
    assertEquals(
        Route.DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE, processIn(policy, base).getLocalPreference());
    // 7224:7200 (medium) → MEDIUM
    assertEquals(
        Route.DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE,
        processIn(policy, withCommunity(base, DirectConnectGateway.DX_MEDIUM_PREF_COMMUNITY))
            .getLocalPreference());
    // 7224:7300 (high) → HIGH
    assertEquals(
        Route.DIRECT_CONNECT_HIGH_LOCAL_PREFERENCE,
        processIn(policy, withCommunity(base, DirectConnectGateway.DX_HIGH_PREF_COMMUNITY))
            .getLocalPreference());
    // 7224:7100 (low) → LOW
    assertEquals(
        Route.DIRECT_CONNECT_LOW_LOCAL_PREFERENCE,
        processIn(policy, withCommunity(base, DirectConnectGateway.DX_LOW_PREF_COMMUNITY))
            .getLocalPreference());
  }

  private static Bgpv4Route processIn(RoutingPolicy policy, Bgpv4Route route) {
    Bgpv4Route.Builder builder = route.toBuilder();
    boolean accepted = policy.process(route, builder, Environment.Direction.IN);
    assertThat(accepted, equalTo(true));
    return builder.build();
  }

  private static Bgpv4Route withCommunity(Bgpv4Route base, StandardCommunity community) {
    return base.toBuilder().setCommunities(CommunitySet.of(community)).build();
  }
}
