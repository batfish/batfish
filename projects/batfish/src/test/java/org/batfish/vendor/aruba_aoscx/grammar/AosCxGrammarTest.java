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
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixList6;
import org.batfish.datamodel.StaticRoute6;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteMap6;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3ExternalSummary;
import org.batfish.datamodel.ospf.Ospfv3VirtualLink;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfv3ExternalSummary;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfv3Process;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixList;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPrefixListEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMap;
import org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMapEntry;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute6;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;
import org.batfish.vendor.aruba_aoscx.representation.AosCxPortSpec.Operator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class AosCxGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testHostnameExtraction() {
    AosCxConfiguration vc = parseVendorConfig("aoscx-hostname");
    assertThat(vc.getHostname(), equalTo("aoscx-router"));
  }




  @Test
  public void testLagTrunkConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-lag-trunk");
    Configuration c = configs.get("aoscx-router");

    org.batfish.datamodel.Interface lag =
        c.getAllInterfaces().get("lag 1");

    assertThat(lag, notNullValue());
    assertThat(lag.getSwitchport(), equalTo(true));
    assertThat(lag.getSwitchportMode(), equalTo(SwitchportMode.TRUNK));
    assertThat(
        lag.getAllowedVlans(),
        equalTo(IntegerSpace.parse("1000-1001,1005")));

    // Native VLAN 1 is configured tagged on AOS-CX, so it must not
    // become Batfish's untagged native VLAN.
    assertThat(lag.getNativeVlan(), equalTo(null));

    assertThat(
        lag.getChannelGroupMembers(),
        containsInAnyOrder("1/9/1", "1/10/1"));
  }

  @Test
  public void testLagTrunkExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-lag-trunk");

    AosCxInterface lag = c.getInterfaces().get("lag 1");
    assertThat(lag, notNullValue());
    assertThat(lag.getSwitchport(), equalTo(true));
    assertThat(lag.getNativeVlan(), equalTo(1));
    assertThat(lag.getNativeVlanTagged(), equalTo(true));
    assertThat(
        lag.getAllowedVlans(),
        equalTo(IntegerSpace.parse("1000-1001,1005")));

    assertThat(
        c.getInterfaces().get("1/9/1").getLagName(),
        equalTo("lag 1"));
    assertThat(
        c.getInterfaces().get("1/10/1").getLagName(),
        equalTo("lag 1"));
  }

  @Test
  public void testLagExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-lag");

    AosCxInterface loopback = c.getInterfaces().get("loopback 0");
    assertThat(loopback, notNullValue());
    assertThat(loopback.getIpv6LinkLocalEnabled(), equalTo(true));
    assertThat(
        loopback.getIpv6Addresses(),
        contains(ConcreteInterfaceAddress6.parse("2001:db8:1::41/128")));

    assertThat(c.getInterfaces(), hasKey("lag 13"));
    assertThat(c.getInterfaces().get("1/9/3").getLagName(), equalTo("lag 13"));
    assertThat(c.getInterfaces().get("1/10/3").getLagName(), equalTo("lag 13"));
  }

  @Test
  public void testLagConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-lag");
    Configuration c = configs.get("aoscx-router");

    org.batfish.datamodel.Interface lag =
        c.getAllInterfaces().get("lag 13");

    assertThat(lag, notNullValue());
    assertThat(lag.getInterfaceType(), equalTo(InterfaceType.AGGREGATED));
    assertThat(
        lag.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("203.0.113.230/29")));
    assertThat(
        lag.getChannelGroupMembers(),
        containsInAnyOrder("1/9/3", "1/10/3"));
    assertThat(lag.getOspfSettings(), notNullValue());
    assertThat(lag.getOspfSettings().getCost(), equalTo(1));

    org.batfish.datamodel.Interface loopback =
        c.getAllInterfaces().get("loopback 0");
    assertThat(loopback, notNullValue());
    assertThat(loopback.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(
        loopback.getAllAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("192.0.2.41/32"),
            ConcreteInterfaceAddress6.parse("2001:db8:1::41/128")));
    assertThat(
        loopback.getAllConcreteAddresses6(),
        contains(ConcreteInterfaceAddress6.parse("2001:db8:1::41/128")));
    assertThat(loopback.getOspfSettings(), notNullValue());
    assertThat(loopback.getOspfSettings().getCost(), equalTo(1));

    assertThat(
        c.getAllInterfaces().get("1/9/3").getChannelGroup(),
        equalTo("lag 13"));
    assertThat(
        c.getAllInterfaces().get("1/10/3").getChannelGroup(),
        equalTo("lag 13"));
  }




  @Test
  public void testManagementInterfaceExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-mgmt");

    AosCxInterface mgmt = c.getInterfaces().get("mgmt");
    assertThat(mgmt, notNullValue());
    assertThat(mgmt.getVrfName(), equalTo("mgmt"));
    assertThat(
        mgmt.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.0.2.10/24")));

    assertThat(c.getStaticRoutes().size(), equalTo(1));

    AosCxStaticRoute defaultRoute = c.getStaticRoutes().get(0);
    assertThat(defaultRoute.getPrefix(), equalTo(Prefix.ZERO));
    assertThat(defaultRoute.getVrfName(), equalTo("mgmt"));
    assertThat(defaultRoute.getNextHopType(), equalTo(NextHopType.IP));
    assertThat(defaultRoute.getNextHop(), equalTo("192.0.2.1"));
  }

  @Test
  public void testManagementInterfaceConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-mgmt");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getVrfs(), hasKey("mgmt"));

    org.batfish.datamodel.Interface mgmt =
        c.getAllInterfaces().get("mgmt");

    assertThat(mgmt, notNullValue());
    assertThat(mgmt.getVrfName(), equalTo("mgmt"));
    assertThat(mgmt.getAdminUp(), equalTo(true));
    assertThat(
        mgmt.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("192.0.2.10/24")));

    assertThat(
        c.getVrfs().get("mgmt").getStaticRoutes().size(),
        equalTo(1));

    StaticRoute defaultRoute =
        c.getVrfs().get("mgmt").getStaticRoutes().first();

    assertThat(defaultRoute.getNetwork(), equalTo(Prefix.ZERO));
    assertThat(
        defaultRoute.getNextHop(),
        equalTo(NextHopIp.of(Ip.parse("192.0.2.1"))));
  }

  @Test
  public void testDescriptionExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-description");

    AosCxInterface physical = c.getInterfaces().get("1/1/1");
    assertThat(physical, notNullValue());
    assertThat(
        physical.getDescription(),
        equalTo("Uplink to vlan 1000"));

    AosCxInterface lag = c.getInterfaces().get("lag 10");
    assertThat(lag, notNullValue());
    assertThat(
        lag.getDescription(),
        equalTo("Core aggregate link"));
  }

  @Test
  public void testDescriptionConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-description");
    Configuration c = configs.get("aoscx-router");

    org.batfish.datamodel.Interface physical =
        c.getAllInterfaces().get("1/1/1");
    assertThat(physical, notNullValue());
    assertThat(
        physical.getDescription(),
        equalTo("Uplink to vlan 1000"));

    org.batfish.datamodel.Interface lag =
        c.getAllInterfaces().get("lag 10");
    assertThat(lag, notNullValue());
    assertThat(
        lag.getDescription(),
        equalTo("Core aggregate link"));
  }

  @Test
  public void testMtuExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-mtu");

    AosCxInterface routed = c.getInterfaces().get("1/1/1");
    assertThat(routed, notNullValue());
    assertThat(routed.getMtu(), equalTo(9198));
    assertThat(routed.getIpMtu(), equalTo(9178));

    AosCxInterface layer2 = c.getInterfaces().get("1/1/2");
    assertThat(layer2, notNullValue());
    assertThat(layer2.getMtu(), equalTo(9198));
    assertThat(layer2.getIpMtu(), equalTo(null));
  }

  @Test
  public void testMtuConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-mtu");
    Configuration c = configs.get("aoscx-router");

    org.batfish.datamodel.Interface routed =
        c.getAllInterfaces().get("1/1/1");
    assertThat(routed, notNullValue());
    assertThat(routed.getMtu(), equalTo(9178));

    org.batfish.datamodel.Interface layer2 =
        c.getAllInterfaces().get("1/1/2");
    assertThat(layer2, notNullValue());
    assertThat(layer2.getMtu(), equalTo(9198));
  }

  @Test
  public void testInterfaceExtraction() {
    AosCxConfiguration vc = parseVendorConfig("aoscx-interfaces");

    AosCxInterface physical = vc.getInterfaces().get("1/1/2");
    assertThat(physical.getEnabled(), equalTo(true));
    assertThat(
        physical.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.0.2.2/30")));

    AosCxInterface loopback = vc.getInterfaces().get("loopback 0");
    assertThat(
        loopback.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("192.0.2.41/32")));

    AosCxInterface vlan = vc.getInterfaces().get("vlan 1000");
    assertThat(vlan.getEnabled(), equalTo(false));
    assertThat(
        vlan.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("198.51.100.137/30")));
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
  public void testIpv6AclExtraction() {
    AosCxConfiguration c =
        parseVendorConfig("aoscx-acl-ipv6");

    assertThat(
        c.getIpv6AccessLists(),
        hasKey("V6-IN"));
    assertThat(
        c.getIpv6AccessLists(),
        hasKey("V6-OUT"));

    var v6In =
        c.getIpv6AccessLists().get("V6-IN");

    assertThat(
        v6In.getEntries().size(),
        equalTo(4));

    assertThat(
        v6In.getEntries()
            .get(10L)
            .getAction(),
        equalTo(LineAction.DENY));

    assertThat(
        v6In.getEntries()
            .get(20L)
            .getProtocol(),
        equalTo("tcp"));

    assertThat(
        v6In.getEntries()
            .get(20L)
            .getSource(),
        equalTo(
            "2001:db8:10::/64"));

    assertThat(
        v6In.getEntries()
            .get(20L)
            .getSourcePort()
            .getOperator(),
        equalTo(Operator.GT));

    assertThat(
        v6In.getEntries()
            .get(20L)
            .getDestinationPort()
            .getOperator(),
        equalTo(Operator.EQ));

    assertThat(
        v6In.getEntries()
            .get(40L)
            .getProtocol(),
        equalTo("ipv6"));

    AosCxInterface physical =
        c.getInterfaces().get("1/1/2");

    assertThat(
        physical.getIncomingIpv6Acl(),
        equalTo("V6-IN"));

    assertThat(
        physical.getOutgoingIpv6Acl(),
        equalTo("V6-OUT"));

    AosCxInterface svi =
        c.getInterfaces().get("vlan 200");

    assertThat(
        svi.getIncomingIpv6Acl(),
        equalTo("V6-IN"));
  }

  @Test
  public void testIpv6AclConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-acl-ipv6");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(c, notNullValue());

    assertThat(
        c.getIp6AccessLists(),
        hasKey("V6-IN"));

    org.batfish.datamodel.Ip6AccessList acl =
        c.getIp6AccessLists().get("V6-IN");

    assertThat(
        acl.getLines().size(),
        equalTo(4));

    org.batfish.datamodel.Flow6 ssh =
        org.batfish.datamodel.Flow6.builder()
            .setIngressNode(
                "aoscx-router")
            .setSrcIp(
                org.batfish.datamodel.Ip6.parse(
                    "2001:db8:10::10"))
            .setDstIp(
                org.batfish.datamodel.Ip6.parse(
                    "2001:db8:20::20"))
            .setIpProtocol(
                org.batfish.datamodel.IpProtocol.TCP)
            .setSrcPort(40000)
            .setDstPort(22)
            .setPacketLength(80)
            .build();

    assertThat(
        acl.filter(ssh).getAction(),
        equalTo(LineAction.DENY));

    org.batfish.datamodel.Flow6 https =
        ssh.toBuilder()
            .setDstPort(443)
            .build();

    assertThat(
        acl.filter(https).getAction(),
        equalTo(LineAction.PERMIT));

    org.batfish.datamodel.Interface physical =
        c.getAllInterfaces().get("1/1/2");

    assertThat(
        physical.getIncomingFilter6(),
        notNullValue());

    assertThat(
        physical.getIncomingFilter6()
            .getName(),
        equalTo("V6-IN"));

    assertThat(
        physical.getOutgoingFilter6(),
        notNullValue());

    assertThat(
        physical.getOutgoingFilter6()
            .getName(),
        equalTo("V6-OUT"));

    org.batfish.datamodel.Interface svi =
        c.getAllInterfaces().get("vlan 200");

    assertThat(
        svi.getIncomingFilter6(),
        notNullValue());
  }

  @Test
  public void testAclExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-acl");

    assertThat(c.getIpAccessLists(), hasKey("EDGE-IN"));
    assertThat(c.getIpAccessLists(), hasKey("EDGE-OUT"));

    var edgeIn = c.getIpAccessLists().get("EDGE-IN");
    assertThat(edgeIn.getEntries().size(), equalTo(4));
    assertThat(edgeIn.getEntries().get(10L).getAction(), equalTo(LineAction.PERMIT));
    assertThat(edgeIn.getEntries().get(10L).getProtocol(), equalTo("tcp"));
    assertThat(edgeIn.getEntries().get(10L).getSource(), equalTo("10.0.0.0/8"));
    assertThat(edgeIn.getEntries().get(10L).getDestination(), equalTo("192.0.2.10"));
    assertThat(
        edgeIn.getEntries().get(12L).getAction(),
        equalTo(LineAction.PERMIT));
    assertThat(
        edgeIn.getEntries().get(12L).getProtocol(),
        equalTo("any"));
    assertThat(
        edgeIn.getEntries().get(12L).getSource(),
        equalTo("198.51.100.0/255.255.255.0"));
    assertThat(
        edgeIn.getEntries().get(12L).getDestination(),
        equalTo("any"));

    assertThat(edgeIn.getEntries().get(20L).getAction(), equalTo(LineAction.DENY));

    assertThat(edgeIn.getEntries().get(15L).getSourcePort(), notNullValue());
    assertThat(
        edgeIn.getEntries().get(15L).getSourcePort().getOperator(),
        equalTo(Operator.GT));
    assertThat(
        edgeIn.getEntries().get(15L).getSourcePort().getFirst(),
        equalTo(1023));
    assertThat(
        edgeIn.getEntries().get(15L).getDestinationPort().getOperator(),
        equalTo(Operator.EQ));
    assertThat(
        edgeIn.getEntries().get(15L).getDestinationPort().getFirst(),
        equalTo(443));

    var edgeOut = c.getIpAccessLists().get("EDGE-OUT");
    assertThat(edgeOut.getEntries(), hasKey(10L));
    assertThat(edgeOut.getEntries(), hasKey(20L));

    AosCxInterface physical = c.getInterfaces().get("1/1/2");
    assertThat(physical.getIncomingAcl(), equalTo("EDGE-IN"));
    assertThat(physical.getOutgoingAcl(), equalTo("EDGE-OUT"));

    AosCxInterface svi = c.getInterfaces().get("vlan 200");
    assertThat(svi.getIncomingAcl(), equalTo("EDGE-IN"));
  }

  @Test
  public void testAclConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-acl");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getIpAccessLists(), hasKey("EDGE-IN"));
    assertThat(c.getIpAccessLists(), hasKey("EDGE-OUT"));

    org.batfish.datamodel.IpAccessList edgeIn =
        c.getIpAccessLists().get("EDGE-IN");
    assertThat(edgeIn.getLines().size(), equalTo(4));

    org.batfish.datamodel.ExprAclLine first =
        (org.batfish.datamodel.ExprAclLine) edgeIn.getLines().get(0);
    org.batfish.datamodel.ExprAclLine second =
        (org.batfish.datamodel.ExprAclLine) edgeIn.getLines().get(1);
    org.batfish.datamodel.ExprAclLine third =
        (org.batfish.datamodel.ExprAclLine) edgeIn.getLines().get(2);
    org.batfish.datamodel.ExprAclLine fourth =
        (org.batfish.datamodel.ExprAclLine) edgeIn.getLines().get(3);

    assertThat(first.getAction(), equalTo(LineAction.PERMIT));
    assertThat(second.getAction(), equalTo(LineAction.PERMIT));
    assertThat(third.getAction(), equalTo(LineAction.PERMIT));
    assertThat(fourth.getAction(), equalTo(LineAction.DENY));

    org.batfish.datamodel.Interface physical =
        c.getAllInterfaces().get("1/1/2");
    assertThat(physical, notNullValue());
    assertThat(physical.getIncomingFilter(), notNullValue());
    assertThat(physical.getIncomingFilter().getName(), equalTo("EDGE-IN"));
    assertThat(physical.getOutgoingFilter(), notNullValue());
    assertThat(physical.getOutgoingFilter().getName(), equalTo("EDGE-OUT"));

    org.batfish.datamodel.Interface svi =
        c.getAllInterfaces().get("vlan 200");
    assertThat(svi, notNullValue());
    assertThat(svi.getIncomingFilter(), notNullValue());
    assertThat(svi.getIncomingFilter().getName(), equalTo("EDGE-IN"));
  }

  @Test
  public void testIpv6StaticRouteConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-static-routes-ipv6");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(c, notNullValue());

    assertThat(
        c.getDefaultVrf()
            .getStaticRoutes6()
            .size(),
        equalTo(6));

    StaticRoute6 nextHopRoute =
        c.getDefaultVrf()
            .getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getNetwork().equals(
                        Prefix6.parse(
                            "2001:db8:100::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        nextHopRoute.getNextHopIp(),
        equalTo(
            org.batfish.datamodel.Ip6.parse(
                "2001:db8:10::2")));

    assertThat(
        nextHopRoute.getNextHopInterface(),
        equalTo(
            org.batfish.datamodel.Route
                .UNSET_NEXT_HOP_INTERFACE));

    StaticRoute6 interfaceRoute =
        c.getDefaultVrf()
            .getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getNetwork().equals(
                        Prefix6.parse(
                            "2001:db8:200::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        interfaceRoute.getNextHopInterface(),
        equalTo("1/1/2"));

    StaticRoute6 nullRoute =
        c.getDefaultVrf()
            .getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getNetwork().equals(
                        Prefix6.parse(
                            "2001:db8:300::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        nullRoute.getNextHopInterface(),
        equalTo(
            org.batfish.datamodel.Interface
                .NULL_INTERFACE_NAME));

    assertThat(
        c.getVrfs(),
        hasKey("BLUE"));

    assertThat(
        c.getVrfs()
            .get("BLUE")
            .getStaticRoutes6()
            .size(),
        equalTo(1));

    assertThat(
        c.getVrfs()
            .get("BLUE")
            .getStaticRoutes6()
            .first()
            .getNetwork(),
        equalTo(
            Prefix6.parse(
                "2001:db8:500::/64")));
  }

  @Test
  public void testVrfExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-vrf");

    assertThat(c.getVrfs(), hasItem("BLUE"));

    AosCxInterface iface = c.getInterfaces().get("vlan 200");
    assertThat(iface, notNullValue());
    assertThat(iface.getVrfName(), equalTo("BLUE"));
    assertThat(
        iface.getAddress(),
        equalTo(ConcreteInterfaceAddress.parse("10.20.0.1/24")));

    assertThat(c.getStaticRoutes().size(), equalTo(2));
    assertThat(c.getStaticRoutes().get(0).getVrfName(), equalTo("BLUE"));
  }

  @Test
  public void testVrfConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-vrf");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getVrfs(), hasKey("default"));
    assertThat(c.getVrfs(), hasKey("BLUE"));

    org.batfish.datamodel.Interface iface =
        c.getAllInterfaces().get("vlan 200");
    assertThat(iface, notNullValue());
    assertThat(iface.getVrfName(), equalTo("BLUE"));

    assertThat(c.getVrfs().get("BLUE").getStaticRoutes().size(), equalTo(1));
    assertThat(
        c.getVrfs().get("BLUE").getStaticRoutes().first().getNetwork(),
        equalTo(Prefix.parse("10.30.0.0/24")));

    assertThat(c.getDefaultVrf().getStaticRoutes().size(), equalTo(1));
    assertThat(
        c.getDefaultVrf().getStaticRoutes().first().getNetwork(),
        equalTo(Prefix.ZERO));
  }



  @Test
  public void testOspfStubAreaExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-ospf-stub");

    assertThat(c.getOspfProcesses(), hasKey(1));
    assertThat(
        c.getOspfProcesses().get(1).getStubAreas().get("0.0.0.10"),
        equalTo(false));
    assertThat(
        c.getOspfProcesses().get(1).getStubAreas().get("0.0.0.20"),
        equalTo(true));
  }

  @Test
  public void testOspfStubAreaConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-ospf-stub");
    Configuration c = configs.get("aoscx-router");

    org.batfish.datamodel.ospf.OspfProcess process =
        c.getDefaultVrf().getOspfProcesses().get("1");

    assertThat(process, notNullValue());
    assertThat(process.getAreas(), hasKey(10L));
    assertThat(process.getAreas(), hasKey(20L));

    org.batfish.datamodel.ospf.OspfArea stub =
        process.getAreas().get(10L);

    assertThat(stub.getStubType(), equalTo(StubType.STUB));
    assertThat(stub.getStub(), notNullValue());
    assertThat(stub.getStub().getSuppressType3(), equalTo(false));
    assertThat(stub.getMetricOfDefaultRoute(), equalTo(1));

    org.batfish.datamodel.ospf.OspfArea totallyStubby =
        process.getAreas().get(20L);

    assertThat(totallyStubby.getStubType(), equalTo(StubType.STUB));
    assertThat(totallyStubby.getStub(), notNullValue());
    assertThat(
        totallyStubby.getStub().getSuppressType3(),
        equalTo(true));
    assertThat(totallyStubby.getMetricOfDefaultRoute(), equalTo(1));
  }

  @Test
  public void testOspfVrfExtraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-ospf-vrf");

    assertThat(c.getOspfProcesses(), hasKey(1));
    assertThat(
        c.getOspfProcesses().get(1).getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));

    assertThat(c.getOspfProcesses("BLUE"), hasKey(1));
    assertThat(
        c.getOspfProcesses("BLUE").get(1).getRouterId(),
        equalTo(Ip.parse("10.20.0.1")));

    AosCxInterface blueInterface =
        c.getInterfaces().get("vlan 200");
    assertThat(blueInterface, notNullValue());
    assertThat(blueInterface.getVrfName(), equalTo("BLUE"));
    assertThat(blueInterface.getOspfProcessId(), equalTo(1));
  }

  @Test
  public void testOspfVrfConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-ospf-vrf");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());

    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKey("1"));
    assertThat(
        c.getDefaultVrf()
            .getOspfProcesses()
            .get("1")
            .getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));

    assertThat(c.getVrfs(), hasKey("BLUE"));
    assertThat(
        c.getVrfs().get("BLUE").getOspfProcesses(),
        hasKey("1"));
    assertThat(
        c.getVrfs()
            .get("BLUE")
            .getOspfProcesses()
            .get("1")
            .getRouterId(),
        equalTo(Ip.parse("10.20.0.1")));

    org.batfish.datamodel.Interface blueInterface =
        c.getAllInterfaces().get("vlan 200");
    assertThat(blueInterface, notNullValue());
    assertThat(blueInterface.getVrfName(), equalTo("BLUE"));
    assertThat(blueInterface.getOspfSettings(), notNullValue());
    assertThat(
        blueInterface.getOspfSettings().getProcess(),
        equalTo("1"));
  }

  @Test
  public void testOspfExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-ospf");

    assertThat(c.getOspfProcesses(), hasKey(1));
    assertThat(c.getOspfProcesses().get(1).getRouterId(), equalTo(Ip.parse("192.0.2.41")));
    assertThat(c.getOspfProcesses().get(1).getRedistributeConnected(), equalTo(true));

    AosCxInterface iface = c.getInterfaces().get("1/1/2");
    assertThat(iface, notNullValue());
    assertThat(iface.getBandwidth(), equalTo(1_000_000_000D));
    assertThat(iface.getOspfProcessId(), equalTo(1));
    assertThat(iface.getOspfArea(), equalTo("0.0.0.0"));
    assertThat(iface.getOspfCost(), equalTo(25));
    assertThat(
        iface.getOspfNetworkType(),
        equalTo(AosCxInterface.OspfNetworkType.POINT_TO_POINT));
  }




  @Test
  public void testBgpVrfExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-bgp-vrf");

    assertThat(c.getBgpProcess(), notNullValue());
    assertThat(c.getBgpProcess().getLocalAs(), equalTo(65000L));
    assertThat(
        c.getBgpProcess().getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        c.getBgpProcess().getNeighbors(),
        hasKey(Ip.parse("192.0.2.2")));

    assertThat(c.getBgpProcess("BLUE"), notNullValue());
    assertThat(c.getBgpProcess("BLUE").getLocalAs(), equalTo(65000L));
    assertThat(
        c.getBgpProcess("BLUE").getRouterId(),
        equalTo(Ip.parse("10.20.0.1")));
    assertThat(
        c.getBgpProcess("BLUE").getNeighbors(),
        hasKey(Ip.parse("10.20.0.2")));
    assertThat(
        c.getBgpProcess("BLUE")
            .getNeighbors()
            .get(Ip.parse("10.20.0.2"))
            .getRemoteAs(),
        equalTo(65100L));
    assertThat(
        c.getBgpProcess("BLUE")
            .getNeighbors()
            .get(Ip.parse("10.20.0.2"))
            .getIpv4UnicastActive(),
        equalTo(true));
  }

  @Test
  public void testBgpVrfConversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-bgp-vrf");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());

    BgpProcess defaultProcess =
        c.getDefaultVrf().getBgpProcess();
    assertThat(defaultProcess, notNullValue());
    assertThat(
        defaultProcess.getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        defaultProcess.getActiveNeighbors(),
        hasKey(Ip.parse("192.0.2.2")));

    assertThat(c.getVrfs(), hasKey("BLUE"));

    BgpProcess blueProcess =
        c.getVrfs().get("BLUE").getBgpProcess();
    assertThat(blueProcess, notNullValue());
    assertThat(
        blueProcess.getRouterId(),
        equalTo(Ip.parse("10.20.0.1")));
    assertThat(
        blueProcess.getActiveNeighbors(),
        hasKey(Ip.parse("10.20.0.2")));

    BgpActivePeerConfig bluePeer =
        blueProcess
            .getActiveNeighbors()
            .get(Ip.parse("10.20.0.2"));

    assertThat(bluePeer.getLocalAs(), equalTo(65000L));
    assertThat(
        bluePeer.getRemoteAsns().contains(65100L),
        equalTo(true));
    assertThat(
        bluePeer.getIpv4UnicastAddressFamily(),
        notNullValue());
  }

  @Test
  public void testBgpExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-bgp");

    assertThat(c.getBgpProcess(), notNullValue());
    assertThat(c.getBgpProcess().getLocalAs(), equalTo(65000L));
    assertThat(c.getBgpProcess().getRouterId(), equalTo(Ip.parse("192.0.2.41")));
    assertThat(c.getBgpProcess().getNeighbors(), hasKey(Ip.parse("10.255.1.1")));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getRemoteAs(),
        equalTo(65001L));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getIpv4UnicastActive(),
        equalTo(true));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getRouteMapIn(),
        equalTo("FROM-CORE"));
    assertThat(
        c.getBgpProcess().getNeighbors().get(Ip.parse("10.255.1.1")).getRouteMapOut(),
        equalTo("TO-CORE"));
  }



  @Test
  public void testPrefixListExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-prefix-lists");

    assertThat(c.getPrefixLists(), hasKey("DEFAULT"));
    assertThat(c.getPrefixLists(), hasKey("INTERNAL"));
    assertThat(c.getPrefixLists(), hasKey("BLOCK"));

    AosCxPrefixList defaultList = c.getPrefixLists().get("DEFAULT");
    assertThat(defaultList.getEntries(), hasKey(10L));
    AosCxPrefixListEntry defaultEntry = defaultList.getEntries().get(10L);
    assertThat(defaultEntry.getAction(), equalTo(LineAction.PERMIT));
    assertThat(defaultEntry.getPrefix(), equalTo(Prefix.ZERO));

    AosCxPrefixList internal = c.getPrefixLists().get("INTERNAL");
    AosCxPrefixListEntry internalEntry = internal.getEntries().get(20L);
    assertThat(internalEntry.getAction(), equalTo(LineAction.PERMIT));
    assertThat(internalEntry.getPrefix(), equalTo(Prefix.parse("10.0.0.0/8")));
    assertThat(internalEntry.getGe(), equalTo(16));
    assertThat(internalEntry.getLe(), equalTo(24));

    AosCxPrefixList block = c.getPrefixLists().get("BLOCK");
    assertThat(block.getEntries(), hasKey(10L));
    assertThat(block.getEntries(), hasKey(20L));
    assertThat(block.getEntries().get(10L).getAction(), equalTo(LineAction.DENY));
    assertThat(
        block.getEntries().get(10L).getPrefix(),
        equalTo(Prefix.parse("192.0.2.0/24")));
    assertThat(block.getEntries().get(20L).getAction(), equalTo(LineAction.PERMIT));
  }



  @Test
  public void testRouteMapExtraction() throws IOException {
    AosCxConfiguration c = parseVendorConfig("aoscx-route-maps");

    assertThat(c.getRouteMaps(), hasKey("FROM-CORE"));

    AosCxRouteMap routeMap = c.getRouteMaps().get("FROM-CORE");
    assertThat(routeMap.getEntries(), hasKey(10L));
    assertThat(routeMap.getEntries(), hasKey(20L));

    AosCxRouteMapEntry entry10 = routeMap.getEntries().get(10L);
    assertThat(entry10.getAction(), equalTo(LineAction.PERMIT));
    assertThat(entry10.getMatchPrefixList(), equalTo("INTERNAL"));
    assertThat(entry10.getSetLocalPreference(), equalTo(200L));

    AosCxRouteMapEntry entry20 = routeMap.getEntries().get(20L);
    assertThat(entry20.getAction(), equalTo(LineAction.DENY));
  }


  @Test
  public void testRouteMapConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-route-maps");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getRoutingPolicies(), hasKey("FROM-CORE"));
    assertThat(c.getRoutingPolicies(), hasKey("ONLY-INTERNAL"));

    RoutingPolicy fromCore = c.getRoutingPolicies().get("FROM-CORE");

    Bgpv4Route internalRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("10.1.0.0/16"))
            .build();
    Bgpv4Route.Builder internalOutput = Bgpv4Route.testBuilder();

    assertThat(fromCore.process(internalRoute, internalOutput, Direction.IN), equalTo(true));
    assertThat(internalOutput.getLocalPreference(), equalTo(200L));

    Bgpv4Route externalRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("203.0.113.0/24"))
            .build();

    assertThat(
        fromCore.process(externalRoute, Bgpv4Route.testBuilder(), Direction.IN),
        equalTo(false));

    RoutingPolicy onlyInternal = c.getRoutingPolicies().get("ONLY-INTERNAL");

    assertThat(
        onlyInternal.process(internalRoute, Bgpv4Route.testBuilder(), Direction.IN),
        equalTo(true));

    assertThat(
        onlyInternal.process(externalRoute, Bgpv4Route.testBuilder(), Direction.IN),
        equalTo(false));
  }

  @Test
  public void testPrefixListConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-prefix-lists");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getRouteFilterLists(), hasKey("DEFAULT"));
    assertThat(c.getRouteFilterLists(), hasKey("INTERNAL"));
    assertThat(c.getRouteFilterLists(), hasKey("BLOCK"));

    RouteFilterList defaultList = c.getRouteFilterLists().get("DEFAULT");
    assertThat(defaultList.permits(Prefix.ZERO), equalTo(true));
    assertThat(defaultList.permits(Prefix.parse("10.0.0.0/8")), equalTo(false));

    RouteFilterList internal = c.getRouteFilterLists().get("INTERNAL");
    assertThat(internal.permits(Prefix.parse("10.1.0.0/16")), equalTo(true));
    assertThat(internal.permits(Prefix.parse("10.1.1.0/24")), equalTo(true));
    assertThat(internal.permits(Prefix.parse("10.0.0.0/8")), equalTo(false));
    assertThat(internal.permits(Prefix.parse("10.1.1.128/25")), equalTo(false));

    RouteFilterList block = c.getRouteFilterLists().get("BLOCK");
    assertThat(block.permits(Prefix.parse("192.0.2.0/24")), equalTo(false));
    assertThat(block.permits(Prefix.parse("198.51.100.0/24")), equalTo(true));
    assertThat(block.permits(Prefix.parse("203.0.113.0/24")), equalTo(false));
  }

  @Test
  public void testBgpConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-bgp");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getDefaultVrf().getBgpProcess(), notNullValue());

    BgpProcess process = c.getDefaultVrf().getBgpProcess();
    assertThat(process.getRouterId(), equalTo(Ip.parse("192.0.2.41")));
    assertThat(process.getActiveNeighbors(), hasKey(Ip.parse("10.255.1.1")));

    BgpActivePeerConfig peer =
        process.getActiveNeighbors().get(Ip.parse("10.255.1.1"));

    assertThat(peer.getLocalAs(), equalTo(65000L));
    assertThat(peer.getRemoteAsns().contains(65001L), equalTo(true));
    assertThat(peer.getIpv4UnicastAddressFamily(), notNullValue());
    assertThat(
        peer.getIpv4UnicastAddressFamily().getImportPolicy(),
        equalTo("FROM-CORE"));
    assertThat(
        peer.getIpv4UnicastAddressFamily().getExportPolicy(),
        equalTo("TO-CORE"));
  }

  @Test
  public void testOspfConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-ospf");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKey("1"));

    org.batfish.datamodel.ospf.OspfProcess process =
        c.getDefaultVrf().getOspfProcesses().get("1");

    assertThat(process.getRouterId(), equalTo(Ip.parse("192.0.2.41")));
    assertThat(process.getAreas(), hasKey(0L));
    assertThat(process.getAreas().get(0L).getInterfaces(), hasItem("1/1/2"));

    assertThat(process.getExportPolicy(), notNullValue());
    RoutingPolicy exportPolicy =
        c.getRoutingPolicies().get(process.getExportPolicy());
    assertThat(exportPolicy, notNullValue());

    org.batfish.datamodel.ConnectedRoute connectedRoute =
        new org.batfish.datamodel.ConnectedRoute(
            Prefix.parse("10.10.0.0/24"), "1/1/99");
    org.batfish.datamodel.OspfExternalRoute.Builder connectedOutput =
        org.batfish.datamodel.OspfExternalRoute.builder();

    assertThat(
        exportPolicy.process(
            connectedRoute, connectedOutput, Direction.OUT),
        equalTo(true));
    assertThat(
        connectedOutput.getOspfMetricType(),
        equalTo(org.batfish.datamodel.ospf.OspfMetricType.E2));
    assertThat(connectedOutput.getMetric(), equalTo(25L));

    StaticRoute staticRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("203.0.113.0/24"))
            .build();

    assertThat(
        exportPolicy.process(
            staticRoute,
            org.batfish.datamodel.OspfExternalRoute.builder(),
            Direction.OUT),
        equalTo(false));

    org.batfish.datamodel.Interface iface = c.getAllInterfaces().get("1/1/2");
    assertThat(iface.getOspfSettings(), notNullValue());
    assertThat(iface.getOspfSettings().getAreaName(), equalTo(0L));
    assertThat(iface.getOspfSettings().getCost(), equalTo(25));
    assertThat(iface.getOspfSettings().getProcess(), equalTo("1"));
    assertThat(iface.getOspfSettings().getNetworkType(), equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(iface.getOspfSettings().getHelloInterval(), equalTo(10));
    assertThat(iface.getOspfSettings().getDeadInterval(), equalTo(40));
  }

  @Test
  public void testStaticRouteConversion() throws IOException {
    Map<String, Configuration> configs = parseTextConfigs("aoscx-static-routes");
    Configuration c = configs.get("aoscx-router");

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
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());
    assertThat(c.getConfigurationFormat(), equalTo(ARUBA_AOSCX));

    org.batfish.datamodel.Interface physical = c.getAllInterfaces().get("1/1/2");
    assertThat(physical, notNullValue());
    assertThat(physical.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(physical.getAdminUp(), equalTo(true));
    assertThat(
        physical.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("192.0.2.2/30")));

    org.batfish.datamodel.Interface loopback =
        c.getAllInterfaces().get("loopback 0");
    assertThat(loopback, notNullValue());
    assertThat(loopback.getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(
        loopback.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("192.0.2.41/32")));

    org.batfish.datamodel.Interface vlan =
        c.getAllInterfaces().get("vlan 1000");
    assertThat(vlan, notNullValue());
    assertThat(vlan.getInterfaceType(), equalTo(InterfaceType.VLAN));
    assertThat(vlan.getAdminUp(), equalTo(false));
    assertThat(
        vlan.getAllAddresses(),
        contains(ConcreteInterfaceAddress.parse("198.51.100.137/30")));
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

  @Test
  public void testOspfv3Conversion() throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs("aoscx-ospfv3");
    Configuration c = configs.get("aoscx-router");

    assertThat(c, notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf().getOspfv3Processes().get("1");

    assertThat(process, notNullValue());
    assertThat(
        process.getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(process.getAdminCost(), equalTo(110));
    assertThat(
        process.getReferenceBandwidth(),
        equalTo(40_000_000_000D));
    assertThat(
        process.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        process.getRedistributionMetric(),
        equalTo(25L));

    long area0 = 0L;
    long area10 = Ip.parse("10.0.0.1").asLong();

    assertThat(process.getAreas(), hasKey(area0));
    assertThat(process.getAreas(), hasKey(area10));

    assertThat(
        process.getAreas().get(area0).getInterfaces(),
        contains("1/1/1"));
    assertThat(
        process.getAreas().get(area10).getInterfaces(),
        contains("1/1/2"));

    org.batfish.datamodel.Interface routed =
        c.getAllInterfaces().get("1/1/1");
    assertThat(routed, notNullValue());
    assertThat(
        routed.getAllConcreteAddresses6(),
        contains(
            ConcreteInterfaceAddress6.parse(
                "2001:db8:10::1/64")));

    assertThat(routed.getOspfv3Settings(), notNullValue());
    assertThat(
        routed.getOspfv3Settings().getAreaName(),
        equalTo(area0));
    assertThat(
        routed.getOspfv3Settings().getCost(),
        equalTo(25));
    assertThat(
        routed.getOspfv3Settings().getProcess(),
        equalTo("1"));
    assertThat(
        routed.getOspfv3Settings().getNetworkType(),
        equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        routed.getOspfv3Settings().getHelloInterval(),
        equalTo(10));
    assertThat(
        routed.getOspfv3Settings().getDeadInterval(),
        equalTo(40));
    assertThat(
        routed.getOspfv3Settings().getEnabled(),
        equalTo(true));
    assertThat(
        routed.getOspfv3Settings().getPassive(),
        equalTo(false));

    org.batfish.datamodel.Interface linkLocal =
        c.getAllInterfaces().get("1/1/2");
    assertThat(linkLocal, notNullValue());
    assertThat(linkLocal.getOspfv3Settings(), notNullValue());
    assertThat(
        linkLocal.getOspfv3Settings().getAreaName(),
        equalTo(area10));
    assertThat(
        linkLocal.getOspfv3Settings().getProcess(),
        equalTo("1"));
    assertThat(
        linkLocal.getOspfv3Settings().getNetworkType(),
        equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        linkLocal.getOspfv3Settings().getPassive(),
        equalTo(true));
  }

  @Test
  public void testOspfv3Extraction() {
    AosCxConfiguration c = parseVendorConfig("aoscx-ospfv3");

    assertThat(c.getOspfv3Processes(), hasKey(1));

    AosCxOspfv3Process process = c.getOspfv3Processes().get(1);
    assertThat(process.getRouterId(), equalTo(Ip.parse("192.0.2.1")));
    assertThat(process.getRedistributeConnected(), equalTo(true));
    assertThat(
        process.getReferenceBandwidth(),
        equalTo(40_000_000_000D));
    assertThat(
        process.getPassiveInterfaceDefault(),
        equalTo(true));
    assertThat(
        process.getAreas(),
        containsInAnyOrder("0.0.0.0", "10.0.0.1"));

    AosCxInterface routed = c.getInterfaces().get("1/1/1");
    assertThat(routed, notNullValue());
    assertThat(routed.getOspfv3ProcessId(), equalTo(1));
    assertThat(routed.getOspfv3Area(), equalTo("0.0.0.0"));
    assertThat(routed.getOspfv3Cost(), equalTo(25));
    assertThat(
        routed.getOspfv3Passive(),
        equalTo(false));
    assertThat(
        routed.getOspfv3NetworkType(),
        equalTo(AosCxInterface.OspfNetworkType.POINT_TO_POINT));

    AosCxInterface linkLocal = c.getInterfaces().get("1/1/2");
    assertThat(linkLocal, notNullValue());
    assertThat(linkLocal.getIpv6LinkLocalEnabled(), equalTo(true));
    assertThat(linkLocal.getOspfv3ProcessId(), equalTo(1));
    assertThat(linkLocal.getOspfv3Area(), equalTo("10.0.0.1"));
    assertThat(
        linkLocal.getOspfv3NetworkType(),
        equalTo(AosCxInterface.OspfNetworkType.POINT_TO_POINT));
  }


  @Test
  public void testIpv6StaticRouteAttributes()
      throws IOException {
    AosCxConfiguration vc =
        parseVendorConfig(
            "aoscx-static-routes-ipv6");

    AosCxStaticRoute6 distanceRoute =
        vc.getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getPrefix().equals(
                        Prefix6.parse(
                            "2001:db8:600::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        distanceRoute.getAdministrativeDistance(),
        equalTo(200L));

    assertThat(
        distanceRoute.getTag(),
        equalTo(
            org.batfish.datamodel.Route
                .UNSET_ROUTE_TAG));

    AosCxStaticRoute6 taggedRoute =
        vc.getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getPrefix().equals(
                        Prefix6.parse(
                            "2001:db8:700::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        taggedRoute.getAdministrativeDistance(),
        equalTo(1L));

    assertThat(
        taggedRoute.getTag(),
        equalTo(12345L));

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-static-routes-ipv6");

    Configuration c =
        configs.get("aoscx-router");

    StaticRoute6 viDistanceRoute =
        c.getDefaultVrf()
            .getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getNetwork().equals(
                        Prefix6.parse(
                            "2001:db8:600::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        viDistanceRoute
            .getAdministrativeCost(),
        equalTo(200L));

    StaticRoute6 viTaggedRoute =
        c.getDefaultVrf()
            .getStaticRoutes6()
            .stream()
            .filter(
                route ->
                    route.getNetwork().equals(
                        Prefix6.parse(
                            "2001:db8:700::/64")))
            .findFirst()
            .orElseThrow();

    assertThat(
        viTaggedRoute.getTag(),
        equalTo(12345L));
  }

  @Test
  public void testOspfv3VrfExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-vrf");

    assertThat(
        c.getOspfv3Processes(),
        hasKey(1));

    assertThat(
        c.getOspfv3Processes()
            .get(1)
            .getRouterId(),
        equalTo(
            Ip.parse("192.0.2.1")));

    assertThat(
        c.getOspfv3Processes("BLUE"),
        hasKey(1));

    AosCxOspfv3Process blue =
        c.getOspfv3Processes("BLUE")
            .get(1);

    assertThat(
        blue.getRouterId(),
        equalTo(
            Ip.parse("192.0.2.200")));

    assertThat(
        blue.getRedistributeConnected(),
        equalTo(true));

    assertThat(
        blue.getAreas(),
        contains("0.0.0.10"));

    AosCxInterface blueInterface =
        c.getInterfaces()
            .get("vlan 200");

    assertThat(
        blueInterface,
        notNullValue());

    assertThat(
        blueInterface.getVrfName(),
        equalTo("BLUE"));

    assertThat(
        blueInterface.getOspfv3ProcessId(),
        equalTo(1));

    assertThat(
        blueInterface.getOspfv3Area(),
        equalTo("0.0.0.10"));

    assertThat(
        blueInterface.getOspfv3Cost(),
        equalTo(15));
  }

  @Test
  public void testOspfv3VrfConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-vrf");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(c, notNullValue());

    assertThat(
        c.getDefaultVrf()
            .getOspfv3Processes(),
        hasKey("1"));

    org.batfish.datamodel.ospf.Ospfv3Process
        defaultProcess =
            c.getDefaultVrf()
                .getOspfv3Processes()
                .get("1");

    assertThat(
        defaultProcess.getRouterId(),
        equalTo(
            Ip.parse("192.0.2.1")));

    assertThat(
        defaultProcess.getAreas(),
        hasKey(0L));

    assertThat(
        defaultProcess.getAreas()
            .get(0L)
            .getInterfaces(),
        contains("1/1/1"));

    assertThat(
        c.getVrfs(),
        hasKey("BLUE"));

    org.batfish.datamodel.ospf.Ospfv3Process
        blueProcess =
            c.getVrfs()
                .get("BLUE")
                .getOspfv3Processes()
                .get("1");

    assertThat(
        blueProcess,
        notNullValue());

    assertThat(
        blueProcess.getRouterId(),
        equalTo(
            Ip.parse("192.0.2.200")));

    assertThat(
        blueProcess.getRedistributeConnected(),
        equalTo(true));

    assertThat(
        blueProcess.getAreas(),
        hasKey(10L));

    assertThat(
        blueProcess.getAreas()
            .get(10L)
            .getInterfaces(),
        contains("vlan 200"));

    org.batfish.datamodel.Interface blueInterface =
        c.getAllInterfaces()
            .get("vlan 200");

    assertThat(
        blueInterface.getVrfName(),
        equalTo("BLUE"));

    assertThat(
        blueInterface.getOspfv3Settings(),
        notNullValue());

    assertThat(
        blueInterface
            .getOspfv3Settings()
            .getProcess(),
        equalTo("1"));

    assertThat(
        blueInterface
            .getOspfv3Settings()
            .getAreaName(),
        equalTo(10L));

    assertThat(
        blueInterface
            .getOspfv3Settings()
            .getCost(),
        equalTo(15));
  }

  @Test
  public void testOspfv3InterfaceKnobsExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-interface-knobs");

    AosCxInterface configured =
        c.getInterfaces().get("1/1/1");

    assertThat(configured, notNullValue());
    assertThat(
        configured.getOspfv3Cost(),
        equalTo(25));
    assertThat(
        configured.getOspfv3HelloInterval(),
        equalTo(5));
    assertThat(
        configured.getOspfv3DeadInterval(),
        equalTo(20));
    assertThat(
        configured.getOspfv3NetworkType(),
        equalTo(
            AosCxInterface.OspfNetworkType
                .POINT_TO_POINT));

    AosCxInterface broadcast =
        c.getInterfaces().get("1/1/2");

    assertThat(
        broadcast.getOspfv3NetworkType(),
        equalTo(
            AosCxInterface.OspfNetworkType
                .BROADCAST));

    AosCxInterface reset =
        c.getInterfaces().get("1/1/3");

    assertThat(reset, notNullValue());
    assertThat(
        reset.getOspfv3Cost(),
        equalTo(null));
    assertThat(
        reset.getOspfv3HelloInterval(),
        equalTo(null));
    assertThat(
        reset.getOspfv3DeadInterval(),
        equalTo(null));
    assertThat(
        reset.getOspfv3NetworkType(),
        equalTo(null));
  }

  @Test
  public void testOspfv3InterfaceKnobsConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-interface-knobs");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.Interface configured =
        c.getAllInterfaces().get("1/1/1");

    assertThat(
        configured.getOspfv3Settings(),
        notNullValue());
    assertThat(
        configured.getOspfv3Settings().getCost(),
        equalTo(25));
    assertThat(
        configured
            .getOspfv3Settings()
            .getHelloInterval(),
        equalTo(5));
    assertThat(
        configured
            .getOspfv3Settings()
            .getDeadInterval(),
        equalTo(20));
    assertThat(
        configured
            .getOspfv3Settings()
            .getNetworkType(),
        equalTo(
            OspfNetworkType.POINT_TO_POINT));

    org.batfish.datamodel.Interface broadcast =
        c.getAllInterfaces().get("1/1/2");

    assertThat(
        broadcast
            .getOspfv3Settings()
            .getNetworkType(),
        equalTo(
            OspfNetworkType.BROADCAST));

    org.batfish.datamodel.Interface reset =
        c.getAllInterfaces().get("1/1/3");

    assertThat(
        reset.getOspfv3Settings(),
        notNullValue());
    assertThat(
        reset.getOspfv3Settings().getCost(),
        equalTo(null));
    assertThat(
        reset
            .getOspfv3Settings()
            .getHelloInterval(),
        equalTo(10));
    assertThat(
        reset
            .getOspfv3Settings()
            .getDeadInterval(),
        equalTo(40));
    assertThat(
        reset
            .getOspfv3Settings()
            .getNetworkType(),
        equalTo(
            OspfNetworkType.BROADCAST));
  }

  @Test
  public void testOspfv3AutomaticRouterId()
      throws IOException {
    AosCxConfiguration vc =
        parseVendorConfig(
            "aoscx-ospfv3-auto-router-id");

    // No explicit router IDs were configured.
    assertThat(
        vc.getOspfv3Processes()
            .get(1)
            .getRouterId(),
        equalTo(null));

    assertThat(
        vc.getOspfv3Processes("BLUE")
            .get(2)
            .getRouterId(),
        equalTo(null));

    assertThat(
        vc.getOspfv3Processes("RED")
            .get(3)
            .getRouterId(),
        equalTo(null));

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-auto-router-id");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(c, notNullValue());

    /*
     * Default VRF: loopback wins over the numerically higher
     * physical-interface address.
     */
    org.batfish.datamodel.ospf.Ospfv3Process
        defaultProcess =
            c.getDefaultVrf()
                .getOspfv3Processes()
                .get("1");

    assertThat(
        defaultProcess,
        notNullValue());

    assertThat(
        defaultProcess.getRouterId(),
        equalTo(
            Ip.parse("192.0.2.10")));

    /*
     * BLUE: router-ID selection is VRF-local and again prefers
     * the loopback.
     */
    org.batfish.datamodel.ospf.Ospfv3Process
        blueProcess =
            c.getVrfs()
                .get("BLUE")
                .getOspfv3Processes()
                .get("2");

    assertThat(
        blueProcess,
        notNullValue());

    assertThat(
        blueProcess.getRouterId(),
        equalTo(
            Ip.parse("192.0.2.200")));

    /*
     * RED: there is no IPv4 loopback, so the highest active
     * physical-interface IPv4 address is selected.
     */
    org.batfish.datamodel.ospf.Ospfv3Process
        redProcess =
            c.getVrfs()
                .get("RED")
                .getOspfv3Processes()
                .get("3");

    assertThat(
        redProcess,
        notNullValue());

    assertThat(
        redProcess.getRouterId(),
        equalTo(
            Ip.parse("10.0.2.200")));
  }


  @Test
  public void testOspfv3StubAreaExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-stub");

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(process, notNullValue());

    assertThat(
        process.getAreas(),
        containsInAnyOrder(
            "0.0.0.0",
            "0.0.0.10",
            "0.0.0.20",
            "0.0.0.30",
            "0.0.0.40",
            "0.0.0.50"));

    /*
     * Ordinary stub remains ordinary stub.
     */
    assertThat(
        process.getStubAreas()
            .get("0.0.0.10"),
        equalTo(false));

    /*
     * no-summary remains set.
     */
    assertThat(
        process.getStubAreas()
            .get("0.0.0.20"),
        equalTo(true));

    /*
     * no area ... stub no-summary retains stub status
     * but clears summary suppression.
     */
    assertThat(
        process.getStubAreas()
            .get("0.0.0.30"),
        equalTo(false));

    /*
     * no area ... stub returns the area to normal.
     */
    assertThat(
        process.getStubAreas()
            .containsKey("0.0.0.40"),
        equalTo(false));

    assertThat(
        process.getAreaDefaultMetrics()
            .get("0.0.0.10"),
        equalTo(7L));

    assertThat(
        process.getAreaDefaultMetrics()
            .get("0.0.0.20"),
        equalTo(11L));

    /*
     * no area ... default-metric restores the default,
     * represented by absence of an explicit override.
     */
    assertThat(
        process.getAreaDefaultMetrics()
            .containsKey("0.0.0.50"),
        equalTo(false));
  }

  @Test
  public void testOspfv3StubAreaConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-stub");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process
        process =
            c.getDefaultVrf()
                .getOspfv3Processes()
                .get("1");

    assertThat(process, notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Area
        backbone =
            process.getAreas().get(0L);

    org.batfish.datamodel.ospf.Ospfv3Area
        stub =
            process.getAreas().get(10L);

    org.batfish.datamodel.ospf.Ospfv3Area
        totallyStubby =
            process.getAreas().get(20L);

    org.batfish.datamodel.ospf.Ospfv3Area
        resetNoSummary =
            process.getAreas().get(30L);

    org.batfish.datamodel.ospf.Ospfv3Area
        resetStub =
            process.getAreas().get(40L);

    org.batfish.datamodel.ospf.Ospfv3Area
        resetMetric =
            process.getAreas().get(50L);

    assertThat(backbone, notNullValue());
    assertThat(
        backbone.getStub(),
        equalTo(false));
    assertThat(
        backbone.getDefaultMetric(),
        equalTo(1L));

    assertThat(stub, notNullValue());
    assertThat(
        stub.getStub(),
        equalTo(true));
    assertThat(
        stub.getSuppressInterArea(),
        equalTo(false));
    assertThat(
        stub.getDefaultMetric(),
        equalTo(7L));

    assertThat(totallyStubby, notNullValue());
    assertThat(
        totallyStubby.getStub(),
        equalTo(true));
    assertThat(
        totallyStubby.getSuppressInterArea(),
        equalTo(true));
    assertThat(
        totallyStubby.getDefaultMetric(),
        equalTo(11L));

    assertThat(resetNoSummary, notNullValue());
    assertThat(
        resetNoSummary.getStub(),
        equalTo(true));
    assertThat(
        resetNoSummary.getSuppressInterArea(),
        equalTo(false));

    assertThat(resetStub, notNullValue());
    assertThat(
        resetStub.getStub(),
        equalTo(false));

    assertThat(resetMetric, notNullValue());
    assertThat(
        resetMetric.getStub(),
        equalTo(true));
    assertThat(
        resetMetric.getDefaultMetric(),
        equalTo(1L));
  }

  @Test
  public void testOspfv3ExternalControlsExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-external-controls");

    AosCxOspfv3Process p1 =
        c.getOspfv3Processes().get(1);

    assertThat(p1, notNullValue());
    assertThat(
        p1.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        p1.getRedistributeStatic(),
        equalTo(true));
    assertThat(
        p1.getRedistributionMetric(),
        equalTo(36L));
    assertThat(
        p1.getDefaultInformationOriginate(),
        equalTo(true));
    assertThat(
        p1.getDefaultInformationOriginateAlways(),
        equalTo(true));
    assertThat(
        p1.getDefaultInformationMetric(),
        equalTo(7L));

    AosCxOspfv3Process p2 =
        c.getOspfv3Processes().get(2);

    assertThat(p2, notNullValue());
    assertThat(
        p2.getRedistributeConnected(),
        equalTo(false));
    assertThat(
        p2.getRedistributeStatic(),
        equalTo(false));
    assertThat(
        p2.getRedistributionMetric(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));
    assertThat(
        p2.getDefaultInformationOriginate(),
        equalTo(false));
    assertThat(
        p2.getDefaultInformationOriginateAlways(),
        equalTo(false));
    assertThat(
        p2.getDefaultInformationMetric(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_INFORMATION_METRIC));
  }

  @Test
  public void testOspfv3ExternalControlsConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-external-controls");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(p1, notNullValue());
    assertThat(
        p1.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        p1.getRedistributeStatic(),
        equalTo(true));
    assertThat(
        p1.getRedistributionMetric(),
        equalTo(36L));
    assertThat(
        p1.getDefaultInformationOriginate(),
        equalTo(true));
    assertThat(
        p1.getDefaultInformationOriginateAlways(),
        equalTo(true));
    assertThat(
        p1.getDefaultInformationMetric(),
        equalTo(7L));

    org.batfish.datamodel.ospf.Ospfv3Process p2 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("2");

    assertThat(p2, notNullValue());
    assertThat(
        p2.getRedistributeConnected(),
        equalTo(false));
    assertThat(
        p2.getRedistributeStatic(),
        equalTo(false));
    assertThat(
        p2.getRedistributionMetric(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Process
                .DEFAULT_REDISTRIBUTION_METRIC));
    assertThat(
        p2.getDefaultInformationOriginate(),
        equalTo(false));
    assertThat(
        p2.getDefaultInformationOriginateAlways(),
        equalTo(false));
    assertThat(
        p2.getDefaultInformationMetric(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Process
                .DEFAULT_INFORMATION_METRIC));
  }

  @Test
  public void testOspfv3RedistributionRouteMapExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-redistribution-route-map");

    assertThat(
        c.getIpv6PrefixLists(),
        hasKey("CONN_OK"));

    assertThat(
        c.getIpv6PrefixLists()
            .get("CONN_OK")
            .getEntries()
            .get(10L)
            .getPrefix(),
        equalTo(
            Prefix6.parse(
                "2001:db8:100::/48")));

    assertThat(
        c.getIpv6PrefixLists()
            .get("CONN_OK")
            .getEntries()
            .get(10L)
            .getGe(),
        equalTo(64));

    assertThat(
        c.getIpv6PrefixLists()
            .get("CONN_OK")
            .getEntries()
            .get(10L)
            .getLe(),
        equalTo(64));

    AosCxRouteMapEntry connEntry =
        c.getRouteMaps()
            .get("RM-CONN")
            .getEntries()
            .get(10L);

    assertThat(
        connEntry.getMatchIpv6PrefixList(),
        equalTo("CONN_OK"));
    assertThat(
        connEntry.getSetMetric(),
        equalTo(31L));
    assertThat(
        connEntry.getSetOspfMetricType(),
        equalTo(OspfMetricType.E1));
    assertThat(
        connEntry.getSetTag(),
        equalTo(101L));

    AosCxRouteMapEntry staticDeny =
        c.getRouteMaps()
            .get("RM-STATIC")
            .getEntries()
            .get(10L);

    assertThat(
        staticDeny.getAction(),
        equalTo(LineAction.DENY));
    assertThat(
        staticDeny.getMatchIpv6PrefixList(),
        equalTo("STATIC_BLOCK"));

    AosCxRouteMapEntry staticPermit =
        c.getRouteMaps()
            .get("RM-STATIC")
            .getEntries()
            .get(20L);

    assertThat(
        staticPermit.getSetOspfMetricType(),
        equalTo(OspfMetricType.E2));

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(process, notNullValue());

    assertThat(
        process.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        process.getRedistributeConnectedRouteMap(),
        equalTo("RM-CONN"));

    assertThat(
        process.getRedistributeStatic(),
        equalTo(true));
    assertThat(
        process.getRedistributeStaticRouteMap(),
        equalTo("RM-STATIC"));
  }

  @Test
  public void testOspfv3RedistributionRouteMapConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-redistribution-route-map");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process
        process =
            c.getDefaultVrf()
                .getOspfv3Processes()
                .get("1");

    assertThat(process, notNullValue());

    RouteMap6 connectedMap =
        process.getRedistributeConnectedRouteMap();

    assertThat(
        connectedMap,
        notNullValue());

    RouteMap6.Result connectedAllowed =
        connectedMap
            .process(
                Prefix6.parse(
                    "2001:db8:100:1::/64"),
                process.getRedistributionMetric(),
                Route.UNSET_ROUTE_TAG)
            .orElseThrow();

    assertThat(
        connectedAllowed.getMetric(),
        equalTo(31L));
    assertThat(
        connectedAllowed.getOspfMetricType(),
        equalTo(OspfMetricType.E1));
    assertThat(
        connectedAllowed.getTag(),
        equalTo(101L));

    assertThat(
        connectedMap
            .process(
                Prefix6.parse(
                    "2001:db8:200:1::/64"),
                process.getRedistributionMetric(),
                Route.UNSET_ROUTE_TAG)
            .isEmpty(),
        equalTo(true));

    RouteMap6 staticMap =
        process.getRedistributeStaticRouteMap();

    assertThat(
        staticMap,
        notNullValue());

    assertThat(
        staticMap
            .process(
                Prefix6.parse(
                    "2001:db8:400::/64"),
                process.getRedistributionMetric(),
                999L)
            .isEmpty(),
        equalTo(true));

    RouteMap6.Result staticAllowed =
        staticMap
            .process(
                Prefix6.parse(
                    "2001:db8:300::/64"),
                process.getRedistributionMetric(),
                999L)
            .orElseThrow();

    assertThat(
        staticAllowed.getMetric(),
        equalTo(41L));
    assertThat(
        staticAllowed.getOspfMetricType(),
        equalTo(OspfMetricType.E2));
    assertThat(
        staticAllowed.getTag(),
        equalTo(202L));
  }

  @Test
  public void testOspfv3DistanceStateExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-distance-state");

    AosCxOspfv3Process p1 =
        c.getOspfv3Processes().get(1);

    assertThat(p1, notNullValue());
    assertThat(
        p1.getIntraAreaDistance(),
        equalTo(24));
    assertThat(
        p1.getInterAreaDistance(),
        equalTo(66));
    assertThat(
        p1.getExternalDistance(),
        equalTo(77));
    assertThat(
        p1.getEnabled(),
        equalTo(true));

    AosCxOspfv3Process p2 =
        c.getOspfv3Processes().get(2);

    assertThat(p2, notNullValue());

    /*
     * distance 99 changes all three; no distance inter-area
     * restores only the inter-area distance to 110.
     */
    assertThat(
        p2.getIntraAreaDistance(),
        equalTo(99));
    assertThat(
        p2.getInterAreaDistance(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_ADMIN_DISTANCE));
    assertThat(
        p2.getExternalDistance(),
        equalTo(99));
    assertThat(
        p2.getEnabled(),
        equalTo(false));

    AosCxInterface shutdown =
        c.getInterfaces().get("1/1/1");

    AosCxInterface reenabled =
        c.getInterfaces().get("1/1/2");

    assertThat(shutdown, notNullValue());
    assertThat(
        shutdown.getOspfv3Enabled(),
        equalTo(false));

    assertThat(reenabled, notNullValue());
    assertThat(
        reenabled.getOspfv3Enabled(),
        equalTo(true));
  }

  @Test
  public void testOspfv3DistanceStateConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-distance-state");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(p1, notNullValue());
    assertThat(
        p1.getIntraAreaAdminCost(),
        equalTo(24));
    assertThat(
        p1.getInterAreaAdminCost(),
        equalTo(66));
    assertThat(
        p1.getExternalAdminCost(),
        equalTo(77));
    assertThat(
        p1.getEnabled(),
        equalTo(true));

    org.batfish.datamodel.ospf.Ospfv3Process p2 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("2");

    assertThat(p2, notNullValue());
    assertThat(
        p2.getIntraAreaAdminCost(),
        equalTo(99));
    assertThat(
        p2.getInterAreaAdminCost(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Process
                .DEFAULT_ADMIN_COST));
    assertThat(
        p2.getExternalAdminCost(),
        equalTo(99));
    assertThat(
        p2.getEnabled(),
        equalTo(false));

    org.batfish.datamodel.Interface shutdown =
        c.getAllInterfaces().get("1/1/1");

    org.batfish.datamodel.Interface reenabled =
        c.getAllInterfaces().get("1/1/2");

    assertThat(
        shutdown.getOspfv3Settings(),
        notNullValue());
    assertThat(
        shutdown
            .getOspfv3Settings()
            .getEnabled(),
        equalTo(false));

    assertThat(
        reenabled.getOspfv3Settings(),
        notNullValue());
    assertThat(
        reenabled
            .getOspfv3Settings()
            .getEnabled(),
        equalTo(true));
  }

  @Test
  public void testOspfv3NssaExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-nssa");

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(process, notNullValue());

    assertThat(
        process.getNssaAreas()
            .get("0.0.0.1"),
        equalTo(false));

    assertThat(
        process.getNssaAreas()
            .get("0.0.0.2"),
        equalTo(true));

    /*
     * no area ... nssa no-summary retains NSSA
     * but clears summary suppression.
     */
    assertThat(
        process.getNssaAreas()
            .get("0.0.0.3"),
        equalTo(false));

    /*
     * no area ... nssa changes the area back to normal.
     */
    assertThat(
        process.getNssaAreas()
            .containsKey("0.0.0.4"),
        equalTo(false));

    /*
     * Changing stub -> NSSA replaces the old area type.
     */
    assertThat(
        process.getNssaAreas()
            .get("0.0.0.5"),
        equalTo(false));
    assertThat(
        process.getStubAreas()
            .containsKey("0.0.0.5"),
        equalTo(false));

    /*
     * Changing NSSA -> stub replaces NSSA type.
     */
    assertThat(
        process.getStubAreas()
            .get("0.0.0.6"),
        equalTo(false));
    assertThat(
        process.getNssaAreas()
            .containsKey("0.0.0.6"),
        equalTo(false));

    assertThat(
        process.getAreaDefaultMetrics()
            .get("0.0.0.1"),
        equalTo(7L));
  }

  @Test
  public void testOspfv3NssaConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-nssa");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process
        process =
            c.getDefaultVrf()
                .getOspfv3Processes()
                .get("1");

    assertThat(process, notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Area
        nssa =
            process.getAreas().get(1L);

    org.batfish.datamodel.ospf.Ospfv3Area
        noSummary =
            process.getAreas().get(2L);

    org.batfish.datamodel.ospf.Ospfv3Area
        resetNoSummary =
            process.getAreas().get(3L);

    org.batfish.datamodel.ospf.Ospfv3Area
        resetNssa =
            process.getAreas().get(4L);

    org.batfish.datamodel.ospf.Ospfv3Area
        changedToNssa =
            process.getAreas().get(5L);

    org.batfish.datamodel.ospf.Ospfv3Area
        changedToStub =
            process.getAreas().get(6L);

    assertThat(
        nssa.getNssa(),
        equalTo(true));
    assertThat(
        nssa.getStub(),
        equalTo(false));
    assertThat(
        nssa.getSuppressInterArea(),
        equalTo(false));
    assertThat(
        nssa.getDefaultMetric(),
        equalTo(7L));

    assertThat(
        noSummary.getNssa(),
        equalTo(true));
    assertThat(
        noSummary.getSuppressInterArea(),
        equalTo(true));

    assertThat(
        resetNoSummary.getNssa(),
        equalTo(true));
    assertThat(
        resetNoSummary.getSuppressInterArea(),
        equalTo(false));

    assertThat(
        resetNssa.getNssa(),
        equalTo(false));
    assertThat(
        resetNssa.getStub(),
        equalTo(false));

    assertThat(
        changedToNssa.getNssa(),
        equalTo(true));
    assertThat(
        changedToNssa.getStub(),
        equalTo(false));

    assertThat(
        changedToStub.getStub(),
        equalTo(true));
    assertThat(
        changedToStub.getNssa(),
        equalTo(false));
  }

  @Test
  public void testOspfv3DistributeListExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-distribute-list");

    AosCxOspfv3Process p1 =
        c.getOspfv3Processes().get(1);

    assertThat(p1, notNullValue());

    assertThat(
        p1.getDistributeListIn(),
        equalTo("DL-IN"));

    assertThat(
        p1.getDistributeListOut(),
        equalTo("DL-OUT"));

    AosCxOspfv3Process p2 =
        c.getOspfv3Processes().get(2);

    assertThat(p2, notNullValue());

    assertThat(
        p2.getDistributeListIn(),
        nullValue());

    assertThat(
        p2.getDistributeListOut(),
        nullValue());
  }

  @Test
  public void testOspfv3DistributeListConversion()
      throws IOException {
    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-distribute-list");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(p1, notNullValue());

    PrefixList6 inbound =
        p1.getInboundDistributeList();

    PrefixList6 outbound =
        p1.getOutboundDistributeList();

    assertThat(
        inbound,
        notNullValue());

    assertThat(
        inbound.permits(
            Prefix6.parse(
                "2001:db8:100:1::/64")),
        equalTo(false));

    assertThat(
        inbound.permits(
            Prefix6.parse(
                "2001:db8:200:1::/64")),
        equalTo(true));

    assertThat(
        outbound,
        notNullValue());

    assertThat(
        outbound.permits(
            Prefix6.parse(
                "2001:db8:300:1::/64")),
        equalTo(false));

    assertThat(
        outbound.permits(
            Prefix6.parse(
                "2001:db8:400:1::/64")),
        equalTo(true));

    org.batfish.datamodel.ospf.Ospfv3Process p2 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("2");

    assertThat(p2, notNullValue());

    assertThat(
        p2.getInboundDistributeList(),
        nullValue());

    assertThat(
        p2.getOutboundDistributeList(),
        nullValue());
  }

  @Test
  public void testOspfv3AdditionalInterfaceKnobsExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-interface-knobs");

    AosCxInterface configured =
        c.getInterfaces().get("1/1/1");

    assertThat(
        configured,
        notNullValue());

    assertThat(
        configured.getOspfv3Priority(),
        equalTo(200));

    assertThat(
        configured.getOspfv3RetransmitInterval(),
        equalTo(17));

    assertThat(
        configured.getOspfv3TransitDelay(),
        equalTo(9));

    /*
     * No explicit values on 1/1/2:
     * vendor representation preserves absence.
     */
    AosCxInterface defaults =
        c.getInterfaces().get("1/1/2");

    assertThat(
        defaults,
        notNullValue());

    assertThat(
        defaults.getOspfv3Priority(),
        equalTo(null));

    assertThat(
        defaults.getOspfv3RetransmitInterval(),
        equalTo(null));

    assertThat(
        defaults.getOspfv3TransitDelay(),
        equalTo(null));

    /*
     * Explicit commands followed by "no" also return to
     * the implicit platform defaults.
     */
    AosCxInterface reset =
        c.getInterfaces().get("1/1/3");

    assertThat(
        reset,
        notNullValue());

    assertThat(
        reset.getOspfv3Priority(),
        equalTo(null));

    assertThat(
        reset.getOspfv3RetransmitInterval(),
        equalTo(null));

    assertThat(
        reset.getOspfv3TransitDelay(),
        equalTo(null));
  }

  @Test
  public void testOspfv3AdditionalInterfaceKnobsConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-interface-knobs");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.Interface configured =
        c.getAllInterfaces().get("1/1/1");

    assertThat(
        configured.getOspfv3Settings(),
        notNullValue());

    assertThat(
        configured
            .getOspfv3Settings()
            .getPriority(),
        equalTo(200));

    assertThat(
        configured
            .getOspfv3Settings()
            .getRetransmitInterval(),
        equalTo(17));

    assertThat(
        configured
            .getOspfv3Settings()
            .getTransitDelay(),
        equalTo(9));

    org.batfish.datamodel.Interface defaults =
        c.getAllInterfaces().get("1/1/2");

    assertThat(
        defaults.getOspfv3Settings(),
        notNullValue());

    assertThat(
        defaults
            .getOspfv3Settings()
            .getPriority(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3InterfaceSettings
                .DEFAULT_PRIORITY));

    assertThat(
        defaults
            .getOspfv3Settings()
            .getRetransmitInterval(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3InterfaceSettings
                .DEFAULT_RETRANSMIT_INTERVAL));

    assertThat(
        defaults
            .getOspfv3Settings()
            .getTransitDelay(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3InterfaceSettings
                .DEFAULT_TRANSIT_DELAY));

    org.batfish.datamodel.Interface reset =
        c.getAllInterfaces().get("1/1/3");

    assertThat(
        reset.getOspfv3Settings(),
        notNullValue());

    assertThat(
        reset
            .getOspfv3Settings()
            .getPriority(),
        equalTo(1));

    assertThat(
        reset
            .getOspfv3Settings()
            .getRetransmitInterval(),
        equalTo(5));

    assertThat(
        reset
            .getOspfv3Settings()
            .getTransitDelay(),
        equalTo(1));
  }

  @Test
  public void testOspfv3MaximumPathsExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-maximum-paths");

    AosCxOspfv3Process p1 =
        c.getOspfv3Processes().get(1);

    assertThat(
        p1,
        notNullValue());

    assertThat(
        p1.getMaximumPaths(),
        equalTo(12));

    AosCxOspfv3Process p2 =
        c.getOspfv3Processes().get(2);

    assertThat(
        p2,
        notNullValue());

    /*
     * no maximum-paths restores the AOS-CX default.
     */
    assertThat(
        p2.getMaximumPaths(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_MAXIMUM_PATHS));

    AosCxOspfv3Process p3 =
        c.getOspfv3Processes().get(3);

    assertThat(
        p3,
        notNullValue());

    /*
     * Invalid values are ignored, leaving the default intact.
     */
    assertThat(
        p3.getMaximumPaths(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_MAXIMUM_PATHS));
  }

  @Test
  public void testOspfv3MaximumPathsConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-maximum-paths");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        p1,
        notNullValue());

    assertThat(
        p1.getMaximumPaths(),
        equalTo(12));

    org.batfish.datamodel.ospf.Ospfv3Process p2 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("2");

    assertThat(
        p2,
        notNullValue());

    assertThat(
        p2.getMaximumPaths(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3Process
                .DEFAULT_MAXIMUM_PATHS));

    org.batfish.datamodel.ospf.Ospfv3Process p3 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("3");

    assertThat(
        p3,
        notNullValue());

    assertThat(
        p3.getMaximumPaths(),
        equalTo(4));
  }

  @Test
  public void testOspfv3AreaRangeExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-area-range");

    AosCxOspfv3Process p1 =
        c.getOspfv3Processes().get(1);

    assertThat(
        p1,
        notNullValue());

    assertThat(
        p1.getInterAreaRanges()
            .get("1")
            .get(
                Prefix6.parse(
                    "2001:db8:100::/48")),
        equalTo(true));

    assertThat(
        p1.getInterAreaRanges()
            .get("2")
            .get(
                Prefix6.parse(
                    "2001:db8:200::/48")),
        equalTo(false));

    AosCxOspfv3Process p2 =
        c.getOspfv3Processes().get(2);

    assertThat(
        p2,
        notNullValue());

    /*
     * "no ... no-advertise" retains the range but restores
     * advertisement.
     */
    assertThat(
        p2.getInterAreaRanges()
            .get("3")
            .get(
                Prefix6.parse(
                    "2001:db8:300::/48")),
        equalTo(true));

    AosCxOspfv3Process p3 =
        c.getOspfv3Processes().get(3);

    assertThat(
        p3,
        notNullValue());

    /*
     * Plain "no area ... range" removes the range.
     */
    assertThat(
        p3.getInterAreaRanges()
            .containsKey("4"),
        equalTo(false));
  }

  @Test
  public void testOspfv3AreaRangeConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-area-range");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        p1,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Area area1 =
        p1.getAreas().get(1L);

    assertThat(
        area1,
        notNullValue());

    assertThat(
        area1.getRanges().size(),
        equalTo(1));

    assertThat(
        area1.getRanges()
            .get(0)
            .getPrefix(),
        equalTo(
            Prefix6.parse(
                "2001:db8:100::/48")));

    assertThat(
        area1.getRanges()
            .get(0)
            .getType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3AreaRange.Type
                .INTER_AREA));

    assertThat(
        area1.getRanges()
            .get(0)
            .getAdvertise(),
        equalTo(true));

    org.batfish.datamodel.ospf.Ospfv3Area area2 =
        p1.getAreas().get(2L);

    assertThat(
        area2,
        notNullValue());

    assertThat(
        area2.getRanges()
            .get(0)
            .getAdvertise(),
        equalTo(false));
  }

  @Test
  public void testOspfv3NssaRangeExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-nssa-range");

    AosCxOspfv3Process p10 =
        c.getOspfv3Processes().get(10);

    assertThat(
        p10,
        notNullValue());

    assertThat(
        p10.getNssaRanges()
            .get("1")
            .get(
                Prefix6.parse(
                    "2001:db8:700::/48")),
        equalTo(true));

    assertThat(
        p10.getNssaRanges()
            .get("1")
            .get(
                Prefix6.parse(
                    "2001:db8:800::/48")),
        equalTo(false));

    AosCxOspfv3Process p20 =
        c.getOspfv3Processes().get(20);

    assertThat(
        p20,
        notNullValue());

    assertThat(
        p20.getNssaRanges()
            .get("2")
            .get(
                Prefix6.parse(
                    "2001:db8:900::/48")),
        equalTo(true));

    AosCxOspfv3Process p30 =
        c.getOspfv3Processes().get(30);

    assertThat(
        p30,
        notNullValue());

    assertThat(
        p30.getNssaRanges()
            .containsKey("3"),
        equalTo(false));
  }

  @Test
  public void testOspfv3NssaRangeConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-nssa-range");

    Configuration c =
        configs.get("aoscx-router");

    org.batfish.datamodel.ospf.Ospfv3Process p10 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("10");

    assertThat(
        p10,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Area area =
        p10.getAreas().get(1L);

    assertThat(
        area,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3AreaRange
        advertised =
            area.getRanges()
                .stream()
                .filter(
                    range ->
                        range.getPrefix()
                            .equals(
                                Prefix6.parse(
                                    "2001:db8:700::/48")))
                .findFirst()
                .orElse(null);

    assertThat(
        advertised,
        notNullValue());

    assertThat(
        advertised.getType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3AreaRange.Type.NSSA));

    assertThat(
        advertised.getAdvertise(),
        equalTo(true));

    org.batfish.datamodel.ospf.Ospfv3AreaRange
        suppressed =
            area.getRanges()
                .stream()
                .filter(
                    range ->
                        range.getPrefix()
                            .equals(
                                Prefix6.parse(
                                    "2001:db8:800::/48")))
                .findFirst()
                .orElse(null);

    assertThat(
        suppressed,
        notNullValue());

    assertThat(
        suppressed.getType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3AreaRange.Type.NSSA));

    assertThat(
        suppressed.getAdvertise(),
        equalTo(false));
  }

  @Test
  public void testOspfv3SummaryAddressExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-summary-address");

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getExternalSummaries().size(),
        equalTo(3));

    AosCxOspfv3ExternalSummary summary100 =
        process
            .getExternalSummaries()
            .get(
                Prefix6.parse(
                    "2001:db8:100::/48"));

    assertThat(
        summary100,
        notNullValue());

    assertThat(
        summary100.getAdvertise(),
        equalTo(true));

    assertThat(
        summary100.getTag(),
        nullValue());

    AosCxOspfv3ExternalSummary summary200 =
        process
            .getExternalSummaries()
            .get(
                Prefix6.parse(
                    "2001:db8:200::/48"));

    assertThat(
        summary200,
        notNullValue());

    assertThat(
        summary200.getAdvertise(),
        equalTo(true));

    assertThat(
        summary200.getTag(),
        equalTo(1234L));

    AosCxOspfv3ExternalSummary summary300 =
        process
            .getExternalSummaries()
            .get(
                Prefix6.parse(
                    "2001:db8:300::/48"));

    assertThat(
        summary300,
        notNullValue());

    assertThat(
        summary300.getAdvertise(),
        equalTo(false));

    assertThat(
        process
            .getExternalSummaries()
            .containsKey(
                Prefix6.parse(
                    "2001:db8:400::/48")),
        equalTo(false));
  }

  @Test
  public void testOspfv3SummaryAddressConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-summary-address");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getExternalSummaries(),
        containsInAnyOrder(
            new Ospfv3ExternalSummary(
                Prefix6.parse(
                    "2001:db8:100::/48"),
                true,
                null),
            new Ospfv3ExternalSummary(
                Prefix6.parse(
                    "2001:db8:200::/48"),
                true,
                1234L),
            new Ospfv3ExternalSummary(
                Prefix6.parse(
                    "2001:db8:300::/48"),
                false,
                null)));
  }

  @Test
  public void testOspfv3RedistributeLocalLoopbackExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-redistribute-local-loopback");

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getRedistributeLocalLoopback(),
        equalTo(true));

    assertThat(
        process.getRedistributeLocalLoopbackRouteMap(),
        equalTo("LOCAL-RM"));

    AosCxOspfv3Process disabled =
        c.getOspfv3Processes().get(2);

    assertThat(
        disabled,
        notNullValue());

    assertThat(
        disabled.getRedistributeLocalLoopback(),
        equalTo(false));

    assertThat(
        disabled.getRedistributeLocalLoopbackRouteMap(),
        nullValue());
  }

  @Test
  public void testOspfv3RedistributeLocalLoopbackConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-redistribute-local-loopback");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getRedistributeLocalLoopback(),
        equalTo(true));

    assertThat(
        process.getRedistributeConnected(),
        equalTo(false));

    assertThat(
        process.getRedistributeLocalLoopbackRouteMap(),
        notNullValue());

    RouteMap6.Result result =
        process
            .getRedistributeLocalLoopbackRouteMap()
            .process(
                Prefix6.parse(
                    "2001:db8:100::1/128"),
                process.getRedistributionMetric(),
                Route.UNSET_ROUTE_TAG)
            .orElseThrow();

    assertThat(
        result.getMetric(),
        equalTo(17L));

    assertThat(
        result.getOspfMetricType(),
        equalTo(OspfMetricType.E1));

    assertThat(
        result.getTag(),
        equalTo(707L));
  }

  @Test
  public void testOspfv3VirtualLinkExtraction() {
    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-virtual-link");

    AosCxOspfv3Process process =
        c.getOspfv3Processes().get(1);

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getVirtualLinks(),
        hasKey("1"));

    assertThat(
        process
            .getVirtualLinks()
            .get("1"),
        contains(
            Ip.parse(
                "192.0.2.2")));

    assertThat(
        process
            .getVirtualLinks()
            .containsKey("2"),
        equalTo(false));
  }

  @Test
  public void testOspfv3VirtualLinkConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-virtual-link");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    assertThat(
        process.getVirtualLinks(),
        contains(
            new Ospfv3VirtualLink(
                1L,
                Ip.parse(
                    "192.0.2.2"))));

    assertThat(
        process.getAreas(),
        hasKey(1L));
  }

  @Test
  public void testOspfv3MaxMetricRouterLsaExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-max-metric-router-lsa");

    AosCxOspfv3Process process1 =
        c.getOspfv3Processes().get(1);

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1.getMaxMetricRouterLsa(),
        equalTo(true));

    assertThat(
        process1
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(3000));

    AosCxOspfv3Process process2 =
        c.getOspfv3Processes().get(2);

    assertThat(
        process2,
        notNullValue());

    assertThat(
        process2.getMaxMetricRouterLsa(),
        equalTo(false));

    assertThat(
        process2
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(null));

    AosCxOspfv3Process process3 =
        c.getOspfv3Processes().get(3);

    assertThat(
        process3,
        notNullValue());

    assertThat(
        process3.getMaxMetricRouterLsa(),
        equalTo(false));

    assertThat(
        process3
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(
            AosCxOspfv3Process
                .DEFAULT_MAX_METRIC_ROUTER_LSA_ON_STARTUP_SECONDS));
  }

  @Test
  public void testOspfv3MaxMetricRouterLsaConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-max-metric-router-lsa");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1.getMaxMetricRouterLsa(),
        equalTo(true));

    assertThat(
        process1
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(3000));

    org.batfish.datamodel.ospf.Ospfv3Process process3 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("3");

    assertThat(
        process3,
        notNullValue());

    assertThat(
        process3.getMaxMetricRouterLsa(),
        equalTo(false));

    assertThat(
        process3
            .getMaxMetricRouterLsaOnStartupSeconds(),
        equalTo(600));
  }

  @Test
  public void testOspfv3ActiveBackboneExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-active-backbone");

    AosCxOspfv3Process process1 =
        c.getOspfv3Processes().get(1);

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1
            .getActiveBackboneStubDefaultRoute(),
        equalTo(false));

    AosCxOspfv3Process process2 =
        c.getOspfv3Processes().get(2);

    assertThat(
        process2,
        notNullValue());

    assertThat(
        process2
            .getActiveBackboneStubDefaultRoute(),
        equalTo(true));

    /*
     * Feature defaults enabled on AOS-CX.
     */
    AosCxOspfv3Process process3 =
        c.getOspfv3Processes().get(3);

    assertThat(
        process3,
        notNullValue());

    assertThat(
        process3
            .getActiveBackboneStubDefaultRoute(),
        equalTo(true));
  }

  @Test
  public void testOspfv3ActiveBackboneConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-active-backbone");

    Configuration c =
        configs.get("aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1
            .getActiveBackboneStubDefaultRoute(),
        equalTo(false));

    org.batfish.datamodel.ospf.Ospfv3Process process2 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("2");

    assertThat(
        process2,
        notNullValue());

    assertThat(
        process2
            .getActiveBackboneStubDefaultRoute(),
        equalTo(true));

    org.batfish.datamodel.ospf.Ospfv3Process process3 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("3");

    assertThat(
        process3,
        notNullValue());

    assertThat(
        process3
            .getActiveBackboneStubDefaultRoute(),
        equalTo(true));
  }

  @Test
  public void testRouteMapMatchTagExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-route-map-match-tag");

    org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMap
        routeMap =
            c.getRouteMaps()
                .get("TAG-FILTER");

    assertThat(
        routeMap,
        notNullValue());

    org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMapEntry
        denyEntry =
            routeMap
                .getEntries()
                .get(10L);

    assertThat(
        denyEntry,
        notNullValue());

    assertThat(
        denyEntry.getMatchTag(),
        equalTo(100L));

    org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMapEntry
        permitEntry =
            routeMap
                .getEntries()
                .get(20L);

    assertThat(
        permitEntry,
        notNullValue());

    assertThat(
        permitEntry.getMatchTag(),
        equalTo(200L));

    org.batfish.vendor.aruba_aoscx.representation.AosCxRouteMap
        clearedMap =
            c.getRouteMaps()
                .get("TAG-CLEAR");

    assertThat(
        clearedMap,
        notNullValue());

    assertThat(
        clearedMap
            .getEntries()
            .get(10L)
            .getMatchTag(),
        equalTo(null));
  }

  @Test
  public void testRouteMapMatchTagConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-route-map-match-tag");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    org.batfish.datamodel.RouteMap6 routeMap =
        process
            .getRedistributeStaticRouteMap();

    assertThat(
        routeMap,
        notNullValue());

    /*
     * Tag 100 is denied by sequence 10.
     */
    assertThat(
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:100::/64"),
                25L,
                100L)
            .isEmpty(),
        equalTo(true));

    /*
     * Tag 200 matches sequence 20 and receives all configured set
     * transformations.
     */
    org.batfish.datamodel.RouteMap6.Result matched =
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                25L,
                200L)
            .orElseThrow();

    assertThat(
        matched.getMetric(),
        equalTo(44L));

    assertThat(
        matched.getOspfMetricType(),
        equalTo(
            org.batfish.datamodel.ospf.OspfMetricType.E1));

    assertThat(
        matched.getTag(),
        equalTo(999L));

    /*
     * Tag 300 reaches the unconditional permit sequence and retains its
     * original metric and tag.
     */
    org.batfish.datamodel.RouteMap6.Result fallback =
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:300::/64"),
                25L,
                300L)
            .orElseThrow();

    assertThat(
        fallback.getMetric(),
        equalTo(25L));

    assertThat(
        fallback.getTag(),
        equalTo(300L));
  }

  @Test
  public void testOspfv3RedistributeOspfExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-redistribute-ospf");

    AosCxOspfv3Process process1 =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1
            .getRedistributeOspfProcesses()
            .contains(2),
        equalTo(true));

    assertThat(
        process1
            .getRedistributeOspfProcesses()
            .contains(3),
        equalTo(false));

    assertThat(
        process1
            .getRedistributeOspfRouteMaps()
            .get(2),
        equalTo(
            "OSPF-X"));

    assertThat(
        process1
            .getRedistributeOspfRouteMaps()
            .containsKey(3),
        equalTo(false));
  }

  @Test
  public void testOspfv3RedistributeOspfConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-redistribute-ospf");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process1 =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1
            .getRedistributeOspfProcesses()
            .contains("2"),
        equalTo(true));

    assertThat(
        process1
            .getRedistributeOspfProcesses()
            .contains("3"),
        equalTo(false));

    org.batfish.datamodel.RouteMap6 routeMap =
        process1
            .getRedistributeOspfRouteMaps()
            .get("2");

    assertThat(
        routeMap,
        notNullValue());

    org.batfish.datamodel.RouteMap6.Result result =
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                25L,
                222L)
            .orElseThrow();

    assertThat(
        result.getMetric(),
        equalTo(40L));

    assertThat(
        result.getOspfMetricType(),
        equalTo(
            org.batfish.datamodel.ospf.OspfMetricType.E1));

    assertThat(
        result.getTag(),
        equalTo(999L));

    assertThat(
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:300::/64"),
                25L,
                333L)
            .isEmpty(),
        equalTo(true));
  }

  @Test
  public void testOspfv3AuthenticationExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-authentication");

    AosCxOspfv3Process process =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process,
        notNullValue());

    org.batfish.vendor.aruba_aoscx.representation
            .AosCxOspfv3Authentication
        areaAuth =
            process
                .getAreaAuthentications()
                .get("0");

    assertThat(
        areaAuth,
        notNullValue());

    assertThat(
        areaAuth.getSpi(),
        equalTo(256L));

    assertThat(
        areaAuth.getAuthType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Authentication.AuthType.SHA1));

    assertThat(
        areaAuth.getKeyType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Authentication.KeyType.PLAINTEXT));

    assertThat(
        areaAuth.getKey(),
        equalTo(
            "area-secret"));

    /*
     * Area 1 authentication was configured and then removed.
     */
    assertThat(
        process
            .getAreaAuthentications()
            .containsKey("1"),
        equalTo(false));

    AosCxInterface inherited =
        c.getInterfaces()
            .get("1/1/1");

    assertThat(
        inherited,
        notNullValue());

    assertThat(
        inherited.getOspfv3Authentication(),
        equalTo(null));

    assertThat(
        inherited.getOspfv3AuthenticationNull(),
        equalTo(false));

    AosCxInterface nullOverride =
        c.getInterfaces()
            .get("1/1/2");

    assertThat(
        nullOverride,
        notNullValue());

    assertThat(
        nullOverride.getOspfv3Authentication(),
        equalTo(null));

    assertThat(
        nullOverride.getOspfv3AuthenticationNull(),
        equalTo(true));

    AosCxInterface explicit =
        c.getInterfaces()
            .get("1/1/3");

    assertThat(
        explicit,
        notNullValue());

    assertThat(
        explicit
            .getOspfv3Authentication()
            .getSpi(),
        equalTo(400L));

    assertThat(
        explicit
            .getOspfv3Authentication()
            .getAuthType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Authentication.AuthType.MD5));

    assertThat(
        explicit
            .getOspfv3Authentication()
            .getKey(),
        equalTo(
            "interface-secret"));

    /*
     * no ipv6 ospfv3 authentication removes the explicit override, so the
     * interface returns to area inheritance.
     */
    AosCxInterface cleared =
        c.getInterfaces()
            .get("1/1/4");

    assertThat(
        cleared,
        notNullValue());

    assertThat(
        cleared.getOspfv3Authentication(),
        equalTo(null));

    assertThat(
        cleared.getOspfv3AuthenticationNull(),
        equalTo(false));
  }

  @Test
  public void testOspfv3AuthenticationConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-authentication");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.Interface inherited =
        c.getAllInterfaces()
            .get("1/1/1");

    assertThat(
        inherited,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Authentication
        inheritedAuth =
            inherited
                .getOspfv3Settings()
                .getAuthentication();

    assertThat(
        inheritedAuth,
        notNullValue());

    assertThat(
        inheritedAuth.getSpi(),
        equalTo(256L));

    assertThat(
        inheritedAuth.getAuthType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3Authentication.AuthType.SHA1));

    assertThat(
        inheritedAuth.getKeyType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3Authentication.KeyType.PLAINTEXT));

    assertThat(
        inheritedAuth.getKey(),
        equalTo(
            "area-secret"));

    /*
     * authentication null explicitly suppresses area inheritance.
     */
    org.batfish.datamodel.Interface nullOverride =
        c.getAllInterfaces()
            .get("1/1/2");

    assertThat(
        nullOverride
            .getOspfv3Settings()
            .getAuthentication(),
        equalTo(null));

    /*
     * Explicit interface IPsec wins over the area.
     */
    org.batfish.datamodel.ospf.Ospfv3Authentication
        explicitAuth =
            c.getAllInterfaces()
                .get("1/1/3")
                .getOspfv3Settings()
                .getAuthentication();

    assertThat(
        explicitAuth,
        notNullValue());

    assertThat(
        explicitAuth.getSpi(),
        equalTo(400L));

    assertThat(
        explicitAuth.getAuthType(),
        equalTo(
            org.batfish.datamodel.ospf
                .Ospfv3Authentication.AuthType.MD5));

    assertThat(
        explicitAuth.getKey(),
        equalTo(
            "interface-secret"));

    /*
     * The no form clears the interface override and restores the area
     * authentication.
     */
    org.batfish.datamodel.ospf.Ospfv3Authentication
        clearedAuth =
            c.getAllInterfaces()
                .get("1/1/4")
                .getOspfv3Settings()
                .getAuthentication();

    assertThat(
        clearedAuth,
        equalTo(
            inheritedAuth));
  }

  @Test
  public void testOspfv3VirtualLinkAuthenticationExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-virtual-link-authentication");

    AosCxOspfv3Process process =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process,
        notNullValue());

    Ip authenticatedPeer =
        Ip.parse(
            "192.0.2.2");

    org.batfish.vendor.aruba_aoscx.representation
            .AosCxOspfv3Authentication
        authentication =
            process
                .getVirtualLinkAuthentication(
                    "1",
                    authenticatedPeer);

    assertThat(
        authentication,
        notNullValue());

    assertThat(
        authentication.getSpi(),
        equalTo(
            256L));

    assertThat(
        authentication.getAuthType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Authentication.AuthType.SHA1));

    assertThat(
        authentication.getKeyType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Authentication.KeyType.PLAINTEXT));

    assertThat(
        authentication.getKey(),
        equalTo(
            "vlink-secret"));

    /*
     * "no authentication" removes authentication but keeps the virtual link.
     */
    Ip clearedPeer =
        Ip.parse(
            "192.0.2.3");

    assertThat(
        process
            .getVirtualLinks()
            .get("2")
            .contains(
                clearedPeer),
        equalTo(
            true));

    assertThat(
        process
            .getVirtualLinkAuthentication(
                "2",
                clearedPeer),
        equalTo(
            null));
  }

  @Test
  public void testOspfv3VirtualLinkAuthenticationConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-virtual-link-authentication");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3VirtualLink
        authenticatedLink =
            process
                .getVirtualLinks()
                .stream()
                .filter(
                    link ->
                        link.getTransitArea()
                            == 1L)
                .findFirst()
                .orElseThrow();

    assertThat(
        authenticatedLink.getPeerRouterId(),
        equalTo(
            Ip.parse(
                "192.0.2.2")));

    assertThat(
        authenticatedLink.getAuthentication(),
        equalTo(
            new org.batfish.datamodel.ospf.Ospfv3Authentication(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Authentication
                    .KeyType.PLAINTEXT,
                "vlink-secret")));

    org.batfish.datamodel.ospf.Ospfv3VirtualLink
        clearedLink =
            process
                .getVirtualLinks()
                .stream()
                .filter(
                    link ->
                        link.getTransitArea()
                            == 2L)
                .findFirst()
                .orElseThrow();

    assertThat(
        clearedLink.getAuthentication(),
        equalTo(
            null));
  }

  @Test
  public void testOspfv3BfdExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-bfd");

    AosCxOspfv3Process process1 =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process1,
        notNullValue());

    assertThat(
        process1.getBfdAllInterfaces(),
        equalTo(true));

    AosCxOspfv3Process process2 =
        c.getOspfv3Processes(
                "blue")
            .get(2);

    assertThat(
        process2,
        notNullValue());

    assertThat(
        process2.getBfdAllInterfaces(),
        equalTo(false));

    /*
     * No explicit interface command: inherit process setting.
     */
    assertThat(
        c.getInterfaces()
            .get("1/1/1")
            .getOspfv3Bfd(),
        equalTo(null));

    /*
     * Explicit bfd disable overrides global enable.
     */
    assertThat(
        c.getInterfaces()
            .get("1/1/2")
            .getOspfv3Bfd(),
        equalTo(false));

    /*
     * no ipv6 ospfv3 bfd clears the override and restores inheritance.
     */
    assertThat(
        c.getInterfaces()
            .get("1/1/3")
            .getOspfv3Bfd(),
        equalTo(null));

    /*
     * Explicit bfd enables BFD despite the blue process global false.
     */
    assertThat(
        c.getInterfaces()
            .get("1/1/4")
            .getOspfv3Bfd(),
        equalTo(true));

    /*
     * No override on globally-disabled process.
     */
    assertThat(
        c.getInterfaces()
            .get("1/1/5")
            .getOspfv3Bfd(),
        equalTo(null));
  }

  @Test
  public void testOspfv3BfdConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-bfd");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    /*
     * Global process setting inherited.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/1")
            .getOspfv3Settings()
            .getBfdEnabled(),
        equalTo(true));

    /*
     * Explicit interface disable wins over global enable.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/2")
            .getOspfv3Settings()
            .getBfdEnabled(),
        equalTo(false));

    /*
     * no ipv6 ospfv3 bfd returned this interface to process inheritance.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/3")
            .getOspfv3Settings()
            .getBfdEnabled(),
        equalTo(true));

    /*
     * Explicit interface enable wins over globally-disabled blue process.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/4")
            .getOspfv3Settings()
            .getBfdEnabled(),
        equalTo(true));

    /*
     * No override inherits globally-disabled blue process state.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/5")
            .getOspfv3Settings()
            .getBfdEnabled(),
        equalTo(false));
  }

  @Test
  public void testOspfv3EncryptionExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-encryption");

    AosCxOspfv3Process process =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process,
        notNullValue());

    org.batfish.vendor.aruba_aoscx.representation
            .AosCxOspfv3Encryption
        areaEncryption =
            process
                .getAreaEncryptions()
                .get("0");

    assertThat(
        areaEncryption,
        notNullValue());

    assertThat(
        areaEncryption.getSpi(),
        equalTo(256L));

    assertThat(
        areaEncryption.getAuthType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.AuthType.SHA1));

    assertThat(
        areaEncryption.getAuthKey(),
        equalTo(
            "area-auth"));

    assertThat(
        areaEncryption.getEncryptionType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.EncryptionType.AES));

    assertThat(
        areaEncryption.getEncryptionKey(),
        equalTo(
            "0123456789abcdef"));

    /*
     * Area 1 ESP was configured then removed.
     */
    assertThat(
        process
            .getAreaEncryptions()
            .containsKey("1"),
        equalTo(false));

    AosCxInterface inherited =
        c.getInterfaces()
            .get("1/1/1");

    assertThat(
        inherited.getOspfv3Encryption(),
        equalTo(null));

    assertThat(
        inherited.getOspfv3EncryptionNull(),
        equalTo(false));

    /*
     * "ipv6 ospfv3 encryption null" is an explicit suppress-inheritance
     * state.
     */
    AosCxInterface disabled =
        c.getInterfaces()
            .get("1/1/2");

    assertThat(
        disabled.getOspfv3Encryption(),
        equalTo(null));

    assertThat(
        disabled.getOspfv3EncryptionNull(),
        equalTo(true));

    AosCxInterface explicit =
        c.getInterfaces()
            .get("1/1/3");

    assertThat(
        explicit.getOspfv3Encryption(),
        notNullValue());

    assertThat(
        explicit
            .getOspfv3Encryption()
            .getSpi(),
        equalTo(400L));

    assertThat(
        explicit
            .getOspfv3Encryption()
            .getEncryptionType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.EncryptionType.DES));

    /*
     * no ipv6 ospfv3 encryption clears the interface override.
     */
    AosCxInterface cleared =
        c.getInterfaces()
            .get("1/1/4");

    assertThat(
        cleared.getOspfv3Encryption(),
        equalTo(null));

    assertThat(
        cleared.getOspfv3EncryptionNull(),
        equalTo(false));

    /*
     * ESP NULL remains an actual ESP SA; it is not equivalent to the
     * interface-level "encryption null" command.
     */
    AosCxInterface nullEsp =
        c.getInterfaces()
            .get("1/1/5");

    assertThat(
        nullEsp.getOspfv3Encryption(),
        notNullValue());

    assertThat(
        nullEsp
            .getOspfv3Encryption()
            .getEncryptionType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.EncryptionType.NULL));

    assertThat(
        nullEsp.getOspfv3EncryptionNull(),
        equalTo(false));
  }

  @Test
  public void testOspfv3EncryptionConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-encryption");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Encryption
        inherited =
            c.getAllInterfaces()
                .get("1/1/1")
                .getOspfv3Settings()
                .getEncryption();

    assertThat(
        inherited,
        equalTo(
            new org.batfish.datamodel.ospf.Ospfv3Encryption(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "area-auth",
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .EncryptionType.AES,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "0123456789abcdef")));

    assertThat(
        c.getAllInterfaces()
            .get("1/1/2")
            .getOspfv3Settings()
            .getEncryption(),
        equalTo(null));

    org.batfish.datamodel.ospf.Ospfv3Encryption
        explicit =
            c.getAllInterfaces()
                .get("1/1/3")
                .getOspfv3Settings()
                .getEncryption();

    assertThat(
        explicit,
        notNullValue());

    assertThat(
        explicit.getSpi(),
        equalTo(400L));

    assertThat(
        explicit.getEncryptionType(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Encryption
                .EncryptionType.DES));

    /*
     * Cleared override returns to inherited area ESP.
     */
    assertThat(
        c.getAllInterfaces()
            .get("1/1/4")
            .getOspfv3Settings()
            .getEncryption(),
        equalTo(
            inherited));

    org.batfish.datamodel.ospf.Ospfv3Encryption
        nullEsp =
            c.getAllInterfaces()
                .get("1/1/5")
                .getOspfv3Settings()
                .getEncryption();

    assertThat(
        nullEsp,
        notNullValue());

    assertThat(
        nullEsp.getEncryptionType(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Encryption
                .EncryptionType.NULL));

    assertThat(
        nullEsp.getEncryptionKey(),
        equalTo(null));
  }

  @Test
  public void testOspfv3VirtualLinkEncryptionExtraction() {

    AosCxConfiguration c =
        parseVendorConfig(
            "aoscx-ospfv3-virtual-link-encryption");

    AosCxOspfv3Process process =
        c.getOspfv3Processes()
            .get(1);

    assertThat(
        process,
        notNullValue());

    Ip encryptedPeer =
        Ip.parse(
            "192.0.2.2");

    org.batfish.vendor.aruba_aoscx.representation
            .AosCxOspfv3Encryption
        encryption =
            process
                .getVirtualLinkEncryption(
                    "1",
                    encryptedPeer);

    assertThat(
        encryption,
        notNullValue());

    assertThat(
        encryption.getSpi(),
        equalTo(
            256L));

    assertThat(
        encryption.getAuthType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.AuthType.SHA1));

    assertThat(
        encryption.getAuthKey(),
        equalTo(
            "vlink-auth"));

    assertThat(
        encryption.getEncryptionType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.EncryptionType.AES));

    assertThat(
        encryption.getEncryptionKey(),
        equalTo(
            "0123456789abcdef"));

    /*
     * no encryption removes ESP while retaining the virtual link.
     */
    Ip clearedPeer =
        Ip.parse(
            "192.0.2.3");

    assertThat(
        process
            .getVirtualLinks()
            .get("2")
            .contains(
                clearedPeer),
        equalTo(
            true));

    assertThat(
        process
            .getVirtualLinkEncryption(
                "2",
                clearedPeer),
        equalTo(
            null));

    /*
     * NULL is an ESP encryption type, not equivalent to no encryption.
     */
    org.batfish.vendor.aruba_aoscx.representation
            .AosCxOspfv3Encryption
        nullEsp =
            process
                .getVirtualLinkEncryption(
                    "3",
                    Ip.parse(
                        "192.0.2.4"));

    assertThat(
        nullEsp,
        notNullValue());

    assertThat(
        nullEsp.getEncryptionType(),
        equalTo(
            org.batfish.vendor.aruba_aoscx.representation
                .AosCxOspfv3Encryption.EncryptionType.NULL));

    assertThat(
        nullEsp.getEncryptionKey(),
        equalTo(
            null));
  }

  @Test
  public void testOspfv3VirtualLinkEncryptionConversion()
      throws IOException {

    Map<String, Configuration> configs =
        parseTextConfigs(
            "aoscx-ospfv3-virtual-link-encryption");

    Configuration c =
        configs.get(
            "aoscx-router");

    assertThat(
        c,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3Process process =
        c.getDefaultVrf()
            .getOspfv3Processes()
            .get("1");

    assertThat(
        process,
        notNullValue());

    org.batfish.datamodel.ospf.Ospfv3VirtualLink encrypted =
        process
            .getVirtualLinks()
            .stream()
            .filter(
                link ->
                    link.getTransitArea()
                        == 1L)
            .findFirst()
            .orElseThrow();

    assertThat(
        encrypted.getEncryption(),
        equalTo(
            new org.batfish.datamodel.ospf.Ospfv3Encryption(
                256L,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .AuthType.SHA1,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "vlink-auth",
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .EncryptionType.AES,
                org.batfish.datamodel.ospf.Ospfv3Encryption
                    .KeyType.PLAINTEXT,
                "0123456789abcdef")));

    org.batfish.datamodel.ospf.Ospfv3VirtualLink cleared =
        process
            .getVirtualLinks()
            .stream()
            .filter(
                link ->
                    link.getTransitArea()
                        == 2L)
            .findFirst()
            .orElseThrow();

    assertThat(
        cleared.getEncryption(),
        equalTo(
            null));

    org.batfish.datamodel.ospf.Ospfv3VirtualLink nullEsp =
        process
            .getVirtualLinks()
            .stream()
            .filter(
                link ->
                    link.getTransitArea()
                        == 3L)
            .findFirst()
            .orElseThrow();

    assertThat(
        nullEsp.getEncryption(),
        notNullValue());

    assertThat(
        nullEsp
            .getEncryption()
            .getEncryptionType(),
        equalTo(
            org.batfish.datamodel.ospf.Ospfv3Encryption
                .EncryptionType.NULL));
  }

}
