package org.batfish.question.ipsecpeers;

import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IKE_PHASE1_FAILED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IKE_PHASE1_KEY_MISMATCH;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IPSEC_PHASE2_FAILED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.IPSEC_SESSION_ESTABLISHED;
import static org.batfish.question.ipsecpeers.IpsecPeeringInfo.IpsecPeeringStatus.MISSING_END_POINT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

class IpsecPeersAnswerer extends Answerer {
  static final String COL_INITIATOR = "Initiator";
  static final String COL_INIT_INTERFACE_IP = "InitiatorInterfaceAndIp";
  static final String COL_RESPONDER = "Responder";
  static final String COL_RESPONDER_INTERFACE_IP = "ResponderInterfaceAndIp";
  static final String COL_STATUS = "status";
  static final String COL_TUNNEL_INTERFACE = "TunnelInterface";
  private static final String NOT_APPLICABLE = "Not Applicable";

  IpsecPeersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    IpsecPeersQuestion question = (IpsecPeersQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    ValueGraph<Pair<String, IpsecPeerConfig>, IpsecSession> ipsecTopology =
        CommonUtil.initIpsecTopology(configurations);

    Set<String> initiatorNodes = question.getInitiatorRegex().getMatchingNodes(_batfish);
    Set<String> responderNodes = question.getResponderRegex().getMatchingNodes(_batfish);

    TableAnswerElement answerElement = new TableAnswerElement(getTableMetadata());

    Multiset<IpsecPeeringInfo> ipsecPeerings =
        rawAnswer(ipsecTopology, initiatorNodes, responderNodes);
    answerElement.postProcessAnswer(
        question,
        ipsecPeerings
            .stream()
            .filter(
                ipsecPeeringInfo ->
                    question.matchesStatus(ipsecPeeringInfo.getIpsecPeeringStatus()))
            .map(IpsecPeersAnswerer::toRow)
            .collect(Collectors.toCollection(HashMultiset::create)));
    return answerElement;
  }

  @VisibleForTesting
  static Multiset<IpsecPeeringInfo> rawAnswer(
      ValueGraph<Pair<String, IpsecPeerConfig>, IpsecSession> ipsecTopology,
      Set<String> initiatorNodes,
      Set<String> responderNodes) {
    Multiset<IpsecPeeringInfo> ipsecPeeringsInfos = HashMultiset.create();

    for (Pair<String, IpsecPeerConfig> node : ipsecTopology.nodes()) {
      if (node.getSecond() instanceof IpsecDynamicPeerConfig
          || !initiatorNodes.contains(node.getFirst())) {
        continue;
      }
      IpsecPeeringInfo.Builder ipsecPeeringInfoBuilder = IpsecPeeringInfo.builder();

      ipsecPeeringInfoBuilder.setInitiatorHostname(node.getFirst());
      ipsecPeeringInfoBuilder.setInitiatorInterface(node.getSecond().getPhysicalInterface());
      ipsecPeeringInfoBuilder.setInitiatorIp(node.getSecond().getLocalAddress());
      ipsecPeeringInfoBuilder.setInitiatorTunnelInterface(node.getSecond().getTunnelInterface());

      Set<Pair<String, IpsecPeerConfig>> neigbors = ipsecTopology.adjacentNodes(node);

      if (neigbors.isEmpty()) {
        ipsecPeeringInfoBuilder.setIpsecPeeringStatus(MISSING_END_POINT);
        ipsecPeeringsInfos.add(ipsecPeeringInfoBuilder.build());
        continue;
      }

      for (Pair<String, IpsecPeerConfig> neighbor : neigbors) {
        if (!responderNodes.contains(neighbor.getFirst())) {
          continue;
        }
        IpsecSession ipsecSession = ipsecTopology.edgeValueOrDefault(node, neighbor, null);
        if (ipsecSession == null) {
          continue;
        }
        processNeighbor(ipsecPeeringInfoBuilder, neighbor, ipsecSession);
        ipsecPeeringsInfos.add(ipsecPeeringInfoBuilder.build());
      }
    }
    return ipsecPeeringsInfos;
  }

  private static void processNeighbor(
      IpsecPeeringInfo.Builder ipsecPeeringInfoBuilder,
      Pair<String, IpsecPeerConfig> neighbor,
      IpsecSession ipsecSession) {

    ipsecPeeringInfoBuilder.setResponderHostname(neighbor.getFirst());
    ipsecPeeringInfoBuilder.setResponderInterface(neighbor.getSecond().getPhysicalInterface());
    ipsecPeeringInfoBuilder.setResponderIp(neighbor.getSecond().getLocalAddress());
    ipsecPeeringInfoBuilder.setResponderTunnelInterface(neighbor.getSecond().getTunnelInterface());

    if (ipsecSession.getNegotiatedIkeP1Proposal() == null) {
      ipsecPeeringInfoBuilder.setIpsecPeeringStatus(IKE_PHASE1_FAILED);
    } else if (ipsecSession.getNegotiatedIkeP1Key() == null) {
      ipsecPeeringInfoBuilder.setIpsecPeeringStatus(IKE_PHASE1_KEY_MISMATCH);
    } else if (ipsecSession.getNegotiatedIpsecP2Proposal() == null) {
      ipsecPeeringInfoBuilder.setIpsecPeeringStatus(IPSEC_PHASE2_FAILED);
    } else {
      ipsecPeeringInfoBuilder.setIpsecPeeringStatus(IPSEC_SESSION_ESTABLISHED);
    }
  }

  /**
   * Creates a {@link Row} object from the corresponding {@link IpsecPeeringInfo} object.
   *
   * @param info input {@link IpsecPeeringInfo}
   * @return The output {@link Row}
   */
  public static Row toRow(@Nonnull IpsecPeeringInfo info) {
    RowBuilder row = Row.builder();
    row.put(COL_INITIATOR, new Node(info.getInitiatorHostname()))
        .put(
            COL_INIT_INTERFACE_IP,
            String.format("%s:%s", info.getInitiatorInterface(), info.getInitiatorIp()))
        .put(COL_RESPONDER, new Node(info.getResponderHostname()))
        .put(
            COL_RESPONDER_INTERFACE_IP,
            String.format("%s:%s", info.getResponderInterface(), info.getResponderIp()))
        .put(
            COL_TUNNEL_INTERFACE,
            info.getInitiatorTunnelInterface() != null && info.getResponderTunnelInterface() != null
                ? String.format(
                    "%s->%s",
                    info.getInitiatorTunnelInterface(), info.getResponderTunnelInterface())
                : NOT_APPLICABLE)
        .put(COL_STATUS, info.getIpsecPeeringStatus());
    return row.build();
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
}
