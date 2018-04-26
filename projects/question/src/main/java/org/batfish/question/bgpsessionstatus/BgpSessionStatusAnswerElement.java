package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;

public class BgpSessionStatusAnswerElement extends TableAnswerElement {

  private static final String COL_CONFIGURED_STATUS = "configuredStatus";
  private static final String COL_ESTABLISHED_NEIGHBORS = "establishedNeighbors";
  private static final String COL_LOCAL_IP = "localIp";
  private static final String COL_NODE = "node";
  private static final String COL_ON_LOOPBACK = "onLoopback";
  private static final String COL_REMOTE_NODE = "remoteNode";
  private static final String COL_REMOTE_PREFIX = "remotePrefix";
  private static final String COL_SESSION_TYPE = "sessionType";
  private static final String COL_VRF_NAME = "vrfName";

  @JsonCreator
  public BgpSessionStatusAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public static BgpSessionStatusAnswerElement create(BgpSessionStatusQuestion question) {
    Map<String, Schema> columnSchemas =
        new ImmutableMap.Builder<String, Schema>()
            .put(COL_CONFIGURED_STATUS, new Schema("String"))
            .put(COL_ESTABLISHED_NEIGHBORS, new Schema("Integer"))
            .put(COL_LOCAL_IP, new Schema("Ip"))
            .put(COL_NODE, new Schema("Node"))
            .put(COL_ON_LOOPBACK, new Schema("Boolean"))
            .put(COL_REMOTE_NODE, new Schema("Node"))
            .put(COL_REMOTE_PREFIX, new Schema("Prefix"))
            .put(COL_SESSION_TYPE, new Schema("String"))
            .put(COL_VRF_NAME, new Schema("String"))
            .build();
    List<String> primaryKey =
        new ImmutableList.Builder<String>()
            .add(COL_NODE)
            .add(COL_REMOTE_PREFIX)
            .add(COL_VRF_NAME)
            .build();
    List<String> primaryValue =
        new ImmutableList.Builder<String>()
            .add(COL_CONFIGURED_STATUS)
            .add(COL_ESTABLISHED_NEIGHBORS)
            .build();
    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} session ${%s}:${%s} has configured status ${%s}"
                  + " and established count ${%s}",
              COL_NODE,
              COL_VRF_NAME,
              COL_REMOTE_PREFIX,
              COL_CONFIGURED_STATUS,
              COL_ESTABLISHED_NEIGHBORS));
    }
    TableMetadata metadata = new TableMetadata(columnSchemas, primaryKey, primaryValue, dhints);
    return new BgpSessionStatusAnswerElement(metadata);
  }

  public static BgpSessionInfo fromRow(ObjectNode row) throws JsonProcessingException {
    Ip localIp = BatfishObjectMapper.mapper().treeToValue(row.get(COL_LOCAL_IP), Ip.class);
    SessionStatus status =
        BatfishObjectMapper.mapper()
            .treeToValue(row.get(COL_CONFIGURED_STATUS), SessionStatus.class);
    Integer establishedNeighbors =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_ESTABLISHED_NEIGHBORS), Integer.class);
    Boolean onLoopback =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_ON_LOOPBACK), Boolean.class);
    Node node = BatfishObjectMapper.mapper().treeToValue(row.get(COL_NODE), Node.class);
    Node remoteNode =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_REMOTE_NODE), Node.class);
    Prefix remotePrefix =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_REMOTE_PREFIX), Prefix.class);
    SessionType sessionType =
        BatfishObjectMapper.mapper().treeToValue(row.get(COL_SESSION_TYPE), SessionType.class);
    String vrfName = BatfishObjectMapper.mapper().treeToValue(row.get(COL_VRF_NAME), String.class);

    return new BgpSessionInfo(
        node.getName(),
        vrfName,
        remotePrefix,
        localIp,
        onLoopback,
        remoteNode.getName(),
        status,
        establishedNeighbors,
        sessionType);
  }

  public static ObjectNode toRow(BgpSessionInfo info) {
    ObjectNode row = BatfishObjectMapper.mapper().createObjectNode();
    row.set(
        COL_CONFIGURED_STATUS, BatfishObjectMapper.mapper().valueToTree(info._configuredStatus));
    row.set(
        COL_ESTABLISHED_NEIGHBORS,
        BatfishObjectMapper.mapper().valueToTree(info._establishedNeighbors));
    row.set(COL_LOCAL_IP, BatfishObjectMapper.mapper().valueToTree(info._localIp));
    row.set(COL_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info.getNodeName())));
    row.set(COL_ON_LOOPBACK, BatfishObjectMapper.mapper().valueToTree(info._onLoopback));
    row.set(COL_REMOTE_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info._remoteNode)));
    row.set(COL_REMOTE_PREFIX, BatfishObjectMapper.mapper().valueToTree(info.getRemotePrefix()));
    row.set(COL_SESSION_TYPE, BatfishObjectMapper.mapper().valueToTree(info._sessionType));
    row.set(COL_VRF_NAME, BatfishObjectMapper.mapper().valueToTree(info.getVrfName()));
    return row;
  }
}
