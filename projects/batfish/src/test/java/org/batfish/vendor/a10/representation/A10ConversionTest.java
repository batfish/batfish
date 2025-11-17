package org.batfish.vendor.a10.representation;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasRedFlag;
import static org.batfish.datamodel.BgpTieBreaker.ROUTER_ID;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasSendCommunity;
import static org.batfish.datamodel.matchers.AddressFamilyCapabilitiesMatchers.hasSendExtendedCommunity;
import static org.batfish.datamodel.matchers.AddressFamilyMatchers.hasAddressFamilyCapabilites;
import static org.batfish.datamodel.matchers.AddressFamilyMatchers.hasExportPolicy;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasDescription;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasIpv4UnicastAddressFamily;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalAs;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasLocalIp;
import static org.batfish.datamodel.matchers.BgpNeighborMatchers.hasRemoteAs;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasActiveNeighbor;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathEbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasMultipathIbgp;
import static org.batfish.datamodel.matchers.BgpProcessMatchers.hasRouterId;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.batfish.vendor.a10.representation.A10Configuration.arePortTypesCompatible;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_EBGP_ADMIN_COST;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_IBGP_ADMIN_COST;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_LOCAL_ADMIN_COST;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_VRRP_A_PREEMPT;
import static org.batfish.vendor.a10.representation.A10Conversion.DEFAULT_VRRP_A_PRIORITY;
import static org.batfish.vendor.a10.representation.A10Conversion.KERNEL_ROUTE_TAG_FLOATING_IP;
import static org.batfish.vendor.a10.representation.A10Conversion.KERNEL_ROUTE_TAG_NAT_POOL;
import static org.batfish.vendor.a10.representation.A10Conversion.KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED;
import static org.batfish.vendor.a10.representation.A10Conversion.KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED;
import static org.batfish.vendor.a10.representation.A10Conversion.computeAclName;
import static org.batfish.vendor.a10.representation.A10Conversion.computeUpdateSource;
import static org.batfish.vendor.a10.representation.A10Conversion.convertAccessList;
import static org.batfish.vendor.a10.representation.A10Conversion.createAndAttachBgpNeighbor;
import static org.batfish.vendor.a10.representation.A10Conversion.createBgpProcess;
import static org.batfish.vendor.a10.representation.A10Conversion.findHaSourceAddress;
import static org.batfish.vendor.a10.representation.A10Conversion.findVrrpAEnabledSourceAddress;
import static org.batfish.vendor.a10.representation.A10Conversion.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Conversion.getNatPoolIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIps;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIpsByHaGroup;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerIpsForAllVrids;
import static org.batfish.vendor.a10.representation.A10Conversion.getVirtualServerKernelRoutes;
import static org.batfish.vendor.a10.representation.A10Conversion.toDstTransformationSteps;
import static org.batfish.vendor.a10.representation.A10Conversion.toIntegerSpace;
import static org.batfish.vendor.a10.representation.A10Conversion.toKernelRoute;
import static org.batfish.vendor.a10.representation.A10Conversion.toMatchExpr;
import static org.batfish.vendor.a10.representation.A10Conversion.toProtocol;
import static org.batfish.vendor.a10.representation.A10Conversion.toVrrpGroup;
import static org.batfish.vendor.a10.representation.A10Conversion.vrrpADisabledAppliesToInterface;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForAccessList;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForDestAddressAny;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForProtocol;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForSourceHost;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForVirtualServer;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForVirtualServerPort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AclTracer;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.a10.representation.BgpNeighbor.SendCommunity;
import org.junit.Test;

/** Tests of {@link A10Conversion}. */
public class A10ConversionTest {
  private final BddTestbed _tb = new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @Test
  public void testToIntegerSpace() {
    // Real ports
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, null)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, 0)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new ServerPort(80, ServerPort.Type.TCP, 10)),
        equalTo(IntegerSpace.of(new SubRange(80, 90))));

    // Virtual ports
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, null)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 0)),
        equalTo(IntegerSpace.of(new SubRange(80, 80))));
    assertThat(
        toIntegerSpace(new VirtualServerPort(80, VirtualServerPort.Type.TCP, 10)),
        equalTo(IntegerSpace.of(new SubRange(80, 90))));
  }

  @Test
  public void testToProtocol() {
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.HTTP, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.HTTPS, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.RADIUS, 0)),
        equalTo(Optional.of(IpProtocol.UDP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.TCP_PROXY, 0)),
        equalTo(Optional.of(IpProtocol.TCP)));
    assertThat(
        toProtocol(new VirtualServerPort(1, VirtualServerPort.Type.UDP, 0)),
        equalTo(Optional.of(IpProtocol.UDP)));

    // Make sure all types are handled
    for (VirtualServerPort.Type type : VirtualServerPort.Type.values()) {
      // Should not throw
      Optional<IpProtocol> protocol = toProtocol(new VirtualServerPort(1, type, 0));
      assertTrue(protocol.isPresent());
    }
  }

  @Test
  public void testArePortTypesCompatible() {
    List<VirtualServerPort.Type> tcpCompatibleVirtualTypes =
        ImmutableList.of(
            VirtualServerPort.Type.HTTP,
            VirtualServerPort.Type.HTTPS,
            VirtualServerPort.Type.TCP,
            VirtualServerPort.Type.TCP_PROXY);
    List<VirtualServerPort.Type> udpCompatibleVirtualTypes =
        ImmutableList.of(VirtualServerPort.Type.RADIUS, VirtualServerPort.Type.UDP);

    for (VirtualServerPort.Type typeToCheck : tcpCompatibleVirtualTypes) {
      assertTrue(arePortTypesCompatible(ServerPort.Type.TCP, typeToCheck));
      assertFalse(arePortTypesCompatible(ServerPort.Type.UDP, typeToCheck));
    }
    for (VirtualServerPort.Type typeToCheck : udpCompatibleVirtualTypes) {
      assertFalse(arePortTypesCompatible(ServerPort.Type.TCP, typeToCheck));
      assertTrue(arePortTypesCompatible(ServerPort.Type.UDP, typeToCheck));
    }

    // Make sure all types are handled
    for (VirtualServerPort.Type typeToCheck : VirtualServerPort.Type.values()) {
      assertTrue(
          arePortTypesCompatible(ServerPort.Type.TCP, typeToCheck)
              || arePortTypesCompatible(ServerPort.Type.UDP, typeToCheck));
    }
  }

  @Test
  public void testVirtualServerToMatchExpr() {
    Ip addr = Ip.parse("10.10.10.10");
    VirtualServerTarget vst = new VirtualServerTargetAddress(addr);
    VirtualServer vs = new VirtualServer("vs", vst);

    AclLineMatchExpr matchExpr = toMatchExpr(vs, "filename");
    assertThat(_tb.toBDD(matchExpr), equalTo(_tb.toBDD(AclLineMatchExprs.matchDst(addr))));
    assertThat(matchExpr.getTraceElement(), equalTo(traceElementForVirtualServer(vs, "filename")));
  }

  @Test
  public void testVirtualServerTargetToMatchExpr() {
    Ip addr = Ip.parse("10.10.10.10");
    VirtualServerTarget vst = new VirtualServerTargetAddress(addr);

    assertThat(
        _tb.toBDD(A10Conversion.VirtualServerTargetToMatchExpr.INSTANCE.visit(vst)),
        equalTo(_tb.toBDD(AclLineMatchExprs.matchDst(addr))));
  }

  @Test
  public void testVirtualServerPortToMatchExpr() {
    VirtualServerPort vsp = new VirtualServerPort(10, VirtualServerPort.Type.UDP, 1);

    AclLineMatchExpr matchExpr = toMatchExpr(vsp);
    assertThat(
        _tb.toBDD(matchExpr),
        equalTo(
            _tb.toBDD(
                AclLineMatchExprs.and(
                    AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP),
                    AclLineMatchExprs.matchDstPort(IntegerSpace.of(new SubRange(10, 11)))))));
    assertThat(matchExpr.getTraceElement(), equalTo(traceElementForVirtualServerPort(vsp)));
  }

  @Test
  public void testConvertAccessList() {
    /* Test conversion and tracing of an ACL with:
     *   1. explicit permit line
     *   2. explicit deny line
     *   3. implicit deny all at the end
     * */
    Configuration c = new Configuration("dummy", ConfigurationFormat.A10_ACOS);
    String filename = "filename";
    String aclName = "aclName";
    String viAclName = computeAclName(aclName);
    String ifaceName = "Ethernet1"; // Arbitrary interface
    Ip permittedHost = Ip.parse("10.0.0.1");
    Ip deniedHost = Ip.parse("10.0.0.2");
    Ip unmatchedHost = Ip.parse("10.0.0.3");
    Flow permittedFlow =
        Flow.builder()
            .setIngressNode("node")
            .setIngressInterface(ifaceName)
            .setIpProtocol(IpProtocol.TCP)
            .setSrcIp(permittedHost)
            // Arbitrary ports and dest ip
            .setDstIp(Ip.parse("10.0.0.4"))
            .setSrcPort(4096)
            .setDstPort(443)
            .build();
    Flow deniedFlow = permittedFlow.toBuilder().setSrcIp(deniedHost).build();
    Flow unmatchedFlow = permittedFlow.toBuilder().setSrcIp(unmatchedHost).build();

    AccessList acl = new AccessList(aclName);
    acl.addRule(
        new AccessListRuleTcp(
            AccessListRule.Action.PERMIT,
            new AccessListAddressHost(permittedHost),
            AccessListAddressAny.INSTANCE,
            "permit"));
    acl.addRule(
        new AccessListRuleTcp(
            AccessListRule.Action.DENY,
            new AccessListAddressHost(deniedHost),
            AccessListAddressAny.INSTANCE,
            "deny"));
    convertAccessList(acl, c, filename);
    assertThat(c.getIpAccessLists(), hasKey(viAclName));
    IpAccessList viAcl = c.getIpAccessLists().get(viAclName);
    Function<Flow, List<TraceTree>> trace =
        (flow) ->
            AclTracer.trace(
                viAcl,
                flow,
                ifaceName,
                c.getIpAccessLists(),
                c.getIpSpaces(),
                c.getIpSpaceMetadata());

    // Explicit permit line match
    {
      assertThat(viAcl, accepts(permittedFlow, ifaceName, ImmutableMap.of(), ImmutableMap.of()));
      List<TraceTree> traces = trace.apply(permittedFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  traceElementForAccessList(aclName, filename, true),
                  isTraceTree(traceElementForDestAddressAny()),
                  isTraceTree(traceElementForSourceHost(new AccessListAddressHost(permittedHost))),
                  isTraceTree(traceElementForProtocol(IpProtocol.TCP)))));
    }

    // Explicit deny line match
    {
      assertThat(viAcl, rejects(deniedFlow, ifaceName, ImmutableMap.of(), ImmutableMap.of()));
      List<TraceTree> traces = trace.apply(deniedFlow);
      assertThat(
          traces,
          contains(
              isTraceTree(
                  traceElementForAccessList(aclName, filename, false),
                  isTraceTree(traceElementForDestAddressAny()),
                  isTraceTree(traceElementForSourceHost(new AccessListAddressHost(deniedHost))),
                  isTraceTree(traceElementForProtocol(IpProtocol.TCP)))));
    }

    // No explicit match, implicit deny
    {
      assertThat(viAcl, rejects(unmatchedFlow, ifaceName, ImmutableMap.of(), ImmutableMap.of()));
      // No traces exist in this case
      assertThat(trace.apply(unmatchedFlow), emptyIterable());
    }
  }

  @Test
  public void testToDstTransformationSteps() {
    Ip server1Ip = Ip.parse("10.0.0.1");
    Server server1 = new Server("server1", new ServerTargetAddress(server1Ip));
    server1.getOrCreatePort(80, ServerPort.Type.TCP, null);
    server1.getOrCreatePort(90, ServerPort.Type.TCP, 1);
    ServerPort port = server1.getOrCreatePort(100, ServerPort.Type.TCP, null);
    port.setEnable(false);
    // Different protocol (UDP, not TCP)
    server1.getOrCreatePort(80, ServerPort.Type.UDP, 100);

    Ip server2Ip = Ip.parse("10.0.0.2");
    Server server2 = new Server("server2", new ServerTargetAddress(server2Ip));
    server2.getOrCreatePort(80, ServerPort.Type.TCP, null);
    server2.setEnable(false);

    Map<String, Server> servers = ImmutableMap.of("server1", server1, "server2", server2);
    ServiceGroup serviceGroup = new ServiceGroup("serviceGroup", ServerPort.Type.TCP);
    serviceGroup.getOrCreateMember("server1", 80);
    serviceGroup.getOrCreateMember("server1", 90);
    // Port not enabled
    serviceGroup.getOrCreateMember("server1", 100);
    // Server not enabled
    serviceGroup.getOrCreateMember("server2", 80);

    // Transformation steps include all *enabled* service-group member references
    assertThat(
        toDstTransformationSteps(serviceGroup, servers),
        containsInAnyOrder(
            new ApplyAll(
                TransformationStep.assignDestinationPort(80),
                TransformationStep.assignDestinationIp(server1Ip)),
            new ApplyAll(
                TransformationStep.assignDestinationPort(90, 91),
                TransformationStep.assignDestinationIp(server1Ip))));
  }

  @Test
  public void testGetNatPoolIps() {
    Ip pool0Start = Ip.parse("10.0.0.1");
    Ip pool0End = Ip.parse("10.0.0.3");
    Ip pool1Start = Ip.parse("10.0.1.1");
    Ip pool1End = Ip.parse("10.0.1.2");
    NatPool natPoolVrid0 = new NatPool("pool0", pool0Start, pool0End, 24);
    NatPool natPoolVrid1 = new NatPool("pool1", pool1Start, pool1End, 24);
    natPoolVrid1.setVrid(1);
    List<NatPool> natPools = ImmutableList.of(natPoolVrid0, natPoolVrid1);

    assertThat(
        getNatPoolIps(natPools, 0).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(pool0Start, pool0End, Ip.parse("10.0.0.2")));
    assertThat(
        getNatPoolIps(natPools, 1).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(pool1Start, pool1End));
  }

  @Test
  public void testGetVirtualServerIps() {
    Ip vs0Ip = Ip.parse("10.0.0.1");
    Ip vs1EnabledIp = Ip.parse("10.0.1.1");
    Ip vs1DisabledIp = Ip.parse("10.0.3.1");
    VirtualServer vs0 = new VirtualServer("vs0", new VirtualServerTargetAddress(vs0Ip));
    vs0.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    VirtualServer vs1Enabled =
        new VirtualServer("vs1Enabled", new VirtualServerTargetAddress(vs1EnabledIp));
    vs1Enabled.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1Enabled.setVrid(1);
    VirtualServer vs1EnabledIpv6 =
        new VirtualServer(
            "vs1EnabledIpv6", new VirtualServerTargetAddress6(Ip6.parse("dead:beef::1")));
    vs1EnabledIpv6.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1EnabledIpv6.setVrid(1);
    VirtualServer vs1Disabled =
        new VirtualServer("vs1Disabled", new VirtualServerTargetAddress(vs1DisabledIp));
    vs1Disabled.setVrid(1);
    vs1Disabled.setEnable(false);
    List<VirtualServer> virtualServers =
        ImmutableList.of(vs0, vs1Enabled, vs1EnabledIpv6, vs1Disabled);

    assertThat(
        getVirtualServerIps(virtualServers, 0).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs0Ip));
    assertThat(
        getVirtualServerIps(virtualServers, 1).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs1EnabledIp));
  }

  @Test
  public void testGetVirtualServerIpsForAllVrids() {
    Ip vs0Ip = Ip.parse("10.0.0.1");
    Ip vs1EnabledIp = Ip.parse("10.0.1.1");
    Ip vs1DisabledIp = Ip.parse("10.0.3.1");
    VirtualServer vs0 = new VirtualServer("vs0", new VirtualServerTargetAddress(vs0Ip));
    vs0.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    VirtualServer vs1Enabled =
        new VirtualServer("vs1Enabled", new VirtualServerTargetAddress(vs1EnabledIp));
    vs1Enabled.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1Enabled.setVrid(1);
    VirtualServer vs1EnabledIpv6 =
        new VirtualServer(
            "vs1EnabledIpv6", new VirtualServerTargetAddress6(Ip6.parse("dead:beef::1")));
    vs1EnabledIpv6.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1EnabledIpv6.setVrid(1);
    VirtualServer vs1Disabled =
        new VirtualServer("vs1Disabled", new VirtualServerTargetAddress(vs1DisabledIp));
    vs1Disabled.setVrid(1);
    vs1Disabled.setEnable(false);
    List<VirtualServer> virtualServers =
        ImmutableList.of(vs0, vs1Enabled, vs1EnabledIpv6, vs1Disabled);

    assertThat(
        getVirtualServerIpsForAllVrids(virtualServers).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs0Ip, vs1EnabledIp));
  }

  @Test
  public void testGetVirtualServerIpsByHaGroup() {
    Ip vs0Ip = Ip.parse("10.0.0.1");
    Ip vs1EnabledIp = Ip.parse("10.0.1.1");
    Ip vs1DisabledIp = Ip.parse("10.0.3.1");
    VirtualServer vs0 = new VirtualServer("vs0", new VirtualServerTargetAddress(vs0Ip));
    vs0.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    VirtualServer vs1Enabled =
        new VirtualServer("vs1Enabled", new VirtualServerTargetAddress(vs1EnabledIp));
    vs1Enabled.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1Enabled.setHaGroup(1);
    VirtualServer vs1EnabledIpv6 =
        new VirtualServer(
            "vs1EnabledIpv6", new VirtualServerTargetAddress6(Ip6.parse("dead:beef::1")));
    vs1EnabledIpv6.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1EnabledIpv6.setHaGroup(1);
    VirtualServer vs1Disabled =
        new VirtualServer("vs1Disabled", new VirtualServerTargetAddress(vs1DisabledIp));
    vs1Disabled.setHaGroup(1);
    vs1Disabled.setEnable(false);
    List<VirtualServer> virtualServers =
        ImmutableList.of(vs0, vs1Enabled, vs1EnabledIpv6, vs1Disabled);

    assertThat(
        getVirtualServerIpsByHaGroup(virtualServers, 0).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs0Ip));
    assertThat(
        getVirtualServerIpsByHaGroup(virtualServers, 1).collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs1EnabledIp));
  }

  @Test
  public void testGetVirtualServerKernelRoutes() {
    Ip vs0Ip = Ip.parse("10.0.0.1");
    Ip vs1EnabledIp = Ip.parse("10.0.1.1");
    Ip vs1DisabledIp = Ip.parse("10.0.3.1");
    VirtualServer vs0 = new VirtualServer("vs0", new VirtualServerTargetAddress(vs0Ip));
    vs0.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    VirtualServer vs1Enabled =
        new VirtualServer("vs1Enabled", new VirtualServerTargetAddress(vs1EnabledIp));
    vs1Enabled.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1Enabled.setVrid(1);
    VirtualServer vs1EnabledIpv6 =
        new VirtualServer(
            "vs1EnabledIpv6", new VirtualServerTargetAddress6(Ip6.parse("dead:beef::1")));
    vs1EnabledIpv6.getOrCreatePort(22, VirtualServerPort.Type.TCP, null);
    vs1EnabledIpv6.setVrid(1);
    VirtualServer vs1Disabled =
        new VirtualServer("vs1Disabled", new VirtualServerTargetAddress(vs1DisabledIp));
    vs1Disabled.setVrid(1);
    vs1Disabled.setEnable(false);
    List<VirtualServer> virtualServers =
        ImmutableList.of(vs0, vs1Enabled, vs1EnabledIpv6, vs1Disabled);

    assertThat(
        getVirtualServerKernelRoutes(virtualServers)
            .map(KernelRoute::getNetwork)
            .map(Prefix::getStartIp)
            .collect(ImmutableSet.toImmutableSet()),
        containsInAnyOrder(vs0Ip, vs1EnabledIp));
  }

  @Test
  public void testToVrrpGroup() {
    Ip ip = Ip.parse("1.1.1.1");
    ConcreteInterfaceAddress sourceAddress = ConcreteInterfaceAddress.parse("10.0.0.1/30");
    // null vrid config (must be vrid 0)
    assertThat(
        toVrrpGroup(
            null, sourceAddress, ImmutableSet.of(ip), ImmutableList.of("foo"), ImmutableMap.of()),
        equalTo(
            VrrpGroup.builder()
                .setPreempt(DEFAULT_VRRP_A_PREEMPT)
                .setPriority(DEFAULT_VRRP_A_PRIORITY)
                .setSourceAddress(sourceAddress)
                .setVirtualAddresses("foo", ImmutableSet.of(ip))
                .build()));

    // non-null vrid config
    VrrpAVrid vridConfig = new VrrpAVrid();
    vridConfig.setPreemptModeDisable(true);
    vridConfig.getOrCreateBladeParameters().setPriority(5);
    vridConfig.getOrCreateBladeParameters().setFailOverPolicyTemplate("template");
    Map<String, Map<String, TrackAction>> trackActionsByTemplate =
        ImmutableMap.of("template", ImmutableMap.of("method", new DecrementPriority(1)));

    assertThat(
        toVrrpGroup(
            vridConfig,
            sourceAddress,
            ImmutableSet.of(ip),
            ImmutableList.of("foo"),
            trackActionsByTemplate),
        equalTo(
            VrrpGroup.builder()
                .setPreempt(false)
                .setPriority(5)
                .setSourceAddress(sourceAddress)
                .setVirtualAddresses("foo", ImmutableSet.of(ip))
                .setTrackActions(trackActionsByTemplate.get("template"))
                .build()));
  }

  @Test
  public void testVrrpADisabledAppliesToInterface() {
    org.batfish.datamodel.Interface.Builder ifaceBuilder =
        org.batfish.datamodel.TestInterface.builder().setName("placeholder");
    // No concrete address
    assertFalse(
        vrrpADisabledAppliesToInterface(
            ifaceBuilder.setType(InterfaceType.PHYSICAL).setAddress(null).build()));
    // Loopback interface
    assertFalse(
        vrrpADisabledAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.LOOPBACK)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build()));

    assertTrue(
        vrrpADisabledAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.PHYSICAL)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build()));
    assertTrue(
        vrrpADisabledAppliesToInterface(
            ifaceBuilder
                .setType(InterfaceType.AGGREGATED)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build()));
  }

  @Test
  public void testFindVrrpAEnabledSourceAddress() {
    org.batfish.datamodel.Interface.Builder ifaceBuilder =
        org.batfish.datamodel.TestInterface.builder().setName("placeholder");
    Set<Ip> peerIps = ImmutableSet.of(Ip.parse("10.10.10.11"));
    // No concrete address
    assertThat(
        findVrrpAEnabledSourceAddress(
            ifaceBuilder.setType(InterfaceType.PHYSICAL).setAddress(null).build(), peerIps),
        equalTo(Optional.empty()));
    // Loopback interface
    assertThat(
        findVrrpAEnabledSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.LOOPBACK)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build(),
            peerIps),
        equalTo(Optional.empty()));
    // subnet does not contain a peerIp
    assertThat(
        findVrrpAEnabledSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.AGGREGATED)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build(),
            peerIps),
        equalTo(Optional.empty()));

    assertThat(
        findVrrpAEnabledSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.PHYSICAL)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build(),
            peerIps),
        equalTo(Optional.of(ConcreteInterfaceAddress.parse("10.10.10.10/24"))));
  }

  @Test
  public void testFindHaSourceAddress() {
    org.batfish.datamodel.Interface.Builder ifaceBuilder =
        org.batfish.datamodel.TestInterface.builder().setName("placeholder");
    Ip connMirror = Ip.parse("10.10.10.11");
    // No concrete address
    assertThat(
        findHaSourceAddress(
            ifaceBuilder.setType(InterfaceType.PHYSICAL).setAddress(null).build(), connMirror),
        equalTo(Optional.empty()));
    // Loopback interface
    assertThat(
        findHaSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.LOOPBACK)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build(),
            connMirror),
        equalTo(Optional.empty()));
    // subnet does not contain a peerIp
    assertThat(
        findHaSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.AGGREGATED)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/32"))
                .build(),
            connMirror),
        equalTo(Optional.empty()));

    assertThat(
        findHaSourceAddress(
            ifaceBuilder
                .setType(InterfaceType.PHYSICAL)
                .setAddress(ConcreteInterfaceAddress.parse("10.10.10.10/24"))
                .build(),
            connMirror),
        equalTo(Optional.of(ConcreteInterfaceAddress.parse("10.10.10.10/24"))));
  }

  @Test
  public void testToKernelRouteVirtualServer() {
    Ip target = Ip.parse("10.0.0.1");
    {
      // unflagged
      VirtualServer vs = new VirtualServer("vs", new VirtualServerTargetAddress(target));
      assertThat(
          toKernelRoute(vs),
          equalTo(
              KernelRoute.builder()
                  .setNetwork(Prefix.strict("10.0.0.1/32"))
                  .setRequiredOwnedIp(target)
                  .setTag(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED)
                  .build()));
    }
    {
      // flagged
      VirtualServer vs = new VirtualServer("vs", new VirtualServerTargetAddress(target));
      vs.setRedistributionFlagged(true);
      assertThat(
          toKernelRoute(vs),
          equalTo(
              KernelRoute.builder()
                  .setNetwork(Prefix.strict("10.0.0.1/32"))
                  .setRequiredOwnedIp(target)
                  .setTag(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED)
                  .build()));
    }
  }

  @Test
  public void testToNonForwardingKernelRouteNatPool() {
    Ip start = Ip.parse("10.0.0.1");
    NatPool pool = new NatPool("pool1", start, Ip.parse("10.0.0.5"), 24);
    assertThat(
        A10Conversion.toKernelRoute(pool),
        equalTo(
            KernelRoute.builder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setRequiredOwnedIp(start)
                .setTag(KERNEL_ROUTE_TAG_NAT_POOL)
                .build()));
  }

  @Test
  public void testToForwardingKernelRoutesNatPool() {
    Ip start = Ip.parse("10.0.0.1");
    NatPool pool = new NatPool("pool1", start, Ip.parse("10.0.0.5"), 24);
    assertThat(
        A10Conversion.toKernelRoute(pool),
        equalTo(
            KernelRoute.builder()
                .setNetwork(Prefix.strict("10.0.0.0/24"))
                .setRequiredOwnedIp(start)
                .setTag(KERNEL_ROUTE_TAG_NAT_POOL)
                .build()));
  }

  @Test
  public void testToKernelRouteFloatingIp() {
    Ip floatingIp = Ip.parse("10.0.0.1");
    assertThat(
        toKernelRoute(floatingIp),
        equalTo(
            KernelRoute.builder()
                .setNetwork(Prefix.strict("10.0.0.1/32"))
                .setRequiredOwnedIp(floatingIp)
                .setTag(KERNEL_ROUTE_TAG_FLOATING_IP)
                .build()));
  }

  @Test
  public void testCreateBgpProcess() {
    Ip routerId = Ip.parse("10.1.1.1");
    long remoteAs = 6L;
    BgpProcess bgpProcess = new BgpProcess(remoteAs);
    Ip peerIp = Ip.parse("10.2.2.2");
    BgpNeighborId neighborId = new BgpNeighborIdAddress(peerIp);
    BgpNeighbor neighbor = bgpProcess.getOrCreateNeighbor(neighborId);
    neighbor.setRemoteAs(10L);
    neighbor.setUpdateSource(new BgpNeighborUpdateSourceAddress(routerId));

    Configuration c = testConfig();

    String redistName = generatedBgpRedistributionPolicyName(DEFAULT_VRF_NAME);
    Prefix network = Prefix.ZERO;
    ConnectedRoute connected = new ConnectedRoute(network, "foo");
    KernelRoute floatingIp =
        KernelRoute.builder().setNetwork(network).setTag(KERNEL_ROUTE_TAG_FLOATING_IP).build();
    KernelRoute natPool =
        KernelRoute.builder().setNetwork(network).setTag(KERNEL_ROUTE_TAG_NAT_POOL).build();
    KernelRoute vipUnflagged =
        KernelRoute.builder()
            .setNetwork(network)
            .setTag(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED)
            .build();
    KernelRoute vipFlagged =
        KernelRoute.builder()
            .setNetwork(network)
            .setTag(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED)
            .build();

    // Note: test clauses may not be re-ordered
    {
      // no process without routerId
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);

      assertNull(c.getDefaultVrf().getBgpProcess());
      assertThat(
          w,
          hasRedFlag(
              hasText(
                  "Converting a BgpProcess without an explicit router-id is currently"
                      + " unsupported")));
    }
    bgpProcess.setRouterId(routerId);
    {
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      org.batfish.datamodel.BgpProcess newBgpProcess = c.getDefaultVrf().getBgpProcess();

      // no multipath, check common properties
      assertThat(
          newBgpProcess,
          allOf(hasRouterId(routerId), hasMultipathEbgp(false), hasMultipathIbgp(false)));
      assertThat(newBgpProcess.getEbgpAdminCost(), equalTo(DEFAULT_EBGP_ADMIN_COST));
      assertThat(newBgpProcess.getIbgpAdminCost(), equalTo(DEFAULT_IBGP_ADMIN_COST));
      assertThat(newBgpProcess.getLocalAdminCost(), equalTo(DEFAULT_LOCAL_ADMIN_COST));
      assertThat(newBgpProcess.getMultipathEquivalentAsPathMatchMode(), equalTo(EXACT_PATH));
      assertThat(newBgpProcess.getTieBreaker(), equalTo(ROUTER_ID));
      assertThat(newBgpProcess.getRedistributionPolicy(), equalTo(redistName));
      assertThat(w.getRedFlagWarnings(), empty());

      // check that neighbor is converted
      assertThat(newBgpProcess.getActiveNeighbors(), hasKey(peerIp));

      // nothing should be redistributed
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);
      assertFalse(redist.processReadOnly(connected));
      assertFalse(redist.processReadOnly(floatingIp));
      assertFalse(redist.processReadOnly(natPool));
      assertFalse(redist.processReadOnly(vipFlagged));
      assertFalse(redist.processReadOnly(vipUnflagged));
    }
    bgpProcess.setMaximumPaths(2);
    {
      // multipath
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      org.batfish.datamodel.BgpProcess newBgpProcess = c.getDefaultVrf().getBgpProcess();

      assertThat(
          newBgpProcess,
          allOf(hasRouterId(routerId), hasMultipathEbgp(true), hasMultipathIbgp(true)));
      assertThat(w.getRedFlagWarnings(), empty());
    }
    {
      // redistribute connected
      bgpProcess.setRedistributeConnected(true);
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);

      assertTrue(redist.processReadOnly(connected));
      assertFalse(redist.processReadOnly(floatingIp));
      assertFalse(redist.processReadOnly(natPool));
      assertFalse(redist.processReadOnly(vipFlagged));
      assertFalse(redist.processReadOnly(vipUnflagged));
      assertThat(w.getRedFlagWarnings(), empty());

      // reset
      bgpProcess.setRedistributeConnected(false);
    }
    {
      // redistribute floating-ip
      bgpProcess.setRedistributeFloatingIp(true);
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);

      assertFalse(redist.processReadOnly(connected));
      assertTrue(redist.processReadOnly(floatingIp));
      assertFalse(redist.processReadOnly(natPool));
      assertFalse(redist.processReadOnly(vipFlagged));
      assertFalse(redist.processReadOnly(vipUnflagged));
      assertThat(w.getRedFlagWarnings(), empty());

      // reset
      bgpProcess.setRedistributeFloatingIp(false);
    }
    {
      // redistribute ip nat pool
      bgpProcess.setRedistributeIpNat(true);
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);

      assertFalse(redist.processReadOnly(connected));
      assertFalse(redist.processReadOnly(floatingIp));
      assertTrue(redist.processReadOnly(natPool));
      assertFalse(redist.processReadOnly(vipFlagged));
      assertFalse(redist.processReadOnly(vipUnflagged));
      assertThat(w.getRedFlagWarnings(), empty());

      // reset
      bgpProcess.setRedistributeIpNat(false);
    }
    {
      // redistribute vip flagged
      bgpProcess.setRedistributeVipOnlyFlagged(true);
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);

      assertFalse(redist.processReadOnly(connected));
      assertFalse(redist.processReadOnly(floatingIp));
      assertFalse(redist.processReadOnly(natPool));
      assertTrue(redist.processReadOnly(vipFlagged));
      assertFalse(redist.processReadOnly(vipUnflagged));
      assertThat(w.getRedFlagWarnings(), empty());

      // reset
      bgpProcess.setRedistributeVipOnlyFlagged(false);
    }
    {
      // redistribute vip unflagged
      bgpProcess.setRedistributeVipOnlyNotFlagged(true);
      Warnings w = new Warnings(false, true, true);
      createBgpProcess(bgpProcess, c, w);
      RoutingPolicy redist = c.getRoutingPolicies().get(redistName);

      assertFalse(redist.processReadOnly(connected));
      assertFalse(redist.processReadOnly(floatingIp));
      assertFalse(redist.processReadOnly(natPool));
      assertFalse(redist.processReadOnly(vipFlagged));
      assertTrue(redist.processReadOnly(vipUnflagged));
      assertThat(w.getRedFlagWarnings(), empty());

      // reset
      bgpProcess.setRedistributeVipOnlyNotFlagged(false);
    }
  }

  private @Nonnull Configuration testConfig() {
    Configuration c =
        Configuration.builder()
            .setHostname("foo")
            .setConfigurationFormat(ConfigurationFormat.A10_ACOS)
            .build();
    Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(c).build();
    return c;
  }

  @Test
  public void testCreateAndAttachBgpNeighbor() {
    Ip routerId = Ip.parse("10.1.1.1");
    Ip localIp = Ip.parse("10.0.0.1");
    Ip remoteIp = Ip.parse("10.0.0.2");
    String description = "desc";
    BgpNeighborId id = new BgpNeighborIdAddress(remoteIp);
    long defaultLocalAs = 5L;
    long remoteAs = 6L;
    BgpNeighbor bgpNeighbor = new BgpNeighbor(id);
    bgpNeighbor.setUpdateSource(new BgpNeighborUpdateSourceAddress(localIp));
    bgpNeighbor.setDescription(description);
    bgpNeighbor.setSendCommunity(SendCommunity.BOTH);
    bgpNeighbor.setRemoteAs(remoteAs);
    org.batfish.datamodel.BgpProcess newBgpProcess =
        org.batfish.datamodel.BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(1)
            .setIbgpAdminCost(2)
            .setLocalAdminCost(3)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .build();
    Configuration c = testConfig();
    {
      Warnings w = new Warnings(false, true, true);
      createAndAttachBgpNeighbor(id, defaultLocalAs, bgpNeighbor, newBgpProcess, c, w);

      assertThat(
          newBgpProcess,
          hasActiveNeighbor(
              remoteIp,
              allOf(
                  hasLocalIp(localIp),
                  hasLocalAs(defaultLocalAs),
                  hasRemoteAs(remoteAs),
                  hasDescription(description),
                  hasIpv4UnicastAddressFamily(
                      allOf(
                          hasAddressFamilyCapabilites(
                              allOf(hasSendCommunity(true), hasSendExtendedCommunity(true))),
                          hasExportPolicy(
                              Names.generatedBgpPeerExportPolicyName(
                                  DEFAULT_VRF_NAME, remoteIp.toString())))))));
    }
  }

  @Test
  public void testComputeUpdateSource() {
    String ifaceName = "eth0";
    Ip interfaceAddress = Ip.parse("10.0.0.1");
    Map<String, org.batfish.datamodel.Interface> interfaces =
        ImmutableMap.of(
            ifaceName,
            org.batfish.datamodel.TestInterface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setAddress(ConcreteInterfaceAddress.create(interfaceAddress, 24))
                .build());
    Ip remoteIp = Ip.parse("10.0.0.2");
    {
      // explicit update-source
      Warnings warnings = new Warnings(false, true, true);
      Ip updateSource = Ip.parse("5.5.5.5");
      assertThat(
          computeUpdateSource(
              interfaces, remoteIp, new BgpNeighborUpdateSourceAddress(updateSource), warnings),
          equalTo(updateSource));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // implicit update-source (use interface)
      Warnings warnings = new Warnings(false, true, true);
      assertThat(
          computeUpdateSource(interfaces, remoteIp, null, warnings), equalTo(interfaceAddress));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // no matching interface, no explicit update source
      Warnings warnings = new Warnings(false, true, true);
      assertNull(computeUpdateSource(ImmutableMap.of(), remoteIp, null, warnings));
    }
  }

  @Test
  public void testGetInterfaceEnabledEffective() {
    Interface ethNullEnabled = new Interface(Interface.Type.ETHERNET, 1);
    Interface loopNullEnabled = new Interface(Interface.Type.LOOPBACK, 1);

    // Defaults
    // Ethernet default is determined by ACOS major version number
    assertTrue(getInterfaceEnabledEffective(ethNullEnabled, null));
    assertTrue(getInterfaceEnabledEffective(ethNullEnabled, 2));
    assertFalse(getInterfaceEnabledEffective(ethNullEnabled, 4));
    // Loopback is enabled by default, regardless of version number
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled, null));
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled, 2));
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled, 4));

    // Explicit enabled value set
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);
    eth.setEnabled(true);
    assertTrue(getInterfaceEnabledEffective(eth, null));
    eth.setEnabled(false);
    assertFalse(getInterfaceEnabledEffective(eth, null));
  }
}
