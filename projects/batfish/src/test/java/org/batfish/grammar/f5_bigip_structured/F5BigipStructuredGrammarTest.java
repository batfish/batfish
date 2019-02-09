package org.batfish.grammar.f5_bigip_structured;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRoute6FilterLists;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasRouteFilterLists;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAddress;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasAllowedVlans;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasNativeVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSpeed;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasSwitchPortMode;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasVlan;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isSwitchport;
import static org.batfish.datamodel.matchers.RouteFilterListMatchers.permits;
import static org.batfish.datamodel.matchers.RouteFilterListMatchers.rejects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.matchers.Route6FilterListMatchers;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class F5BigipStructuredGrammarTest {
  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/grammar/f5_bigip_structured/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname.toLowerCase());
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  @Test
  public void testHostname() throws IOException {
    String filename = "f5_bigip_structured_hostname";
    String hostname = "myhostname";
    Map<String, Configuration> configurations = parseTextConfigs(filename);

    assertThat(configurations, hasKey(hostname));
  }

  @Test
  public void testInterfaceSpeed() throws IOException {
    Configuration c = parseConfig("f5_bigip_structured_interface");

    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder("1.0", "2.0"));
    assertThat(c, hasInterface("1.0", hasSpeed(40E9D)));
    assertThat(c, hasInterface("2.0", hasSpeed(100E9D)));
  }

  @Test
  public void testPrefixList() throws IOException {
    String hostname = "f5_bigip_structured_net_routing_prefix_list";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    Configuration c = batfish.loadConfigurations().get(hostname);
    String v4Name = "/Common/MY_IPV4_PREFIX_LIST";
    String v6Name = "/Common/MY_IPV6_PREFIX_LIST";
    String invalidName = "/Common/INVALID_MIXED_PREFIX_LIST";

    // Check the presence and behavior of the IPv4 prefix-list
    assertThat(c, hasRouteFilterLists(hasKey(v4Name)));

    RouteFilterList v4 = c.getRouteFilterLists().get(v4Name);

    assertThat(v4, rejects(Prefix.parse("192.0.2.0/31")));
    assertThat(v4, permits(Prefix.parse("192.0.2.4/30")));
    assertThat(v4, permits(Prefix.parse("192.0.2.4/31")));
    assertThat(v4, rejects(Prefix.parse("192.0.2.4/32")));

    // Check the presence and behavior of the IPv6 prefix-list
    assertThat(c, hasRoute6FilterLists(hasKey(v6Name)));

    Route6FilterList v6 = c.getRoute6FilterLists().get(v6Name);

    assertThat(v6, Route6FilterListMatchers.permits(new Prefix6("dead:beef:1::/64")));
    assertThat(v6, Route6FilterListMatchers.rejects(new Prefix6("dead:beef:1::/128")));

    // The invalid list should not make it into the data model
    assertThat(c, hasRouteFilterLists(not(hasKey(invalidName))));
    assertThat(c, hasRoute6FilterLists(not(hasKey(invalidName))));

    // Check errors
    Warnings warnings = batfish.initInfo(false, true).getWarnings().get(hostname);

    assertThat(
        "Missing IPv4 prefix reported",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(Predicates.containsPattern("Missing IPv4 prefix.*PL4_WITH_MISSING_PREFIX")));
    assertThat(
        "Missing IPv6 prefix reported",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(Predicates.containsPattern("Missing IPv6 prefix.*PL6_WITH_MISSING_PREFIX")));
    assertThat(
        "Invalid IPv4 length-range reported",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(
                Predicates.containsPattern(
                    "Invalid IPv4 prefix-len-range.*PL4_WITH_INVALID_LENGTH")));
    assertThat(
        "Invalid IPv6 length-range reported",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(
                Predicates.containsPattern(
                    "Invalid IPv6 prefix-len-range.*PL6_WITH_INVALID_LENGTH")));
    assertThat(
        "Missing action reported for IPv4 prefix-list",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(Predicates.containsPattern("Missing action.*PL4_WITH_MISSING_ACTION")));
    assertThat(
        "Missing action reported for IPv6 prefix-list",
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .anyMatch(Predicates.containsPattern("Missing action.*PL6_WITH_MISSING_ACTION")));
  }

  @Test
  public void testRouteMap() throws IOException {
    Configuration c = parseConfig("f5_bigip_structured_net_routing_route_map");
    String acceptAllName = "/Common/ACCEPT_ALL";
    String rm1Name = "/Common/rm1";

    // ACCEPT_ALL
    assertThat(c.getRoutingPolicies(), hasKey(acceptAllName));
    assertThat(
        "ACCEPT_ALL accepts arbitrary prefix 10.0.0.0/24",
        c.getRoutingPolicies()
            .get(acceptAllName)
            .call(
                Environment.builder(c)
                    .setDirection(Direction.OUT)
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());

    // rm1
    assertThat(c.getRoutingPolicies(), hasKey(rm1Name));

    RoutingPolicy rm1 = c.getRoutingPolicies().get(rm1Name);

    assertThat(
        "rm1 denies prefix 10.0.0.0/24 (via 10)",
        !rm1.call(
                Environment.builder(c)
                    .setDirection(Direction.OUT)
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.0.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());

    ConnectedRoute acceptedRoute =
        new ConnectedRoute(Prefix.strict("10.0.1.0/24"), "/Common/outint");
    BgpRoute.Builder outputRoute =
        BgpRoute.builder()
            .setNetwork(acceptedRoute.getNetwork())
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP);
    Environment acceptedPrefixEnvironment =
        Environment.builder(c)
            .setDirection(Direction.OUT)
            .setOutputRoute(outputRoute)
            .setVrf(Configuration.DEFAULT_VRF_NAME)
            .setOriginalRoute(acceptedRoute)
            .build();
    Result acceptedBy20 = rm1.call(acceptedPrefixEnvironment);

    assertThat("rm1 accepts prefix 10.0.1.0/24 (via 20)", acceptedBy20.getBooleanValue());
    assertThat(
        "rm1 sets communities 1:2 and 33:44 on the output route",
        outputRoute.build().getCommunities(),
        equalTo(
            Stream.of("1:2", "33:44")
                .map(CommonUtil::communityStringToLong)
                .collect(ImmutableSet.toImmutableSet())));

    assertThat(
        "rm1 rejects prefix 10.0.2.0/24 (no matching entry)",
        !rm1.call(
                Environment.builder(c)
                    .setDirection(Direction.OUT)
                    .setVrf(Configuration.DEFAULT_VRF_NAME)
                    .setOriginalRoute(
                        new ConnectedRoute(Prefix.strict("10.0.2.0/24"), "/Common/outint"))
                    .build())
            .getBooleanValue());
  }

  @Test
  public void testVlan() throws IOException {
    Configuration c = parseConfig("f5_bigip_structured_vlan");
    String portName = "1.0";
    String vlanName = "/Common/MYVLAN";

    assertThat(c.getAllInterfaces().keySet(), containsInAnyOrder(portName, vlanName));

    // port interface
    assertThat(c, hasInterface(portName, isActive()));
    assertThat(c, hasInterface(portName, isSwitchport()));
    assertThat(c, hasInterface(portName, hasSwitchPortMode(SwitchportMode.TRUNK)));
    assertThat(c, hasInterface(portName, hasAllowedVlans(IntegerSpace.of(123))));
    assertThat(c, hasInterface(portName, hasNativeVlan(nullValue())));

    // vlan interface
    assertThat(c, hasInterface(vlanName, isActive()));
    assertThat(c, hasInterface(vlanName, hasVlan(123)));
    assertThat(c, hasInterface(vlanName, hasAddress("10.0.0.1/24")));
  }
}
