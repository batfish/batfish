package org.batfish.question.namedstructures;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
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
import org.batfish.datamodel.questions.NamedStructureSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class NamedStructuresAnswerer extends Answerer {

  public static final String COL_NODE = "Node";

  public NamedStructuresAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates {@link ColumnMetadata}s that the answer should have based on the {@code
   * namedStructureSpecifier}.
   *
   * <p>The {@link NamedStructureSpecifier} that describes the set of named structures
   *
   * @return The {@link List} of {@link ColumnMetadata}s
   */
  public static TableMetadata createNamedStructuresMetadata(
      NamedStructuresQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes) {
    ImmutableList.Builder<ColumnMetadata> columnMetadataList = ImmutableList.builder();
    columnMetadataList.add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false));

    for (String nodeName : nodes) {
      for (String namedStructure : question.getProperties().getMatchingProperties()) {
        Object namedStructureValues =
            NamedStructureSpecifier.JAVA_MAP
                .get(namedStructure)
                .getGetter()
                .apply(configurations.get(nodeName));
        if (namedStructureValues != null
            && ((namedStructureValues instanceof Map<?, ?>)
                && !((Map<?, ?>) namedStructureValues).isEmpty())) {

          for (Map.Entry<?, ?> namedStructureEntry :
              ((Map<?, ?>) namedStructureValues).entrySet()) {
            String namedStructureEntryKey = namedStructureEntry.getKey().toString();

            Schema columnSchema = Schema.OBJECT;
            String finalNameStructureEntry = namedStructure + ":" + namedStructureEntryKey;

            if (!columnMetadataList
                .build()
                .stream()
                .anyMatch(
                    columnMetadata -> columnMetadata.getName().equals(finalNameStructureEntry))) {

              columnMetadataList.add(
                  new ColumnMetadata(
                      finalNameStructureEntry,
                      columnSchema,
                      namedStructureEntryKey,
                      Boolean.FALSE,
                      Boolean.TRUE));
            }
          }
        }
      }
    }

    String textDesc = String.format("Properties of node ${%s}.", COL_NODE);
    DisplayHints dhints = question.getDisplayHints();
    if (dhints != null && dhints.getTextDesc() != null) {
      textDesc = dhints.getTextDesc();
    }
    return new TableMetadata(columnMetadataList.build(), textDesc);
  }

  @VisibleForTesting
  static Multiset<Row> rawNamedStructuresAnswer(
      NamedStructuresQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes,
      TableMetadata tableMetadata) {

    Multiset<Row> rows = HashMultiset.create();
    Map<String, ColumnMetadata> columns = tableMetadata.toColumnMap();
    for (String nodeName : nodes) {
      RowBuilder row = Row.builder(columns);
      row.put(COL_NODE, new Node(nodeName));

      for (Map.Entry<String, ColumnMetadata> columnEntry : columns.entrySet()) {
        String columnName = columnEntry.getKey();
        if (columnName.equalsIgnoreCase(COL_NODE)) {
          continue;
        }

        String[] columnSplits = columnName.split(":", 2);
        String actualNameStructureType = columnSplits[0];

        Object namedStructureValues =
            NamedStructureSpecifier.JAVA_MAP
                .get(actualNameStructureType)
                .getGetter()
                .apply(configurations.get(nodeName));
        if (namedStructureValues != null) {

          if ((namedStructureValues instanceof Map<?, ?>)
              && !((Map<?, ?>) namedStructureValues).isEmpty()) {
            for (Map.Entry<?, ?> namedStructureEntry :
                ((Map<?, ?>) namedStructureValues).entrySet()) {
              Object namedStructutureEntryValue = namedStructureEntry.getValue();
              row.put(columnName, namedStructutureEntryValue);
            }
          } else {
            /* To fill the missing cells in the answer Table. */
            row.put(columnName, null);
          }
        }
      }
      rows.add(row.build());
    }
    return rows;
  }

  @Override
  public AnswerElement answer() {
    NamedStructuresQuestion question = (NamedStructuresQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodes().getMatchingNodes(_batfish);

    TableMetadata tableMetadata = createNamedStructuresMetadata(question, configurations, nodes);

    Multiset<Row> propertyRows =
        rawNamedStructuresAnswer(question, configurations, nodes, tableMetadata);

    TableAnswerElement answer = new TableAnswerElement(tableMetadata);
    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }
}
