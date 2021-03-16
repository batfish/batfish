package org.batfish.bddreachability;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import net.sf.javabdd.BDD;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.ReachabilityParameters;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.NameRegexNodeSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLocationSpecifier;
import org.batfish.symbolic.IngressLocation;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDReachabilityAnalysisIgnoreFiltersTest {
  private static final String NODE1 = "node1";
  private static final ConcreteInterfaceAddress NODE1_ADDR =
      ConcreteInterfaceAddress.parse("1.2.3.1/24");
  private static final ConcreteInterfaceAddress NODE2_ADDR =
      ConcreteInterfaceAddress.parse("1.2.3.2/24");
  private static final String NODE2 = "node2";
  private static final Ip DENIED_IN_SRC_IP = Ip.parse("1.1.1.1");
  private static final Ip DENIED_OUT_SRC_IP = Ip.parse("1.1.1.2");
  private static final IngressLocation INGRESS_LOCATION =
      IngressLocation.vrf(NODE1, Configuration.DEFAULT_VRF_NAME);
  private static final Ip NAT_MATCH_IP = Ip.parse("2.2.2.2");
  private static final Ip NAT_POOL_IP = Ip.parse("2.2.2.3");

  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _zero = _pkt.getFactory().zero();

  public static @ClassRule TemporaryFolder temp = new TemporaryFolder();

  private static Batfish batfish;
  private static InterfaceLocation IFACE1_LOCATION;

  @BeforeClass
  public static void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();

    Configuration c1 =
        nf.configurationBuilder()
            .setHostname(NODE1)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf1 = nf.vrfBuilder().setOwner(c1).setName(Configuration.DEFAULT_VRF_NAME).build();
    IpAccessList outgoingFilter =
        nf.aclBuilder()
            .setOwner(c1)
            .setLines(ImmutableList.of(ExprAclLine.rejecting(matchSrc(DENIED_OUT_SRC_IP))))
            .build();
    Interface iface1 =
        nf.interfaceBuilder()
            .setOwner(c1)
            .setVrf(vrf1)
            .setActive(true)
            .setAddress(NODE1_ADDR)
            .setOutgoingTransformation(
                when(matchSrc(NAT_MATCH_IP))
                    .apply(assignSourceIp(NAT_POOL_IP, NAT_POOL_IP))
                    .build())
            .setOutgoingFilter(outgoingFilter)
            .build();

    Configuration c2 =
        nf.configurationBuilder()
            .setHostname(NODE2)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).setName(Configuration.DEFAULT_VRF_NAME).build();
    IpAccessList incomingFilter =
        nf.aclBuilder()
            .setOwner(c2)
            .setLines(ImmutableList.of(ExprAclLine.rejecting(matchSrc(DENIED_IN_SRC_IP))))
            .build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setVrf(vrf2)
        .setActive(true)
        .setAddress(NODE2_ADDR)
        .setIncomingFilter(incomingFilter)
        .build();

    IFACE1_LOCATION = new InterfaceLocation(c1.getHostname(), iface1.getName());

    batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2), temp);
    batfish.computeDataPlane(batfish.getSnapshot());
  }

  BDDReachabilityAnalysis initAnalysis(
      IpSpace initialSrcIp, FlowDisposition disposition, boolean ignoreFilters) {
    NetworkSnapshot snapshot = batfish.getSnapshot();
    Map<String, Configuration> configs = batfish.loadConfigurations(snapshot);
    DataPlane dataPlane = batfish.loadDataPlane(snapshot);
    return new BDDReachabilityAnalysisFactory(
            _pkt,
            configs,
            dataPlane.getForwardingAnalysis(),
            new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
            ignoreFilters,
            false)
        .bddReachabilityAnalysis(
            IpSpaceAssignment.builder().assign(IFACE1_LOCATION, initialSrcIp).build(),
            matchDst(NODE2_ADDR.getIp().toIpSpace()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configs.keySet(),
            ImmutableSet.of(disposition));
  }

  @Test
  public void ignoreInAcl() {
    Map<IngressLocation, BDD> reachableBDDs =
        initAnalysis(DENIED_IN_SRC_IP.toIpSpace(), ACCEPTED, true)
            .getIngressLocationReachableBDDs();
    assertThat(reachableBDDs, hasEntry(equalTo(INGRESS_LOCATION), not(equalTo(_zero))));
  }

  @Test
  public void dontIgnoreInAcl() {
    Map<IngressLocation, BDD> reachableBDDs =
        initAnalysis(DENIED_IN_SRC_IP.toIpSpace(), ACCEPTED, false)
            .getIngressLocationReachableBDDs();
    assertThat(reachableBDDs, hasEntry(equalTo(INGRESS_LOCATION), equalTo(_zero)));
  }

  @Test
  public void ignoreOutAcl() {
    Map<IngressLocation, BDD> reachableBDDs =
        initAnalysis(DENIED_OUT_SRC_IP.toIpSpace(), ACCEPTED, true)
            .getIngressLocationReachableBDDs();
    assertThat(reachableBDDs, hasEntry(equalTo(INGRESS_LOCATION), not(equalTo(_zero))));
  }

  @Test
  public void dontIgnoreOutAcl() {
    Map<IngressLocation, BDD> reachableBDDs =
        initAnalysis(DENIED_OUT_SRC_IP.toIpSpace(), ACCEPTED, false)
            .getIngressLocationReachableBDDs();
    assertThat(reachableBDDs, hasEntry(equalTo(INGRESS_LOCATION), equalTo(_zero)));
  }

  @Test
  public void testParameters() {
    ReachabilityParameters.Builder parameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(new NameRegexNodeSpecifier(Pattern.compile(NODE2)))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLocationSpecifier(Pattern.compile(NODE1)))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(DENIED_OUT_SRC_IP.toIpSpace()))
            .setDestinationIpSpaceSpecifier(
                new ConstantIpSpaceSpecifier(NODE2_ADDR.getIp().toIpSpace()));

    TraceWrapperAsAnswerElement traceWrapper =
        (TraceWrapperAsAnswerElement)
            batfish.bddSingleReachability(batfish.getSnapshot(), parameters.build());
    assertThat(traceWrapper.getFlowTraces().entrySet(), empty());

    traceWrapper =
        (TraceWrapperAsAnswerElement)
            batfish.bddSingleReachability(
                batfish.getSnapshot(), parameters.setIgnoreFilters(true).build());
    assertThat(traceWrapper.getFlowTraces().entrySet(), hasSize(1));
  }
}
