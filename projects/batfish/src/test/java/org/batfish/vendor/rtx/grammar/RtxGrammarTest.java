package org.batfish.vendor.rtx.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.YAMAHA_RTX;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.rtx.representation.RtxBgpImportRule;
import org.batfish.vendor.rtx.representation.RtxBgpNeighbor;
import org.batfish.vendor.rtx.representation.RtxBgpRouteFilter;
import org.batfish.vendor.rtx.representation.RtxConfiguration;
import org.batfish.vendor.rtx.representation.RtxConversions;
import org.batfish.vendor.rtx.representation.RtxFilter;
import org.batfish.vendor.rtx.representation.RtxInterface;
import org.batfish.vendor.rtx.representation.RtxNatDescriptor;
import org.batfish.vendor.rtx.representation.RtxVrrpGroup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of the Yamaha RTX grammar, extraction, and conversion. */
@ParametersAreNonnullByDefault
public final class RtxGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/rtx/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  // ----------------------------------------------------------------- hostname

  @Test
  public void testHostnameExtraction() {
    // RTX has no host-name command; `console prompt` names the device.
    assertThat(parseVendorConfig("rtx_hostname").getHostname(), equalTo("rtx_hostname"));
  }

  @Test
  public void testFormatDetectionAndConversion() throws IOException {
    Configuration c = parseConfig("rtx_hostname");
    assertThat(c, hasConfigurationFormat(YAMAHA_RTX));
  }

  // --------------------------------------------------------------- null lines

  @Test
  public void testNullCommandsParseWithoutWarnings() {
    // Every command that does not affect what Batfish models must still be recognized, including
    // the whole IPv6 configuration.
    RtxConfiguration vc = parseVendorConfig("rtx_null_commands");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(vc.getUnrecognized(), equalTo(false));
    assertThat(vc.getHostname(), equalTo("rtx_null_commands"));
    // No IPv6 state leaks into the model.
    assertThat(vc.getInterfaces(), not(hasKey("lan1")));
  }

  // -------------------------------------------------------------- interfaces

  @Test
  public void testInterfaceExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_interfaces");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(
        vc.getInterfaces().keySet(),
        containsInAnyOrder("lan1", "lan3", "lan3/1", "lan3/2", "tunnel1"));

    RtxInterface lan1 = vc.getInterfaces().get("lan1");
    assertThat(lan1.getType(), equalTo(RtxInterface.Type.LAN));
    assertThat(
        lan1.getAddresses(),
        contains(
            ConcreteInterfaceAddress.parse("10.0.0.1/30"),
            ConcreteInterfaceAddress.parse("10.0.1.1/24")));

    RtxInterface sub = vc.getInterfaces().get("lan3/1");
    assertThat(sub.getType(), equalTo(RtxInterface.Type.LAN_SUBINTERFACE));
    assertThat(sub.getVlanId(), equalTo(501));
    assertThat(sub.getParentInterface(), equalTo("lan3"));
    assertThat(sub.getAddresses(), contains(ConcreteInterfaceAddress.parse("192.168.1.1/24")));

    // The parent LAN interface is created implicitly; RTX configs need not mention it.
    RtxInterface parent = vc.getInterfaces().get("lan3");
    assertThat(parent.getType(), equalTo(RtxInterface.Type.LAN));
    assertThat(parent.getAddresses(), empty());

    assertThat(vc.getInterfaces().get("tunnel1").getType(), equalTo(RtxInterface.Type.TUNNEL));
  }

  @Test
  public void testInterfaceConversion() throws IOException {
    Configuration c = parseConfig("rtx_interfaces");

    Interface lan1 = c.getAllInterfaces().get("lan1");
    assertThat(lan1.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertThat(lan1.getConcreteAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.0.1/30")));
    assertThat(
        lan1.getAllConcreteAddresses(),
        containsInAnyOrder(
            ConcreteInterfaceAddress.parse("10.0.0.1/30"),
            ConcreteInterfaceAddress.parse("10.0.1.1/24")));

    Interface sub = c.getAllInterfaces().get("lan3/1");
    assertThat(sub.getInterfaceType(), equalTo(InterfaceType.LOGICAL));
    assertThat(sub.getEncapsulationVlan(), equalTo(501));
    assertThat(
        sub.getDependencies(),
        contains(new Interface.Dependency("lan3", Interface.DependencyType.BIND)));

    assertThat(
        c.getAllInterfaces().get("tunnel1").getInterfaceType(), equalTo(InterfaceType.TUNNEL));
  }

  // ------------------------------------------------------------------ filters

  @Test
  public void testFilterExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_filters");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    assertThat(
        vc.getFilters().keySet(),
        containsInAnyOrder(101L, 102L, 103L, 104L, 105L, 106L, 107L, 200L, 201L));

    // `pass * 10.57.244.8/32 tcp,udp * domain,https`
    RtxFilter f101 = vc.getFilters().get(101L);
    assertThat(f101.getAction(), equalTo(LineAction.PERMIT));
    assertThat(f101.getSrcIps(), nullValue());
    assertThat(f101.getDstIps(), equalTo(Prefix.parse("10.57.244.8/32").toIpSpace()));
    assertThat(f101.getProtocols(), equalTo(ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP)));
    assertThat(f101.getSrcPorts(), nullValue());
    assertThat(
        f101.getDstPorts(), equalTo(IntegerSpace.builder().including(53).including(443).build()));

    // reject-nolog maps to DENY, and a comma-separated address list is a union.
    RtxFilter f102 = vc.getFilters().get(102L);
    assertThat(f102.getAction(), equalTo(LineAction.DENY));
    assertThat(f102.getDstIps(), notNullValue());

    // `pass * * icmp * *` constrains only the protocol.
    RtxFilter f103 = vc.getFilters().get(103L);
    assertThat(f103.getProtocols(), equalTo(ImmutableSet.of(IpProtocol.ICMP)));
    assertThat(f103.getIcmpTypes(), nullValue());
    assertThat(f103.getIcmpCodes(), nullValue());

    // `established` means TCP with the ACK flag set; trailing arguments default to the wildcard.
    RtxFilter f104 = vc.getFilters().get(104L);
    assertThat(f104.getEstablished(), equalTo(true));
    assertThat(f104.getProtocols(), equalTo(ImmutableSet.of(IpProtocol.TCP)));
    assertThat(f104.getSrcPorts(), nullValue());
    assertThat(f104.getDstPorts(), nullValue());

    // A hyphenated address pair is an inclusive range.
    assertThat(vc.getFilters().get(105L).getSrcIps(), notNullValue());

    // A range endpoint may be written as a mnemonic: `135-netbios_ssn,445`.
    assertThat(
        vc.getFilters().get(106L).getDstPorts(),
        equalTo(
            IntegerSpace.builder()
                .including(com.google.common.collect.Range.closed(135, 139))
                .including(445)
                .build()));

    // A wildcard protocol combined with a port constraint is narrowed to TCP and UDP, since only
    // those headers have ports.
    RtxFilter f107 = vc.getFilters().get(107L);
    assertThat(f107.getProtocols(), equalTo(ImmutableSet.of(IpProtocol.TCP, IpProtocol.UDP)));
    assertThat(
        f107.getDstPorts(),
        equalTo(
            IntegerSpace.builder()
                .including(com.google.common.collect.Range.closed(8301, 8332))
                .build()));
  }

  @Test
  public void testSecureFilterExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_filters");
    RtxInterface lan1 = vc.getInterfaces().get("lan1");
    assertThat(
        lan1.getInboundFilters(),
        equalTo(ImmutableList.of(101L, 102L, 103L, 104L, 105L, 106L, 107L)));
    // Dynamic filters are stateful and are not part of the static model.
    assertThat(lan1.getOutboundFilters(), equalTo(ImmutableList.of(200L, 201L)));
  }

  @Test
  public void testSecureFilterConversion() throws IOException {
    Configuration c = parseConfig("rtx_filters");
    String inName = RtxConversions.computeInboundFilterName("lan1");
    String outName = RtxConversions.computeOutboundFilterName("lan1");
    assertThat(c.getIpAccessLists(), hasKey(inName));
    assertThat(c.getIpAccessLists(), hasKey(outName));
    assertThat(c.getAllInterfaces().get("lan1").getIncomingFilter().getName(), equalTo(inName));
    assertThat(c.getAllInterfaces().get("lan1").getOutgoingFilter().getName(), equalTo(outName));

    // Seven referenced filters plus the implicit deny that ends every secure filter list.
    IpAccessList in = c.getIpAccessLists().get(inName);
    assertThat(in.getLines(), org.hamcrest.Matchers.hasSize(8));

    // A DNS query to the permitted server is accepted by filter 101.
    assertThat(
        in.filter(
                dnsFlow(Ip.parse("10.1.1.1"), Ip.parse("10.57.244.8")),
                "lan1",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getAction(),
        equalTo(LineAction.PERMIT));

    // A DNS query to a host inside the ranges rejected by filter 102 is denied.
    assertThat(
        in.filter(
                dnsFlow(Ip.parse("10.1.1.1"), Ip.parse("10.57.240.1")),
                "lan1",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testSecureFilterImplicitDeny() throws IOException {
    Configuration c = parseConfig("rtx_filters");
    // The outbound list is `200 201`, where 200 is a pass-all. Replace the question with the
    // inbound list, which has no pass-all: traffic matching none of its filters must be denied,
    // because `ip <if> secure filter` discards packets that match no filter.
    IpAccessList in = c.getIpAccessLists().get(RtxConversions.computeInboundFilterName("lan1"));
    Flow unmatched =
        Flow.builder()
            .setIngressNode("rtx_filters")
            .setSrcIp(Ip.parse("10.1.1.1"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.OSPF)
            .build();
    assertThat(
        in.filter(unmatched, "lan1", c.getIpAccessLists(), c.getIpSpaces()).getAction(),
        equalTo(LineAction.DENY));
  }

  @Test
  public void testFilterWarnings() {
    // A filter whose arguments the device cannot interpret is ignored by the device, so Batfish
    // warns and drops the definition rather than guessing.
    RtxConfiguration vc = parseVendorConfig("rtx_filter_warnings");
    assertThat(
        vc.getWarnings().getParseWarnings(),
        containsInAnyOrder(
            hasComment("Cannot interpret 'tcp,udp' as a destination address; ignoring this filter"),
            hasComment("Cannot interpret 'bogusproto' as a protocol; ignoring this filter"),
            hasComment(
                "Cannot interpret 'nosuchservice' as a destination port list; ignoring this"
                    + " filter")));
    assertThat(vc.getFilters().keySet(), empty());
    // The interface still references them; the references are dangling.
    assertThat(
        vc.getInterfaces().get("lan1").getInboundFilters(),
        equalTo(ImmutableList.of(301L, 302L, 303L)));
  }

  @Test
  public void testUndefinedFilterReferenceIsOmitted() throws IOException {
    Configuration c = parseConfig("rtx_filter_warnings");
    IpAccessList in = c.getIpAccessLists().get(RtxConversions.computeInboundFilterName("lan1"));
    // None of the three referenced filters could be interpreted, so only the implicit deny remains.
    assertThat(in.getLines(), org.hamcrest.Matchers.hasSize(1));
  }

  // ------------------------------------------------------------------- routes

  @Test
  public void testStaticRouteConversion() throws IOException {
    Configuration c = parseConfig("rtx_routes");
    assertThat(
        c.getDefaultVrf().getStaticRoutes(),
        containsInAnyOrder(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
                .setAdministrativeCost(1)
                .setMetric(0L)
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("172.16.0.0/16"))
                .setNextHop(NextHopIp.of(Ip.parse("10.0.0.3")))
                .setAdministrativeCost(1)
                .setMetric(0L)
                .build(),
            // A bare address is a host route, and `gateway tunnel 1` points at the tunnel.
            StaticRoute.builder()
                .setNetwork(Prefix.parse("192.0.2.1/32"))
                .setNextHop(NextHopInterface.of("tunnel1"))
                .setAdministrativeCost(1)
                .setMetric(0L)
                .build()));
  }

  // --------------------------------------------------------------------- VRRP

  @Test
  public void testVrrpExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_vrrp");
    assertThat(vc.getWarnings().getParseWarnings(), empty());
    Map<Integer, RtxVrrpGroup> groups = vc.getInterfaces().get("lan4/1").getVrrpGroups();
    assertThat(groups.keySet(), containsInAnyOrder(10, 20, 30));

    assertThat(groups.get(10).getVirtualAddress(), equalTo(Ip.parse("10.57.0.4")));
    assertThat(groups.get(10).getPriority(), equalTo(200));
    assertThat(groups.get(10).getPreempt(), equalTo(true));

    assertThat(groups.get(20).getPriority(), equalTo(100));
    assertThat(groups.get(20).getPreempt(), equalTo(false));

    // Priority and preempt both default when unspecified.
    assertThat(groups.get(30).getPriority(), equalTo(RtxVrrpGroup.DEFAULT_PRIORITY));
    assertThat(groups.get(30).getPreempt(), equalTo(RtxVrrpGroup.DEFAULT_PREEMPT));
  }

  @Test
  public void testVrrpConversion() throws IOException {
    Configuration c = parseConfig("rtx_vrrp");
    Map<Integer, VrrpGroup> groups = c.getAllInterfaces().get("lan4/1").getVrrpGroups();
    assertThat(groups.keySet(), containsInAnyOrder(10, 20, 30));
    VrrpGroup group = groups.get(10);
    assertThat(group.getPriority(), equalTo(200));
    assertThat(group.getPreempt(), equalTo(true));
    assertThat(group.getSourceAddress(), equalTo(ConcreteInterfaceAddress.parse("10.57.0.2/18")));
    assertThat(
        group.getVirtualAddresses(),
        hasEntry(equalTo("lan4/1"), containsInAnyOrder(Ip.parse("10.57.0.4"))));
  }

  @Test
  public void testVrrpElectionAcrossBroadcastDomains() throws IOException {
    // The J57 core routers reuse the same VRIDs on every VLAN sub-interface, so the winner of each
    // election can only be determined per broadcast domain. That requires layer-1 topology; with
    // it, each virtual address is owned by whichever router has the higher priority for that VRID.
    String l1Topology =
        "{\"edges\":[{\"node1\":{\"hostname\":\"rtx_vrrp_a\",\"interfaceName\":\"lan4\"},"
            + "\"node2\":{\"hostname\":\"rtx_vrrp_b\",\"interfaceName\":\"lan4\"}}]}";
    IBatfish batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationText(
                    ImmutableMap.of(
                        "rtx_vrrp_a", readResource(TESTCONFIGS_PREFIX + "rtx_vrrp_a", UTF_8),
                        "rtx_vrrp_b", readResource(TESTCONFIGS_PREFIX + "rtx_vrrp_b", UTF_8)))
                .setLayer1TopologyBytes(l1Topology.getBytes(UTF_8))
                .build(),
            _folder.newFolder().toPath());
    batfish.loadConfigurations(batfish.getSnapshot());
    Map<Ip, Set<String>> owners =
        batfish.getTopologyProvider().getInitialIpOwners(batfish.getSnapshot()).getNodeOwners(true);

    // VRID 10 is higher-priority on A, VRID 20 on B, on both VLANs.
    assertThat(owners, hasEntry(Ip.parse("10.57.0.4"), ImmutableSet.of("rtx_vrrp_a")));
    assertThat(owners, hasEntry(Ip.parse("10.57.242.4"), ImmutableSet.of("rtx_vrrp_a")));
    assertThat(owners, hasEntry(Ip.parse("10.57.0.5"), ImmutableSet.of("rtx_vrrp_b")));
    assertThat(owners, hasEntry(Ip.parse("10.57.242.5"), ImmutableSet.of("rtx_vrrp_b")));
  }

  // ---------------------------------------------------------------------- NAT

  @Test
  public void testNatDescriptorExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_nat");
    // The unmodeled sub-commands (timer, session limit, log) must not produce warnings.
    assertThat(vc.getWarnings().getParseWarnings(), empty());

    assertThat(vc.getNatDescriptors().keySet(), contains(1));
    RtxNatDescriptor descriptor = vc.getNatDescriptors().get(1);
    assertThat(descriptor.getType(), equalTo(RtxNatDescriptor.Type.MASQUERADE));
    assertThat(descriptor.getFirstOuterAddress(), equalTo(Ip.parse("211.14.212.249")));
    assertThat(
        descriptor
            .getOuterAddresses()
            .encloses(Range.closed(Ip.parse("211.14.212.249"), Ip.parse("211.14.212.254"))),
        equalTo(true));
    assertThat(descriptor.getInnerAddresses(), notNullValue());
    assertThat(
        descriptor.getMasqueradePorts(),
        equalTo(IntegerSpace.builder().including(Range.closed(1024, 65534)).build()));

    assertThat(vc.getInterfaces().get("lan3/3").getNatDescriptors(), equalTo(ImmutableList.of(1)));
  }

  @Test
  public void testMasqueradeTranslatesSourceAddressAndPort() throws IOException {
    Configuration c = parseConfig("rtx_nat");
    Interface outside = c.getAllInterfaces().get("lan3/3");
    assertThat(outside.getOutgoingTransformation(), notNullValue());

    Flow flow =
        Flow.builder()
            .setIngressNode("rtx_nat")
            .setSrcIp(Ip.parse("10.57.0.100"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(NamedPort.HTTPS.number())
            .build();
    Flow translated =
        TransformationEvaluator.eval(
                outside.getOutgoingTransformation(),
                flow,
                "lan4/1",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getOutputFlow();

    // Masquerade uses only the first of the six outer addresses.
    assertThat(translated.getSrcIp(), equalTo(Ip.parse("211.14.212.249")));
    assertThat(translated.getSrcPort(), org.hamcrest.Matchers.greaterThanOrEqualTo(1024));
    // Everything else is untouched.
    assertThat(translated.getDstIp(), equalTo(Ip.parse("8.8.8.8")));
    assertThat(translated.getDstPort(), equalTo(NamedPort.HTTPS.number()));
  }

  @Test
  public void testMasqueradeSkipsSourcesOutsideInnerRange() throws IOException {
    Configuration c = parseConfig("rtx_nat");
    Interface outside = c.getAllInterfaces().get("lan3/3");
    // `nat descriptor address inner 1 10.57.0.0-10.57.255.255` limits which sources are
    // translated.
    Flow flow =
        Flow.builder()
            .setIngressNode("rtx_nat")
            .setSrcIp(Ip.parse("192.0.2.5"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(NamedPort.HTTPS.number())
            .build();
    assertThat(
        TransformationEvaluator.eval(
                outside.getOutgoingTransformation(),
                flow,
                "lan4/1",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getOutputFlow(),
        equalTo(flow));
  }

  @Test
  public void testMasqueradeSkipsProtocolsWithoutPorts() throws IOException {
    Configuration c = parseConfig("rtx_nat");
    Interface outside = c.getAllInterfaces().get("lan3/3");
    // Masquerade needs a port or ICMP identifier to demultiplex return traffic.
    Flow flow =
        Flow.builder()
            .setIngressNode("rtx_nat")
            .setSrcIp(Ip.parse("10.57.0.100"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.OSPF)
            .build();
    assertThat(
        TransformationEvaluator.eval(
                outside.getOutgoingTransformation(),
                flow,
                "lan4/1",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getOutputFlow(),
        equalTo(flow));
  }

  @Test
  public void testNatSetsUpSessionForReturnTraffic() throws IOException {
    Configuration c = parseConfig("rtx_nat");
    FirewallSessionInterfaceInfo session =
        c.getAllInterfaces().get("lan3/3").getFirewallSessionInterfaceInfo();
    assertThat(session, notNullValue());
    assertThat(session.getAction(), equalTo(FirewallSessionInterfaceInfo.Action.FORWARD_OUT_IFACE));
    assertThat(session.getSessionInterfaces(), contains("lan3/3"));
    // Session traffic is still filtered by the interface's own secure filters.
    assertThat(
        session.getIncomingAclName(), equalTo(RtxConversions.computeInboundFilterName("lan3/3")));
    assertThat(
        session.getOutgoingAclName(), equalTo(RtxConversions.computeOutboundFilterName("lan3/3")));
    // An interface with no NAT descriptor gets no session.
    assertThat(c.getAllInterfaces().get("lan4/1").getFirewallSessionInterfaceInfo(), nullValue());
  }

  @Test
  public void testDynamicNatUsesWholePoolAndLeavesPortsAlone() throws IOException {
    Configuration c = parseConfig("rtx_nat_dynamic");
    Interface iface = c.getAllInterfaces().get("lan2");
    assertThat(iface.getOutgoingTransformation(), notNullValue());

    Flow flow =
        Flow.builder()
            .setIngressNode("rtx_nat_dynamic")
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.TCP)
            .setSrcPort(12345)
            .setDstPort(NamedPort.HTTPS.number())
            .build();
    Flow translated =
        TransformationEvaluator.eval(
                iface.getOutgoingTransformation(),
                flow,
                "lan3",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getOutputFlow();
    // `type nat` translates the address from the pool but not the port.
    assertThat(translated.getSrcIp(), equalTo(Ip.parse("192.0.2.10")));
    assertThat(translated.getSrcPort(), equalTo(12345));
  }

  @Test
  public void testMasqueradeWithPrimaryOuterAddress() throws IOException {
    // `nat descriptor address outer <n> primary` means the interface's own primary address.
    Configuration c = parseConfig("rtx_nat_dynamic");
    Interface iface = c.getAllInterfaces().get("lan3");
    Flow flow =
        Flow.builder()
            .setIngressNode("rtx_nat_dynamic")
            .setSrcIp(Ip.parse("10.0.0.1"))
            .setDstIp(Ip.parse("8.8.8.8"))
            .setIpProtocol(IpProtocol.UDP)
            .setSrcPort(12345)
            .setDstPort(53)
            .build();
    Flow translated =
        TransformationEvaluator.eval(
                iface.getOutgoingTransformation(),
                flow,
                "lan2",
                c.getIpAccessLists(),
                c.getIpSpaces())
            .getOutputFlow();
    assertThat(translated.getSrcIp(), equalTo(Ip.parse("198.51.100.1")));
    // No `masquerade port range`, so the device's default ranges apply.
    assertThat(
        RtxNatDescriptor.DEFAULT_MASQUERADE_PORTS.contains(translated.getSrcPort()), equalTo(true));
  }

  @Test
  public void testUnmodeledNatIsWarnedNotTranslated() throws IOException {
    RtxConfiguration vc = parseVendorConfig("rtx_nat_unmodeled");
    // `reverse` descriptors are parsed but not modeled.
    assertThat(
        vc.getWarnings().getParseWarnings(), hasItem(hasText("nat descriptor 9 reverse 10")));
    assertThat(
        vc.getInterfaces().get("lan1").getReverseNatDescriptors(), equalTo(ImmutableList.of(10)));
    // `static` and `backward-compatibility` are recognized but discarded, and `type none`
    // translates nothing.
    assertThat(vc.getNatDescriptors().keySet(), containsInAnyOrder(9, 10));
    assertThat(vc.getNatDescriptors().get(10).getType(), equalTo(RtxNatDescriptor.Type.NONE));
    assertThat(vc.getUnrecognized(), equalTo(false));

    // The outer address is learned over PPP IPCP, so there is nothing to translate to.
    Configuration c = parseConfig("rtx_nat_unmodeled");
    assertThat(c.getAllInterfaces().get("lan1").getOutgoingTransformation(), nullValue());
  }

  // ---------------------------------------------------------------------- BGP

  @Test
  public void testBgpExtraction() {
    RtxConfiguration vc = parseVendorConfig("rtx_bgp");
    assertThat(vc.getBgpProcess(), notNullValue());
    assertThat(vc.getBgpProcess().getEnabled(), equalTo(true));
    assertThat(vc.getBgpProcess().getLocalAs(), equalTo(65000L));
    Map<Integer, RtxBgpNeighbor> neighbors = vc.getBgpProcess().getNeighbors();
    assertThat(neighbors.keySet(), containsInAnyOrder(10, 20));
    assertThat(neighbors.get(10).getRemoteAs(), equalTo(64998L));
    assertThat(neighbors.get(10).getRemoteAddress(), equalTo(Ip.parse("192.168.157.21")));
    assertThat(neighbors.get(20).getRemoteAs(), equalTo(65000L));
    assertThat(vc.getBgpProcess().getImportFilters().keySet(), containsInAnyOrder(10, 20, 30));
    assertThat(vc.getBgpProcess().getImportFilters().get(10).getAction(), equalTo(LineAction.DENY));
    assertThat(
        vc.getBgpProcess().getImportFilters().get(10).getMatchType(),
        equalTo(RtxBgpRouteFilter.MatchType.EQUAL));
    assertThat(
        vc.getBgpProcess().getImportFilters().get(20).getMatchType(),
        equalTo(RtxBgpRouteFilter.MatchType.INCLUDE));
    assertThat(
        vc.getBgpProcess().getImportFilters().get(30).getMatchType(),
        equalTo(RtxBgpRouteFilter.MatchType.REFINES));
    assertThat(vc.getBgpProcess().getImportRules(), org.hamcrest.Matchers.hasSize(4));
    RtxBgpImportRule bgpRule = vc.getBgpProcess().getImportRules().get(1);
    assertThat(bgpRule.getProtocol(), equalTo(RtxBgpImportRule.Protocol.BGP));
    assertThat(bgpRule.getFromAs(), equalTo(65000L));
    assertThat(bgpRule.getFilters(), contains(10, 20));
    assertThat(vc.getBgpProcess().getExportFilters().keySet(), contains(10));
    assertThat(vc.getBgpProcess().getExportRules(), org.hamcrest.Matchers.hasSize(2));
    assertThat(vc.getWarnings().getParseWarnings(), empty());
  }

  @Test
  public void testBgpConversion() throws IOException {
    Configuration c = parseConfig("rtx_bgp");
    assertThat(c.getDefaultVrf().getBgpProcess(), notNullValue());
    assertThat(
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors(), hasKey(Ip.parse("192.168.157.21")));
    assertThat(
        c.getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("192.168.157.21"))
            .getLocalAs(),
        equalTo(65000L));
    // The local IP is the address of the interface on the peer's subnet.
    assertThat(
        c.getDefaultVrf()
            .getBgpProcess()
            .getActiveNeighbors()
            .get(Ip.parse("192.168.157.21"))
            .getLocalIp(),
        equalTo(Ip.parse("192.168.157.22")));

    BgpActivePeerConfig ebgp =
        c.getDefaultVrf().getBgpProcess().getActiveNeighbors().get(Ip.parse("192.168.157.21"));
    String importPolicyName = ebgp.getIpv4UnicastAddressFamily().getImportPolicy();
    String exportPolicyName = ebgp.getIpv4UnicastAddressFamily().getExportPolicy();
    assertThat(
        importPolicyName,
        equalTo(RtxConversions.computeBgpImportPolicyName(ebgp.getPeerAddress())));
    assertThat(
        exportPolicyName,
        equalTo(RtxConversions.computeBgpExportPolicyName(ebgp.getPeerAddress())));
    RoutingPolicy importPolicy = c.getRoutingPolicies().get(importPolicyName);
    RoutingPolicy exportPolicy = c.getRoutingPolicies().get(exportPolicyName);
    assertThat(importPolicy, notNullValue());
    assertThat(exportPolicy, notNullValue());

    // `include 0.0.0.0/0` means the default and every more-specific route, not only default.
    assertTrue(importPolicy.processReadOnly(bgpRoute(Prefix.ZERO, 64998L)));
    assertTrue(importPolicy.processReadOnly(bgpRoute(Prefix.parse("203.0.113.0/24"), 64998L)));

    // For advertisements to AS 64998, filter 10 rejects only default and filter 20 then permits
    // the remaining static and iBGP routes.
    assertFalse(exportPolicy.processReadOnly(staticRoute(Prefix.ZERO)));
    assertTrue(exportPolicy.processReadOnly(staticRoute(Prefix.parse("10.57.0.0/16"))));
    assertFalse(exportPolicy.processReadOnly(ibgpRoute(Prefix.ZERO)));
    assertTrue(exportPolicy.processReadOnly(ibgpRoute(Prefix.parse("10.57.0.0/16"))));

    // A route learned from the external AS does not satisfy the `bgp 65000` source clause.
    assertFalse(exportPolicy.processReadOnly(bgpRoute(Prefix.parse("198.51.100.0/24"), 64998L)));

    // Every neighbor now has both policies, preventing the null-policy data-plane failure.
    c.getDefaultVrf()
        .getBgpProcess()
        .getActiveNeighbors()
        .values()
        .forEach(
            peer -> {
              assertThat(peer.getIpv4UnicastAddressFamily().getImportPolicy(), notNullValue());
              assertThat(peer.getIpv4UnicastAddressFamily().getExportPolicy(), notNullValue());
            });
  }

  @Test
  public void testBgpRouteFiltersInDataPlane() throws IOException {
    Batfish batfish = getBatfishForConfigurationNames("rtx_bgp_exporter", "rtx_bgp_receiver");
    batfish.computeDataPlane(batfish.getSnapshot());
    Set<Prefix> receiverBgpNetworks =
        batfish
            .loadDataPlane(batfish.getSnapshot())
            .getBgpRoutes()
            .get("rtx_bgp_receiver", Configuration.DEFAULT_VRF_NAME)
            .stream()
            .map(Bgpv4Route::getNetwork)
            .collect(ImmutableSet.toImmutableSet());

    // The two peers establish, and filter 10 rejects only default before filter 20 permits the
    // more-specific static route.
    assertThat(receiverBgpNetworks, equalTo(ImmutableSet.of(Prefix.parse("10.57.0.0/16"))));
  }

  // ------------------------------------------------------------------ helpers

  private static @Nonnull Flow dnsFlow(Ip srcIp, Ip dstIp) {
    return Flow.builder()
        .setIngressNode("rtx")
        .setSrcIp(srcIp)
        .setDstIp(dstIp)
        .setIpProtocol(IpProtocol.UDP)
        .setSrcPort(NamedPort.EPHEMERAL_LOWEST.number())
        .setDstPort(NamedPort.DOMAIN.number())
        .build();
  }

  private static @Nonnull StaticRoute staticRoute(Prefix network) {
    return StaticRoute.testBuilder().setNetwork(network).build();
  }

  private static @Nonnull Bgpv4Route bgpRoute(Prefix network, long firstAs) {
    return Bgpv4Route.testBuilder()
        .setNetwork(network)
        .setProtocol(RoutingProtocol.BGP)
        .setAsPath(AsPath.ofSingletonAsSets(firstAs))
        .build();
  }

  private static @Nonnull Bgpv4Route ibgpRoute(Prefix network) {
    return Bgpv4Route.testBuilder().setNetwork(network).setProtocol(RoutingProtocol.IBGP).build();
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(hostname);
    Map<String, Configuration> configs = iBatfish.loadConfigurations(iBatfish.getSnapshot());
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull RtxConfiguration parseVendorConfig(String hostname) {
    return parseVendorConfigText(
        readResource(TESTCONFIGS_PREFIX + hostname, UTF_8), TESTCONFIGS_PREFIX + hostname);
  }

  /** Parses {@code src} directly as an RTX config, attributing it to {@code name}. */
  private @Nonnull RtxConfiguration parseVendorConfigText(String src, String name) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    RtxCombinedParser parser = new RtxCombinedParser(src, settings);
    Warnings warnings = new Warnings(true, true, true);
    RtxControlPlaneExtractor extractor =
        new RtxControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    RtxConfiguration vc = (RtxConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(name);

    // Crash if the vendor model is not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }
}
