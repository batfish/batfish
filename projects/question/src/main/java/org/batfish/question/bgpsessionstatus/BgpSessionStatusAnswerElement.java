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
import org.batfish.datamodel.table.Row;
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
  public Object fromRow(Row row) throws JsonProcessingException {
    return fromRowStatic(row);
  }

  public static BgpSessionInfo fromRowStatic(Row row) throws JsonProcessingException {
    ObjectNode data = row.getData();
    Ip localIp = BatfishObjectMapper.mapper().treeToValue(data.get(COL_LOCAL_IP), Ip.class);
    SessionStatus configuredStatus =
        BatfishObjectMapper.mapper()
            .treeToValue(data.get(COL_CONFIGURED_STATUS), SessionStatus.class);
    Integer establishedNeighbors =
        BatfishObjectMapper.mapper()
            .treeToValue(data.get(COL_ESTABLISHED_NEIGHBORS), Integer.class);
    Boolean onLoopback =
        BatfishObjectMapper.mapper().treeToValue(data.get(COL_ON_LOOPBACK), Boolean.class);
    Node node = BatfishObjectMapper.mapper().treeToValue(data.get(COL_NODE), Node.class);
    Node remoteNode =
        BatfishObjectMapper.mapper().treeToValue(data.get(COL_REMOTE_NODE), Node.class);
    Prefix remotePrefix =
        BatfishObjectMapper.mapper().treeToValue(data.get(COL_REMOTE_PREFIX), Prefix.class);
    SessionType sessionType =
        BatfishObjectMapper.mapper().treeToValue(data.get(COL_SESSION_TYPE), SessionType.class);
    String vrfName = BatfishObjectMapper.mapper().treeToValue(data.get(COL_VRF_NAME), String.class);

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

  @Override
  public Row toRow(Object o) {
    return toRowStatic((BgpSessionInfo) o);
  }

  public static Row toRowStatic(BgpSessionInfo info) {
    ObjectNode data = BatfishObjectMapper.mapper().createObjectNode();
    data.set(
        COL_CONFIGURED_STATUS,
        BatfishObjectMapper.mapper().valueToTree(info.getConfiguredStatus()));
    data.set(
        COL_ESTABLISHED_NEIGHBORS,
        BatfishObjectMapper.mapper().valueToTree(info.getEstablishedNeighbors()));
    data.set(COL_LOCAL_IP, BatfishObjectMapper.mapper().valueToTree(info.getLocalIp()));
    data.set(COL_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info.getNodeName())));
    data.set(COL_ON_LOOPBACK, BatfishObjectMapper.mapper().valueToTree(info.getOnLoopback()));
    data.set(
        COL_REMOTE_NODE, BatfishObjectMapper.mapper().valueToTree(new Node(info.getRemoteNode())));
    data.set(COL_REMOTE_PREFIX, BatfishObjectMapper.mapper().valueToTree(info.getRemotePrefix()));
    data.set(COL_SESSION_TYPE, BatfishObjectMapper.mapper().valueToTree(info.getSessionType()));
    data.set(COL_VRF_NAME, BatfishObjectMapper.mapper().valueToTree(info.getVrfName()));
    return new Row(data);
  }
}
