package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDMultipathInconsistency.computeMultipathInconsistencies;
import static org.batfish.bddreachability.BDDMultipathInconsistency.computeMultipathInconsistencyBDDs;
import static org.batfish.bddreachability.BDDMultipathInconsistency.multipathInconsistencyToFlow;
import static org.batfish.bddreachability.TestNetwork.DST_PREFIX_2;
import static org.batfish.bddreachability.TestNetwork.POST_SOURCE_NAT_ACL_DEST_PORT;
import static org.batfish.bddreachability.TestNetwork.SOURCE_NAT_ACL_IP;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.symbolic.IngressLocation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDMultipathInconsistencyTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private final BDDPacket _pkt = new BDDPacket();
  private final IpSpaceToBDD _srcToBDD = _pkt.getSrcIpSpaceToBDD();

  private BDDReachabilityAnalysisFactory _graphFactory;
  private TestNetwork _net;

  private Ip _dstIface2Ip;

  private String _srcName;

  private Batfish _batfish;

  private BDD dstPortBDD(int port) {
    return _pkt.getDstPort().value(port);
  }

  private BDD srcIpBDD(Ip ip) {
    return _srcToBDD.toBDD(ip);
  }

  @Before
  public void setup() throws IOException {
    _net = new TestNetwork();
    _batfish = BatfishTestUtils.getBatfish(_net._configs, temp);

    _batfish.computeDataPlane(_batfish.getSnapshot());
    DataPlane dataPlane = _batfish.loadDataPlane(_batfish.getSnapshot());
    _graphFactory =
        new BDDReachabilityAnalysisFactory(
            _pkt,
            _net._configs,
            dataPlane.getForwardingAnalysis(),
            new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
            false,
            false);

    _dstIface2Ip = DST_PREFIX_2.getStartIp();
    _srcName = _net._srcNode.getHostname();
  }

  @Test
  public void testBDDNetworkGraph_sourceNat_noMatch() {
    IpSpaceAssignment assignment =
        IpSpaceAssignment.builder()
            .assign(
                new InterfaceLocation(_net._srcNode.getHostname(), _net._link1Src.getName()),
                Ip.MAX.toIpSpace())
            .build();

    Set<String> finalNodes = _net._configs.keySet();
    Map<IngressLocation, BDD> acceptedBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(_dstIface2Ip.toIpSpace()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(ACCEPTED))
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> deniedOutBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(_dstIface2Ip.toIpSpace()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(DENIED_IN))
            .getIngressLocationReachableBDDs();

    BDD tcpBdd = _pkt.getIpProtocol().value(IpProtocol.TCP);
    BDD dstIpBDD = _graphFactory.getIpSpaceToBDD().toBDD(_dstIface2Ip);
    BDD srcIpBDD = srcIpBDD(Ip.MAX);
    BDD postNatAclBDD = dstPortBDD(POST_SOURCE_NAT_ACL_DEST_PORT);

    List<MultipathInconsistency> inconsistencies =
        computeMultipathInconsistencyBDDs(acceptedBDDs, deniedOutBDDs).collect(Collectors.toList());

    assertThat(inconsistencies, hasSize(1));
    MultipathInconsistency inconsistency = inconsistencies.get(0);

    BDD expected = tcpBdd.and(dstIpBDD).and(srcIpBDD).and(postNatAclBDD);
    assertEquals(expected, inconsistency.getBDD());

    Flow flow = multipathInconsistencyToFlow(_pkt, inconsistency);
    assertThat(flow, hasDstIp(_dstIface2Ip));
  }

  @Test
  public void testBDDNetworkGraph_sourceNat_match() {
    IpSpaceAssignment assignment =
        IpSpaceAssignment.builder()
            .assign(
                new InterfaceLocation(_net._srcNode.getHostname(), _net._link1Src.getName()),
                SOURCE_NAT_ACL_IP.toIpSpace())
            .build();

    Set<String> finalNodes = _net._configs.keySet();
    Map<IngressLocation, BDD> acceptedBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(_dstIface2Ip.toIpSpace()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(ACCEPTED))
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> deniedOutBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(_dstIface2Ip.toIpSpace()),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(DENIED_IN))
            .getIngressLocationReachableBDDs();

    BDD natAclIpBDD = srcIpBDD(SOURCE_NAT_ACL_IP);
    BDD srcNatAclBDD = IpAccessListToBdd.toBDD(_pkt, _net._link2SrcSourceNatAcl);
    assertThat(srcNatAclBDD, equalTo(natAclIpBDD));

    /*
     * ... and we detect a violation for the intersection.
     */
    List<MultipathInconsistency> inconsistencies =
        computeMultipathInconsistencyBDDs(acceptedBDDs, deniedOutBDDs).collect(Collectors.toList());
    assertThat(inconsistencies, hasSize(1));
    MultipathInconsistency inconsistency = inconsistencies.get(0);
    assertThat(
        inconsistency.getIngressLocation(),
        equalTo(IngressLocation.vrf(_srcName, DEFAULT_VRF_NAME)));
  }

  @Test
  public void testAllDispositions() {
    IpSpaceAssignment assignment =
        _batfish.getAllSourcesInferFromLocationIpSpaceAssignment(_batfish.getSnapshot());
    Set<FlowDisposition> dropDispositions =
        ImmutableSet.of(DENIED_IN, DENIED_OUT, NO_ROUTE, NULL_ROUTED);

    Set<String> finalNodes = _net._configs.keySet();
    Map<IngressLocation, BDD> acceptedBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(UniverseIpSpace.INSTANCE),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(ACCEPTED))
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> deniedInBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(UniverseIpSpace.INSTANCE),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(DENIED_IN))
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> dropBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(UniverseIpSpace.INSTANCE),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                dropDispositions)
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> dropExceptDeniedInBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(UniverseIpSpace.INSTANCE),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                Sets.difference(dropDispositions, ImmutableSet.of(DENIED_IN)))
            .getIngressLocationReachableBDDs();
    Map<IngressLocation, BDD> neighborUnreachableBDDs =
        _graphFactory
            .bddReachabilityAnalysis(
                assignment,
                matchDst(UniverseIpSpace.INSTANCE),
                ImmutableSet.of(),
                ImmutableSet.of(),
                finalNodes,
                ImmutableSet.of(NEIGHBOR_UNREACHABLE))
            .getIngressLocationReachableBDDs();

    assertThat(
        computeMultipathInconsistencies(_pkt, acceptedBDDs, dropExceptDeniedInBDDs), empty());
    assertThat(
        computeMultipathInconsistencies(_pkt, acceptedBDDs, dropBDDs),
        equalTo(computeMultipathInconsistencies(_pkt, acceptedBDDs, deniedInBDDs)));
    assertThat(computeMultipathInconsistencies(_pkt, neighborUnreachableBDDs, dropBDDs), empty());
    assertThat(
        computeMultipathInconsistencies(_pkt, acceptedBDDs, neighborUnreachableBDDs), empty());
  }
}
