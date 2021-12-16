package org.batfish.question.a10;

import static org.batfish.vendor.a10.representation.A10Conversion.isVirtualServerEnabled;
import static org.batfish.vendor.a10.representation.A10Conversion.isVirtualServerPortEnabled;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.ServerPort;
import org.batfish.vendor.a10.representation.ServerTargetVisitor;
import org.batfish.vendor.a10.representation.ServiceGroup;
import org.batfish.vendor.a10.representation.VirtualServer;
import org.batfish.vendor.a10.representation.VirtualServerPort;
import org.batfish.vendor.a10.representation.VirtualServerTargetAddress;
import org.batfish.vendor.a10.representation.VirtualServerTargetVisitor;

public class A10VirtualServerConfigurationAnswerer extends Answerer {

  // ordered based on how they are in the table
  public static final String COL_NODE = "Node";
  public static final String COL_VIRTUAL_SERVER_NAME = "Virtual_Server_Name";
  public static final String COL_VIRTUAL_SERVER_IP = "Virtual_Server_IP";
  public static final String COL_VIRTUAL_SERVER_ENABLED = "Virtual_Server_Enabled";
  public static final String COL_VIRTUAL_SERVER_PORT = "Virtual_Server_Port";
  public static final String COL_VIRTUAL_SERVER_PORT_ENABLED = "Virtual_Server_Port_Enabled";
  public static final String COL_VIRTUAL_SERVER_TYPE = "Virtual_Server_Type";
  public static final String COL_VIRTUAL_SERVER_PORT_TYPE_NAME = "Virtual_Server_Port_Type_Name";
  public static final String COL_SERVICE_GROUP_NAME = "Service_Group_Name";
  public static final String COL_SERVICE_GROUP_TYPE = "Service_Group_Type";
  public static final String COL_SERVERS = "Servers";
  public static final String COL_SOURCE_NAT_POOL_NAME = "Source_NAT_Pool_Name";

  /**
   * Creates {@link ColumnMetadata}s that the answer should have.
   *
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static List<ColumnMetadata> createColumnMetadata() {
    return ImmutableList.<ColumnMetadata>builder()
        .add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_NAME, Schema.STRING, "Virtual Server Name", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_ENABLED, Schema.BOOLEAN, "Virtual Server Enabled", true, false))
        .add(new ColumnMetadata(COL_VIRTUAL_SERVER_IP, Schema.IP, "Virtual Server IP", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_PORT, Schema.INTEGER, "Virtual Server Port", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_PORT_ENABLED,
                Schema.BOOLEAN,
                "Virtual Server Port Enabled",
                true,
                false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_TYPE, Schema.STRING, "Virtual Server Type", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_SERVER_PORT_TYPE_NAME,
                Schema.STRING,
                "Virtual Server Port Type Name",
                false,
                true))
        .add(
            new ColumnMetadata(
                COL_SERVICE_GROUP_NAME, Schema.STRING, "Service Group Name", false, true))
        .add(
            new ColumnMetadata(
                COL_SERVICE_GROUP_TYPE, Schema.STRING, "Service Group Type", false, true))
        .add(
            new ColumnMetadata(
                COL_SERVERS, Schema.set(Schema.list(Schema.STRING)), "Servers", false, true))
        .add(
            new ColumnMetadata(
                COL_SOURCE_NAT_POOL_NAME, Schema.STRING, "Source NAT Pool Name", false, true))
        .build();
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(A10VirtualServerConfigurationQuestion question) {
    String textDesc =
        String.format(
            "Configuration of Virtual Server ${%s}: ${%s}: ${%s} : ${%s}",
            COL_NODE, COL_VIRTUAL_SERVER_NAME, COL_VIRTUAL_SERVER_PORT, COL_SERVICE_GROUP_TYPE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(), textDesc);
  }

  public static Multiset<Row> getAnswerRows(
      Map<String, VendorConfiguration> vendorConfigurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columnMetadata) {
    Multiset<Row> rows = HashMultiset.create();
    for (String nodeName : nodes) {
      VendorConfiguration vc = vendorConfigurations.get(nodeName);
      if (!(vc instanceof A10Configuration)) {
        continue;
      }
      A10Configuration a10Vc = (A10Configuration) vc;
      Node node = new Node(nodeName);
      for (VirtualServer virtualServer : a10Vc.getVirtualServers().values()) {
        for (VirtualServerPort virtualServerPort : virtualServer.getPorts().values()) {
          String serviceGroupName = virtualServerPort.getServiceGroup();
          ServiceGroup serviceGroup =
              serviceGroupName == null ? null : a10Vc.getServiceGroups().get(serviceGroupName);
          Set<List<String>> servers =
              serviceGroup == null
                  ? ImmutableSet.of()
                  : serviceGroup.getMembers().values().stream()
                      .map(
                          member ->
                              ImmutableList.of(
                                  member.getName(),
                                  Integer.toString(member.getPort()),
                                  getServerTarget(member.getName(), a10Vc)))
                      .collect(ImmutableSet.toImmutableSet());
          rows.add(
              getRow(
                  node,
                  virtualServer.getName(),
                  isVirtualServerEnabled(virtualServer),
                  ((VirtualServerTargetVisitor<Ip>) VirtualServerTargetAddress::getAddress)
                      .visit(virtualServer.getTarget()),
                  virtualServerPort.getNumber(),
                  isVirtualServerPortEnabled(virtualServerPort),
                  virtualServerPort.getType(),
                  virtualServerPort.getName(),
                  serviceGroupName,
                  serviceGroup == null ? null : serviceGroup.getType(),
                  servers,
                  virtualServerPort.getSourceNat(),
                  columnMetadata));
        }
      }
    }
    return rows;
  }

  private static String getServerTarget(String serverName, A10Configuration a10Vc) {
    return Optional.ofNullable(a10Vc.getServers().get(serverName))
        .map(
            server ->
                ((ServerTargetVisitor<String>) address -> address.getAddress().toString())
                    .visit(server.getTarget()))
        .orElse("Undefined");
  }

  private static Row getRow(
      Node node,
      String virtualServerName,
      boolean virtualServerEnabled,
      Ip virtualServerIp,
      int virtualServerPort,
      boolean virtualServerPortEnabled,
      VirtualServerPort.Type virtualServerType,
      String virtualServerPortTypeName,
      @Nullable String serviceGroupName,
      @Nullable ServerPort.Type serviceGroupType,
      Set<List<String>> servers,
      String sourceNatPoolName,
      Map<String, ColumnMetadata> columnMetadata) {
    return Row.builder(columnMetadata)
        .put(COL_NODE, node)
        .put(COL_VIRTUAL_SERVER_NAME, virtualServerName)
        .put(COL_VIRTUAL_SERVER_ENABLED, virtualServerEnabled)
        .put(COL_VIRTUAL_SERVER_IP, virtualServerIp)
        .put(COL_VIRTUAL_SERVER_PORT, virtualServerPort)
        .put(COL_VIRTUAL_SERVER_PORT_ENABLED, virtualServerPortEnabled)
        .put(COL_VIRTUAL_SERVER_TYPE, virtualServerType)
        .put(COL_VIRTUAL_SERVER_PORT_TYPE_NAME, virtualServerPortTypeName)
        .put(COL_SERVICE_GROUP_NAME, serviceGroupName)
        .put(COL_SERVICE_GROUP_TYPE, serviceGroupType)
        .put(COL_SERVERS, servers)
        .put(COL_SOURCE_NAT_POOL_NAME, sourceNatPoolName)
        .build();
  }

  public A10VirtualServerConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    A10VirtualServerConfigurationQuestion question =
        (A10VirtualServerConfigurationQuestion) _question;
    Map<String, VendorConfiguration> vendorConfigurations =
        _batfish.loadVendorConfigurations(snapshot);
    Set<String> nodes = question.getNodeSpecifier().resolve(_batfish.specifierContext(snapshot));
    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    Multiset<Row> propertyRows =
        getAnswerRows(vendorConfigurations, nodes, tableMetadata.toColumnMap());
    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }
}
