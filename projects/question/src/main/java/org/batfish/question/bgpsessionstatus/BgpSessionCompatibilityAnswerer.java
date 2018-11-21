package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus.DYNAMIC_MATCH;
import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus.NO_MATCH_FOUND;
import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus.UNIQUE_MATCH;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class BgpSessionCompatibilityAnswerer extends BgpSessionAnswerer {

  public static final String COL_CONFIGURED_STATUS = "Configured_Status";

  /** Answerer for the BGP session compatibility question. */
  public BgpSessionCompatibilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    BgpSessionCompatibilityQuestion question = (BgpSessionCompatibilityQuestion) _question;
    TableAnswerElement answer =
        new TableAnswerElement(BgpSessionCompatibilityAnswerer.createMetadata(question));
    answer.postProcessAnswer(question, getRows(question));
    return answer;
  }

  /**
   * Return the answer for {@link BgpSessionCompatibilityQuestion} -- a set of BGP sessions and
   * their compatibility.
   */
  @Override
  public List<Row> getRows(BgpSessionQuestion question) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);
    Set<String> remoteNodes = question.getRemoteNodes().getMatchingNodes(_batfish);

    Map<Ip, Set<String>> ipOwners = TopologyUtil.computeIpNodeOwners(configurations, true);
    Set<Ip> allInterfaceIps = ipOwners.keySet();

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology =
        BgpTopologyUtils.initBgpTopology(configurations, ipOwners, true);

    // Keep track of active peers' statuses to assess whether passive peers have compatible remotes.
    Map<BgpPeerConfigId, ConfiguredSessionStatus> activePeerStatuses = new TreeMap<>();

    Stream<Row> activePeerRows =
        configuredBgpTopology
            .nodes()
            .stream()
            .map(
                neighbor -> {
                  BgpPeerConfig bpc = getBgpPeerConfig(configurations, neighbor);
                  if (bpc instanceof BgpPassivePeerConfig) {
                    return null;
                  } else if (!(bpc instanceof BgpActivePeerConfig)) {
                    throw new BatfishException(
                        "Unsupported type of BGP peer config (not active or passive): "
                            + bpc.getClass().getName());
                  }
                  BgpActivePeerConfig activePeer = (BgpActivePeerConfig) bpc;

                  SessionType type = BgpSessionProperties.getSessionType(activePeer);
                  ConfiguredSessionStatus configuredStatus =
                      getConfiguredStatus(
                          neighbor, activePeer, type, allInterfaceIps, configuredBgpTopology);

                  // Add neighbor's status to activePeerStatuses map. Can't filter earlier than this
                  // because all active peers need to be represented in activePeerStatuses.
                  activePeerStatuses.put(neighbor, configuredStatus);

                  // Build row. Will be null if row doesn't match question filters.
                  return buildActivePeerRow(
                      neighbor,
                      activePeer,
                      type,
                      configuredStatus,
                      nodes,
                      remoteNodes,
                      question,
                      configuredBgpTopology,
                      configurations);
                })
            .filter(Objects::nonNull);

    Stream<Row> passivePeerRows =
        configuredBgpTopology
            .nodes()
            .stream()
            .filter(neighbor -> nodes.contains(neighbor.getHostname()))
            .flatMap(
                neighbor -> {
                  BgpPeerConfig bpc = getBgpPeerConfig(configurations, neighbor);
                  if (!(bpc instanceof BgpPassivePeerConfig)) {
                    return Stream.of();
                  }
                  BgpPassivePeerConfig passivePeer = (BgpPassivePeerConfig) bpc;

                  // If peer has null remote prefix or empty remote AS list, generate one row
                  ConfiguredSessionStatus brokenStatus = getLocallyBrokenStatus(passivePeer);
                  if (brokenStatus != null) {
                    return question.matchesStatus(brokenStatus)
                        ? Stream.of(
                            buildPassivePeerWithoutRemoteRow(
                                question, neighbor, passivePeer, brokenStatus))
                        : Stream.of();
                  }

                  // Find all correctly configured remote peers compatible with this peer
                  Set<BgpPeerConfigId> remoteIds =
                      configuredBgpTopology
                          .adjacentNodes(neighbor)
                          .stream()
                          .filter(remoteId -> activePeerStatuses.get(remoteId).equals(UNIQUE_MATCH))
                          .collect(ImmutableSet.toImmutableSet());

                  // If no compatible neighbors exist, generate one NO_MATCH_FOUND row
                  if (remoteIds.isEmpty()) {
                    return question.matchesStatus(NO_MATCH_FOUND)
                        ? Stream.of(
                            buildPassivePeerWithoutRemoteRow(
                                question, neighbor, passivePeer, NO_MATCH_FOUND))
                        : Stream.of();
                  }

                  // Compatible neighbors exist. Generate a row for each.
                  return question.matchesStatus(DYNAMIC_MATCH)
                      ? remoteIds
                          .stream()
                          .filter(remoteId -> remoteNodes.contains(remoteId.getHostname()))
                          .map(
                              remoteId ->
                                  // Returns null if session type doesn't match question's filter
                                  buildDynamicMatchRow(
                                      question,
                                      neighbor,
                                      passivePeer,
                                      remoteId,
                                      (BgpActivePeerConfig)
                                          getBgpPeerConfig(configurations, remoteId),
                                      configurations))
                          .filter(Objects::nonNull)
                      : Stream.of();
                })
            .filter(Objects::nonNull);

    return Streams.concat(activePeerRows, passivePeerRows).collect(ImmutableList.toImmutableList());
  }

  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(
                COL_NODE, Schema.NODE, "The node where this session is configured", true, false),
            new ColumnMetadata(
                COL_VRF, Schema.STRING, "The VRF in which this session is configured", true, false),
            new ColumnMetadata(
                COL_LOCAL_AS, Schema.LONG, "The local AS of the session", false, false),
            new ColumnMetadata(
                COL_LOCAL_INTERFACE,
                Schema.INTERFACE,
                "Local interface of the session",
                false,
                true),
            new ColumnMetadata(
                COL_LOCAL_IP, Schema.IP, "The local IP of the session", false, false),
            new ColumnMetadata(
                COL_REMOTE_AS,
                Schema.SELF_DESCRIBING,
                "The remote AS of the session",
                false,
                false),
            new ColumnMetadata(
                COL_REMOTE_NODE, Schema.NODE, "Remote node for this session", false, false),
            new ColumnMetadata(
                COL_REMOTE_IP, Schema.SELF_DESCRIBING, "Remote IP for this session", true, false),
            new ColumnMetadata(
                COL_SESSION_TYPE, Schema.STRING, "The type of this session", false, false),
            new ColumnMetadata(
                COL_CONFIGURED_STATUS, Schema.STRING, "Configured status", false, true));

    String textDesc =
        String.format(
            "On ${%s} session ${%s}:${%s} has configured status ${%s}.",
            COL_NODE, COL_VRF, COL_REMOTE_IP, COL_CONFIGURED_STATUS);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadata, textDesc);
  }

  private @Nullable Row buildActivePeerRow(
      BgpPeerConfigId activeId,
      BgpActivePeerConfig activePeer,
      SessionType type,
      ConfiguredSessionStatus status,
      Set<String> nodes,
      Set<String> remoteNodes,
      BgpSessionQuestion question,
      ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology,
      Map<String, Configuration> configurations) {
    if (!nodes.contains(activeId.getHostname())
        || !question.matchesType(type)
        || !question.matchesStatus(status)) {
      return null;
    }
    Node remoteNode = null;
    if (status == UNIQUE_MATCH) {
      String remoteNodeName = bgpTopology.adjacentNodes(activeId).iterator().next().getHostname();
      if (!remoteNodes.contains(remoteNodeName)) {
        return null;
      }
      remoteNode = new Node(remoteNodeName);
    }

    Ip localIp = activePeer.getLocalIp();
    NodeInterfacePair localInterface =
        getInterface(configurations.get(activeId.getHostname()), localIp);

    return Row.builder(createMetadata(question).toColumnMap())
        .put(COL_CONFIGURED_STATUS, status)
        .put(COL_LOCAL_INTERFACE, localInterface)
        .put(COL_LOCAL_AS, activePeer.getLocalAs())
        .put(COL_LOCAL_IP, activePeer.getLocalIp())
        .put(COL_NODE, new Node(activeId.getHostname()))
        .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, activePeer.getRemoteAs()))
        .put(COL_REMOTE_NODE, remoteNode)
        .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, activePeer.getPeerAddress()))
        .put(COL_SESSION_TYPE, type)
        .put(COL_VRF, activeId.getVrfName())
        .build();
  }

  private @Nonnull Row buildPassivePeerWithoutRemoteRow(
      BgpSessionQuestion question,
      BgpPeerConfigId passiveId,
      BgpPassivePeerConfig passivePeer,
      ConfiguredSessionStatus status) {
    return Row.builder(createMetadata(question).toColumnMap())
        .put(COL_CONFIGURED_STATUS, status)
        .put(COL_LOCAL_INTERFACE, null)
        .put(COL_LOCAL_AS, passivePeer.getLocalAs())
        .put(COL_LOCAL_IP, passivePeer.getLocalIp())
        .put(COL_NODE, new Node(passiveId.getHostname()))
        .put(
            COL_REMOTE_AS,
            new SelfDescribingObject(Schema.list(Schema.LONG), passivePeer.getRemoteAs()))
        .put(COL_REMOTE_NODE, null)
        .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.PREFIX, passivePeer.getPeerPrefix()))
        .put(COL_SESSION_TYPE, SessionType.UNSET)
        .put(COL_VRF, passiveId.getVrfName())
        .build();
  }

  private @Nullable Row buildDynamicMatchRow(
      BgpSessionQuestion question,
      BgpPeerConfigId passiveId,
      BgpPassivePeerConfig passivePeer,
      BgpPeerConfigId activeId,
      BgpActivePeerConfig activePeer,
      Map<String, Configuration> configurations) {
    SessionType type = BgpSessionProperties.getSessionType(activePeer);
    if (!question.matchesType(type)) {
      return null;
    }
    Ip localIp = activePeer.getPeerAddress();
    NodeInterfacePair localInterface =
        getInterface(configurations.get(passiveId.getHostname()), localIp);
    return Row.builder(createMetadata(question).toColumnMap())
        .put(COL_CONFIGURED_STATUS, DYNAMIC_MATCH)
        .put(COL_LOCAL_INTERFACE, localInterface)
        .put(COL_LOCAL_AS, passivePeer.getLocalAs())
        .put(COL_LOCAL_IP, localIp)
        .put(COL_NODE, new Node(passiveId.getHostname()))
        .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, activePeer.getLocalAs()))
        .put(COL_REMOTE_NODE, new Node(activeId.getHostname()))
        .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, activePeer.getLocalIp()))
        .put(COL_SESSION_TYPE, type)
        .put(COL_VRF, passiveId.getVrfName())
        .build();
  }
}
