package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionAnswerer.ConfiguredSessionStatus.UNIQUE_MATCH;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    return configuredBgpTopology
        .nodes()
        .stream()
        .filter(neighbor -> nodes.contains(neighbor.getHostname()))
        .map(
            neighbor -> {
              BgpPeerConfig bpc = getBgpPeerConfig(configurations, neighbor);
              if (bpc instanceof BgpPassivePeerConfig) {
                return null;
              } else if (!(bpc instanceof BgpActivePeerConfig)) {
                throw new BatfishException(
                    "Unsupported type of BGP peer config (not active or passive)");
              }
              BgpActivePeerConfig bgpPeerConfig = (BgpActivePeerConfig) bpc;
              SessionType type = BgpSessionProperties.getSessionType(bgpPeerConfig);
              if (!question.matchesType(type)) {
                return null;
              }

              // Local IP and interface
              Ip localIp = bgpPeerConfig.getLocalIp();
              NodeInterfacePair localInterface =
                  getInterface(configurations.get(neighbor.getHostname()), localIp);

              ConfiguredSessionStatus configuredStatus =
                  getConfiguredStatus(
                      neighbor, bgpPeerConfig, type, allInterfaceIps, configuredBgpTopology);

              if (!question.matchesStatus(configuredStatus)) {
                return null;
              }

              Node remoteNode = null;
              if (configuredStatus == UNIQUE_MATCH) {
                String remoteNodeName =
                    configuredBgpTopology.adjacentNodes(neighbor).iterator().next().getHostname();
                if (!remoteNodes.contains(remoteNodeName)) {
                  return null;
                }
                remoteNode = new Node(remoteNodeName);
              }

              return Row.builder(createMetadata(question).toColumnMap())
                  .put(COL_CONFIGURED_STATUS, configuredStatus)
                  .put(COL_LOCAL_INTERFACE, localInterface)
                  .put(COL_LOCAL_AS, bgpPeerConfig.getLocalAs())
                  .put(COL_LOCAL_IP, bgpPeerConfig.getLocalIp())
                  .put(COL_NODE, new Node(neighbor.getHostname()))
                  .put(COL_REMOTE_AS, bgpPeerConfig.getRemoteAs())
                  .put(COL_REMOTE_NODE, remoteNode)
                  .put(COL_REMOTE_IP, neighbor.getRemotePeerPrefix().getStartIp())
                  .put(COL_SESSION_TYPE, type)
                  .put(COL_VRF, neighbor.getVrfName())
                  .build();
            })
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
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
                COL_REMOTE_AS, Schema.LONG, "The remote AS of the session", false, false),
            new ColumnMetadata(
                COL_REMOTE_NODE, Schema.NODE, "Remote node for this session", false, false),
            new ColumnMetadata(COL_REMOTE_IP, Schema.IP, "Remote IP for this session", true, false),
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
}
