package org.batfish.question.ipsecpeers;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IKE_PHASE1_FAILED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IKE_PHASE1_KEY_MISMATCH;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IPSEC_PHASE2_FAILED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IPSEC_SESSION_ESTABLISHED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.MISSING_END_POINT;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfoMatchers.hasIpsecPeeringStatus;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_INITIATOR;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_INIT_INTERFACE_IP;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_RESPONDER;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_RESPONDER_INTERFACE_IP;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_STATUS;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_TUNNEL_INTERFACE;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.rawAnswer;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.toRow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.batfish.common.Pair;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.table.Row;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link IpsecPeersAnswerer} */
public class IpsecPeersAnswererTest {

  private static final String INITIATOR_HOST_NAME = "initiator_host_name";
  private static final String RESPONDER_HOST_NAME = "responder_host_name";

  private IpsecStaticPeerConfig.Builder _ipsecStaticPeerConfigBuilder =
      IpsecStaticPeerConfig.builder();
  private MutableValueGraph<Pair<String, IpsecPeerConfig>, IpsecSession> _graph;
  private IpsecSession _ipsecSession;

  @Before
  public void setup() {
    _ipsecStaticPeerConfigBuilder
        .setPhysicalInterface("Test_interface")
        .setLocalAddress(new Ip("1.2.3.4"))
        .setTunnelInterface("Tunnel_interface");
    _graph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();
    _ipsecSession = new IpsecSession();
  }

  @Test
  public void testGenerateRowsIke1Fail() {
    // IPSecSession does not have IKE phase 1 proposal set
    _graph.putEdgeValue(
        new Pair<>(INITIATOR_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(RESPONDER_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<IpsecPeeringInfo> peerings =
        rawAnswer(
            _graph, ImmutableSet.of(INITIATOR_HOST_NAME), ImmutableSet.of(RESPONDER_HOST_NAME));

    // answer should have exactly one row
    assertThat(peerings, hasSize(1));

    assertThat(peerings.iterator().next(), hasIpsecPeeringStatus(equalTo(IKE_PHASE1_FAILED)));
  }

  @Test
  public void testGenerateRowsIke1KeyFail() {
    // IPSecSession does not have IKE phase 1 key set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _graph.putEdgeValue(
        new Pair<>(INITIATOR_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(RESPONDER_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<IpsecPeeringInfo> peerings =
        rawAnswer(
            _graph, ImmutableSet.of(INITIATOR_HOST_NAME), ImmutableSet.of(RESPONDER_HOST_NAME));

    // answer should have exactly one row
    assertThat(peerings, hasSize(1));

    assertThat(peerings.iterator().next(), hasIpsecPeeringStatus(equalTo(IKE_PHASE1_KEY_MISMATCH)));
  }

  @Test
  public void testGenerateRowsIpsec2Fail() {
    // IPSecSession does not have IPSec phase 2 proposal set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _ipsecSession.setNegotiatedIkeP1Key(new IkePhase1Key());
    _graph.putEdgeValue(
        new Pair<>(INITIATOR_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(RESPONDER_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<IpsecPeeringInfo> peerings =
        rawAnswer(
            _graph, ImmutableSet.of(INITIATOR_HOST_NAME), ImmutableSet.of(RESPONDER_HOST_NAME));

    // answer should have exactly one row
    assertThat(peerings, hasSize(1));

    assertThat(peerings.iterator().next(), hasIpsecPeeringStatus(equalTo(IPSEC_PHASE2_FAILED)));
  }

  @Test
  public void testGenerateRowsMissingEndpoint() {
    // Responder not set in the graph
    _graph.addNode(new Pair<>(INITIATOR_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()));
    Multiset<IpsecPeeringInfo> peerings =
        rawAnswer(
            _graph, ImmutableSet.of(INITIATOR_HOST_NAME), ImmutableSet.of(RESPONDER_HOST_NAME));

    // answer should have exactly one row
    assertThat(peerings, hasSize(1));

    assertThat(peerings.iterator().next(), hasIpsecPeeringStatus(equalTo(MISSING_END_POINT)));
  }

  @Test
  public void testGenerateRowsIpsecEstablished() {
    // IPSecSession does not have IPSec phase 2 proposal set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _ipsecSession.setNegotiatedIkeP1Key(new IkePhase1Key());
    _ipsecSession.setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal());
    _graph.putEdgeValue(
        new Pair<>(INITIATOR_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(RESPONDER_HOST_NAME, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<IpsecPeeringInfo> peerings =
        rawAnswer(
            _graph, ImmutableSet.of(INITIATOR_HOST_NAME), ImmutableSet.of(RESPONDER_HOST_NAME));

    // answer should have exactly one row
    assertThat(peerings, hasSize(1));

    assertThat(
        peerings.iterator().next(), hasIpsecPeeringStatus(equalTo(IPSEC_SESSION_ESTABLISHED)));
  }

  @Test
  public void testToRow() {
    IpsecPeeringInfo.Builder ipsecPeeringInfoBuilder = IpsecPeeringInfo.builder();

    IpsecPeeringInfo ipsecPeeringInfo =
        ipsecPeeringInfoBuilder
            .setInitiatorHostname(INITIATOR_HOST_NAME)
            .setInitiatorInterface("Test_interface")
            .setInitiatorIp(new Ip("1.2.3.4"))
            .setInitiatorTunnelInterface("Tunnel_interface")
            .setResponderHostname(RESPONDER_HOST_NAME)
            .setResponderInterface("Test_interface")
            .setResponderIp(new Ip("2.3.4.5"))
            .setResponderTunnelInterface("Tunnel1_interface")
            .setIpsecPeeringStatus(IPSEC_SESSION_ESTABLISHED)
            .build();

    Row row = toRow(ipsecPeeringInfo);

    assertThat(
        row,
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node(INITIATOR_HOST_NAME)), Schema.NODE),
            hasColumn(COL_RESPONDER, equalTo(new Node(RESPONDER_HOST_NAME)), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_RESPONDER_INTERFACE_IP, equalTo("Test_interface:2.3.4.5"), Schema.STRING),
            hasColumn(
                COL_TUNNEL_INTERFACE,
                equalTo("Tunnel_interface->Tunnel1_interface"),
                Schema.STRING),
            hasColumn(COL_STATUS, equalTo("IPSEC_SESSION_ESTABLISHED"), Schema.STRING)));
  }
}
