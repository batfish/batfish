package org.batfish.question.bgpsessionstatus;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class BgpSessionStatusAnswerer extends BgpSessionAnswerer {

  public static final String COL_ESTABLISHED_STATUS = "Established_Status";

  /** Answerer for the BGP Session status question (new version). */
  public BgpSessionStatusAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    BgpSessionStatusQuestion question = (BgpSessionStatusQuestion) _question;
    TableAnswerElement answer =
        new TableAnswerElement(BgpSessionStatusAnswerer.createMetadata(question));
    answer.postProcessAnswer(question, getRows(question));
    return answer;
  }

  /**
   * Return the answer for {@link BgpSessionStatusQuestion} -- a set of BGP sessions and their
   * status.
   */
  @Override
  public List<Row> getRows(BgpSessionQuestion question) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);
    Set<String> remoteNodes = question.getRemoteNodes().getMatchingNodes(_batfish);
    Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpNodeOwners(configurations, true);
    Set<Ip> allInterfaceIps = ipOwners.keySet();

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology =
        CommonUtil.initBgpTopology(configurations, ipOwners, true);

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> establishedBgpTopology;
    DataPlane dp = _batfish.loadDataPlane();
    establishedBgpTopology =
        CommonUtil.initBgpTopology(
            configurations,
            ipOwners,
            false,
            true,
            _batfish.getDataPlanePlugin().getTracerouteEngine(),
            dp);

    return configuredBgpTopology
        .edges()
        .stream()
        .filter(
            // Filter out edges to/from excluded nodes
            edge ->
                nodes.contains(edge.source().getHostname())
                    && remoteNodes.contains(edge.target().getHostname()))
        .map(
            edge -> {
              BgpPeerConfigId id = edge.source();
              BgpPeerConfig bgpPeerConfig = getBgpPeerConfig(configurations, id);
              if (bgpPeerConfig == null) {
                return null;
              }
              SessionType type = getSessionType(bgpPeerConfig);
              if (!question.matchesType(type)) {
                return null;
              }

              // Remote AS
              SelfDescribingObject remoteAsEntry = null;
              if (bgpPeerConfig instanceof BgpPassivePeerConfig) {
                remoteAsEntry =
                    new SelfDescribingObject(
                        Schema.list(Schema.LONG),
                        ((BgpPassivePeerConfig) bgpPeerConfig).getRemoteAs());
              } else if (bgpPeerConfig instanceof BgpActivePeerConfig) {
                remoteAsEntry =
                    new SelfDescribingObject(
                        Schema.LONG, ((BgpActivePeerConfig) bgpPeerConfig).getRemoteAs());
              } else {
                throw new BatfishException(
                    "Unsupported type of BGP peer config (not active or passive)");
              }

              // Local IP and interface
              Ip localIp = bgpPeerConfig.getLocalIp();
              NodeInterfacePair localInterface =
                  getInterface(configurations.get(id.getHostname()), localIp);

              ConfiguredSessionStatus configuredStatus =
                  getConfiguredStatus(
                      id, bgpPeerConfig, type, allInterfaceIps, configuredBgpTopology);

              // Established status
              SessionStatus establishedStatus = SessionStatus.NOT_ESTABLISHED;
              if (!isCompatible(configuredStatus)) {
                establishedStatus = SessionStatus.NOT_COMPATIBLE;
              } else if (establishedBgpTopology.edges().contains(edge)) {
                establishedStatus = SessionStatus.ESTABLISHED;
              }
              if (!question.matchesStatus(establishedStatus)) {
                return null;
              }

              return Row.builder(createMetadata(question).toColumnMap())
                  .put(COL_ESTABLISHED_STATUS, establishedStatus)
                  .put(COL_LOCAL_INTERFACE, localInterface)
                  .put(COL_LOCAL_AS, bgpPeerConfig.getLocalAs())
                  .put(COL_LOCAL_IP, bgpPeerConfig.getLocalIp())
                  .put(COL_NODE, new Node(id.getHostname()))
                  .put(COL_REMOTE_AS, remoteAsEntry)
                  .put(COL_REMOTE_NODE, new Node(edge.target().getHostname()))
                  .put(COL_REMOTE_IP, getRemoteIpEntry(id.getRemotePeerPrefix()))
                  .put(COL_SESSION_TYPE, type)
                  .put(COL_VRF, id.getVrfName())
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
                COL_ESTABLISHED_STATUS, Schema.STRING, "Established status", false, true));

    String textDesc =
        String.format(
            "On ${%s} session ${%s}:${%s} has status ${%s}.",
            COL_NODE, COL_VRF, COL_REMOTE_IP, COL_ESTABLISHED_STATUS);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadata, textDesc);
  }
}
