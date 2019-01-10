package org.batfish.question.vxlanproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link VxlanVniPropertiesQuestion}. */
final class VxlanVniPropertiesAnswerer extends Answerer {
  @Override
  public TableAnswerElement answer() {
    VxlanVniPropertiesQuestion question = (VxlanVniPropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);

    Rows rows = new Rows();
    nodes.forEach(
        n -> {
          configurations
              .get(n)
              .getVrfs()
              .forEach(
                  (vrfName, vrf) -> {
                    vrf.getVniSettings()
                        .forEach(
                            (vni, vniSettings) -> {
                              rows.add(getRow(n, vniSettings));
                            });
                  });
        });

    TableAnswerElement answerElement = new TableAnswerElement(TABLE_METADATA);
    answerElement.postProcessAnswer(_question, rows.getData());
    return answerElement;
  }

  VxlanVniPropertiesAnswerer(VxlanVniPropertiesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }

  @Nonnull
  @VisibleForTesting
  static Row getRow(String node, VniSettings vniSettings) {
    Row.TypedRowBuilder builder = Row.builder(TABLE_METADATA.toColumnMap());
    builder.put(COL_VNI, vniSettings.getVni());
    builder.put(COL_NODE, node);
    builder.put(COL_VLAN, vniSettings.getVlan());
    builder.put(COL_LOCAL_VTEP_IP, vniSettings.getSourceAddress());
    boolean unicast = vniSettings.getBumTransportMethod() == BumTransportMethod.UNICAST_FLOOD_GROUP;
    builder.put(COL_MULTICAST_GROUP, unicast ? null : vniSettings.getBumTransportIps().first());
    builder.put(COL_VTEP_FLOOD_LIST, unicast ? vniSettings.getBumTransportIps() : null);
    builder.put(COL_VXLAN_PORT, vniSettings.getUdpPort());
    return builder.build();
  }

  static final String COL_NODE = "Node";
  static final String COL_LOCAL_VTEP_IP = "Local_VTEP_IP";
  static final String COL_MULTICAST_GROUP = "Multicast_Group";
  static final String COL_VLAN = "VLAN";
  static final String COL_VNI = "VNI";
  static final String COL_VTEP_FLOOD_LIST = "VTEP_Flood_List";
  static final String COL_VXLAN_PORT = "VXLAN_Port";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_VNI, Schema.INTEGER, "VXLAN network segment identifier", true, false),
          new ColumnMetadata(
              COL_NODE, Schema.STRING, "Node containing this VXLAN VNI", true, false),
          new ColumnMetadata(
              COL_VLAN, Schema.INTEGER, "VLAN ID associated with this VNI", false, true),
          new ColumnMetadata(
              COL_LOCAL_VTEP_IP, Schema.IP, "Source IP address for this VTEP", false, true),
          new ColumnMetadata(
              COL_MULTICAST_GROUP,
              Schema.IP,
              "Multicast group used for replication of broadcast and unknown traffic",
              false,
              true),
          new ColumnMetadata(
              COL_VTEP_FLOOD_LIST,
              Schema.set(Schema.IP),
              "List of IP addresses configured for remote VTEP flooding",
              false,
              true),
          new ColumnMetadata(
              COL_VXLAN_PORT, Schema.INTEGER, "UDP port for this VXLAN tunnel", false, true));

  private static final String TEXT_DESC =
      String.format(
          "VXLAN segment ${%s} corresponding to VLAN ${%s} on node ${%s}",
          COL_VNI, COL_VLAN, COL_NODE);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
