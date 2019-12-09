package org.batfish.question.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.vendor_family.f5_bigip.F5BigipFamily;
import org.batfish.datamodel.vendor_family.f5_bigip.Pool;
import org.batfish.datamodel.vendor_family.f5_bigip.PoolMember;
import org.batfish.datamodel.vendor_family.f5_bigip.Virtual;
import org.batfish.datamodel.vendor_family.f5_bigip.VirtualAddress;

public class F5BigipVipConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_DESCRIPTION = "Description";
  public static final String COL_SERVERS = "Servers";
  public static final String COL_VIRTUAL_ENDPOINT = "VIP_Endpoint";
  public static final String COL_VIRTUAL_NAME = "VIP_Name";
  private static final List<String> COLUMN_ORDER =
      ImmutableList.of(
          COL_NODE, COL_VIRTUAL_NAME, COL_VIRTUAL_ENDPOINT, COL_SERVERS, COL_DESCRIPTION);

  /**
   * Creates {@link ColumnMetadata}s that the answer should have.
   *
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static List<ColumnMetadata> createColumnMetadata() {
    Map<String, ColumnMetadata> columnMetadataMap =
        ImmutableMap.<String, ColumnMetadata>builder()
            .put(COL_NODE, new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
            .put(
                COL_DESCRIPTION,
                new ColumnMetadata(COL_DESCRIPTION, Schema.STRING, "Description", true, false))
            .put(
                COL_SERVERS,
                new ColumnMetadata(COL_SERVERS, Schema.set(Schema.STRING), "Servers", false, false))
            .put(
                COL_VIRTUAL_NAME,
                new ColumnMetadata(
                    COL_VIRTUAL_NAME, Schema.STRING, "Virtual Service Name", true, false))
            .put(
                COL_VIRTUAL_ENDPOINT,
                new ColumnMetadata(
                    COL_VIRTUAL_ENDPOINT, Schema.STRING, "Virtual Service Endpoint", true, false))
            .build();

    // List the metadatas in order, with any unknown columns tacked onto the end of the table
    return COLUMN_ORDER.stream()
        .map(columnMetadataMap::get)
        .collect(ImmutableList.toImmutableList());
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(F5BigipVipConfigurationQuestion question) {
    String textDesc =
        String.format(
            "Configuration of Virtual IP/Server ${%s}: ${%s}: ${%s}",
            COL_NODE, COL_VIRTUAL_NAME, COL_VIRTUAL_ENDPOINT);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(), textDesc);
  }

  @VisibleForTesting
  public static Multiset<Row> getAnswerRows(
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columnMetadata) {
    Multiset<Row> rows = HashMultiset.create();
    for (String nodeName : nodes) {
      F5BigipFamily f5 = configurations.get(nodeName).getVendorFamily().getF5Bigip();
      if (f5 == null) {
        continue;
      }
      Node node = new Node(nodeName);
      for (Virtual virtual : f5.getVirtuals().values()) {
        String destination = virtual.getDestination();
        VirtualAddress virtualAddress = f5.getVirtualAddresses().get(destination);
        if (virtualAddress == null) {
          // undefined reference
          continue;
        }
        Integer destinationPort = virtual.getDestinationPort();
        if (destinationPort == null) {
          // malformed
          continue;
        }
        Ip destinationAddress = virtualAddress.getAddress();
        if (destinationAddress == null) {
          // IPv6 or malformed
          continue;
        }
        IpProtocol protocol = virtual.getIpProtocol();
        if (protocol == null) {
          // malformed or unsupported
          continue;
        }
        String virtualEndpointStr =
            String.format("%s:%d %s", destinationAddress, destinationPort, protocol.name());
        Pool pool = f5.getPools().get(virtual.getPool());
        Set<String> servers = toServers(pool);
        String description =
            firstNonNull(
                virtual.getDescription(),
                Optional.ofNullable(pool).map(Pool::getDescription).orElse(""));
        rows.add(
            getRow(
                node, virtual.getName(), virtualEndpointStr, servers, description, columnMetadata));
      }
    }
    return rows;
  }

  private static Row getRow(
      Node node,
      String virtualName,
      String virtualEndpoint,
      Set<String> servers,
      String description,
      Map<String, ColumnMetadata> columnMetadata) {
    return Row.builder(columnMetadata)
        .put(COL_NODE, node)
        .put(COL_VIRTUAL_NAME, virtualName)
        .put(COL_VIRTUAL_ENDPOINT, virtualEndpoint)
        .put(COL_SERVERS, servers)
        .put(COL_DESCRIPTION, description)
        .build();
  }

  private static @Nullable String toServer(PoolMember member) {
    Ip address = member.getAddress();
    if (address == null) {
      // IPv6 or malformed
      return null;
    }
    return String.format("%s:%d", address, member.getPort());
  }

  private static @Nonnull Set<String> toServers(Pool pool) {
    if (pool == null) {
      // undefined reference
      return ImmutableSet.of();
    }
    return pool.getMembers().values().stream()
        .map(member -> toServer(member))
        .filter(Objects::nonNull)
        .collect(ImmutableSet.toImmutableSet());
  }

  public F5BigipVipConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    F5BigipVipConfigurationQuestion question = (F5BigipVipConfigurationQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations(snapshot);
    Set<String> nodes = question.getNodesSpecifier().resolve(_batfish.specifierContext(snapshot));
    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    Multiset<Row> propertyRows = getAnswerRows(configurations, nodes, tableMetadata.toColumnMap());
    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }
}
