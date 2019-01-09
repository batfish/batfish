package org.batfish.question.vxlanproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.Vrf;
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
          Map<String, Vrf> vrfs = configurations.get(n).getVrfs();
          vrfs.forEach(vrf -> {});

          VniSettings vniSettings = configurations.get(n).getVniSettings();
          rows.add(getRow(n /*, vniSettings*/));
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
  static Row getRow(String node /*, VniSettings vniSettings*/) {
    Row.TypedRowBuilder builder = Row.builder(TABLE_METADATA.toColumnMap());
    builder.put(COL_VNI, 10007);
    builder.put(COL_NODE, node);
    builder.put(COL_VLAN, 7);
    builder.put(COL_VTEP_ADDRESS, Ip.parse("1.2.3.4"));
    builder.put(COL_REMOTE_VTEP_MULTICAST_GROUP, null);
    builder.put(
        COL_REMOTE_VTEP_UNICAST_ADDRESSES,
        ImmutableSet.of(Ip.parse("2.2.2.2"), Ip.parse("2.2.2.3")));
    builder.put(COL_VXLAN_UDP_PORT, 2345);
    return builder.build();
  }

  static final String COL_NODE = "Node";
  static final String COL_REMOTE_VTEP_MULTICAST_GROUP = "Remote_Vtep_Multicast_Address";
  static final String COL_REMOTE_VTEP_UNICAST_ADDRESSES = "Remote_Vtep_Unicast_Addresses";
  static final String COL_VLAN = "Vlan";
  static final String COL_VNI = "Vni";
  static final String COL_VTEP_ADDRESS = "Vtep_Address";
  static final String COL_VXLAN_UDP_PORT = "Vxlan_Udp_Port";

  static final String COL_CONVERT_STATUS = "Status";

  private static final List<ColumnMetadata> METADATA =
      ImmutableList.of(
          new ColumnMetadata(
              COL_VNI, Schema.INTEGER, "VXLAN network segment identifier", true, false),
          new ColumnMetadata(
              COL_NODE, Schema.STRING, "The node containing this VXLAN VNI", true, false),
          new ColumnMetadata(
              COL_VLAN, Schema.INTEGER, "VLAN ID associated with this VNI", false, true),
          new ColumnMetadata(
              COL_VTEP_ADDRESS, Schema.IP, "Source address for this VTEP", false, true),
          new ColumnMetadata(
              COL_REMOTE_VTEP_MULTICAST_GROUP,
              Schema.IP,
              "VXLAN network remote VTEP multicast group",
              false,
              true),
          new ColumnMetadata(
              COL_REMOTE_VTEP_UNICAST_ADDRESSES,
              Schema.set(Schema.IP),
              "VXLAN network remote VTEP IP addresses",
              false,
              true),
          new ColumnMetadata(
              COL_VXLAN_UDP_PORT, Schema.INTEGER, "UDP port for this VXLAN tunnel", false, true));

  private static final String TEXT_DESC =
      String.format(
          "VXLAN segment ${%s} corresponding to VLAN ${%s} on node ${%s}",
          COL_VNI, COL_VLAN, COL_NODE);

  private static final TableMetadata TABLE_METADATA = new TableMetadata(METADATA, TEXT_DESC);
}
