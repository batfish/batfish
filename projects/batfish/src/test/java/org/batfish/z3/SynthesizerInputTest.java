package org.batfish.z3;

import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledAcls;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledEdges;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledFlowSinks;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledInterfaces;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledNodes;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledVrfs;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasFibConditions;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasIpsByHostname;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasTopologyInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestDataPlane;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.FibRowMatchExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.matchers.SynthesizerInputMatchers;
import org.junit.Before;
import org.junit.Test;

public class SynthesizerInputTest {

  private IpAccessList.Builder _aclb;

  private Configuration.Builder _cb;

  private Interface.Builder _ib;

  private SynthesizerInput.Builder _inputBuilder;

  private NetworkFactory _nf;

  private Vrf.Builder _vb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder();
    _ib = _nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    _aclb = _nf.aclBuilder();
    _inputBuilder = SynthesizerInput.builder();
  }

  @Test
  public void testComputeAclConditions() {
    Configuration c = _cb.build();
    IpAccessList aclWithoutLines = _aclb.setOwner(c).build();
    IpAccessList aclWithLines =
        _aclb
            .setLines(
                ImmutableList.<IpAccessListLine>of(
                    IpAccessListLine.builder()
                        .setDstIps(ImmutableSet.of(new IpWildcard(new Ip("1.2.3.4"))))
                        .build(),
                    IpAccessListLine.builder()
                        .setDstIps(ImmutableSet.of(new IpWildcard(new Ip("5.6.7.8"))))
                        .build()))
            .build();
    SynthesizerInput input =
        _inputBuilder.setConfigurations(ImmutableMap.of(c.getName(), c)).build();

    assertThat(
        input,
        SynthesizerInputMatchers.hasAclConditions(
            equalTo(
                ImmutableMap.of(
                    c.getName(),
                    ImmutableMap.of(
                        aclWithoutLines.getName(),
                        ImmutableMap.of(),
                        aclWithLines.getName(),
                        ImmutableMap.of(
                            0,
                            new HeaderSpaceMatchExpr(aclWithLines.getLines().get(0)),
                            1,
                            new HeaderSpaceMatchExpr(aclWithLines.getLines().get(1))))))));
  }

  @Test
  public void testComputeEnabledAcls() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    IpAccessList srcInterfaceInAcl = _aclb.setOwner(srcNode).build();
    IpAccessList srcInterfaceOutAcl = _aclb.build();
    IpAccessList iFlowSinkInAcl = _aclb.build();
    IpAccessList iFlowSinkOutAcl = _aclb.build();
    IpAccessList iNoEdgeInAcl = _aclb.build();
    IpAccessList iNoEdgeOutAcl = _aclb.build();
    IpAccessList nextHopInterfaceInAcl = _aclb.setOwner(nextHop).build();
    IpAccessList nextHopInterfaceOutAcl = _aclb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Interface srcInterface =
        _ib.setOwner(srcNode)
            .setVrf(srcVrf)
            .setIncomingFilter(srcInterfaceInAcl)
            .setOutgoingFilter(srcInterfaceOutAcl)
            .build();
    Interface iFlowSink =
        _ib.setIncomingFilter(iFlowSinkInAcl).setOutgoingFilter(iFlowSinkOutAcl).build();
    /*
     * Interface without an edge: Its ACLs should be absent with data plane, but present without
     * data plane.
     */
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
                ImmutableMap.of(srcNode.getName(), srcNode, nextHop.getName(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setDataPlane(
                TestDataPlane.builder()
                    .setFlowSinks(
                        ImmutableSet.of(
                            new NodeInterfacePair(srcNode.getName(), iFlowSink.getName())))
                    .setTopologyEdges(ImmutableSortedSet.of(forwardEdge, backEdge))
                    .build())
            .build();
    Map<String, IpAccessList> expectedSrcNodeWithDataPlane =
        ImmutableMap.of(
            srcInterfaceInAcl.getName(),
            srcInterfaceInAcl,
            srcInterfaceOutAcl.getName(),
            srcInterfaceOutAcl,
            iFlowSinkInAcl.getName(),
            iFlowSinkInAcl,
            iFlowSinkOutAcl.getName(),
            iFlowSinkOutAcl);
    Map<String, IpAccessList> expectedSrcNodeWithoutDataPlane =
        ImmutableMap.<String, IpAccessList>builder()
            .putAll(expectedSrcNodeWithDataPlane)
            .put(iNoEdgeInAcl.getName(), iNoEdgeInAcl)
            .put(iNoEdgeOutAcl.getName(), iNoEdgeOutAcl)
            .build();
    Map<String, IpAccessList> expectedNextHop =
        ImmutableMap.of(
            nextHopInterfaceInAcl.getName(),
            nextHopInterfaceInAcl,
            nextHopInterfaceOutAcl.getName(),
            nextHopInterfaceOutAcl);

    assertThat(
        inputWithDataPlane,
        hasEnabledAcls(
            equalTo(
                ImmutableMap.of(
                    srcNode.getName(),
                    expectedSrcNodeWithDataPlane,
                    nextHop.getName(),
                    expectedNextHop))));
    assertThat(
        inputWithoutDataPlane,
        hasEnabledAcls(
            equalTo(
                ImmutableMap.of(
                    srcNode.getName(),
                    expectedSrcNodeWithoutDataPlane,
                    nextHop.getName(),
                    expectedNextHop))));
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
                ImmutableMap.of(srcNode.getName(), srcNode, nextHop.getName(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setDataPlane(
                TestDataPlane.builder()
                    .setTopologyEdges(
                        ImmutableSortedSet.of(expectedEnabledEdge, expectedDisabledEdge))
                    .build())
            .build();

    assertThat(inputWithDataPlane, hasEnabledEdges(hasItem(expectedEnabledEdge)));
    assertThat(inputWithDataPlane, hasEnabledEdges(not(hasItem(expectedDisabledEdge))));
    assertThat(inputWithDataPlane, hasEnabledEdges(not(hasItem(expectedDisabledEdge))));
    assertThat(inputWithoutDataPlane, hasEnabledEdges(nullValue()));
  }

  @Test
  public void testComputeEnabledFlowSinks() {
    Configuration cEnabled = _cb.build();
    Configuration cDisabled = _cb.build();
    Vrf vEnabled = _vb.setOwner(cEnabled).build();
    // Enabled but not flow sink. Should not appear in enabledFlowSinks.
    _ib.setOwner(cEnabled).setVrf(vEnabled).build();
    Interface iEnabledAndFlowSink = _ib.build();
    Interface iDisabledViaInactiveAndFlowSink = _ib.setActive(false).build();
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(cEnabled.getName(), cEnabled, cDisabled.getName(), cDisabled))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setDataPlane(
                TestDataPlane.builder()
                    .setFlowSinks(
                        ImmutableSet.of(
                            new NodeInterfacePair(
                                cEnabled.getName(), iEnabledAndFlowSink.getName()),
                            new NodeInterfacePair(
                                cEnabled.getName(), iDisabledViaInactiveAndFlowSink.getName())))
                    .build())
            .build();

    assertThat(
        inputWithDataPlane,
        hasEnabledFlowSinks(
            equalTo(
                ImmutableSet.of(
                    new NodeInterfacePair(cEnabled.getName(), iEnabledAndFlowSink.getName())))));
    assertThat(inputWithoutDataPlane, hasEnabledFlowSinks(nullValue()));
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
                ImmutableMap.of(cEnabled.getName(), cEnabled, cDisabled.getName(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getName()))
            .setDisabledVrfs(
                ImmutableMap.of(cEnabled.getName(), ImmutableSet.of(vDisabledViaVrf.getName())))
            .setDisabledInterfaces(
                ImmutableMap.of(
                    cEnabled.getName(), ImmutableSet.of(iDisabledViaInterface.getName())))
            .build();

    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getName()), hasEntry(iEnabled.getName(), iEnabled))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getName()), not(hasKey(iDisabledViaInactive.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getName()), not(hasKey(iDisabledViaBlacklisted.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getName()), not(hasKey(iDisabledViaInterface.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(cEnabled.getName()), not(hasKey(iDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledInterfaces(not(hasKey(cDisabled.getName()))));
  }

  @Test
  public void testComputeEnabledNodes() {
    Configuration cEnabled = _cb.build();
    Configuration cDisabled = _cb.build();
    SynthesizerInput input =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(cEnabled.getName(), cEnabled, cDisabled.getName(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getName()))
            .build();

    assertThat(input, hasEnabledNodes(hasEntry(cEnabled.getName(), cEnabled)));
    assertThat(input, hasEnabledNodes(not(hasKey(cDisabled.getName()))));
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
                ImmutableMap.of(cEnabled.getName(), cEnabled, cDisabled.getName(), cDisabled))
            .setDisabledNodes(ImmutableSet.of(cDisabled.getName()))
            .setDisabledVrfs(
                ImmutableMap.of(cEnabled.getName(), ImmutableSet.of(vDisabledViaVrf.getName())))
            .build();

    assertThat(
        input,
        hasEnabledVrfs(
            hasEntry(equalTo(cEnabled.getName()), hasEntry(vEnabled.getName(), vEnabled))));
    assertThat(
        input,
        hasEnabledVrfs(
            hasEntry(equalTo(cEnabled.getName()), not(hasKey(vDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledVrfs(not(hasKey(cDisabled.getName()))));
  }

  @Test
  public void testComputeFibConditions() {
    Configuration srcNode = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Interface srcInterface = _ib.setOwner(srcNode).setVrf(srcVrf).build();
    String nextHop1 = "nextHop1";
    String nextHopInterface1 = "nextHopInterface1";
    String nextHop2 = "nextHop2";
    String nextHopInterface2 = "nextHopInterface2";
    Prefix p1 = Prefix.parse("1.2.3.0/24");
    Prefix p2 = Prefix.parse("3.4.5.0/24");
    FibRow iEnabledFibRow11 = new FibRow(p1, srcInterface.getName(), nextHop1, nextHopInterface1);
    FibRow iEnabledFibRow12 = new FibRow(p2, srcInterface.getName(), nextHop1, nextHopInterface1);
    FibRow iEnabledFibRow21 = new FibRow(p1, srcInterface.getName(), nextHop2, nextHopInterface2);
    FibRow iEnabledFibRow22 = new FibRow(p2, srcInterface.getName(), nextHop2, nextHopInterface2);
    FibRow defaultDropFibRow = new FibRow(Prefix.ZERO, FibRow.DROP_NO_ROUTE, "", "");
    SortedSet<FibRow> fibs =
        ImmutableSortedSet.of(
            iEnabledFibRow11,
            iEnabledFibRow12,
            iEnabledFibRow21,
            iEnabledFibRow22,
            defaultDropFibRow);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder.setConfigurations(ImmutableMap.of(srcNode.getName(), srcNode)).build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setDataPlane(
                TestDataPlane.builder()
                    .setFibs(
                        ImmutableMap.of(srcNode.getName(), ImmutableMap.of(srcVrf.getName(), fibs)))
                    .build())
            .build();

    assertThat(
        inputWithDataPlane,
        hasFibConditions(
            equalTo(
                ImmutableMap.of(
                    srcNode.getName(),
                    ImmutableMap.of(
                        srcVrf.getName(),
                        ImmutableMap.of(
                            srcInterface.getName(),
                            ImmutableMap.of(
                                new NodeInterfacePair(nextHop1, nextHopInterface1),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            srcNode.getName(),
                                            srcVrf.getName(),
                                            ImmutableList.copyOf(fibs),
                                            1,
                                            iEnabledFibRow11),
                                        FibRowMatchExpr.getFibRowConditions(
                                            srcNode.getName(),
                                            srcVrf.getName(),
                                            ImmutableList.copyOf(fibs),
                                            3,
                                            iEnabledFibRow12))),
                                new NodeInterfacePair(nextHop2, nextHopInterface2),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            srcNode.getName(),
                                            srcVrf.getName(),
                                            ImmutableList.copyOf(fibs),
                                            2,
                                            iEnabledFibRow21),
                                        FibRowMatchExpr.getFibRowConditions(
                                            srcNode.getName(),
                                            srcVrf.getName(),
                                            ImmutableList.copyOf(fibs),
                                            4,
                                            iEnabledFibRow22)))),
                            FibRow.DROP_NO_ROUTE,
                            ImmutableMap.of(
                                new NodeInterfacePair("", ""),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            srcNode.getName(),
                                            srcVrf.getName(),
                                            ImmutableList.copyOf(fibs),
                                            0,
                                            defaultDropFibRow))))))))));
    assertThat(inputWithoutDataPlane, hasFibConditions(nullValue()));
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
        _inputBuilder.setConfigurations(ImmutableMap.of(c.getName(), c)).build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder.setDataPlane(TestDataPlane.builder().build()).build();

    assertThat(inputWithoutDataPlane, hasIpsByHostname(nullValue()));
    assertThat(
        inputWithDataPlane,
        hasIpsByHostname(
            equalTo(ImmutableMap.of(c.getName(), ImmutableSet.of(ipEnabled1, ipEnabled2)))));
  }

  @Test
  public void testComputeTopologyInterfaces() {
    Configuration srcNode = _cb.build();
    Configuration nextHop = _cb.build();
    Vrf srcVrf = _vb.setOwner(srcNode).build();
    Vrf nextHopVrf = _vb.setOwner(nextHop).build();
    Interface srcInterface = _ib.setOwner(srcNode).setVrf(srcVrf).build();
    Interface iFlowSink = _ib.build();
    Interface iNoEdge = _ib.build();
    Interface nextHopInterface = _ib.setOwner(nextHop).setVrf(nextHopVrf).build();
    Edge forwardEdge = new Edge(srcInterface, nextHopInterface);
    Edge backEdge = new Edge(nextHopInterface, srcInterface);
    SynthesizerInput inputWithoutDataPlane =
        _inputBuilder
            .setConfigurations(
                ImmutableMap.of(srcNode.getName(), srcNode, nextHop.getName(), nextHop))
            .build();
    SynthesizerInput inputWithDataPlane =
        _inputBuilder
            .setDataPlane(
                TestDataPlane.builder()
                    .setFlowSinks(
                        ImmutableSet.of(
                            new NodeInterfacePair(srcNode.getName(), iFlowSink.getName())))
                    .setTopologyEdges(ImmutableSortedSet.of(forwardEdge, backEdge))
                    .build())
            .build();

    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(srcNode.getName()), hasItem(sameInstance(srcInterface)))));
    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(srcNode.getName()), not(hasItem(sameInstance(iNoEdge))))));
    assertThat(
        inputWithDataPlane,
        hasTopologyInterfaces(
            hasEntry(equalTo(nextHop.getName()), hasItem(sameInstance(nextHopInterface)))));
    assertThat(inputWithoutDataPlane, hasTopologyInterfaces(nullValue()));
  }
}
