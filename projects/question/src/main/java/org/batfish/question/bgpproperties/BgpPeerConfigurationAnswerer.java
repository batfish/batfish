package org.batfish.question.bgpproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class BgpPeerConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VRF = "VRF";
  public static final String COL_LOCAL_AS = "Local_AS";
  public static final String COL_REMOTE_AS = "Remote_AS";
  public static final String COL_LOCAL_IP = "Local_IP";
  public static final String COL_REMOTE_IP = "Remote_IP";
  public static final String COL_ROUTE_REFLECTOR_CLIENT = "Route_Reflector_Client";
  public static final String COL_PEER_GROUP = "Peer_Group";
  public static final String COL_IMPORT_POLICY = "Import_Policy";
  public static final String COL_EXPORT_POLICY = "Export_Policy";
  public static final String COL_SEND_COMMUNITY = "Send_Community";

  public BgpPeerConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates {@link ColumnMetadata}s that the answer should have.
   *
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static List<ColumnMetadata> createColumnMetadata() {
    return ImmutableList.<ColumnMetadata>builder()
        .add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
        .add(new ColumnMetadata(COL_VRF, Schema.STRING, "VRF", true, false))
        .add(new ColumnMetadata(COL_LOCAL_AS, Schema.LONG, "Local AS", false, false))
        .add(new ColumnMetadata(COL_LOCAL_IP, Schema.IP, "Local IP", false, false))
        .add(new ColumnMetadata(COL_REMOTE_AS, Schema.SELF_DESCRIBING, "Remote AS", false, false))
        .add(new ColumnMetadata(COL_REMOTE_IP, Schema.SELF_DESCRIBING, "Remote IP", true, false))
        .add(
            new ColumnMetadata(
                COL_ROUTE_REFLECTOR_CLIENT, Schema.BOOLEAN, "Route reflector client", false, false))
        .add(new ColumnMetadata(COL_PEER_GROUP, Schema.STRING, "Peer group", false, false))
        .add(
            new ColumnMetadata(
                COL_IMPORT_POLICY, Schema.set(Schema.STRING), "Import policy", false, false))
        .add(
            new ColumnMetadata(
                COL_EXPORT_POLICY, Schema.set(Schema.STRING), "Export policy", false, false))
        .add(new ColumnMetadata(COL_SEND_COMMUNITY, Schema.BOOLEAN, "Send community", false, false))
        .build();
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(BgpPeerConfigurationQuestion question) {
    String textDesc =
        String.format(
            "Properties of BGP peer ${%s}:${%s}: ${%s} to ${%s}",
            COL_NODE, COL_VRF, COL_LOCAL_IP, COL_REMOTE_IP);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(), textDesc);
  }

  @Override
  public AnswerElement answer() {
    BgpPeerConfigurationQuestion question = (BgpPeerConfigurationQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodesSpecifier().resolve(_batfish.specifierContext());

    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows = getAnswerRows(configurations, nodes, tableMetadata.toColumnMap());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  @VisibleForTesting
  public static Multiset<Row> getAnswerRows(
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columnMetadata) {

    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      for (Vrf vrf : configurations.get(nodeName).getVrfs().values()) {
        BgpProcess bgpProcess = vrf.getBgpProcess();
        if (bgpProcess == null) {
          continue;
        }
        Node node = new Node(nodeName);
        for (BgpActivePeerConfig peer : bgpProcess.getActiveNeighbors().values()) {
          RowBuilder rowBuilder =
              Row.builder(columnMetadata)
                  .put(COL_NODE, node)
                  .put(COL_VRF, vrf.getName())
                  .put(COL_LOCAL_AS, peer.getLocalAs())
                  .put(COL_REMOTE_AS, new SelfDescribingObject(Schema.LONG, peer.getRemoteAs()))
                  .put(COL_LOCAL_IP, peer.getLocalIp())
                  .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.IP, peer.getPeerAddress()))
                  .put(COL_ROUTE_REFLECTOR_CLIENT, peer.getRouteReflectorClient())
                  .put(COL_PEER_GROUP, peer.getGroup())
                  .put(COL_IMPORT_POLICY, peer.getImportPolicySources())
                  .put(COL_EXPORT_POLICY, peer.getExportPolicySources())
                  .put(COL_SEND_COMMUNITY, peer.getSendCommunity());
          rows.add(rowBuilder.build());
        }
        for (BgpPassivePeerConfig peer : bgpProcess.getPassiveNeighbors().values()) {
          RowBuilder rowBuilder =
              Row.builder(columnMetadata)
                  .put(COL_NODE, node)
                  .put(COL_VRF, vrf.getName())
                  .put(COL_LOCAL_AS, peer.getLocalAs())
                  .put(
                      COL_REMOTE_AS,
                      new SelfDescribingObject(Schema.list(Schema.LONG), peer.getRemoteAs()))
                  .put(COL_LOCAL_IP, peer.getLocalIp())
                  .put(COL_REMOTE_IP, new SelfDescribingObject(Schema.PREFIX, peer.getPeerPrefix()))
                  .put(COL_ROUTE_REFLECTOR_CLIENT, peer.getRouteReflectorClient())
                  .put(COL_PEER_GROUP, peer.getGroup())
                  .put(COL_IMPORT_POLICY, peer.getImportPolicySources())
                  .put(COL_EXPORT_POLICY, peer.getExportPolicySources())
                  .put(COL_SEND_COMMUNITY, peer.getSendCommunity());
          rows.add(rowBuilder.build());
        }
      }
    }
    return rows;
  }
}
