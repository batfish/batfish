package org.batfish.representation.cisco;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.INVALID_LOCAL_INTERFACE;
import static org.batfish.representation.cisco.CiscoConversions.createAclWithSymmetricalLines;
import static org.batfish.representation.cisco.CiscoConversions.generateBgpImportPolicy;
import static org.batfish.representation.cisco.CiscoConversions.getMatchingPsk;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.matchers.IkePhase1KeyMatchers;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CiscoConversions} class */
@RunWith(JUnit4.class)
public class CiscoConversionsTest {

  private static final String IKE_PHASE1_KEY = "IKE_Phase1_Key";
  private NetworkFactory _nf;
  private Warnings _warnings;

  @Before
  public void before() {
    _warnings = new Warnings(true, true, true);
    _nf = new NetworkFactory();
  }

  @Test
  public void testCreateAclWithSymmetricalLines() {
    IpAccessList ipAccessList =
        _nf.aclBuilder()
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setName("permit ip 1.1.1.1 0.0.0.0 2.2.2.2 0.0.0.0")
                        .setMatchCondition(
                            new MatchHeaderSpace(
                                HeaderSpace.builder()
                                    .setSrcIps(new IpWildcard("1.1.1.1").toIpSpace())
                                    .setDstIps(new IpWildcard("2.2.2.2").toIpSpace())
                                    .setNotSrcIps(new IpWildcard("3.3.3.3").toIpSpace())
                                    .setIpProtocols(ImmutableSet.of(IpProtocol.IP))
                                    .build()))
                        .build()))
            .build();

    // notSrcIps should cause the returned IpAccessList to be null

    assertThat(createAclWithSymmetricalLines(ipAccessList), nullValue());
  }

  @Test
  public void testGetMatchingPskInvalidLocalIface() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Missing_Local_Interface");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName(INVALID_LOCAL_INTERFACE);
    isakmpProfile.setKeyring("IKE_Phase1_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo(
            "Invalid local address interface configured for ISAKMP profile Missing_Local_Interface"));
  }

  @Test
  public void testGetMatchingPskNullKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Null_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Keyring not set for ISAKMP profile Null_Keyring"));
  }

  @Test
  public void testGetMatchingPskMissingKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Missing_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("Unknown_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Cannot find keyring Unknown_Key for ISAKMP profile Missing_Keyring"));
  }

  @Test
  public void testGetMatchingPskInvalidLocalIfaceKr() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Invalid_Iface_Local_Address");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface(INVALID_LOCAL_INTERFACE);

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
    assertThat(
        Iterables.getOnlyElement(_warnings.getRedFlagWarnings()).getText(),
        equalTo("Invalid local address interface configured for keyring IKE_Phase1_Key"));
  }

  @Test
  public void testGetMatchingPskValidKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Valid_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");
    isakmpProfile.setMatchIdentity(new IpWildcard("1.2.3.4:0.0.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.4:0.0.0.0").toIpSpace());

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, IkePhase1KeyMatchers.hasKeyHash("test_key"));
  }

  @Test
  public void testGetMatchingPskInvalidKeyring() {
    IsakmpProfile isakmpProfile = new IsakmpProfile("Invalid_Keyring");
    // Empty interfaceName value represents incorrect local-address value
    isakmpProfile.setLocalInterfaceName("local_interface");
    isakmpProfile.setKeyring("IKE_Phase1_Key");
    isakmpProfile.setMatchIdentity(new IpWildcard("2.4.3.4:255.255.0.0"));

    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash("test_key");
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY);
    ikePhase1Key.setLocalInterface("local_interface");
    ikePhase1Key.setRemoteIdentity(new IpWildcard("1.2.3.5:0.0.0.0").toIpSpace());

    IkePhase1Key matchingKey =
        getMatchingPsk(isakmpProfile, _warnings, ImmutableMap.of(IKE_PHASE1_KEY, ikePhase1Key));

    assertThat(matchingKey, is(nullValue()));
  }

  @Test
  public void testGenerateBgpImportPolicyWithNoConstraints() {
    // Create a BGP import policy from a peer group with no constraints on incoming routes.
    // Resulting import policy should be null.
    Configuration c =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    LeafBgpPeerGroup lpg = new IpBgpPeerGroup(Ip.parse("1.2.3.4"));
    Warnings w = new Warnings(true, true, true);
    RoutingPolicy bgpImportPolicy = generateBgpImportPolicy(lpg, DEFAULT_VRF_NAME, c, w);

    assertThat(bgpImportPolicy, nullValue());
    assertThat(w.getRedFlagWarnings(), empty());
  }

  @Test
  public void testGenerateBgpImportPolicyWithPrefixList() {
    // Set up a config with a prefix-list and create a BGP import policy from a peer group that uses
    // that prefix-list to filter incoming routes. Resulting import policy should match prefix-list.
    String prefixListName = "PREFIX_LIST";
    Prefix permittedPrefix = Prefix.parse("1.1.1.0/24");
    Prefix deniedPrefix = Prefix.parse("2.2.2.0/24");
    Ip peerAddress = Ip.parse("1.2.3.4");
    Configuration c =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RouteFilterList prefixList =
        new RouteFilterList(
            prefixListName,
            ImmutableList.of(
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.fromPrefix(permittedPrefix)),
                new RouteFilterLine(LineAction.DENY, PrefixRange.fromPrefix(deniedPrefix))));
    c.getRouteFilterLists().put(prefixListName, prefixList);
    LeafBgpPeerGroup lpg = new IpBgpPeerGroup(peerAddress);
    lpg.setInboundPrefixList(prefixListName);
    Warnings w = new Warnings(true, true, true);
    RoutingPolicy bgpImportPolicy = generateBgpImportPolicy(lpg, DEFAULT_VRF_NAME, c, w);
    assertThat(bgpImportPolicy, notNullValue());
    assertThat(w.getRedFlagWarnings(), empty());

    // Test the generated policy against some routes
    BgpRoute.Builder r =
        BgpRoute.builder()
            .setOriginatorIp(peerAddress)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.IBGP);
    BgpRoute permittedRoute = r.setNetwork(permittedPrefix).build();
    BgpRoute deniedRoute = r.setNetwork(deniedPrefix).build();
    BgpRoute unmatchedRoute = r.setNetwork(Prefix.parse("3.3.3.0/24")).build();
    assertThat(
        bgpImportPolicy.process(
            permittedRoute,
            permittedRoute.toBuilder(),
            peerAddress,
            DEFAULT_VRF_NAME,
            Direction.IN),
        equalTo(true));
    assertThat(
        bgpImportPolicy.process(
            deniedRoute, deniedRoute.toBuilder(), peerAddress, DEFAULT_VRF_NAME, Direction.IN),
        equalTo(false));
    assertThat(
        bgpImportPolicy.process(
            unmatchedRoute,
            unmatchedRoute.toBuilder(),
            peerAddress,
            DEFAULT_VRF_NAME,
            Direction.IN),
        equalTo(false));
  }

  @Test
  public void testGenerateBgpImportPolicyWithRouteMap() {
    // Set up a config with a route-map and create a BGP import policy from a peer group that uses
    // that route-map to filter incoming routes. Resulting import policy should match the route-map.
    String routeMapName = "ROUTE_MAP";
    Configuration c =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy routeMap =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(routeMapName)
            .addStatement(Statements.ExitReject.toStaticStatement())
            .build();

    LeafBgpPeerGroup lpg = new IpBgpPeerGroup(Ip.parse("1.2.3.4"));
    lpg.setInboundRouteMap(routeMapName);
    Warnings w = new Warnings(true, true, true);

    RoutingPolicy bgpImportPolicy = generateBgpImportPolicy(lpg, DEFAULT_VRF_NAME, c, w);

    assertThat(bgpImportPolicy, equalTo(routeMap));
    assertThat(w.getRedFlagWarnings(), empty());
  }

  @Test
  public void testGenerateBgpImportPolicyWithRouteMapAndPrefixList() {
    // Set up a config with both a route-map and a prefix-list, and create a BGP import policy from
    // a peer group that uses both to filter incoming routes. Resulting import policy should match
    // the route-map, and a warning should be generated indicating the prefix-list was ignored.
    String routeMapName = "ROUTE_MAP";
    String prefixListName = "PREFIX_LIST";
    Prefix permittedPrefix = Prefix.parse("1.1.1.0/24");
    Prefix deniedPrefix = Prefix.parse("2.2.2.0/24");
    Configuration c =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    RoutingPolicy routeMap =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(routeMapName)
            .addStatement(Statements.ExitReject.toStaticStatement())
            .build();
    RouteFilterList prefixList =
        new RouteFilterList(
            prefixListName,
            ImmutableList.of(
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.fromPrefix(permittedPrefix)),
                new RouteFilterLine(LineAction.DENY, PrefixRange.fromPrefix(deniedPrefix))));
    c.getRouteFilterLists().put(prefixListName, prefixList);

    LeafBgpPeerGroup lpg = new IpBgpPeerGroup(Ip.parse("1.2.3.4"));
    lpg.setInboundPrefixList(prefixListName);
    lpg.setInboundRouteMap(routeMapName);
    Warnings w = new Warnings(true, true, true);

    RoutingPolicy bgpImportPolicy = generateBgpImportPolicy(lpg, DEFAULT_VRF_NAME, c, w);

    assertThat(bgpImportPolicy, equalTo(routeMap));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
    assertThat(
        w.getRedFlagWarnings().get(0).getText(),
        equalTo(
            "Batfish does not support configuring both a route-map and a prefix-list for incoming BGP routes."
                + " When this occurs, the prefix-list will be ignored."));
  }
}
