package org.batfish.question.ipsecpeers;

import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_INITIATOR;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_INIT_INTERFACE_IP;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_RESPONDER;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_RESPONDER_INTERFACE_IP;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.COL_STATUS;
import static org.batfish.question.ipsecpeers.IpsecPeersAnswerer.generateRows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
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

  private Configuration _initiator;
  private Configuration _responder;
  private IpsecStaticPeerConfig.Builder _ipsecStaticPeerConfigBuilder =
      IpsecStaticPeerConfig.builder();
  private MutableValueGraph<Pair<Configuration, IpsecPeerConfig>, IpsecSession> _graph;
  private IpsecSession _ipsecSession;

  @Before
  public void setup() {
    _initiator = new Configuration("testhost", ConfigurationFormat.UNKNOWN);
    _responder = new Configuration("responder", ConfigurationFormat.UNKNOWN);
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
        new Pair<>(_initiator, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(_responder, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<Row> rows =
        generateRows(_graph, ImmutableSet.of("testhost"), ImmutableSet.of("responder"));

    // answer should have exactly one row
    assertThat(rows, hasSize(1));

    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node("testhost")), Schema.NODE),
            hasColumn(COL_RESPONDER, equalTo(new Node("responder")), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_RESPONDER_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_STATUS, equalTo("IKE_PHASE1_FAILED"), Schema.STRING)));
  }

  @Test
  public void testGenerateRowsIke1KeyFail() {
    // IPSecSession does not have IKE phase 1 key set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _graph.putEdgeValue(
        new Pair<>(_initiator, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(_responder, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<Row> rows =
        generateRows(_graph, ImmutableSet.of("testhost"), ImmutableSet.of("responder"));

    // answer should have exactly one row
    assertThat(rows, hasSize(1));

    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node("testhost")), Schema.NODE),
            hasColumn(COL_RESPONDER, equalTo(new Node("responder")), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_RESPONDER_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_STATUS, equalTo("IKE_PHASE1_KEY_MISMATCH"), Schema.STRING)));
  }

  @Test
  public void testGenerateRowsIpsec2Fail() {
    // IPSecSession does not have IPSec phase 2 proposal set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _ipsecSession.setNegotiatedIkeP1Key(new IkePhase1Key());
    _graph.putEdgeValue(
        new Pair<>(_initiator, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(_responder, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<Row> rows =
        generateRows(_graph, ImmutableSet.of("testhost"), ImmutableSet.of("responder"));

    // answer should have exactly one row
    assertThat(rows, hasSize(1));

    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node("testhost")), Schema.NODE),
            hasColumn(COL_RESPONDER, equalTo(new Node("responder")), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_RESPONDER_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_STATUS, equalTo("IPSEC_PHASE2_FAILED"), Schema.STRING)));
  }

  @Test
  public void testGenerateRowsMissingEndpoint() {
    // Responder not set in the graph
    _graph.addNode(new Pair<>(_initiator, _ipsecStaticPeerConfigBuilder.build()));
    Multiset<Row> rows =
        generateRows(_graph, ImmutableSet.of("testhost"), ImmutableSet.of("responder"));

    // answer should have exactly one row
    assertThat(rows, hasSize(1));

    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node("testhost")), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_STATUS, equalTo("MISSING_END_POINT"), Schema.STRING)));
  }

  @Test
  public void testGenerateRowsIpsecEstablished() {
    // IPSecSession does not have IPSec phase 2 proposal set
    _ipsecSession.setNegotiatedIkeP1Proposal(new IkePhase1Proposal("test_ike_proposal"));
    _ipsecSession.setNegotiatedIkeP1Key(new IkePhase1Key());
    _ipsecSession.setNegotiatedIpsecP2Proposal(new IpsecPhase2Proposal());
    _graph.putEdgeValue(
        new Pair<>(_initiator, _ipsecStaticPeerConfigBuilder.build()),
        new Pair<>(_responder, _ipsecStaticPeerConfigBuilder.build()),
        _ipsecSession);
    Multiset<Row> rows =
        generateRows(_graph, ImmutableSet.of("testhost"), ImmutableSet.of("responder"));

    // answer should have exactly one row
    assertThat(rows, hasSize(1));

    assertThat(
        rows.iterator().next(),
        allOf(
            hasColumn(COL_INITIATOR, equalTo(new Node("testhost")), Schema.NODE),
            hasColumn(COL_RESPONDER, equalTo(new Node("responder")), Schema.NODE),
            hasColumn(COL_INIT_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_RESPONDER_INTERFACE_IP, equalTo("Test_interface:1.2.3.4"), Schema.STRING),
            hasColumn(COL_STATUS, equalTo("IPSEC_SESSION_ESTABLISHED"), Schema.STRING)));
  }
}
