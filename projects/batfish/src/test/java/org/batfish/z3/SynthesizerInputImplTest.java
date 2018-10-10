package org.batfish.z3;

import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessLists;
import static org.batfish.z3.expr.visitors.TransformationExprTransformer.transformedSuffixMatchExpr;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasAclActions;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasAclConditions;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasArpTrueEdge;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEgressDstNats;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEgressSrcNats;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledEdges;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledInterfaces;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledNodes;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledVrfs;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasIngressDstNats;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasIngressSrcNats;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasIpsByHostname;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasNeighborUnreachable;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasTopologyInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessList.Builder;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MockForwardingAnalysis;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation.RuleAction;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.visitors.IpSpaceBooleanExprTransformer;
import org.batfish.z3.state.AclPermit;
import org.junit.Before;
import org.junit.Test;

public class SynthesizerInputImplTest {

  private IpAccessList.Builder _aclb;

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private SynthesizerInputImpl.Builder _inputBuilder;

  private NetworkFactory _nf;

  private DynamicNatRule.Builder _dtb;

  private StaticNatRule.Builder _stb;

  private Vrf.Builder _vb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder();
    _ib = _nf.interfaceBuilder().setBandwidth(1E9d);
    _aclb = _nf.aclBuilder();
    _inputBuilder = SynthesizerInputImpl.builder();
    _dtb = org.batfish.datamodel.transformation.DynamicNatRule.builder();
    _stb = org.batfish.datamodel.transformation.StaticNatRule.builder();
  }

  @Test
  public void testComputeAclActions() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    IpAccessList edgeInterfaceInAcl =
        _aclb
            .setOwner(srcNode)
            .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL, IpAccessListLine.REJECT_ALL))
            .build();
    IpAccessList srcInterfaceOutAcl = _aclb.build();
    IpAccessList iNoEdgeInAcl = _aclb.build();
    IpAccessList iNoEdgeOutAcl = _aclb.build();
    IpAccessList nextHopInterfaceInAcl = _aclb.setOwner(nextHop).build();
    IpAccessList nextHopInterfaceOutAcl = _aclb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Interface srcInterface =
        _ib.setOwner(srcNode)
            .setVrf(srcVrf)
            .setIncomingFilter(edgeInterfaceInAcl)
            .setOutgoingFilter(srcInterfaceOutAcl)
            .build();

    _ib.setIncomingFilter(iNoEdgeInAcl).setOutgoingFilter(iNoEdgeOutAcl).build();
    Interface nextHopInterface =
        _ib.setIncomingFilter(nextHopInterfaceInAcl)
            .setOutgoingFilter(nextHopInterfaceOutAcl)
            .setOwner(nextHop)
            .setVrf(nextHopVrf)
            .build();
    Edge forwardEdge = new Edge(srcInterface, nextHopInterface);
    Edge backEdge = new Edge(nextHopInterface, srcInterface);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(new Topology(ImmutableSortedSet.of(forwardEdge, backEdge)))
            .build();
    List<LineAction> expectedActions = ImmutableList.of(LineAction.PERMIT, LineAction.DENY);
    Map<String, List<LineAction>> expectedSrcNodeWithDataPlane =
        ImmutableMap.of(
            edgeInterfaceInAcl.getName(),
            expectedActions,
            srcInterfaceOutAcl.getName(),
            expectedActions);
    Map<String, List<LineAction>> expectedSrcNodeWithoutDataPlane =
        ImmutableMap.<String, List<LineAction>>builder()
            .putAll(expectedSrcNodeWithDataPlane)
            .put(iNoEdgeInAcl.getName(), expectedActions)
            .put(iNoEdgeOutAcl.getName(), expectedActions)
            .build();
    Map<String, List<LineAction>> expectedNextHop =
        ImmutableMap.of(
            nextHopInterfaceInAcl.getName(),
            expectedActions,
            nextHopInterfaceOutAcl.getName(),
            expectedActions);

    assertThat(
        inputWithDataPlane,
        hasAclActions(
            equalTo(
                ImmutableMap.of(
                    srcNode.getHostname(),
                    expectedSrcNodeWithoutDataPlane,
                    nextHop.getHostname(),
                    expectedNextHop))));
    assertThat(
        inputWithoutDataPlane,
        hasAclActions(
            equalTo(
                ImmutableMap.of(
                    srcNode.getHostname(),
                    expectedSrcNodeWithoutDataPlane,
                    nextHop.getHostname(),
                    expectedNextHop))));
  }

  @Test
  public void testComputeAclConditions() {
    Configuration c = _cb.build();
    IpAccessList aclWithoutLines = _aclb.setOwner(c).build();
    IpAccessList aclWithLines =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(ImmutableSet.of(new IpWildcard(new Ip("1.2.3.4"))))
                            .build()),
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstIps(ImmutableSet.of(new IpWildcard(new Ip("5.6.7.8"))))
                            .build())))
            .build();
    Vrf vrf = _vb.setOwner(c).build();
    _ib.setOwner(c).setVrf(vrf).setOutgoingFilter(aclWithoutLines).build();
    _ib.setOutgoingFilter(aclWithLines).build();
    _ib.setOutgoingFilter(null);

    SynthesizerInput input =
        _inputBuilder.setConfigurations(ImmutableMap.of(c.getHostname(), c)).build();
    AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr =
        new AclLineMatchExprToBooleanExpr(
            ImmutableMap.of(), ImmutableMap.of(), null, ImmutableMap.of());

    assertThat(
        input,
        hasAclConditions(
            equalTo(
                ImmutableMap.of(
                    c.getHostname(),
                    ImmutableMap.of(
                        aclWithoutLines.getName(),
                        ImmutableList.of(),
                        aclWithLines.getName(),
                        ImmutableList.of(
                            aclLineMatchExprToBooleanExpr.toBooleanExpr(
                                aclWithLines.getLines().get(0).getMatchCondition()),
                            aclLineMatchExprToBooleanExpr.toBooleanExpr(
                                aclWithLines.getLines().get(1).getMatchCondition())))))));

    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Ip ip11 = new Ip("1.0.0.0");
    Ip ip12 = new Ip("1.0.0.10");
    Ip ip21 = new Ip("2.0.0.0");
    Ip ip22 = new Ip("2.0.0.10");
    IpAccessList sourceNat1Acl = _aclb.setLines(ImmutableList.of()).setOwner(srcNode).build();
    IpAccessList sourceNat2Acl = _aclb.build();
    _dtb.setAction(RuleAction.SOURCE_INSIDE);
    DynamicNatRule sourceNat1 =
        _dtb.setPoolIpFirst(ip11).setPoolIpLast(ip12).setAcl(sourceNat1Acl).build();
    assertThat(sourceNat1, notNullValue());
    DynamicNatRule sourceNat2 =
        _dtb.setPoolIpFirst(ip21).setPoolIpLast(ip22).setAcl(sourceNat2Acl).build();
    assertThat(sourceNat2, notNullValue());
    Interface srcInterfaceZeroSourceNats =
        _ib.setOwner(srcNode).setVrf(srcVrf).setEgressNats(ImmutableList.of()).build();
    Interface srcInterfaceOneSourceNat = _ib.setEgressNats(ImmutableList.of(sourceNat1)).build();
    Interface srcInterfaceTwoSourceNats =
        _ib.setEgressNats(ImmutableList.of(sourceNat1, sourceNat2)).build();
    Interface nextHopInterface =
        _ib.setOwner(nextHop).setVrf(nextHopVrf).setEgressNats(ImmutableList.of()).build();
    Edge forwardEdge1 = new Edge(srcInterfaceZeroSourceNats, nextHopInterface);
    Edge forwardEdge2 = new Edge(srcInterfaceOneSourceNat, nextHopInterface);
    Edge forwardEdge3 = new Edge(srcInterfaceTwoSourceNats, nextHopInterface);
    Edge backEdge1 = new Edge(nextHopInterface, srcInterfaceZeroSourceNats);
    Edge backEdge2 = new Edge(nextHopInterface, srcInterfaceOneSourceNat);
    Edge backEdge3 = new Edge(nextHopInterface, srcInterfaceTwoSourceNats);
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(
                new Topology(
                    ImmutableSortedSet.of(
                        forwardEdge1, forwardEdge2, forwardEdge3, backEdge1, backEdge2, backEdge3)))
            .build();
    assertThat(
        inputWithDataPlane,
        hasAclConditions(
            equalTo(
                ImmutableMap.of(
                    srcNode.getHostname(),
                    ImmutableMap.of(
                        sourceNat1Acl.getName(), ImmutableList.of(),
                        sourceNat2Acl.getName(), ImmutableList.of()),
                    nextHop.getHostname(),
                    ImmutableMap.of()))));
  }

  @Test
  public void testComputeArpTrueEdge() {
    Configuration srcNode = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Interface srcInterface = _ib.setOwner(srcNode).setVrf(srcVrf).build();
    String nextHop1 = "nextHop1";
    String nextHopInterface1 = "nextHopInterface1";
    String nextHop2 = "nextHop2";
    String nextHopInterface2 = "nextHopInterface2";
    IpSpace ipSpace1 = Ip.ZERO.toIpSpace();
    IpSpace ipSpace2 = Ip.MAX.toIpSpace();
    BooleanExpr m1 =
        ipSpace1.accept(new IpSpaceBooleanExprTransformer(ImmutableMap.of(), Field.DST_IP));
    BooleanExpr m2 =
        ipSpace2.accept(new IpSpaceBooleanExprTransformer(ImmutableMap.of(), Field.DST_IP));
    Edge edge1 =
        Edge.of(srcNode.getHostname(), srcInterface.getName(), nextHop1, nextHopInterface1);
    Edge edge2 =
        Edge.of(srcNode.getHostname(), srcInterface.getName(), nextHop2, nextHopInterface2);

    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder.setConfigurations(ImmutableMap.of(srcNode.getHostname(), srcNode)).build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(
                MockForwardingAnalysis.builder()
                    .setArpTrueEdge(ImmutableMap.of(edge1, ipSpace1, edge2, ipSpace2))
                    .build())
            .setTopology(new Topology(ImmutableSortedSet.of(edge1, edge2)))
            .build();

    assertThat(inputWithoutDataPlane, hasArpTrueEdge(nullValue()));
    assertThat(
        inputWithDataPlane,
        hasArpTrueEdge(
            equalTo(
                ImmutableMap.of(
                    srcNode.getHostname(),
                    ImmutableMap.of(
                        srcVrf.getName(),
                        ImmutableMap.of(
                            srcInterface.getName(),
                            ImmutableMap.of(
                                nextHop1,
                                ImmutableMap.of(nextHopInterface1, m1),
                                nextHop2,
                                ImmutableMap.of(nextHopInterface2, m2))))))));
  }

  @Test
  public void testComputeEnabledEdges() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Interface srcInterface = _ib.setOwner(srcNode).setVrf(srcVrf).build();
    Interface nextHopInterface = _ib.setOwner(nextHop).setVrf(nextHopVrf).build();
    Interface disabledNextHopInterface = _ib.setActive(false).build();
    Edge expectedEnabledEdge = new Edge(srcInterface, nextHopInterface);
    Edge expectedDisabledEdge = new Edge(srcInterface, disabledNextHopInterface);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(
                new Topology(ImmutableSortedSet.of(expectedEnabledEdge, expectedDisabledEdge)))
            .build();

    assertThat(inputWithDataPlane, hasEnabledEdges(hasItem(expectedEnabledEdge)));
    assertThat(inputWithDataPlane, hasEnabledEdges(not(hasItem(expectedDisabledEdge))));
    assertThat(inputWithDataPlane, hasEnabledEdges(not(hasItem(expectedDisabledEdge))));
    assertThat(inputWithoutDataPlane, hasEnabledEdges(nullValue()));
  }

  @Test
  public void testComputeEnabledInterfaces() {
    Configuration cEnabled = _cb.build();
    Configuration cDisabled = _cb.build();
    Vrf vEnabled = _vb.setOwner(cEnabled).build();
    Vrf vDisabledViaVrf = _vb.build();
    Vrf vDisabledViaNode = _vb.setOwner(cDisabled).build();
    Interface iEnabled = _ib.setOwner(cEnabled).setVrf(vEnabled).build();
    Interface iDisabledViaInactive = _ib.setActive(false).build();
    Interface iDisabledViaBlacklisted = _ib.setActive(true).setBlacklisted(true).build();
    Interface iDisabledViaInterface = _ib.setBlacklisted(false).build();
    Interface iDisabledViaVrf = _ib.setVrf(vDisabledViaVrf).build();
    // interface disabled via disabledNodes
    _ib.setOwner(cDisabled).setVrf(vDisabledViaNode).build();
    SynthesizerInput input =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(
                    cEnabled.getHostname(), cEnabled, cDisabled.getHostname(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getHostname()))
            .setDisabledVrfs(
                ImmutableMap.of(cEnabled.getHostname(), ImmutableSet.of(vDisabledViaVrf.getName())))
            .setDisabledInterfaces(
                ImmutableMap.of(
                    cEnabled.getHostname(), ImmutableSet.of(iDisabledViaInterface.getName())))
            .build();

    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getHostname()), hasItem(iEnabled.getName()))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(cEnabled.getHostname()), not(hasItem(iDisabledViaInactive.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(cEnabled.getHostname()), not(hasItem(iDisabledViaBlacklisted.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(cEnabled.getHostname()), not(hasItem(iDisabledViaInterface.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getHostname()), not(hasItem(iDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledInterfaces(not(hasKey(cDisabled.getHostname()))));
  }

  @Test
  public void testComputeEnabledNodes() {
    Configuration cEnabled = _cb.build();
    Configuration cDisabled = _cb.build();
    SynthesizerInput input =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(
                    cEnabled.getHostname(), cEnabled, cDisabled.getHostname(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getHostname()))
            .build();

    assertThat(input, hasEnabledNodes(hasItem(cEnabled.getHostname())));
    assertThat(input, hasEnabledNodes(not(hasItem(cDisabled.getHostname()))));
  }

  @Test
  public void testComputeEnabledVrfs() {
    Configuration cEnabled = _cb.build();
    Configuration cDisabled = _cb.build();
    Vrf vEnabled = _vb.setOwner(cEnabled).build();
    Vrf vDisabledViaVrf = _vb.build();
    // disabled via disabledNodes
    _vb.setOwner(cDisabled).build();
    SynthesizerInput input =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(
                    cEnabled.getHostname(), cEnabled, cDisabled.getHostname(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getHostname()))
            .setDisabledVrfs(
                ImmutableMap.of(cEnabled.getHostname(), ImmutableSet.of(vDisabledViaVrf.getName())))
            .build();

    assertThat(
        input,
        hasEnabledVrfs(hasEntry(equalTo(cEnabled.getHostname()), hasItem(vEnabled.getName()))));
    assertThat(
        input,
        hasEnabledVrfs(
            hasEntry(equalTo(cEnabled.getHostname()), not(hasItem(vDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledVrfs(not(hasKey(cDisabled.getHostname()))));
  }

  @Test
  public void testComputeIpsByHostname() {
    Configuration c = _cb.build();
    Vrf v = _vb.setOwner(c).build();
    // Enabled but not flow sink. Should not appear in enabledFlowSinks.
    Ip ipEnabled1 = new Ip("1.1.1.1");
    Ip ipEnabled2 = new Ip("2.2.2.2");
    Ip ipDisabled = new Ip("3.3.3.3");
    // enabledInterface1
    _ib.setOwner(c)
        .setVrf(v)
        .setAddress(new InterfaceAddress(ipEnabled1, Prefix.MAX_PREFIX_LENGTH))
        .build();
    // enabledInterface1
    _ib.setAddress(new InterfaceAddress(ipEnabled2, Prefix.MAX_PREFIX_LENGTH)).build();
    // disabledInterface
    _ib.setAddress(new InterfaceAddress(ipDisabled, Prefix.MAX_PREFIX_LENGTH))
        .setActive(false)
        .build();
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder.setConfigurations(ImmutableMap.of(c.getHostname(), c)).build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(new Topology(ImmutableSortedSet.of()))
            .build();

    assertThat(inputWithoutDataPlane, hasIpsByHostname(nullValue()));
    assertThat(
        inputWithDataPlane,
        hasIpsByHostname(
            equalTo(ImmutableMap.of(c.getHostname(), ImmutableSet.of(ipEnabled1, ipEnabled2)))));
  }

  /** Hosts that own no IPs should be assigned an empty set by computeIpsByHostname */
  @Test
  public void testComputeIpsByHostname_noIps() {
    Configuration c = _cb.build();
    Vrf v = _vb.setOwner(c).build();
    _ib.setOwner(c).setVrf(v).build();

    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setConfigurations(ImmutableMap.of(c.getHostname(), c))
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(new Topology(ImmutableSortedSet.of()))
            .build();
    assertThat(
        inputWithDataPlane,
        hasIpsByHostname(equalTo(ImmutableMap.of(c.getHostname(), ImmutableSet.of()))));
  }

  @Test
  public void testComputeNeighborUnreachable() {
    Configuration node = _cb.build();
    Vrf vrf = _vb.setOwner(node).build();
    Interface iface1 = _ib.setOwner(node).setVrf(vrf).build();
    Interface iface2 = _ib.build();
    IpSpace srcIpSpace = Ip.ZERO.toIpSpace();
    IpSpace dstIpSpace = Ip.MAX.toIpSpace();
    BooleanExpr m1 =
        srcIpSpace.accept(new IpSpaceBooleanExprTransformer(ImmutableMap.of(), Field.DST_IP));
    BooleanExpr m2 =
        dstIpSpace.accept(new IpSpaceBooleanExprTransformer(ImmutableMap.of(), Field.DST_IP));

    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder.setConfigurations(ImmutableMap.of(node.getHostname(), node)).build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(
                MockForwardingAnalysis.builder()
                    .setNeighborUnreachableOrExitsNetwork(
                        ImmutableMap.of(
                            node.getHostname(),
                            ImmutableMap.of(
                                vrf.getName(),
                                ImmutableMap.of(
                                    iface1.getName(), srcIpSpace, iface2.getName(), dstIpSpace))))
                    .build())
            .setTopology(new Topology(ImmutableSortedSet.of()))
            .build();

    assertThat(inputWithoutDataPlane, hasNeighborUnreachable(nullValue()));
    assertThat(
        inputWithDataPlane,
        hasNeighborUnreachable(
            equalTo(
                ImmutableMap.of(
                    node.getHostname(),
                    ImmutableMap.of(
                        vrf.getName(),
                        ImmutableMap.of(iface1.getName(), m1, iface2.getName(), m2))))));
  }

  @Test
  public void testComputeDynamicSourceNats() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Ip ip11 = new Ip("1.0.0.0");
    Ip ip12 = new Ip("1.0.0.10");
    Ip ip21 = new Ip("2.0.0.0");
    Ip ip22 = new Ip("2.0.0.10");
    IpAccessList sourceNat1Acl = _aclb.setLines(ImmutableList.of()).setOwner(srcNode).build();
    IpAccessList sourceNat2Acl = _aclb.build();
    _dtb.setAction(RuleAction.SOURCE_INSIDE);
    DynamicNatRule sourceNat1 =
        _dtb.setPoolIpFirst(ip11).setPoolIpLast(ip12).setAcl(sourceNat1Acl).build();
    assertThat(sourceNat1, notNullValue());
    DynamicNatRule sourceNat2 =
        _dtb.setPoolIpFirst(ip21).setPoolIpLast(ip22).setAcl(sourceNat2Acl).build();
    assertThat(sourceNat2, notNullValue());
    Interface srcInterfaceZeroSourceNats =
        _ib.setOwner(srcNode).setVrf(srcVrf).setEgressNats(ImmutableList.of()).build();
    Interface srcInterfaceOneSourceNat = _ib.setEgressNats(ImmutableList.of(sourceNat1)).build();
    Interface srcInterfaceTwoSourceNats =
        _ib.setEgressNats(ImmutableList.of(sourceNat1, sourceNat2)).build();
    Interface nextHopInterface =
        _ib.setOwner(nextHop).setVrf(nextHopVrf).setEgressNats(ImmutableList.of()).build();
    Edge forwardEdge1 = new Edge(srcInterfaceZeroSourceNats, nextHopInterface);
    Edge forwardEdge2 = new Edge(srcInterfaceOneSourceNat, nextHopInterface);
    Edge forwardEdge3 = new Edge(srcInterfaceTwoSourceNats, nextHopInterface);
    Edge backEdge1 = new Edge(nextHopInterface, srcInterfaceZeroSourceNats);
    Edge backEdge2 = new Edge(nextHopInterface, srcInterfaceOneSourceNat);
    Edge backEdge3 = new Edge(nextHopInterface, srcInterfaceTwoSourceNats);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(
                new Topology(
                    ImmutableSortedSet.of(
                        forwardEdge1, forwardEdge2, forwardEdge3, backEdge1, backEdge2, backEdge3)))
            .build();

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceZeroSourceNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceZeroSourceNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceZeroSourceNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceZeroSourceNats.getName()), equalTo(ImmutableList.of())))));

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneSourceNat.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneSourceNat.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), sourceNat1Acl.getName()),
                                new RangeMatchExpr(
                                    new TransformedVarIntExpr(Field.SRC_IP),
                                    Field.SRC_IP.getSize(),
                                    ImmutableSet.of(
                                        Range.closed(ip11.asLong(), ip12.asLong()))))))))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneSourceNat.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneSourceNat.getName()), equalTo(ImmutableList.of())))));

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoSourceNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoSourceNats.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), sourceNat1Acl.getName()),
                                new RangeMatchExpr(
                                    new TransformedVarIntExpr(Field.SRC_IP),
                                    Field.SRC_IP.getSize(),
                                    ImmutableSet.of(Range.closed(ip11.asLong(), ip12.asLong())))),
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), sourceNat2Acl.getName()),
                                new RangeMatchExpr(
                                    new TransformedVarIntExpr(Field.SRC_IP),
                                    Field.SRC_IP.getSize(),
                                    ImmutableSet.of(
                                        Range.closed(ip21.asLong(), ip22.asLong()))))))))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoSourceNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoSourceNats.getName()), equalTo(ImmutableList.of())))));

    assertThat(inputWithoutDataPlane, hasEgressDstNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasEgressSrcNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasIngressDstNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasIngressSrcNats(nullValue()));
  }

  @Test
  public void testComputeStaticNats() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Prefix nat1Local = new Prefix(new Ip("1.0.0.0"), 24);
    Prefix nat1Global = new Prefix(new Ip("2.0.0.0"), 24);
    Prefix nat2Local = new Prefix(new Ip("3.0.0.1"), 32);
    Prefix nat2Global = new Prefix(new Ip("4.0.0.1"), 32);
    String insideInterface = "inside0";
    _aclb.setOwner(srcNode);

    // NAT 1 is SOURCE_INSIDE
    ImmutableList.Builder<AclLineMatchExpr> nat1EgressAclExpr = new ImmutableList.Builder<>();
    nat1EgressAclExpr.add(new MatchSrcInterface(Collections.singleton(insideInterface)));
    nat1EgressAclExpr.add(
        new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(nat1Local.toIpSpace()).build()));

    ImmutableList.Builder<AclLineMatchExpr> nat1IngressAclExpr = new ImmutableList.Builder<>();
    nat1IngressAclExpr.add(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(nat1Global.toIpSpace()).build()));

    // NAT 2 is SOURCE_OUTSIDE
    ImmutableList.Builder<AclLineMatchExpr> nat2EgressAclExpr = new ImmutableList.Builder<>();
    nat2EgressAclExpr.add(new MatchSrcInterface(Collections.singleton(insideInterface)));
    nat2EgressAclExpr.add(
        new MatchHeaderSpace(HeaderSpace.builder().setDstIps(nat2Local.toIpSpace()).build()));

    ImmutableList.Builder<AclLineMatchExpr> nat2IngressAclExpr = new ImmutableList.Builder<>();
    nat2IngressAclExpr.add(
        new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(nat2Global.toIpSpace()).build()));

    IpAccessList nat1IngressAcl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new AndMatchExpr(nat1IngressAclExpr.build()))
                        .build()))
            .build();
    IpAccessList nat1EgressAcl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new AndMatchExpr(nat1EgressAclExpr.build()))
                        .build()))
            .build();
    IpAccessList nat2IngressAcl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new AndMatchExpr(nat2IngressAclExpr.build()))
                        .build()))
            .build();
    IpAccessList nat2EgressAcl =
        _aclb
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new AndMatchExpr(nat2EgressAclExpr.build()))
                        .build()))
            .build();

    _stb.setAction(RuleAction.SOURCE_INSIDE);
    StaticNatRule nat1Ingress =
        _stb.setLocalNetwork(nat1Local).setGlobalNetwork(nat1Global).setAcl(nat1IngressAcl).build();
    assertThat(nat1Ingress, notNullValue());
    StaticNatRule nat1Egress =
        _stb.setLocalNetwork(nat1Local).setGlobalNetwork(nat1Global).setAcl(nat1EgressAcl).build();
    assertThat(nat1Egress, notNullValue());
    _stb.setAction(RuleAction.SOURCE_OUTSIDE);
    StaticNatRule nat2Ingress =
        _stb.setLocalNetwork(nat2Local).setGlobalNetwork(nat2Global).setAcl(nat2IngressAcl).build();
    assertThat(nat2Ingress, notNullValue());
    StaticNatRule nat2Egress =
        _stb.setLocalNetwork(nat2Local).setGlobalNetwork(nat2Global).setAcl(nat2EgressAcl).build();
    assertThat(nat2Egress, notNullValue());

    Interface srcInterfaceZeroNats =
        _ib.setOwner(srcNode).setVrf(srcVrf).setEgressNats(ImmutableList.of()).build();
    Interface srcInterfaceOneNat =
        _ib.setEgressNats(ImmutableList.of(nat1Egress))
            .setIngressNats(ImmutableList.of(nat1Ingress))
            .build();
    Interface srcInterfaceTwoNats =
        _ib.setEgressNats(ImmutableList.of(nat1Egress, nat2Egress))
            .setIngressNats(ImmutableList.of(nat1Ingress, nat2Ingress))
            .setName(insideInterface) // Need to make sure insideInterface exists
            .build();
    Interface nextHopInterface =
        _ib.setOwner(nextHop).setVrf(nextHopVrf).setEgressNats(ImmutableList.of()).build();

    Edge forwardEdge1 = new Edge(srcInterfaceZeroNats, nextHopInterface);
    Edge forwardEdge2 = new Edge(srcInterfaceOneNat, nextHopInterface);
    Edge forwardEdge3 = new Edge(srcInterfaceTwoNats, nextHopInterface);
    Edge backEdge1 = new Edge(nextHopInterface, srcInterfaceZeroNats);
    Edge backEdge2 = new Edge(nextHopInterface, srcInterfaceOneNat);
    Edge backEdge3 = new Edge(nextHopInterface, srcInterfaceTwoNats);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(
                new Topology(
                    ImmutableSortedSet.of(
                        forwardEdge1, forwardEdge2, forwardEdge3, backEdge1, backEdge2, backEdge3)))
            .build();

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceZeroNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceZeroNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceZeroNats.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceZeroNats.getName()), equalTo(ImmutableList.of())))));

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceOneNat.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneNat.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat1EgressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.SRC_IP), nat1Global),
                                        transformedSuffixMatchExpr(
                                            Field.SRC_IP, nat1Global.getPrefixLength()))))))))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(equalTo(srcInterfaceOneNat.getName()), equalTo(ImmutableList.of())))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneNat.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat1IngressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.DST_IP), nat1Local),
                                        transformedSuffixMatchExpr(
                                            Field.DST_IP, nat1Local.getPrefixLength()))))))))));

    assertThat(
        inputWithDataPlane,
        hasEgressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoNats.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat2EgressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.DST_IP), nat2Global),
                                        transformedSuffixMatchExpr(
                                            Field.DST_IP, nat2Global.getPrefixLength()))))))))));
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoNats.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat1EgressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.SRC_IP), nat1Global),
                                        transformedSuffixMatchExpr(
                                            Field.SRC_IP, nat1Global.getPrefixLength()))))))))));
    assertThat(
        inputWithDataPlane,
        hasIngressDstNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoNats.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat1IngressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.DST_IP), nat1Local),
                                        transformedSuffixMatchExpr(
                                            Field.DST_IP, nat1Local.getPrefixLength()))))))))));
    assertThat(
        inputWithDataPlane,
        hasIngressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceTwoNats.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                new AclPermit(srcNode.getHostname(), nat2IngressAcl.getName()),
                                new AndExpr(
                                    ImmutableList.of(
                                        new PrefixMatchExpr(
                                            new TransformedVarIntExpr(Field.SRC_IP), nat2Local),
                                        transformedSuffixMatchExpr(
                                            Field.SRC_IP, nat2Local.getPrefixLength()))))))))));

    assertThat(inputWithoutDataPlane, hasEgressDstNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasEgressSrcNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasIngressDstNats(nullValue()));
    assertThat(inputWithoutDataPlane, hasIngressSrcNats(nullValue()));
  }

  @Test
  public void testComputeTopologyInterfaces() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Interface srcInterface = _ib.setOwner(srcNode).setVrf(srcVrf).build();
    Interface iNoEdge = _ib.build();
    Interface nextHopInterface = _ib.setOwner(nextHop).setVrf(nextHopVrf).build();
    Edge forwardEdge = new Edge(srcInterface, nextHopInterface);
    Edge backEdge = new Edge(nextHopInterface, srcInterface);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(new Topology(ImmutableSortedSet.of(forwardEdge, backEdge)))
            .build();

    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(srcNode.getHostname()), hasItem(srcInterface.getName()))));
    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(srcNode.getHostname()), not(hasItem(iNoEdge.getName())))));
    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(nextHop.getHostname()), hasItem(nextHopInterface.getName()))));
    assertThat(inputWithoutDataPlane, hasTopologyInterfaces(nullValue()));
  }

  /**
   * Test that synthesizer only encodes whitelisted ACL when given a configuration with two ACLs.
   */
  @Test
  public void testEnabledAcls() {
    Configuration config = _cb.build();
    Builder aclb = _nf.aclBuilder().setOwner(config);
    IpAccessList acl1 = aclb.setLines(ImmutableList.of()).setName("acl1").build();
    IpAccessList acl2 = aclb.setLines(ImmutableList.of()).setName("acl2").build();

    assertThat(config, hasIpAccessLists(hasEntry(equalTo("acl1"), equalTo(acl1))));
    assertThat(config, hasIpAccessLists(hasEntry(equalTo("acl2"), equalTo(acl2))));

    Map<String, Configuration> configurations = ImmutableMap.of(config.getHostname(), config);
    Map<String, Set<String>> enabledAcls =
        ImmutableMap.of(config.getHostname(), ImmutableSet.of(acl1.getName()));
    SynthesizerInputImpl inputImpl =
        SynthesizerInputImpl.builder()
            .setConfigurations(configurations)
            .setEnabledAcls(enabledAcls)
            .build();

    // ACL actions and ACL conditions should both contain acl1 and not acl2.
    assertThat(
        inputImpl.getAclActions(),
        hasEntry(equalTo(config.getHostname()), hasKey(equalTo(acl1.getName()))));
    assertThat(inputImpl.getAclActions().get(config.getHostname()).size(), equalTo(1));

    assertThat(
        inputImpl.getAclConditions(),
        hasEntry(equalTo(config.getHostname()), hasKey(equalTo(acl1.getName()))));
    assertThat(inputImpl.getAclConditions().get(config.getHostname()).size(), equalTo(1));
  }

  /**
   * Test that for a SourceNat with no ACL, the SynthesizerInput will have an "accept everything"
   * ACL.
   */
  @Test
  public void testSourceNatWithNoAcl() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Ip ip1 = new Ip("1.0.0.0");
    Ip ip2 = new Ip("1.0.0.10");
    DynamicNatRule sourceNat =
        _dtb.setAction(RuleAction.SOURCE_INSIDE).setPoolIpFirst(ip1).setPoolIpLast(ip2).build();
    assertThat(sourceNat, notNullValue());
    Interface srcInterfaceOneSourceNat =
        _ib.setOwner(srcNode).setVrf(srcVrf).setEgressNats(ImmutableList.of(sourceNat)).build();
    Interface nextHopInterface =
        _ib.setOwner(nextHop).setVrf(nextHopVrf).setEgressNats(ImmutableList.of()).build();
    Edge forwardEdge = new Edge(srcInterfaceOneSourceNat, nextHopInterface);
    Edge backEdge = new Edge(nextHopInterface, srcInterfaceOneSourceNat);
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getHostname(), srcNode, nextHop.getHostname(), nextHop))
            .setForwardingAnalysis(MockForwardingAnalysis.builder().build())
            .setTopology(new Topology(ImmutableSortedSet.of(forwardEdge, backEdge)))
            .build();

    // Acl for the SourceNat is DefaultSourceNatAcl
    assertThat(
        inputWithDataPlane,
        hasEgressSrcNats(
            hasEntry(
                equalTo(srcNode.getHostname()),
                hasEntry(
                    equalTo(srcInterfaceOneSourceNat.getName()),
                    equalTo(
                        ImmutableList.of(
                            immutableEntry(
                                null,
                                new RangeMatchExpr(
                                    new TransformedVarIntExpr(Field.SRC_IP),
                                    Field.SRC_IP.getSize(),
                                    ImmutableSet.of(
                                        Range.closed(ip1.asLong(), ip2.asLong()))))))))));
  }
}
