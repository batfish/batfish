package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;

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
  public BgpSessionStatusAnswerElement(
      @Nonnull @JsonProperty(PROP_METADATA) TableMetadata tableMetadata) {
    super(tableMetadata);
  }

  public static TableMetadata createMetadata(Question question) {
    Map<String, Schema> columnSchemas = new HashMap<>();
    columnSchemas.put(COL_CONFIGURED_STATUS, new Schema("String"));
    columnSchemas.put(COL_ESTABLISHED_NEIGHBORS, new Schema("Integer"));
    columnSchemas.put(COL_LOCAL_IP, new Schema("Ip"));
    columnSchemas.put(COL_NODE, new Schema("Node"));
    columnSchemas.put(COL_ON_LOOPBACK, new Schema("Boolean"));
    columnSchemas.put(COL_REMOTE_NODE, new Schema("Node"));
    columnSchemas.put(COL_REMOTE_PREFIX, new Schema("Prefix"));
    columnSchemas.put(COL_SESSION_TYPE, new Schema("String"));
    columnSchemas.put(COL_VRF_NAME, new Schema("String"));

    List<String> primaryKey = new LinkedList<>();
    primaryKey.add(COL_NODE);
    primaryKey.add(COL_REMOTE_PREFIX);
    primaryKey.add(COL_VRF_NAME);

    List<String> primaryValue = new LinkedList<>();
    primaryValue.add(COL_CONFIGURED_STATUS);
    primaryValue.add(COL_ESTABLISHED_NEIGHBORS);

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(
          String.format(
              "On ${%s} session ${%s}:${%s} has configured status ${%s}.",
              COL_NODE,
              COL_VRF_NAME,
              COL_REMOTE_PREFIX,
              COL_CONFIGURED_STATUS,
              COL_ESTABLISHED_NEIGHBORS));
    }
    return new TableMetadata(columnSchemas, primaryKey, primaryValue, dhints);
  }

  @Override
  public Object fromRow(ObjectNode row) throws JsonProcessingException {
    return fromRowStatic(row);
  }

  public static BgpSessionInfo fromRowStatic(ObjectNode row) throws JsonProcessingException {
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

  @Override
  public ObjectNode toRow(Object o) {
    return toRowStatic((BgpSessionInfo) o);
  }

  public static ObjectNode toRowStatic(BgpSessionInfo info) {
    ObjectNode row = BatfishObjectMapper.mapper().createObjectNode();
    row.set(
        COL_CONFIGURED_STATUS,
        BatfishObjectMapper.mapper().valueToTree(info.getConfiguredStatus()));
    row.set(
        COL_ESTABLISHED_NEIGHBORS,
        BatfishObjectMapper.mapper().valueToTree(info.getEstablishedNeighbors()));
    row.set(COL_LOCAL_IP, BatfishObjectMapper.mapper().valueToTree(info.getLocalIp()));
    row.set(COL_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info.getNodeName())));
    row.set(COL_ON_LOOPBACK, BatfishObjectMapper.mapper().valueToTree(info.getOnLoopback()));
    row.set(
        COL_REMOTE_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info.getRemoteNode())));
    row.set(COL_REMOTE_PREFIX, BatfishObjectMapper.mapper().valueToTree(info.getRemotePrefix()));
    row.set(COL_SESSION_TYPE, BatfishObjectMapper.mapper().valueToTree(info.getSessionType()));
    row.set(COL_VRF_NAME, BatfishObjectMapper.mapper().valueToTree(info.getVrfName()));
    return row;
  }
}
