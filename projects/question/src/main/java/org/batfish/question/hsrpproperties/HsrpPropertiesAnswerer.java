package org.batfish.question.hsrpproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Implements {@link HsrpPropertiesQuestion}. */
public final class HsrpPropertiesAnswerer extends Answerer {

  public static final String COL_INTERFACE = "Interface";
  public static final String COL_GROUP_ID = "Group_Id";
  public static final String COL_VIRTUAL_ADDRESSES = "Virtual_Addresses";
  public static final String COL_SOURCE_ADDRESS = "Source_Address";
  public static final String COL_PRIORITY = "Priority";
  public static final String COL_PREEMPT = "Preempt";
  public static final String COL_ACTIVE = "Active";

  /** Creates {@link ColumnMetadata}s for the answer */
  public static List<ColumnMetadata> createColumnMetadata() {
    return ImmutableList.<ColumnMetadata>builder()
        .add(new ColumnMetadata(COL_INTERFACE, Schema.INTERFACE, "Interface", true, false))
        .add(new ColumnMetadata(COL_GROUP_ID, Schema.INTEGER, "HSRP Group ID", true, false))
        .add(
            new ColumnMetadata(
                COL_VIRTUAL_ADDRESSES, Schema.set(Schema.IP), "Virtual Addresses", false, true))
        .add(
            new ColumnMetadata(
                COL_SOURCE_ADDRESS,
                Schema.STRING,
                "Source Address used for HSRP messages",
                false,
                true))
        .add(new ColumnMetadata(COL_PRIORITY, Schema.INTEGER, "HSRP router priority", false, true))
        .add(
            new ColumnMetadata(
                COL_PREEMPT, Schema.BOOLEAN, "Whether preemption is allowed", false, true))
        .add(
            new ColumnMetadata(
                COL_ACTIVE, Schema.BOOLEAN, "Whether the interface is active", false, true))
        .build();
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  static TableMetadata createTableMetadata(HsrpPropertiesQuestion question) {
    String textDesc =
        String.format(
            "Properties of HSRP group ${%s} on interface ${%s}.", COL_GROUP_ID, COL_INTERFACE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(), textDesc);
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    HsrpPropertiesQuestion question = (HsrpPropertiesQuestion) _question;
    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    Multiset<Row> propertyRows =
        getProperties(
            _batfish.specifierContext(snapshot),
            question.getNodesSpecifier(),
            question.getInterfacesSpecifier(),
            question.getVirtualAddressSpecifier(),
            question.getExcludeShutInterfaces(),
            tableMetadata.toColumnMap());
    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  /**
   * Gets properties of HSRP groups.
   *
   * @return A multiset of {@link Row}s where each row corresponds to a node/vlan pair and columns
   *     correspond to property values.
   */
  @VisibleForTesting
  static Multiset<Row> getProperties(
      SpecifierContext ctxt,
      NodeSpecifier nodeSpecifier,
      InterfaceSpecifier interfaceSpecifier,
      IpSpaceSpecifier virtualAddressSpecifier,
      boolean excludeShutInterfaces,
      Map<String, ColumnMetadata> columns) {
    Multiset<Row> rows = HashMultiset.create();
    Map<String, Configuration> configs = ctxt.getConfigs();
    IpSpace virtualAddressSpace = virtualAddressSpecifier.resolve(ctxt);

    for (String nodeName : nodeSpecifier.resolve(ctxt)) {
      for (NodeInterfacePair ifaceId :
          interfaceSpecifier.resolve(ImmutableSet.of(nodeName), ctxt)) {
        Interface iface =
            configs.get(ifaceId.getHostname()).getAllInterfaces().get(ifaceId.getInterface());
        if (iface.getHsrpGroups().isEmpty()) {
          continue;
        }
        if (excludeShutInterfaces && !iface.getActive()) {
          continue;
        }
        RowBuilder row =
            Row.builder(columns)
                .put(COL_INTERFACE, NodeInterfacePair.of(nodeName, iface.getName()))
                .put(COL_ACTIVE, iface.getActive());
        iface.getHsrpGroups().entrySet().stream()
            .filter(
                e ->
                    e.getValue().getVirtualAddresses().stream()
                        .anyMatch(
                            address -> virtualAddressSpace.containsIp(address, ImmutableMap.of())))
            .forEach(e -> rows.add(populateRow(row, e.getKey(), e.getValue()).build()));
      }
    }
    return rows;
  }

  @VisibleForTesting
  static RowBuilder populateRow(RowBuilder row, Integer id, HsrpGroup group) {
    row.put(COL_GROUP_ID, id)
        // TODO: expose receivingInterface structure
        .put(COL_VIRTUAL_ADDRESSES, ImmutableSortedSet.copyOf(group.getVirtualAddresses()))
        .put(COL_SOURCE_ADDRESS, group.getSourceAddress())
        .put(COL_PRIORITY, group.getPriority())
        .put(COL_PREEMPT, group.getPreempt());
    return row;
  }

  public HsrpPropertiesAnswerer(HsrpPropertiesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }
}
