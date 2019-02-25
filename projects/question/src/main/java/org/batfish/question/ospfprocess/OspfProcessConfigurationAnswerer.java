package org.batfish.question.ospfprocess;

import static org.batfish.datamodel.questions.OspfPropertySpecifier.AREAS;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.AREA_BORDER_ROUTER;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.EXPORT_POLICY;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.EXPORT_POLICY_SOURCES;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.REFERENCE_BANDWIDTH;
import static org.batfish.datamodel.questions.OspfPropertySpecifier.ROUTER_ID;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.OspfPropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier;
import org.batfish.datamodel.questions.PropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class OspfProcessConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_VRF = "VRF";
  public static final String COL_PROCESS_ID = "Process_ID";

  // this list also ensures order of columns excluding keys
  private static final List<String> COLUMNS_FROM_PROP_SPEC =
      ImmutableList.of(
          AREAS,
          REFERENCE_BANDWIDTH,
          ROUTER_ID,
          EXPORT_POLICY_SOURCES,
          EXPORT_POLICY,
          AREA_BORDER_ROUTER);

  public OspfProcessConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    OspfProcessConfigurationQuestion question = (OspfProcessConfigurationQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);

    Set<String> matchingProperties =
        ImmutableSet.copyOf(question.getProperties().getMatchingProperties());
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
        getRows(orderedProperties, configurations, nodes, tableMetadata.toColumnMap());

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  public static List<ColumnMetadata> createColumnMetadata(List<String> properties) {
    List<ColumnMetadata> columnMetadatas = new ArrayList<>();
    columnMetadatas.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false));
    columnMetadatas.add(new ColumnMetadata(COL_VRF, Schema.STRING, "VRF", true, false));
    columnMetadatas.add(
        new ColumnMetadata(COL_PROCESS_ID, Schema.STRING, "Process ID", true, false));
    for (String property : properties) {
      columnMetadatas.add(
          new ColumnMetadata(
              property,
              OspfPropertySpecifier.JAVA_MAP.get(property).getSchema(),
              "Property " + property,
              false,
              true));
    }
    return columnMetadatas;
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(
      @Nullable String textDescription, List<String> propertiesList) {
    return new TableMetadata(
        createColumnMetadata(propertiesList),
        textDescription == null
            ? String.format(
                "Properties of BGP peer ${%s}:${%s}: ${%s}", COL_NODE, COL_VRF, COL_PROCESS_ID)
            : textDescription);
  }

  /**
   * Returns a set of rows that contain the values of all the properties denoted by {@code
   * propertySpecifier}.
   */
  public static Multiset<Row> getRows(
      List<String> properties,
      Map<String, Configuration> configurations,
      Set<String> nodes,
      Map<String, ColumnMetadata> columnMetadata) {

    Multiset<Row> rows = HashMultiset.create();
    nodes.forEach(
        nodeName -> {
          configurations
              .get(nodeName)
              .getVrfs()
              .values()
              .forEach(
                  vrf -> {
                    OspfProcess ospfProcess = vrf.getOspfProcess();
                    if (ospfProcess == null) {
                      return;
                    }
                    RowBuilder rowBuilder =
                        Row.builder(columnMetadata)
                            .put(COL_NODE, new Node(nodeName))
                            .put(COL_VRF, vrf.getName())
                            .put(COL_PROCESS_ID, ospfProcess.getProcessId());

                    for (String property : properties) {
                      PropertyDescriptor<OspfProcess> propertyDescriptor =
                          OspfPropertySpecifier.JAVA_MAP.get(property);
                      try {
                        PropertySpecifier.fillProperty(
                            propertyDescriptor, ospfProcess, property, rowBuilder);
                      } catch (ClassCastException e) {
                        throw new BatfishException(
                            String.format(
                                "Type mismatch between property value ('%s') and Schema ('%s') for property '%s' for OSPF process '%s->%s-%s': %s",
                                propertyDescriptor.getGetter().apply(ospfProcess),
                                propertyDescriptor.getSchema(),
                                property,
                                nodeName,
                                vrf.getName(),
                                ospfProcess,
                                e.getMessage()),
                            e);
                      }
                    }

                    rows.add(rowBuilder.build());
                  });
        });
    return rows;
  }
}
