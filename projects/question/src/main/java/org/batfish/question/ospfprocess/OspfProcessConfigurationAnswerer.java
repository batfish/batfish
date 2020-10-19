package org.batfish.question.ospfprocess;

import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.AREAS;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.AREA_BORDER_ROUTER;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.EXPORT_POLICY_SOURCES;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.REFERENCE_BANDWIDTH;
import static org.batfish.datamodel.questions.OspfProcessPropertySpecifier.ROUTER_ID;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.OspfProcessPropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContext;

/** Implements {@link OspfProcessConfigurationQuestion}. */
@ParametersAreNonnullByDefault
public final class OspfProcessConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VRF = "VRF";
  public static final String COL_PROCESS_ID = "Process_ID";

  // this list also ensures order of columns excluding keys
  static final List<String> COLUMNS_FROM_PROP_SPEC =
      ImmutableList.of(
          AREAS, REFERENCE_BANDWIDTH, ROUTER_ID, EXPORT_POLICY_SOURCES, AREA_BORDER_ROUTER);

  public OspfProcessConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    OspfProcessConfigurationQuestion question = (OspfProcessConfigurationQuestion) _question;

    Set<String> matchingProperties =
        ImmutableSet.copyOf(question.getPropertySpecifier().getMatchingProperties());
    List<String> orderedProperties =
        COLUMNS_FROM_PROP_SPEC.stream()
            .filter(matchingProperties::contains)
            .collect(ImmutableList.toImmutableList());
    TableMetadata tableMetadata =
        createTableMetadata(
            question.getDisplayHints() != null ? question.getDisplayHints().getTextDesc() : null,
            orderedProperties);

    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows =
        getProperties(
            orderedProperties,
            _batfish.specifierContext(snapshot),
            question.getNodesSpecifier(),
            tableMetadata.toColumnMap());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  public static List<ColumnMetadata> createColumnMetadata(List<String> properties) {
    List<ColumnMetadata> columnMetadatas = new ArrayList<>();
    columnMetadatas.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false));
    columnMetadatas.add(new ColumnMetadata(COL_VRF, Schema.STRING, "VRF name", true, false));
    columnMetadatas.add(
        new ColumnMetadata(COL_PROCESS_ID, Schema.STRING, "Process ID", true, false));
    for (String property : properties) {
      columnMetadatas.add(
          new ColumnMetadata(
              getColumnName(property),
              OspfProcessPropertySpecifier.getPropertyDescriptor(property).getSchema(),
              OspfProcessPropertySpecifier.getPropertyDescriptor(property).getDescription(),
              false,
              true));
    }
    return columnMetadatas;
  }

  /** Creates a {@link TableMetadata} object from the question. */
  @VisibleForTesting
  static TableMetadata createTableMetadata(
      @Nullable String textDescription, List<String> propertiesList) {
    return new TableMetadata(
        createColumnMetadata(propertiesList),
        textDescription == null
            ? String.format(
                "Configuration of OSPF process ${%s}:${%s}: ${%s}",
                COL_NODE, COL_VRF, COL_PROCESS_ID)
            : textDescription);
  }

  public static Multiset<Row> getProperties(
      List<String> properties,
      SpecifierContext ctxt,
      NodeSpecifier nodeSpecifier,
      Map<String, ColumnMetadata> columnMetadata) {

    Multiset<Row> rows = HashMultiset.create();
    nodeSpecifier
        .resolve(ctxt)
        .forEach(
            nodeName -> {
              ctxt.getConfigs()
                  .get(nodeName)
                  .getVrfs()
                  .values()
                  .forEach(
                      vrf -> {
                        for (OspfProcess ospfProcess : vrf.getOspfProcesses().values()) {
                          rows.add(
                              getRow(
                                  nodeName,
                                  vrf.getName(),
                                  ospfProcess,
                                  properties,
                                  columnMetadata));
                        }
                      });
            });
    return rows;
  }

  @VisibleForTesting
  static Row getRow(
      String nodeName,
      String vrfName,
      OspfProcess ospfProcess,
      List<String> properties,
      Map<String, ColumnMetadata> columnMetadataMap) {
    RowBuilder rowBuilder =
        Row.builder(columnMetadataMap)
            .put(COL_NODE, new Node(nodeName))
            .put(COL_VRF, vrfName)
            .put(COL_PROCESS_ID, ospfProcess.getProcessId());

    for (String property : properties) {
      PropertyDescriptor<OspfProcess> propertyDescriptor =
          OspfProcessPropertySpecifier.getPropertyDescriptor(property);
      try {
        PropertySpecifier.fillProperty(propertyDescriptor, ospfProcess, property, rowBuilder);
      } catch (ClassCastException e) {
        throw new BatfishException(
            String.format(
                "Type mismatch between property value ('%s') and Schema ('%s') for property '%s'"
                    + " for OSPF process '%s->%s-%s': %s",
                propertyDescriptor.getGetter().apply(ospfProcess),
                propertyDescriptor.getSchema(),
                property,
                nodeName,
                vrfName,
                ospfProcess,
                e.getMessage()),
            e);
      }
    }
    return rowBuilder.build();
  }

  /** Returns the name of the column that contains the value of property {@code property} */
  public static String getColumnName(String property) {
    return property;
  }
}
