package org.batfish.question.f5_bigip;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.vendor_family.f5_bigip.F5BigipFamily;

public class F5BigipVipConfigurationAnswerer extends Answerer {

  public static final String COL_NODE = "Node";
  public static final String COL_SERVERS = "Servers";
  public static final String COL_VIRTUAL = "Virtual";
  private static final List<String> COLUMN_ORDER =
      ImmutableList.of(COL_NODE, COL_VIRTUAL, COL_SERVERS);

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
                COL_SERVERS,
                new ColumnMetadata(COL_SERVERS, Schema.set(Schema.STRING), "Servers", false, false))
            .put(
                COL_VIRTUAL,
                new ColumnMetadata(COL_VIRTUAL, Schema.STRING, "Virtual IP/Server", true, false))
            .build();

    // List the metadatas in order, with any unknown columns tacked onto the end of the table
    return COLUMN_ORDER.stream()
        .map(columnMetadataMap::get)
        .collect(ImmutableList.toImmutableList());
  }

  /** Creates a {@link TableMetadata} object from the question. */
  static TableMetadata createTableMetadata(F5BigipVipConfigurationQuestion question) {
    String textDesc =
        String.format("Configuration of Virtual IP/Server ${%s}: ${%s}", COL_NODE, COL_VIRTUAL);
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
      rows.add(getRow(node, "", ImmutableSet.of(), columnMetadata));
    }
    return rows;
  }

  /** Returns the name of the column that contains the value of property {@code property} */
  public static String getColumnName(String property) {
    return property;
  }

  private static Row getRow(
      Node node, String virtual, Set<String> servers, Map<String, ColumnMetadata> columnMetadata) {
    return Row.builder(columnMetadata)
        .put(COL_NODE, node)
        .put(COL_VIRTUAL, virtual)
        .put(COL_SERVERS, servers)
        .build();
  }

  public F5BigipVipConfigurationAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    F5BigipVipConfigurationQuestion question = (F5BigipVipConfigurationQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodesSpecifier().resolve(_batfish.specifierContext());
    TableMetadata tableMetadata = createTableMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    Multiset<Row> propertyRows = getAnswerRows(configurations, nodes, tableMetadata.toColumnMap());
    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }
}
