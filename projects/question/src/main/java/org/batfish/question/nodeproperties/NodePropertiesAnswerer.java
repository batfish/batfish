package org.batfish.question.nodeproperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.NodePropertySpecifier;
import org.batfish.datamodel.questions.NodePropertySpecifier.PropertyDescriptor;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class NodePropertiesAnswerer extends Answerer {

  public static final String COL_NODE = "node";

  public NodePropertiesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /**
   * Creates a {@link TableMetadata} object from the question.
   *
   * @param question The question
   * @return The resulting {@link TableMetadata} object
   */
  static TableMetadata createMetadata(NodePropertiesQuestion question) {
    List<ColumnMetadata> columnMetadata =
        new ImmutableList.Builder<ColumnMetadata>()
            .add(new ColumnMetadata(COL_NODE, Schema.NODE, "Node", true, false))
            .addAll(
                question
                    .getPropertySpec()
                    .getMatchingProperties()
                    .stream()
                    .map(
                        prop ->
                            new ColumnMetadata(
                                prop,
                                NodePropertySpecifier.JAVA_MAP.get(prop).getSchema(),
                                "Property " + prop,
                                false,
                                true))
                    .collect(Collectors.toList()))
            .build();

    DisplayHints dhints = question.getDisplayHints();
    if (dhints == null) {
      dhints = new DisplayHints();
      dhints.setTextDesc(String.format("Properties of node ${%s}.", COL_NODE));
    }
    return new TableMetadata(columnMetadata, dhints);
  }

  @Override
  public AnswerElement answer() {
    NodePropertiesQuestion question = (NodePropertiesQuestion) _question;
    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> nodes = question.getNodeRegex().getMatchingNodes(_batfish);

    TableMetadata tableMetadata = createMetadata(question);
    TableAnswerElement answer = new TableAnswerElement(tableMetadata);

    Multiset<Row> propertyRows = rawAnswer(question, configurations, nodes);

    answer.postProcessAnswer(question, propertyRows);
    return answer;
  }

  @VisibleForTesting
  static Object convertTypeIfNeeded(Object propertyValue, PropertyDescriptor propertyDescriptor) {

    // for Maps (e.g., routing policies) we use set of keys
    if (propertyValue instanceof Map<?, ?>) {
      propertyValue =
          ((Map<?, ?>) propertyValue)
              .keySet()
              .stream()
              .map(k -> k.toString())
              .collect(Collectors.toSet());
    }

    // check if a conversion to String is needed for complex objects (e.g., VRF)
    if (propertyDescriptor.getSchema().equals(Schema.STRING)
        && propertyValue != null
        && !(propertyValue instanceof String)) {
      if (propertyValue instanceof ComparableStructure) {
        propertyValue = ((ComparableStructure<?>) propertyValue).getName();
      } else {
        propertyValue = propertyValue.toString();
      }
    }

    return propertyValue;
  }

  @VisibleForTesting
  static void fillProperty(Configuration configuration, String property, RowBuilder row) {
    PropertyDescriptor propertyDescriptor = NodePropertySpecifier.JAVA_MAP.get(property);
    Object propertyValue = propertyDescriptor.getGetter().apply(configuration);

    propertyValue = convertTypeIfNeeded(propertyValue, propertyDescriptor);

    String columnName = property;
    fillProperty(columnName, propertyValue, row, propertyDescriptor); // separate for testing
  }

  @VisibleForTesting
  static void fillProperty(
      String columnName,
      Object propertyValue,
      RowBuilder row,
      PropertyDescriptor propertyDescriptor) {
    row.put(columnName, propertyValue);
    // if this barfs, the value cannot be converted to expected Schema
    row.build().get(columnName, propertyDescriptor.getSchema());
  }

  @VisibleForTesting
  static Multiset<Row> rawAnswer(
      NodePropertiesQuestion question,
      Map<String, Configuration> configurations,
      Set<String> nodes) {
    Multiset<Row> rows = HashMultiset.create();

    for (String nodeName : nodes) {
      RowBuilder row = Row.builder().put(COL_NODE, new Node(nodeName));

      for (String property : question.getPropertySpec().getMatchingProperties()) {
        fillProperty(configurations.get(nodeName), property, row);
      }

      rows.add(row.build());
    }

    return rows;
  }
}
