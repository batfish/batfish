package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;

@ParametersAreNonnullByDefault
public class BgpSessionStatusAnswerElement extends TableAnswerElement {

  public static final String COL_CONFIGURED_STATUS = "configuredStatus";
  public static final String COL_ESTABLISHED_NEIGHBORS = "establishedNeighbors";
  public static final String COL_LOCAL_IP = "localIp";
  public static final String COL_NODE = "node";
  public static final String COL_ON_LOOPBACK = "onLoopback";
  public static final String COL_REMOTE_NODE = "remoteNode";
  public static final String COL_REMOTE_PREFIX = "remotePrefix";
  public static final String COL_SESSION_TYPE = "sessionType";
  public static final String COL_VRF_NAME = "vrfName";

  @JsonCreator
  public BgpSessionStatusAnswerElement(@JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
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
                COL_REMOTE_NODE, Schema.NODE, "Remote node for this session", false, false),
            new ColumnMetadata(
                COL_REMOTE_PREFIX, Schema.PREFIX, "Remote prefix for this session", true, false),
            new ColumnMetadata(
                COL_SESSION_TYPE, Schema.STRING, "The type of this session", false, false),
            new ColumnMetadata(
                COL_CONFIGURED_STATUS, Schema.STRING, "Configured status", false, true),
            new ColumnMetadata(
                COL_ESTABLISHED_NEIGHBORS,
                Schema.INTEGER,
                "Number of neighbors with whom BGP session was established",
                false,
                true),
            new ColumnMetadata(
                COL_ON_LOOPBACK,
                Schema.BOOLEAN,
                "Whether the session was established on loopback interface",
                false,
                true));

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} session ${%s}:${%s} has configured status ${%s}.",
              COL_NODE, COL_VRF_NAME, COL_REMOTE_PREFIX, COL_CONFIGURED_STATUS));
    }
    return new TableMetadata(columnMetadata, dhints);
  }

  /**
   * Creates a {@link BgpSessionInfo} object from the corresponding {@link Row} object.
   *
   * @param row The input row
   * @return The output object
   */
  public static BgpSessionInfo fromRow(Row row) {
    Ip localIp = row.get(COL_LOCAL_IP, Ip.class);
    SessionStatus configuredStatus = row.get(COL_CONFIGURED_STATUS, SessionStatus.class);
    Integer establishedNeighbors = row.get(COL_ESTABLISHED_NEIGHBORS, Integer.class);
    Boolean onLoopback = row.get(COL_ON_LOOPBACK, Boolean.class);
    Node node = row.get(COL_NODE, Node.class);
    Node remoteNode = row.get(COL_REMOTE_NODE, Node.class);
    Prefix remotePrefix = row.get(COL_REMOTE_PREFIX, Prefix.class);
    SessionType sessionType = row.get(COL_SESSION_TYPE, SessionType.class);
    String vrfName = row.get(COL_VRF_NAME, String.class);

    return new BgpSessionInfo(
        configuredStatus,
        establishedNeighbors,
        node.getName(),
        localIp,
        onLoopback,
        remoteNode.getName(),
        remotePrefix,
        sessionType,
        vrfName);
  }

  /**
   * Creates a {@link Row} object from the corresponding {@link BgpSessionInfo} object.
   *
   * @param info The input object
   * @return The output row
   */
  public static Row toRow(BgpSessionInfo info) {
    RowBuilder row = Row.builder();
    row.put(COL_CONFIGURED_STATUS, info.getConfiguredStatus())
        .put(COL_ESTABLISHED_NEIGHBORS, info.getEstablishedNeighbors())
        .put(COL_LOCAL_IP, info.getLocalIp())
        .put(COL_NODE, new Node(info.getNodeName()))
        .put(COL_ON_LOOPBACK, info.getOnLoopback())
        .put(COL_REMOTE_NODE, info.getRemoteNode() == null ? null : new Node(info.getRemoteNode()))
        .put(COL_REMOTE_PREFIX, info.getRemotePrefix())
        .put(COL_SESSION_TYPE, info.getSessionType())
        .put(COL_VRF_NAME, info.getVrfName());
    return row.build();
  }
}
