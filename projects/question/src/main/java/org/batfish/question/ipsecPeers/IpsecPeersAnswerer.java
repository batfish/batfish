package org.batfish.question.ipsecPeers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.ipsecPeers.IpsecPeersAnswerer.IpsecPeeringStatus.IKE_PHASE1_FAILED;
import static org.batfish.question.ipsecPeers.IpsecPeersAnswerer.IpsecPeeringStatus.IKE_PHASE1_KEY_NOT_MATCHING;
import static org.batfish.question.ipsecPeers.IpsecPeersAnswerer.IpsecPeeringStatus.IPSEC_PHASE2_FAILED;
import static org.batfish.question.ipsecPeers.IpsecPeersAnswerer.IpsecPeeringStatus.IPSEC_SESSION_ESTABLISHED;
import static org.batfish.question.ipsecPeers.IpsecPeersAnswerer.IpsecPeeringStatus.MISSING_END_POINT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecSession;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

class IpsecPeersAnswerer extends Answerer {
  private static final String COL_INITIATOR = "Initiator";
  private static final String COL_INIT_INTERFACE_IP = "InitiatorInterfaceAndIp";
  private static final String COL_RESPONDER = "Responder";
  private static final String COL_RESPONDER_INTERFACE_IP = "ResponderInterfaceAndIp";
  private static final String COL_STATUS = "status";
  private static final String COL_TUNNEL_INTERFACE = "TunnelInterface";
  private static final String NOT_APPLICABLE = "Not Applicable";

  IpsecPeersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    IpsecPeersQuestion question = (IpsecPeersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    ValueGraph<Pair<Configuration, IpsecPeerConfig>, IpsecSession> ipsecTopology =
        CommonUtil.initIpsecTopology(configurations);

    Set<String> initiatorNodes = question.getInitiatorRegex().getMatchingNodes(_batfish);
    Set<String> responderNodes = question.getResponderRegex().getMatchingNodes(_batfish);

    TableAnswerElement answerElement = new TableAnswerElement(getTableMetadata());

    answerElement.postProcessAnswer(
        _question, generateRows(ipsecTopology, initiatorNodes, responderNodes));
    return answerElement;
  }

  @VisibleForTesting
  static Multiset<Row> generateRows(
      ValueGraph<Pair<Configuration, IpsecPeerConfig>, IpsecSession> ipsecTopology,
      Set<String> initiatorNodes,
      Set<String> responderNodes) {
    Multiset<Row> rows = HashMultiset.create();
    for (Pair<Configuration, IpsecPeerConfig> node : ipsecTopology.nodes()) {
      if (node.getSecond() instanceof IpsecDynamicPeerConfig
          || !initiatorNodes.contains(node.getFirst().getHostname())) {
        continue;
      }
      Row.RowBuilder rowBuilder = Row.builder();
      rowBuilder.put(COL_INITIATOR, node.getFirst().getHostname());
      rowBuilder.put(
          COL_INIT_INTERFACE_IP,
          String.format(
              "%s:%s",
              node.getSecond().getPhysicalInterface(), node.getSecond().getLocalAddress()));
      Set<Pair<Configuration, IpsecPeerConfig>> neigbors = ipsecTopology.adjacentNodes(node);
      if (neigbors.isEmpty()) {
        rowBuilder.put(COL_STATUS, MISSING_END_POINT);
        rowBuilder.put(
            COL_TUNNEL_INTERFACE,
            firstNonNull(node.getSecond().getTunnelInterface(), NOT_APPLICABLE));
        rows.add(rowBuilder.build());
        continue;
      }
      for (Pair<Configuration, IpsecPeerConfig> neighbor : neigbors) {
        if (!responderNodes.contains(neighbor.getFirst().getHostname())) {
          continue;
        }
        IpsecSession ipsecSession = ipsecTopology.edgeValueOrDefault(node, neighbor, null);
        if (ipsecSession == null) {
          continue;
        }
        rowBuilder.put(COL_RESPONDER, neighbor.getFirst().getHostname());
        rowBuilder.put(
            COL_RESPONDER_INTERFACE_IP,
            String.format(
                "%s:%s",
                neighbor.getSecond().getPhysicalInterface(),
                neighbor.getSecond().getLocalAddress()));
        rowBuilder.put(COL_TUNNEL_INTERFACE, NOT_APPLICABLE);
        if (node.getSecond().getTunnelInterface() != null
            && node.getSecond().getTunnelInterface() != null) {
          rowBuilder.put(
              COL_TUNNEL_INTERFACE,
              String.format(
                  "%s:%s",
                  node.getSecond().getTunnelInterface(),
                  neighbor.getSecond().getTunnelInterface()));
        }
        if (ipsecSession.getNegotiatedIkeP1Proposal() == null) {
          rowBuilder.put(COL_STATUS, IKE_PHASE1_FAILED);
        } else if (ipsecSession.getNegotiatedIkePhase1Key() == null) {
          rowBuilder.put(COL_STATUS, IKE_PHASE1_KEY_NOT_MATCHING);
        } else if (ipsecSession.getNegotiatedIpsecPhase2Proposal() == null) {
          rowBuilder.put(COL_STATUS, IPSEC_PHASE2_FAILED);
        } else {
          rowBuilder.put(COL_STATUS, IPSEC_SESSION_ESTABLISHED);
        }
        rows.add(rowBuilder.build());
      }
    }
    return rows;
  }

  /** Create table metadata for this answer. */
  private static TableMetadata getTableMetadata() {
    List<ColumnMetadata> columnMetadata = getColumnMetadata();
    DisplayHints displayHints = new DisplayHints();
    displayHints.setTextDesc(
        String.format(
            " IPSec peering between initiator ${%s} with interface and IP ${%s} and responder ${%s} with interface and IP ${%s}s has status ${%s}.",
            COL_INITIATOR,
            COL_INIT_INTERFACE_IP,
            COL_RESPONDER,
            COL_RESPONDER_INTERFACE_IP,
            COL_TUNNEL_INTERFACE,
            COL_STATUS));
    return new TableMetadata(columnMetadata, displayHints);
  }

  /** Create column metadata. */
  private static List<ColumnMetadata> getColumnMetadata() {
    return ImmutableList.of(
        new ColumnMetadata(COL_INITIATOR, Schema.NODE, "IPSec initiator"),
        new ColumnMetadata(COL_INIT_INTERFACE_IP, Schema.NODE, "Initiator Interface & IP"),
        new ColumnMetadata(COL_RESPONDER, Schema.NODE, "IPSec responder"),
        new ColumnMetadata(COL_RESPONDER_INTERFACE_IP, Schema.NODE, "Responder Interface & IP"),
        new ColumnMetadata(COL_STATUS, Schema.STRING, "IPSec peering status"),
        new ColumnMetadata(COL_TUNNEL_INTERFACE, Schema.STRING, "Tunnel interface"));
  }

  public enum IpsecPeeringStatus {
    IPSEC_SESSION_ESTABLISHED,
    IKE_PHASE1_FAILED,
    IKE_PHASE1_KEY_NOT_MATCHING,
    IPSEC_PHASE2_FAILED,
    MISSING_END_POINT
  }
}
