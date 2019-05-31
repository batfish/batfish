package org.batfish.question.vxlanproperties;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.VxlanVniPropertiesRow;
import org.batfish.datamodel.questions.VxlanVniPropertySpecifier;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

/** Implements {@link VxlanVniPropertiesQuestion}. */
public final class VxlanVniPropertiesAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VNI = "VNI";

  /**
   * Creates {@link ColumnMetadata}s that the answer should have based on the {@code
   * propertySpecifier}.
   *
   * @param propertySpecifier The {@link VxlanVniPropertySpecifier} that describes the set of
   *     properties
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static List<ColumnMetadata> createColumnMetadata(
      VxlanVniPropertySpecifier propertySpecifier) {
    return ImmutableList.<ColumnMetadata>builder()
        .add(new ColumnMetadata(COL_NODE, Schema.STRING, "Node", true, false))
        .add(new ColumnMetadata(COL_VNI, Schema.INTEGER, "VXLAN Segment ID", true, false))
        .addAll(
            propertySpecifier.getMatchingProperties().stream()
                .map(
                    prop ->
                        new ColumnMetadata(
                            prop,
                            VxlanVniPropertySpecifier.JAVA_MAP.get(prop).getSchema(),
                            VxlanVniPropertySpecifier.JAVA_MAP.get(prop).getDescription(),
                            false,
                            true))
                .collect(Collectors.toList()))
        .build();
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  private static TableMetadata createTableMetadata(VxlanVniPropertiesQuestion question) {
    String textDesc =
        String.format("Properties of VXLAN VNI ${%s} on node ${%s}.", COL_VNI, COL_NODE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(createColumnMetadata(question.getProperties()), textDesc);
  }

  @Override
  public TableAnswerElement answer() {
    VxlanVniPropertiesQuestion question = (VxlanVniPropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodeSpecifier().resolve(_batfish.specifierContext());

    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows =
        getProperties(question.getProperties(), configurations, nodes, tableMetadata.toColumnMap());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  /**
   * Gets properties of VXLAN VNIs.
   *
   * @param propertySpecifier Specifies which properties to get
   * @param configurations configuration to use in extractions
   * @param nodes the set of nodes to consider
   * @param columns a map from column name to {@link ColumnMetadata}
   * @return A multiset of {@link Row}s where each row corresponds to a node and columns correspond
   *     to property values.
   */
  public static Multiset<Row> getProperties(
      VxlanVniPropertySpecifier propertySpecifier,
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columns) {
    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      for (Vrf vrf : configurations.get(nodeName).getVrfs().values()) {
        vrf.getVniSettings()
            .forEach(
                (vni, vniSettings) -> {
                  RowBuilder row = Row.builder(columns).put(COL_NODE, nodeName).put(COL_VNI, vni);
                  boolean unicast =
                      vniSettings.getBumTransportMethod() == BumTransportMethod.UNICAST_FLOOD_GROUP;
                  SortedSet<Ip> bumTransportIps = vniSettings.getBumTransportIps();
                  VxlanVniPropertiesRow vxlanVniProperties =
                      new VxlanVniPropertiesRow(
                          nodeName,
                          vni,
                          vniSettings.getVlan(),
                          vniSettings.getSourceAddress(),
                          unicast
                              ? null
                              : bumTransportIps.isEmpty() ? null : bumTransportIps.first(),
                          unicast ? bumTransportIps : null,
                          vniSettings.getUdpPort());

                  for (String property : propertySpecifier.getMatchingProperties()) {
                    PropertyDescriptor<VxlanVniPropertiesRow> propertyDescriptor =
                        VxlanVniPropertySpecifier.JAVA_MAP.get(property);
                    try {
                      PropertySpecifier.fillProperty(
                          propertyDescriptor, vxlanVniProperties, property, row);
                    } catch (ClassCastException e) {
                      throw new BatfishException(
                          String.format(
                              "Type mismatch between property value ('%s') and Schema ('%s') for property '%s' for VXLAN VNI settings '%s': %s",
                              propertyDescriptor.getGetter().apply(vxlanVniProperties),
                              propertyDescriptor.getSchema(),
                              property,
                              vxlanVniProperties,
                              e.getMessage()),
                          e);
                    }
                  }

                  rows.add(row.build());
                });
      }
    }
    return rows;
  }

  public VxlanVniPropertiesAnswerer(VxlanVniPropertiesQuestion question, IBatfish batfish) {
    super(question, batfish);
  }
}
