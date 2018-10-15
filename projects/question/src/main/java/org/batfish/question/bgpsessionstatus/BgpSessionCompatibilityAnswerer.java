package org.batfish.question.bgpsessionstatus;

import static org.batfish.question.bgpsessionstatus.BgpSessionInfo.getBgpSessionInfo;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.graph.ValueGraph;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class BgpSessionCompatibilityAnswerer extends Answerer {

  public static final String COL_CONFIGURED_STATUS = "Configured_Status";
  public static final String COL_LOCAL_INTERFACE = "Local_Interface";
  public static final String COL_LOCAL_IP = "Local_IP";
  public static final String COL_NODE = "Node";
  public static final String COL_REMOTE_NODE = "Remote_Node";
  public static final String COL_REMOTE_PREFIX = "Remote_Prefix";
  public static final String COL_SESSION_TYPE = "Session_Type";
  public static final String COL_VRF_NAME = "VRF";

  /** Answerer for the BGP session compatibility question. */
  public BgpSessionCompatibilityAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    BgpSessionCompatibilityQuestion question = (BgpSessionCompatibilityQuestion) _question;
    Multiset<BgpSessionInfo> sessions = rawAnswer(question);
    TableAnswerElement answer =
        new TableAnswerElement(BgpSessionCompatibilityAnswerer.createMetadata(question));
    answer.postProcessAnswer(
        question,
        sessions
            .stream()
            .map(BgpSessionCompatibilityAnswerer::toRow)
            .collect(Collectors.toCollection(HashMultiset::create)));
    return answer;
  }

  /**
   * Return the answer for {@link BgpSessionCompatibilityQuestion} -- a set of BGP sessions and
   * their compatibility.
   */
  public Multiset<BgpSessionInfo> rawAnswer(BgpSessionCompatibilityQuestion question) {
    Multiset<BgpSessionInfo> sessions = HashMultiset.create();
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes1 = question.getNodes().getMatchingNodes(_batfish);
    Set<String> includeNodes2 = question.getRemoteNodes().getMatchingNodes(_batfish);

    Map<Ip, Set<String>> ipOwners = TopologyUtil.computeIpNodeOwners(configurations, true);
    Set<Ip> allInterfaceIps = ipOwners.keySet();

    ValueGraph<BgpPeerConfigId, BgpSessionProperties> configuredBgpTopology =
        BgpTopologyUtils.initBgpTopology(configurations, ipOwners, true);

    sessions.addAll(
        configuredBgpTopology
            .nodes()
            .stream()
            .map(
                neighbor ->
                    getBgpSessionInfo(
                        question,
                        configurations,
                        includeNodes1,
                        includeNodes2,
                        ipOwners,
                        allInterfaceIps,
                        configuredBgpTopology,
                        null,
                        neighbor))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableList.toImmutableList()));

    return sessions;
  }

  public static TableMetadata createMetadata(Question question) {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(
                COL_NODE, Schema.NODE, "The node where this session is configured", true, false),
            new ColumnMetadata(
                COL_LOCAL_IP, Schema.IP, "The local IP of the session", false, false),
            new ColumnMetadata(
                COL_VRF_NAME,
                Schema.STRING,
                "The VRF in which this session is configured",
                true,
                false),
            new ColumnMetadata(
                COL_LOCAL_INTERFACE,
                Schema.INTERFACE,
                "Local interface of the session",
                false,
                true),
            new ColumnMetadata(
                COL_REMOTE_NODE, Schema.NODE, "Remote node for this session", false, false),
            new ColumnMetadata(
                COL_REMOTE_PREFIX, Schema.PREFIX, "Remote prefix for this session", true, false),
            new ColumnMetadata(
                COL_SESSION_TYPE, Schema.STRING, "The type of this session", false, false),
            new ColumnMetadata(
                COL_CONFIGURED_STATUS, Schema.STRING, "Configured status", false, true));

    String textDesc =
        String.format(
            "On ${%s} session ${%s}:${%s} has configured status ${%s}.",
            COL_NODE, COL_VRF_NAME, COL_REMOTE_PREFIX, COL_CONFIGURED_STATUS);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadata, textDesc);
  }

  /**
   * Creates a {@link Row} object from the corresponding {@link BgpSessionInfo} object.
   *
   * @param info The input object
   * @return The output row
   */
  public static Row toRow(@Nonnull BgpSessionInfo info) {
    RowBuilder row = Row.builder();
    row.put(COL_CONFIGURED_STATUS, info.getConfiguredStatus())
        .put(COL_LOCAL_INTERFACE, info.getLocalInterface())
        .put(COL_LOCAL_IP, info.getLocalIp())
        .put(COL_NODE, new Node(info.getNodeName()))
        .put(COL_REMOTE_NODE, info.getRemoteNode() == null ? null : new Node(info.getRemoteNode()))
        .put(COL_REMOTE_PREFIX, info.getRemotePrefix())
        .put(COL_SESSION_TYPE, info.getSessionType())
        .put(COL_VRF_NAME, info.getVrfName());
    return row.build();
  }
}
